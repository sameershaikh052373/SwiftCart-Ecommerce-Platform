package com.example.EShopProject.Repository;

import com.example.EShopProject.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    
    Optional<PasswordResetToken> findByToken(String token);
    
    Optional<PasswordResetToken> findByUser_IdAndUsedFalse(Integer userId);
    
    List<PasswordResetToken> findAllByUser_Id(Integer userId);

} 