package com.church.repository;

import com.church.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByNoticeIdOrderByCreatedAtDesc(Long noticeId);
    void deleteAllByNoticeId(Long noticeId);
}
