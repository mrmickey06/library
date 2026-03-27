package com.successpoint.library.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.successpoint.library.entity.Student;
import com.successpoint.library.repository.StudentRepository;

@Service
public class StudentService {

   @Autowired
    private StudentRepository studentRepository;

    // ADD THIS NEW LINE:
    @Autowired
    private EmailService emailService;

    // UPDATE THIS METHOD:
    public Student registerStudent(Student student) {
        LocalDate today = LocalDate.now();

        // Set all date fields for new student
        student.setJoiningDate(today);
        student.setOriginalJoiningDate(today);  // First time joining - original date
        student.setLastRenewalDate(null);       // Not renewed yet
        student.setRenewalCount(0);             // No renewals yet
        student.setTotalFeesCollected(student.getFeesPaid()); // First payment
        student.setDueDate(today.plusMonths(student.getFeesPeriodMonths()));
        student.setPassword("SP@123");

        Student savedStudent = studentRepository.save(student);

        // ADD THIS NEW LINE to send the email after saving to the database
        emailService.sendInvoiceEmail(savedStudent);

        return savedStudent;
    }

    // NEW METHOD: Renew student membership (for fee payment renewals)
    public Student renewStudent(Long id, int feesPeriodMonths, double feesPaid) {
        Student student = studentRepository.findById(id).orElse(null);
        if (student == null) return null;

        LocalDate today = LocalDate.now();

        // Update joining date to renewal date (what user sees as current period start)
        student.setJoiningDate(today);
        student.setLastRenewalDate(today);

        // Calculate new due date from today
        student.setDueDate(today.plusMonths(feesPeriodMonths));

        // Update fees for current period
        student.setFeesPeriodMonths(feesPeriodMonths);
        student.setFeesPaid(feesPaid);

        // Track renewal history
        student.setRenewalCount(student.getRenewalCount() + 1);
        student.setTotalFeesCollected(student.getTotalFeesCollected() + feesPaid);

        Student savedStudent = studentRepository.save(student);

        // Send renewal invoice email
        emailService.sendInvoiceEmail(savedStudent);

        return savedStudent;
    }
    // 2. Get a list of all students (for the Admin dashboard)
    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    // 3. Get a specific student by their ID (needed when Admin clicks "Edit")
    public Student getStudentById(Long id) {
        return studentRepository.findById(id).orElse(null);
    }

    // 4. Update an existing student's details
    public Student updateStudent(Student student) {
        return studentRepository.save(student); // .save() updates if the ID already exists
    }

    // 5. Delete / Remove a student entirely
    public void deleteStudent(Long id) {
        studentRepository.deleteById(id);
    }

    // 6. Find all students whose due date has passed (for the Dues tab)
    public List<Student> getStudentsWithDues() {
        return studentRepository.findByDueDateBefore(LocalDate.now());
    }
}