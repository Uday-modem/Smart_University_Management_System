package com.rfid.tracker.controller;

import com.rfid.tracker.entity.Marks;
import com.rfid.tracker.entity.Student;
import com.rfid.tracker.entity.Subject;
import com.rfid.tracker.repository.MarksRepository;
import com.rfid.tracker.repository.StudentRepository;
import com.rfid.tracker.repository.SubjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/marks")
@CrossOrigin(origins = "http://localhost:5173")
public class MarksController {

    @Autowired
    private MarksRepository marksRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    // Get subjects for a section (admin view)
    @GetMapping("/subjects/{branch}/{year}/{semester}/{regulationId}")
    public ResponseEntity<?> getSubjects(@PathVariable String branch, @PathVariable Integer year, 
                                         @PathVariable Integer semester, @PathVariable Long regulationId) {
        try {
            System.out.println("📌 Fetching subjects for: " + branch + ", Year: " + year + ", Sem: " + semester + ", Regulation: " + regulationId);
            List<Subject> subjects = subjectRepository.findByBranchAndYearAndSemesterAndRegulationId(branch, year, semester, regulationId);
            System.out.println("✅ Subjects found: " + subjects.size());
            return ResponseEntity.ok(subjects);
        } catch (Exception e) {
            System.err.println("❌ Error fetching subjects: " + e.getMessage());
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Failed to fetch subjects");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // Get students in a section (admin view)
   @GetMapping("/students/section/{sectionId}")
public ResponseEntity<?> getStudentsInSection(@PathVariable String sectionId) {
    try {
        System.out.println("📌 Fetching students for section: " + sectionId);
        List<Student> students = studentRepository.findBySectionId(sectionId);
        System.out.println("✅ Students found: " + students.size());
        return ResponseEntity.ok(students);
    } catch (Exception e) {
        System.err.println("❌ Error fetching students: " + e.getMessage());
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("message", "Failed to fetch students");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}


    // Add or update marks (admin only)
    @PostMapping("/add")
    public ResponseEntity<?> addMarks(@RequestBody MarksRequest request) {
        try {
            System.out.println("📌 Adding marks for student: " + request.getStudentId());
            
            Student student = studentRepository.findById(request.getStudentId())
                    .orElseThrow(() -> new RuntimeException("Student not found"));
            
            Subject subject = subjectRepository.findById(request.getSubjectId())
                    .orElseThrow(() -> new RuntimeException("Subject not found"));

            Marks marks = marksRepository.findByStudentIdAndSubjectIdAndSemester(
                    request.getStudentId(), request.getSubjectId(), request.getSemester())
                    .orElse(new Marks(student, subject, request.getSemester()));

            marks.setInternalMarks(new BigDecimal(request.getInternalMarks()));
            marks.setExternalMarks(new BigDecimal(request.getExternalMarks()));
            marks.calculateTotalMarks();
            marks.setUpdatedAt(LocalDateTime.now());

            marksRepository.save(marks);
            System.out.println("✅ Marks saved successfully");
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Marks added successfully");
            response.put("marks", marks);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("❌ Error adding marks: " + e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to add marks: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // Get student's marks for a semester (student view)
    @GetMapping("/student/semester/{semester}")
    public ResponseEntity<?> getStudentMarksBySemester(@PathVariable Integer semester) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            
            System.out.println("📌 Fetching marks for student: " + email + ", Semester: " + semester);
            
            Student student = studentRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Student not found"));

            List<Marks> marks = marksRepository.findByStudentIdAndSemester(student.getId(), semester);
            
            List<Map<String, Object>> response = marks.stream().map(m -> {
                Map<String, Object> markMap = new HashMap<>();
                markMap.put("id", m.getId());
                markMap.put("subjectCode", m.getSubject().getSubjectCode());
                markMap.put("subjectName", m.getSubject().getSubjectName());
                markMap.put("internalMarks", m.getInternalMarks());
                markMap.put("externalMarks", m.getExternalMarks());
                markMap.put("totalMarks", m.getTotalMarks());
                markMap.put("grade", m.getGrade());
                markMap.put("status", m.getStatus());
                return markMap;
            }).collect(Collectors.toList());

            System.out.println("✅ Marks found: " + response.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("❌ Error fetching marks: " + e.getMessage());
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Failed to fetch marks");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // Request DTO
    public static class MarksRequest {
        private Long studentId;
        private Long subjectId;
        private Integer semester;
        private Double internalMarks;
        private Double externalMarks;

        public Long getStudentId() { return studentId; }
        public void setStudentId(Long studentId) { this.studentId = studentId; }

        public Long getSubjectId() { return subjectId; }
        public void setSubjectId(Long subjectId) { this.subjectId = subjectId; }

        public Integer getSemester() { return semester; }
        public void setSemester(Integer semester) { this.semester = semester; }

        public Double getInternalMarks() { return internalMarks; }
        public void setInternalMarks(Double internalMarks) { this.internalMarks = internalMarks; }

        public Double getExternalMarks() { return externalMarks; }
        public void setExternalMarks(Double externalMarks) { this.externalMarks = externalMarks; }
    }
}
