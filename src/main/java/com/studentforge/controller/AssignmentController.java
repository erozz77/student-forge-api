package com.studentforge.controller;

import com.studentforge.dto.response.AssignmentResponse;
import com.studentforge.service.AssignmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/subjects/{subjectId}/assignments")
@RequiredArgsConstructor
public class AssignmentController {

    private final AssignmentService assignmentService;

    @GetMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Page<AssignmentResponse>> getAssignments(
            @PathVariable UUID subjectId,
            @RequestParam(required = false) String status,
            @PageableDefault(size = 20, sort = "dueDate", direction = Sort.Direction.ASC) Pageable pageable) {
        if (status != null && !status.isEmpty()) {
            return ResponseEntity.ok(assignmentService.getAssignmentsBySubjectAndStatus(subjectId, status, pageable));
        }
        return ResponseEntity.ok(assignmentService.getAssignmentsBySubject(subjectId, pageable));
    }
}