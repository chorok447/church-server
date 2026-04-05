package com.church.repository;

import com.church.model.Notice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NoticeRepository extends JpaRepository<Notice, Long> {

    // --- 삭제되지 않은 항목만 조회 ---
    @EntityGraph(attributePaths = {"author"})
    Page<Notice> findByDeletedFalse(Pageable pageable);

    @EntityGraph(attributePaths = {"author"})
    List<Notice> findByDeletedFalse(Sort sort);

    @EntityGraph(attributePaths = {"author"})
    Page<Notice> findByDeletedFalseAndTitleContaining(String title, Pageable pageable);

    @EntityGraph(attributePaths = {"author"})
    Page<Notice> findByDeletedFalseAndContentContaining(String content, Pageable pageable);

    @EntityGraph(attributePaths = {"author"})
    Page<Notice> findByDeletedFalseAndTitleContainingOrDeletedFalseAndContentContaining(
            String title, String content, Pageable pageable);

    @EntityGraph(attributePaths = {"author"})
    List<Notice> findByDeletedFalseAndPinnedTrueOrderByCreatedAtDesc();

    // --- 기존 호환 ---
    Page<Notice> findByTitleContaining(String title, Pageable pageable);
    Page<Notice> findByContentContaining(String content, Pageable pageable);
    Page<Notice> findByTitleContainingOrContentContaining(String title, String content, Pageable pageable);

    List<Notice> findByPinnedTrueOrderByCreatedAtDesc();

    @Modifying
    @Query("UPDATE Notice n SET n.viewCount = n.viewCount + 1 WHERE n.id = :id")
    void incrementViewCount(@Param("id") Long id);
}