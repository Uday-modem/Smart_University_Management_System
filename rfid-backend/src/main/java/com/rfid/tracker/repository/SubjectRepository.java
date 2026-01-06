package com.rfid.tracker.repository;

import com.rfid.tracker.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubjectRepository extends JpaRepository<Subject, Long> {
    List<Subject> findByBranchAndYearAndSemesterAndRegulationId(String branch, Integer year, Integer semester, Long regulationId);
    List<Subject> findByRegulationId(Long regulationId);
}
