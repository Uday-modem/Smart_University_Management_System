package com.rfid.tracker.repository;

import com.rfid.tracker.entity.SemesterConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SemesterConfigRepository extends JpaRepository<SemesterConfig, Long> {
    Optional<SemesterConfig> findByAcademicYearAndRegulationIdAndYearAndSemester(
            String academicYear, Long regulationId, Integer year, Integer semester);
    List<SemesterConfig> findByAcademicYear(String academicYear);
    List<SemesterConfig> findByRegulationId(Long regulationId);
}
