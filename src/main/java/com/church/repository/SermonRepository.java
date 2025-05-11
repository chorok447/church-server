package com.church.repository;

import com.church.model.Sermon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SermonRepository extends JpaRepository<Sermon, Long> {
    Page<Sermon> findAll(Pageable pageable);
    Page<Sermon> findByTitleContaining(String title, Pageable pageable);
}