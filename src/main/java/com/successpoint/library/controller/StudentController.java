package com.successpoint.library.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.successpoint.library.entity.Student;
import com.successpoint.library.repository.StudentRepository;

import jakarta.servlet.http.HttpSession;

@Controller
public class StudentController {

    @Autowired
    private StudentRepository studentRepository;

    // 1. Show the Student Login Page
    @GetMapping("/student-login")
    public String showStudentLogin() {
        return "student_login";
    }

    // 2. Process the Student Login
    @PostMapping("/student-login")
    public String processStudentLogin(@RequestParam("mobileNumber") String mobileNumber,
                                      @RequestParam("password") String password,
                                      HttpSession session,
                                      Model model) {
        
        Optional<Student> studentOpt = studentRepository.findByMobileNumber(mobileNumber);
        
        // Check if student exists and password matches (default is SP@123)
        if (studentOpt.isPresent() && studentOpt.get().getPassword().equals(password)) {
            // Save student ID in session
            session.setAttribute("studentId", studentOpt.get().getId());
            return "redirect:/student/dashboard";
        } else {
            model.addAttribute("error", "Invalid Mobile Number or Password!");
            return "student_login";
        }
    }

    // 3. The Blank Student Dashboard (For future features)
    @GetMapping("/student/dashboard")
    public String showStudentDashboard(HttpSession session, Model model) {
        // Security check: Make sure a student is actually logged in
        Long studentId = (Long) session.getAttribute("studentId");
        if (studentId == null) {
            return "redirect:/student-login";
        }
        
        // Fetch the logged-in student's data to display their name on the blank portal
        Optional<Student> studentOpt = studentRepository.findById(studentId);
        if (studentOpt.isPresent()) {
            model.addAttribute("student", studentOpt.get());
            return "student_dashboard"; // This will be our blank Thymeleaf page
        }
        
        return "redirect:/student-login";
    }

    // 4. Student Logout
    @GetMapping("/student-logout")
    public String studentLogout(HttpSession session) {
        session.invalidate();
        return "redirect:/student-login";
    }
}