package mrk.domain.model;

import mrk.domain.exception.InvalidJobStateException;

import java.time.Instant;
import java.util.UUID;

public class EncryptionJob {

    private final UUID id;
    private String plaintextPayload;
    private String encryptedPayload;
    private JobStatus status;
    private String failureReason;
    private final Instant createdAt;
    private Instant processedAt;

    public EncryptionJob(
            UUID id,
            String plaintextPayload,
            String encryptedPayload,
            JobStatus status,
            String failureReason,
            Instant createdAt,
            Instant processedAt
    ) {
        this.id = id;
        this.plaintextPayload = plaintextPayload;
        this.encryptedPayload = encryptedPayload;
        this.status = status;
        this.failureReason = failureReason;
        this.createdAt = createdAt;
        this.processedAt = processedAt;
    }

    // Use case: создание новой задачи на шифрование.
    // Доменная модель сразу фиксирует начальное состояние RECEIVED.
    public static EncryptionJob createNew(UUID id, String plaintextPayload) {
        if (plaintextPayload == null || plaintextPayload.isBlank()) {
            throw new IllegalArgumentException("Text cannot be empty");
        }

        if (plaintextPayload.length() > 10_000) {
            throw new IllegalArgumentException("Text length must be <= 10000");
        }

        return new EncryptionJob(
                id,
                plaintextPayload,
                null,
                JobStatus.RECEIVED,
                null,
                Instant.now(),
                null
        );
    }

    // Use case: воркер начинает обработку задачи.
    // Переход допустим только из RECEIVED, чтобы избежать некорректных повторных запусков.
    public void markProcessing() {
        if (status != JobStatus.RECEIVED) {
            throw new InvalidJobStateException(id, status, "RECEIVED");
        }
        this.status = JobStatus.PROCESSING;
    }

    // Use case: успешное завершение асинхронной обработки.
    // Plaintext зануляется, чтобы не хранить исходный текст дольше необходимого.
    public void markCompleted(String encryptedPayload) {
        if (status != JobStatus.RECEIVED && status != JobStatus.PROCESSING) {
            throw new InvalidJobStateException(id, status, "RECEIVED or PROCESSING");
        }
        this.encryptedPayload = encryptedPayload;
        this.plaintextPayload = null;
        this.failureReason = null;
        this.status = JobStatus.COMPLETED;
        this.processedAt = Instant.now();
    }

    // Use case: фиксация ошибки обработки для последующей диагностики через status endpoint.
    public void markFailed(String failureReason) {
        this.failureReason = failureReason;
        this.status = JobStatus.FAILED;
        this.processedAt = Instant.now();
    }

    // Use case: дешифровать можно только завершённую задачу, у которой есть ciphertext.
    public String requireEncryptedPayloadForDecryption() {
        if (status != JobStatus.COMPLETED) {
            throw new InvalidJobStateException(id, status, "COMPLETED");
        }
        if (encryptedPayload == null || encryptedPayload.isBlank()) {
            throw new IllegalStateException("Encrypted payload is empty");
        }
        return encryptedPayload;
    }

    public UUID getId() {
        return id;
    }

    public String getPlaintextPayload() {
        return plaintextPayload;
    }

    public String getEncryptedPayload() {
        return encryptedPayload;
    }

    public JobStatus getStatus() {
        return status;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getProcessedAt() {
        return processedAt;
    }
}