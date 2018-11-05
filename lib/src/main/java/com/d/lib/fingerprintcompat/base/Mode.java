package com.d.lib.fingerprintcompat.base;

import javax.crypto.Cipher;

public enum Mode {
    AUTHENTICATION(Cipher.ENCRYPT_MODE),
    DECRYPTION(Cipher.DECRYPT_MODE),
    ENCRYPTION(Cipher.ENCRYPT_MODE);

    private final int cipherMode;

    Mode(int cipherMode) {
        this.cipherMode = cipherMode;
    }

    public int cipherMode() {
        return cipherMode;
    }
}
