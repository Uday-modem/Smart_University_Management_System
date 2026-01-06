package com.rfid.tracker.service;


import com.rfid.tracker.entity.*;
import com.rfid.tracker.repository.*;
import com.rfid.tracker.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.Duration;
import java.util.Locale;
import java.util.Optional;
import java.util.List;


@Service
public class HardwareIntegrationService {


    @Autowired
    private FingerprintTemplateRepository fingerprintTemplateRepository;


    @Autowired
    private MorningFingerprintLogRepository morningFingerprintLogRepository;


    @Autowired
    private PeriodAttendanceLogRepository periodAttendanceLogRepository;


    @Autowired
    private StaffEntryLogRepository staffEntryLogRepository;


    @Autowired
    private StaffLateAlertRepository staffLateAlertRepository;


    @Autowired
    private HardwareDeviceRepository hardwareDeviceRepository;


    @Autowired
    private AttendanceRepository attendanceRepository;


    @Autowired
    private AttendanceProcessingLogRepository attendanceProcessingLogRepository;


    @Autowired
    private StudentRepository studentRepository;


    @Autowired
    private StaffRepository staffRepository;

    @Autowired
    private RfidCardRepository rfidCardRepository;

    @Autowired
    private TimetableRepository timetableRepository;


    @Autowired
    private EmailService emailService;


    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final LocalTime MORNING_ATTENDANCE_START = LocalTime.of(9, 0);
    private static final LocalTime MORNING_ATTENDANCE_END = LocalTime.of(9, 10);
    private static final LocalTime LOGOUT_TIME_START = LocalTime.of(16, 0);
    private static final int LATE_THRESHOLD_MINUTES = 10;


    public HardwareResponseDTO processFingerprint(FingerprintScanRequest request) {
        try {
            LocalTime scanTime = LocalTime.parse(request.getScanTime(), TIME_FORMATTER);
            LocalDate scanDate = LocalDate.parse(request.getScanDate(), DATE_FORMATTER);


            Optional<FingerprintTemplate> template = fingerprintTemplateRepository
                    .findByFingerprintId(request.getFingerprintId());


            if (!template.isPresent()) {
                return new HardwareResponseDTO("ERROR", "Fingerprint not found in database");
            }


            FingerprintTemplate fp = template.get();
            if (scanTime.isAfter(LOGOUT_TIME_START) || scanTime.equals(LOGOUT_TIME_START)) {
                return processLogoutFingerprint(fp, scanTime, scanDate, request.getFingerprintId());
            } else {
                return processMorningFingerprint(fp, scanTime, scanDate, request.getFingerprintId());
            }
        } catch (Exception e) {
            return new HardwareResponseDTO("ERROR", "Error processing fingerprint: " + e.getMessage());
        }
    }


    private HardwareResponseDTO processMorningFingerprint(FingerprintTemplate fp, LocalTime scanTime,
                                                          LocalDate scanDate, Integer fingerprintId) {
        Optional<MorningFingerprintLog> existing = morningFingerprintLogRepository
                .findByUserIdentifierAndScanDateAndUserType(
                        fp.getUserIdentifier(),
                        scanDate,
                        fp.getUserType() == FingerprintTemplate.UserType.STUDENT ?
                                MorningFingerprintLog.UserType.STUDENT :
                                MorningFingerprintLog.UserType.STAFF
                );


        if (existing.isPresent()) {
            return new HardwareResponseDTO("ERROR", "Morning fingerprint already recorded today");
        }


        // ✅ FIXED: Updated Enum Name to 'Status'
        MorningFingerprintLog.Status status = scanTime.isBefore(MORNING_ATTENDANCE_END) ?
                MorningFingerprintLog.Status.ON_TIME :
                MorningFingerprintLog.Status.LATE;


        MorningFingerprintLog log = new MorningFingerprintLog();
        log.setUserType(fp.getUserType() == FingerprintTemplate.UserType.STUDENT ?
                MorningFingerprintLog.UserType.STUDENT :
                MorningFingerprintLog.UserType.STAFF);
        log.setUserIdentifier(fp.getUserIdentifier());
        log.setFingerprintId(fingerprintId);
        log.setScanDate(scanDate);
        log.setScanTime(scanTime);
        log.setStatus(status);


        morningFingerprintLogRepository.save(log);


        UserInfoDTO userInfo = getUserInfoByIdentifier(fp.getUserIdentifier(), fp.getUserType().toString());
        return new HardwareResponseDTO("SUCCESS", "Morning check-in recorded: " + status.toString(), userInfo);
    }


