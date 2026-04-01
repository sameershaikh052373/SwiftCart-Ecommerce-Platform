package com.example.EShopProject.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException)
            throws IOException, ServletException {

        // Get the current logged-in user's role
        HttpSession session = request.getSession(false);
        String redirectUrl = "/login";

        if (session != null) {
            Object currentUser = session.getAttribute("currentUser");
            if (currentUser != null) {
                // You can also cast to User and inspect role
                // For simplicity, redirect users based on path
                if (request.getRequestURI().startsWith("/admin")) {
                    redirectUrl = "/";
                } else {
                    redirectUrl = "/admin/home";
                }
            }
        }

        response.sendRedirect(redirectUrl);
    }
}
