package com.church.service;

import com.church.dto.AttachmentResponse;
import com.church.exception.ResourceNotFoundException;
import com.church.model.Attachment;
import com.church.model.Notice;
import com.church.repository.AttachmentRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class FileStorageService {

    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

    private Path uploadPath;

    private final AttachmentRepository attachmentRepository;

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final int MAX_FILES_PER_NOTICE = 5;
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "jpg", "jpeg", "png", "gif", "webp",
            "pdf", "doc", "docx", "hwp", "xlsx", "xls", "pptx", "ppt"
    );

    @PostConstruct
    public void init() {
        uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(uploadPath);
        } catch (IOException e) {
            throw new RuntimeException("업로드 디렉토리를 생성할 수 없습니다: " + uploadPath, e);
        }
    }

    @Transactional
    public AttachmentResponse store(MultipartFile file) {
        validateFile(file);

        String originalFilename = file.getOriginalFilename();
        String extension = getExtension(originalFilename);
        String storedFilename = UUID.randomUUID().toString() + "." + extension;

        try {
            Path targetPath = uploadPath.resolve(storedFilename);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            Attachment attachment = new Attachment();
            attachment.setOriginalFilename(originalFilename);
            attachment.setStoredFilename(storedFilename);
            attachment.setFilePath(targetPath.toString());
            attachment.setFileSize(file.getSize());
            attachment.setContentType(file.getContentType());

            return AttachmentResponse.from(attachmentRepository.save(attachment));
        } catch (IOException e) {
            throw new RuntimeException("파일 저장에 실패했습니다: " + originalFilename, e);
        }
    }

    public Resource loadAsResource(String storedFilename) {
        try {
            Path filePath = uploadPath.resolve(storedFilename).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            }
            throw new ResourceNotFoundException("파일", 0L);
        } catch (MalformedURLException e) {
            throw new ResourceNotFoundException("파일", 0L);
        }
    }

    @Transactional
    public void linkToNotice(List<Long> attachmentIds, Notice notice) {
        if (attachmentIds == null || attachmentIds.isEmpty()) return;
        if (attachmentIds.size() > MAX_FILES_PER_NOTICE) {
            throw new IllegalArgumentException("첨부파일은 최대 " + MAX_FILES_PER_NOTICE + "개까지 가능합니다.");
        }

        List<Attachment> attachments = attachmentRepository.findAllById(attachmentIds);
        for (Attachment attachment : attachments) {
            attachment.setNotice(notice);
        }
        attachmentRepository.saveAll(attachments);
    }

    @Transactional
    public void unlinkFromNotice(Long attachmentId) {
        Attachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new ResourceNotFoundException("첨부파일", attachmentId));
        // delete physical file
        deletePhysicalFile(attachment.getStoredFilename());
        attachmentRepository.delete(attachment);
    }

    @Transactional
    public void deleteAllByNotice(Long noticeId) {
        List<Attachment> attachments = attachmentRepository.findByNoticeId(noticeId);
        for (Attachment a : attachments) {
            deletePhysicalFile(a.getStoredFilename());
        }
        attachmentRepository.deleteByNoticeId(noticeId);
    }

    public List<AttachmentResponse> getByNoticeId(Long noticeId) {
        return attachmentRepository.findByNoticeId(noticeId)
                .stream()
                .map(AttachmentResponse::from)
                .toList();
    }

    public Attachment getAttachmentByStoredFilename(String storedFilename) {
        return attachmentRepository.findByStoredFilename(storedFilename)
                .orElseThrow(() -> new ResourceNotFoundException("파일", 0L));
    }

    private void deletePhysicalFile(String storedFilename) {
        try {
            Path filePath = uploadPath.resolve(storedFilename);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            // Log but don't fail
            System.err.println("파일 삭제 실패: " + storedFilename);
        }
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("빈 파일은 업로드할 수 없습니다.");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("파일 크기는 10MB를 초과할 수 없습니다.");
        }
        String extension = getExtension(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new IllegalArgumentException(
                    "허용되지 않는 파일 형식입니다. 허용: " + String.join(", ", ALLOWED_EXTENSIONS));
        }
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }
}
