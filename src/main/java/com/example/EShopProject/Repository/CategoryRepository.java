package com.example.EShopProject.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.EShopProject.entity.Category;

public interface CategoryRepository extends JpaRepository<Category,Integer>{

}