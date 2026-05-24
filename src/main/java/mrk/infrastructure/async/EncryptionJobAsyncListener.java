package mrk.infrastructure.async;

import lombok.RequiredArgsConstructor;
import mrk.application.event.EncryptionJobSubmittedEvent;
import mrk.application.port.out.RunEncryptionJobPort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class EncryptionJobAsyncListener {

    private final RunEncryptionJobPort runEncryptionJobPort;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onEncryptionJobSubmitted(EncryptionJobSubmittedEvent event) {
        runEncryptionJobPort.run(event.jobId(), event.key());
    }
}