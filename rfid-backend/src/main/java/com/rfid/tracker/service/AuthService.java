package com.rfid.tracker.service;

import com.rfid.tracker.entity.Student;
import com.rfid.tracker.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private StudentRepository studentRepository;

    // ✅ FIX: Changed from passwordEncoder to bCryptPasswordEncoder
    // This now matches the new @Bean method in SecurityConfig
    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    // ✅ FIX: Added <Student> generic type to Optional
    // ✅ FIX: Complete method with proper closing braces
    public Optional<Student> findStudentByEmailOrRegistration(String identifier) {
        System.out.println("🔍 Searching for student: " + identifier);

        // Try email first
        Optional<Student> student = studentRepository.findByEmail(identifier);
        if (student.isPresent()) {
            System.out.println("✅ Found student by email");
            return student;
        }

        // Try registration number
        student = studentRepository.findByRegistrationNumber(identifier);
        if (student.isPresent()) {
            System.out.println("✅ Found student by registration number");
            return student;
        }


        System.out.println("❌ Student not found with identifier: " + identifier);
        return Optional.empty();
    }

    // ✅ FIX: Complete method with proper closing braces
    public boolean verifyPassword(String rawPassword, String hashedPassword) {
        System.out.println("🔐 Verifying password...");
        if (hashedPassword == null || hashedPassword.isEmpty()) {
            System.out.println("❌ Hashed password is empty");
            return false;
        }

        boolean matches = bCryptPasswordEncoder.matches(rawPassword, hashedPassword);
        System.out.println(matches ? "✅ Password verified" : "❌ Password verification failed");
        return matches;
    }

    // ✅ FIX: Added <Student> generic type to Optional
    // ✅ FIX: Complete method with proper closing braces
    public Optional<Student> getStudentById(Long id) {
        System.out.println("🔍 Fetching student with ID: " + id);
        return studentRepository.findById(id);
    }

    // ✅ FIX: Complete method with proper closing braces
    public boolean studentExists(String email) {
        return studentRepository.findByEmail(email).isPresent();
    }

}