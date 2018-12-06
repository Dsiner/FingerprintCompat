package com.d.lib.fingerprintcompat.callback;

import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;

import com.d.lib.fingerprintcompat.FingerprintCompat;
import com.d.lib.fingerprintcompat.base.FingerprintException;
import com.d.lib.fingerprintcompat.base.IFingerprint;
import com.d.lib.fingerprintcompat.base.Mode;
import com.d.lib.fingerprintcompat.crypto.Crypto;

@RequiresApi(api = Build.VERSION_CODES.M)
public class SimpleAuthenticationCallback extends CancellableAuthenticationCallback<IFingerprint.Callback> {

    public SimpleAuthenticationCallback(Mode mode, Crypto crypto, String value,
                                        @Nullable IFingerprint.Callback callback) {
        super(mode, crypto, value, callback);
    }

    @Override
    public void onAuthenticationError(int errMsgId, CharSequence errString) {
        if (!shouldReactToError(errMsgId)) {
            return;
        }
        onError(errMsgId, errString);
    }

    @Override
    public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
        if (mCancellationSignal.isCanceled()) {
            return;
        }
        onError(helpMsgId, helpString);
    }

    @Override
    public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
        if (mCancellationSignal.isCanceled()) {
            return;
        }
        FingerprintCompat.d("Successful authentication");
        if (mMode == Mode.AUTHENTICATION) {
            if (mCallback != null) {
                mCallback.onSuccess("");
            }
        } else {
            cipherValue(result.getCryptoObject(), mValue);
        }
    }

    @Override
    public void onAuthenticationFailed() {
        if (!mCancellationSignal.isCanceled()) {
            onError(-1, "Fingerprint not recognized. Try again.");
        }
    }

    private void cipherValue(FingerprintManager.CryptoObject cryptoObject, String value) {
        String cipheredValue = null;
        switch (mMode) {
            case DECRYPTION:
                cipheredValue = mCrypto.decrypt(cryptoObject, value);
                break;
            case ENCRYPTION:
                cipheredValue = mCrypto.encrypt(cryptoObject, value);
                break;
        }

        if (cipheredValue != null) {
            FingerprintCompat.d("Ciphered [" + value + "] => [" + cipheredValue + "]");
            if (mCallback != null) {
                mCallback.onSuccess(cipheredValue);
            }
        } else {
            String error = (mMode == Mode.DECRYPTION) ? "decryption failed" : "encryption failed";
            onError(-1, error);
        }
    }

    private void onError(int error, CharSequence errString) {
        FingerprintCompat.e("Error " + error);
        if (mCallback != null) {
            mCallback.onError(new FingerprintException(error, errString));
        }
    }
}
