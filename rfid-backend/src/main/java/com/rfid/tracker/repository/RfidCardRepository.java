package com.rfid.tracker.repository;

import com.rfid.tracker.entity.RfidCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface RfidCardRepository extends JpaRepository<RfidCard, Long> {
    
    /**
     * Finds an active RFID card by its unique UID.
     * This is more secure as it prevents deactivated cards from being used.
     */
    Optional<RfidCard> findByCardUidAndIsActiveTrue(String cardUid);
}
