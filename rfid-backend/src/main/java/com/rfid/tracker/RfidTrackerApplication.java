package com.rfid.tracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling; // ✅ Import this

@SpringBootApplication
@EnableScheduling // ✅ Add this annotation
public class RfidTrackerApplication {

    public static void main(String[] args) {
        SpringApplication.run(RfidTrackerApplication.class, args);
    }
}
