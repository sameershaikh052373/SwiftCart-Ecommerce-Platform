package com.example.EShopProject.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
public class Suggestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fullName;

    private String email;

    private String type; 

    @Column(length = 2000)
    private String message;

    private LocalDateTime submittedAt;
}
