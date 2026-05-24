package mrk.domain.exception;

import mrk.domain.model.JobStatus;
import java.util.UUID;

public class InvalidJobStateException extends DomainException {

    public InvalidJobStateException(UUID jobId, JobStatus currentStatus, String expectedState) {
        super("Job " + jobId + " has status " + currentStatus + ", expected: " + expectedState);
    }
}