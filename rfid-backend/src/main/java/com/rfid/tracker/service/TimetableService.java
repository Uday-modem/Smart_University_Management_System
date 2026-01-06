package com.rfid.tracker.service;

import com.rfid.tracker.entity.Timetable;
import com.rfid.tracker.repository.TimetableRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
public class TimetableService {

    @Autowired
    private TimetableRepository timetableRepository;

    // CHANGED: sectionId parameter is now String (format: 22ECE1A)
    public List<Timetable> getTimetableBySection(String sectionId) {
        return timetableRepository.findBySectionId(sectionId);
    }

    public Timetable createEntry(Timetable entry) {
        return timetableRepository.save(entry);
    }

    public Timetable updateEntry(Long id, Timetable updatedEntry) {
        Optional<Timetable> existing = timetableRepository.findById(id);
        if (existing.isPresent()) {
            Timetable entry = existing.get();
            entry.setDayOfWeek(updatedEntry.getDayOfWeek());
            entry.setTimeSlot(updatedEntry.getTimeSlot());
            entry.setScheduledStartTime(updatedEntry.getScheduledStartTime());
            entry.setScheduledEndTime(updatedEntry.getScheduledEndTime());
            entry.setSubject(updatedEntry.getSubject());
            entry.setStaffId(updatedEntry.getStaffId());
            entry.setRoom(updatedEntry.getRoom());
            return timetableRepository.save(entry);
        }
        return null;
    }

    public void deleteEntry(Long id) {
        timetableRepository.deleteById(id);
    }

    // CHANGED: sectionId parameter is now String
    // Get lunch break entry for a section (special entry where subject = "LUNCH BREAK")
    public Timetable getLunchBreakBySection(String sectionId) {
        List<Timetable> entries = timetableRepository.findBySectionId(sectionId);
        return entries.stream()
                .filter(e -> "LUNCH BREAK".equalsIgnoreCase(e.getSubject()))
                .findFirst()
                .orElse(null);
    }

    // CHANGED: sectionId parameter is now String
    // Create or update lunch break for a section
    public Timetable saveLunchBreak(String sectionId, String timeSlot, LocalTime startTime, LocalTime endTime) {
        List<Timetable> entries = timetableRepository.findBySectionId(sectionId);

        // Check if lunch break already exists
        Optional<Timetable> existingLunch = entries.stream()
                .filter(e -> "LUNCH BREAK".equalsIgnoreCase(e.getSubject()))
                .findFirst();

        Timetable lunchEntry;
        if (existingLunch.isPresent()) {
            lunchEntry = existingLunch.get();
            lunchEntry.setTimeSlot(timeSlot);
            lunchEntry.setScheduledStartTime(startTime);
            lunchEntry.setScheduledEndTime(endTime);
        } else {
            lunchEntry = new Timetable();
            lunchEntry.setSectionId(sectionId); // ✅ Now String
            lunchEntry.setDayOfWeek("ALL"); // Special marker for all days
            lunchEntry.setTimeSlot(timeSlot);
            lunchEntry.setScheduledStartTime(startTime);
            lunchEntry.setScheduledEndTime(endTime);
            lunchEntry.setSubject("LUNCH BREAK");
            lunchEntry.setStaffId("0"); // ✅ FIXED: Changed from 0L to "0" (String)
            lunchEntry.setRoom("LUNCH HALL");
        }

        return timetableRepository.save(lunchEntry);
    }
}