package com.example.EShopProject.Validation;


import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class MobileValidator implements ConstraintValidator<Mobile,String>{

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		
		boolean b = value.length()==10 && value.matches("[0-9]+");
		return b;
	}

	

}

