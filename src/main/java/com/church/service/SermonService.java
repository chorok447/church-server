package com.church.service;

import com.church.dto.SermonRequest;
import com.church.dto.SermonResponse;
import com.church.exception.ResourceNotFoundException;
import com.church.model.Sermon;
import com.church.repository.SermonRepository;
import lombok.RequiredArgsConstructor;
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
public class SermonService {

    private final SermonRepository sermonRepository;

    public Page<SermonResponse> getSermons(Pageable pageable) {
        return sermonRepository.findAll(pageable).map(SermonResponse::from);
    }

    public List<SermonResponse> getAllSermonsSorted(Sort sort) {
        return sermonRepository.findAll(sort)
                .stream()
                .map(SermonResponse::from)
                .collect(Collectors.toList());
    }

    public SermonResponse getSermonById(Long id) {
        Sermon sermon = sermonRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("설교", id));
        return SermonResponse.from(sermon);
    }

    @Transactional
    public SermonResponse createSermon(SermonRequest request) {
        Sermon sermon = new Sermon();
        sermon.setTitle(request.getTitle());
        sermon.setDate(request.getDate());
        sermon.setVideoUrl(request.getVideoUrl());
        sermon.setDescription(request.getDescription());
        sermon.setPreacher(request.getPreacher());
        return SermonResponse.from(sermonRepository.save(sermon));
    }

    @Transactional
    public SermonResponse updateSermon(Long id, SermonRequest request) {
        Sermon sermon = sermonRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("설교", id));
        sermon.setTitle(request.getTitle());
        sermon.setDate(request.getDate());
        sermon.setVideoUrl(request.getVideoUrl());
        sermon.setDescription(request.getDescription());
        sermon.setPreacher(request.getPreacher());
        return SermonResponse.from(sermonRepository.save(sermon));
    }

    @Transactional
    public void deleteSermon(Long id) {
        if (!sermonRepository.existsById(id)) {
            throw new ResourceNotFoundException("설교", id);
        }
        sermonRepository.deleteById(id);
    }

    public Page<SermonResponse> searchByTitle(String title, Pageable pageable) {
        return sermonRepository.findByTitleContaining(title, pageable).map(SermonResponse::from);
    }

    public Page<SermonResponse> searchByPreacher(String preacher, Pageable pageable) {
        return sermonRepository.findByPreacherContaining(preacher, pageable).map(SermonResponse::from);
    }
}