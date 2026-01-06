package com.rfid.tracker.controller;

import com.rfid.tracker.entity.*;
import com.rfid.tracker.repository.*;
import com.rfid.tracker.service.AttendanceService;
import com.rfid.tracker.dto.AttendanceBatchRequest;
import com.rfid.tracker.dto.AttendanceMarkRequest;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/attendance")
@CrossOrigin(origins = "http://localhost:5173")
public class AttendanceController {

    private static final Logger logger = LoggerFactory.getLogger(AttendanceController.class);

    @Autowired
    private AttendanceRepository attendanceRepository;
    @Autowired
    private StudentRepository studentRepository;
    @Autowired
    private StaffRepository staffRepository;
    @Autowired
    private AttendanceSummaryRepository attendanceSummaryRepository;
    @Autowired
    private SemesterConfigRepository semesterConfigRepository;
    @Autowired
    private AttendanceService attendanceService;

    // HARDWARE REPOSITORIES
    @Autowired
    private FingerprintTemplateRepository fingerprintTemplateRepository;
    @Autowired
    private RfidCardRepository rfidCardRepository;

    // ADDED NEW LOG REPOSITORIES FOR STAGING
    @Autowired
    private MorningFingerprintLogRepository morningFingerprintLogRepository;
    @Autowired
    private PeriodAttendanceLogRepository periodAttendanceLogRepository;
    @Autowired
    private StaffEntryLogRepository staffEntryLogRepository;

    // NEW REPOSITORIES FOR CODE VERIFICATION + TIMETABLE
    @Autowired
    private VerificationCodeRepository verificationCodeRepository;
    @Autowired
    private TimetableRepository timetableRepository;

    // ========== EXISTING ENDPOINTS ==========

