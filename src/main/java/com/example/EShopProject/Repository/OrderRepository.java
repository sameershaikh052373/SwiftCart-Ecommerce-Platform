package com.example.EShopProject.Repository;

import com.example.EShopProject.entity.Order;
import com.example.EShopProject.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUser(User user); 
}
