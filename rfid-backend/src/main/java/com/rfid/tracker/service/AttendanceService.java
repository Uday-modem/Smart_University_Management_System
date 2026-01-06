package com.rfid.tracker.service;

import com.rfid.tracker.dto.AttendanceMarkRequest;
import com.rfid.tracker.dto.AttendanceBatchRequest;
import com.rfid.tracker.entity.*;
import com.rfid.tracker.repository.*;
import com.rfid.tracker.util.CodeGenerationUtil;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
public class AttendanceService {

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private StaffRepository staffRepository;

    @Autowired
    private SectionRepository sectionRepository;

    @Autowired
    private StaffExpertiseRepository staffExpertiseRepository;

    // ✅ ADDED REPOSITORIES FOR TWO-CONDITION LATE ALERT SYSTEM
    @Autowired
    private MorningFingerprintLogRepository morningFingerprintLogRepository;

    @Autowired
    private PeriodAttendanceLogRepository periodAttendanceLogRepository;

    @Autowired
    private AttendanceSummaryRepository attendanceSummaryRepository;

    @Autowired
    private SemesterConfigRepository semesterConfigRepository;

    @Autowired
    private StaffEntryLogRepository staffEntryLogRepository;

    @Autowired
    private VerificationCodeRepository verificationCodeRepository;

    @Autowired
    private RfidCardRepository rfidCardRepository;

    @Autowired
    private TimetableRepository timetableRepository;

    @Autowired
    private StaffLateAlertRepository staffLateAlertRepository;

    @Autowired
    private EmailService emailService;

    @Value("${app.staff.late.threshold:15}")
    private int staffLateThreshold;

    private static final LocalTime DEFAULT_ENTRY_TIME = LocalTime.of(9, 0);
    private static final int DEFAULT_LATE_WINDOW_MINUTES = 10;
    private static final Logger logger = Logger.getLogger(AttendanceService.class.getName());

    // =================================================================================
    // ✅ TWO-CONDITION LATE ALERT SYSTEM
    // =================================================================================
    // CONDITION A: Morning Fingerprint exists (staff marked morning attendance)
    // CONDITION B: Staff Entry Log exists (staff scanned RFID for at least one class)
    // LOGIC: Send alert ONLY if EITHER condition is FALSE
    // =================================================================================

