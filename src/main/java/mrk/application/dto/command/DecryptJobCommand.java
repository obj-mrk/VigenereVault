package mrk.application.dto.command;

import java.util.UUID;

public record DecryptJobCommand(UUID jobId, String key) {
}