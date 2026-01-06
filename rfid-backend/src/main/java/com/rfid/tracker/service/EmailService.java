package com.rfid.tracker.service;


import com.rfid.tracker.entity.Attendance;
import com.rfid.tracker.entity.Staff;
import com.rfid.tracker.entity.StaffLateAlert;
import com.rfid.tracker.entity.Timetable;
import com.rfid.tracker.repository.AttendanceRepository;
import com.rfid.tracker.repository.StaffLateAlertRepository;
import com.rfid.tracker.repository.StaffRepository;
import com.rfid.tracker.repository.TimetableRepository;
import com.rfid.tracker.repository.StaffEntryLogRepository;
import com.rfid.tracker.repository.MorningFingerprintLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;


@Service
public class EmailService {


    private static final Logger logger = Logger.getLogger(EmailService.class.getName());


    @Autowired
    private JavaMailSender mailSender;


    @Autowired
    private TimetableRepository timetableRepository;


    @Autowired
    private AttendanceRepository attendanceRepository;


    @Autowired
    private StaffRepository staffRepository;


    @Autowired
    private StaffLateAlertRepository alertRepository;


    @Autowired
    private StaffEntryLogRepository staffEntryLogRepository;


    @Autowired
    private MorningFingerprintLogRepository morningFingerprintLogRepository;


    @Value("${app.admin.email}")
    private String adminEmail;


    @Value("${app.staff.late.threshold:15}")
    private int lateThreshold;


