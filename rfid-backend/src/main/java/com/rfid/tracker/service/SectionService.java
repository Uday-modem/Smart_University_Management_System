package com.rfid.tracker.service;

import com.rfid.tracker.entity.Section;
import com.rfid.tracker.repository.SectionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SectionService { // <--- LOOK HERE! It says SectionService, NOT StudentService

    @Autowired
    private SectionRepository sectionRepository;

    public List<Section> getSectionsByBranch(String branch) {
        return sectionRepository.findByBranch(branch);
    }

    public List<Section> getSectionsByBranchAndYear(String branch, Integer year) {
        return sectionRepository.findByBranchAndYear(branch, year);
    }
    
    public List<Section> getAvailableSections(String branch, Integer year) {
        return sectionRepository.findAvailableSections(branch, year);
    }

    public Optional<Section> getSectionByCode(String sectionCode) {
        return sectionRepository.findBySectionCode(sectionCode);
    }

    public Section saveSection(Section section) {
        return sectionRepository.save(section);
    }

    public List<Section> getAllSections() {
        return sectionRepository.findAll();
    }

    public List<String> getAllBranches() {
        return sectionRepository.findDistinctBranches();
    }
}
