package com.church.service;

import com.church.dto.MemberResponse;
import com.church.dto.RegisterRequest;
import com.church.model.Member;
import com.church.model.Role;
import com.church.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberAccountService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public MemberResponse register(RegisterRequest request) {
        if (memberRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        Member member = new Member();
        member.setEmail(request.getEmail());
        member.setPassword(passwordEncoder.encode(request.getPassword()));
        member.setName(request.getName());
        member.setRole(Role.USER);
        member.setApproved(false);
        return MemberResponse.from(memberRepository.save(member));
    }

    @Transactional
    public void createMember(String email, String password, String name, Role role) {
        Member member = new Member();
        member.setEmail(email);
        member.setPassword(passwordEncoder.encode(password));
        member.setName(name);
        member.setRole(role);
        member.setApproved(true);
        memberRepository.save(member);
    }

    @Transactional(readOnly = true)
    public Member getCurrentMember() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return getMemberByEmail(email);
    }

    @Transactional(readOnly = true)
    public Member getMemberByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + email));
    }

    @Transactional
    public void updateLoginInfo(String email, String ip) {
        Member member = getMemberByEmail(email);
        member.setLastLoginIp(ip);
        member.setLastLoginDate(java.time.LocalDateTime.now());
    }
}
