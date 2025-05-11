package com.church.config;

import com.church.model.Admin;
import com.church.repository.AdminRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(AdminRepository adminRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (adminRepository.findByEmail("admin@example.com").isEmpty()) {
                Admin admin = new Admin();
                admin.setEmail("admin@example.com");
                admin.setPassword(passwordEncoder.encode("password123")); // 비밀번호 암호화
                adminRepository.save(admin);
                System.out.println("관리자 계정 생성: admin@example.com / password123");
            }
        };
    }
}