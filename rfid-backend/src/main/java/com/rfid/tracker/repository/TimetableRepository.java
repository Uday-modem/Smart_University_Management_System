package com.rfid.tracker.repository;

import com.rfid.tracker.entity.Timetable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TimetableRepository extends JpaRepository<Timetable, Long> {

    // ✅ EXISTING: sectionId parameter is String (format: 22ECE1A)
    List<Timetable> findBySectionId(String sectionId);

    // ✅ ADDED THIS TO FIX COMPILATION ERROR
    List<Timetable> findByDayOfWeek(String dayOfWeek);

    // ✅ NEW: Find all timetable entries for a specific staff member
    List<Timetable> findByStaffId(String staffId);

    // ✅ NEW: Find timetable by section and day of week
    @Query("SELECT t FROM Timetable t WHERE t.sectionId = :sectionId AND t.dayOfWeek = :dayOfWeek")
    List<Timetable> findBySectionIdAndDayOfWeek(
            @Param("sectionId") String sectionId,
            @Param("dayOfWeek") String dayOfWeek);

    // ✅ NEW: CRITICAL - Validate staff assignment for RFID access control
    // This method checks if a staff member is assigned to teach a specific class
    @Query("SELECT t FROM Timetable t WHERE t.staffId = :staffId AND t.dayOfWeek = :dayOfWeek AND t.timeSlot = :timeSlot")
    Optional<Timetable> findByStaffIdAndDayOfWeekAndTimeSlot(
            @Param("staffId") String staffId,
            @Param("dayOfWeek") String dayOfWeek,
            @Param("timeSlot") String timeSlot);

    // ✅ NEW: Get distinct days for a section (useful for UI)
    @Query("SELECT DISTINCT t.dayOfWeek FROM Timetable t WHERE t.sectionId = :sectionId ORDER BY FIELD(t.dayOfWeek, 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday')")
    List<String> findDistinctDaysBySectionId(@Param("sectionId") String sectionId);

    // ✅ NEW: Find timetable by multiple criteria (advanced search)
    @Query("SELECT t FROM Timetable t WHERE t.sectionId = :sectionId AND t.dayOfWeek = :dayOfWeek AND t.timeSlot = :timeSlot")
    Optional<Timetable> findBySectionIdAndDayOfWeekAndTimeSlot(
            @Param("sectionId") String sectionId,
            @Param("dayOfWeek") String dayOfWeek,
            @Param("timeSlot") String timeSlot);

    // ✅ NEW: Check if a room is occupied at a specific time
    @Query("SELECT t FROM Timetable t WHERE t.room = :room AND t.dayOfWeek = :dayOfWeek AND t.timeSlot = :timeSlot")
    Optional<Timetable> findByRoomAndDayOfWeekAndTimeSlot(
            @Param("room") String room,
            @Param("dayOfWeek") String dayOfWeek,
            @Param("timeSlot") String timeSlot);

    // ✅ NEW: Get staff schedule for a specific day
    @Query("SELECT t FROM Timetable t WHERE t.staffId = :staffId AND t.dayOfWeek = :dayOfWeek ORDER BY t.scheduledStartTime")
    List<Timetable> findByStaffIdAndDayOfWeek(
            @Param("staffId") String staffId,
            @Param("dayOfWeek") String dayOfWeek);

    /**
     * ✅ NEW, MORE ROBUST METHOD
     * Finds a timetable entry by checking if the scanTime falls between the
     * scheduled_start_time and scheduled_end_time for a given room and day.
     *
     * @param roomNumber The room where the scan occurred.
     * @param dayOfWeek The day of the week (e.g., "MONDAY").
     * @param scanTime The exact time of the scan.
     * @return An Optional containing the matching Timetable entry if found.
     */
    @Query("SELECT t FROM Timetable t WHERE t.room = :roomNumber AND t.dayOfWeek = :dayOfWeek AND :scanTime BETWEEN t.scheduledStartTime AND t.scheduledEndTime")
    Optional<Timetable> findByRoomAndDayAndTimeRange(
            @Param("roomNumber") String roomNumber,
            @Param("dayOfWeek") String dayOfWeek,
            @Param("scanTime") LocalTime scanTime
    );
}