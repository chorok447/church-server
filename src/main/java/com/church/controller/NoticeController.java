package com.church.controller;

import com.church.model.Notice;
import com.church.service.NoticeService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/notices")
public class NoticeController {
    private final NoticeService noticeService;

    public NoticeController(NoticeService noticeService) {
        this.noticeService = noticeService;
    }

    // 페이지네이션 목록 (id 내림차순 기본 정렬)
    @GetMapping
    public Page<Notice> getNotices(Pageable pageable) {
        Pageable sortedByIdDesc = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "id")
        );
        return noticeService.getNotices(sortedByIdDesc);
    }

    // 전체 목록
    @GetMapping("/all")
    public List<Notice> getAllNotices() {
        return noticeService.getAllNoticesSortedByIdDesc();
    }

    @GetMapping("/search/title")
    public Page<Notice> searchByTitle(@RequestParam String title, Pageable pageable) {
        return noticeService.searchByTitle(title, pageable);
    }

    @GetMapping("/search/content")
    public Page<Notice> searchByContent(@RequestParam String content, Pageable pageable) {
        return noticeService.searchByContent(content, pageable);
    }

    @GetMapping("/search/all")
    public Page<Notice> searchByTitleOrContent(@RequestParam String keyword, Pageable pageable) {
        return noticeService.searchByTitleOrContent(keyword, pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Notice> getNoticeById(@PathVariable Long id) {
        return noticeService.getNoticeById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Notice createNotice(@RequestBody Notice notice) {
        return noticeService.createNotice(notice);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Notice> updateNotice(@PathVariable Long id, @RequestBody Notice notice) {
        try {
            return ResponseEntity.ok(noticeService.updateNotice(id, notice));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotice(@PathVariable Long id) {
        noticeService.deleteNotice(id);
        return ResponseEntity.noContent().build();
    }
}