package com.example.EShopProject.Service;

import com.example.EShopProject.Repository.PasswordResetTokenRepository;
import com.example.EShopProject.Repository.UserRepository;
import com.example.EShopProject.entity.PasswordResetToken;
import com.example.EShopProject.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PasswordResetService {

    @Autowired
    private PasswordResetTokenRepository tokenRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    
    private String lastGeneratedResetLink = null;

    public boolean sendPasswordResetEmail(String email) {
        System.out.println("Attempting to send password reset email to: " + email);
        User user = userRepository.findByEmail(email);
        if (user == null) {
            System.out.println("No user found with email: " + email);
            return false;
        }
        System.out.println("User found: " + user.getUsername());

        
        List<PasswordResetToken> oldTokens = tokenRepository.findAllByUser_Id(user.getId());
        if (!oldTokens.isEmpty()) {
            tokenRepository.deleteAll(oldTokens);
            System.out.println("Deleted old tokens for user: " + user.getUsername());
        }


        // Create new token
        PasswordResetToken token = new PasswordResetToken();
        token.setToken(UUID.randomUUID().toString());
        token.setUser(user);
        token.setExpiryDate(LocalDateTime.now().plusHours(24)); // Token expires in 24 hours
        token.setUsed(false);
        
        tokenRepository.save(token);
        System.out.println("Password reset token created: " + token.getToken());

        // Send email
        String resetLink = "http://localhost:8080/reset-password?token=" + token.getToken();
        lastGeneratedResetLink = resetLink; // Store for testing
        
        String emailContent = String.format(
            "Hello %s,\n\n" +
            "You have requested to reset your password for your EShop account.\n\n" +
            "Please click the following link to reset your password:\n%s\n\n" +
            "This link will expire in 24 hours.\n\n" +
            "If you did not request this password reset, please ignore this email.\n\n" +
            "Best regards,\nEShop Team",
            user.getUsername(),
            resetLink
        );

//        try {
//            emailService.sendEmail(email, "Password Reset Request - EShop", emailContent);
//            System.out.println("Password reset email sent successfully to: " + email);
//            return true;
//        } catch (Exception e) {
//            System.err.println("Error sending password reset email: " + e.getMessage());
//            e.printStackTrace();
//            // Delete the token since email failed
//            tokenRepository.delete(token);
//            return false;
//        }
        
        try {
            emailService.sendEmail(email, "Password Reset Request - EShop", emailContent);
            System.out.println("Password reset email sent successfully to: " + email);
            return true;
        } catch (Exception e) {
            System.err.println("Error sending password reset email: " + e.getMessage());
            e.printStackTrace();

            // ⚠️ Commenting this to keep token alive for manual testing
            // tokenRepository.delete(token);

            // ✅ Print the reset link so we can still test manually
            System.out.println("=== EMAIL EXCEPTION - RESET LINK FOR TESTING ===");
            System.out.println("Reset link: " + lastGeneratedResetLink);
            System.out.println("Copy this link to test password reset functionality");
            System.out.println("================================================");

            return false;
        }

    }

    public String getLastGeneratedResetLink() {
        return lastGeneratedResetLink;
    }

    public boolean validateToken(String token) {
        Optional<PasswordResetToken> tokenOpt = tokenRepository.findByToken(token);
        if (tokenOpt.isPresent()) {
            PasswordResetToken resetToken = tokenOpt.get();
            return !resetToken.isExpired() && !resetToken.isUsed();
        }
        return false;
    }

    public boolean resetPassword(String token, String newPassword) {
        Optional<PasswordResetToken> tokenOpt = tokenRepository.findByToken(token);
        if (tokenOpt.isPresent()) {
            PasswordResetToken resetToken = tokenOpt.get();
            
            if (resetToken.isExpired() || resetToken.isUsed()) {
                return false;
            }

            // Update user password
            User user = resetToken.getUser();
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);

            // Mark token as used
            resetToken.setUsed(true);
            tokenRepository.save(resetToken);

            return true;
        }
        return false;
    }

    public User getUserFromToken(String token) {
        Optional<PasswordResetToken> tokenOpt = tokenRepository.findByToken(token);
        return tokenOpt.map(PasswordResetToken::getUser).orElse(null);
    }
} 