    // ==================================================================================
    // ✅ NEW TWO-CONDITION SYSTEM: Morning FP + Staff Entry Log Required
    // ==================================================================================
    /**
     * ✅ COMPREHENSIVE LATE ALERT SCHEDULER WITH TWO-CONDITION CHECK
     * 
     * This is the main scheduler that runs every 15 minutes and performs:
     * 1. Finds all classes scheduled for today
     * 2. Checks if current time is in "late window" (start + threshold < now < end)
     * 3. FOR EACH CLASS IN LATE WINDOW:
     *    - CONDITION A: Check if Morning Fingerprint exists (staff entered in morning)
     *    - CONDITION B: Check if Staff Entry Log exists (staff scanned RFID for class)
     *    - IF BOTH CONDITIONS TRUE: Staff is properly registered → SKIP ALERT
     *    - IF ANY CONDITION FALSE: Staff is missing/not scanned → SEND ALERT
     * 4. Alert sent to: Staff + Admin
     * 5. Prevent duplicates using StaffLateAlert table
     */
    @Scheduled(fixedRate = 900000) // Every 15 minutes
    @Transactional
    public void checkAndSendStaffAbsenceAlerts() {
        try {
            System.out.println("🕒 [SCHEDULER] Checking for staff absences/lateness at " + LocalTime.now());
            
            String dayOfWeek = LocalDate.now().getDayOfWeek()
                .getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.ENGLISH);
            
            LocalTime now = LocalTime.now();
            LocalDate today = LocalDate.now();


            // ✅ FIX: Add proper type casting with <Timetable>
            List<Timetable> todaysClasses = timetableRepository.findByDayOfWeek(dayOfWeek);
            System.out.println("📚 Found " + todaysClasses.size() + " classes for " + dayOfWeek);


            for (Timetable classEntry : todaysClasses) {
                LocalTime classStart = classEntry.getScheduledStartTime();
                LocalTime classEnd = classEntry.getScheduledEndTime();


                if (classStart == null || classEnd == null) {
                    System.out.println("⏭️ Skipping class (null times): " + classEntry.getSubject());
                    continue;
                }


                // Check if current time is AFTER threshold (start + lateThreshold) and BEFORE class end
                LocalTime thresholdTime = classStart.plusMinutes(lateThreshold);
                boolean isInLateWindow = now.isAfter(thresholdTime) && now.isBefore(classEnd);


                if (isInLateWindow) {
                    System.out.println("⏰ Class " + classEntry.getSubject() + " is in LATE WINDOW");
                    System.out.println(" Start: " + classStart + " | Threshold: " + thresholdTime + " | Now: " + now);


                    String staffId = classEntry.getStaffId();
                    String timeSlot = classEntry.getTimeSlot();


                    // ✅ NEW TWO-CONDITION CHECK
                    System.out.println("\n🔍 [TWO-CONDITION CHECK] Evaluating staff: " + staffId);
                    
                    // CONDITION A: Check if Morning Fingerprint exists
                    boolean hasMorningFingerprint = !morningFingerprintLogRepository
                        .findByUserIdentifierAndScanDate(staffId, today).isEmpty();
                    System.out.println("  ✓ Condition A (Morning FP): " + (hasMorningFingerprint ? "✅ TRUE" : "❌ FALSE"));


                    // CONDITION B: Check if Staff Entry Log exists for this class/room/timeSlot
                    boolean hasStaffEntry = !staffEntryLogRepository
                        .findByStaffIdNumberAndEntryDateAndTimeSlot(staffId, today, timeSlot).isEmpty();
                    System.out.println("  ✓ Condition B (Staff Entry): " + (hasStaffEntry ? "✅ TRUE" : "❌ FALSE"));


                    // LOGIC:
                    // IF (A=TRUE AND B=TRUE) → Staff properly registered → NO ALERT
                    // IF (A=FALSE OR B=FALSE) → Staff missing/not scanned → SEND ALERT
                    
                    if (hasMorningFingerprint && hasStaffEntry) {
                        System.out.println("✅ [SKIP ALERT] Both conditions TRUE - Staff is properly registered");
                        System.out.println("   (Code already sent, attendance is being tracked)");
                        continue; // SKIP ALERT - Staff is properly registered
                    }


                    // IF we reach here, at least one condition is FALSE
                    System.out.println("⚠️ [SEND ALERT] At least one condition FALSE - Staff is missing/not scanned");


                    // Check if alert already sent for this slot today (prevent duplicates)
                    boolean alreadyAlerted = alertRepository
                        .existsByStaffIdNumberAndAlertDateAndTimeSlot(staffId, today, timeSlot);


                    if (alreadyAlerted) {
                        System.out.println("🔄 Alert already sent for " + staffId + " at " + timeSlot + ". Skipping...");
                        continue;
                    }


                    // Get staff details
                    // ✅ FIX: Add proper type casting with <Staff>
                    Optional<Staff> staffOpt = staffRepository.findByStaffId(staffId);
                    if (staffOpt.isPresent()) {
                        Staff staff = staffOpt.get();


                        // Build detailed alert message indicating which condition failed
                        String conditionStatus = "Missing: ";
                        if (!hasMorningFingerprint) conditionStatus += "[Morning Fingerprint] ";
                        if (!hasStaffEntry) conditionStatus += "[Class Entry Scan] ";


                        // 1. Send Email Notifications (staff + admin)
                        sendStaffAbsenceAlert(
                            staff.getName(),
                            staff.getStaffId(),
                            staff.getEmail(),
                            classEntry.getSubject() + " (" + classEntry.getSectionId() + ")",
                            now,
                            staff.getPhone(),
                            staff.getBranch(),
                            conditionStatus // NEW: Include condition failure details
                        );


                        // 2. Save Alert to Database for Dashboard
                        saveLateAlert(staff, classEntry, now, conditionStatus);


                        System.out.println("✅ Alert processed for " + staffId);


                    } else {
                        logger.warning("⚠️ Staff found in Timetable but NOT in Staff table: " + staffId);
                    }


                } else {
                    System.out.println("⏭️ Class " + classEntry.getSubject() + " not in late window yet");
                    System.out.println(" Start: " + classStart + " | Threshold: " + classStart.plusMinutes(lateThreshold) + " | Now: " + now);
                }
            }


            System.out.println("✅ [SCHEDULER] Check completed at " + LocalTime.now());


        } catch (Exception e) {
            System.err.println("❌ [SCHEDULER ERROR] " + e.getMessage());
            e.printStackTrace();
        }
    }


    // ==================================================================================
    // ✅ SAVE ALERT TO DATABASE WITH CONDITION STATUS
    // ==================================================================================
    /**
     * ✅ Save alert to database for Admin Dashboard with condition failure details
     */
    private void saveLateAlert(Staff staff, Timetable classEntry, LocalTime now, String conditionStatus) {
        try {
            StaffLateAlert alert = new StaffLateAlert();
            alert.setStaffIdNumber(staff.getStaffId());
            alert.setStaffName(staff.getName());
            alert.setAlertDate(LocalDate.now());
            alert.setScheduledTime(classEntry.getScheduledStartTime());
            alert.setActualEntryTime(LocalTime.of(0, 0));
            
            long minutesLate = ChronoUnit.MINUTES.between(classEntry.getScheduledStartTime(), now);
            alert.setMinutesLate((int) minutesLate);
            alert.setTimeSlot(classEntry.getTimeSlot());
            alert.setRoomNumber(classEntry.getRoom());
            alert.setDayOfWeek(classEntry.getDayOfWeek());
            alert.setEntryLogId(0L);
            alert.setNotificationSentToStaff(true);
            alert.setNotificationSentToAdmin(true);
            alert.setAdminAcknowledged(false);


            alertRepository.save(alert);
            System.out.println("💾 Alert saved to DB with reason: " + conditionStatus);


        } catch (Exception e) {
            System.err.println("❌ Failed to save alert to DB: " + e.getMessage());
            e.printStackTrace();
        }
    }


    // ==================================================================================
    // ✅ SEND LATE/ABSENT ALERT EMAIL (MODIFIED WITH CONDITION STATUS)
    // ==================================================================================
    /**
     * ✅ Send LATE/ABSENT email to BOTH staff and admin with condition failure details
     */
    public void sendStaffAbsenceAlert(String staffName, String staffId, String staffEmail,
            String className, LocalTime markTime, String contactInfo,
            String branch, String conditionStatus) {
        try {
            String formattedTime = markTime.format(DateTimeFormatter.ofPattern("HH:mm:ss"));


            // Email to Staff (late/absent)
            String staffSubject = "⚠️ Attendance Alert - " + staffName;
            String staffBody = buildStaffEmailBody(staffName, staffId, className, formattedTime, branch, conditionStatus);
            sendEmail(staffEmail, staffSubject, staffBody);


            // Email to Admin
            String adminSubject = "🚨 Staff Attendance Alert - " + staffName + " (" + staffId + ")";
            String adminBody = buildAdminEmailBody(staffName, staffId, staffEmail, className, formattedTime, contactInfo, branch, conditionStatus);
            sendEmail(adminEmail, adminSubject, adminBody);


            System.out.println("📧 Absence alert emails sent for " + staffName + " to " + staffEmail + " and " + adminEmail);


        } catch (Exception e) {
            System.err.println("❌ Error sending absence alert email: " + e.getMessage());
            e.printStackTrace();
        }
    }


    // ✅ OVERLOAD for backward compatibility (without conditionStatus)
    public void sendStaffAbsenceAlert(String staffName, String staffId, String staffEmail,
            String className, LocalTime markTime, String contactInfo, String branch) {
        sendStaffAbsenceAlert(staffName, staffId, staffEmail, className, markTime, contactInfo, branch, "Attendance not verified");
    }


    // ==================================================================================
    // ✅ SEND VERIFICATION CODE EMAIL (UNCHANGED)
    // ==================================================================================
    /**
     * ✅ NEW METHOD: Send email with verification code ONLY to the staff
     * (you can call your SMS gateway here too if needed)
     */
    public void sendStaffAttendanceCodeEmail(String staffName, String staffId, String staffEmail,
            String className, String sectionId, LocalTime scanTime,
            String verificationCode, String contactInfo, String branch) {
        try {
            System.out.println("📧 [EMAIL] Preparing attendance code email...");
            String formattedTime = scanTime.format(DateTimeFormatter.ofPattern("HH:mm:ss"));


            // Email to Staff with Code (ONLY staff, no admin)
            String staffSubject = "🔐 Attendance Code - " + className + " (" + sectionId + ")";
            String staffBody = buildStaffAttendanceCodeEmailBody(
                staffName, staffId, className, sectionId,
                formattedTime, verificationCode, branch
            );
            sendEmail(staffEmail, staffSubject, staffBody);


            // If later you want SMS, you can integrate here (Twilio, MSG91, etc.)
            System.out.println("🔑 [EMAIL] Verification Code: " + verificationCode);
            System.out.println("✅ [EMAIL] Attendance code email sent successfully!");
            System.out.println(" To: " + staffEmail);
            System.out.println(" Code: " + verificationCode);


        } catch (Exception e) {
            System.err.println("❌ Error sending attendance code email: " + e.getMessage());
            e.printStackTrace();
        }
    }


    // ==================================================================================
    // ✅ EMAIL BODY BUILDERS (UPDATED & EXISTING)
    // ==================================================================================
    
    /**
     * ✅ Build email body for staff WITH verification code
     */
    private String buildStaffAttendanceCodeEmailBody(String staffName, String staffId, String className,
            String sectionId, String time, String verificationCode, String branch) {
        return "Dear " + staffName + ",\n\n" +
            "✅ YOUR ATTENDANCE HAS BEEN MARKED SUCCESSFULLY!\n\n" +
            "═══════════════════════════════════════════\n" +
            "CLASS DETAILS:\n" +
            "═══════════════════════════════════════════\n" +
            "📚 Class: " + className + "\n" +
            "🏫 Section: " + sectionId + "\n" +
            "👤 Staff ID: " + staffId + "\n" +
            "⏰ Time: " + time + "\n" +
            "🏢 Branch: " + branch + "\n\n" +
            "═══════════════════════════════════════════\n" +
            "🔐 STUDENT ATTENDANCE CODE:\n" +
            "═══════════════════════════════════════════\n" +
            "🔑 CODE: " + verificationCode + "\n" +
            "⏰ Valid for: 30 minutes\n\n" +
            "═══════════════════════════════════════════\n" +
            "📝 INSTRUCTIONS FOR STUDENTS:\n" +
            "═══════════════════════════════════════════\n" +
            "1. Open Smart University Management System\n" +
            "2. Go to Mark Attendance → Enter Verification Code\n" +
            "3. Paste code: " + verificationCode + "\n" +
            "4. Click Submit\n" +
            "5. Attendance marked PRESENT ✅\n\n" +
            "⚠️ IMPORTANT NOTES:\n" +
            "• Code expires in 30 minutes\n" +
            "• Each code can only be used ONCE per period\n" +
            "• Do NOT share the code with students outside your class\n" +
            "• Code is valid only for this class session\n\n" +
            "Regards,\n" +
            "Smart University Management System";
    }


    /**
     * ✅ Build email body for staff (attendance alert with condition details)
     */
    private String buildStaffEmailBody(String staffName, String staffId, String className, String time, 
            String branch, String conditionStatus) {
        return "Dear " + staffName + ",\n\n" +
            "This is to notify you regarding your attendance status.\n\n" +
            "═══════════════════════════════════════════\n" +
            "ATTENDANCE DETAILS:\n" +
            "═══════════════════════════════════════════\n" +
            "Staff ID: " + staffId + "\n" +
            "Class: " + className + "\n" +
            "Time: " + time + "\n" +
            "Branch: " + branch + "\n\n" +
            "⚠️ REASON: " + conditionStatus + "\n\n" +
            "═══════════════════════════════════════════\n" +
            "REQUIRED ACTIONS:\n" +
            "═══════════════════════════════════════════\n" +
            "• Ensure you mark morning attendance (fingerprint)\n" +
            "• Scan your RFID card when entering the classroom\n" +
            "• Contact administration if you need assistance\n\n" +
            "Please contact the administration if this is a mistake.\n\n" +
            "Regards,\n" +
            "Smart University Management System";
    }


    /**
     * ✅ Build email body for admin (attendance alert with condition details)
     */
    private String buildAdminEmailBody(String staffName, String staffId, String staffEmail,
            String className, String time, String contactInfo, String branch, String conditionStatus) {
        return "STAFF ATTENDANCE ALERT\n\n" +
            "═══════════════════════════════════════════\n" +
            "STAFF DETAILS:\n" +
            "═══════════════════════════════════════════\n" +
            "Name: " + staffName + "\n" +
            "Staff ID: " + staffId + "\n" +
            "Email: " + staffEmail + "\n" +
            "Contact: " + contactInfo + "\n" +
            "Branch: " + branch + "\n\n" +
            "═══════════════════════════════════════════\n" +
            "CLASS DETAILS:\n" +
            "═══════════════════════════════════════════\n" +
            "Class: " + className + "\n" +
            "Time Marked: " + time + "\n" +
            "Late Threshold: " + lateThreshold + " minutes\n\n" +
            "═══════════════════════════════════════════\n" +
            "❌ FAILURE REASON:\n" +
            "═══════════════════════════════════════════\n" +
            conditionStatus + "\n\n" +
            "TWO-CONDITION VERIFICATION:\n" +
            "• Condition A: Morning Fingerprint\n" +
            "• Condition B: Class Entry Scan (RFID)\n\n" +
            "BOTH conditions must be TRUE to skip the alert.\n" +
            "If either condition is FALSE, this alert is triggered.\n\n" +
            "Please take necessary action as required.\n\n" +
            "Smart University Management System";
    }


    /**
     * ✅ Low-level email sending method
     */
    private void sendEmail(String toEmail, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(body);
            message.setFrom(adminEmail);
            mailSender.send(message);
            System.out.println("✅ Email sent to: " + toEmail);


        } catch (Exception e) {
            System.err.println("❌ Failed to send email to " + toEmail + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}