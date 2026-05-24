package mrk.application.dto.result;

import java.util.UUID;

public record DecryptJobResult(UUID jobId, String decryptedText) {
}