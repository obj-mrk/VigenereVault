package mrk.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import mrk.domain.model.JobStatus;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "encryption_jobs", indexes = {
        @Index(name = "idx_encryption_jobs_status", columnList = "status"),
        @Index(name = "idx_encryption_jobs_created_at", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EncryptionJobJpaEntity {

    @Id
    private UUID id;

    @Version
    private Long version;

    @Column(name = "plaintext_payload", length = 10000)
    private String plaintextPayload;

    @Column(name = "encrypted_payload", length = 10000)
    private String encryptedPayload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private JobStatus status;

    @Column(name = "failure_reason", length = 1000)
    private String failureReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "processed_at")
    private Instant processedAt;
}