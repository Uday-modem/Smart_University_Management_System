package com.rfid.tracker.repository;

import com.rfid.tracker.entity.MindMap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MindMapRepository extends JpaRepository<MindMap, String> {

    /**
     * Find all mind maps for a specific student
     */
    List<MindMap> findByStudentId(Long studentId);

    /**
     * Find all favorite mind maps for a student
     */
    List<MindMap> findByStudentIdAndIsFavoriteTrue(Long studentId);

    /**
     * Count mind maps for a student
     */
    long countByStudentId(Long studentId);

    /**
     * Find mind maps by status
     */
    List<MindMap> findByStatus(String status);

    /**
     * Find mind maps by subject
     */
    List<MindMap> findBySubject(String subject);
}