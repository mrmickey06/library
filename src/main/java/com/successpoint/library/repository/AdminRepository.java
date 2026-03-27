package com.successpoint.library.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.successpoint.library.entity.Admin;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {
    
    // Custom method to find the admin by their mobile number for login
    Optional<Admin> findByMobileNumber(String mobileNumber);
}