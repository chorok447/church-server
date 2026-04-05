package com.church.dto;

import com.church.model.Notice;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class NoticeResponse {
    private Long id;
    private String title;
    private LocalDate date;
    private String content;
    private Integer viewCount;
    private Boolean pinned;
    private String authorName;
    private Long authorId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<AttachmentResponse> attachments;

    public static NoticeResponse from(Notice notice) {
        return NoticeResponse.builder()
                .id(notice.getId())
                .title(notice.getTitle())
                .date(notice.getDate())
                .content(notice.getContent())
                .viewCount(notice.getViewCount())
                .pinned(notice.getPinned())
                .authorName(notice.getAuthor() != null ? notice.getAuthor().getName() : null)
                .authorId(notice.getAuthor() != null ? notice.getAuthor().getId() : null)
                .createdAt(notice.getCreatedAt())
                .updatedAt(notice.getUpdatedAt())
                .build();
    }

    public static NoticeResponse from(Notice notice, List<AttachmentResponse> attachments) {
        return NoticeResponse.builder()
                .id(notice.getId())
                .title(notice.getTitle())
                .date(notice.getDate())
                .content(notice.getContent())
                .viewCount(notice.getViewCount())
                .pinned(notice.getPinned())
                .authorName(notice.getAuthor() != null ? notice.getAuthor().getName() : null)
                .authorId(notice.getAuthor() != null ? notice.getAuthor().getId() : null)
                .createdAt(notice.getCreatedAt())
                .updatedAt(notice.getUpdatedAt())
                .attachments(attachments)
                .build();
    }
}
