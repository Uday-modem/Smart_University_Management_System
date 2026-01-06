package com.rfid.tracker.service;

import com.rfid.tracker.entity.Regulation;
import com.rfid.tracker.entity.Section;
import com.rfid.tracker.entity.Student;
import com.rfid.tracker.repository.RegulationRepository;
import com.rfid.tracker.repository.SectionRepository;
import com.rfid.tracker.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class StudentService {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private SectionRepository sectionRepository;

    @Autowired
    private RegulationRepository regulationRepository;

    private static final int SECTION_CAPACITY = 50;

    @Transactional
    public void assignSectionAtSignup(Student student) {
        String branch = student.getBranch();
        Integer year = student.getYear();
        Section assignedSection = findOrCreateSection(branch, year);

        student.setSectionId(assignedSection.getSectionCode());
        student.setSection(assignedSection.getDisplayName());
        assignedSection.setCurrentCount(assignedSection.getCurrentCount() + 1);
        sectionRepository.save(assignedSection);
    }

    private Section findOrCreateSection(String branch, Integer year) {
        List<Section> sections = sectionRepository.findByBranchAndYear(branch, year);
        sections.sort((s1, s2) -> {
            String l1 = s1.getSectionLetter() == null ? "A" : s1.getSectionLetter();
            String l2 = s2.getSectionLetter() == null ? "A" : s2.getSectionLetter();
            return l1.compareTo(l2);
        });

        for (Section s : sections) {
            if (s.getCurrentCount() < s.getCapacity()) {
                return s;
            }
        }
        return createNewSection(branch, year, sections);
    }

    private Section createNewSection(String branch, Integer year, List<Section> existingSections) {
        String nextLetter = "A";
        if (!existingSections.isEmpty()) {
            String lastLetter = existingSections.get(existingSections.size() - 1).getSectionLetter();
            if (lastLetter != null && !lastLetter.isEmpty()) {
                char nextChar = (char) (lastLetter.charAt(0) + 1);
                nextLetter = String.valueOf(nextChar);
            }
        }

        String newSectionCode = branch.toUpperCase() + year + nextLetter;
        Section newSection = new Section();
        newSection.setSectionCode(newSectionCode);
        newSection.setBranch(branch);
        newSection.setYear(year);
        newSection.setSectionLetter(nextLetter);
        newSection.setDisplayName(branch.toUpperCase() + "-" + nextLetter);
        newSection.setCapacity(SECTION_CAPACITY);
        newSection.setCurrentCount(0);
        return sectionRepository.save(newSection);
    }

    @Transactional
    public String bulkAssignSections(String branch, Integer year) {
        List<Student> students = studentRepository.findByBranchAndYearAndAttendanceStatusOrderByRegistrationNumberAsc(branch, year, "ACTIVE");
        if (students.isEmpty()) return "No active students found";

        int sectionIndex = 0;
        for (Student student : students) {
            if (sectionIndex % SECTION_CAPACITY == 0) {
                char sectionLetter = (char) ('A' + (sectionIndex / SECTION_CAPACITY));
                String sectionCode = branch.toUpperCase() + year + sectionLetter;
                
                if (sectionRepository.findBySectionCode(sectionCode).isEmpty()) {
                     Section s = new Section();
                     s.setSectionCode(sectionCode);
                     s.setBranch(branch);
                     s.setYear(year);
                     s.setSectionLetter(String.valueOf(sectionLetter));
                     s.setDisplayName(branch.toUpperCase() + "-" + sectionLetter);
                     s.setCapacity(SECTION_CAPACITY);
                     s.setCurrentCount(0);
                     sectionRepository.save(s);
                }
            }
            char sectionLetter = (char) ('A' + (sectionIndex / SECTION_CAPACITY));
            String sectionCode = branch.toUpperCase() + year + sectionLetter;
            Optional<Section> sectionOpt = sectionRepository.findBySectionCode(sectionCode);
            
            if (sectionOpt.isPresent()) {
                Section sec = sectionOpt.get();
                student.setSectionId(sec.getSectionCode());
                student.setSection(sec.getDisplayName());
                sec.setCurrentCount(sec.getCurrentCount() + 1);
                sectionRepository.save(sec);
            }
            sectionIndex++;
        }
        studentRepository.saveAll(students);
        return "Successfully assigned " + students.size() + " students";
    }

    public List<Student> getStudentsBySectionId(String sectionId) {
        return studentRepository.findBySectionId(sectionId);
    }

    public List<Student> getStudentsBySection(String section) {
        return studentRepository.findBySection(section);
    }

    public Optional<Student> getStudentById(Long id) {
        return studentRepository.findById(id);
    }

    public Optional<Student> getStudentByEmail(String email) {
        return studentRepository.findByEmail(email);
    }

    public Student saveStudent(Student student) {
        return studentRepository.save(student);
    }
}
