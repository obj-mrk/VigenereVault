package mrk.application.usecase;

import lombok.RequiredArgsConstructor;
import mrk.application.dto.command.CreateEncryptionJobCommand;
import mrk.application.dto.result.SubmitEncryptionJobResult;
import mrk.application.event.EncryptionJobSubmittedEvent;
import mrk.application.port.in.SubmitEncryptionJobUseCase;
import mrk.domain.model.EncryptionJob;
import mrk.domain.model.VigenereKey;
import mrk.domain.repository.EncryptionJobRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SubmitEncryptionJobService implements SubmitEncryptionJobUseCase {

    private final EncryptionJobRepository repository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public SubmitEncryptionJobResult submit(CreateEncryptionJobCommand command) {
        VigenereKey key = new VigenereKey(command.key());

        EncryptionJob job = EncryptionJob.createNew(UUID.randomUUID(), command.text());
        repository.save(job);

        eventPublisher.publishEvent(new EncryptionJobSubmittedEvent(job.getId(), key.value()));

        return new SubmitEncryptionJobResult(job.getId(), job.getStatus().name());
    }
}