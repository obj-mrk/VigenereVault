package mrk.presentation.api.response;

import java.util.UUID;

public record EncryptResponse(UUID jobId, String status) {
}