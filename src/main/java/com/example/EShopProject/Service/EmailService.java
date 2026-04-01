package com.example.EShopProject.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendEmail(String toEmail, String subject, String message) throws Exception {
        try {
            System.out.println("=== Email Service Debug ===");
            System.out.println("Attempting to send email to: " + toEmail);
            System.out.println("Subject: " + subject);
            System.out.println("Message length: " + message.length() + " characters");
            
            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setTo(toEmail);
            mail.setSubject(subject);
            mail.setText(message);
            
            System.out.println("Mail object created successfully");
            System.out.println("Attempting to send via JavaMailSender...");
            
            mailSender.send(mail);
            System.out.println("✅ Email sent successfully to: " + toEmail);
        } catch (Exception e) {
            System.err.println("❌ Error sending email to " + toEmail);
            System.err.println("Error type: " + e.getClass().getSimpleName());
            System.err.println("Error message: " + e.getMessage());
            
            
            String errorMsg = e.getMessage();
            if (errorMsg != null) {
                if (errorMsg.contains("535") || errorMsg.contains("Authentication")) {
                    System.err.println("🔐 This appears to be an authentication error. Please check your Gmail app password.");
                } else if (errorMsg.contains("535-5.7.8")) {
                    System.err.println("🔐 Gmail authentication failed. The app password may be incorrect or expired.");
                } else if (errorMsg.contains("535-5.7.9")) {
                    System.err.println("🔐 Gmail requires app-specific password. Please generate a new app password.");
                } else if (errorMsg.contains("javax.mail.AuthenticationFailedException")) {
                    System.err.println("🔐 Authentication failed. Check your Gmail username and app password.");
                } else if (errorMsg.contains("javax.mail.MessagingException")) {
                    System.err.println("📧 Messaging exception. Check SMTP configuration and network connection.");
                } else if (errorMsg.contains("Connection")) {
                    System.err.println("🌐 Connection issue. Check internet connection and Gmail SMTP settings.");
                } else if (errorMsg.contains("SSL") || errorMsg.contains("TLS")) {
                    System.err.println("🔒 SSL/TLS issue. Check STARTTLS configuration.");
                }
            }
            
            e.printStackTrace();
            throw e;
        }
    }
}
