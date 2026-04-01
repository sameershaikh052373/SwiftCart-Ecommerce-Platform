package com.example.EShopProject.config;

import com.example.EShopProject.Repository.UserRepository;
import com.example.EShopProject.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User u = userRepository.findByUsername(username);
        if (u == null) {
            throw new UsernameNotFoundException("User not found");
        }
        return new CustomUserDetails(u);
    }
}
