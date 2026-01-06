package com.rfid.tracker.repository;

import com.rfid.tracker.entity.Branch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BranchRepository extends JpaRepository<Branch, Long> {
    
    // ✅ Find branch by code (ECE, CSE, MECH, CIVIL, EEE, ME, etc.)
    Optional<Branch> findByBranchCode(String branchCode);
    
    // ✅ Find by full branch name
    Optional<Branch> findByBranchName(String branchName);
    
    // ✅ Get all branches (for dropdown)
    List<Branch> findAll();
}