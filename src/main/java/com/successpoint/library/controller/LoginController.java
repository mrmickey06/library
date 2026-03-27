package com.successpoint.library.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.successpoint.library.entity.Admin;
import com.successpoint.library.repository.AdminRepository;

import jakarta.servlet.http.HttpSession;

@Controller
public class LoginController {

    @Autowired
    private AdminRepository adminRepository;

    // 1. Show the Admin Login Page
    @GetMapping("/admin-login")
    public String showAdminLogin() {
        return "admin_login";
    }

    // 2. Process the Login Form
    @PostMapping("/admin-login")
    public String processAdminLogin(@RequestParam("mobileNumber") String mobileNumber,
                                    @RequestParam("password") String password,
                                    HttpSession session,
                                    Model model) {
        
        Optional<Admin> adminOpt = adminRepository.findByMobileNumber(mobileNumber);
        
        if (adminOpt.isPresent() && adminOpt.get().getPassword().equals(password)) {
            session.setAttribute("adminId", adminOpt.get().getId());
            return "redirect:/admin/dashboard";
        } else {
            model.addAttribute("error", "Invalid Mobile Number or Password!");
            return "admin_login";
        }
    }

    // 3. Admin Logout - Now redirects to Index/Home Page
    @GetMapping("/admin-logout")
    public String adminLogout(HttpSession session) {
        session.invalidate(); 
        return "redirect:/"; 
    }
}