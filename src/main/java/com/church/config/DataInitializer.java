package com.church.config;

import com.church.model.Role;
import com.church.repository.MemberRepository;
import com.church.service.MemberAccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(name = "app.seed-default-admin", havingValue = "true")
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final MemberAccountService memberAccountService;
    private final MemberRepository memberRepository;

    @Value("${app.default-admin-email:}")
    private String defaultAdminEmail;

    @Value("${app.default-admin-password:}")
    private String defaultAdminPassword;

    @Value("${app.default-admin-name:}")
    private String defaultAdminName;

    @Override
    public void run(String... args) {
        if (defaultAdminEmail.isBlank() || defaultAdminPassword.isBlank() || defaultAdminName.isBlank()) {
            log.warn("기본 관리자 계정을 생성하지 않았습니다. 필요한 설정값이 비어 있습니다.");
            return;
        }

        if (!memberRepository.existsByEmail(defaultAdminEmail)) {
            memberAccountService.createMember(defaultAdminEmail, defaultAdminPassword, defaultAdminName, Role.ADMIN);
            log.info("기본 관리자 계정이 생성되었습니다. ({})", defaultAdminEmail);
        } else {
            log.info("기본 관리자 계정이 이미 존재합니다.");
        }
    }
}
