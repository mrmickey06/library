package com.successpoint.library.config;

import java.io.IOException;

import org.springframework.stereotype.Component;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Component
public class AdminAuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
            throws IOException, ServletException {
        
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        String uri = req.getRequestURI();

        // Check if the user is trying to access ANY page inside the /admin/ area
        if (uri.startsWith("/admin/")) {
            HttpSession session = req.getSession(false); // false means don't create a new session if one doesn't exist
            
            // If the server restarted (session is null) OR they aren't logged in, redirect to Home!
            if (session == null || session.getAttribute("adminId") == null) {
                res.sendRedirect("/");
                return; // Stop them right here
            }
        }
        
        // If they are logged in, let them pass through normally
        chain.doFilter(request, response);
    }
}