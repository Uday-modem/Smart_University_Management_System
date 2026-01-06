package com.rfid.tracker.repository;

import com.rfid.tracker.entity.Semester;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SemesterRepository extends JpaRepository<Semester, Long> {
    List<Semester> findByYear(Integer year);
}
