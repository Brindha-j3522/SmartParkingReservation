package com.smartparking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SmartParkingApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartParkingApplication.class, args);

        System.out.println();
        System.out.println("========================================");
        System.out.println("   SMART PARKING RESERVATION SYSTEM");
        System.out.println("========================================");
        System.out.println("Application started successfully!");
        System.out.println("Open your application:");
        System.out.println("http://localhost:8080");
        System.out.println("========================================");
    }
}
