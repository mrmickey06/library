package com.successpoint.library.controller;

import java.time.YearMonth;

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

    @GetMapping("/dashboard")
    public String showDashboard(Model model) {
        model.addAttribute("students", studentService.getAllStudents());
        model.addAttribute("applicationsCount", studentService.getPendingApplications().size());
        model.addAttribute("pendingFeesCount", studentService.getPendingFeeStudents().size());
        return "admin_dashboard";
    }

    @GetMapping("/add-student")
    public String showAddStudentForm(Model model) {
        model.addAttribute("student", new Student());
        return "add_student";
    }

    @PostMapping("/save-student")
    public String saveStudent(@ModelAttribute("student") Student student,
                              @RequestParam("feeStatus") String feeStatus) {
        studentService.registerStudent(student, feeStatus);
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/edit-student/{id}")
    public String showEditStudentForm(@PathVariable Long id, Model model) {
        model.addAttribute("student", studentService.getStudentById(id));
        return "edit_student";
    }

    @PostMapping("/update-student/{id}")
    public String updateStudent(@PathVariable Long id, @ModelAttribute("student") Student formStudent) {
        studentService.updateStudentProfile(id, formStudent);
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/delete-student/{id}")
    public String deleteStudent(@PathVariable Long id) {
        studentService.deleteStudent(id);
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/dues")
    public String showDues(Model model) {
        model.addAttribute("students", studentService.getStudentsWithDues());
        model.addAttribute("applicationsCount", studentService.getPendingApplications().size());
        model.addAttribute("pendingFeesCount", studentService.getPendingFeeStudents().size());
        return "dues_dashboard";
    }

    @GetMapping("/applications")
    public String showApplications(Model model) {
        model.addAttribute("students", studentService.getPendingApplications());
        model.addAttribute("applicationsCount", studentService.getPendingApplications().size());
        model.addAttribute("pendingFeesCount", studentService.getPendingFeeStudents().size());
        return "applications_dashboard";
    }

    @GetMapping("/applications/{id}")
    public String showApplicationReview(@PathVariable Long id, Model model) {
        model.addAttribute("student", studentService.getStudentById(id));
        return "review_application";
    }

    @PostMapping("/applications/{id}/approve")
    public String approveApplication(@PathVariable Long id,
                                     @ModelAttribute("student") Student formStudent,
                                     @RequestParam("feeStatus") String feeStatus) {
        studentService.approveApplication(id, formStudent, feeStatus);
        return "redirect:/admin/applications";
    }

    @PostMapping("/applications/{id}/reject")
    public String rejectApplication(@PathVariable Long id, @ModelAttribute("student") Student formStudent) {
        studentService.rejectApplication(id, formStudent);
        return "redirect:/admin/applications";
    }

    @GetMapping("/pending-fees")
    public String showPendingFees(Model model) {
        model.addAttribute("students", studentService.getPendingFeeStudents());
        model.addAttribute("applicationsCount", studentService.getPendingApplications().size());
        model.addAttribute("pendingFeesCount", studentService.getPendingFeeStudents().size());
        return "pending_fees_dashboard";
    }

    @PostMapping("/pending-fees/{id}/clear")
    public String clearPendingFees(@PathVariable Long id) {
        studentService.clearPendingFees(id);
        return "redirect:/admin/pending-fees";
    }

    @GetMapping("/total-amount")
    public String showTotalAmount(@RequestParam(value = "month", required = false) String month,
                                  Model model) {
        YearMonth selectedMonth;
        try {
            selectedMonth = (month == null || month.isBlank()) ? YearMonth.now() : YearMonth.parse(month);
        } catch (Exception e) {
            selectedMonth = YearMonth.now();
        }

        model.addAttribute("selectedMonth", selectedMonth.toString());
        model.addAttribute("students", studentService.getMonthlyEarningStudents(selectedMonth));
        model.addAttribute("totalAmount", studentService.getMonthlyEarningsTotal(selectedMonth));
        model.addAttribute("applicationsCount", studentService.getPendingApplications().size());
        model.addAttribute("pendingFeesCount", studentService.getPendingFeeStudents().size());
        return "total_amount_dashboard";
    }

    @GetMapping("/renew-student/{id}")
    public String showRenewStudentForm(@PathVariable Long id, Model model) {
        model.addAttribute("student", studentService.getStudentById(id));
        return "renew_student";
    }

    @PostMapping("/process-renewal/{id}")
    public String processRenewal(@PathVariable Long id,
                                 @RequestParam("feesPeriodMonths") int feesPeriodMonths,
                                 @RequestParam("feesPaid") double feesPaid) {
        studentService.renewStudent(id, feesPeriodMonths, feesPaid);
        return "redirect:/admin/dues";
    }

    @GetMapping("/download-excel")
    public org.springframework.http.ResponseEntity<org.springframework.core.io.InputStreamResource> downloadExcel(
            @RequestParam("startDate") java.time.LocalDate startDate,
            @RequestParam("endDate") java.time.LocalDate endDate) throws java.io.IOException {
        java.util.List<Student> students = studentRepository.findByJoiningDateBetween(startDate, endDate);
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
