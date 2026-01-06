package com.rfid.tracker.repository;

import com.rfid.tracker.entity.Section;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SectionRepository extends JpaRepository<Section, Long> {

    // ✅ Find section by code (ECE1A, CSE2B, MECH3C, etc.)
    Optional<Section> findBySectionCode(String sectionCode);

    // ✅ Find all sections for a branch
    List<Section> findByBranch(String branch);

    // ✅ Find sections for specific branch and year
    List<Section> findByBranchAndYear(String branch, Integer year);

    // ✅ Find sections by regulation
    List<Section> findByRegulationId(Long regulationId);

    // ✅ Find available sections (not at capacity)
    @Query("SELECT s FROM Section s WHERE s.branch = :branch AND s.year = :year " +
           "AND s.currentCount < s.capacity ORDER BY s.sectionCode ASC")
    List<Section> findAvailableSections(String branch, Integer year);

    // ✅ Get all distinct branches from sections
    @Query("SELECT DISTINCT s.branch FROM Section s")
    List<String> findDistinctBranches();

    // ✅ Get all years for a specific branch
    @Query("SELECT DISTINCT s.year FROM Section s WHERE s.branch = ?1")
    List<Integer> findDistinctYearsByBranch(String branch);

    // ✅ Find all sections
    List<Section> findAll();
}