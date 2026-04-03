package com.church.dto;

import com.church.model.Attachment;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AttachmentResponse {
    private Long id;
    private String originalFilename;
    private String storedFilename;
    private Long fileSize;
    private String contentType;
    private String downloadUrl;

    public static AttachmentResponse from(Attachment attachment) {
        return AttachmentResponse.builder()
                .id(attachment.getId())
                .originalFilename(attachment.getOriginalFilename())
                .storedFilename(attachment.getStoredFilename())
                .fileSize(attachment.getFileSize())
                .contentType(attachment.getContentType())
                .downloadUrl("/api/files/download/" + attachment.getStoredFilename())
                .build();
    }
}
