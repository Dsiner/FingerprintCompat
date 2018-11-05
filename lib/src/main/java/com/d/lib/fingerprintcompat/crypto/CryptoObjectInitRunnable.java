package com.d.lib.fingerprintcompat.crypto;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat;

import com.d.lib.fingerprintcompat.base.Mode;

class CryptoObjectInitRunnable implements Runnable {
    private final static Handler mMainHandler = new Handler(Looper.getMainLooper());

    private final Mode mMode;
    private final CryptoFactory mCryptoFactory;
    private final String mKeyName;
    private final AsyncCryptoFactory.Callback mCallback;

    CryptoObjectInitRunnable(Mode mode, CryptoFactory cryptoFactory, String keyName,
                             @NonNull AsyncCryptoFactory.Callback callback) {
        this.mCryptoFactory = cryptoFactory;
        this.mKeyName = keyName;
        this.mMode = mode;
        this.mCallback = callback;
    }

    @Override
    public void run() {
        final FingerprintManagerCompat.CryptoObject cryptoObject;
        switch (mMode) {
            case AUTHENTICATION:
                cryptoObject = mCryptoFactory.createAuthenticationCryptoObject(mKeyName);
                break;
            case DECRYPTION:
                cryptoObject = mCryptoFactory.createDecryptionCryptoObject(mKeyName);
                break;
            case ENCRYPTION:
                cryptoObject = mCryptoFactory.createEncryptionCryptoObject(mKeyName);
                break;
            default:
                cryptoObject = null;
                break;
        }

        if (!mCallback.isCanceled()) {
            /* Return callback back to main thread as this is executed in the background */
            mMainHandler.post(new Runnable() {
                @Override
                public void run() {
                    mCallback.onCryptoObjectCreated(cryptoObject);
                }
            });
        }
    }
}