    @GetMapping("/view/student/{sectionId}/{year}/{month}")
    public ResponseEntity<Map<String, Object>> getStudentAttendanceGrid(
            @PathVariable String sectionId,
            @PathVariable int year,
            @PathVariable int month) {
        try {
            YearMonth ym = YearMonth.of(year, month);
            LocalDate startDate = ym.atDay(1);
            LocalDate endDate = ym.atEndOfMonth();

            List<Student> students = studentRepository.findBySectionId(sectionId);
            List<Map<String, Object>> attendanceList = new ArrayList<>();

            for (Student student : students) {
                Map<String, Object> studentData = new HashMap<>();
                studentData.put("id", student.getId());
                studentData.put("name", student.getName());
                studentData.put("registrationNumber", student.getRegistrationNumber());

                List<Attendance> records = attendanceRepository.findByStudentIdAndDateBetween(
                        student.getId(), startDate, endDate);

                Map<String, String> dayStatus = new HashMap<>();
                for (Attendance record : records) {
                    dayStatus.put(record.getDate().toString(), record.getStatus().toString());
                }

                studentData.put("attendance", dayStatus);
                attendanceList.add(studentData);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("students", attendanceList);
            response.put("totalDays", ym.lengthOfMonth());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Error fetching attendance grid: " + e.getMessage());
            e.printStackTrace();

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Failed to fetch student attendance data");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/month/{sectionId}/{yearMonth}")
    public ResponseEntity<List<Map<String, Object>>> getAttendanceByMonth(
            @PathVariable String sectionId,
            @PathVariable String yearMonth) {
        try {
            System.out.println("Fetching attendance for section: " + sectionId + ", Month: " + yearMonth);
            YearMonth ym = YearMonth.parse(yearMonth);
            LocalDate startDate = ym.atDay(1);
            LocalDate endDate = ym.atEndOfMonth();

            List<Student> students = studentRepository.findBySectionId(sectionId);
            List<Map<String, Object>> attendanceData = new ArrayList<>();

            for (Student student : students) {
                List<Attendance> records = attendanceRepository.findByStudentIdAndDateBetween(
                        student.getId(), startDate, endDate);

                Map<String, Object> studentData = new HashMap<>();
                studentData.put("studentId", student.getId());
                studentData.put("studentName", student.getName());
                studentData.put("registrationNumber", student.getRegistrationNumber());
                studentData.put("section", student.getSection());
                studentData.put("records", records);

                attendanceData.add(studentData);
            }

            System.out.println("Attendance data fetched for " + students.size() + " students");
            return ResponseEntity.ok(attendanceData);
        } catch (Exception e) {
            System.err.println("Error fetching attendance: " + e.getMessage());
            e.printStackTrace();

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Failed to fetch attendance data");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonList(errorResponse));
        }
    }

    @PostMapping("/mark")
    public ResponseEntity<Map<String, Object>> markAttendance(@RequestBody AttendanceRequest request) {
        try {
            System.out.println("Marking attendance for student: " + request.getStudentId());
            Student student = studentRepository.findById(request.getStudentId())
                    .orElseThrow(() -> new RuntimeException("Student not found"));

            Optional<Attendance> existingAttendance = attendanceRepository
                    .findByStudentIdAndDate(request.getStudentId(), request.getDate());

            Attendance attendance;
            if (existingAttendance.isPresent()) {
                attendance = existingAttendance.get();
                attendance.setStatus(AttendanceStatus.valueOf(request.getStatus().toUpperCase()));
                System.out.println("Updating existing attendance");
            } else {
                attendance = new Attendance();
                attendance.setStudentId(request.getStudentId());
                attendance.setDate(request.getDate());
                attendance.setStatus(AttendanceStatus.valueOf(request.getStatus().toUpperCase()));
                attendance.setSectionId(student.getSectionId());
                attendance.setBranch(student.getBranch());
                attendance.setUserIdentifier(student.getRegistrationNumber());
                attendance.setUserType(Attendance.UserType.STUDENT);
                System.out.println("Creating new attendance record");
            }

            attendanceRepository.save(attendance);
            System.out.println("Attendance marked successfully");

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Attendance marked successfully");
            response.put("attendance", attendance);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Error marking attendance: " + e.getMessage());
            e.printStackTrace();

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to mark attendance: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/student/month/{yearMonth}")
    public ResponseEntity<Map<String, Object>> getStudentAttendanceByMonth(@PathVariable String yearMonth) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();

            System.out.println("Fetching student attendance for email: " + email + ", Month: " + yearMonth);
            Student student = studentRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Student not found"));

            YearMonth ym = YearMonth.parse(yearMonth);
            LocalDate startDate = ym.atDay(1);
            LocalDate endDate = ym.atEndOfMonth();

            List<Attendance> records = attendanceRepository
                    .findByStudentIdAndDateBetween(student.getId(), startDate, endDate);

            Map<String, Object> response = new HashMap<>();
            response.put("studentName", student.getName());
            response.put("section", student.getSection());
            response.put("sectionId", student.getSectionId());
            response.put("month", yearMonth);
            response.put("records", records);

            long presentCount = records.stream()
                    .filter(r -> r.getStatus() == AttendanceStatus.PRESENT)
                    .count();
            long absentCount = records.stream()
                    .filter(r -> r.getStatus() == AttendanceStatus.ABSENT)
                    .count();

            response.put("presentCount", presentCount);
            response.put("absentCount", absentCount);
            response.put("totalDays", records.size());

            System.out.println("Student attendance fetched: " + records.size() + " days");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Error fetching student attendance: " + e.getMessage());
            e.printStackTrace();

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Failed to fetch attendance");

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ========== STAFF ATTENDANCE - FIXED WITH DEFENSIVE CHECKS ==========

    @GetMapping("/view/staff/{branch}/{year}/{month}")
    public ResponseEntity<Map<String, Object>> getStaffAttendanceGrid(
            @PathVariable String branch,
            @PathVariable int year,
            @PathVariable int month) {
        try {
            System.out.println("📌 Fetching staff attendance for branch: " + branch);

            YearMonth ym = YearMonth.of(year, month);
            LocalDate startDate = ym.atDay(1);
            LocalDate endDate = ym.atEndOfMonth();

            // ✅ DEFENSIVE: Handle null/empty branch
            if (branch == null || branch.trim().isEmpty()) {
                System.out.println("⚠️ Branch is null/empty, returning empty list");
                Map<String, Object> response = new HashMap<>();
                response.put("staff", Collections.emptyList());
                response.put("totalDays", ym.lengthOfMonth());
                return ResponseEntity.ok(response);
            }

            // ✅ DEFENSIVE: Safe load with null checking
            List<Staff> allStaff = staffRepository.findAll();
            if (allStaff == null) {
                allStaff = new ArrayList<>();
            }

            // ✅ Filter with null guards
            List<Staff> staffMembers = allStaff.stream()
                    .filter(Objects::nonNull)
                    .filter(s -> s.getBranch() != null)
                    .filter(s -> branch.equalsIgnoreCase(s.getBranch()))
                    .collect(Collectors.toList());

            System.out.println("✅ Found " + staffMembers.size() + " staff members for branch " + branch);

            // ✅ DEFENSIVE: Safe load of all attendance records once
            List<Attendance> allRecords = attendanceRepository.findAll();
            if (allRecords == null) {
                allRecords = new ArrayList<>();
            }

            List<Map<String, Object>> attendanceList = new ArrayList<>();

            for (Staff staff : staffMembers) {
                try {
                    Map<String, Object> staffData = new HashMap<>();
                    staffData.put("id", staff.getId());
                    staffData.put("name", staff.getName());
                    staffData.put("staffId", staff.getStaffId());

                    // ✅ DEFENSIVE: Filter attendance with multiple null checks
                    List<Attendance> records = allRecords.stream()
                            .filter(Objects::nonNull)
                            .filter(a -> a.getUserIdentifier() != null)
                            .filter(a -> a.getDate() != null)
                            .filter(a -> a.getUserType() != null)
                            .filter(a -> staff.getStaffId() != null && staff.getStaffId().equals(a.getUserIdentifier()))
                            .filter(a -> !a.getDate().isBefore(startDate) && !a.getDate().isAfter(endDate))
                            .filter(a -> a.getUserType() == Attendance.UserType.STAFF)
                            .collect(Collectors.toList());

                    // ✅ Calculate P/L/A counts safely
                    long presentCount = records.stream()
                            .filter(r -> r.getStatus() == AttendanceStatus.PRESENT)
                            .count();
                    long lateCount = records.stream()
                            .filter(r -> r.getStatus() == AttendanceStatus.LATE)
                            .count();
                    long absentCount = records.stream()
                            .filter(r -> r.getStatus() == AttendanceStatus.ABSENT)
                            .count();

                    Map<String, String> dayStatus = new HashMap<>();
                    for (Attendance record : records) {
                        if (record.getDate() != null && record.getStatus() != null) {
                            dayStatus.put(record.getDate().toString(), record.getStatus().toString());
                        }
                    }

                    staffData.put("attendance", dayStatus);
                    staffData.put("present", presentCount);
                    staffData.put("late", lateCount);
                    staffData.put("absent", absentCount);

                    attendanceList.add(staffData);
                } catch (Exception staffEx) {
                    System.err.println("❌ Error processing staff " + staff.getStaffId() + ": " + staffEx.getMessage());
                    staffEx.printStackTrace();
                    // Continue with next staff member
                }
            }

            Map<String, Object> response = new HashMap<>();
            response.put("staff", attendanceList);
            response.put("totalDays", ym.lengthOfMonth());

            System.out.println("✅ Staff attendance grid prepared, returned " + attendanceList.size() + " staff records");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("❌ Error in getStaffAttendanceGrid: " + e.getMessage());
            e.printStackTrace();

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Failed to fetch staff attendance data: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/student/live-summary")
    public ResponseEntity<Map<String, Object>> getLiveAttendanceSummary() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            System.out.println("Fetching live attendance summary for: " + email);

            Student student = studentRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Student not found"));

            String currentAcademicYear = "2024-2025";
            Integer currentSemester = student.getSemester();
            Integer currentYear = student.getYear();
            Long regulationId = student.getRegulationId();

            if (regulationId == null) {
                regulationId = 1L;
            }

            Optional<SemesterConfig> semesterConfigOpt = semesterConfigRepository
                    .findByAcademicYearAndRegulationIdAndYearAndSemester(
                            currentAcademicYear, regulationId, currentYear, currentSemester);

            if (semesterConfigOpt.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("message", "Semester dates not configured yet");
                return ResponseEntity.ok(response);
            }

            SemesterConfig config = semesterConfigOpt.get();
            LocalDate today = LocalDate.now();
            LocalDate endDate = today.isAfter(config.getEndDate()) ? config.getEndDate() : today;

            List<Attendance> records = attendanceRepository.findByStudentIdAndDateBetween(
                    student.getId(), config.getStartDate(), endDate);

            long total = records.size();
            long attended = records.stream()
                    .filter(a -> a.getStatus() == AttendanceStatus.PRESENT)
                    .count();

            double percentage = total > 0 ? (double) attended / total * 100 : 0.0;

            String status;
            if (percentage < 65) {
                status = "DETAINED";
            } else if (percentage < 75) {
                status = "CONDONATION";
            } else {
                status = "NORMAL";
            }

            AttendanceSummary summary = attendanceSummaryRepository
                    .findByStudentIdAndSemesterAndAcademicYear(
                            student.getId(), currentSemester, currentAcademicYear)
                    .orElse(new AttendanceSummary(student.getId(), currentSemester, currentAcademicYear));

            summary.setTotalClasses((int) total);
            summary.setAttendedClasses((int) attended);
            summary.calculateAttendance();
            attendanceSummaryRepository.save(summary);

            student.setAttendanceStatus(status);
            studentRepository.save(student);

            Map<String, Object> response = new HashMap<>();
            response.put("percentage", Math.round(percentage * 100.0) / 100.0);
            response.put("totalClasses", total);
            response.put("attendedClasses", attended);
            response.put("status", status);
            response.put("startDate", config.getStartDate());
            response.put("endDate", config.getEndDate());

            System.out.println("Live attendance summary calculated");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Error fetching live attendance summary: " + e.getMessage());
            e.printStackTrace();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    // ========== HARDWARE ENDPOINTS FOR LOGGING (FINGERPRINT + RFID) ==========

    @PostMapping("/log-fingerprint")
    public ResponseEntity<Map<String, Object>> logFingerprintScan(@RequestBody Map<String, Object> request) {
        try {
            Integer fingerprintId = ((Number) request.get("fingerprintId")).intValue();
            String scanDateStr = (String) request.get("scanDate");
            String scanTimeStr = (String) request.get("scanTime");

            LocalDate scanDate = scanDateStr != null ? LocalDate.parse(scanDateStr) : LocalDate.now();
            LocalTime scanTime = scanTimeStr != null ? LocalTime.parse(scanTimeStr) : LocalTime.now();

            Optional<FingerprintTemplate> templateOpt = fingerprintTemplateRepository
                    .findByFingerprintIdAndIsActiveTrue(fingerprintId);

            if (templateOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("status", "ERROR", "message", "Fingerprint ID not enrolled"));
            }

            FingerprintTemplate template = templateOpt.get();
            String userIdentifier = template.getUserIdentifier();
            String userTypeStr = template.getUserType().name();

            // After 12 PM treat as LOGOUT (evening scan)
            boolean isEvening = scanTime.isAfter(LocalTime.of(12, 0));

            if (!isEvening) {
                // MORNING LOGIN
                String status = scanTime.isBefore(LocalTime.of(9, 0)) ? "ONTIME" : "LATE";

                MorningFingerprintLog log = new MorningFingerprintLog();
                log.setUserType(MorningFingerprintLog.UserType.valueOf(userTypeStr));
                log.setUserIdentifier(userIdentifier);
                log.setFingerprintId(fingerprintId);
                log.setScanDate(scanDate);
                log.setScanTime(scanTime);
                log.setStatus(MorningFingerprintLog.Status.valueOf(status));
                log.setProcessed(false);

                if (userTypeStr.equals("STUDENT")) {
                    studentRepository.findByRegistrationNumber(userIdentifier).ifPresent(s -> {
                        log.setSectionId(s.getSectionId());
                        log.setBranch(s.getBranch());
                    });
                }

                morningFingerprintLogRepository.save(log);
                System.out.println("LOG: Morning Fingerprint Saved for " + userIdentifier + " (" + userTypeStr + ")");

                return ResponseEntity.ok(Map.of(
                        "status", "LOG_SUCCESS",
                        "message", "Log Saved",
                        "scanStatus", status,
                        "userType", userTypeStr
                ));
            } else {
                // EVENING LOGOUT
                System.out.println("LOGOUT: Evening Fingerprint for " + userIdentifier + " (" + userTypeStr + ")");

                String finalStatus = "UNKNOWN";
                if (userTypeStr.equals("STUDENT")) {
                    finalStatus = attendanceService.calculateDailyStudentAttendance(userIdentifier, scanDate);
                    System.out.println("STUDENT ATTENDANCE CALCULATED: " + finalStatus);
                } else if (userTypeStr.equals("STAFF")) {
                    finalStatus = attendanceService.calculateDailyStaffAttendance(userIdentifier, scanDate);
                    System.out.println("STAFF ATTENDANCE CALCULATED: " + finalStatus);
                }

                return ResponseEntity.ok(Map.of(
                        "status", "CALCULATION_SUCCESS",
                        "message", "Attendance Finalized",
                        "finalStatus", finalStatus,
                        "userType", userTypeStr
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("status", "ERROR", "message", e.getMessage()));
        }
    }

    @PostMapping("/log-rfid")
    public ResponseEntity<Map<String, Object>> logRfidScan(@RequestBody Map<String, Object> request) {
        try {
            String cardUid = (String) request.get("cardUid");
            String scanDateStr = (String) request.get("scanDate");
            String scanTimeStr = (String) request.get("scanTime");
            String roomNumber = (String) request.get("roomNumber");

            LocalDate scanDate = scanDateStr != null ? LocalDate.parse(scanDateStr) : LocalDate.now();
            LocalTime scanTime = scanTimeStr != null ? LocalTime.parse(scanTimeStr) : LocalTime.now();

            Optional<RfidCard> cardOpt = rfidCardRepository.findByCardUidAndIsActiveTrue(cardUid);

            if (cardOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("status", "ERROR", "message", "Card not registered"));
            }

            RfidCard card = cardOpt.get();

            if (card.getUserType() == RfidCard.UserType.STUDENT) {
                // STUDENT PERIOD ATTENDANCE
                Optional<Student> studentOpt = studentRepository.findById(card.getUserId());

                if (studentOpt.isEmpty()) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(Map.of("status", "ERROR", "message", "Student not found"));
                }

                Student student = studentOpt.get();
                String timeSlot = String.format("%02d:00-%02d:00", scanTime.getHour(), scanTime.getHour() + 1);

                PeriodAttendanceLog log = new PeriodAttendanceLog();
                log.setStudentRegistrationNumber(student.getRegistrationNumber());
                log.setSectionId(student.getSectionId());
                log.setScanDate(scanDate);
                log.setScanTime(scanTime);
                log.setRoomNumber(roomNumber);
                log.setTimeSlot(timeSlot);
                log.setVerifiedVia("RFID");

                periodAttendanceLogRepository.save(log);
                System.out.println("LOG: Saved Period Log for Student: " + student.getName());

            } else {
                // STAFF ENTRY LOG WITH TIMETABLE MATCHING
                Optional<Staff> staffOpt = staffRepository.findById(card.getUserId());

                if (staffOpt.isEmpty()) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(Map.of("status", "ERROR", "message", "Staff not found"));
                }

                Staff staff = staffOpt.get();
                String dayOfWeek = scanDate.getDayOfWeek()
                        .getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.ENGLISH);

                Optional<Timetable> timetableOpt = timetableRepository
                        .findByRoomAndDayAndTimeRange(roomNumber, dayOfWeek, scanTime);

                String timeSlotForLog;
                if (timetableOpt.isPresent()) {
                    timeSlotForLog = timetableOpt.get().getTimeSlot();
                } else {
                    timeSlotForLog = String.format("%02d:00-%02d:00", scanTime.getHour(), scanTime.getHour() + 1);
                }

                // Prevent duplicate log
                Optional<StaffEntryLog> existingLog = staffEntryLogRepository
                        .findByStaffIdNumberAndEntryDateAndTimeSlot(staff.getStaffId(), scanDate, timeSlotForLog);

                if (existingLog.isPresent()) {
                    System.out.println("DUPLICATE: Staff " + staff.getStaffId() + " already logged at " + timeSlotForLog);
                    return ResponseEntity.ok(Map.of(
                            "status", "DUPLICATE_SKIPPED",
                            "message", "Entry already logged for this time slot"
                    ));
                }

                StaffEntryLog log = new StaffEntryLog();
                log.setStaffIdNumber(staff.getStaffId());
                log.setStaffDbId(staff.getId().intValue());
                log.setRoomNumber(roomNumber);
                log.setEntryDate(scanDate);
                log.setEntryTime(scanTime);
                log.setEntryDateTime(LocalDateTime.now());
                log.setDayOfWeek(dayOfWeek);
                log.setRfidTag(cardUid);
                log.setTimeSlot(timeSlotForLog);

                if (timetableOpt.isPresent()) {
                    Timetable timetable = timetableOpt.get();
                    log.setSectionId(timetable.getSectionId());
                    log.setTimetableId(timetable.getId());
                    log.setStatus(StaffEntryLog.Status.PROCESSED);
                    log.setExpectedTime(timetable.getScheduledStartTime());

                    staffEntryLogRepository.save(log);

                    String code = generateRandomCode();
                    VerificationCode verificationCode = new VerificationCode(
                            staff.getStaffId(),
                            timetable.getSubject(),
                            timetable.getSectionId(),
                            code,
                            LocalDateTime.now().plusMinutes(45),
                            timetable.getId()
                    );
                    verificationCodeRepository.save(verificationCode);

                    System.out.println("MATCHED: Staff Entry Log for " + staff.getName()
                            + " - Subject: " + timetable.getSubject());

                    return ResponseEntity.ok(Map.of(
                            "status", "LOG_SUCCESS_WITH_CODE",
                            "message", "Log Saved, Code Generated",
                            "code", code,
                            "sectionId", timetable.getSectionId(),
                            "subject", timetable.getSubject()
                    ));
                } else {
                    log.setStatus(StaffEntryLog.Status.FAILED);
                    staffEntryLogRepository.save(log);
                    System.out.println("NO MATCH: Staff Entry Log saved with FAILED status");

                    return ResponseEntity.ok(Map.of(
                            "status", "LOG_SAVED_NO_MATCH",
                            "message", "Log saved but no matching class found",
                            "hint", "Check timetable room, day, and time range."
                    ));
                }
            }

            return ResponseEntity.ok(Map.of("status", "LOG_SUCCESS", "message", "Log Saved"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("status", "ERROR", "message", e.getMessage()));
        }
    }

    @PostMapping("/rfid-scan")
    public ResponseEntity<Map<String, Object>> logRfidAttendanceDirectly(@RequestBody Map<String, Object> request) {
        try {
            String cardUid = (String) request.get("cardUid");
            String scanDateStr = (String) request.get("scanDate");
            String scanTimeStr = (String) request.get("scanTime");
            String roomNumber = (String) request.get("roomNumber");

            LocalDate scanDate = scanDateStr != null ? LocalDate.parse(scanDateStr) : LocalDate.now();
            LocalTime scanTime = scanTimeStr != null ? LocalTime.parse(scanTimeStr) : LocalTime.now();

            System.out.println("CONTROLLER: RFID Scan Received for " + cardUid);
            return attendanceService.logRfidAttendance(cardUid, scanDate, scanTime, roomNumber);
        } catch (Exception e) {
            System.err.println("CONTROLLER: Error in rfid-scan: " + e.getMessage());
            e.printStackTrace();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("status", "ERROR", "message", e.getMessage()));
        }
    }

    // ========== CODE VERIFICATION ENDPOINTS ==========

    @PostMapping("/verify-code")
    public ResponseEntity<Map<String, Object>> verifyCodeEntry(@RequestBody Map<String, Object> request) {
        try {
            String studentRegNo = (String) request.get("studentRegNo");
            String code = (String) request.get("code");
            String sectionId = (String) request.get("sectionId");
            String dateStr = (String) request.get("date");
            String timeSlot = (String) request.get("timeSlot");

            System.out.println("ENDPOINT: verify-code called for Student " + studentRegNo);

            if (studentRegNo == null || code == null || sectionId == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Missing required fields"));
            }

            LocalDate date = dateStr != null ? LocalDate.parse(dateStr) : LocalDate.now();

            if (timeSlot == null) {
                timeSlot = "GENERAL";
            }

            Map<String, Object> result = attendanceService
                    .verifyStudentCodeEntry(studentRegNo, code, sectionId, date, timeSlot);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            System.err.println("Error in verify-code: " + e.getMessage());
            e.printStackTrace();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @GetMapping("/code/active/{sectionId}")
    public ResponseEntity<Map<String, Object>> getActiveCode(@PathVariable String sectionId) {
        try {
            System.out.println("ENDPOINT: code/active called for section " + sectionId);

            Optional<VerificationCode> codeOpt = attendanceService.getActiveCodeForSection(sectionId);
            Map<String, Object> response = new HashMap<>();

            if (codeOpt.isPresent()) {
                VerificationCode code = codeOpt.get();
                response.put("success", true);
                response.put("codeActive", true);
                response.put("code", code.getCode());
                response.put("staffId", code.getStaffId());
                response.put("className", code.getClassName());
                response.put("validUntil", code.getValidUntil().toString());
                response.put("generatedTime", code.getGeneratedTime().toString());

                long minutesRemaining = java.time.temporal.ChronoUnit.MINUTES
                        .between(LocalDateTime.now(), code.getValidUntil());
                response.put("minutesRemaining", minutesRemaining);

                System.out.println("Active code found: " + code.getCode());
            } else {
                response.put("success", true);
                response.put("codeActive", false);
                response.put("message", "No active verification code for this section");
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Error in code/active: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @GetMapping("/code/validate/{code}")
    public ResponseEntity<Map<String, Object>> validateCode(@PathVariable String code) {
        try {
            boolean isValid = attendanceService.isCodeValid(code);
            return ResponseEntity.ok(Map.of("valid", isValid));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("valid", false));
        }
    }

    @GetMapping("/code/stats/{staffId}/{date}")
    public ResponseEntity<Map<String, Object>> getCodeStatistics(
            @PathVariable String staffId,
            @PathVariable String date) {
        try {
            LocalDate scanDate = LocalDate.parse(date);
            List<VerificationCode> codes = verificationCodeRepository.findByStaffIdAndToday(staffId);

            Map<String, Object> stats = new HashMap<>();
            stats.put("staffId", staffId);
            stats.put("date", scanDate.toString());
            stats.put("totalCodesGenerated", codes.size());

            int totalStudentsUsedCode = 0;
            for (VerificationCode c : codes) {
                totalStudentsUsedCode += c.getCodeEnteredCount();
            }

            stats.put("totalStudentsUsedCode", totalStudentsUsedCode);
            stats.put("codes", codes);

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // ========== ADDITIONAL HELPER ENDPOINTS ==========

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<Attendance>> getStudentAttendance(@PathVariable Long studentId) {
        try {
            List<Attendance> all = attendanceRepository.findAll();
            List<Attendance> attendance = all.stream()
                    .filter(a -> a.getStudentId() != null && a.getStudentId().equals(studentId))
                    .collect(Collectors.toList());

            return ResponseEntity.ok(attendance);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.emptyList());
        }
    }

    @PostMapping("/mark-bulk")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Attendance>> markBulkAttendance(@RequestBody AttendanceBatchRequest request) {
        try {
            List<Attendance> markedAttendances = attendanceService.markBulkAttendance(request);
            return ResponseEntity.ok(markedAttendances);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.emptyList());
        }
    }

    @GetMapping("/staff-records/{staffId}/{year}/{month}")
    public ResponseEntity<List<Attendance>> getStaffAttendance(
            @PathVariable String staffId,
            @PathVariable int year,
            @PathVariable int month) {
        try {
            YearMonth ym = YearMonth.of(year, month);
            LocalDate startDate = ym.atDay(1);
            LocalDate endDate = ym.atEndOfMonth();

            List<Attendance> allRecords = attendanceRepository.findAll();
            List<Attendance> staffRecords = allRecords.stream()
                    .filter(a -> staffId.equals(a.getUserIdentifier()))
                    .filter(a -> !a.getDate().isBefore(startDate) && !a.getDate().isAfter(endDate))
                    .filter(a -> a.getUserType() == Attendance.UserType.STAFF)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(staffRecords);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.emptyList());
        }
    }

    @GetMapping("/staff/{staffId}/today")
    public ResponseEntity<Map<String, String>> getStaffTodayStatus(@PathVariable String staffId) {
        try {
            LocalDate today = LocalDate.now();
            List<Attendance> allRecords = attendanceRepository.findAll();

            Optional<Attendance> attendance = allRecords.stream()
                    .filter(a -> staffId.equals(a.getUserIdentifier()))
                    .filter(a -> a.getDate().equals(today))
                    .filter(a -> a.getUserType() == Attendance.UserType.STAFF)
                    .findFirst();

            String status = "ABSENT";
            if (attendance.isPresent()) {
                status = attendance.get().getStatus().toString();
            }

            return ResponseEntity.ok(Collections.singletonMap("status", status));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("status", "ERROR"));
        }
    }

    @GetMapping("/student/today")
    public ResponseEntity<?> getStudentTodayAttendance() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUserName = authentication.getName();

            Optional<Student> studentOpt = studentRepository.findByEmail(currentUserName);

            if (studentOpt.isPresent()) {
                Student student = studentOpt.get();
                LocalDate today = LocalDate.now();

                Optional<Attendance> attendance = attendanceRepository
                        .findByStudentIdAndDate(student.getId(), today);

                Map<String, Object> response = new HashMap<>();

                if (attendance.isPresent()) {
                    response.put("status", attendance.get().getStatus());
                    response.put("date", today);
                } else {
                    response.put("status", "NOT_MARKED");
                    response.put("date", today);
                }

                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Student not found");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error");
        }
    }

    @PostMapping("/upload-students")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> uploadStudents(@RequestParam("file") MultipartFile file) {
        try {
            return ResponseEntity.ok("Students uploaded successfully");
        } catch (Exception e) {
            logger.error("Error uploading students file", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error uploading file: " + e.getMessage());
        }
    }

    @GetMapping("/export-students/{sectionId}")
    public ResponseEntity<InputStreamResource> exportStudents(@PathVariable String sectionId) {
        try {
            List<Student> students = studentRepository.findBySectionId(sectionId);
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // ========== HELPER METHODS ==========

    private String generateRandomCode() {
        return String.format("%06d", new java.util.Random().nextInt(1000000));
    }

    // ========== INNER CLASS ==========

    public static class AttendanceRequest {
        private Long studentId;
        private LocalDate date;
        private String status;

        public Long getStudentId() {
            return studentId;
        }

        public void setStudentId(Long id) {
            this.studentId = id;
        }

        public LocalDate getDate() {
            return date;
        }

        public void setDate(LocalDate date) {
            this.date = date;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }
}