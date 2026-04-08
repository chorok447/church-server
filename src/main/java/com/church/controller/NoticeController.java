package com.church.controller;

import com.church.dto.ApiResponse;
import com.church.dto.NoticeRequest;
import com.church.dto.NoticeResponse;
import com.church.model.Member;
import com.church.service.MemberAccountService;
import com.church.service.NoticeService;
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
@RequestMapping("/api/notices")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;
    private final MemberAccountService memberAccountService;

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("id", "title", "date", "createdAt", "viewCount");

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
    public ResponseEntity<ApiResponse<Page<NoticeResponse>>> getNotices(
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction,
            Pageable pageable) {
        Pageable sortedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                buildSort(sortBy, direction)
        );
        return ResponseEntity.ok(ApiResponse.success(noticeService.getNotices(sortedPageable)));
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<NoticeResponse>>> getAllNotices(
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction) {
        return ResponseEntity.ok(ApiResponse.success(
                noticeService.getAllNoticesSorted(buildSort(sortBy, direction))));
    }

    @GetMapping("/pinned")
    public ResponseEntity<ApiResponse<List<NoticeResponse>>> getPinnedNotices() {
        return ResponseEntity.ok(ApiResponse.success(noticeService.getPinnedNotices()));
    }

    @GetMapping("/search/title")
    public ResponseEntity<ApiResponse<Page<NoticeResponse>>> searchByTitle(
            @RequestParam String title,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction,
            Pageable pageable) {
        Pageable sortedPageable = PageRequest.of(
                pageable.getPageNumber(), pageable.getPageSize(), buildSort(sortBy, direction));
        return ResponseEntity.ok(ApiResponse.success(noticeService.searchByTitle(title, sortedPageable)));
    }

    @GetMapping("/search/content")
    public ResponseEntity<ApiResponse<Page<NoticeResponse>>> searchByContent(
            @RequestParam String content,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction,
            Pageable pageable) {
        Pageable sortedPageable = PageRequest.of(
                pageable.getPageNumber(), pageable.getPageSize(), buildSort(sortBy, direction));
        return ResponseEntity.ok(ApiResponse.success(noticeService.searchByContent(content, sortedPageable)));
    }

    @GetMapping("/search/all")
    public ResponseEntity<ApiResponse<Page<NoticeResponse>>> searchByTitleOrContent(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction,
            Pageable pageable) {
        Pageable sortedPageable = PageRequest.of(
                pageable.getPageNumber(), pageable.getPageSize(), buildSort(sortBy, direction));
        return ResponseEntity.ok(ApiResponse.success(noticeService.searchByTitleOrContent(keyword, sortedPageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<NoticeResponse>> getNoticeById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(noticeService.getNoticeById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<NoticeResponse>> createNotice(@Valid @RequestBody NoticeRequest request) {
        Member author = memberAccountService.getCurrentMember();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("공지사항이 등록되었습니다.", noticeService.createNotice(request, author)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<NoticeResponse>> updateNotice(
            @PathVariable Long id, @Valid @RequestBody NoticeRequest request) {
        return ResponseEntity.ok(ApiResponse.success("공지사항이 수정되었습니다.", noticeService.updateNotice(id, request)));
    }

    @PatchMapping("/{id}/pin")
    public ResponseEntity<ApiResponse<NoticeResponse>> togglePin(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("고정 상태가 변경되었습니다.", noticeService.togglePin(id)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteNotice(@PathVariable Long id) {
        noticeService.deleteNotice(id);
        return ResponseEntity.ok(ApiResponse.success("공지사항이 삭제되었습니다.", null));
    }
}
