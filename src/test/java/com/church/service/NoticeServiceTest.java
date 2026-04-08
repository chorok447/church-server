package com.church.service;

import com.church.dto.AttachmentResponse;
import com.church.dto.NoticeRequest;
import com.church.dto.NoticeResponse;
import com.church.exception.ResourceNotFoundException;
import com.church.model.Member;
import com.church.model.Notice;
import com.church.model.Role;
import com.church.repository.NoticeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@SuppressWarnings("null")
class NoticeServiceTest {

    private final NoticeRepository noticeRepository = Mockito.mock(NoticeRepository.class);
    private final FileStorageService fileStorageService = Mockito.mock(FileStorageService.class);
    private final NoticeService noticeService = new NoticeService(noticeRepository, fileStorageService);

    private Notice createNotice(Long id, String title) {
        Notice notice = new Notice();
        notice.setId(id);
        notice.setTitle(title);
        notice.setDate(LocalDate.now());
        notice.setContent("test content");
        return notice;
    }

    private Member createMember() {
        Member member = new Member();
        member.setId(1L);
        member.setEmail("test@example.com");
        member.setName("Test Admin");
        member.setRole(Role.ADMIN);
        return member;
    }

    private AttachmentResponse createAttachmentResponse(Long id) {
        return AttachmentResponse.builder()
                .id(id)
                .originalFilename("file-" + id + ".pdf")
                .storedFilename("stored-" + id + ".pdf")
                .fileSize(128L)
                .contentType("application/pdf")
                .downloadUrl("/api/files/download/stored-" + id + ".pdf")
                .build();
    }

    @Test
    @DisplayName("getNotices uses non-deleted query")
    void getNotices() {
        Notice notice = createNotice(1L, "Test notice");
        Page<Notice> page = new PageImpl<>(List.of(notice));
        Pageable pageable = PageRequest.of(0, 10, Sort.by("id").descending());
        given(noticeRepository.findByDeletedFalse(pageable)).willReturn(page);

        Page<NoticeResponse> result = noticeService.getNotices(pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Test notice");
    }

    @Test
    @DisplayName("getNoticeById increments view count and returns attachments")
    void getNoticeById() {
        Notice notice = createNotice(1L, "Single notice");
        given(noticeRepository.findById(1L)).willReturn(Optional.of(notice));
        given(fileStorageService.getByNoticeId(1L)).willReturn(List.of(createAttachmentResponse(10L)));

        NoticeResponse result = noticeService.getNoticeById(1L);

        assertThat(result.getTitle()).isEqualTo("Single notice");
        assertThat(result.getAttachments()).hasSize(1);
        assertThat(result.getViewCount()).isEqualTo(1);
        verify(noticeRepository).incrementViewCount(1L);
    }

    @Test
    @DisplayName("getNoticeById throws when notice is missing")
    void getNoticeById_notFound() {
        given(noticeRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> noticeService.getNoticeById(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("getNoticeById throws when notice is soft deleted")
    void getNoticeById_deletedNotice() {
        Notice notice = createNotice(1L, "Deleted notice");
        notice.setDeleted(true);
        given(noticeRepository.findById(1L)).willReturn(Optional.of(notice));

        assertThatThrownBy(() -> noticeService.getNoticeById(1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("createNotice links attachments when attachment ids are provided")
    void createNoticeTest() {
        Notice saved = createNotice(1L, "New notice");
        given(noticeRepository.save(any(Notice.class))).willReturn(saved);
        given(fileStorageService.getByNoticeId(1L)).willReturn(List.of(createAttachmentResponse(100L)));

        NoticeRequest request = new NoticeRequest();
        request.setTitle("New notice");
        request.setDate(LocalDate.now());
        request.setContent("content");
        request.setPinned(true);
        request.setAttachmentIds(List.of(100L, 101L));

        Member author = createMember();
        NoticeResponse result = noticeService.createNotice(request, author);

        assertThat(result.getTitle()).isEqualTo("New notice");
        assertThat(result.getAttachments()).hasSize(1);
        verify(fileStorageService).linkToNotice(List.of(100L, 101L), saved);
    }

    @Test
    @DisplayName("updateNotice updates fields and links new attachments")
    void updateNotice() {
        Notice existing = createNotice(1L, "Original title");
        Notice updated = createNotice(1L, "Updated title");
        updated.setPinned(true);

        given(noticeRepository.findById(1L)).willReturn(Optional.of(existing));
        given(noticeRepository.save(any(Notice.class))).willReturn(updated);
        given(fileStorageService.getByNoticeId(1L)).willReturn(List.of(createAttachmentResponse(200L)));

        NoticeRequest request = new NoticeRequest();
        request.setTitle("Updated title");
        request.setDate(LocalDate.now().plusDays(1));
        request.setContent("updated content");
        request.setPinned(true);
        request.setAttachmentIds(List.of(200L));

        NoticeResponse result = noticeService.updateNotice(1L, request);

        assertThat(result.getTitle()).isEqualTo("Updated title");
        assertThat(result.getAttachments()).hasSize(1);
        assertThat(existing.getContent()).isEqualTo("updated content");
        assertThat(existing.getPinned()).isTrue();
        verify(fileStorageService).linkToNotice(List.of(200L), updated);
    }

    @Test
    @DisplayName("deleteNotice performs soft delete and removes attachments")
    void deleteNotice() {
        Notice notice = createNotice(1L, "Delete me");
        given(noticeRepository.findById(1L)).willReturn(Optional.of(notice));

        noticeService.deleteNotice(1L);

        ArgumentCaptor<Notice> captor = ArgumentCaptor.forClass(Notice.class);
        verify(fileStorageService).deleteAllByNotice(1L);
        verify(noticeRepository).save(captor.capture());
        assertThat(captor.getValue().getDeleted()).isTrue();
    }

    @Test
    @DisplayName("deleteNotice throws when notice is missing")
    void deleteNotice_notFound() {
        given(noticeRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> noticeService.deleteNotice(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("searchByTitle uses non-deleted title query")
    void searchByTitle() {
        Notice notice = createNotice(1L, "Search target");
        Page<Notice> page = new PageImpl<>(List.of(notice));
        Pageable pageable = PageRequest.of(0, 10);
        given(noticeRepository.findByDeletedFalseAndTitleContaining("Search", pageable)).willReturn(page);

        Page<NoticeResponse> result = noticeService.searchByTitle("Search", pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Search target");
    }
}
