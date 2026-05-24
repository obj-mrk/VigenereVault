package mrk.application.port.out;

import java.util.UUID;

public interface RunEncryptionJobPort {

    void run(UUID jobId, String key);
}