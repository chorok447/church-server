package com.church.service;

import com.church.dto.CommentRequest;
import com.church.dto.CommentResponse;
import com.church.exception.ResourceNotFoundException;
import com.church.model.Comment;
import com.church.model.Member;
import com.church.model.Notice;
import com.church.model.Role;
import com.church.repository.CommentRepository;
import com.church.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;
    private final NoticeRepository noticeRepository;

    public List<CommentResponse> getCommentsByNotice(Long noticeId) {
        return commentRepository.findByNoticeIdOrderByCreatedAtDesc(noticeId)
                .stream()
                .map(CommentResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public CommentResponse createComment(Long noticeId, CommentRequest request, Member author) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new ResourceNotFoundException("공지사항", noticeId));

        Comment comment = new Comment();
        comment.setContent(request.getContent());
        comment.setAuthor(author);
        comment.setNotice(notice);

        return CommentResponse.from(commentRepository.save(comment));
    }

    @Transactional
    public void deleteComment(Long commentId, Member actor) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("댓글", commentId));

        // 작성자 또는 ADMIN만 삭제 가능
        if (!comment.getAuthor().getId().equals(actor.getId()) && actor.getRole() != Role.ADMIN) {
            throw new IllegalArgumentException("댓글을 삭제할 권한이 없습니다.");
        }

        commentRepository.delete(comment);
    }
}
