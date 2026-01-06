package com.rfid.tracker.repository;

import com.rfid.tracker.entity.Marks;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MarksRepository extends JpaRepository<Marks, Long> {
    List<Marks> findByStudentIdAndSemester(Long studentId, Integer semester);
    List<Marks> findByStudentId(Long studentId);
    Optional<Marks> findByStudentIdAndSubjectIdAndSemester(Long studentId, Long subjectId, Integer semester);
}
