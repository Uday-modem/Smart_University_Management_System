package com.rfid.tracker.repository;

import com.rfid.tracker.entity.Attendance;
import com.rfid.tracker.entity.AttendanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    // ========== STUDENT ATTENDANCE QUERIES ==========
    
    Optional<Attendance> findByStudentIdAndDate(Long studentId, LocalDate date);
    
    List<Attendance> findByStudentIdAndDateBetween(
        Long studentId, LocalDate startDate, LocalDate endDate
    );
    
    // ✅ CHANGED: sectionId parameter is now String
    @Query("SELECT a FROM Attendance a WHERE a.sectionId = :sectionId " +
           "AND YEAR(a.date) = :year AND MONTH(a.date) = :month " +
           "ORDER BY a.studentId, a.date")
    List<Attendance> findBySectionAndMonth(
        @Param("sectionId") String sectionId,
        @Param("year") int year,
        @Param("month") int month
    );
    
    // ✅ CHANGED: sectionId parameter is now String
    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.sectionId = :sectionId " +
           "AND a.date = :date AND a.status = :status")
    long countBySectionAndDateAndStatus(
        @Param("sectionId") String sectionId,
        @Param("date") LocalDate date,
        @Param("status") AttendanceStatus status
    );

    // ========== STAFF ATTENDANCE QUERIES ==========
    
    Optional<Attendance> findByStaffIdAndDate(String staffId, LocalDate date);
    
    @Query("SELECT a FROM Attendance a WHERE a.branch = :branch " +
           "AND YEAR(a.date) = :year AND MONTH(a.date) = :month " +
           "AND a.staffId IS NOT NULL " +
           "ORDER BY a.staffId, a.date")
    List<Attendance> findByBranchAndMonth(
        @Param("branch") String branch,
        @Param("year") int year,
        @Param("month") int month
    );
    
    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.branch = :branch " +
           "AND a.date = :date AND a.status = :status AND a.staffId IS NOT NULL")
    long countByBranchAndDateAndStatus(
        @Param("branch") String branch,
        @Param("date") LocalDate date,
        @Param("status") AttendanceStatus status
    );

    // ========== BATCH OPERATIONS ==========
    
    // ✅ CHANGED: sectionId parameter is now String
    @Query("SELECT a FROM Attendance a WHERE a.sectionId = :sectionId AND a.date = :date")
    List<Attendance> findBySectionAndDate(
        @Param("sectionId") String sectionId,
        @Param("date") LocalDate date
    );
    
    @Query("SELECT a FROM Attendance a WHERE a.branch = :branch AND a.date = :date " +
           "AND a.staffId IS NOT NULL")
    List<Attendance> findByBranchAndDate(
        @Param("branch") String branch,
        @Param("date") LocalDate date
    );

    // ========== DELETE OPERATIONS ==========
    
    void deleteByStudentId(Long studentId);
    void deleteByStaffId(String staffId);
    
    // ✅ CHANGED: sectionId parameter is now String
    void deleteBySectionId(String sectionId);
}
