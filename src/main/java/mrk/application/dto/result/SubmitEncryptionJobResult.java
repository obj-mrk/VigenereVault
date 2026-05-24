package mrk.application.dto.result;

import java.util.UUID;

public record SubmitEncryptionJobResult(UUID jobId, String status) {
}