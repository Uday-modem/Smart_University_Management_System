package com.rfid.tracker.util;

import com.rfid.tracker.entity.Section;
import com.rfid.tracker.entity.Semester;
import com.rfid.tracker.entity.Staff;
import com.rfid.tracker.entity.StaffExpertise;
import com.rfid.tracker.repository.SectionRepository;
import com.rfid.tracker.repository.SemesterRepository;
import com.rfid.tracker.repository.StaffExpertiseRepository;
import com.rfid.tracker.repository.StaffRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired
    private StaffRepository staffRepository;

    @Autowired
    private StaffExpertiseRepository staffExpertiseRepository;

    @Autowired
    private SectionRepository sectionRepository;

    @Autowired
    private SemesterRepository semesterRepository;

    @Override
    public void run(String... args) throws Exception {
        // Data seeding disabled - database already has data
        System.out.println("--- Data seeding skipped - database already contains data ---");
        return;
        
        /* DISABLED: Data seeding - database already has data
        boolean seeded = false;

        // Seed Staff data
        if (staffRepository.count() == 0) {
            System.out.println("--- NO STAFF DATA FOUND. SEEDING DATABASE ---");

            // Staff objects (branch required)
            Staff staff1 = new Staff("Dr. Alan Turing", "alan.turing@university.com", "123-456-7890", "CSE");
            Staff staff2 = new Staff("Dr. Grace Hopper", "grace.hopper@university.com", "123-456-7891", "CSE");
            Staff staff3 = new Staff("Dr. Tim Berners-Lee", "tim.bl@university.com", "123-456-7892", "CSE");
            staffRepository.saveAll(List.of(staff1, staff2, staff3));

            // Expertise objects (just staff + subject)
            StaffExpertise expertise1 = new StaffExpertise(staff1, "Algorithms");
            StaffExpertise expertise2 = new StaffExpertise(staff1, "Theory of Computation");
            StaffExpertise expertise3 = new StaffExpertise(staff2, "Compilers");
            StaffExpertise expertise4 = new StaffExpertise(staff2, "Digital Logic");
            StaffExpertise expertise5 = new StaffExpertise(staff3, "Web Technologies");
            staffExpertiseRepository.saveAll(List.of(expertise1, expertise2, expertise3, expertise4, expertise5));

            seeded = true;
        } else {
            System.out.println("--- Staff data already exists. Skipping staff seeding. ---");
        }

        // Seed Section data (for branch dropdown)
        if (sectionRepository.count() == 0) {
            System.out.println("--- NO SECTIONS DATA FOUND. SEEDING SECTIONS ---");

            // Create sample sections for different branches and years
            // CSE Branch
            Section cse1a = new Section("22CSE1A", "CSE", 2, null, "A");
            Section cse1b = new Section("22CSE1B", "CSE", 2, null, "B");
            Section cse2a = new Section("21CSE2A", "CSE", 3, null, "A");
            
            // ECE Branch
            Section ece1a = new Section("22ECE1A", "ECE", 2, null, "A");
            Section ece1b = new Section("22ECE1B", "ECE", 2, null, "B");
            
            // EEE Branch
            Section eee1a = new Section("22EEE1A", "EEE", 2, null, "A");
            
            // ME Branch
            Section me1a = new Section("22ME1A", "ME", 2, null, "A");
            
            sectionRepository.saveAll(List.of(cse1a, cse1b, cse2a, ece1a, ece1b, eee1a, me1a));
            
            seeded = true;
            System.out.println("--- Sections seeded: CSE, ECE, EEE, ME branches ---");
        } else {
            System.out.println("--- Sections data already exists. Skipping section seeding. ---");
        }

        // Seed Semester data (for semester dropdown)
        if (semesterRepository.count() == 0) {
            System.out.println("--- NO SEMESTERS DATA FOUND. SEEDING SEMESTERS ---");

            // Create semesters for different years
            // Year 1: Semesters 1 and 2
            Semester sem1_1 = new Semester("Semester 1", 1, 1);
            Semester sem1_2 = new Semester("Semester 2", 2, 1);
            
            // Year 2: Semesters 3 and 4
            Semester sem2_1 = new Semester("Semester 3", 3, 2);
            Semester sem2_2 = new Semester("Semester 4", 4, 2);
            
            // Year 3: Semesters 5 and 6
            Semester sem3_1 = new Semester("Semester 5", 5, 3);
            Semester sem3_2 = new Semester("Semester 6", 6, 3);
            
            // Year 4: Semesters 7 and 8
            Semester sem4_1 = new Semester("Semester 7", 7, 4);
            Semester sem4_2 = new Semester("Semester 8", 8, 4);
            
            semesterRepository.saveAll(List.of(
                sem1_1, sem1_2,
                sem2_1, sem2_2,
                sem3_1, sem3_2,
                sem4_1, sem4_2
            ));
            
            seeded = true;
            System.out.println("--- Semesters seeded: Years 1-4, Semesters 1-8 ---");
        } else {
            System.out.println("--- Semesters data already exists. Skipping semester seeding. ---");
        }

        if (seeded) {
            System.out.println("--- DATABASE SEEDING COMPLETE. ---");
        }
        */
    }
}
