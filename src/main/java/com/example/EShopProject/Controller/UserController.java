package com.example.EShopProject.Controller;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import com.example.EShopProject.Repository.OrderRepository;
import com.example.EShopProject.Repository.ReviewRepository;
import com.example.EShopProject.Repository.SuggestionRepository;
import com.example.EShopProject.Repository.UserRepository;
import com.example.EShopProject.Service.CartService;
import com.example.EShopProject.Service.EmailService;
import com.example.EShopProject.Service.PasswordResetService;
import com.example.EShopProject.Service.ProductService;
import com.example.EShopProject.entity.Order;
import com.example.EShopProject.entity.Product;
import com.example.EShopProject.entity.Review;
import com.example.EShopProject.entity.Suggestion;
import com.example.EShopProject.entity.User;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
public class UserController {
	@Autowired private OrderRepository orderRepository;
    @Autowired private UserRepository repo;
    @Autowired private ProductService service;
    @Autowired private CartService cartService;
    @Autowired
    private SuggestionRepository suggestionRepository;
    @Autowired
    private ReviewRepository reviewRepository;
    @Autowired
    private EmailService emailService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private PasswordResetService passwordResetService;

    @ModelAttribute
    public void commonData(Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser != null) {
            int cartCount = cartService.getCartItems(currentUser).size();
            model.addAttribute("cartCount", cartCount);
        } else {
            model.addAttribute("cartCount", 0);
        }
    }

    @GetMapping("/admin/")
    public String showAdminHomePage(Model m) {
        Collection<Product> plist = service.getAllProduct();
        m.addAttribute("productlist", plist);
        return "admin/index";
    }
    
    @GetMapping("/")
    public String showHomePage(Model m) {
        Collection<Product> plist = service.getAllProduct();
        m.addAttribute("productlist", plist);
        return "index";
    }

    @GetMapping("/registration")
    public String showRegistrationForm(Model m) {
        User u = new User();
        m.addAttribute("user", u);
        return "register";
    }

//    @PostMapping("/registeruser")
//    public String getRegisterData(@ModelAttribute @Valid User user, BindingResult result, Model m) {
//        user.setRole("user");
//
//        if (result.hasErrors()) {
//            return "register";
//        }
//
//        repo.save(user);
//        m.addAttribute("msg", "User Registration done Successfully");
//        return "login";
//    }
    

//@PostMapping("/registeruser")
//public String getRegisterData(@ModelAttribute @Valid User user, BindingResult result, Model m) {
//    user.setRole("user");
//
//    if (result.hasErrors()) {
//        return "register";
//    }
//
//    user.setPassword(passwordEncoder.encode(user.getPassword())); // Encrypt password
//    repo.save(user);
//
//    m.addAttribute("msg", "User Registration done Successfully");
//    return "login";
//}

    @PostMapping("/registeruser")
    public String getRegisterData(@ModelAttribute @Valid User user, BindingResult result, Model m) {
        // Check if username already exists
        if (repo.findByUsername(user.getUsername()) != null) {
            m.addAttribute("user", user); // preserve form data
            m.addAttribute("usernameExists", true); // trigger error message in HTML
            return "registration"; // show registration page again
        }

        // ✅ Handle validation errors
        if (result.hasErrors()) {
            return "registration"; // not "register" unless your template is actually named that
        }

        // ✅ Set role and encode password
        user.setRole("user");
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        repo.save(user);

        m.addAttribute("msg", "User Registration done Successfully");
        return "login";
    }


