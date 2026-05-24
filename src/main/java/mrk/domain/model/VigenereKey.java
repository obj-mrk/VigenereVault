package mrk.domain.model;

import mrk.domain.exception.InvalidKeyException;

public record VigenereKey(String value) {

    public VigenereKey {
        if (value == null || value.isBlank()) {
            throw new InvalidKeyException("Key cannot be empty");
        }

        if (!value.matches("^[A-Za-z]{1,128}$")) {
            throw new InvalidKeyException("Key must contain only Latin letters and have length from 1 to 128");
        }

        value = value.toUpperCase();
    }
}