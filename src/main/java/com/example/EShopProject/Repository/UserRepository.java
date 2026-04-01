package com.example.EShopProject.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.EShopProject.entity.User;


public interface UserRepository extends JpaRepository<User, Integer> 
{
	User findByUsernameAndPassword(String username,String password);
	User findByUsername(String username);
	User findByEmail(String email);
}

