package com.church.controller;

import com.church.dto.ApiResponse;
import com.church.dto.LoginRequest;
import com.church.dto.MemberResponse;
import com.church.dto.RegisterRequest;
import com.church.model.Member;
import com.church.security.JwtUtil;
import com.church.service.MemberDetailsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final MemberDetailsService memberDetailsService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
            Member member = memberDetailsService.getMemberByEmail(request.getEmail());
            String token = jwtUtil.generateToken(member.getEmail(), member.getRole().name());

            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("role", member.getRole().name());
            response.put("name", member.getName());
            response.put("email", member.getEmail());
            response.put("id", member.getId());

            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (DisabledException e) {
            // 미승인 회원이 로그인 시도
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("관리자 승인 대기 중입니다. 승인 후 로그인이 가능합니다."));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("이메일 또는 비밀번호가 올바르지 않습니다."));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            MemberResponse member = memberDetailsService.register(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("회원가입 신청이 완료되었습니다. 관리자 승인 후 로그인이 가능합니다.", member));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMe() {
        try {
            Member member = memberDetailsService.getCurrentMember();
            return ResponseEntity.ok(ApiResponse.success(MemberResponse.from(member)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("로그인이 필요합니다."));
        }
    }
}