package com.rfid.tracker.repository;

import com.rfid.tracker.entity.Complaint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, Long> {
    
    // Find all complaints by student
    List<Complaint> findByStudentId(Long studentId);
    
    // Find all complaints by status
    List<Complaint> findByStatus(String status);
    
    // Count pending complaints
    long countByStatus(String status);
    
    // Find all ordered by created date (newest first)
    List<Complaint> findAllByOrderByCreatedAtDesc();
}
