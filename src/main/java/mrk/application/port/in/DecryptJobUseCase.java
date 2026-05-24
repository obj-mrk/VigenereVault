package mrk.application.port.in;

import mrk.application.dto.command.DecryptJobCommand;
import mrk.application.dto.result.DecryptJobResult;

public interface DecryptJobUseCase {

    DecryptJobResult decrypt(DecryptJobCommand command);
}