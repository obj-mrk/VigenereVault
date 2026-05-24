package mrk.infrastructure.persistence.repository;

import lombok.RequiredArgsConstructor;
import mrk.domain.model.EncryptionJob;
import mrk.domain.repository.EncryptionJobRepository;
import mrk.infrastructure.persistence.entity.EncryptionJobJpaEntity;
import mrk.infrastructure.persistence.mapper.EncryptionJobMapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class JpaEncryptionJobRepository implements EncryptionJobRepository {

    private final SpringDataEncryptionJobRepository springDataRepository;
    private final EncryptionJobMapper mapper;

    @Override
    public EncryptionJob save(EncryptionJob job) {
        EncryptionJobJpaEntity entity = springDataRepository.findById(job.getId())
                .orElseGet(EncryptionJobJpaEntity::new);

        entity.setId(job.getId());
        entity.setPlaintextPayload(job.getPlaintextPayload());
        entity.setEncryptedPayload(job.getEncryptedPayload());
        entity.setStatus(job.getStatus());
        entity.setFailureReason(job.getFailureReason());
        entity.setCreatedAt(job.getCreatedAt());
        entity.setProcessedAt(job.getProcessedAt());

        EncryptionJobJpaEntity saved = springDataRepository.saveAndFlush(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<EncryptionJob> findById(UUID id) {
        return springDataRepository.findById(id).map(mapper::toDomain);
    }
}