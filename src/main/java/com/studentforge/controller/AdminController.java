package com.studentforge.controller;

import com.studentforge.entity.*;
import com.studentforge.enums.AssignmentStatus;
import com.studentforge.enums.AssignmentType;
import com.studentforge.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.net.URLEncoder;
import java.io.IOException;
import com.studentforge.enums.Role;
import java.math.BigDecimal;
import java.nio.file.*;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;
import com.studentforge.entity.Submission;
import com.studentforge.enums.SubmissionStatus;
import com.studentforge.repository.SubmissionRepository;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserRepository userRepository;
    private final AcademicPeriodRepository periodRepository;
    private final GroupRepository groupRepository;
    private final SubjectRepository subjectRepository;
    private final AssignmentRepository assignmentRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final GradeRepository gradeRepository;
    private final SubmissionRepository submissionRepository;

    @GetMapping("/periods")
    public ResponseEntity<List<Map<String, Object>>> periods() {
        return ResponseEntity.ok(periodRepository.findAll().stream()
                .map(p -> Map.<String, Object>of("id", p.getId(), "name", p.getName(), "startDate", p.getStartDate(), "endDate", p.getEndDate()))
                .collect(Collectors.toList()));
    }

    @PostMapping("/periods")
    public ResponseEntity<Map<String, Object>> createPeriod(@RequestBody Map<String, Object> b) {
        AcademicPeriod p = periodRepository.save(AcademicPeriod.builder()
                .name((String) b.get("name"))
                .startDate(LocalDate.parse((String) b.get("startDate")))
                .endDate(LocalDate.parse((String) b.get("endDate")))
                .active(true)
                .build());
        return ResponseEntity.ok(Map.<String, Object>of("id", p.getId(), "name", p.getName()));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getRole() == Role.ADMIN) {
            return ResponseEntity.badRequest().body(Map.of("error", "Cannot delete admin user"));
        }
        userRepository.delete(user);
        return ResponseEntity.ok(Map.of("message", "User deleted"));
    }

    @DeleteMapping("/groups/{id}")
    public ResponseEntity<?> deleteGroup(@PathVariable UUID id) {
        Group group = groupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Группа не найдена"));
        enrollmentRepository.deleteAll(group.getEnrollments());
        for (Subject subject : group.getSubjects()) {
            for (Assignment assignment : subject.getAssignments()) {
                gradeRepository.deleteAll(assignment.getGrades());
            }
            assignmentRepository.deleteAll(subject.getAssignments());
        }
        subjectRepository.deleteAll(group.getSubjects());
        groupRepository.delete(group);
        return ResponseEntity.ok(Map.of("message", "Группа удалена"));
    }

    @DeleteMapping("/subjects/{id}")
    public ResponseEntity<?> deleteSubject(@PathVariable UUID id) {
        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Предмет не найден"));
        for (Assignment assignment : subject.getAssignments()) {
            gradeRepository.deleteAll(assignment.getGrades());
        }
        assignmentRepository.deleteAll(subject.getAssignments());
        subjectRepository.delete(subject);
        return ResponseEntity.ok(Map.of("message", "Предмет удален вместе со всеми заданиями"));
    }

    @DeleteMapping("/assignments/{id}")
    public ResponseEntity<?> deleteAssignment(@PathVariable UUID id) {
        Assignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));
        if (!assignment.getGrades().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Cannot delete assignment with grades. Remove grades first."));
        }
        assignmentRepository.delete(assignment);
        return ResponseEntity.ok(Map.of("message", "Assignment deleted"));
    }

    @DeleteMapping("/periods/{id}")
    public ResponseEntity<?> deletePeriod(@PathVariable UUID id) {
        AcademicPeriod period = periodRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Period not found"));
        if (!period.getGroups().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Cannot delete period with groups. Remove groups first."));
        }
        periodRepository.delete(period);
        return ResponseEntity.ok(Map.of("message", "Period deleted"));
    }

    @GetMapping("/groups")
    public ResponseEntity<List<Map<String, Object>>> groups() {
        return ResponseEntity.ok(groupRepository.findAll().stream()
                .map(g -> Map.<String, Object>of("id", g.getId(), "name", g.getName(),
                        "periodName", g.getPeriod() != null ? g.getPeriod().getName() : "",
                        "studentCount", g.getEnrollments().size()))
                .collect(Collectors.toList()));
    }

    @PostMapping("/groups")
    public ResponseEntity<Map<String, Object>> createGroup(@RequestBody Map<String, Object> b) {
        String name = (String) b.get("name");
        if (name == null || name.trim().isEmpty()) {
            throw new RuntimeException("Название группы не может быть пустым");
        }
        Group g = groupRepository.save(Group.builder()
                .name(name.trim())
                .description(b.containsKey("description") ? (String) b.get("description") : null)
                .period(periodRepository.findById(UUID.fromString((String) b.get("periodId"))).orElseThrow())
                .build());
        return ResponseEntity.ok(Map.<String, Object>of("id", g.getId(), "name", g.getName()));
    }

    @GetMapping("/groups/{id}/students")
    public ResponseEntity<List<Map<String, Object>>> groupStudents(@PathVariable UUID id) {
        return ResponseEntity.ok(enrollmentRepository.findAllByGroupId(id).stream()
                .map(e -> Map.<String, Object>of("id", e.getUser().getId(), "firstName", e.getUser().getFirstName(),
                        "lastName", e.getUser().getLastName(), "email", e.getUser().getEmail()))
                .collect(Collectors.toList()));
    }

    @GetMapping("/subjects")
    public ResponseEntity<List<Map<String, Object>>> subjects() {
        return ResponseEntity.ok(subjectRepository.findAll().stream()
                .map(s -> Map.<String, Object>of("id", s.getId(), "name", s.getName(), "teacherName", s.getTeacherName(),
                        "totalHours", s.getTotalHours(), "groupName", s.getGroup().getName()))
                .collect(Collectors.toList()));
    }

    @PostMapping("/subjects")
    public ResponseEntity<Map<String, Object>> createSubject(@RequestBody Map<String, Object> b) {
        Subject s = subjectRepository.save(Subject.builder()
                .name((String) b.get("name"))
                .teacherName((String) b.getOrDefault("teacherName", ""))
                .weightCoefficient(new BigDecimal(b.getOrDefault("weightCoefficient", 0.5).toString()))
                .totalHours((Integer) b.getOrDefault("totalHours", 0))
                .group(groupRepository.findById(UUID.fromString((String) b.get("groupId"))).orElseThrow())
                .build());
        return ResponseEntity.ok(Map.<String, Object>of("id", s.getId(), "name", s.getName()));
    }

    @GetMapping("/assignments")
    public ResponseEntity<List<Map<String, Object>>> assignments() {
        return ResponseEntity.ok(assignmentRepository.findAll().stream()
                .map(a -> Map.<String, Object>of("id", a.getId(), "title", a.getTitle(), "subjectName", a.getSubject().getName(),
                        "dueDate", a.getDueDate(), "status", a.getStatus().name()))
                .collect(Collectors.toList()));
    }

    @PostMapping("/assignments")
    public ResponseEntity<Map<String, Object>> createAssignment(@RequestBody Map<String, Object> b) {
        Assignment a = assignmentRepository.save(Assignment.builder()
                .title((String) b.get("title"))
                .subject(subjectRepository.findById(UUID.fromString((String) b.get("subjectId"))).orElseThrow())
                .dueDate(LocalDateTime.parse((String) b.get("dueDate")))
                .status(AssignmentStatus.PENDING)
                .type(AssignmentType.valueOf((String) b.get("type")))
                .weight(new BigDecimal(b.getOrDefault("weight", 0.33).toString()))
                .build());
        return ResponseEntity.ok(Map.<String, Object>of("id", a.getId(), "title", a.getTitle()));
    }

    @GetMapping("/students")
    public ResponseEntity<List<Map<String, Object>>> students() {
        return ResponseEntity.ok(userRepository.findAll().stream()
                .filter(u -> u.getRole().name().equals("USER"))
                .map(u -> {
                    List<Enrollment> enr = enrollmentRepository.findAllByUserId(u.getId());
                    return Map.<String, Object>of("id", u.getId(), "firstName", u.getFirstName(), "lastName", u.getLastName(),
                            "email", u.getEmail(), "groupName", enr.isEmpty() ? "—" : enr.get(0).getGroup().getName(),
                            "totalGrades", gradeRepository.findAllByUserId(u.getId()).size());
                }).collect(Collectors.toList()));
    }

    @PostMapping("/enroll")
    public ResponseEntity<String> enroll(@RequestBody Map<String, String> b) {
        UUID userId = UUID.fromString(b.get("userId"));
        UUID groupId = UUID.fromString(b.get("groupId"));
        List<Enrollment> existingEnrollments = enrollmentRepository.findAllByUserId(userId);
        if (!existingEnrollments.isEmpty()) {
            return ResponseEntity.badRequest().body("Студент уже зачислен в группу");
        }
        Group group = groupRepository.findById(groupId).orElseThrow();
        User user = userRepository.findById(userId).orElseThrow();
        enrollmentRepository.save(Enrollment.builder()
                .user(user)
                .group(group)
                .build());
        StudentProfile profile = user.getStudentProfile();
        if (profile == null) {
            profile = new StudentProfile();
            profile.setUser(user);
        }
        profile.setGroupName(group.getName());
        user.setStudentProfile(profile);
        userRepository.save(user);
        return ResponseEntity.ok("OK");
    }

    @DeleteMapping("/unenroll")
    public ResponseEntity<String> unenroll(@RequestBody Map<String, String> b) {
        UUID userId = UUID.fromString(b.get("userId"));
        UUID groupId = UUID.fromString(b.get("groupId"));
        enrollmentRepository.findAllByUserId(userId).stream()
                .filter(e -> e.getGroup().getId().equals(groupId))
                .findFirst()
                .ifPresent(enrollmentRepository::delete);
        boolean hasAnyGroup = enrollmentRepository.findAllByUserId(userId).stream()
                .findAny()
                .isPresent();
        User user = userRepository.findById(userId).orElseThrow();
        StudentProfile profile = user.getStudentProfile();
        if (profile != null) {
            if (hasAnyGroup) {
                Group anyGroup = enrollmentRepository.findAllByUserId(userId).get(0).getGroup();
                profile.setGroupName(anyGroup.getName());
            } else {
                profile.setGroupName("");
            }
            user.setStudentProfile(profile);
            userRepository.save(user);
        }
        return ResponseEntity.ok("OK");
    }

    @GetMapping("/users/{userId}/groups")
    public ResponseEntity<List<Map<String, Object>>> getUserGroups(@PathVariable UUID userId) {
        List<Enrollment> enrollments = enrollmentRepository.findAllByUserId(userId);
        return ResponseEntity.ok(enrollments.stream()
                .map(e -> Map.<String, Object>of("groupId", e.getGroup().getId(), "groupName", e.getGroup().getName()))
                .collect(Collectors.toList()));
    }

    @PutMapping("/users/{userId}/group-name")
    public ResponseEntity<?> updateUserGroupName(@PathVariable UUID userId, @RequestBody Map<String, String> body) {
        User user = userRepository.findById(userId).orElseThrow();
        StudentProfile profile = user.getStudentProfile();
        if (profile == null) {
            profile = new StudentProfile();
            profile.setUser(user);
        }
        profile.setGroupName(body.get("groupName"));
        user.setStudentProfile(profile);
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("message", "Группа в профиле обновлена"));
    }

    @GetMapping("/submissions")
    public ResponseEntity<List<Map<String, Object>>> submissions() {
        List<Submission> submissions = submissionRepository.findAll().stream()
                .filter(s -> s.getStatus() == SubmissionStatus.SUBMITTED)
                .collect(Collectors.toList());
        List<Map<String, Object>> result = submissions.stream().map(s -> {
            Map<String, Object> item = new HashMap<>();
            item.put("assignmentId", s.getAssignment().getId());
            item.put("title", s.getAssignment().getTitle());
            item.put("subjectName", s.getAssignment().getSubject().getName());
            item.put("studentId", s.getUser().getId());
            item.put("studentName", s.getUser().getFirstName() + " " + s.getUser().getLastName());
            item.put("filename", s.getFilePath());
            return item;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/grades")
    public ResponseEntity<String> grade(@RequestBody Map<String, Object> b) {
        UUID userId = UUID.fromString((String) b.get("userId"));
        UUID assignmentId = UUID.fromString((String) b.get("assignmentId"));
        Integer score = (Integer) b.get("score");
        Assignment assignment = assignmentRepository.findById(assignmentId).orElseThrow();
        Submission submission = submissionRepository.findByAssignmentIdAndUserId(assignmentId, userId)
                .orElseThrow(() -> new RuntimeException("Отправка не найдена"));
        submission.setStatus(SubmissionStatus.GRADED);
        submissionRepository.save(submission);
        Optional<Grade> existingGrade = gradeRepository.findAllByUserId(userId).stream()
                .filter(g -> g.getAssignment().getId().equals(assignmentId))
                .findFirst();
        if (existingGrade.isPresent()) {
            Grade grade = existingGrade.get();
            grade.setScore(score);
            grade.setGradedAt(Instant.now());
            gradeRepository.save(grade);
        } else {
            gradeRepository.save(Grade.builder()
                    .user(userRepository.findById(userId).orElseThrow())
                    .assignment(assignment)
                    .score(score)
                    .gradedAt(Instant.now())
                    .build());
        }
        try {
            Path uploadDir = Path.of("uploads/submissions");
            Files.list(uploadDir).forEach(file -> {
                String filename = file.getFileName().toString();
                if (filename.startsWith(userId + "_" + assignmentId)) {
                    try {
                        Files.delete(file);
                    } catch (IOException e) {
                        System.out.println("Не удалось удалить файл: " + filename);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.ok("OK");
    }

    @PostMapping("/reject")
    public ResponseEntity<String> reject(@RequestBody Map<String, String> body) {
        UUID assignmentId = UUID.fromString(body.get("assignmentId"));
        UUID userId = UUID.fromString(body.get("userId"));
        Submission submission = submissionRepository.findByAssignmentIdAndUserId(assignmentId, userId)
                .orElseThrow(() -> new RuntimeException("Отправка не найдена"));
        submission.setStatus(SubmissionStatus.RETURNED);
        submissionRepository.save(submission);
        Optional<Grade> existingGrade = gradeRepository.findAllByUserId(userId).stream()
                .filter(g -> g.getAssignment().getId().equals(assignmentId))
                .findFirst();
        existingGrade.ifPresent(gradeRepository::delete);
        try {
            Path uploadDir = Path.of("uploads/submissions");
            Files.list(uploadDir).forEach(file -> {
                String filename = file.getFileName().toString();
                if (filename.startsWith(userId + "_" + assignmentId)) {
                    try {
                        Files.delete(file);
                    } catch (IOException e) {
                        System.out.println("Не удалось удалить файл: " + filename);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.ok("OK");
    }

    @GetMapping("/stats/{userId}")
    public ResponseEntity<Map<String, Object>> stats(@PathVariable UUID userId) {
        List<Grade> grades = gradeRepository.findAllByUserId(userId);
        double avg = grades.stream().mapToInt(Grade::getScore).average().orElse(0);
        return ResponseEntity.ok(Map.<String, Object>of("averageScore", Math.round(avg * 100.0) / 100.0,
                "totalGrades", grades.size(),
                "grades", grades.stream().map(g -> Map.<String, Object>of("assignment", g.getAssignment().getTitle(),
                        "score", g.getScore(), "subject", g.getAssignment().getSubject().getName())).collect(Collectors.toList())));
    }

    @PutMapping("/periods/{id}")
    public ResponseEntity<Map<String, Object>> updatePeriod(@PathVariable UUID id, @RequestBody Map<String, Object> body) {
        AcademicPeriod period = periodRepository.findById(id).orElseThrow();
        if (body.containsKey("name")) period.setName((String) body.get("name"));
        if (body.containsKey("startDate")) period.setStartDate(LocalDate.parse((String) body.get("startDate")));
        if (body.containsKey("endDate")) period.setEndDate(LocalDate.parse((String) body.get("endDate")));
        periodRepository.save(period);
        return ResponseEntity.ok(Map.of("id", period.getId(), "name", period.getName()));
    }

    @PutMapping("/groups/{id}")
    public ResponseEntity<Map<String, Object>> updateGroup(@PathVariable UUID id, @RequestBody Map<String, Object> body) {
        Group group = groupRepository.findById(id).orElseThrow(() -> new RuntimeException("Группа не найдена"));
        if (body.containsKey("name")) {
            String name = (String) body.get("name");
            if (name != null && !name.trim().isEmpty()) {
                group.setName(name.trim());
            }
        }
        if (body.containsKey("periodId")) {
            AcademicPeriod period = periodRepository.findById(UUID.fromString((String) body.get("periodId"))).orElseThrow();
            group.setPeriod(period);
        }
        groupRepository.save(group);
        return ResponseEntity.ok(Map.of("id", group.getId(), "name", group.getName(), "periodId", group.getPeriod().getId()));
    }

    @PutMapping("/subjects/{id}")
    public ResponseEntity<Map<String, Object>> updateSubject(@PathVariable UUID id, @RequestBody Map<String, Object> body) {
        Subject subject = subjectRepository.findById(id).orElseThrow();
        if (body.containsKey("name")) subject.setName((String) body.get("name"));
        if (body.containsKey("teacherName")) subject.setTeacherName((String) body.get("teacherName"));
        if (body.containsKey("totalHours")) subject.setTotalHours((Integer) body.get("totalHours"));
        if (body.containsKey("groupId")) {
            Group group = groupRepository.findById(UUID.fromString((String) body.get("groupId"))).orElseThrow();
            subject.setGroup(group);
        }
        subjectRepository.save(subject);
        return ResponseEntity.ok(Map.of("id", subject.getId(), "name", subject.getName()));
    }

    @PutMapping("/assignments/{id}")
    public ResponseEntity<Map<String, Object>> updateAssignment(@PathVariable UUID id, @RequestBody Map<String, Object> body) {
        Assignment assignment = assignmentRepository.findById(id).orElseThrow();
        if (body.containsKey("title")) assignment.setTitle((String) body.get("title"));
        if (body.containsKey("dueDate")) assignment.setDueDate(LocalDateTime.parse((String) body.get("dueDate")));
        if (body.containsKey("type")) assignment.setType(AssignmentType.valueOf((String) body.get("type")));
        if (body.containsKey("subjectId")) {
            Subject subject = subjectRepository.findById(UUID.fromString((String) body.get("subjectId"))).orElseThrow();
            assignment.setSubject(subject);
        }
        assignmentRepository.save(assignment);
        return ResponseEntity.ok(Map.of("id", assignment.getId(), "title", assignment.getTitle()));
    }

    @GetMapping("/groups/{id}")
    public ResponseEntity<Map<String, Object>> getGroup(@PathVariable UUID id) {
        Group group = groupRepository.findById(id).orElseThrow();
        return ResponseEntity.ok(Map.of(
                "id", group.getId(),
                "name", group.getName(),
                "periodId", group.getPeriod().getId()
        ));
    }

    @GetMapping("/subjects/{id}")
    public ResponseEntity<Map<String, Object>> getSubject(@PathVariable UUID id) {
        Subject subject = subjectRepository.findById(id).orElseThrow();
        return ResponseEntity.ok(Map.of(
                "id", subject.getId(),
                "name", subject.getName(),
                "teacherName", subject.getTeacherName(),
                "totalHours", subject.getTotalHours(),
                "groupId", subject.getGroup().getId()
        ));
    }

    @GetMapping("/assignments/{id}")
    public ResponseEntity<Map<String, Object>> getAssignment(@PathVariable UUID id) {
        Assignment assignment = assignmentRepository.findById(id).orElseThrow();
        return ResponseEntity.ok(Map.of(
                "id", assignment.getId(),
                "title", assignment.getTitle(),
                "subjectId", assignment.getSubject().getId(),
                "dueDate", assignment.getDueDate().toString(),
                "type", assignment.getType().name()
        ));
    }

    @GetMapping("/download/{assignmentId}")
    public ResponseEntity<Resource> download(@PathVariable UUID assignmentId, @RequestParam(required = false) UUID userId) {
        try {
            Path dir = Path.of("uploads/submissions");
            Files.createDirectories(dir);
            Optional<Path> file;
            if (userId != null) {
                file = Files.list(dir)
                        .filter(f -> f.getFileName().toString().startsWith(userId + "_" + assignmentId))
                        .findFirst();
            } else {
                file = Files.list(dir)
                        .filter(f -> f.getFileName().toString().contains(assignmentId.toString()))
                        .findFirst();
            }
            if (file.isPresent()) {
                String originalName = file.get().getFileName().toString();
                String[] parts = originalName.split("_", 3);
                String cleanName = parts.length >= 3 ? parts[2] : originalName;
                Resource r = new UrlResource(file.get().toUri());
                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
                        .header(HttpHeaders.CONTENT_DISPOSITION,
                                "attachment; filename*=UTF-8''" + URLEncoder.encode(cleanName, "UTF-8").replace("+", "%20"))
                        .body(r);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.notFound().build();
    }
}