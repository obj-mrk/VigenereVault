package mrk.presentation.api.response;

import java.util.UUID;

public record DecryptResponse(UUID jobId, String decryptedText) {
}