//    @PostMapping("/getlogin")
//    public String getUserLogin(@ModelAttribute User user, Model model, HttpSession session) {
//        if (user.getUsername() == null || user.getUsername().isEmpty() ||
//            user.getPassword() == null || user.getPassword().isEmpty()) {
//            return "fail";
//        }
//
//        User u = repo.findByUsernameAndPassword(user.getUsername(), user.getPassword());
//        if (u == null) return "fail";
//
//        session.setAttribute("currentUser", u);
//
//        if ("admin".equalsIgnoreCase(u.getRole())) {
//            return "redirect:/admin/";
//        } else if ("user".equalsIgnoreCase(u.getRole())) {
//            return "redirect:/";
//        }
//
//        return "fail";
//    }
    
    @GetMapping("/login")
    public String showLoginPage() {
        return "login"; 
    }

    @GetMapping("/products/{id}")
    public String showProductDetails(@PathVariable Integer id, Model m) {
        Product p = service.getSingleProduct(id);
        m.addAttribute("product", p);
        return "product_detail";
    }
    
    @GetMapping("/my-orders")
    public String viewUserOrders(Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) return "redirect:/login";

        List<Order> orders = orderRepository.findByUser(currentUser);
        model.addAttribute("orders", orders);
        model.addAttribute("cartCount", cartService.getCartItems(currentUser).size());
        return "order_history"; // your Thymeleaf HTML file
    }

    @GetMapping("/logout")
    public String logout(HttpSession session, Model model) {
        session.invalidate();
        model.addAttribute("message", "You have been logged out successfully");
        return "logout";
    }
    
    @GetMapping("/about")
    public String aboutPage(Model model) {
        List<Review> topReviews = reviewRepository.findTop5ByOrderBySubmittedAtDesc();
        model.addAttribute("topReviews", topReviews);
        return "about";
    }
    
 // Show suggestion form
    @GetMapping("/suggestion")
    public String showSuggestionForm(Model model, HttpSession session) {
        if (session.getAttribute("currentUser") == null) return "redirect:/login";
        model.addAttribute("suggestion", new Suggestion());
        return "suggestion";
    }

    // Handle suggestion form submission
//    @PostMapping("/suggestion")
//    public String handleSuggestionSubmission(@ModelAttribute("suggestion") Suggestion suggestion,
//                                             HttpSession session) {
//        User currentUser = (User) session.getAttribute("currentUser");
//        if (currentUser == null) return "redirect:/login";
//
//        suggestion.setEmail(currentUser.getEmail());
//        suggestion.setSubmittedAt(LocalDateTime.now());
//
//        suggestionRepository.save(suggestion);
//        return "redirect:/thank-you";
//    }
    @PostMapping("/suggestion")
    public String handleSuggestionSubmission(@ModelAttribute("suggestion") Suggestion suggestion,
                                             HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) return "redirect:/login";

        suggestion.setEmail(currentUser.getEmail());
        suggestion.setSubmittedAt(LocalDateTime.now());

        // Save to DB
        suggestionRepository.save(suggestion);

        try {
            emailService.sendEmail(
                currentUser.getEmail(),
                "Thank You for Your Suggestion",
                "Dear " + suggestion.getFullName() + ",\n\nThank you for your suggestion on EShop. Your feedback is under process and helps us improve the platform.\n\n- EShop Team"
            );
        } catch (Exception e) {
            e.printStackTrace(); 
        }

        return "redirect:/thank-you";
    }




    // Thank you page
    @GetMapping("/thank-you")
    public String showThankYouPage() {
        return "thank_you";
    }
    
    @GetMapping("/review")
    public String showReviewForm(Model model, HttpSession session) {
        if (session.getAttribute("currentUser") == null) return "redirect:/login";
        model.addAttribute("review", new Review());
        return "review";
    }
    
