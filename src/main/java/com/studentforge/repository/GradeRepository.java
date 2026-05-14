package com.studentforge.repository;

import com.studentforge.entity.Grade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GradeRepository extends JpaRepository<Grade, UUID> {

    List<Grade> findAllByUserId(UUID userId);

    List<Grade> findAllByAssignmentId(UUID assignmentId);

    boolean existsByUserIdAndAssignmentId(UUID userId, UUID assignmentId);
}