    @Transactional
    private HardwareResponseDTO processLogoutFingerprint(FingerprintTemplate fp, LocalTime scanTime,
                                                         LocalDate scanDate, Integer fingerprintId) {
        Optional<MorningFingerprintLog> morningLog = morningFingerprintLogRepository
                .findByUserIdentifierAndScanDateAndUserType(
                        fp.getUserIdentifier(),
                        scanDate,
                        fp.getUserType() == FingerprintTemplate.UserType.STUDENT ?
                                MorningFingerprintLog.UserType.STUDENT :
                                MorningFingerprintLog.UserType.STAFF
                );


        if (!morningLog.isPresent()) {
            return new HardwareResponseDTO("ERROR", "No morning check-in found. Please scan morning fingerprint first.");
        }


        MorningFingerprintLog log = morningLog.get();
        if (log.getLogoutFingerprintId() != null) {
            return new HardwareResponseDTO("ERROR", "Evening check-out already recorded today");
        }


        log.setLogoutFingerprintId(fingerprintId);
        log.setLogoutScanTime(scanTime);
        log.setLogoutScanDateTime(LocalDateTime.now());
        morningFingerprintLogRepository.save(log);


        HardwareResponseDTO processingResult = autoProcessAttendance(fp.getUserIdentifier(), scanDate,
                fp.getUserType().toString());
        UserInfoDTO userInfo = getUserInfoByIdentifier(fp.getUserIdentifier(), fp.getUserType().toString());
        userInfo.setEmail(processingResult.getMessage());


        return new HardwareResponseDTO("SUCCESS", "Evening check-out recorded. " + processingResult.getMessage(), userInfo);
    }


