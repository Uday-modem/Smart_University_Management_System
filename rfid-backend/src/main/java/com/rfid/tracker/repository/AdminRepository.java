package com.rfid.tracker.repository;

import com.rfid.tracker.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {
    
    // ✅ Find admin by email (for login)
    Optional<Admin> findByEmail(String email);
    
    // ✅ Check if admin exists by email
    boolean existsByEmail(String email);
}
