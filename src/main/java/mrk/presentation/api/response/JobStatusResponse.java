package mrk.presentation.api.response;

import java.time.Instant;
import java.util.UUID;

public record JobStatusResponse(
        UUID jobId,
        String status,
        String encryptedText,
        String failureReason,
        Instant createdAt,
        Instant processedAt
) {
}