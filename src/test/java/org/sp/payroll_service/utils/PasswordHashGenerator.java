package org.sp.payroll_service.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Utility to generate BCrypt password hashes for seed data.
 * Run this main method to generate hashes for test passwords.
 */
public class PasswordHashGenerator {
    
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
        
        System.out.println("=== Password Hash Generator ===\n");
        
        // Admin password
        System.out.println("Admin@123: " + encoder.encode("Admin@123"));
        System.out.println();
        
        // Employer passwords
        System.out.println("Tech@123: " + encoder.encode("Tech@123"));
        System.out.println("Inno@123: " + encoder.encode("Inno@123"));
        System.out.println("Dhaka@123: " + encoder.encode("Dhaka@123"));
        System.out.println();
        
        // Employee passwords
        System.out.println("Dir@123: " + encoder.encode("Dir@123"));
        System.out.println("Mgr@123: " + encoder.encode("Mgr@123"));
        System.out.println("InnDir@123: " + encoder.encode("InnDir@123"));
        System.out.println("InnMgr1@123: " + encoder.encode("InnMgr1@123"));
        System.out.println("InnMgr2@123: " + encoder.encode("InnMgr2@123"));
        System.out.println("InnSr1@123: " + encoder.encode("InnSr1@123"));
        System.out.println("InnSr2@123: " + encoder.encode("InnSr2@123"));
        System.out.println("InnDev1@123: " + encoder.encode("InnDev1@123"));
        System.out.println("InnDev2@123: " + encoder.encode("InnDev2@123"));
        System.out.println("InnJr1@123: " + encoder.encode("InnJr1@123"));
        System.out.println("InnJr2@123: " + encoder.encode("InnJr2@123"));
        System.out.println("InnInt1@123: " + encoder.encode("InnInt1@123"));
        System.out.println("DDir@123: " + encoder.encode("DDir@123"));
        System.out.println("DMgr@123: " + encoder.encode("DMgr@123"));
        System.out.println("DSr@123: " + encoder.encode("DSr@123"));
        System.out.println("DDev@123: " + encoder.encode("DDev@123"));
        System.out.println("DJr@123: " + encoder.encode("DJr@123"));
        System.out.println("DInt@123: " + encoder.encode("DInt@123"));
    }
}
