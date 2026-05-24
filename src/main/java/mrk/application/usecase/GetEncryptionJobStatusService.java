package mrk.application.usecase;

import lombok.RequiredArgsConstructor;
import mrk.application.dto.result.EncryptionJobStatusResult;
import mrk.application.port.in.GetEncryptionJobStatusUseCase;
import mrk.domain.exception.JobNotFoundException;
import mrk.domain.model.EncryptionJob;
import mrk.domain.repository.EncryptionJobRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetEncryptionJobStatusService implements GetEncryptionJobStatusUseCase {

    private final EncryptionJobRepository repository;

    @Override
    @Transactional(readOnly = true)
    public EncryptionJobStatusResult getStatus(UUID jobId) {
        EncryptionJob job = repository.findById(jobId)
                .orElseThrow(() -> new JobNotFoundException(jobId));

        return new EncryptionJobStatusResult(
                job.getId(),
                job.getStatus().name(),
                job.getEncryptedPayload(),
                job.getFailureReason(),
                job.getCreatedAt(),
                job.getProcessedAt()
        );
    }
}