//    @PostMapping("/review")
//    public String handleReviewSubmit(@ModelAttribute("review") Review review,
//                                     HttpSession session) {
//        if (session.getAttribute("currentUser") == null) return "redirect:/login";
//
//        review.setSubmittedAt(LocalDateTime.now());
//        reviewRepository.save(review);
//
//        return "redirect:/thank-you";
//    }
    
    @GetMapping("/reviews")
    public String showAllReviews(Model model) {
        List<Review> allReviews = reviewRepository.findAllByOrderBySubmittedAtDesc();
        model.addAttribute("allReviews", allReviews);
        return "all_reviews";
    }

    @PostMapping("/review")
    public String handleReviewSubmit(@ModelAttribute("review") Review review,
                                     HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) return "redirect:/login";

        review.setSubmittedAt(LocalDateTime.now());
        reviewRepository.save(review);

        try {
            
            emailService.sendEmail(
                currentUser.getEmail(),
                "Thank You for Your Review",
                "Dear " + review.getFullName() + ",\n\nThank you for sharing your review on EShop. Your feedback helps us improve our platform!\n\nPlease visit again.\n\n- EShop Team"
            );
        } catch (Exception e) {
            e.printStackTrace(); // Or use logger.error("Email failed", e);
        }

        return "redirect:/thank-you";
    }

    
    @GetMapping("/fail")
    public String showLoginFailedPage() {
        return "fail"; // this will render fail.html
    }

    @GetMapping("/test-forgot-password")
    public String showTestForgotPasswordPage() {
        return "test_forgot_password";
    }

    // Forgot Password functionality
    @GetMapping("/forgot-password")
    public String showForgotPasswordForm() {
        System.out.println("Forgot password form requested");
        return "forgot_password";
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam String email, Model model) {
        System.out.println("Processing forgot password request for email: " + email);
        
        // Validate email format
        if (email == null || email.trim().isEmpty()) {
            model.addAttribute("message", "Please enter a valid email address.");
            model.addAttribute("success", false);
            return "forgot_password";
        }
        
        // Check if user exists first
        User user = repo.findByEmail(email);
        if (user == null) {
            model.addAttribute("message", "No account found with that email address. Please check your email or register a new account.");
            model.addAttribute("success", false);
            System.out.println("No user found with email: " + email);
            return "forgot_password";
        }
        
        System.out.println("User found: " + user.getUsername() + " with email: " + user.getEmail());
        
        try {
            boolean emailSent = passwordResetService.sendPasswordResetEmail(email);
            
            if (emailSent) {
                model.addAttribute("message", "Password reset link has been sent to your email address. Please check your inbox and spam folder.");
                model.addAttribute("success", true);
                System.out.println("Password reset email sent successfully to: " + email);
            } else {
                // Fallback: Show reset link in console for testing
                String resetLink = passwordResetService.getLastGeneratedResetLink();
                if (resetLink != null) {
                    System.out.println("=== EMAIL SENDING FAILED - RESET LINK FOR TESTING ===");
                    System.out.println("Reset link: " + resetLink);
                    System.out.println("Copy this link to test password reset functionality");
                    System.out.println("==================================================");
                    
                    model.addAttribute("message", "Email sending failed, but reset link generated. Check console for testing link. Please contact support for email configuration.");
                    model.addAttribute("success", false);
                } else {
                    model.addAttribute("message", "Failed to send password reset email. Please try again later or contact support.");
                    model.addAttribute("success", false);
                }
                System.out.println("Failed to send password reset email to: " + email);
            }
        } catch (Exception e) {
            System.err.println("Error processing forgot password request: " + e.getMessage());
            e.printStackTrace();
            
            // Fallback: Show reset link in console for testing
            String resetLink = passwordResetService.getLastGeneratedResetLink();
            if (resetLink != null) {
                System.out.println("=== EMAIL EXCEPTION - RESET LINK FOR TESTING ===");
                System.out.println("Reset link: " + resetLink);
                System.out.println("Copy this link to test password reset functionality");
                System.out.println("================================================");
                
                model.addAttribute("message", "Email configuration issue detected. Check console for testing link. Please contact support for email setup.");
                model.addAttribute("success", false);
            } else {
                model.addAttribute("message", "Unable to process your request at this time. Please try again later or contact support.");
                model.addAttribute("success", false);
            }
        }
        
        return "forgot_password";
    }

    @GetMapping("/reset-password")
    public String showResetPasswordForm(@RequestParam String token, Model model) {
        if (passwordResetService.validateToken(token)) {
            model.addAttribute("token", token);
            return "reset_password";
        } else {
            model.addAttribute("message", "Invalid or expired password reset link.");
            return "reset_password_error";
        }
    }

    @PostMapping("/reset-password")
    public String processResetPassword(@RequestParam String token, 
                                     @RequestParam String password, 
                                     @RequestParam String confirmPassword, 
                                     Model model) {
        
        if (!password.equals(confirmPassword)) {
            model.addAttribute("token", token);
            model.addAttribute("error", "Passwords do not match.");
            return "reset_password";
        }
        
        if (password.length() < 3) {
            model.addAttribute("token", token);
            model.addAttribute("error", "Password must be at least 3 characters long.");
            return "reset_password";
        }
        
        boolean resetSuccess = passwordResetService.resetPassword(token, password);
        
        if (resetSuccess) {
            model.addAttribute("message", "Password has been reset successfully. You can now login with your new password.");
            return "reset_password_success";
        } else {
            model.addAttribute("message", "Invalid or expired password reset link.");
            return "reset_password_error";
        }
    }

}
