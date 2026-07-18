package com.saas.core.membership;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import javax.sql.DataSource;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import com.saas.core.membership.entity.Member;
import com.saas.core.membership.entity.Membership;
import com.saas.core.membership.entity.MembershipPlan;
import com.saas.core.membership.repository.MemberRepository;
import com.saas.core.membership.repository.MembershipPlanRepository;
import com.saas.core.membership.repository.MembershipRepository;
import com.saas.core.tenant.TenantContext;
import com.saas.testsupport.AbstractIntegrationTest;

class MembershipModelTest extends AbstractIntegrationTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MembershipPlanRepository membershipPlanRepository;

    @Autowired
    private MembershipRepository membershipRepository;

    @Autowired
    private DataSource dataSource;

    @AfterEach
    void clearTenantContext() {
        TenantContext.clear();
    }

    /**
     * INV-1 (domain-invariants.md): a member can't hold two ACTIVE memberships in the same
     * tenant at once. Proves idx_membership_one_active_per_member actually blocks it, not just
     * that the migration ran without error.
     */
    @Test
    void aMemberCannotHoldTwoActiveMembershipsAtOnce() throws Exception {
        UUID tenantId = insertTenant();
        TenantContext.setTenantId(tenantId);

        Member member = memberRepository.save(new Member(tenantId, "Test Member", "5551234567"));
        MembershipPlan plan = membershipPlanRepository.save(new MembershipPlan(tenantId, "Monthly", 30, null));

        Instant now = Instant.now();
        membershipRepository.save(
                new Membership(tenantId, member.getId(), plan.getId(), now, now.plus(30, ChronoUnit.DAYS)));

        Membership secondActive =
                new Membership(tenantId, member.getId(), plan.getId(), now, now.plus(30, ChronoUnit.DAYS));
        assertThatThrownBy(() -> membershipRepository.saveAndFlush(secondActive))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    /**
     * A frozen membership for the same member doesn't count as "active", so this must succeed -
     * otherwise the constraint would be "one membership ever" instead of "one active at a time".
     */
    @Test
    void aFrozenMembershipDoesNotBlockANewActiveOne() throws Exception {
        UUID tenantId = insertTenant();
        TenantContext.setTenantId(tenantId);

        Member member = memberRepository.save(new Member(tenantId, "Test Member", "5551234567"));
        MembershipPlan plan = membershipPlanRepository.save(new MembershipPlan(tenantId, "Monthly", 30, null));

        Instant now = Instant.now();
        Membership first = membershipRepository.save(
                new Membership(tenantId, member.getId(), plan.getId(), now, now.plus(30, ChronoUnit.DAYS)));
        first.freeze();
        membershipRepository.saveAndFlush(first);

        Membership second = membershipRepository.saveAndFlush(
                new Membership(tenantId, member.getId(), plan.getId(), now, now.plus(30, ChronoUnit.DAYS)));

        assertThat(second.getId()).isNotEqualTo(first.getId());
    }

    /**
     * concurrency-and-idempotency.md: two staff updating the same membership at once must not
     * let the later write silently overwrite the earlier one. Loads the same row through two
     * separate repository calls (two distinct managed instances, as two concurrent requests
     * would each have their own) to prove the version check itself, not just that the column
     * exists.
     */
    @Test
    void concurrentUpdatesToTheSameMembershipConflict() throws Exception {
        UUID tenantId = insertTenant();
        TenantContext.setTenantId(tenantId);

        Member member = memberRepository.save(new Member(tenantId, "Test Member", "5551234567"));
        MembershipPlan plan = membershipPlanRepository.save(new MembershipPlan(tenantId, "Monthly", 30, null));
        Instant now = Instant.now();
        UUID membershipId = membershipRepository.save(
                new Membership(tenantId, member.getId(), plan.getId(), now, now.plus(30, ChronoUnit.DAYS))).getId();

        Membership firstCopy = membershipRepository.findById(membershipId).orElseThrow();
        Membership staleCopy = membershipRepository.findById(membershipId).orElseThrow();

        firstCopy.freeze();
        membershipRepository.saveAndFlush(firstCopy);

        staleCopy.expire();
        assertThatThrownBy(() -> membershipRepository.saveAndFlush(staleCopy))
                .isInstanceOf(ObjectOptimisticLockingFailureException.class);
    }

    /**
     * Same RLS policy shape as tenant/user (tenant-isolation-enforcement.md's pattern) but this
     * is a hand-written copy for a new table - confirms it wasn't mistyped, not just that the
     * mechanism works in the abstract.
     */
    @Test
    void memberIsInvisibleWithNoTenantContext() throws Exception {
        UUID tenantId = insertTenant();
        TenantContext.setTenantId(tenantId);
        memberRepository.save(new Member(tenantId, "Invisible Member", "5551234567"));

        TenantContext.clear();
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement statement = connection.prepareStatement("SELECT count(*) FROM member");
                    ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                assertThat(resultSet.getLong(1)).isZero();
            }
            connection.rollback();
        }
    }

    private UUID insertTenant() throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement statement = connection.prepareStatement("SET LOCAL app.platform_context = 'true'")) {
                statement.execute();
            }
            try (PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO tenant (name) VALUES (?) RETURNING id")) {
                statement.setString(1, "Membership Test Tenant");
                try (ResultSet resultSet = statement.executeQuery()) {
                    resultSet.next();
                    UUID id = resultSet.getObject("id", UUID.class);
                    connection.commit();
                    return id;
                }
            }
        }
    }
}
