package com.studentforge.repository;

import com.studentforge.entity.Submission;
import com.studentforge.enums.SubmissionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, UUID> {
    Optional<Submission> findByAssignmentIdAndUserId(UUID assignmentId, UUID userId);
    List<Submission> findAllByUserId(UUID userId);
    List<Submission> findAllByAssignmentId(UUID assignmentId);
    List<Submission> findAllByStatus(SubmissionStatus status);
}