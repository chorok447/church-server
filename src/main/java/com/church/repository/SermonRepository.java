package com.church.repository;

import com.church.model.Sermon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;

public interface SermonRepository extends JpaRepository<Sermon, Long> {
    @NonNull
    Page<Sermon> findAll(@NonNull Pageable pageable);
    Page<Sermon> findByTitleContaining(String title, Pageable pageable);
    Page<Sermon> findByPreacherContaining(String preacher, Pageable pageable);
}