package com.example.EShopProject.config;

import com.example.EShopProject.entity.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        // Get the logged-in user
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.getUserEntity();

        // Set it in session
        HttpSession session = request.getSession();
        session.setAttribute("currentUser", user);

        // Redirect based on role
        String role = user.getRole();
        if ("admin".equalsIgnoreCase(role)) {
            response.sendRedirect("/admin/home");
        } else {
            response.sendRedirect("/");
        }
    }
}
