package com.rubin.insurance.policy_management_service.model.policy;

import com.rubin.insurance.policy_management_service.configuration.exception_handling.BusinessException;
import com.rubin.insurance.policy_management_service.model.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Year;

@Entity
@Table(
        name = "policies",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_policy_number", columnNames = "policy_number")
        },
        indexes = {
                @Index(name = "idx_policy_customer_email", columnList = "customer_email"),
                @Index(name = "idx_policy_status", columnList = "status")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Policy extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "policy_id_seq")
    @SequenceGenerator(name = "policy_id_seq", sequenceName = "policy_id_seq", allocationSize = 1)
    private Long id;

    @Column(name = "policy_number", nullable = false, unique = true, updatable = false, length = 20)
    private String policyNumber;

    @Column(name = "customer_name", nullable = false)
    private String customerName;

    @Column(name = "customer_email", nullable = false)
    private String customerEmail;

    @Enumerated(EnumType.STRING)
    @Column(name = "policy_type", nullable = false, length = 20)
    private PolicyType policyType;

    @Column(name = "coverage_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal coverageAmount;

    @Column(name = "premium_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal premiumAmount;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private PolicyStatus status = PolicyStatus.ACTIVE;

    @PrePersist
    private void prePersist() {
        if (this.policyNumber == null || this.policyNumber.isBlank()) {
            this.policyNumber = generatePolicyNumber();
        }
    }

    private String generatePolicyNumber() {
        int year = Year.now().getValue();
        long sequence = id != null ? id : System.nanoTime() % 1_000_000L;
        return String.format("POL-%d-%06d", year, Math.abs(sequence));
    }


    public void renew(){
        if (this.endDate.isAfter(LocalDate.now()) && this.status.equals(PolicyStatus.ACTIVE)) {
            throw new BusinessException("You can't renew until the first period finishes");
        }
        this.startDate = LocalDate.now();
        this.endDate = LocalDate.now().plusMonths(6);
    }

    public void cancel() {
        if (status != PolicyStatus.ACTIVE) {
            throw new BusinessException("Only ACTIVE policies can be cancelled.");
        }
        this.status = PolicyStatus.CANCELLED;
    }
}