    /**
     * ✅ TWO-CONDITION LATE ALERT CHECK
     * Called from scheduler: Checks both conditions and sends intelligent alerts
     */
    @Transactional
    public void checkAndAlertStaffAbsence(Attendance attendance) {
        if (attendance.getStaffId() == null) {
            System.out.println("⏭️ [ALERT] Skipping: No staff ID in attendance record");
            return;
        }

        try {
            String staffId = attendance.getStaffId();
            LocalDate date = attendance.getDate();

            System.out.println("\n" + "=".repeat(80));
            System.out.println("🔍 [TWO-CONDITION CHECK] Staff: " + staffId + " | Date: " + date);
            System.out.println("=".repeat(80));

            // ✅ CONDITION A: Check if Morning Fingerprint exists
            boolean hasConditionA = hasMorningFingerprint(staffId, date);
            System.out.println("📍 CONDITION A (Morning Fingerprint): " + (hasConditionA ? "✅ TRUE" : "❌ FALSE"));

            // ✅ CONDITION B: Check if Staff Entry Log exists
            boolean hasConditionB = hasStaffEntryLog(staffId, date);
            System.out.println("📍 CONDITION B (Staff Entry Log): " + (hasConditionB ? "✅ TRUE" : "❌ FALSE"));

            // ✅ DECISION LOGIC
            System.out.println("\n📊 DECISION LOGIC:");

            if (hasConditionA && hasConditionB) {
                // ✅ Both conditions TRUE = Staff properly registered & attended
                System.out.println("   ✅ Both TRUE → NO ALERT (Staff registered and present)");
                System.out.println("   Action: SKIP EMAIL");
                logAlertCheck(staffId, date, hasConditionA, hasConditionB, "SKIPPED", "Both conditions TRUE");
                return;
            }

            // ❌ Either condition FALSE = Alert required
            String failureReason = buildFailureReason(hasConditionA, hasConditionB, staffId, date);
            System.out.println("   ❌ At least one FALSE → SEND ALERT");
            System.out.println("   Reason: " + failureReason);

            // Get staff details
            Optional<Staff> staffOpt = staffRepository.findByStaffId(staffId);
            if (staffOpt.isEmpty()) {
                System.out.println("   ⚠️ Staff not found in database");
                return;
            }

            Staff staff = staffOpt.get();
            String className = attendance.getSectionId() != null ? attendance.getSectionId() : "General";

            // ✅ Send alert to STAFF and ADMIN
            System.out.println("\n📧 [EMAIL] Sending alert...");
            System.out.println("   To: " + staff.getEmail() + " (and Admin)");
            System.out.println("   Subject: Late/Absent Alert");
            System.out.println("   Reason: " + failureReason);

            emailService.sendStaffAbsenceAlert(
                    staff.getName(),
                    staff.getStaffId(),
                    staff.getEmail(),
                    className,
                    attendance.getMarkTime(),
                    staff.getPhone(),
                    attendance.getBranch()
            );

            // ✅ Save to StaffLateAlert table using ACTUAL field names
            StaffLateAlert alert = new StaffLateAlert();
            alert.setStaffIdNumber(staffId);
            alert.setStaffName(staff.getName());
            alert.setAlertDate(date); // ✅ FIXED: LocalDate, not LocalDateTime
            alert.setScheduledTime(DEFAULT_ENTRY_TIME); // ✅ FIXED: setScheduledTime()
            alert.setActualEntryTime(attendance.getMarkTime());
            alert.setNotificationSentToStaff(true); // ✅ FIXED: setNotificationSentToStaff()
            alert.setNotificationSentToAdmin(true); // ✅ FIXED: setNotificationSentToAdmin()
            staffLateAlertRepository.save(alert);

            System.out.println("   ✅ Alert saved to database");
            System.out.println("   ✅ Email sent successfully");
            logAlertCheck(staffId, date, hasConditionA, hasConditionB, "SENT", failureReason);

        } catch (Exception e) {
            System.err.println("❌ [ERROR] Exception in checkAndAlertStaffAbsence:");
            System.err.println("   Message: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("=".repeat(80) + "\n");
    }

    /**
     * ✅ CONDITION A: Check if Morning Fingerprint exists
     * Returns TRUE if staff has at least one morning fingerprint scan
     */
    private boolean hasMorningFingerprint(String staffId, LocalDate date) {
        try {
            List<MorningFingerprintLog> logs = morningFingerprintLogRepository
                    .findByUserIdentifierAndScanDate(staffId, date);
            
            boolean exists = !logs.isEmpty();
            System.out.println("   🔎 Query: Morning fingerprint logs for " + staffId + " on " + date);
            System.out.println("   📊 Result: " + logs.size() + " log(s) found");
            
            return exists;
        } catch (Exception e) {
            System.err.println("   ⚠️ Error checking morning fingerprint: " + e.getMessage());
            return false; // Assume FALSE if error
        }
    }

    /**
     * ✅ CONDITION B: Check if Staff Entry Log exists
     * Returns TRUE if staff has at least one entry log (RFID scan for a class)
     */
    private boolean hasStaffEntryLog(String staffId, LocalDate date) {
        try {
            List<StaffEntryLog> logs = staffEntryLogRepository
                    .findByStaffIdNumberAndEntryDate(staffId, date);
            
            boolean exists = !logs.isEmpty();
            System.out.println("   🔎 Query: Staff entry logs for " + staffId + " on " + date);
            System.out.println("   📊 Result: " + logs.size() + " log(s) found");
            
            return exists;
        } catch (Exception e) {
            System.err.println("   ⚠️ Error checking staff entry log: " + e.getMessage());
            return false; // Assume FALSE if error
        }
    }

    /**
     * ✅ Build failure reason based on which conditions failed
     */
    private String buildFailureReason(boolean conditionA, boolean conditionB, String staffId, LocalDate date) {
        if (!conditionA && !conditionB) {
            return "BOTH CONDITIONS FALSE: No morning fingerprint AND no class entry for " + staffId + " on " + date;
        } else if (!conditionA) {
            return "MISSING MORNING FINGERPRINT: Staff " + staffId + " did not scan fingerprint on " + date;
        } else if (!conditionB) {
            return "NO CLASS ENTRY: Staff " + staffId + " did not scan RFID for any class on " + date;
        }
        return "UNKNOWN CONDITION FAILURE";
    }

    /**
     * ✅ Log alert check for audit trail
     */
    private void logAlertCheck(String staffId, LocalDate date, boolean condA, boolean condB, String action, String reason) {
        System.out.println("\n📝 [AUDIT LOG]");
        System.out.println("   Staff ID: " + staffId);
        System.out.println("   Date: " + date);
        System.out.println("   Condition A (Morning FP): " + condA);
        System.out.println("   Condition B (Entry Log): " + condB);
        System.out.println("   Action: " + action);
        System.out.println("   Reason: " + reason);
    }

    /**
     * ✅ Manual condition check for testing
     */
    public Map<String, Object> checkStaffAttendanceConditions(String staffId, LocalDate date) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean conditionA = hasMorningFingerprint(staffId, date);
            boolean conditionB = hasStaffEntryLog(staffId, date);

            result.put("staffId", staffId);
            result.put("date", date);
            result.put("conditionA_morningFingerprint", conditionA);
            result.put("conditionB_staffEntryLog", conditionB);
            result.put("shouldSendAlert", !conditionA || !conditionB);
            result.put("failureReason", buildFailureReason(conditionA, conditionB, staffId, date));

            return result;
        } catch (Exception e) {
            result.put("error", e.getMessage());
            return result;
        }
    }

    // =================================================================================
    // ✅ RFID SCAN FOR STAFF - GENERATES VERIFICATION CODE
    // =================================================================================

