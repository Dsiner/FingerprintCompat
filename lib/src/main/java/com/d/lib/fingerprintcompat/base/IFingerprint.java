package com.d.lib.fingerprintcompat.base;

import android.support.annotation.NonNull;

import com.d.lib.fingerprintcompat.crypto.Crypto;

/**
 * IFingerprint
 * Created by D on 2018/11/5.
 **/
public interface IFingerprint {

    /**
     * Returns true if device has fingerprint hardware, false otherwise.
     */
    boolean isHardwareDetected();

    /**
     * Returns true if user has fingerprint in device settings, false otherwise.
     */
    boolean hasEnrolledFingerprints();

    /**
     * Authenticate user via Fingerprint.
     * <p>
     * Example - Process payment after successful fingerprint authentication.
     */
    void authenticate(Callback callback);

    /**
     * Authenticate user via Fingerprint. If user is successfully authenticated,
     * {@link Crypto} implementation is used to automatically decrypt given value.
     * <p>
     * Should be used together with {@link IFingerprint#encrypt(String, String, Callback)} to decrypt saved data.
     *
     * @param keyName unique key identifier, {@link java.security.Key} saved under this value is loaded from {@link java.security.KeyStore}
     * @param value   String value which will be decrypted if user successfully authenticates
     */
    void decrypt(String keyName, String value, Callback callback);

    /**
     * Authenticate user via Fingerprint. If user is successfully authenticated,
     * {@link Crypto} implementation is used to automatically encrypt given value.
     * <p>
     * Use it when saving some data that should not be saved as plain text (e.g. password).
     * To decrypt the value use {@link IFingerprint#decrypt(String, String, Callback)} method.
     * <p>
     * Example - Allow auto-login via Fingerprint.
     *
     * @param keyName unique key identifier, {@link java.security.Key} is stored to {@link java.security.KeyStore} under this value
     * @param value   String value which will be encrypted if user successfully authenticates
     */
    void encrypt(String keyName, String value, Callback callback);

    /**
     * Cancel current active Fingerprint authentication.
     */
    void cancel();

    abstract class Callback {

        /**
         * Callback is dispatched after {@link com.d.lib.fingerprintcompat.base.FingerprintManagerCompat.CryptoObject} is
         * initialized and just before Fingerprint authentication is started.
         * <p>
         * Example - You want to display Dialog only if initialization is successful.
         */
        public void onReady() {

        }

        /**
         * User successfully authenticated.
         *
         * @param value This value can be one of:
         *              1) Empty string - if {@link #authenticate(Callback)} is used
         *              2) Encrypted string - if {@link #encrypt(String, String, Callback)} is used
         *              3) Decrypted string - if {@link #decrypt(String, String, Callback)} is used
         */
        abstract public void onSuccess(String value);

        abstract public void onError(@NonNull FingerprintException e);
    }
}
