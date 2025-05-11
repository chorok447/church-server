package com.church.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "sermons")
public class Sermon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String date;
    private String videoUrl;
}