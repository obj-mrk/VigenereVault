package mrk.application.event;

import java.util.UUID;

public record EncryptionJobSubmittedEvent(UUID jobId, String key) {
}