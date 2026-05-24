package mrk.application.usecase;

import lombok.RequiredArgsConstructor;
import mrk.application.dto.command.DecryptJobCommand;
import mrk.application.dto.result.DecryptJobResult;
import mrk.application.port.in.DecryptJobUseCase;
import mrk.domain.exception.JobNotFoundException;
import mrk.domain.model.EncryptionJob;
import mrk.domain.model.VigenereKey;
import mrk.domain.repository.EncryptionJobRepository;
import mrk.domain.service.VigenereCipher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DecryptJobService implements DecryptJobUseCase {

    private final EncryptionJobRepository repository;
    private final VigenereCipher vigenereCipher;

    @Override
    @Transactional(readOnly = true)
    public DecryptJobResult decrypt(DecryptJobCommand command) {
        EncryptionJob job = repository.findById(command.jobId())
                .orElseThrow(() -> new JobNotFoundException(command.jobId()));

        VigenereKey key = new VigenereKey(command.key());

        // Use case: дешифрование разрешено только для завершённой задачи.
        String encryptedPayload = job.requireEncryptedPayloadForDecryption();
        String decrypted = vigenereCipher.decrypt(encryptedPayload, key);

        return new DecryptJobResult(job.getId(), decrypted);
    }
}