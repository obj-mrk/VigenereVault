package mrk.infrastructure.crypto;

import mrk.domain.model.VigenereKey;
import mrk.domain.service.VigenereCipher;
import org.springframework.stereotype.Component;

@Component
public class VigenereCipherService implements VigenereCipher {

    @Override
    public String encrypt(String plaintext, VigenereKey key) {
        validateText(plaintext);
        return transform(plaintext, key.value(), true);
    }

    @Override
    public String decrypt(String ciphertext, VigenereKey key) {
        validateText(ciphertext);
        return transform(ciphertext, key.value(), false);
    }

    // Общий алгоритм для encrypt/decrypt.
    // Нелатинские символы не меняются и не двигают индекс ключа — это соответствует исходной реализации. [file:1]
    private String transform(String input, String key, boolean encryptMode) {
        StringBuilder result = new StringBuilder();
        int keyIndex = 0;

        for (char currentChar : input.toCharArray()) {
            if (isLatinLetter(currentChar)) {
                int shift = key.charAt(keyIndex % key.length()) - 'A';
                result.append(encryptMode ? shiftChar(currentChar, shift) : unshiftChar(currentChar, shift));
                keyIndex++;
            } else {
                result.append(currentChar);
            }
        }

        return result.toString();
    }

    private void validateText(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Text cannot be empty");
        }
    }

    private boolean isLatinLetter(char ch) {
        return (ch >= 'A' && ch <= 'Z') || (ch >= 'a' && ch <= 'z');
    }

    private char shiftChar(char character, int shift) {
        if (Character.isUpperCase(character)) {
            return (char) ((character - 'A' + shift) % 26 + 'A');
        }
        return (char) ((character - 'a' + shift) % 26 + 'a');
    }

    private char unshiftChar(char character, int shift) {
        if (Character.isUpperCase(character)) {
            return (char) ((character - 'A' - shift + 26) % 26 + 'A');
        }
        return (char) ((character - 'a' - shift + 26) % 26 + 'a');
    }
}