package com.studentforge.repository;

import com.studentforge.entity.AcademicPeriod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AcademicPeriodRepository extends JpaRepository<AcademicPeriod, UUID> {

    Optional<AcademicPeriod> findByActiveTrue();
}