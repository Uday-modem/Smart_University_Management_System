package com.rfid.tracker.repository;

import com.rfid.tracker.entity.AttendanceSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AttendanceSummaryRepository extends JpaRepository<AttendanceSummary, Long> {
    Optional<AttendanceSummary> findByStudentIdAndSemesterAndAcademicYear(
            Long studentId, Integer semester, String academicYear);
}