    @Transactional
    public HardwareResponseDTO autoProcessAttendance(String userIdentifier, LocalDate date, String userType) {
        try {
            MorningFingerprintLog.UserType logUserType = "STUDENT".equals(userType) ?
                    MorningFingerprintLog.UserType.STUDENT :
                    MorningFingerprintLog.UserType.STAFF;


            Optional<MorningFingerprintLog> morningLog = morningFingerprintLogRepository
                    .findByUserIdentifierAndScanDateAndUserType(userIdentifier, date, logUserType);


            if (!morningLog.isPresent()) {
                return new HardwareResponseDTO("ERROR", "No morning check-in found");
            }


            MorningFingerprintLog log = morningLog.get();
            String finalStatus;
            String processingNotes;
            long periodCount = 0;


            if ("STUDENT".equals(userType)) {
                periodCount = periodAttendanceLogRepository.countPeriodScans(userIdentifier, date);


                if (log.getLogoutFingerprintId() == null) {
                    finalStatus = "ABSENT";
                    processingNotes = "No evening check-out recorded";
                } else if (periodCount >= 7) {
                    // ✅ FIXED: Updated Enum Name to 'Status'
                    finalStatus = log.getStatus() == MorningFingerprintLog.Status.ON_TIME ? "PRESENT" : "LATE";
                    processingNotes = String.format("Full attendance %d periods, Morning %s", periodCount, log.getStatus());
                } else if (periodCount >= 4) {
                    finalStatus = "HALFDAY";
                    processingNotes = String.format("Half attendance %d periods (4-6 range)", periodCount);
                } else {
                    finalStatus = "ABSENT";
                    processingNotes = String.format("Insufficient periods only %d scanned (minimum 4)", periodCount);
                }
            } else {
                if (log.getLogoutFingerprintId() == null) {
                    finalStatus = "ABSENT";
                    processingNotes = "No evening check-out recorded";
                } else {
                    // ✅ FIXED: Updated Enum Name to 'Status'
                    finalStatus = log.getStatus() == MorningFingerprintLog.Status.ON_TIME ? "PRESENT" : "LATE";
                    processingNotes = String.format("Fingerprint attendance Morning %s, Evening logout completed", log.getStatus());
                }
            }


            Attendance attendance = new Attendance();
            if ("STUDENT".equals(userType)) {
                Optional<Student> student = studentRepository.findByRegistrationNumber(userIdentifier);
                if (student.isPresent()) {
                    attendance.setStudentId(student.get().getId());
                    attendance.setSectionId(student.get().getSectionId());
                    attendance.setBranch(student.get().getBranch());
                }
            } else {
                Optional<Staff> staff = staffRepository.findByStaffId(userIdentifier);
                if (staff.isPresent()) {
                    attendance.setStaffId(staff.get().getStaffId());
                    attendance.setBranch(staff.get().getBranch());
                }
            }


            attendance.setDate(date);
            attendance.setStatus(AttendanceStatus.valueOf(finalStatus));
            attendance.setMarkTime(LocalTime.now());
            attendance.setRemarks("Auto-processed: " + processingNotes);


            attendanceRepository.save(attendance);


            AttendanceProcessingLog processLog = new AttendanceProcessingLog();
            processLog.setUserType(AttendanceProcessingLog.UserType.valueOf(userType));
            processLog.setUserIdentifier(userIdentifier);
            processLog.setProcessDate(date);
            // ✅ FIXED: Updated Enum Name to 'Status'
            processLog.setMorningFingerprintStatus(log.getStatus() == MorningFingerprintLog.Status.ON_TIME ?
                    AttendanceProcessingLog.MorningFingerprintStatus.ON_TIME :
                    AttendanceProcessingLog.MorningFingerprintStatus.LATE);
            processLog.setLogoutFingerprintStatus(log.getLogoutFingerprintId() != null ?
                    AttendanceProcessingLog.LogoutFingerprintStatus.COMPLETED :
                    AttendanceProcessingLog.LogoutFingerprintStatus.MISSING);
            processLog.setTotalPeriodsScanned((int) periodCount);
            processLog.setFinalStatus(AttendanceProcessingLog.FinalStatus.valueOf(finalStatus));
            processLog.setProcessingNotes(processingNotes);


            attendanceProcessingLogRepository.save(processLog);


            log.setProcessed(true);
            morningFingerprintLogRepository.save(log);


            cleanupPeriodLogs(userIdentifier, date, userType);


            return new HardwareResponseDTO("SUCCESS", String.format("Attendance processed: %s", finalStatus));
        } catch (Exception e) {
            return new HardwareResponseDTO("ERROR", "Error processing attendance: " + e.getMessage());
        }
    }


    @Transactional
    private void cleanupPeriodLogs(String userIdentifier, LocalDate date, String userType) {
        try {
            if ("STUDENT".equals(userType)) {
                periodAttendanceLogRepository.deleteByStudentRegistrationNumberAndScanDate(userIdentifier, date);
                System.out.println("Cleaned up period logs for student: " + userIdentifier);
            } else {
                staffEntryLogRepository.deleteByStaffIdNumberAndEntryDate(userIdentifier, date);
                System.out.println("Cleaned up entry logs for staff: " + userIdentifier);
            }
        } catch (Exception e) {
            System.err.println("Error cleaning up logs: " + e.getMessage());
        }
    }