    /**
     * ✅ Handle RFID card scan for staff entry
     */
    @Transactional
    public ResponseEntity<Map<String, Object>> logRfidAttendance(
            String cardUid,
            LocalDate scanDate,
            LocalTime scanTime,
            String roomNumber) {
        Map<String, Object> response = new HashMap<>();
        try {
            System.out.println("🔄 [RFID] Processing RFID scan: " + cardUid + " at " + scanTime);

            // Step 1: Extract user ID from RFID card UID
            Long staffUserId = extractUserIdFromRfidCard(cardUid);
            if (staffUserId == null) {
                System.out.println("❌ [RFID] RFID card not registered: " + cardUid);
                response.put("status", "ERROR");
                response.put("message", "RFID card not registered");
                return ResponseEntity.status(400).body(response);
            }

            // Step 2: Get Staff from user_id
            Optional<Staff> staffOpt = staffRepository.findById(staffUserId);
            if (staffOpt.isEmpty()) {
                System.out.println("❌ [RFID] Staff not found for user_id: " + staffUserId);
                response.put("status", "ERROR");
                response.put("message", "Staff not found");
                return ResponseEntity.status(404).body(response);
            }

            Staff staff = staffOpt.get();
            String staffId = staff.getStaffId();
            System.out.println("✅ [RFID] Staff found: " + staffId + " (" + staff.getName() + ")");

            // Step 3: Find Timetable by staff ID and day of week
            String dayOfWeek = scanDate.getDayOfWeek()
                    .getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.ENGLISH);
            List<Timetable> timetableList = timetableRepository.findByStaffIdAndDayOfWeek(staffId, dayOfWeek);

            Optional<Timetable> timetableOpt = timetableList.stream()
                    .filter(t -> t.getScheduledStartTime() != null && t.getScheduledEndTime() != null)
                    .filter(t -> !scanTime.isBefore(t.getScheduledStartTime())
                            && scanTime.isBefore(t.getScheduledEndTime()))
                    .findFirst();

            if (timetableOpt.isEmpty()) {
                System.out.println("⚠️ [RFID] No timetable found for staff at this time");
                System.out.println("   Staff: " + staffId + " | Time: " + scanTime);
                createStaffEntryLogWithoutTimetable(staffId, scanDate, scanTime, roomNumber);
                response.put("status", "LOG_SUCCESS");
                response.put("message", "Log Saved");
                return ResponseEntity.ok(response);
            }

            Timetable timetable = timetableOpt.get();
            System.out.println("✅ [RFID] Timetable found: " + timetable.getSubject() +
                    " (ID: " + timetable.getId() + ")");

            // Step 4: Create Staff Entry Log
            StaffEntryLog entryLog = new StaffEntryLog();
            entryLog.setStaffIdNumber(staffId);
            entryLog.setEntryDate(scanDate);
            entryLog.setEntryTime(scanTime);
            entryLog.setRoomNumber(roomNumber);
            entryLog.setStatus(StaffEntryLog.Status.ON_TIME);
            entryLog.setSectionId(timetable.getSectionId());
            entryLog.setTimetableId(timetable.getId());
            entryLog.setTimeSlot(timetable.getTimeSlot()); // ✅ FIXED: Set time slot for scheduler
            entryLog.setDayOfWeek(dayOfWeek); // ✅ FIXED: Set day of week
            StaffEntryLog savedLog = staffEntryLogRepository.save(entryLog);
            System.out.println("✅ [RFID] Entry log created");

            // Step 5: Generate Verification Code
            LocalTime classStartTime = timetable.getScheduledStartTime();
            String code = generateVerificationCodeForPeriod(
                    staffId,
                    timetable.getSubject(),
                    timetable.getSectionId(),
                    classStartTime,
                    timetable.getId(),
                    scanTime
            );

            if (code != null) {
                System.out.println("✅ [EMAIL] Sending code to: " + staff.getEmail());
                try {
                    emailService.sendStaffAttendanceCodeEmail(
                            staff.getName(),
                            staff.getStaffId(),
                            staff.getEmail(),
                            timetable.getSubject(),
                            timetable.getSectionId(),
                            scanTime,
                            code,
                            staff.getPhone(),
                            staff.getBranch()
                    );
                    System.out.println("✅ [EMAIL] Verification code sent to staff: " + staff.getEmail());
                } catch (Exception emailEx) {
                    System.err.println("⚠️ [EMAIL] Failed to send email: " + emailEx.getMessage());
                }
            }

            response.put("status", "LOG_SUCCESS");
            response.put("message", "Log Saved");
            response.put("staffId", staffId);
            response.put("timetableId", timetable.getId());
            response.put("codeGenerated", code != null);
            response.put("code", code);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ [RFID] Error processing RFID scan: " + e.getMessage());
            e.printStackTrace();
            response.put("status", "ERROR");
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * ✅ Helper: Extract user_id from RFID card UID
     */
    private Long extractUserIdFromRfidCard(String cardUid) {
        try {
            System.out.println("🔍 [RFID] Looking up card UID: " + cardUid);
            Optional<RfidCard> cardOpt = rfidCardRepository.findByCardUidAndIsActiveTrue(cardUid);
            if (cardOpt.isEmpty()) {
                System.out.println("❌ [RFID] Card not found in database: " + cardUid);
                return null;
            }

            RfidCard card = cardOpt.get();
            Long userId = card.getUserId();
            System.out.println("✅ [RFID] Card found: " + cardUid + " → User ID = " + userId);
            return userId;
        } catch (Exception e) {
            System.err.println("❌ Error extracting user ID from RFID: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * ✅ Helper: Create staff entry log without timetable
     */
    @Transactional
    private void createStaffEntryLogWithoutTimetable(
            String staffId,
            LocalDate scanDate,
            LocalTime scanTime,
            String roomNumber) {
        StaffEntryLog entryLog = new StaffEntryLog();
        entryLog.setStaffIdNumber(staffId);
        entryLog.setEntryDate(scanDate);
        entryLog.setEntryTime(scanTime);
        entryLog.setRoomNumber(roomNumber);
        entryLog.setStatus(StaffEntryLog.Status.ON_TIME);
        entryLog.setSectionId(null);
        staffEntryLogRepository.save(entryLog);
        System.out.println("✅ [RFID] Entry log created (without timetable)");
    }

    /**
     * ✅ Generate verification code when staff scans RFID
     */
    @Transactional
    public String generateVerificationCodeForPeriod(String staffId, String className, String sectionId,
                                                     LocalTime classStartTime, Long timetableId, LocalTime currentScanTime) {
        try {
            LocalTime now = currentScanTime;
            LocalTime t10 = classStartTime.plusMinutes(10);
            LocalTime t30 = classStartTime.plusMinutes(30);

            System.out.println("🔐 [CODE_GEN] Checking code generation eligibility:");
            System.out.println("   Class Start: " + classStartTime + " | Scan Time: " + now);
            System.out.println("   T10 (Late Alert): " + t10 + " | T30 (Code Cutoff): " + t30);

            if (now.isAfter(t30)) {
                System.out.println("❌ [CODE_GEN] VERY LATE (After T30) - Code generation NOT allowed");
                return null;
            }

            System.out.println("✅ [CODE_GEN] Eligible for code generation");

            Optional<VerificationCode> existingCodeOpt = verificationCodeRepository
                    .findCurrentValidCode(staffId, sectionId);

            if (existingCodeOpt.isPresent()) {
                VerificationCode existingCode = existingCodeOpt.get();
                System.out.println("🔄 [CODE_GEN] Code already exists: " + existingCode.getCode());
                return existingCode.getCode();
            }

            String newCode = CodeGenerationUtil.generateUniqueCode();
            LocalDateTime validUntil = LocalDateTime.of(LocalDate.now(), classStartTime).plusMinutes(30);
            boolean isLate = now.isAfter(t10);

            VerificationCode verificationCode = new VerificationCode();
            verificationCode.setStaffId(staffId);
            verificationCode.setClassName(className);
            verificationCode.setSectionId(sectionId);
            verificationCode.setCode(newCode);
            verificationCode.setGeneratedTime(LocalDateTime.now());
            verificationCode.setValidUntil(validUntil);
            verificationCode.setIsUsed(false);
            verificationCode.setCodeEnteredCount(0);
            verificationCode.setTimetableId(timetableId);

            VerificationCode savedCode = verificationCodeRepository.save(verificationCode);
            System.out.println("✅ [CODE_GEN] Code generated: " + newCode);
            System.out.println("   Staff Status: " + (isLate ? "LATE" : "ON_TIME"));
            return newCode;

        } catch (Exception e) {
            System.err.println("❌ [CODE_GEN] Error generating code: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * ✅ Verify code entered by student and mark attendance
     */
    @Transactional
    public Map<String, Object> verifyStudentCodeEntry(String studentRegNo, String code,
                                                       String sectionId, LocalDate date,
                                                       String timeSlot) {
        Map<String, Object> response = new HashMap<>();
        try {
            System.out.println("🔍 [CODE_VERIFY] Verifying code: " + code + " for student: " + studentRegNo);

            Optional<VerificationCode> codeOpt = verificationCodeRepository.findByCode(code);
            if (codeOpt.isEmpty()) {
                System.out.println("❌ [CODE_VERIFY] Code not found: " + code);
                response.put("success", false);
                response.put("message", "Invalid verification code");
                return response;
            }

            VerificationCode verificationCode = codeOpt.get();
            if (verificationCode.isExpired()) {
                System.out.println("❌ [CODE_VERIFY] Code expired: " + code);
                response.put("success", false);
                response.put("message", "Verification code has expired");
                return response;
            }

            List<PeriodAttendanceLog> existingLogs = periodAttendanceLogRepository
                    .findByStudentRegistrationNumberAndScanDate(studentRegNo, date);

            for (PeriodAttendanceLog log : existingLogs) {
                if (code.equals(log.getVerificationCode())) {
                    System.out.println("⚠️ [CODE_VERIFY] Student already entered this code");
                    response.put("success", false);
                    response.put("message", "You have already marked attendance for this period");
                    return response;
                }
            }

            PeriodAttendanceLog log = new PeriodAttendanceLog();
            log.setStudentRegistrationNumber(studentRegNo);
            log.setSectionId(sectionId);
            log.setScanDate(date);
            log.setScanTime(LocalTime.now());
            log.setTimeSlot(timeSlot);
            log.setVerificationCode(code);
            log.setCodeVerificationTimestamp(LocalDateTime.now());
            log.setVerifiedVia("CODE");
            log.setRoomNumber("CODE_ENTRY");

            PeriodAttendanceLog savedLog = periodAttendanceLogRepository.save(log);
            verificationCode.setCodeEnteredCount(verificationCode.getCodeEnteredCount() + 1);
            verificationCodeRepository.save(verificationCode);

            System.out.println("✅ [CODE_VERIFY] Attendance marked for student: " + studentRegNo);
            response.put("success", true);
            response.put("message", "Attendance marked successfully via code entry");
            response.put("log_id", savedLog.getId());
            return response;

        } catch (Exception e) {
            System.err.println("❌ [CODE_VERIFY] Error: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Error verifying code: " + e.getMessage());
            return response;
        }
    }

    /**
     * ✅ Check if code is valid
     */
    public boolean isCodeValid(String code) {
        try {
            return verificationCodeRepository.isCodeValidAndNotExpired(code);
        } catch (Exception e) {
            System.err.println("❌ Error checking code validity: " + e.getMessage());
            return false;
        }
    }

    /**
     * ✅ Get active code for section
     */
    public Optional<VerificationCode> getActiveCodeForSection(String sectionId) {
        try {
            List<VerificationCode> activeCodes = verificationCodeRepository.findActiveCodesBySection(sectionId);
            if (!activeCodes.isEmpty()) {
                return Optional.of(activeCodes.get(0));
            }
            return Optional.empty();
        } catch (Exception e) {
            System.err.println("❌ Error fetching active code: " + e.getMessage());
            return Optional.empty();
        }
    }

    // =================================================================================
    // ✅ CALCULATE STAFF DAILY ATTENDANCE (Fingerprint Duration-Based)
    // =================================================================================

    @Transactional
    public String calculateDailyStaffAttendance(String staffId, LocalDate date) {
        System.out.println("🔄 CALCULATING FINAL STAFF ATTENDANCE FOR: " + staffId + " on " + date);

        Optional<Staff> staffOpt = staffRepository.findByStaffId(staffId);
        if (staffOpt.isEmpty()) return "Staff Not Found";

        Staff staff = staffOpt.get();

        List<MorningFingerprintLog> morningLogs = morningFingerprintLogRepository
                .findByUserIdentifierAndScanDate(staffId, date);

        if (morningLogs.isEmpty()) {
            System.out.println("❌ NO MORNING FINGERPRINT FOUND");
            saveMainStaffAttendance(staff, date, AttendanceStatus.ABSENT);
            return "ABSENT";
        }

        MorningFingerprintLog morningFP = morningLogs.get(0);
        LocalTime loginTime = morningFP.getScanTime();
        System.out.println("✅ MORNING LOGIN: " + loginTime);

        LocalTime logoutTime = LocalTime.now();
        long durationMinutes = java.time.temporal.ChronoUnit.MINUTES.between(loginTime, logoutTime);
        double durationHours = durationMinutes / 60.0;

        System.out.println("📊 DURATION: " + durationHours + " hours (" + durationMinutes + " minutes)");

        AttendanceStatus status;
        if (durationHours < 4.0) {
            status = AttendanceStatus.ABSENT;
            System.out.println("⚠️ DURATION < 4 HOURS → ABSENT");
        } else if (durationHours >= 4.0 && durationHours <= 4.5) {
            status = AttendanceStatus.LATE;
            System.out.println("⚠️ DURATION 4-4.5 HOURS → HALF_ATTENDANCE (LATE)");
        } else {
            status = AttendanceStatus.PRESENT;
            System.out.println("✅ DURATION > 4.5 HOURS → PRESENT");
        }

        saveMainStaffAttendance(staff, date, status);
        staffEntryLogRepository.deleteByStaffIdNumberAndEntryDate(staffId, date);
        System.out.println("🧹 CLEARED Staff Entry Logs (Room Scans) for " + staffId);

        return status.toString();
    }

    @Transactional
    public void saveMainStaffAttendance(Staff staff, LocalDate date, AttendanceStatus status) {
        Optional<Attendance> existing = attendanceRepository.findByStaffIdAndDate(staff.getStaffId(), date);

        Attendance attendance = existing.orElse(new Attendance());
        attendance.setStaffId(staff.getStaffId());
        attendance.setDate(date);
        attendance.setMarkTime(LocalTime.now());
        attendance.setStatus(status);
        attendance.setBranch(staff.getBranch());
        attendance.setUserIdentifier(staff.getStaffId());
        attendance.setUserType(Attendance.UserType.STAFF);
        attendance.setRemarks("Auto-Calculated (Fingerprint Duration): " + status);

        attendanceRepository.save(attendance);
        System.out.println("✅ FINAL STAFF ATTENDANCE SAVED: " + status);

        if (status == AttendanceStatus.ABSENT || status == AttendanceStatus.LATE) {
            checkAndAlertStaffAbsence(attendance);
        }
    }

    // =================================================================================
    // ✅ CALCULATE STUDENT DAILY ATTENDANCE
    // =================================================================================

    @Transactional
    public String calculateDailyStudentAttendance(String regNo, LocalDate date) {
        System.out.println("🔄 CALCULATING FINAL ATTENDANCE FOR: " + regNo + " on " + date);

        Optional<Student> studentOpt = studentRepository.findByRegistrationNumber(regNo);
        if (studentOpt.isEmpty()) return "Student Not Found";

        Student student = studentOpt.get();

        List<MorningFingerprintLog> morningLogs = morningFingerprintLogRepository
                .findByUserIdentifierAndScanDate(regNo, date);

        boolean hasMorningFP = !morningLogs.isEmpty();

        List<PeriodAttendanceLog> periodLogs = periodAttendanceLogRepository
                .findByStudentRegistrationNumberAndScanDate(regNo, date);

        int periodCount = periodLogs.size();

        System.out.println("📊 STATS -> Morning FP: " + hasMorningFP + " | Period Count: " + periodCount);

        AttendanceStatus status;
        if (hasMorningFP && periodCount >= 7) {
            status = AttendanceStatus.PRESENT;
        } else if (hasMorningFP && periodCount >= 4) {
            status = AttendanceStatus.LATE;
        } else {
            status = AttendanceStatus.ABSENT;
        }

        saveMainAttendance(student, date, status);
        periodAttendanceLogRepository.deleteByStudentRegistrationNumberAndScanDate(regNo, date);
        System.out.println("🧹 CLEARED Period Logs for " + regNo);

        return status.toString();
    }

    @Transactional
    public void saveMainAttendance(Student student, LocalDate date, AttendanceStatus status) {
        Optional<Attendance> existing = attendanceRepository.findByStudentIdAndDate(student.getId(), date);

        Attendance attendance = existing.orElse(new Attendance());
        attendance.setStudentId(student.getId());
        attendance.setDate(date);
        attendance.setMarkTime(LocalTime.now());
        attendance.setStatus(status);
        attendance.setSectionId(student.getSectionId());
        attendance.setBranch(student.getBranch());
        attendance.setUserIdentifier(student.getRegistrationNumber());
        attendance.setUserType(Attendance.UserType.STUDENT);
        attendance.setRemarks("Auto-Calculated: " + status);

        attendanceRepository.save(attendance);
        updateAttendanceSummary(student);
        System.out.println("✅ FINAL ATTENDANCE SAVED: " + status);
    }

    // =================================================================================
    // EXISTING METHODS BELOW (UNCHANGED - FULLY PRESERVED)
    // =================================================================================

    @Transactional
    public Attendance markStudentAttendanceBiometric(Long studentId, LocalTime scanTime) {
        LocalDate today = LocalDate.now();
        Optional<Attendance> existingOpt = attendanceRepository.findByStudentIdAndDate(studentId, today);

        if (existingOpt.isPresent()) {
            throw new RuntimeException("Attendance already marked for today");
        }

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        String sectionId = student.getSectionId();
        if (sectionId == null || sectionId.isEmpty()) {
            throw new RuntimeException("Student has no section assigned");
        }

        LocalTime entryTime = DEFAULT_ENTRY_TIME;
        AttendanceStatus status = calculateAttendanceStatus(scanTime, entryTime, DEFAULT_LATE_WINDOW_MINUTES);

        Attendance attendance = new Attendance();
        attendance.setDate(today);
        attendance.setMarkTime(scanTime);
        attendance.setStatus(status);
        attendance.setStudentId(studentId);
        attendance.setSectionId(sectionId);
        attendance.setBranch(student.getBranch());
        attendance.setRemarks("Biometric scan");

        return attendanceRepository.save(attendance);
    }

    @Transactional
    public Attendance markStaffAttendanceBiometric(String staffId, LocalTime scanTime, String branch) {
        LocalDate today = LocalDate.now();
        Optional<Attendance> existingOpt = attendanceRepository.findByStaffIdAndDate(staffId, today);

        if (existingOpt.isPresent()) {
            throw new RuntimeException("Attendance already marked for today");
        }

        Staff staff = staffRepository.findByStaffId(staffId)
                .orElseThrow(() -> new RuntimeException("Staff not found"));

        LocalTime entryTime = DEFAULT_ENTRY_TIME;
        AttendanceStatus status = calculateAttendanceStatus(scanTime, entryTime, DEFAULT_LATE_WINDOW_MINUTES);

        Attendance attendance = new Attendance();
        attendance.setDate(today);
        attendance.setMarkTime(scanTime);
        attendance.setStatus(status);
        attendance.setStaffId(staff.getStaffId());
        attendance.setBranch(branch);
        attendance.setRemarks("Biometric scan");

        Attendance saved = attendanceRepository.save(attendance);
        checkAndAlertStaffAbsence(saved);
        return saved;
    }

    private AttendanceStatus calculateAttendanceStatus(LocalTime scanTime, LocalTime entryTime, int lateWindowMinutes) {
        if (scanTime.isBefore(entryTime)) {
            return AttendanceStatus.PRESENT;
        } else if (scanTime.isBefore(entryTime.plusMinutes(lateWindowMinutes))) {
            return AttendanceStatus.LATE;
        } else {
            return AttendanceStatus.ABSENT;
        }
    }

    @Transactional
    public Attendance markAttendance(AttendanceMarkRequest request) {
        if (request.getStudentId() != null) {
            Optional<Attendance> existingOpt = attendanceRepository.findByStudentIdAndDate(
                    request.getStudentId(), request.getDate());

            if (existingOpt.isPresent()) {
                Attendance existing = existingOpt.get();
                existing.setStatus(request.getStatus());
                existing.setRemarks(request.getRemarks());
                existing.setMarkTime(request.getMarkTime());
                return attendanceRepository.save(existing);
            }
        }

        if (request.getStaffId() != null) {
            Optional<Attendance> existingOpt = attendanceRepository.findByStaffIdAndDate(
                    request.getStaffId(), request.getDate());

            if (existingOpt.isPresent()) {
                Attendance existing = existingOpt.get();
                existing.setStatus(request.getStatus());
                existing.setRemarks(request.getRemarks());
                existing.setMarkTime(request.getMarkTime());
                Attendance saved = attendanceRepository.save(existing);

                if (saved.getStaffId() != null) {
                    checkAndAlertStaffAbsence(saved);
                }

                return saved;
            }
        }

        AttendanceStatus status = request.getStatus();
        if (status == null && request.getMarkTime() != null && request.getEntryTime() != null) {
            int lateWindow = request.getLateWindowMinutes() != null ? request.getLateWindowMinutes() : DEFAULT_LATE_WINDOW_MINUTES;
            status = calculateAttendanceStatus(request.getMarkTime(), request.getEntryTime(), lateWindow);
        }

        Attendance attendance = new Attendance();
        attendance.setDate(request.getDate());
        attendance.setMarkTime(request.getMarkTime());
        attendance.setStatus(status != null ? status : AttendanceStatus.ABSENT);
        attendance.setStudentId(request.getStudentId());
        attendance.setStaffId(request.getStaffId());
        attendance.setSectionId(request.getSectionId());
        attendance.setBranch(request.getBranch());
        attendance.setRemarks(request.getRemarks());

        Attendance saved = attendanceRepository.save(attendance);

        if (saved.getStaffId() != null) {
            checkAndAlertStaffAbsence(saved);
        }

        return saved;
    }

    @Transactional
    public List<Attendance> markBulkAttendance(AttendanceBatchRequest batchRequest) {
        List<Attendance> attendances = new ArrayList<>();
        LocalTime entryTime = batchRequest.getEntryTime() != null ? batchRequest.getEntryTime() : DEFAULT_ENTRY_TIME;
        int lateWindow = batchRequest.getLateWindowMinutes() != null ? batchRequest.getLateWindowMinutes() : DEFAULT_LATE_WINDOW_MINUTES;

        for (AttendanceMarkRequest request : batchRequest.getAttendances()) {
            request.setDate(batchRequest.getDate());
            request.setSectionId(batchRequest.getSectionId());
            request.setBranch(batchRequest.getBranch());
            request.setEntryTime(entryTime);
            request.setLateWindowMinutes(lateWindow);
            attendances.add(markAttendance(request));
        }

        return attendances;
    }

    public Map<String, Object> getStudentAttendanceByMonth(String sectionId, int year, int month) {
        List<Attendance> attendances = attendanceRepository.findBySectionAndMonth(sectionId, year, month);

        Optional<Section> sectionOpt = sectionRepository.findBySectionCode(sectionId);
        if (sectionOpt.isEmpty()) {
            throw new RuntimeException("Section not found: " + sectionId);
        }

        Section section = sectionOpt.get();
        List<Student> students = studentRepository.findBySectionId(sectionId);
        YearMonth yearMonth = YearMonth.of(year, month);
        int daysInMonth = yearMonth.lengthOfMonth();

        List<Map<String, Object>> attendanceGrid = new ArrayList<>();

        for (Student student : students) {
            Map<String, Object> studentRow = new HashMap<>();
            studentRow.put("studentId", student.getId());
            studentRow.put("name", student.getName());
            studentRow.put("registrationNumber", student.getRegistrationNumber());
            studentRow.put("section", student.getSection());

            Map<Integer, AttendanceStatus> dailyAttendance = new HashMap<>();
            int presentCount = 0, lateCount = 0, absentCount = 0;

            for (Attendance att : attendances) {
                if (att.getStudentId() != null && att.getStudentId().equals(student.getId())) {
                    int day = att.getDate().getDayOfMonth();
                    dailyAttendance.put(day, att.getStatus());

                    switch (att.getStatus()) {
                        case PRESENT: presentCount++; break;
                        case LATE: lateCount++; break;
                        case ABSENT: absentCount++; break;
                    }
                }
            }

            studentRow.put("attendance", dailyAttendance);
            studentRow.put("presentCount", presentCount);
            studentRow.put("lateCount", lateCount);
            studentRow.put("absentCount", absentCount);
            attendanceGrid.add(studentRow);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("section", section);
        result.put("sectionId", sectionId);
        result.put("sectionName", section.getDisplayName());
        result.put("year", year);
        result.put("month", month);
        result.put("daysInMonth", daysInMonth);
        result.put("attendanceGrid", attendanceGrid);

        return result;
    }

    public Map<String, Object> getStaffAttendanceByMonth(String branch, int year, int month) {
        List<Attendance> attendances = attendanceRepository.findByBranchAndMonth(branch, year, month);
        List<Staff> staffList = staffRepository.findByBranch(branch);
        YearMonth yearMonth = YearMonth.of(year, month);
        int daysInMonth = yearMonth.lengthOfMonth();

        List<Map<String, Object>> attendanceGrid = new ArrayList<>();

        for (Staff staff : staffList) {
            Map<String, Object> staffRow = new HashMap<>();
            staffRow.put("staffId", staff.getStaffId());
            staffRow.put("name", staff.getName());
            staffRow.put("email", staff.getEmail());

            Map<Integer, AttendanceStatus> dailyAttendance = new HashMap<>();
            int presentCount = 0, lateCount = 0, absentCount = 0;

            for (Attendance att : attendances) {
                if (att.getStaffId() != null && att.getStaffId().equals(staff.getStaffId())) {
                    int day = att.getDate().getDayOfMonth();
                    dailyAttendance.put(day, att.getStatus());

                    switch (att.getStatus()) {
                        case PRESENT: presentCount++; break;
                        case LATE: lateCount++; break;
                        case ABSENT: absentCount++; break;
                    }
                }
            }

            staffRow.put("attendance", dailyAttendance);
            staffRow.put("presentCount", presentCount);
            staffRow.put("lateCount", lateCount);
            staffRow.put("absentCount", absentCount);
            attendanceGrid.add(staffRow);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("branch", branch);
        result.put("year", year);
        result.put("month", month);
        result.put("daysInMonth", daysInMonth);
        result.put("attendanceGrid", attendanceGrid);

        return result;
    }

    public ByteArrayOutputStream exportStudentAttendanceToExcel(String sectionId, int year, int month) throws Exception {
        Map<String, Object> data = getStudentAttendanceByMonth(sectionId, year, month);
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Student Attendance");

        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle presentStyle = createStatusStyle(workbook, IndexedColors.GREEN);
        CellStyle lateStyle = createStatusStyle(workbook, IndexedColors.YELLOW);
        CellStyle absentStyle = createStatusStyle(workbook, IndexedColors.RED);

        int daysInMonth = (int) data.get("daysInMonth");
        List<Map<String, Object>> grid = (List<Map<String, Object>>) data.get("attendanceGrid");

        Row headerRow = sheet.createRow(0);
        createStyledCell(headerRow, 0, "Reg Number", headerStyle);
        createStyledCell(headerRow, 1, "Student Name", headerStyle);
        createStyledCell(headerRow, 2, "Section", headerStyle);

        for (int day = 1; day <= daysInMonth; day++) {
            createStyledCell(headerRow, day + 2, String.valueOf(day), headerStyle);
        }

        createStyledCell(headerRow, daysInMonth + 3, "P", headerStyle);
        createStyledCell(headerRow, daysInMonth + 4, "L", headerStyle);
        createStyledCell(headerRow, daysInMonth + 5, "A", headerStyle);

        int rowNum = 1;
        for (Map<String, Object> studentRow : grid) {
            Row row = sheet.createRow(rowNum);
            row.createCell(0).setCellValue((String) studentRow.get("registrationNumber"));
            row.createCell(1).setCellValue((String) studentRow.get("name"));
            row.createCell(2).setCellValue((String) studentRow.get("section"));

            Map<Integer, AttendanceStatus> attendance = (Map<Integer, AttendanceStatus>) studentRow.get("attendance");

            for (int day = 1; day <= daysInMonth; day++) {
                AttendanceStatus status = attendance.get(day);
                Cell cell = row.createCell(day + 2);

                if (status == null) {
                    cell.setCellValue("-");
                } else {
                    cell.setCellValue(status.getShortCode());
                    switch (status) {
                        case PRESENT: cell.setCellStyle(presentStyle); break;
                        case LATE: cell.setCellStyle(lateStyle); break;
                        case ABSENT: cell.setCellStyle(absentStyle); break;
                    }
                }
            }

            row.createCell(daysInMonth + 3).setCellValue((Integer) studentRow.get("presentCount"));
            row.createCell(daysInMonth + 4).setCellValue((Integer) studentRow.get("lateCount"));
            row.createCell(daysInMonth + 5).setCellValue((Integer) studentRow.get("absentCount"));
            rowNum++;
        }

        for (int i = 0; i < daysInMonth + 5; i++) {
            sheet.autoSizeColumn(i);
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        return outputStream;
    }

    public ByteArrayOutputStream exportStaffAttendanceToExcel(String branch, int year, int month) throws Exception {
        Map<String, Object> data = getStaffAttendanceByMonth(branch, year, month);
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Staff Attendance");

        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle presentStyle = createStatusStyle(workbook, IndexedColors.GREEN);
        CellStyle lateStyle = createStatusStyle(workbook, IndexedColors.YELLOW);
        CellStyle absentStyle = createStatusStyle(workbook, IndexedColors.RED);

        int daysInMonth = (int) data.get("daysInMonth");
        List<Map<String, Object>> grid = (List<Map<String, Object>>) data.get("attendanceGrid");

        Row headerRow = sheet.createRow(0);
        createStyledCell(headerRow, 0, "Staff Name", headerStyle);
        createStyledCell(headerRow, 1, "Email", headerStyle);

        for (int day = 1; day <= daysInMonth; day++) {
            createStyledCell(headerRow, day + 1, String.valueOf(day), headerStyle);
        }

        createStyledCell(headerRow, daysInMonth + 2, "P", headerStyle);
        createStyledCell(headerRow, daysInMonth + 3, "L", headerStyle);
        createStyledCell(headerRow, daysInMonth + 4, "A", headerStyle);

        int rowNum = 1;
        for (Map<String, Object> staffRow : grid) {
            Row row = sheet.createRow(rowNum);
            row.createCell(0).setCellValue((String) staffRow.get("name"));
            row.createCell(1).setCellValue((String) staffRow.get("email"));

            Map<Integer, AttendanceStatus> attendance = (Map<Integer, AttendanceStatus>) staffRow.get("attendance");

            for (int day = 1; day <= daysInMonth; day++) {
                AttendanceStatus status = attendance.get(day);
                Cell cell = row.createCell(day + 1);

                if (status == null) {
                    cell.setCellValue("-");
                } else {
                    cell.setCellValue(status.getShortCode());
                    switch (status) {
                        case PRESENT: cell.setCellStyle(presentStyle); break;
                        case LATE: cell.setCellStyle(lateStyle); break;
                        case ABSENT: cell.setCellStyle(absentStyle); break;
                    }
                }
            }

            row.createCell(daysInMonth + 2).setCellValue((Integer) staffRow.get("presentCount"));
            row.createCell(daysInMonth + 3).setCellValue((Integer) staffRow.get("lateCount"));
            row.createCell(daysInMonth + 4).setCellValue((Integer) staffRow.get("absentCount"));
            rowNum++;
        }

        for (int i = 0; i < daysInMonth + 4; i++) {
            sheet.autoSizeColumn(i);
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        return outputStream;
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private CellStyle createStatusStyle(Workbook workbook, IndexedColors color) {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(color.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private void createStyledCell(Row row, int column, String value, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private void updateAttendanceSummary(Student student) {
        try {
            String currentAcademicYear = "2024-2025";
            Integer currentSemester = student.getSemester();
            Long regulationId = student.getRegulationId() != null ? student.getRegulationId() : 1L;

            Optional<SemesterConfig> semesterConfigOpt = semesterConfigRepository
                    .findByAcademicYearAndRegulationIdAndYearAndSemester(
                            currentAcademicYear, regulationId, student.getYear(), currentSemester);

            if (semesterConfigOpt.isPresent()) {
                SemesterConfig config = semesterConfigOpt.get();
                LocalDate endDate = LocalDate.now().isAfter(config.getEndDate()) ? config.getEndDate() : LocalDate.now();

                List<Attendance> records = attendanceRepository.findByStudentIdAndDateBetween(
                        student.getId(), config.getStartDate(), endDate);

                int total = records.size();
                int attended = (int) records.stream().filter(a -> a.getStatus() == AttendanceStatus.PRESENT).count();

                AttendanceSummary summary = attendanceSummaryRepository
                        .findByStudentIdAndSemesterAndAcademicYear(student.getId(), currentSemester, currentAcademicYear)
                        .orElse(new AttendanceSummary(student.getId(), currentSemester, currentAcademicYear));

                summary.setTotalClasses(total);
                summary.setAttendedClasses(attended);
                summary.calculateAttendance();

                attendanceSummaryRepository.save(summary);

                student.setAttendanceStatus(summary.getStatus());
                studentRepository.save(student);
            }

        } catch (Exception e) {
            System.err.println("⚠️ Failed to update summary: " + e.getMessage());
        }
    }
}
