package mrk.presentation.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import mrk.application.dto.command.CreateEncryptionJobCommand;
import mrk.application.dto.command.DecryptJobCommand;
import mrk.application.dto.result.DecryptJobResult;
import mrk.application.dto.result.EncryptionJobStatusResult;
import mrk.application.dto.result.SubmitEncryptionJobResult;
import mrk.application.port.in.DecryptJobUseCase;
import mrk.application.port.in.GetEncryptionJobStatusUseCase;
import mrk.application.port.in.SubmitEncryptionJobUseCase;
import mrk.presentation.api.request.DecryptRequest;
import mrk.presentation.api.request.EncryptRequest;
import mrk.presentation.api.response.DecryptResponse;
import mrk.presentation.api.response.EncryptResponse;
import mrk.presentation.api.response.JobStatusResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class EncryptionController {

    private final SubmitEncryptionJobUseCase submitEncryptionJobUseCase;
    private final GetEncryptionJobStatusUseCase getEncryptionJobStatusUseCase;
    private final DecryptJobUseCase decryptJobUseCase;

    @PostMapping("/encrypt")
    public ResponseEntity<EncryptResponse> encrypt(@Valid @RequestBody EncryptRequest request) {
        SubmitEncryptionJobResult result = submitEncryptionJobUseCase.submit(
                new CreateEncryptionJobCommand(request.text(), request.key())
        );

        return ResponseEntity.accepted().body(new EncryptResponse(result.jobId(), result.status()));
    }

    @GetMapping("/jobs/{jobId}")
    public ResponseEntity<JobStatusResponse> status(@PathVariable("jobId") UUID jobId) {
        EncryptionJobStatusResult result = getEncryptionJobStatusUseCase.getStatus(jobId);

        return ResponseEntity.ok(new JobStatusResponse(
                result.jobId(),
                result.status(),
                result.encryptedText(),
                result.failureReason(),
                result.createdAt(),
                result.processedAt()
        ));
    }

    @PostMapping("/decrypt/{jobId}")
    public ResponseEntity<DecryptResponse> decrypt(
            @PathVariable("jobId") UUID jobId,
            @Valid @RequestBody DecryptRequest request
    ) {
        DecryptJobResult result = decryptJobUseCase.decrypt(new DecryptJobCommand(jobId, request.key()));
        return ResponseEntity.ok(new DecryptResponse(result.jobId(), result.decryptedText()));
    }
}