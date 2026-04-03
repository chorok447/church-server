package com.church.controller;

import com.church.dto.ApiResponse;
import com.church.dto.MemberResponse;
import com.church.service.MemberDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberDetailsService memberDetailsService;

    /**
     * 전체 회원 목록 조회 (ADMIN 전용)
     */
    @GetMapping
    public ResponseEntity<?> getAllMembers() {
        List<MemberResponse> members = memberDetailsService.getAllMembers();
        return ResponseEntity.ok(ApiResponse.success(members));
    }

    /**
     * 승인 대기 회원 목록 조회 (ADMIN 전용)
     */
    @GetMapping("/pending")
    public ResponseEntity<?> getPendingMembers() {
        List<MemberResponse> pending = memberDetailsService.getPendingMembers();
        long pendingCount = memberDetailsService.getPendingCount();

        Map<String, Object> response = new HashMap<>();
        response.put("members", pending);
        response.put("count", pendingCount);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 회원 승인 (ADMIN 전용)
     */
    @PatchMapping("/{id}/approve")
    public ResponseEntity<?> approveMember(@PathVariable Long id) {
        MemberResponse member = memberDetailsService.approveMember(id);
        return ResponseEntity.ok(ApiResponse.success("회원이 승인되었습니다.", member));
    }

    /**
     * 회원 거부/삭제 (ADMIN 전용)
     */
    @DeleteMapping("/{id}/reject")
    public ResponseEntity<?> rejectMember(@PathVariable Long id) {
        memberDetailsService.rejectMember(id);
        return ResponseEntity.ok(ApiResponse.success("회원 가입이 거부되었습니다.", null));
    }
}
