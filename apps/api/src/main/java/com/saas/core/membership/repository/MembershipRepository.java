package com.saas.core.membership.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.saas.core.membership.entity.Membership;
import com.saas.core.membership.entity.MembershipStatus;

public interface MembershipRepository extends JpaRepository<Membership, UUID> {

    /**
     * At most one result: INV-1 (domain-invariants.md) guarantees a member never holds more than
     * one ACTIVE membership at a time.
     */
    Optional<Membership> findByMemberIdAndStatus(UUID memberId, MembershipStatus status);
}
