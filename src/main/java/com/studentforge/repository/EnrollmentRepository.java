package com.studentforge.repository;

import com.studentforge.entity.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, UUID> {

    List<Enrollment> findAllByUserId(UUID userId);

    List<Enrollment> findAllByGroupId(UUID groupId);

    boolean existsByUserIdAndGroupId(UUID userId, UUID groupId);
}