package com.church.service;

import com.church.dto.SermonRequest;
import com.church.dto.SermonResponse;
import com.church.exception.ResourceNotFoundException;
import com.church.model.Sermon;
import com.church.repository.SermonRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.*;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

class SermonServiceTest {

    private final SermonRepository sermonRepository = Mockito.mock(SermonRepository.class);
    private final SermonService sermonService = new SermonService(sermonRepository);

    private Sermon createSermon(Long id, String title) {
        Sermon sermon = new Sermon();
        sermon.setId(id);
        sermon.setTitle(title);
        sermon.setDate(LocalDate.now());
        return sermon;
    }

    @Test
    @DisplayName("페이징 설교 목록 조회")
    void getSermons_정상조회() {
        Sermon sermon = createSermon(1L, "test");
        Page<Sermon> page = new PageImpl<>(List.of(sermon));
        Pageable pageable = PageRequest.of(0, 10, Sort.by("id").descending());
        given(sermonRepository.findAll(pageable)).willReturn(page);

        Page<SermonResponse> result = sermonService.getSermons(pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("test");
    }

    @Test
    @DisplayName("제목으로 설교 검색")
    void searchByTitle_정상조회() {
        Sermon sermon = createSermon(2L, "spring");
        Page<Sermon> page = new PageImpl<>(List.of(sermon));
        Pageable pageable = PageRequest.of(0, 10);
        given(sermonRepository.findByTitleContaining("spring", pageable)).willReturn(page);

        Page<SermonResponse> result = sermonService.searchByTitle("spring", pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).contains("spring");
    }

    @Test
    @DisplayName("단건 조회 - 존재하지 않는 ID")
    void getSermonById_notFound() {
        given(sermonRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> sermonService.getSermonById(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("설교 생성")
    void createSermon_성공() {
        Sermon saved = createSermon(1L, "새 설교");
        saved.setDescription("요약");
        saved.setPreacher("목사님");
        given(sermonRepository.save(any(Sermon.class))).willReturn(saved);

        SermonRequest request = new SermonRequest();
        request.setTitle("새 설교");
        request.setDate(LocalDate.now());
        request.setDescription("요약");
        request.setPreacher("목사님");

        SermonResponse result = sermonService.createSermon(request);

        assertThat(result.getTitle()).isEqualTo("새 설교");
        assertThat(result.getDescription()).isEqualTo("요약");
        assertThat(result.getPreacher()).isEqualTo("목사님");
    }

    @Test
    @DisplayName("삭제 - 존재하지 않는 ID")
    void deleteSermon_notFound() {
        given(sermonRepository.existsById(999L)).willReturn(false);

        assertThatThrownBy(() -> sermonService.deleteSermon(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}