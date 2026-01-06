package com.rfid.tracker.repository;

import com.rfid.tracker.entity.Staff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StaffRepository extends JpaRepository<Staff, Long> {
    Optional<Staff> findByStaffId(String staffId);
    boolean existsByStaffId(String staffId);
    List<Staff> findByBranch(String branch);
}
