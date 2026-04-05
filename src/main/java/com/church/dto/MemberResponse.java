package com.church.dto;

import com.church.model.Member;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class MemberResponse {
    private Long id;
    private String email;
    private String name;
    private String role;
    private boolean approved;
    private LocalDateTime createdAt;
    private String lastLoginIp;
    private LocalDateTime lastLoginDate;

    public static MemberResponse from(Member member) {
        return MemberResponse.builder()
                .id(member.getId())
                .email(member.getEmail())
                .name(member.getName())
                .role(member.getRole().name())
                .approved(member.isApproved())
                .createdAt(member.getCreatedAt())
                .lastLoginIp(member.getLastLoginIp())
                .lastLoginDate(member.getLastLoginDate())
                .build();
    }
}
