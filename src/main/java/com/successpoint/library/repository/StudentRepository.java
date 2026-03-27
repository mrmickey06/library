package com.successpoint.library.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.successpoint.library.entity.Student;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    
    // To check if a student already exists before registering
    Optional<Student> findByMobileNumber(String mobileNumber);
    
    // We will use this later for the "Dues" tab in the Admin panel
    List<Student> findByDueDateBefore(LocalDate date);
    // Add this to StudentRepository
    List<Student> findByJoiningDateBetween(LocalDate startDate, LocalDate endDate);
}