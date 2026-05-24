package mrk.application.port.in;

import mrk.application.dto.result.EncryptionJobStatusResult;

import java.util.UUID;

public interface GetEncryptionJobStatusUseCase {

    EncryptionJobStatusResult getStatus(UUID jobId);
}