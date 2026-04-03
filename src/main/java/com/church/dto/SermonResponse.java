package com.church.dto;

import com.church.model.Sermon;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class SermonResponse {
    private Long id;
    private String title;
    private LocalDate date;
    private String videoUrl;
    private String description;
    private String preacher;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static SermonResponse from(Sermon sermon) {
        return SermonResponse.builder()
                .id(sermon.getId())
                .title(sermon.getTitle())
                .date(sermon.getDate())
                .videoUrl(sermon.getVideoUrl())
                .description(sermon.getDescription())
                .preacher(sermon.getPreacher())
                .createdAt(sermon.getCreatedAt())
                .updatedAt(sermon.getUpdatedAt())
                .build();
    }
}
