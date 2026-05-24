package mrk.application.port.in;

import mrk.application.dto.command.CreateEncryptionJobCommand;
import mrk.application.dto.result.SubmitEncryptionJobResult;

public interface SubmitEncryptionJobUseCase {

    SubmitEncryptionJobResult submit(CreateEncryptionJobCommand command);
}