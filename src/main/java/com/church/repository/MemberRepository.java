package com.church.repository;

import com.church.model.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmail(String email);
    boolean existsByEmail(String email);
    List<Member> findByApprovedOrderByCreatedAtDesc(boolean approved);
    long countByApproved(boolean approved);
}