    @Transactional
    public HardwareResponseDTO processRFIDScan(RFIDScanRequest request) {
        
        try {
            LocalTime scanTime = LocalTime.parse(request.getScanTime(), TIME_FORMATTER);
            LocalDate scanDate = LocalDate.parse(request.getScanDate(), DATE_FORMATTER);

            // ✅ FIXED: Look up RFID card mapping FIRST
            Optional<RfidCard> rfidCard = rfidCardRepository.findByCardUidAndIsActiveTrue(request.getCardUid());
            
            if (!rfidCard.isPresent()) {
                System.out.println(">>> RFID CARD NOT FOUND: " + request.getCardUid());
                return new HardwareResponseDTO("ERROR", "RFID card not enrolled in system");
            }

            RfidCard card = rfidCard.get();
            System.out.println(">>> RFID CARD FOUND: " + card.getCardUid());
            System.out.println(">>> User Type: " + card.getUserType());
            System.out.println(">>> User ID: " + card.getUserId());

            // ✅ FIXED: Route based on user_type from rfid_cards
            if (RfidCard.UserType.STUDENT.equals(card.getUserType())) {
                Optional<Student> student = studentRepository.findById(card.getUserId());
                if (student.isPresent()) {
                    return processStudentRFID(student.get(), request, scanTime, scanDate);
                } else {
                    return new HardwareResponseDTO("ERROR", "Student not found for this RFID card");
                }
            } else if (RfidCard.UserType.STAFF.equals(card.getUserType())) {
                Optional<Staff> staff = staffRepository.findById(card.getUserId());
                if (staff.isPresent()) {
                    return processStaffRFID(staff.get(), request, scanTime, scanDate);
                } else {
                    return new HardwareResponseDTO("ERROR", "Staff not found for this RFID card");
                }
            }

            return new HardwareResponseDTO("ERROR", "Invalid RFID card user type");
        } catch (Exception e) {
            System.err.println(">>> ERROR PROCESSING RFID: " + e.getMessage());
            e.printStackTrace();
            return new HardwareResponseDTO("ERROR", "Error processing RFID scan: " + e.getMessage());
        }
    }


    private HardwareResponseDTO processStudentRFID(Student student, RFIDScanRequest request,
                                                    LocalTime scanTime, LocalDate scanDate) {
        try {
            String timeSlot = getCurrentTimeSlot(scanTime);
            if (timeSlot == null) {
                return new HardwareResponseDTO("ERROR", "No class scheduled for this time");
            }


            Optional<PeriodAttendanceLog> existing = periodAttendanceLogRepository
                    .findByStudentRegistrationNumberAndTimeSlotAndScanDate(
                            student.getRegistrationNumber(), timeSlot, scanDate);


            if (existing.isPresent()) {
                return new HardwareResponseDTO("ERROR", "Already scanned for this period");
            }


            PeriodAttendanceLog log = new PeriodAttendanceLog(student.getRegistrationNumber(),
                    student.getSectionId(), scanDate, scanTime, timeSlot);
            log.setRoomNumber(request.getRoomNumber());


            String dayOfWeek = scanDate.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
            log.setDayOfWeek(dayOfWeek);


            periodAttendanceLogRepository.save(log);


            long periodCount = periodAttendanceLogRepository.countPeriodScans(student.getRegistrationNumber(), scanDate);


            UserInfoDTO userInfo = new UserInfoDTO(student.getId().toString(),
                    student.getRegistrationNumber(), student.getName(), "STUDENT");
            userInfo.setSectionId(student.getSectionId());
            userInfo.setBranch(student.getBranch());


            return new HardwareResponseDTO("SUCCESS",
                    String.format("Student attendance recorded: %s (%d/7 periods)", timeSlot, periodCount), userInfo);
        } catch (Exception e) {
            return new HardwareResponseDTO("ERROR", "Error processing student RFID: " + e.getMessage());
        }
    }


