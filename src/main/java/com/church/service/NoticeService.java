package com.church.service;

import com.church.model.Notice;
import com.church.repository.NoticeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

@Service
public class NoticeService {
    private final NoticeRepository noticeRepository;

    public NoticeService(NoticeRepository noticeRepository) {
        this.noticeRepository = noticeRepository;
    }

    public List<Notice> getAllNoticesSortedByIdDesc() {
        return noticeRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
    }

    public Page<Notice> searchByTitle(String title, Pageable pageable) {
        return noticeRepository.findByTitleContaining(title, pageable);
    }

    public Page<Notice> searchByContent(String content, Pageable pageable) {
        return noticeRepository.findByContentContaining(content, pageable);
    }

    public Page<Notice> searchByTitleOrContent(String keyword, Pageable pageable) {
        return noticeRepository.findByTitleContainingOrContentContaining(keyword, keyword, pageable);
    }

    public Optional<Notice> getNoticeById(Long id) {
        return noticeRepository.findById(id);
    }

    public Notice createNotice(Notice notice) {
        return noticeRepository.save(notice);
    }

    public Notice updateNotice(Long id, Notice updatedNotice) {
        return noticeRepository.findById(id).map(notice -> {
            notice.setTitle(updatedNotice.getTitle());
            notice.setDate(updatedNotice.getDate());
            notice.setContent(updatedNotice.getContent());
            return noticeRepository.save(notice);
        }).orElseThrow(() -> new RuntimeException("Notice not found"));
    }

    public void deleteNotice(Long id) {
        noticeRepository.deleteById(id);
    }

    public Page<Notice> getNotices(Pageable pageable) {
        return noticeRepository.findAll(pageable);
    }
}