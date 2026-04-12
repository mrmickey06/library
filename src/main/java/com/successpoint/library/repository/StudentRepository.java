package com.successpoint.library.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.successpoint.library.entity.Student;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    
    Optional<Student> findByMobileNumber(String mobileNumber);
    
    List<Student> findByDueDateBefore(LocalDate date);

    List<Student> findByApplicationStatus(String applicationStatus);

    List<Student> findByApplicationStatusAndFeeStatus(String applicationStatus, String feeStatus);

    List<Student> findByApplicationStatusAndDueDateBefore(String applicationStatus, LocalDate date);

    List<Student> findByJoiningDateBetween(LocalDate startDate, LocalDate endDate);
}
