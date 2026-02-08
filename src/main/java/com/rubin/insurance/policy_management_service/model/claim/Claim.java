package com.rubin.insurance.policy_management_service.model.claim;

import com.rubin.insurance.policy_management_service.configuration.exception_handling.BadRequestException;
import com.rubin.insurance.policy_management_service.configuration.exception_handling.BusinessException;
import com.rubin.insurance.policy_management_service.model.common.BaseEntity;
import com.rubin.insurance.policy_management_service.model.policy.Policy;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
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
        name = "claims",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_claim_number", columnNames = "claim_number")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Claim extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "claim_id_seq")
    @SequenceGenerator(name = "claim_id_seq", sequenceName = "claim_id_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "policy_id", nullable = false)
    private Policy policy;

    @Column(name = "claim_number", nullable = false, unique = true, updatable = false, length = 20)
    private String claimNumber;

    @Column(name = "description", nullable = false, length = 500)
    private String description;

    @Column(name = "claim_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal claimAmount;

    @Column(name = "incident_date", nullable = false)
    private LocalDate incidentDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 10)
    @Builder.Default
    private ClaimStatus status = ClaimStatus.SUBMITTED;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;


    @PrePersist
    private void prePersist() {
        if (claimNumber == null || claimNumber.isBlank()) {
            claimNumber = generateClaimNumber();
        }
    }

    private String generateClaimNumber() {
        int year = Year.now().getValue();
        long sequence = id != null ? id : System.nanoTime() % 1_000_000L;
        return String.format("CLM-%d-%06d", year, Math.abs(sequence));
    }

    public void setStatus(ClaimStatus status) {
        if (this.status == ClaimStatus.APPROVED || this.status == ClaimStatus.REJECTED) {
                throw new BusinessException("Approved or rejected claims cannot change status.");
        }
        this.status = status;
    }
}
