package com.rfid.tracker.scheduler;

import com.rfid.tracker.entity.*;
import com.rfid.tracker.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Component
public class AttendanceScheduler {

    @Autowired
    private MorningFingerprintLogRepository morningRepo;
    @Autowired
    private PeriodAttendanceLogRepository periodRepo;
    @Autowired
    private AttendanceRepository attendanceRepo;
    @Autowired
    private StudentRepository studentRepo;

    /**
     * Runs every day at 8:00 PM (20:00)
     * Calculates Final Attendance based on Logs
     */
    @Scheduled(cron = "0 0 20 * * *") // 8 PM Daily
    @Transactional
    public void processDailyAttendance() {
        System.out.println("🕒 Starting End-of-Day Attendance Processing...");
        LocalDate today = LocalDate.now();

        // 1. Process STUDENTS
        // Get all morning scans (unprocessed)
        List<MorningFingerprintLog> morningScans = morningRepo.findByUserTypeAndScanDate(
                MorningFingerprintLog.UserType.STUDENT, today);

        for (MorningFingerprintLog fpLog : morningScans) {
            String regNo = fpLog.getUserIdentifier();
            
            // Count Period Scans (RFID)
            long periodCount = periodRepo.countPeriodScans(regNo, today);
            
            // LOGIC: 
            // - Morning Scan is mandatory for any presence.
            // - 7 Periods = Full Day
            // - 4 Periods = Half Day
            
            AttendanceStatus status = AttendanceStatus.ABSENT;
            if (periodCount >= 7) {
                status = AttendanceStatus.PRESENT;
            } else if (periodCount >= 4) {
                status = AttendanceStatus.HALF_DAY;
            } else {
                status = AttendanceStatus.ABSENT; // Or LATE if you prefer
            }

            // Save to Final Attendance Table
            saveFinalAttendance(regNo, status, today, fpLog.getStatus().toString());

            // Cleanup: Delete period logs for this student
            periodRepo.deleteByStudentRegistrationNumberAndScanDate(regNo, today);
            
            // Mark Morning Log as processed
            fpLog.setProcessed(true);
            morningRepo.save(fpLog);
        }
        
        System.out.println("✅ Daily Attendance Processing Complete!");
    }

    private void saveFinalAttendance(String regNo, AttendanceStatus status, LocalDate date, String remarks) {
        Optional<Student> sOpt = studentRepo.findByRegistrationNumber(regNo);
        if (sOpt.isPresent()) {
            Student s = sOpt.get();
            Attendance a = new Attendance();
            a.setStudentId(s.getId());
            a.setUserIdentifier(regNo);
            a.setUserType(Attendance.UserType.STUDENT);
            a.setDate(date);
            a.setStatus(status);
            a.setSectionId(s.getSectionId());
            a.setBranch(s.getBranch());
            a.setRemarks("Morning: " + remarks);
            attendanceRepo.save(a);
        }
    }
}
