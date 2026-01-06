package com.rfid.tracker.repository;

import com.rfid.tracker.entity.AttendanceProcessingLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.Optional;
import java.util.List;

@Repository
public interface AttendanceProcessingLogRepository extends JpaRepository<AttendanceProcessingLog, Long> {
    
    Optional<AttendanceProcessingLog> findByUserIdentifierAndProcessDate(
            String userIdentifier, LocalDate processDate);
    
    List<AttendanceProcessingLog> findByProcessDate(LocalDate processDate);
    
    List<AttendanceProcessingLog> findByUserTypeAndProcessDate(
            AttendanceProcessingLog.UserType userType, LocalDate processDate);
    
    @Query("SELECT a FROM AttendanceProcessingLog a WHERE a.processDate BETWEEN :startDate AND :endDate")
    List<AttendanceProcessingLog> findByDateRange(
            @Param("startDate") LocalDate startDate, 
            @Param("endDate") LocalDate endDate);
}
