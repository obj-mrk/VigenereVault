package mrk.infrastructure.persistence.mapper;

import mrk.domain.model.EncryptionJob;
import mrk.infrastructure.persistence.entity.EncryptionJobJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class EncryptionJobMapper {

    public EncryptionJob toDomain(EncryptionJobJpaEntity entity) {
        return new EncryptionJob(
                entity.getId(),
                entity.getPlaintextPayload(),
                entity.getEncryptedPayload(),
                entity.getStatus(),
                entity.getFailureReason(),
                entity.getCreatedAt(),
                entity.getProcessedAt()
        );
    }

    public EncryptionJobJpaEntity toJpaEntity(EncryptionJob job) {
        return EncryptionJobJpaEntity.builder()
                .id(job.getId())
                .plaintextPayload(job.getPlaintextPayload())
                .encryptedPayload(job.getEncryptedPayload())
                .status(job.getStatus())
                .failureReason(job.getFailureReason())
                .createdAt(job.getCreatedAt())
                .processedAt(job.getProcessedAt())
                .build();
    }
}