package mrk.domain.exception;

import java.util.UUID;

public class JobNotFoundException extends DomainException {

    public JobNotFoundException(UUID jobId) {
        super("Encryption job not found: " + jobId);
    }
}