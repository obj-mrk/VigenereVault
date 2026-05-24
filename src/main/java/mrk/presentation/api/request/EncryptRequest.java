package mrk.presentation.api.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record EncryptRequest(
        @NotBlank
        @Size(max = 10000)
        String text,

        @NotBlank
        @Pattern(regexp = "^[A-Za-z]+$")
        @Size(min = 1, max = 128)
        String key
) {
}