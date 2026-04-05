package com.church.config;

import com.church.model.Role;
import com.church.repository.MemberRepository;
import com.church.service.MemberDetailsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final MemberDetailsService memberDetailsService;
    private final MemberRepository memberRepository;

    @Override
    public void run(String... args) {
        if (!memberRepository.existsByEmail("admin@example.com")) {
            memberDetailsService.createMember("admin@example.com", "password", "관리자", Role.ADMIN);
            log.info("기본 관리자 계정이 생성되었습니다. (admin@example.com / password)");
        } else {
            log.info("기본 관리자 계정이 이미 존재합니다.");
        }
    }
}
