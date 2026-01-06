package com.rfid.tracker.repository;

import com.rfid.tracker.entity.FingerprintTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface FingerprintTemplateRepository extends JpaRepository<FingerprintTemplate, Long> {
    
    Optional<FingerprintTemplate> findByFingerprintId(Integer fingerprintId);
    
    // ✅ ADDED THIS MISSING METHOD
    Optional<FingerprintTemplate> findByFingerprintIdAndIsActiveTrue(Integer fingerprintId);

    Optional<FingerprintTemplate> findByUserIdentifier(String userIdentifier);
    
    List<FingerprintTemplate> findByUserType(FingerprintTemplate.UserType userType);
    
    List<FingerprintTemplate> findByIsActive(Boolean isActive);
    
    List<FingerprintTemplate> findByUserTypeAndIsActive(FingerprintTemplate.UserType userType, Boolean isActive);
}
