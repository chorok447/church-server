package com.church.service;

import com.church.model.Sermon;
import com.church.repository.SermonRepository;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

@Service
public class SermonService {
    private final SermonRepository sermonRepository;

    public SermonService(SermonRepository sermonRepository) {
        this.sermonRepository = sermonRepository;
    }


    public Page<Sermon> getSermons(Pageable pageable) {
        return sermonRepository.findAll(pageable);
    }

    public List<Sermon> getAllSermonsSortedByIdDesc() {
        return sermonRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
    }

    public List<Sermon> getAllSermons() {
        return sermonRepository.findAll();
    }

    public Optional<Sermon> getSermonById(Long id) {
        return sermonRepository.findById(id);
    }

    public Sermon createSermon(Sermon sermon) {
        return sermonRepository.save(sermon);
    }

    public Sermon updateSermon(Long id, Sermon updatedSermon) {
        return sermonRepository.findById(id).map(sermon -> {
            sermon.setTitle(updatedSermon.getTitle());
            sermon.setDate(updatedSermon.getDate());
            sermon.setVideoUrl(updatedSermon.getVideoUrl());
            return sermonRepository.save(sermon);
        }).orElseThrow(() -> new RuntimeException("Sermon not found"));
    }

    public void deleteSermon(Long id) {
        sermonRepository.deleteById(id);
    }

    public Page<Sermon> searchByTitle(String title, Pageable pageable) {
        return sermonRepository.findByTitleContaining(title, pageable);
    }
}