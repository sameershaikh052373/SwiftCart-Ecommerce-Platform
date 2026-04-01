package com.example.EShopProject.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService userDetailsService;
    @Autowired
    private LoginSuccessHandler loginSuccessHandler;
    
    @Autowired
    private LoginFailureHandler loginFailureHandler;
    
    @Autowired
    private CustomAccessDeniedHandler accessDeniedHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
            		
                // ✅ Allow static resources
                .requestMatchers("/images/**", "/css/**", "/js/**", "/webjars/**", "/fonts/**", "/assets/**").permitAll()

                // ✅ Allow login and error pages
                .requestMatchers("/login", "/getlogin", "/error","/fail").permitAll()

                // ✅ Public pages
                .requestMatchers("/", "/home", "/registeruser", "/registration",
                                 "/products/**", "/product/search", "/category/**", "/products-by-category",
                                 "/about", "/reviews", "/thank-you", "/test-forgot-password").permitAll()

                // ✅ Forgot password endpoints
                .requestMatchers("/forgot-password", "/reset-password").permitAll()

                // ✅ User-only routes
                .requestMatchers("/cart/**", "/address", "/proceed-to-payment", "/payment-success",
                                 "/order-success", "/my-orders", "/review", "/suggestion").hasAuthority("user")
                
                .requestMatchers("/admin/hash-all-passwords").permitAll()

                // ✅ Admin-only routes
                .requestMatchers("/admin/**").hasAuthority("admin")

                // ✅ All other pages require authentication
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
            	    .loginPage("/login")
            	    .loginProcessingUrl("/getlogin")
            	    .successHandler(loginSuccessHandler)   // already set
            	    .failureHandler(loginFailureHandler)   // ✅ this is new
            	    .permitAll()
            	)


            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
            )
        
        .exceptionHandling(ex -> ex
                .accessDeniedHandler(accessDeniedHandler) // ✅ use our handler
            );

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
