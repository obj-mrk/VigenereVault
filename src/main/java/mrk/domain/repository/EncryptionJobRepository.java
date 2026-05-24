package mrk.domain.repository;

import mrk.domain.model.EncryptionJob;

import java.util.Optional;
import java.util.UUID;

public interface EncryptionJobRepository {

    EncryptionJob save(EncryptionJob job);

    Optional<EncryptionJob> findById(UUID id);
}