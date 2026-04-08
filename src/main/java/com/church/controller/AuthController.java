package com.church.controller;

import com.church.dto.ApiResponse;
import com.church.dto.LoginRequest;
import com.church.dto.MemberResponse;
import com.church.dto.RegisterRequest;
import com.church.model.Member;
import com.church.security.JwtUtil;
import com.church.service.MemberAccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import java.util.HashMap;
import java.util.Map;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import org.springframework.lang.NonNull;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@SuppressWarnings("null")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final MemberAccountService memberAccountService;

    @Value("${jwt.cookie-name:jwt}")
    private String cookieName;

    @Value("${jwt.expiration}")
    private long expirationMs;

    @Value("${jwt.cookie-secure:false}")
    private boolean cookieSecure;

    @Value("${jwt.cookie-same-site:Lax}")
    private String cookieSameSite;

    private void writeJwtCookie(HttpServletResponse response, @NonNull String value, @NonNull Duration maxAge) {
        ResponseCookie cookie = ResponseCookie.from(cookieName != null ? cookieName : "jwt", value)
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite(cookieSameSite)
                .path("/")
                .maxAge(maxAge)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
            Member member = memberAccountService.getMemberByEmail(request.getEmail());
            
            String clientIp = httpRequest.getHeader("X-Forwarded-For");
            if (clientIp == null || clientIp.isEmpty() || "unknown".equalsIgnoreCase(clientIp)) {
                clientIp = httpRequest.getRemoteAddr();
            }
            if (clientIp != null && clientIp.contains(",")) {
                clientIp = clientIp.split(",")[0].trim();
            }
            memberAccountService.updateLoginInfo(member.getEmail(), clientIp);

            String token = jwtUtil.generateToken(member.getEmail(), member.getRole().name());

            writeJwtCookie(httpResponse, token, Duration.ofMillis(expirationMs));

            Map<String, Object> response = new HashMap<>();
            // Still returning basic info, but not the token (it's in the cookie)
            response.put("role", member.getRole().name());
            response.put("name", member.getName());
            response.put("email", member.getEmail());
            response.put("id", member.getId());

            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (DisabledException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("관리자 승인 대기 중입니다. 승인 후 로그인이 가능합니다."));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("이메일 또는 비밀번호가 올바르지 않습니다."));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        writeJwtCookie(response, "", Duration.ZERO);
        return ResponseEntity.ok(ApiResponse.success("로그아웃 되었습니다."));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            MemberResponse member = memberAccountService.register(request);
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
            Member member = memberAccountService.getCurrentMember();
            return ResponseEntity.ok(ApiResponse.success(MemberResponse.from(member)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("로그인이 필요합니다."));
        }
    }
}
