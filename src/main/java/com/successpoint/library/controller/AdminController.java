package com.successpoint.library.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.successpoint.library.entity.Student;
import com.successpoint.library.service.StudentService;

@Controller
@RequestMapping("/admin")
public class AdminController {
    @Autowired
    private com.successpoint.library.repository.StudentRepository studentRepository;

    @Autowired
    private com.successpoint.library.service.ExcelService excelService;

    @Autowired
    private StudentService studentService;

    // 1. Show the main Admin Dashboard with the list of all students
    @GetMapping("/dashboard")
    public String showDashboard(Model model) {
        // We fetch all students and add them to the "model" so the HTML page can see them
        model.addAttribute("students", studentService.getAllStudents());
        return "admin_dashboard"; // This tells Spring to look for admin_dashboard.html
    }

    // 2. Show the form to add a new student
    @GetMapping("/add-student")
    public String showAddStudentForm(Model model) {
        // We send a blank Student object to the HTML form to be filled out
        model.addAttribute("student", new Student());
        return "add_student"; 
    }

    // 3. Save the new student and refresh the dashboard
    @PostMapping("/save-student")
    public String saveStudent(@ModelAttribute("student") Student student) {
        studentService.registerStudent(student);
        // "redirect:" forces the browser to reload the dashboard page to show the new data
        return "redirect:/admin/dashboard";
    }

    // 4. Show the form to edit an existing student
    @GetMapping("/edit-student/{id}")
    public String showEditStudentForm(@PathVariable Long id, Model model) {
        model.addAttribute("student", studentService.getStudentById(id));
        return "edit_student";
    }

    // 5. Update the student PROFILE in the database (name, mobile, email only)
    @PostMapping("/update-student/{id}")
    public String updateStudent(@PathVariable Long id, @ModelAttribute("student") Student formStudent) {
        Student existingStudent = studentService.getStudentById(id);
        if (existingStudent == null) {
            return "redirect:/admin/dashboard";
        }

        // Only update profile fields - NOT fees or dates
        existingStudent.setName(formStudent.getName());
        existingStudent.setMobileNumber(formStudent.getMobileNumber());
        existingStudent.setEmail(formStudent.getEmail());

        studentService.updateStudent(existingStudent);
        return "redirect:/admin/dashboard";
    }

    // 6. Delete a student entirely
    @GetMapping("/delete-student/{id}")
    public String deleteStudent(@PathVariable Long id) {
        studentService.deleteStudent(id);
        return "redirect:/admin/dashboard";
    }

    // 7. Show the Dues tab (only students whose due date has passed)
    @GetMapping("/dues")
    public String showDues(Model model) {
        model.addAttribute("students", studentService.getStudentsWithDues());
        return "dues_dashboard";
    }

    // 8. Show the RENEWAL form for a student
    @GetMapping("/renew-student/{id}")
    public String showRenewStudentForm(@PathVariable Long id, Model model) {
        model.addAttribute("student", studentService.getStudentById(id));
        return "renew_student";
    }

    // 9. Process the RENEWAL - updates fees, dates, and sends invoice
    @PostMapping("/process-renewal/{id}")
    public String processRenewal(@PathVariable Long id,
                                  @RequestParam("feesPeriodMonths") int feesPeriodMonths,
                                  @RequestParam("feesPaid") double feesPaid) {
        studentService.renewStudent(id, feesPeriodMonths, feesPaid);
        return "redirect:/admin/dues";
    }
    // Download Excel Data between two dates
    @GetMapping("/download-excel")
    public org.springframework.http.ResponseEntity<org.springframework.core.io.InputStreamResource> downloadExcel(
            @RequestParam("startDate") java.time.LocalDate startDate,
            @RequestParam("endDate") java.time.LocalDate endDate) throws java.io.IOException {
        
        // Find students between the dates
        java.util.List<Student> students = studentRepository.findByJoiningDateBetween(startDate, endDate);
        
        // Generate Excel file
        java.io.ByteArrayInputStream in = excelService.generateExcel(students);
        
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=SuccessPoint_Students.xlsx");
        
        return org.springframework.http.ResponseEntity
                .ok()
                .headers(headers)
                .contentType(org.springframework.http.MediaType.parseMediaType("application/vnd.ms-excel"))
                .body(new org.springframework.core.io.InputStreamResource(in));
    }
}