package com.d.lib.fingerprintcompat.crypto;

import android.support.annotation.Nullable;
import android.util.Base64;

import com.d.lib.fingerprintcompat.FingerprintCompat;
import com.d.lib.fingerprintcompat.base.FingerprintManagerCompat;
import com.d.lib.fingerprintcompat.base.IFingerprint;

/**
 * Interface implements crypto operations on given value when using
 * {@link FingerprintCompat#decrypt(String, String, IFingerprint.Callback)} or
 * {@link FingerprintCompat#encrypt(String, String, IFingerprint.Callback)} methods.
 */
public interface Crypto {

    /**
     * Encrypt value with unlocked cryptoObject. Return null if encryption fails.
     */
    @Nullable
    String encrypt(FingerprintManagerCompat.CryptoObject cryptoObject, String value);

    /**
     * Decrypt value with unlocked cryptoObject. Return null if decryption fails.
     */
    @Nullable
    String decrypt(FingerprintManagerCompat.CryptoObject cryptoObject, String value);

    class Default implements Crypto {

        @Nullable
        @Override
        public String decrypt(FingerprintManagerCompat.CryptoObject cryptoObject, String value) {
            try {
                byte[] decodedBytes = Base64.decode(value, Base64.DEFAULT);
                return new String(cryptoObject.getCipher().doFinal(decodedBytes));
            } catch (Throwable e) {
                FingerprintCompat.e(e.toString());
                return null;
            }
        }

        @Nullable
        @Override
        public String encrypt(FingerprintManagerCompat.CryptoObject cryptoObject, String value) {
            try {
                byte[] encryptedBytes = cryptoObject.getCipher().doFinal(value.getBytes());
                return Base64.encodeToString(encryptedBytes, Base64.DEFAULT);
            } catch (Throwable e) {
                FingerprintCompat.e(e.toString());
                return null;
            }
        }
    }
}
