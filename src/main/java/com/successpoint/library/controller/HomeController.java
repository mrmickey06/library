package com.successpoint.library.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    // This routes the main URL ("/") to your index.html page
    @GetMapping("/")
    public String home() {
        return "index";
    }
}