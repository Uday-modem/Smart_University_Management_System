package com.rfid.tracker.controller;

import com.rfid.tracker.entity.Admin;
import com.rfid.tracker.entity.Student;
import com.rfid.tracker.repository.AdminRepository;
import com.rfid.tracker.repository.StudentRepository;
import com.rfid.tracker.service.AuthService;
import com.rfid.tracker.service.StudentService;
import com.rfid.tracker.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private StudentService studentService;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    // ============================================================
    // ✅ ADMIN LOGIN
    // ============================================================
    @PostMapping("/admin/login")
    public ResponseEntity<?> adminLogin(@RequestBody Map<String, String> loginRequest) {
        try {
            System.out.println("\n" + "=".repeat(60));
            System.out.println("🔐 ADMIN LOGIN REQUEST");
            System.out.println("=".repeat(60));

            String email = loginRequest.get("email");
            String password = loginRequest.get("password");

            System.out.println("📧 Email: " + email);
            System.out.println("🔑 Password: [" + (password != null ? password.length() + " chars" : "NULL") + "]");

            // Validations
            if (email == null || email.trim().isEmpty()) {
                System.out.println("❌ ERROR: Email is empty");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("success", false, "message", "Email is required"));
            }

            if (password == null || password.trim().isEmpty()) {
                System.out.println("❌ ERROR: Password is empty");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("success", false, "message", "Password is required"));
            }

            System.out.println("🔍 STEP 1: Searching for admin in database...");
            Optional<Admin> adminOpt = adminRepository.findByEmail(email);

            if (adminOpt.isEmpty()) {
                System.out.println("❌ ERROR: Admin not found in database");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("success", false, "message", "Invalid email or password"));
            }

            Admin admin = adminOpt.get();
            System.out.println("✅ STEP 2: Admin found - " + admin.getName());
            System.out.println("🔐 STEP 3: Verifying password...");

            String storedHashedPassword = admin.getPassword();
            if (storedHashedPassword == null || storedHashedPassword.isEmpty()) {
                System.out.println("❌ ERROR: Password not set in database");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("success", false, "message", "Account error: password not configured"));
            }

            boolean passwordMatches = authService.verifyPassword(password, storedHashedPassword);
            if (!passwordMatches) {
                System.out.println("❌ ERROR: Password verification FAILED");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("success", false, "message", "Invalid email or password"));
            }

            System.out.println("✅ Password verification PASSED");
            System.out.println("🎫 STEP 4: Generating JWT token...");

            String token = jwtUtil.generateToken(admin.getEmail(), admin.getId().toString(), 
                    "ADMIN", admin.getName(), "");

            System.out.println("✅ JWT token generated successfully");
            System.out.println("✅ LOGIN SUCCESSFUL for: " + admin.getName());
            System.out.println("=".repeat(60) + "\n");

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Admin login successful",
                    "token", token,
                    "role", "ADMIN",
                    "admin", Map.of(
                            "id", admin.getId(),
                            "name", admin.getName(),
                            "email", admin.getEmail()
                    )
            ));

        } catch (Exception e) {
            System.out.println("❌ CRITICAL ERROR during admin login: " + e.getMessage());
            e.printStackTrace();
            System.out.println("=".repeat(60) + "\n");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Login error: " + e.getMessage()));
        }
    }

    // ============================================================
    // ✅ STUDENT LOGIN
    // ============================================================
    @PostMapping("/student/login")
    public ResponseEntity<?> studentLogin(@RequestBody Map<String, String> loginRequest) {
        try {
            System.out.println("\n" + "=".repeat(60));
            System.out.println("🔐 STUDENT LOGIN REQUEST");
            System.out.println("=".repeat(60));

            String identifier = loginRequest.get("email");
            String password = loginRequest.get("password");

            System.out.println("📧 Identifier (email/reg): " + identifier);
            System.out.println("🔑 Password: [" + (password != null ? password.length() + " chars" : "NULL") + "]");

            // Validations
            if (identifier == null || identifier.trim().isEmpty()) {
                System.out.println("❌ ERROR: Email/identifier is empty");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("success", false, "message", "Email or Registration Number is required"));
            }

            if (password == null || password.trim().isEmpty()) {
                System.out.println("❌ ERROR: Password is empty");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("success", false, "message", "Password is required"));
            }

            System.out.println("🔍 STEP 1: Searching for student in database...");
            Optional<Student> studentOpt = authService.findStudentByEmailOrRegistration(identifier);

            if (studentOpt.isEmpty()) {
                System.out.println("❌ ERROR: Student not found in database");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("success", false, "message", "Invalid email/registration or password"));
            }

            Student student = studentOpt.get();
            System.out.println("✅ STEP 2: Student found - " + student.getName());
            System.out.println("🔐 STEP 3: Verifying password...");

            String storedHashedPassword = student.getPassword();
            if (storedHashedPassword == null || storedHashedPassword.isEmpty()) {
                System.out.println("❌ ERROR: Password not set in database");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("success", false, "message", "Account error: password not configured"));
            }

            boolean passwordMatches = authService.verifyPassword(password, storedHashedPassword);
            if (!passwordMatches) {
                System.out.println("❌ ERROR: Password verification FAILED");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("success", false, "message", "Invalid email/registration or password"));
            }

            System.out.println("✅ Password verification PASSED");
            System.out.println("🎫 STEP 4: Generating JWT token...");

            String token = jwtUtil.generateToken(
                    student.getEmail(),
                    student.getId().toString(),
                    "STUDENT",
                    student.getName(),
                    student.getRegistrationNumber()
            );

            System.out.println("✅ JWT token generated successfully");
            System.out.println("✅ LOGIN SUCCESSFUL for: " + student.getName());
            System.out.println("=".repeat(60) + "\n");

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Login successful",
                    "token", token,
                    "role", "STUDENT",
                    "student", Map.of(
                            "id", student.getId(),
                            "name", student.getName(),
                            "email", student.getEmail(),
                            "registrationNumber", student.getRegistrationNumber(),
                            "branch", student.getBranch(),
                            "year", student.getYear(),
                            "semester", student.getSemester(),
                            "sectionId", student.getSectionId(),
                            "section", student.getSection()
                    )
            ));

        } catch (Exception e) {
            System.out.println("❌ CRITICAL ERROR during login: " + e.getMessage());
            e.printStackTrace();
            System.out.println("=".repeat(60) + "\n");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Login error: " + e.getMessage()));
        }
    }

    // ============================================================
    // ✅ STUDENT SIGNUP (AUTO-SECTION MODE)
    // ============================================================
    @PostMapping("/student/signup")
    public ResponseEntity<?> studentSignup(@RequestBody Map<String, Object> signupRequest) {
        try {
            System.out.println("\n" + "=".repeat(60));
            System.out.println("👤 STUDENT SIGNUP REQUEST (AUTO-SECTION MODE)");
            System.out.println("=".repeat(60));

            String email = (String) signupRequest.get("email");
            String password = (String) signupRequest.get("password");
            String name = (String) signupRequest.get("name");
            String registrationNumber = (String) signupRequest.get("registrationNumber");
            String branch = (String) signupRequest.get("branch");
            String entryType = (String) signupRequest.get("entryType");

            // Safe Year Extraction
            Object yearObj = signupRequest.get("year");
            if (yearObj == null) {
                System.out.println("❌ ERROR: Year is missing from request");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("success", false, "message", "Year is required"));
            }

            Integer year;
            try {
                year = ((Number) yearObj).intValue();
            } catch (ClassCastException e) {
                System.out.println("❌ ERROR: Year must be a number");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("success", false, "message", "Year must be a valid number (1-4)"));
            }

            System.out.println("📧 Email: " + email);
            System.out.println("👤 Name: " + name);
            System.out.println("📝 Reg#: " + registrationNumber);
            System.out.println("🏢 Branch: " + branch);
            System.out.println("📅 Year: " + year);
            System.out.println("🎓 Entry Type: " + entryType);

            // Validations
            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("success", false, "message", "Email is required"));
            }

            if (password == null || password.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("success", false, "message", "Password is required"));
            }

            if (name == null || name.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("success", false, "message", "Name is required"));
            }

            if (branch == null || branch.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("success", false, "message", "Branch is required"));
            }

            if (registrationNumber == null || registrationNumber.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("success", false, "message", "Registration Number is required"));
            }

            if (year < 1 || year > 4) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("success", false, "message", "Year must be between 1 and 4"));
            }

            if (authService.studentExists(email)) {
                System.out.println("❌ ERROR: Student already exists with email: " + email);
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of("success", false, "message", "Student with this email already exists"));
            }

            if (studentRepository.existsByRegistrationNumber(registrationNumber)) {
                System.out.println("❌ ERROR: Student already exists with registration number: " + registrationNumber);
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of("success", false, "message", "Student with this registration number already exists"));
            }

            System.out.println("✅ Validation passed - creating student...");

            // Hash password
            String hashedPassword = passwordEncoder.encode(password);

            // Calculate semester based on entry type
            Integer semester;
            if (entryType != null && entryType.equalsIgnoreCase("LATERAL")) {
                semester = 3;
                System.out.println("📍 Lateral Entry: Semester 3");
            } else {
                semester = 1;
                System.out.println("📍 Regular Entry: Semester 1");
            }

            // Create Student Object
            Student student = new Student();
            student.setRegistrationNumber(registrationNumber);
            student.setName(name);
            student.setEmail(email);
            student.setPassword(hashedPassword);
            student.setBranch(branch);
            student.setYear(year);
            student.setSemester(semester);
            student.setEntryType(entryType != null ? entryType : "REGULAR");
            student.setAttendanceStatus("ACTIVE");
            student.setCreatedAt(LocalDateTime.now());

            // ✅ AUTO-ASSIGN SECTION (The Magic Happens Here)
            System.out.println("🎯 Calling StudentService.assignSectionAtSignup()...");
            studentService.assignSectionAtSignup(student);

            // Save Final Student
            Student savedStudent = studentRepository.save(student);

            System.out.println("✅ Student created successfully with ID: " + savedStudent.getId());
            System.out.println("✅ SIGNUP SUCCESSFUL for: " + name);
            System.out.println("   Assigned Section: " + savedStudent.getSection());
            System.out.println("   Section ID: " + savedStudent.getSectionId());
            System.out.println("=".repeat(60) + "\n");

            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "success", true,
                    "message", "Signup successful! Section automatically assigned.",
                    "student", Map.of(
                            "id", savedStudent.getId(),
                            "name", savedStudent.getName(),
                            "email", savedStudent.getEmail(),
                            "registrationNumber", savedStudent.getRegistrationNumber(),
                            "branch", savedStudent.getBranch(),
                            "year", savedStudent.getYear(),
                            "semester", savedStudent.getSemester(),
                            "sectionId", savedStudent.getSectionId(),
                            "section", savedStudent.getSection(),
                            "entryType", savedStudent.getEntryType()
                    )
            ));

        } catch (Exception e) {
            System.err.println("❌ ERROR during signup: " + e.getMessage());
            e.printStackTrace();
            System.out.println("=".repeat(60) + "\n");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Signup error: " + e.getMessage()));
        }
    }

    // ============================================================
    // ✅ CHECK LOGIN STATUS
    // ============================================================
    @GetMapping("/status")
    public ResponseEntity<?> checkStatus(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.ok(Map.of("success", false, "authenticated", false));
            }

            String token = authHeader.substring(7);
            boolean isValid = jwtUtil.validateToken(token);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "authenticated", isValid
            ));

        } catch (Exception e) {
            System.err.println("❌ Error checking status: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Error: " + e.getMessage()));
        }
    }

    // ============================================================
    // ✅ LOGOUT
    // ============================================================
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        try {
            System.out.println("🚪 User logged out");
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Logged out successfully"
            ));

        } catch (Exception e) {
            System.err.println("❌ Logout error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Logout error"));
        }
    }
}
