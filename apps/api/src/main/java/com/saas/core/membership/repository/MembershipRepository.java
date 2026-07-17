package com.saas.core.membership.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.saas.core.membership.entity.Membership;

public interface MembershipRepository extends JpaRepository<Membership, UUID> {
}
