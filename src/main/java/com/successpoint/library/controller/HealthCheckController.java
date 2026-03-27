package com.successpoint.library.controller; // Change this to your actual package name

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthCheckController {

    @GetMapping("/ping")
    public String ping() {
        // This is the endpoint UptimeRobot will hit every 10 minutes
        return "Success Point Library System is Online";
    }
}