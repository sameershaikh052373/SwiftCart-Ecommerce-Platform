package com.example.EShopProject.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.EShopProject.entity.Category;
import com.example.EShopProject.entity.Product;




public interface ProductRepository extends JpaRepository<Product,Integer>{

	 @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
	    List<Product> searchByKeyword(@Param("keyword") String keyword);
	
	Optional<Product> findById(Integer id);
	List<Product> findByCategory(Category category);
}

