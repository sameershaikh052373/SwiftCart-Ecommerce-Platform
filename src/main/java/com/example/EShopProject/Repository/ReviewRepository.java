package com.example.EShopProject.Repository;

import com.example.EShopProject.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findTop5ByOrderBySubmittedAtDesc();  
    List<Review> findAllByOrderBySubmittedAtDesc();   
}
