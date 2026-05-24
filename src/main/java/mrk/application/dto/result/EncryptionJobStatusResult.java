package mrk.application.dto.result;

import java.time.Instant;
import java.util.UUID;

public record EncryptionJobStatusResult(
        UUID jobId,
        String status,
        String encryptedText,
        String failureReason,
        Instant createdAt,
        Instant processedAt
) {
}