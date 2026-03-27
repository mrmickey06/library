package com.successpoint.library.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.successpoint.library.entity.Admin;
import com.successpoint.library.repository.AdminRepository;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner loadData(AdminRepository adminRepository) {
        return args -> {
            // Check if an admin already exists in MySQL
            if (adminRepository.count() == 0) {
                Admin admin = new Admin();
                admin.setName("Main Admin");
                admin.setLibraryName("Success Point Library");
                admin.setMobileNumber("9999999999"); // Demo mobile
                admin.setPassword("admin123");       // Demo password
                
                adminRepository.save(admin);
                System.out.println("Demo Admin created in MySQL: Mobile - 9999999999, Password - admin123");
            }
        };
    }
}