    private HardwareResponseDTO processStaffRFID(Staff staff, RFIDScanRequest request,
                                                  LocalTime scanTime, LocalDate scanDate) {
        try {
            String timeSlot = getCurrentTimeSlot(scanTime);
            if (timeSlot == null) {
                return new HardwareResponseDTO("ERROR", "No class scheduled for this time");
            }


            String dayOfWeek = scanDate.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH);


            Optional<Timetable> timetableEntry = timetableRepository
                    .findByStaffIdAndDayOfWeekAndTimeSlot(staff.getStaffId(), dayOfWeek, timeSlot);


            if (!timetableEntry.isPresent()) {
                System.out.println("UNAUTHORIZED ACCESS ATTEMPT");
                System.out.println("Staff: " + staff.getName() + " (" + staff.getStaffId() + ")");
                System.out.println("Day: " + dayOfWeek);
                System.out.println("Time: " + timeSlot);
                System.out.println("Room: " + request.getRoomNumber());
                System.out.println("Result: NO CLASS ASSIGNED");
                return new HardwareResponseDTO("ERROR", "Access denied: This class is not assigned to you");
            }


            Timetable assignedClass = timetableEntry.get();


            if (!assignedClass.getRoom().equals(request.getRoomNumber())) {
                System.out.println("WRONG ROOM ATTEMPT");
                System.out.println("Staff: " + staff.getName());
                System.out.println("Assigned Room: " + assignedClass.getRoom());
                System.out.println("Scanned Room: " + request.getRoomNumber());
                return new HardwareResponseDTO("ERROR",
                        String.format("Wrong room: Your class is in Room %s", assignedClass.getRoom()));
            }


            System.out.println("Staff RFID Validation Passed");
            System.out.println("Staff: " + staff.getName());
            System.out.println("Subject: " + assignedClass.getSubject());
            System.out.println("Room: " + assignedClass.getRoom());
            System.out.println("Time Slot: " + timeSlot);


            Optional<StaffEntryLog> existingEntry = staffEntryLogRepository
                    .findByStaffIdNumberAndEntryDateAndTimeSlot(staff.getStaffId(), scanDate, timeSlot);


            if (existingEntry.isPresent()) {
                return new HardwareResponseDTO("ERROR", "Already scanned for this class period");
            }


            StaffEntryLog log = new StaffEntryLog(staff.getStaffId(), request.getRoomNumber(), scanDate, scanTime);
            log.setStaffDbId(Math.toIntExact(staff.getId()));
            log.setTimeSlot(timeSlot);
            log.setDayOfWeek(dayOfWeek);


            LocalTime expectedTime = getExpectedStartTime(timeSlot);
            log.setExpectedTime(expectedTime);


            long minutesLate = Duration.between(expectedTime, scanTime).toMinutes();


            if (minutesLate > LATE_THRESHOLD_MINUTES) {
                // ✅ FIXED: Updated Enum Name to 'Status'
                log.setStatus(StaffEntryLog.Status.LATE);


                StaffLateAlert alert = new StaffLateAlert();
                alert.setStaffIdNumber(staff.getStaffId());
                alert.setStaffName(staff.getName());
                alert.setScheduledTime(expectedTime);
                alert.setActualEntryTime(scanTime);
                alert.setMinutesLate((int) minutesLate);
                alert.setRoomNumber(request.getRoomNumber());
                alert.setTimeSlot(timeSlot);
                alert.setDayOfWeek(dayOfWeek);
                alert.setAlertDate(scanDate);


                StaffEntryLog savedLog = staffEntryLogRepository.save(log);
                alert.setEntryLogId(savedLog.getId());
                staffLateAlertRepository.save(alert);


                try {
                    String className = assignedClass.getSubject() + " - Room " + request.getRoomNumber();
                    emailService.sendStaffAbsenceAlert(staff.getName(), staff.getStaffId(),
                            staff.getEmail(), className, scanTime, staff.getPhone(), staff.getBranch());


                    alert.setNotificationSentToAdmin(true);
                    alert.setNotificationSentToStaff(true);
                    staffLateAlertRepository.save(alert);
                } catch (Exception e) {
                    System.err.println("Error sending email alert: " + e.getMessage());
                }


                System.out.println("LATE ALERT CREATED: " + staff.getName() + " was " + minutesLate + " minutes late for " +
                        assignedClass.getSubject());


                UserInfoDTO userInfo = new UserInfoDTO(staff.getId().toString(), staff.getStaffId(),
                        staff.getName(), "STAFF");
                userInfo.setBranch(staff.getBranch());


                return new HardwareResponseDTO("WARNING",
                        String.format("%s - Class entry recorded LATE by %d minutes. Alert sent to admin.",
                                assignedClass.getSubject(), minutesLate), userInfo);
            } else {
                // ✅ FIXED: Updated Enum Name to 'Status'
                log.setStatus(StaffEntryLog.Status.ON_TIME);
                staffEntryLogRepository.save(log);


                UserInfoDTO userInfo = new UserInfoDTO(staff.getId().toString(), staff.getStaffId(),
                        staff.getName(), "STAFF");
                userInfo.setBranch(staff.getBranch());


                return new HardwareResponseDTO("SUCCESS",
                        String.format("%s - Class entry recorded ONTIME", assignedClass.getSubject()), userInfo);
            }
        } catch (Exception e) {
            System.err.println("Error processing staff RFID: " + e.getMessage());
            e.printStackTrace();
            return new HardwareResponseDTO("ERROR", "Error processing staff RFID: " + e.getMessage());
        }
    }


    public UserInfoDTO getUserInfoByIdentifier(String identifier, String userType) {
        try {
            if ("STUDENT".equals(userType)) {
                Optional<Student> student = studentRepository.findByRegistrationNumber(identifier);
                if (student.isPresent()) {
                    Student s = student.get();
                    UserInfoDTO dto = new UserInfoDTO(s.getId().toString(), s.getRegistrationNumber(),
                            s.getName(), "STUDENT");
                    dto.setSectionId(s.getSectionId());
                    dto.setBranch(s.getBranch());
                    dto.setEmail(s.getEmail());
                    return dto;
                }
            } else if ("STAFF".equals(userType)) {
                Optional<Staff> staff = staffRepository.findByStaffId(identifier);
                if (staff.isPresent()) {
                    Staff s = staff.get();
                    UserInfoDTO dto = new UserInfoDTO(s.getId().toString(), s.getStaffId(),
                            s.getName(), "STAFF");
                    dto.setBranch(s.getBranch());
                    dto.setEmail(s.getEmail());
                    return dto;
                }
            }
        } catch (Exception e) {
            System.out.println("Error getting user info: " + e.getMessage());
        }
        return null;
    }


    private String getCurrentTimeSlot(LocalTime time) {
    // Morning periods
    if (time.compareTo(LocalTime.of(9, 0)) >= 0 && time.compareTo(LocalTime.of(10, 0)) < 0) return "09:00-10:00";
    if (time.compareTo(LocalTime.of(10, 0)) >= 0 && time.compareTo(LocalTime.of(11, 0)) < 0) return "10:00-11:00";
    if (time.compareTo(LocalTime.of(11, 0)) >= 0 && time.compareTo(LocalTime.of(12, 0)) < 0) return "11:00-12:00";
    if (time.compareTo(LocalTime.of(12, 0)) >= 0 && time.compareTo(LocalTime.of(13, 0)) < 0) return "12:00-13:00";
    
    // Lunch break (13:00-13:30) - NO CLASSES
    if (time.compareTo(LocalTime.of(13, 0)) >= 0 && time.compareTo(LocalTime.of(13, 30)) < 0) return null;
    
    // Afternoon periods ✅ FIXED!
    if (time.compareTo(LocalTime.of(13, 30)) >= 0 && time.compareTo(LocalTime.of(14, 30)) < 0) return "13:30-14:30";
    if (time.compareTo(LocalTime.of(14, 30)) >= 0 && time.compareTo(LocalTime.of(15, 30)) < 0) return "14:30-15:30";
    if (time.compareTo(LocalTime.of(15, 30)) >= 0 && time.compareTo(LocalTime.of(16, 30)) < 0) return "15:30-16:30";
    
    return null;
}


    

    private LocalTime getExpectedStartTime(String timeSlot) {
        String[] parts = timeSlot.split("-");
        return LocalTime.parse(parts[0], DateTimeFormatter.ofPattern("HH:mm"));
    }


    public HardwareResponseDTO registerDevice(HardwareDevice device) {
        try {
            HardwareDevice saved = hardwareDeviceRepository.save(device);
            return new HardwareResponseDTO("SUCCESS", "Device registered successfully", saved);
        } catch (Exception e) {
            return new HardwareResponseDTO("ERROR", "Error registering device: " + e.getMessage());
        }
    }


    public HardwareResponseDTO updateDevicePing(String deviceId) {
        try {
            Optional<HardwareDevice> device = hardwareDeviceRepository.findByDeviceId(deviceId);
            if (device.isPresent()) {
                HardwareDevice d = device.get();
                d.setLastPing(LocalDateTime.now());
                hardwareDeviceRepository.save(d);
                return new HardwareResponseDTO("SUCCESS", "Device ping updated");
            }
            return new HardwareResponseDTO("ERROR", "Device not found");
        } catch (Exception e) {
            return new HardwareResponseDTO("ERROR", "Error updating device ping: " + e.getMessage());
        }
    }
}