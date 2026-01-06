package com.rfid.tracker.repository;

import com.rfid.tracker.entity.Regulation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface RegulationRepository extends JpaRepository<Regulation, Long> {
    Optional<Regulation> findByRegulationCode(String regulationCode);

    // Fix: change parameter to String to match entity, or remove if unused
    Optional<Regulation> findByStartYear(String startYear);
}
