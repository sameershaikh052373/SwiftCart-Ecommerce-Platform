package com.example.EShopProject.entity;

import com.example.EShopProject.Validation.Mobile;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Entity
@Data
public class User {
	
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private int id;
	@NotBlank(message = "Username is required")
    @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
	private String username;
	@NotBlank(message = "Password is required")
    @Size(min = 3, message = "Password must be at least 3 characters long")
	private String password;
	@NotBlank(message = "Email is required")
    @Email(message = "email not Valid")
	private String email;
	@Mobile
	private String phone;
	private String role; 
}
