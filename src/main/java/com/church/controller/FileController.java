package com.church.controller;

import com.church.dto.ApiResponse;
import com.church.dto.AttachmentResponse;
import com.church.model.Attachment;
import com.church.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileStorageService fileStorageService;

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<List<AttachmentResponse>>> uploadFiles(
            @RequestParam("files") List<MultipartFile> files) {
        if (files.size() > 5) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("파일은 최대 5개까지 업로드할 수 있습니다."));
        }

        List<AttachmentResponse> responses = new ArrayList<>();
        for (MultipartFile file : files) {
            responses.add(fileStorageService.store(file));
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("파일이 업로드되었습니다.", responses));
    }

    @GetMapping("/download/{storedFilename}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String storedFilename) {
        Resource resource = fileStorageService.loadAsResource(storedFilename);
        Attachment attachment = fileStorageService.getAttachmentByStoredFilename(storedFilename);

        String encodedFilename = URLEncoder.encode(attachment.getOriginalFilename(), StandardCharsets.UTF_8)
                .replace("+", "%20");

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(attachment.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename*=UTF-8''" + encodedFilename)
                .body(resource);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteFile(@PathVariable Long id) {
        fileStorageService.unlinkFromNotice(id);
        return ResponseEntity.ok(ApiResponse.success("파일이 삭제되었습니다.", null));
    }
}
