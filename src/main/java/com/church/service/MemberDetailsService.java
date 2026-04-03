package com.church.service;

import com.church.dto.MemberResponse;
import com.church.dto.RegisterRequest;
import com.church.exception.ResourceNotFoundException;
import com.church.model.Member;
import com.church.model.Role;
import com.church.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MemberDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + email));

        return new User(
                member.getEmail(),
                member.getPassword(),
                member.isApproved(),    // enabled — 미승인 회원은 false
                true,                    // accountNonExpired
                true,                    // credentialsNonExpired
                true,                    // accountNonLocked
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + member.getRole().name()))
        );
    }

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
        member.setApproved(false);  // 관리자 승인 전까지 비활성

        return MemberResponse.from(memberRepository.save(member));
    }

    @Transactional
    public void createMember(String email, String password, String name, Role role) {
        Member member = new Member();
        member.setEmail(email);
        member.setPassword(passwordEncoder.encode(password));
        member.setName(name);
        member.setRole(role);
        member.setApproved(true);  // 관리자가 직접 생성한 계정은 자동 승인
        memberRepository.save(member);
    }

    public Member getCurrentMember() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("로그인된 사용자를 찾을 수 없습니다."));
    }

    public Member getMemberByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + email));
    }

    // ── 관리자 회원 관리 메서드 ──

    @Transactional(readOnly = true)
    public List<MemberResponse> getPendingMembers() {
        return memberRepository.findByApprovedOrderByCreatedAtDesc(false).stream()
                .map(MemberResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MemberResponse> getAllMembers() {
        return memberRepository.findAll().stream()
                .map(MemberResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public long getPendingCount() {
        return memberRepository.countByApproved(false);
    }

    @Transactional
    public MemberResponse approveMember(Long id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("회원을 찾을 수 없습니다: " + id));
        member.setApproved(true);
        return MemberResponse.from(memberRepository.save(member));
    }

    @Transactional
    public void rejectMember(Long id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("회원을 찾을 수 없습니다: " + id));
        memberRepository.delete(member);
    }
}
