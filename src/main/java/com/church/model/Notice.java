package com.church.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "notices")
public class Notice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String date;
    private String content;
}