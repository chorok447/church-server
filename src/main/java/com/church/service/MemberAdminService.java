package com.church.service;

import com.church.dto.MemberResponse;
import com.church.exception.ResourceNotFoundException;
import com.church.model.Member;
import com.church.model.Role;
import com.church.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class MemberAdminService {

    private final MemberRepository memberRepository;

    @Transactional(readOnly = true)
    public List<MemberResponse> getPendingMembers() {
        return memberRepository.findByApprovedOrderByCreatedAtDesc(false).stream()
                .map(MemberResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MemberResponse> getAllMembers() {
        return memberRepository.findAll().stream()
                .map(MemberResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public long getPendingCount() {
        return memberRepository.countByApproved(false);
    }

    @Transactional
    public MemberResponse approveMember(Long id) {
        Member member = getMemberById(id);
        member.setApproved(true);
        return MemberResponse.from(memberRepository.save(member));
    }

    @Transactional
    public MemberResponse toggleRole(Long id) {
        Member member = getMemberById(id);
        member.setRole(member.getRole() == Role.ADMIN ? Role.USER : Role.ADMIN);
        return MemberResponse.from(memberRepository.save(member));
    }

    @Transactional
    public void rejectMember(Long id) {
        memberRepository.delete(getMemberById(id));
    }

    private Member getMemberById(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("회원을 찾을 수 없습니다: " + id));
    }
}
