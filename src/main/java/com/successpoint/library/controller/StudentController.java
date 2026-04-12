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
import com.successpoint.library.service.StudentService;

import jakarta.servlet.http.HttpSession;

@Controller
public class StudentController {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private StudentService studentService;

    @GetMapping("/student-login")
    public String showStudentLogin() {
        return "student_login";
    }

    @GetMapping("/student-registration")
    public String showStudentRegistrationForm(Model model) {
        model.addAttribute("student", new Student());
        return "student_registration";
    }

    @PostMapping("/student-registration")
    public String submitStudentRegistration(@org.springframework.web.bind.annotation.ModelAttribute("student") Student student,
                                            Model model) {
        Optional<Student> existingStudent = studentRepository.findByMobileNumber(student.getMobileNumber());
        if (existingStudent.isPresent()) {
            model.addAttribute("student", student);
            model.addAttribute("error", "This mobile number is already used in the system.");
            return "student_registration";
        }

        studentService.submitApplication(student);
        model.addAttribute("student", new Student());
        model.addAttribute("success", "Your registration request has been sent to the admin for approval.");
        return "student_registration";
    }

    @PostMapping("/student-login")
    public String processStudentLogin(@RequestParam("mobileNumber") String mobileNumber,
                                      @RequestParam("password") String password,
                                      HttpSession session,
                                      Model model) {
        Optional<Student> studentOpt = studentRepository.findByMobileNumber(mobileNumber);

        if (studentOpt.isPresent()
                && studentService.canStudentLogin(studentOpt.get())
                && studentOpt.get().getPassword().equals(password)) {
            session.setAttribute("studentId", studentOpt.get().getId());
            return "redirect:/student/dashboard";
        }

        model.addAttribute("error", "Only approved students can login. Please check your mobile number or wait for admin approval.");
        return "student_login";
    }

    @GetMapping("/student/dashboard")
    public String showStudentDashboard(HttpSession session, Model model) {
        Long studentId = (Long) session.getAttribute("studentId");
        if (studentId == null) {
            return "redirect:/student-login";
        }

        Optional<Student> studentOpt = studentRepository.findById(studentId);
        if (studentOpt.isPresent() && studentService.canStudentLogin(studentOpt.get())) {
            model.addAttribute("student", studentOpt.get());
            return "student_dashboard";
        }

        return "redirect:/student-login";
    }

    @GetMapping("/student-logout")
    public String studentLogout(HttpSession session) {
        session.invalidate();
        return "redirect:/student-login";
    }
}
