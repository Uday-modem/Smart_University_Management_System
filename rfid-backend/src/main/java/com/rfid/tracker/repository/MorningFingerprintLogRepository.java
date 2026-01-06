package com.rfid.tracker.repository;

import com.rfid.tracker.entity.MorningFingerprintLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.Optional;
import java.util.List;

@Repository
public interface MorningFingerprintLogRepository extends JpaRepository<MorningFingerprintLog, Long> {
    List<MorningFingerprintLog> findByUserIdentifierAndScanDate(String userIdentifier, LocalDate scanDate);
    
    Optional<MorningFingerprintLog> findByUserIdentifierAndScanDateAndUserType(
            String userIdentifier, LocalDate scanDate, MorningFingerprintLog.UserType userType);
    
    List<MorningFingerprintLog> findByScanDateAndProcessed(LocalDate scanDate, Boolean processed);
    
    @Query("SELECT m FROM MorningFingerprintLog m WHERE m.sectionId = :sectionId AND m.scanDate = :scanDate AND m.processed = false")
    List<MorningFingerprintLog> findUnprocessedBySection(@Param("sectionId") String sectionId, @Param("scanDate") LocalDate scanDate);
    
    List<MorningFingerprintLog> findByUserTypeAndScanDate(MorningFingerprintLog.UserType userType, LocalDate scanDate);
}
