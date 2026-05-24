package mrk.infrastructure.persistence.repository;

import mrk.infrastructure.persistence.entity.EncryptionJobJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SpringDataEncryptionJobRepository extends JpaRepository<EncryptionJobJpaEntity, UUID> {
}