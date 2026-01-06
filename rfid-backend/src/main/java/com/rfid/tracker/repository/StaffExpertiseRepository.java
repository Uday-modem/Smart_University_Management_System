package com.rfid.tracker.repository;

import com.rfid.tracker.entity.StaffExpertise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface StaffExpertiseRepository extends JpaRepository<StaffExpertise, Long> {

    List<StaffExpertise> findByStaffId(String staffId);
    List<StaffExpertise> findByStaffIdIn(List<String> staffIds);
}
