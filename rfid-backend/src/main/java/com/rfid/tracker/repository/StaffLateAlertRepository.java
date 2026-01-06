package com.rfid.tracker.repository;

import com.rfid.tracker.entity.StaffLateAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;          // ✅ added
import org.springframework.data.repository.query.Param;    // ✅ added
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface StaffLateAlertRepository extends JpaRepository<StaffLateAlert, Long> {

    // Fetch unacknowledged alerts sorted by date descending
    List<StaffLateAlert> findByAdminAcknowledgedFalseOrderByCreatedAtDesc();

    // ✅ Check if an alert already exists for this staff, date, and time slot to prevent duplicates
    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END " +
           "FROM StaffLateAlert a " +
           "WHERE a.staffIdNumber = :staffId " +
           "AND a.alertDate = :alertDate " +
           "AND a.timeSlot = :timeSlot")
    boolean existsByStaffIdNumberAndAlertDateAndTimeSlot(
            @Param("staffId") String staffId,
            @Param("alertDate") LocalDate alertDate,
            @Param("timeSlot") String timeSlot
    );
}
