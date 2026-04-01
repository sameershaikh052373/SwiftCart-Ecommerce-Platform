package com.example.EShopProject.Repository;

import com.example.EShopProject.entity.OrderItem;
import com.example.EShopProject.entity.Product;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
	
	List<OrderItem> findByProduct(Product product);
}
