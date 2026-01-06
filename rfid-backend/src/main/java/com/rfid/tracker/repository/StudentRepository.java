package com.rfid.tracker.repository;

import com.rfid.tracker.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    // ========== CRITICAL LOGIN METHODS ==========
    /**
     * Find student by email
     */
    Optional<Student> findByEmail(String email);

    /**
     * Find student by registration number
     */
    /**
     * Find student by roll number
     * ✅ CRITICAL: Required by AuthService.java:39
     */
    Optional<Student> findByRegistrationNumber(String registrationNumber);
    
    /**
     * Find by email OR registration number
     */
    Optional<Student> findByEmailOrRegistrationNumber(String email, String registrationNumber);

    // ========== DUPLICATE CHECKING ==========
    /**
     * Check if student exists by email
     */
    boolean existsByEmail(String email);

    /**
     * Check if student exists by registration number
     */
    boolean existsByRegistrationNumber(String registrationNumber);

    // ========== SECTION ASSIGNMENT ==========
    /**
     * Find students by section ID (code)
     */
    List<Student> findBySectionId(String sectionId);

    /**
     * Find students by section display name (e.g., "ECE-A")
     * ✅ CRITICAL: Required by StudentService.java:176
     */
    List<Student> findBySection(String section);

    // ========== BRANCH & YEAR QUERIES ==========
    /**
     * Find students by branch
     */
    List<Student> findByBranch(String branch);

    /**
     * Find students by year
     */
    List<Student> findByYear(Integer year);

    /**
     * Find students by branch and year
     */
    List<Student> findByBranchAndYear(String branch, Integer year);

    /**
     * Find students by branch, year and attendance status (ordered)
     * ✅ CRITICAL: Required by StudentService.java:104
     * This is used for bulk operations and attendance marking
     */
    List<Student> findByBranchAndYearAndAttendanceStatusOrderByRegistrationNumberAsc(
            String branch, Integer year, String attendanceStatus);

    /**
     * Find students by branch, year and semester
     */
    List<Student> findByBranchAndYearAndSemester(String branch, Integer year, Integer semester);

    /**
     * Find students by branch, year, semester and attendance status
     */
    List<Student> findByBranchAndYearAndSemesterAndAttendanceStatusOrderByRegistrationNumberAsc(
            String branch, Integer year, Integer semester, String attendanceStatus);

    // ========== COUNTING ==========
    /**
     * Count students in a specific section
     */
    long countBySectionId(String sectionId);

    /**
     * Count students in a branch
     */
    long countByBranch(String branch);

    /**
     * Count students by branch and year
     */
    long countByBranchAndYear(String branch, Integer year);

    /**
     * Count students by attendance status
     */
    long countByAttendanceStatus(String attendanceStatus);

    // ========== ATTENDANCE & STATUS ==========
    /**
     * Find active students
     */
    List<Student> findByAttendanceStatus(String attendanceStatus);

    /**
     * Find students by branch and attendance status
     */
    List<Student> findByBranchAndAttendanceStatus(String branch, String attendanceStatus);

    // ========== CUSTOM QUERIES ==========
    /**
     * Find last section ID by branch and year
     * Used for auto-assignment to next available section
     */
    @Query("SELECT s.sectionId FROM Student s " +
           "WHERE s.branch = :branch AND s.year = :year " +
           "AND s.sectionId IS NOT NULL " +
           "ORDER BY s.sectionId DESC LIMIT 1")
    Optional<String> findLastSectionIdByBranchAndYear(
            @Param("branch") String branch,
            @Param("year") Integer year);

    /**
     * Count students in a section (different from countBySectionId)
     */
    long countByBranchAndYearAndSection(String branch, Integer year, String section);

}