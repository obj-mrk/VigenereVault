package mrk.infrastructure.async;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mrk.application.port.out.RunEncryptionJobPort;
import mrk.domain.exception.JobNotFoundException;
import mrk.domain.model.EncryptionJob;
import mrk.domain.model.VigenereKey;
import mrk.domain.repository.EncryptionJobRepository;
import mrk.domain.service.VigenereCipher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class EncryptionJobRunner implements RunEncryptionJobPort {

    private final EncryptionJobRepository repository;
    private final VigenereCipher vigenereCipher;

    @Override
    @Async("encryptionTaskExecutor")
    @Transactional
    public void run(UUID jobId, String key) {
        EncryptionJob job = repository.findById(jobId)
                .orElseThrow(() -> new JobNotFoundException(jobId));

        try {
            job.markProcessing();
            String encrypted = vigenereCipher.encrypt(job.getPlaintextPayload(), new VigenereKey(key));
            job.markCompleted(encrypted);
            repository.save(job);
            log.info("Encryption job completed {}", jobId);
        } catch (Exception ex) {
            job.markFailed(ex.getMessage());
            repository.save(job);
            log.error("Encryption job failed {}", jobId, ex);
        }
    }
}