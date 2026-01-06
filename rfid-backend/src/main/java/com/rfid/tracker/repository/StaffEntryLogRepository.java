package com.rfid.tracker.repository;

import com.rfid.tracker.entity.StaffEntryLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface StaffEntryLogRepository extends JpaRepository<StaffEntryLog, Long> {

    List<StaffEntryLog> findByStaffIdNumberAndEntryDate(String staffIdNumber, LocalDate entryDate);

    List<StaffEntryLog> findByRoomNumberAndEntryDate(String roomNumber, LocalDate entryDate);

    Optional<StaffEntryLog> findByStaffIdNumberAndEntryDateAndTimeSlot(
            String staffIdNumber, LocalDate entryDate, String timeSlot);

    @Query("SELECT s FROM StaffEntryLog s WHERE s.status = 'LATE' AND s.notificationSent = false ORDER BY s.entryDateTime")
    List<StaffEntryLog> findUnnotifiedLateEntries();

    List<StaffEntryLog> findByStaffIdNumberAndEntryDateBetween(
            String staffIdNumber, LocalDate startDate, LocalDate endDate);

    @Query("SELECT COUNT(s) FROM StaffEntryLog s WHERE s.staffIdNumber = :staffId AND s.entryDate = :date AND s.status = 'ON_TIME'")
    long countOnTimeEntries(@Param("staffId") String staffIdNumber, @Param("date") LocalDate entryDate);

    // ✅ NEW: Delete method for cleanup after staff logout
    // This deletes all RFID room entry logs for a staff on a given date
    @Modifying
    @Transactional
    @Query("DELETE FROM StaffEntryLog s WHERE s.staffIdNumber = :staffId AND s.entryDate = :entryDate")
    void deleteByStaffIdNumberAndEntryDate(
            @Param("staffId") String staffIdNumber,
            @Param("entryDate") LocalDate entryDate);

    // ✅ Additional cleanup method (by userIdentifier if you use that instead)
    @Modifying
    @Transactional
    @Query("DELETE FROM StaffEntryLog s WHERE s.staffIdNumber = :userIdentifier AND s.entryDate = :scanDate")
    void deleteByUserIdentifierAndScanDate(
            @Param("userIdentifier") String userIdentifier,
            @Param("scanDate") LocalDate scanDate);
}
