package com.church.service;

import com.church.dto.NoticeRequest;
import com.church.dto.NoticeResponse;
import com.church.exception.ResourceNotFoundException;
import com.church.model.Member;
import com.church.model.Notice;
import com.church.model.Role;
import com.church.repository.NoticeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.*;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

class NoticeServiceTest {

    private final NoticeRepository noticeRepository = Mockito.mock(NoticeRepository.class);
    private final FileStorageService fileStorageService = Mockito.mock(FileStorageService.class);
    private final NoticeService noticeService = new NoticeService(noticeRepository, fileStorageService);

    private Notice createNotice(Long id, String title) {
        Notice notice = new Notice();
        notice.setId(id);
        notice.setTitle(title);
        notice.setDate(LocalDate.now());
        notice.setContent("테스트 내용");
        return notice;
    }

    private Member createMember() {
        Member member = new Member();
        member.setId(1L);
        member.setEmail("test@example.com");
        member.setName("테스트 유저");
        member.setRole(Role.ADMIN);
        return member;
    }

    @Test
    @DisplayName("페이징 공지 목록 조회")
    void getNotices() {
        Notice notice = createNotice(1L, "테스트 공지");
        Page<Notice> page = new PageImpl<>(List.of(notice));
        Pageable pageable = PageRequest.of(0, 10, Sort.by("id").descending());
        given(noticeRepository.findAll(pageable)).willReturn(page);

        Page<NoticeResponse> result = noticeService.getNotices(pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("테스트 공지");
    }

    @Test
    @DisplayName("공지 단건 조회")
    void getNoticeById() {
        Notice notice = createNotice(1L, "단건 조회");
        given(noticeRepository.findById(1L)).willReturn(Optional.of(notice));

        NoticeResponse result = noticeService.getNoticeById(1L);

        assertThat(result.getTitle()).isEqualTo("단건 조회");
    }

    @Test
    @DisplayName("존재하지 않는 공지 조회 시 예외")
    void getNoticeById_notFound() {
        given(noticeRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> noticeService.getNoticeById(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("공지 생성")
    void createNoticeTest() {
        Notice saved = createNotice(1L, "새 공지");
        given(noticeRepository.save(any(Notice.class))).willReturn(saved);

        NoticeRequest request = new NoticeRequest();
        request.setTitle("새 공지");
        request.setDate(LocalDate.now());
        request.setContent("내용");

        Member author = createMember();
        NoticeResponse result = noticeService.createNotice(request, author);

        assertThat(result.getTitle()).isEqualTo("새 공지");
    }

    @Test
    @DisplayName("공지 수정")
    void updateNotice() {
        Notice existing = createNotice(1L, "수정 전");
        Notice updated = createNotice(1L, "수정 후");
        given(noticeRepository.findById(1L)).willReturn(Optional.of(existing));
        given(noticeRepository.save(any(Notice.class))).willReturn(updated);

        NoticeRequest request = new NoticeRequest();
        request.setTitle("수정 후");
        request.setDate(LocalDate.now());

        NoticeResponse result = noticeService.updateNotice(1L, request);

        assertThat(result.getTitle()).isEqualTo("수정 후");
    }

    @Test
    @DisplayName("공지 삭제")
    void deleteNotice() {
        given(noticeRepository.existsById(1L)).willReturn(true);

        noticeService.deleteNotice(1L);

        verify(noticeRepository).deleteById(1L);
    }

    @Test
    @DisplayName("존재하지 않는 공지 삭제 시 예외")
    void deleteNotice_notFound() {
        given(noticeRepository.existsById(999L)).willReturn(false);

        assertThatThrownBy(() -> noticeService.deleteNotice(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("제목 검색")
    void searchByTitle() {
        Notice notice = createNotice(1L, "검색 테스트");
        Page<Notice> page = new PageImpl<>(List.of(notice));
        Pageable pageable = PageRequest.of(0, 10);
        given(noticeRepository.findByTitleContaining("검색", pageable)).willReturn(page);

        Page<NoticeResponse> result = noticeService.searchByTitle("검색", pageable);

        assertThat(result.getContent()).hasSize(1);
    }
}
