package com.rfid.tracker.repository;

import com.rfid.tracker.entity.PeriodAttendanceLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PeriodAttendanceLogRepository extends JpaRepository<PeriodAttendanceLog, Long> {

    List<PeriodAttendanceLog> findByStudentRegistrationNumberAndScanDate(String registrationNumber, LocalDate scanDate);

    List<PeriodAttendanceLog> findBySectionIdAndScanDate(String sectionId, LocalDate scanDate);

    Optional<PeriodAttendanceLog> findByStudentRegistrationNumberAndTimeSlotAndScanDate(
            String registrationNumber, String timeSlot, LocalDate scanDate);

    @Query("SELECT COUNT(p) FROM PeriodAttendanceLog p WHERE p.studentRegistrationNumber = :regNumber AND p.scanDate = :scanDate")
    long countPeriodScans(@Param("regNumber") String registrationNumber, @Param("scanDate") LocalDate scanDate);

    @Query("SELECT p FROM PeriodAttendanceLog p WHERE p.studentRegistrationNumber = :regNumber AND p.scanDate BETWEEN :startDate AND :endDate ORDER BY p.scanTime")
    List<PeriodAttendanceLog> findStudentAttendanceByDateRange(
            @Param("regNumber") String registrationNumber,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    // ✅ NEW: Delete methods for cleanup after final calculation
    @Modifying
    @Transactional
    @Query("DELETE FROM PeriodAttendanceLog p WHERE p.studentRegistrationNumber = :regNumber AND p.scanDate = :scanDate")
    void deleteByStudentRegistrationNumberAndScanDate(
            @Param("regNumber") String registrationNumber, 
            @Param("scanDate") LocalDate scanDate);
}
