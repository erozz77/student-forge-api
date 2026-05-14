package com.studentforge.controller;

import com.studentforge.dto.response.SubjectResponse;
import com.studentforge.entity.*;
import com.studentforge.enums.AssignmentStatus;
import com.studentforge.mapper.UserMapper;
import com.studentforge.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.studentforge.dto.response.SubjectStatsResponse;
import com.studentforge.entity.Submission;
import com.studentforge.enums.SubmissionStatus;
import com.studentforge.repository.SubmissionRepository;
import com.studentforge.repository.UserRepository;
import java.time.LocalDateTime;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class SubjectController {

    private final SubjectRepository subjectRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final AssignmentRepository assignmentRepository;
    private final UserMapper userMapper;
    private final GradeRepository gradeRepository;
    private final UserRepository userRepository;
    private final SubmissionRepository submissionRepository;

    @GetMapping("/my-subjects")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<List<SubjectResponse>> getMySubjects(Authentication auth) {
        UUID uid = (UUID) auth.getPrincipal();
        return ResponseEntity.ok(enrollmentRepository.findAllByUserId(uid).stream()
                .flatMap(e -> subjectRepository.findAllByGroupId(e.getGroup().getId(), Pageable.unpaged()).stream())
                .map(userMapper::toSubjectResponse).collect(Collectors.toList()));
    }

    @GetMapping("/groups/{groupId}/subjects")
    public ResponseEntity<Page<SubjectResponse>> getGroupSubjects(@PathVariable UUID groupId,
                                                                  @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(subjectRepository.findAllByGroupId(groupId, pageable).map(userMapper::toSubjectResponse));
    }

    @PutMapping("/assignments/{id}/submit")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<String> submit(@PathVariable UUID id,
                                         @RequestParam(value = "file", required = false) MultipartFile file,
                                         Authentication auth) {
        UUID userId = (UUID) auth.getPrincipal();
        Assignment assignment = assignmentRepository.findById(id).orElseThrow();
        Submission submission = submissionRepository.findByAssignmentIdAndUserId(id, userId)
                .orElse(Submission.builder()
                        .assignment(assignment)
                        .user(userRepository.findById(userId).orElseThrow())
                        .submittedAt(LocalDateTime.now())
                        .build());
        if (file != null && !file.isEmpty()) {
            try {
                Path up = Path.of("uploads/submissions");
                Files.createDirectories(up);
                String filename = userId + "_" + id + "_" + file.getOriginalFilename();
                Files.copy(file.getInputStream(), up.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
                submission.setFilePath(filename);
            } catch (IOException e) {
                return ResponseEntity.status(500).body("Ошибка загрузки файла");
            }
        }
        submission.setStatus(SubmissionStatus.SUBMITTED);
        submission.setSubmittedAt(LocalDateTime.now());
        submissionRepository.save(submission);
        return ResponseEntity.ok("Отправлено");
    }

    @PutMapping("/assignments/{id}/cancel-submit")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<String> cancel(@PathVariable UUID id, Authentication auth) {
        UUID userId = (UUID) auth.getPrincipal();
        Submission submission = submissionRepository.findByAssignmentIdAndUserId(id, userId)
                .orElseThrow(() -> new RuntimeException("Отправка не найдена"));
        submission.setStatus(SubmissionStatus.PENDING);
        submission.setFilePath(null);
        submissionRepository.save(submission);
        return ResponseEntity.ok("Отменено");
    }

    @GetMapping("/assignments/{id}/my-status")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getMyAssignmentStatus(@PathVariable UUID id, Authentication auth) {
        UUID userId = (UUID) auth.getPrincipal();
        Optional<Submission> submission = submissionRepository.findByAssignmentIdAndUserId(id, userId);
        Map<String, Object> result = new HashMap<>();
        if (submission.isPresent()) {
            result.put("status", submission.get().getStatus().name());
            result.put("filePath", submission.get().getFilePath());
        } else {
            result.put("status", "PENDING");
            result.put("filePath", null);
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/my-stats")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> myStats(Authentication auth) {
        UUID userId = (UUID) auth.getPrincipal();
        List<UUID> groupIds = enrollmentRepository.findAllByUserId(userId).stream()
                .map(e -> e.getGroup().getId())
                .collect(Collectors.toList());
        List<UUID> subjectIds = subjectRepository.findAll().stream()
                .filter(s -> groupIds.contains(s.getGroup().getId()))
                .map(Subject::getId)
                .collect(Collectors.toList());
        List<Grade> allGrades = gradeRepository.findAllByUserId(userId);
        List<Grade> filteredGrades = allGrades.stream()
                .filter(g -> subjectIds.contains(g.getAssignment().getSubject().getId()))
                .collect(Collectors.toList());
        double avg = filteredGrades.stream().mapToInt(Grade::getScore).average().orElse(0);
        return ResponseEntity.ok(Map.of(
                "averageScore", Math.round(avg * 100.0) / 100.0,
                "totalGrades", filteredGrades.size(),
                "grades", filteredGrades.stream().map(g -> Map.of(
                        "assignment", g.getAssignment().getTitle(),
                        "score", g.getScore(),
                        "subject", g.getAssignment().getSubject().getName()
                )).collect(Collectors.toList())
        ));
    }

    @GetMapping("/my-subjects-stats")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<List<SubjectStatsResponse>> getMySubjectsStats(Authentication auth) {
        UUID userId = (UUID) auth.getPrincipal();
        List<Subject> subjects = enrollmentRepository.findAllByUserId(userId).stream()
                .flatMap(e -> subjectRepository.findAllByGroupId(e.getGroup().getId(), Pageable.unpaged()).stream())
                .collect(Collectors.toList());
        List<UUID> currentSubjectIds = subjects.stream()
                .map(Subject::getId)
                .collect(Collectors.toList());
        List<Grade> allGrades = gradeRepository.findAllByUserId(userId);
        List<Grade> filteredGrades = allGrades.stream()
                .filter(g -> currentSubjectIds.contains(g.getAssignment().getSubject().getId()))
                .collect(Collectors.toList());
        Map<UUID, List<Grade>> gradesBySubject = filteredGrades.stream()
                .collect(Collectors.groupingBy(g -> g.getAssignment().getSubject().getId()));
        List<SubjectStatsResponse> result = subjects.stream().map(subject -> {
            List<Grade> subjectGrades = gradesBySubject.getOrDefault(subject.getId(), List.of());
            double avgScore = subjectGrades.stream()
                    .mapToInt(Grade::getScore)
                    .average()
                    .orElse(0.0);
            List<SubjectStatsResponse.AssignmentGradeDto> assignments = subjectGrades.stream()
                    .map(g -> new SubjectStatsResponse.AssignmentGradeDto(
                            g.getAssignment().getId(),
                            g.getAssignment().getTitle(),
                            g.getScore(),
                            g.getGradedAt().toString()
                    ))
                    .collect(Collectors.toList());
            return new SubjectStatsResponse(
                    subject.getId(),
                    subject.getName(),
                    subject.getTeacherName(),
                    Math.round(avgScore * 100.0) / 100.0,
                    subjectGrades.size(),
                    assignments
            );
        }).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }
}