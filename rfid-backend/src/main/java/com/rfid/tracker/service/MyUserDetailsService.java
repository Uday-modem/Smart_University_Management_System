package com.rfid.tracker.service;

import com.rfid.tracker.entity.Admin;
import com.rfid.tracker.entity.Student;
import com.rfid.tracker.repository.AdminRepository;
import com.rfid.tracker.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class MyUserDetailsService implements UserDetailsService {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private AdminRepository adminRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("🔍 Loading user: " + username); // DEBUG
        
        // Try to find a student first
        Student student = studentRepository.findByEmail(username).orElse(null);
        if (student != null) {
            List<GrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority("STUDENT"));
            System.out.println("✅ Found STUDENT with authorities: " + authorities); // DEBUG
            return new User(student.getEmail(), student.getPassword(), authorities);
        }

        // If not a student, try to find an admin
        Admin admin = adminRepository.findByEmail(username).orElse(null);
        if (admin != null) {
            List<GrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority("ADMIN"));
            System.out.println("✅ Found ADMIN with authorities: " + authorities); // DEBUG
            return new User(admin.getEmail(), admin.getPassword(), authorities);
        }
        
        System.err.println("❌ User not found: " + username); // DEBUG
        throw new UsernameNotFoundException("User not found with email: " + username);
    }
}
