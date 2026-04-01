package com.example.EShopProject.entity;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class OrderForm {

	@NotBlank(message = "Full name is required")
    private String fullName;

	@NotBlank(message = "Address is required")
    private String addressLine;

	@NotBlank(message = "Area is required")
    private String area;

	@NotBlank(message = "City is required")
    private String city;

	@NotBlank(message = "State is required")
    private String state;

	@NotBlank(message = "Pincode is required")
    @Pattern(regexp = "\\d{6}", message = "Pincode must be 6 digits")
    private String pinCode;
    
}