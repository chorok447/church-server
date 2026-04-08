package com.church.service;

import com.church.dto.AttachmentResponse;
import com.church.dto.NoticeRequest;
import com.church.dto.NoticeResponse;
import com.church.exception.ResourceNotFoundException;
import com.church.model.Member;
import com.church.model.Notice;
import com.church.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private final FileStorageService fileStorageService;

    private NoticeResponse toResponseWithAttachments(Notice notice) {
        List<AttachmentResponse> attachments = fileStorageService.getByNoticeId(notice.getId());
        return NoticeResponse.from(notice, attachments);
    }

    public Page<NoticeResponse> getNotices(Pageable pageable) {
        return noticeRepository.findByDeletedFalse(pageable).map(NoticeResponse::from);
    }

    @Cacheable(cacheNames = "publicNoticeList", key = "#sort.toString()")
    public List<NoticeResponse> getAllNoticesSorted(Sort sort) {
        return noticeRepository.findByDeletedFalse(sort)
                .stream()
                .map(NoticeResponse::from)
                .collect(Collectors.toList());
    }

    public Page<NoticeResponse> searchByTitle(String title, Pageable pageable) {
        return noticeRepository.findByDeletedFalseAndTitleContaining(title, pageable).map(NoticeResponse::from);
    }

    public Page<NoticeResponse> searchByContent(String content, Pageable pageable) {
        return noticeRepository.findByDeletedFalseAndContentContaining(content, pageable).map(NoticeResponse::from);
    }

    public Page<NoticeResponse> searchByTitleOrContent(String keyword, Pageable pageable) {
        return noticeRepository.findByDeletedFalseAndTitleContainingOrDeletedFalseAndContentContaining(
                keyword, keyword, pageable).map(NoticeResponse::from);
    }

    @Transactional
    public NoticeResponse getNoticeById(Long id) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("공지사항", id));
        if (notice.getDeleted()) {
            throw new ResourceNotFoundException("공지사항", id);
        }
        noticeRepository.incrementViewCount(id);
        notice.setViewCount(notice.getViewCount() + 1);
        return toResponseWithAttachments(notice);
    }

    @Cacheable(cacheNames = "pinnedNoticeList")
    public List<NoticeResponse> getPinnedNotices() {
        return noticeRepository.findByDeletedFalseAndPinnedTrueOrderByCreatedAtDesc()
                .stream()
                .map(NoticeResponse::from)
                .collect(Collectors.toList());
    }

    @Caching(evict = {
            @CacheEvict(cacheNames = "publicNoticeList", allEntries = true),
            @CacheEvict(cacheNames = "pinnedNoticeList", allEntries = true)
    })
    @Transactional
    public NoticeResponse togglePin(Long id) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("공지사항", id));
        notice.setPinned(!notice.getPinned());
        return NoticeResponse.from(noticeRepository.save(notice));
    }

    @Caching(evict = {
            @CacheEvict(cacheNames = "publicNoticeList", allEntries = true),
            @CacheEvict(cacheNames = "pinnedNoticeList", allEntries = true)
    })
    @Transactional
    public NoticeResponse createNotice(NoticeRequest request, Member author) {
        Notice notice = new Notice();
        notice.setTitle(request.getTitle());
        notice.setDate(request.getDate());
        notice.setContent(request.getContent());
        notice.setPinned(request.getPinned() != null ? request.getPinned() : false);
        notice.setAuthor(author);
        Notice saved = noticeRepository.save(notice);

        // Link attachments
        if (request.getAttachmentIds() != null && !request.getAttachmentIds().isEmpty()) {
            fileStorageService.linkToNotice(request.getAttachmentIds(), saved);
        }

        return toResponseWithAttachments(saved);
    }

    @Caching(evict = {
            @CacheEvict(cacheNames = "publicNoticeList", allEntries = true),
            @CacheEvict(cacheNames = "pinnedNoticeList", allEntries = true)
    })
    @Transactional
    public NoticeResponse updateNotice(Long id, NoticeRequest request) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("공지사항", id));
        notice.setTitle(request.getTitle());
        notice.setDate(request.getDate());
        notice.setContent(request.getContent());
        if (request.getPinned() != null) {
            notice.setPinned(request.getPinned());
        }
        Notice saved = noticeRepository.save(notice);

        // Link new attachments if provided
        if (request.getAttachmentIds() != null && !request.getAttachmentIds().isEmpty()) {
            fileStorageService.linkToNotice(request.getAttachmentIds(), saved);
        }

        return toResponseWithAttachments(saved);
    }

    @Caching(evict = {
            @CacheEvict(cacheNames = "publicNoticeList", allEntries = true),
            @CacheEvict(cacheNames = "pinnedNoticeList", allEntries = true)
    })
    @Transactional
    public void deleteNotice(Long id) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("공지사항", id));
        // Delete attachments
        fileStorageService.deleteAllByNotice(id);
        // 소프트 삭제: 실제 삭제 대신 deleted 플래그 설정
        notice.setDeleted(true);
        noticeRepository.save(notice);
    }
}
