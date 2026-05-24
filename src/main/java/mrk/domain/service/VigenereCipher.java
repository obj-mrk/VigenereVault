package mrk.domain.service;

import mrk.domain.model.VigenereKey;

public interface VigenereCipher {

    String encrypt(String plaintext, VigenereKey key);

    String decrypt(String ciphertext, VigenereKey key);
}