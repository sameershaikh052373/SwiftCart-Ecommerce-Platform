package com.example.EShopProject;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.example.EShopProject.Repository.UserRepository;
import com.example.EShopProject.entity.User;

@SpringBootApplication
public class EShopProjectApplication  {
	public static void main(String[] args) {
		SpringApplication.run(EShopProjectApplication.class, args);
	   
		System.out.println("The application is running");
		// admin -- role
		// password- admin123
		// user-Sameer
		// password- sameer31
	}
}
