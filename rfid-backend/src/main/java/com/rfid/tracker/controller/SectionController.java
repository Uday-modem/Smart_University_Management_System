package com.rfid.tracker.controller;

import com.rfid.tracker.entity.Section;
import com.rfid.tracker.repository.SectionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sections")
@CrossOrigin(origins = "http://localhost:5173")
public class SectionController {

    @Autowired
    private SectionRepository sectionRepository;

    // ✅ GET ALL BRANCHES (distinct) from sections
    @GetMapping("/branches")
    public ResponseEntity<?> getBranches() {
        try {
            System.out.println("📌 Fetching distinct branches from sections...");
            
            List<String> branches = sectionRepository.findDistinctBranches();
            
            System.out.println("✅ Found " + branches.size() + " branches: " + branches);
            
            if (branches.isEmpty()) {
                System.out.println("⚠️  Warning: No branches found in sections table. Sections table might be empty.");
            }
            
            return ResponseEntity.ok(branches != null ? branches : List.of());
            
        } catch (Exception e) {
            System.err.println("❌ Error fetching branches: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                Map.of("success", false, "message", "Error fetching branches: " + e.getMessage())
            );
        }
    }

    // ✅ GET YEARS BY REQUEST PARAM
    @GetMapping("/years")
    public ResponseEntity<?> getYearsByRequestParam(@RequestParam String branch) {
        try {
            System.out.println("📌 Getting years for branch (RequestParam): " + branch);
            
            List<Integer> years = sectionRepository.findDistinctYearsByBranch(branch);
            
            System.out.println("✅ Years found: " + years);
            
            if (years.isEmpty()) {
                System.out.println("⚠️  No years found for branch: " + branch);
            }
            
            return ResponseEntity.ok(years);
            
        } catch (Exception e) {
            System.err.println("❌ Error fetching years: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                Map.of("success", false, "message", "Error fetching years: " + e.getMessage())
            );
        }
    }

    // ✅ GET YEARS BY PATH VARIABLE
    @GetMapping("/years/{branch}")
    public ResponseEntity<?> getYearsByPathVariable(@PathVariable String branch) {
        try {
            System.out.println("📌 Getting years for branch (PathVariable): " + branch);
            
            List<Integer> years = sectionRepository.findDistinctYearsByBranch(branch);
            
            System.out.println("✅ Years found: " + years);
            
            if (years.isEmpty()) {
                System.out.println("⚠️  No years found for branch: " + branch);
            }
            
            return ResponseEntity.ok(years);
            
        } catch (Exception e) {
            System.err.println("❌ Error fetching years: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                Map.of("success", false, "message", "Error fetching years: " + e.getMessage())
            );
        }
    }

    // ✅ GET SECTIONS LIST FOR BRANCH + YEAR (NO semester)
    @GetMapping
    public ResponseEntity<?> getSections(
            @RequestParam String branch,
            @RequestParam int year) {
        try {
            System.out.println("📌 Getting sections for: " + branch + ", Year: " + year);
            
            List<Section> sections = sectionRepository.findByBranchAndYear(branch, year);
            
            System.out.println("✅ Sections found: " + sections.size());
            
            if (sections.isEmpty()) {
                System.out.println("⚠️  No sections found for branch: " + branch + ", year: " + year);
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "sections", sections,
                "count", sections.size()
            ));
            
        } catch (Exception e) {
            System.err.println("❌ Error fetching sections: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                Map.of("success", false, "message", "Error fetching sections: " + e.getMessage())
            );
        }
    }

    // ✅ GET SECTION BY CODE
    @GetMapping("/{code}")
    public ResponseEntity<?> getSectionByCode(@PathVariable String code) {
        try {
            System.out.println("📌 Fetching section by code: " + code);
            
            var section = sectionRepository.findBySectionCode(code);
            
            if (section.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    Map.of("success", false, "message", "Section not found")
                );
            }
            
            System.out.println("✅ Section found: " + section.get().getSectionCode());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "section", section.get()
            ));
            
        } catch (Exception e) {
            System.err.println("❌ Error fetching section: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                Map.of("success", false, "message", "Error: " + e.getMessage())
            );
        }
    }

    // ✅ GET ALL SECTIONS
    @GetMapping("/all")
    public ResponseEntity<?> getAllSections() {
        try {
            System.out.println("📌 Fetching all sections...");
            
            List<Section> sections = sectionRepository.findAll();
            
            System.out.println("✅ Total sections: " + sections.size());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "sections", sections,
                "count", sections.size()
            ));
            
        } catch (Exception e) {
            System.err.println("❌ Error fetching sections: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                Map.of("success", false, "message", "Error: " + e.getMessage())
            );
        }
    }

    // ✅ GET AVAILABLE SECTIONS
    @GetMapping("/available")
    public ResponseEntity<?> getAvailableSections(
            @RequestParam String branch,
            @RequestParam int year) {
        try {
            System.out.println("📌 Getting available sections for: " + branch + ", Year: " + year);
            
            List<Section> sections = sectionRepository.findAvailableSections(branch, year);
            
            System.out.println("✅ Available sections: " + sections.size());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "sections", sections,
                "count", sections.size()
            ));
            
        } catch (Exception e) {
            System.err.println("❌ Error fetching available sections: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                Map.of("success", false, "message", "Error: " + e.getMessage())
            );
        }
    }
}