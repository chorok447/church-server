package com.church.controller;

import com.church.dto.ApiResponse;
import com.church.dto.SermonRequest;
import com.church.dto.SermonResponse;
import com.church.service.SermonService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/sermons")
@RequiredArgsConstructor
@SuppressWarnings("null")
public class SermonController {

    private final SermonService sermonService;

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("id", "title", "date", "createdAt");

    private Sort buildSort(String sortBy, String direction) {
        String field = ALLOWED_SORT_FIELDS.contains(sortBy) ? sortBy : "id";
        Sort.Direction dir;
        try {
            dir = Sort.Direction.fromString(direction);
        } catch (IllegalArgumentException e) {
            dir = Sort.Direction.DESC;
        }
        return Sort.by(dir, field);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<SermonResponse>>> getSermons(
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction,
            Pageable pageable) {
        Pageable sortedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                buildSort(sortBy, direction)
        );
        return ResponseEntity.ok(ApiResponse.success(sermonService.getSermons(sortedPageable)));
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<SermonResponse>>> getAllSermons(
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction) {
        return ResponseEntity.ok(ApiResponse.success(
                sermonService.getAllSermonsSorted(buildSort(sortBy, direction))));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SermonResponse>> getSermonById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(sermonService.getSermonById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SermonResponse>> createSermon(@Valid @RequestBody SermonRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("설교가 등록되었습니다.", sermonService.createSermon(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SermonResponse>> updateSermon(
            @PathVariable Long id, @Valid @RequestBody SermonRequest request) {
        return ResponseEntity.ok(ApiResponse.success("설교가 수정되었습니다.", sermonService.updateSermon(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSermon(@PathVariable Long id) {
        sermonService.deleteSermon(id);
        return ResponseEntity.ok(ApiResponse.success("설교가 삭제되었습니다.", null));
    }

    @GetMapping("/search/title")
    public ResponseEntity<ApiResponse<Page<SermonResponse>>> searchByTitle(
            @RequestParam String title,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction,
            Pageable pageable) {
        Pageable sortedPageable = PageRequest.of(
                pageable.getPageNumber(), pageable.getPageSize(), buildSort(sortBy, direction));
        return ResponseEntity.ok(ApiResponse.success(sermonService.searchByTitle(title, sortedPageable)));
    }

    @GetMapping("/search/preacher")
    public ResponseEntity<ApiResponse<Page<SermonResponse>>> searchByPreacher(
            @RequestParam String preacher,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction,
            Pageable pageable) {
        Pageable sortedPageable = PageRequest.of(
                pageable.getPageNumber(), pageable.getPageSize(), buildSort(sortBy, direction));
        return ResponseEntity.ok(ApiResponse.success(sermonService.searchByPreacher(preacher, sortedPageable)));
    }
}