package com.church.repository;

import com.church.model.Notice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoticeRepository extends JpaRepository<Notice, Long> {
    Page<Notice> findByTitleContaining(String title, Pageable pageable);
    Page<Notice> findByContentContaining(String content, Pageable pageable);
    Page<Notice> findByTitleContainingOrContentContaining(String title, String content, Pageable pageable);
}