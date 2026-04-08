package com.church.controller;

import com.church.dto.ApiResponse;
import com.church.dto.CommentRequest;
import com.church.dto.CommentResponse;
import com.church.model.Member;
import com.church.service.CommentService;
import com.church.service.MemberAccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;
    private final MemberAccountService memberAccountService;

    @GetMapping("/notices/{noticeId}/comments")
    public ResponseEntity<ApiResponse<List<CommentResponse>>> getComments(@PathVariable Long noticeId) {
        return ResponseEntity.ok(ApiResponse.success(commentService.getCommentsByNotice(noticeId)));
    }

    @PostMapping("/notices/{noticeId}/comments")
    public ResponseEntity<ApiResponse<CommentResponse>> createComment(
            @PathVariable Long noticeId,
            @Valid @RequestBody CommentRequest request) {
        Member author = memberAccountService.getCurrentMember();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("댓글이 등록되었습니다.", commentService.createComment(noticeId, request, author)));
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(@PathVariable Long commentId) {
        Member actor = memberAccountService.getCurrentMember();
        commentService.deleteComment(commentId, actor);
        return ResponseEntity.ok(ApiResponse.success("댓글이 삭제되었습니다.", null));
    }
}
