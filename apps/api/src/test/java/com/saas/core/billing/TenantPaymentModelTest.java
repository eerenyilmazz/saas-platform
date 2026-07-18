package com.saas.core.billing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Instant;
import java.util.UUID;

import javax.sql.DataSource;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import com.saas.core.billing.entity.PaymentMethod;
import com.saas.core.billing.entity.TenantPayment;
import com.saas.core.billing.repository.TenantPaymentRepository;
import com.saas.core.tenant.TenantContext;
import com.saas.testsupport.AbstractIntegrationTest;

class TenantPaymentModelTest extends AbstractIntegrationTest {

    @Autowired
    private TenantPaymentRepository tenantPaymentRepository;

    @Autowired
    private DataSource dataSource;

    @AfterEach
    void clearTenantContext() {
        TenantContext.clear();
    }

    @Test
    void aTenantPaymentRecordsWhoWhatAndHowMuchWasPaid() throws Exception {
        UUID tenantId = insertTenant();
        TenantContext.setTenantId(tenantId);
        UUID sourceId = UUID.randomUUID();
        Instant paidAt = Instant.now();

        TenantPayment saved = tenantPaymentRepository.save(
                new TenantPayment(tenantId, "membership", sourceId, new BigDecimal("450.00"), PaymentMethod.CASH, paidAt));

        TenantPayment reloaded = tenantPaymentRepository.findById(saved.getId()).orElseThrow();
        assertThat(reloaded.getSourceType()).isEqualTo("membership");
        assertThat(reloaded.getSourceId()).isEqualTo(sourceId);
        assertThat(reloaded.getAmount()).isEqualByComparingTo("450.00");
        assertThat(reloaded.getMethod()).isEqualTo(PaymentMethod.CASH);
    }

    /**
     * The CHECK(amount > 0) constraint (V202607181600) is the only guard against a zero/negative
     * payment - there is no application-layer validation to duplicate it, so this proves the DB
     * constraint itself, not a Java-side check.
     */
    @Test
    void nonPositiveAmountIsRejectedByTheDatabase() throws Exception {
        UUID tenantId = insertTenant();
        TenantContext.setTenantId(tenantId);

        TenantPayment zeroAmount = new TenantPayment(tenantId, "membership", UUID.randomUUID(),
                BigDecimal.ZERO, PaymentMethod.CASH, Instant.now());

        assertThatThrownBy(() -> tenantPaymentRepository.saveAndFlush(zeroAmount))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    /**
     * Same RLS policy shape as member/membership (tenant-isolation-enforcement.md's pattern) but
     * this is a hand-written copy for a new table - confirms it wasn't mistyped, not just that the
     * mechanism works in the abstract.
     */
    @Test
    void paymentIsInvisibleWithNoTenantContext() throws Exception {
        UUID tenantId = insertTenant();
        TenantContext.setTenantId(tenantId);
        tenantPaymentRepository.save(new TenantPayment(tenantId, "membership", UUID.randomUUID(),
                new BigDecimal("100.00"), PaymentMethod.CARD, Instant.now()));

        TenantContext.clear();
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement statement = connection.prepareStatement("SELECT count(*) FROM tenant_payment");
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
                statement.setString(1, "Billing Test Tenant");
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
