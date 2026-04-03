package com.church.repository;

import com.church.model.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AttachmentRepository extends JpaRepository<Attachment, Long> {
    List<Attachment> findByNoticeId(Long noticeId);
    Optional<Attachment> findByStoredFilename(String storedFilename);
    void deleteByNoticeId(Long noticeId);
}
