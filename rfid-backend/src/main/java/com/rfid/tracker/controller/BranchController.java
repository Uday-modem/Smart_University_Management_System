package com.rfid.tracker.controller;

import com.rfid.tracker.entity.Branch;
import com.rfid.tracker.repository.BranchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/branches")
@CrossOrigin(origins = "http://localhost:5173")
public class BranchController {

    @Autowired
    private BranchRepository branchRepository;

    // ✅ GET ALL BRANCHES - Frontend branch dropdown
    @GetMapping
    public ResponseEntity<?> getAllBranches() {
        try {
            System.out.println("📌 Fetching all branches from database...");
            
            List<Branch> branches = branchRepository.findAll();
            
            System.out.println("✅ Found " + branches.size() + " branches");
            
            if (branches.isEmpty()) {
                System.out.println("⚠️  Warning: No branches found in database!");
                return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "No branches found in database",
                    "branches", List.of()
                ));
            }
            
            // Log branch details
            branches.forEach(b -> System.out.println("  - " + b.getBranchCode() + ": " + b.getBranchName()));
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "branches", branches,
                "count", branches.size(),
                "message", "Branches fetched successfully"
            ));
            
        } catch (Exception e) {
            System.err.println("❌ Error fetching branches: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                Map.of("success", false, "message", "Failed to fetch branches: " + e.getMessage())
            );
        }
    }

    // ✅ GET BRANCH BY CODE
    @GetMapping("/{code}")
    public ResponseEntity<?> getBranchByCode(@PathVariable String code) {
        try {
            System.out.println("📌 Fetching branch by code: " + code);
            
            var branch = branchRepository.findByBranchCode(code);
            
            if (branch.isEmpty()) {
                System.out.println("⚠️  Branch not found: " + code);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    Map.of("success", false, "message", "Branch not found with code: " + code)
                );
            }
            
            System.out.println("✅ Branch found: " + branch.get().getBranchName());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "branch", branch.get()
            ));
            
        } catch (Exception e) {
            System.err.println("❌ Error fetching branch: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                Map.of("success", false, "message", "Error: " + e.getMessage())
            );
        }
    }

    // ✅ GET BRANCH BY NAME
    @GetMapping("/name/{name}")
    public ResponseEntity<?> getBranchByName(@PathVariable String name) {
        try {
            System.out.println("📌 Fetching branch by name: " + name);
            
            var branch = branchRepository.findByBranchName(name);
            
            if (branch.isEmpty()) {
                System.out.println("⚠️  Branch not found: " + name);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    Map.of("success", false, "message", "Branch not found with name: " + name)
                );
            }
            
            System.out.println("✅ Branch found: " + branch.get().getBranchCode());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "branch", branch.get()
            ));
            
        } catch (Exception e) {
            System.err.println("❌ Error fetching branch: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                Map.of("success", false, "message", "Error: " + e.getMessage())
            );
        }
    }
}