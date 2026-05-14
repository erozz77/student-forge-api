package com.studentforge.repository;

import com.studentforge.entity.Assignment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, UUID> {

    Page<Assignment> findAllBySubjectId(UUID subjectId, Pageable pageable);

    Page<Assignment> findAllBySubjectIdAndStatus(UUID subjectId, String status, Pageable pageable);

    @Query("SELECT a FROM Assignment a JOIN FETCH a.subject WHERE a.id = :id")
    Optional<Assignment> findByIdWithSubject(@Param("id") UUID id);
}