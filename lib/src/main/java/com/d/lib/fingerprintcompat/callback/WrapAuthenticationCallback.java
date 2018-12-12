package com.d.lib.fingerprintcompat.callback;

import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;

import com.d.lib.fingerprintcompat.FingerprintCompat;
import com.d.lib.fingerprintcompat.base.FingerprintManagerCompat;
import com.d.lib.fingerprintcompat.base.Mode;
import com.d.lib.fingerprintcompat.crypto.Crypto;

@RequiresApi(api = Build.VERSION_CODES.M)
public class WrapAuthenticationCallback extends CancellableAuthenticationCallback<FingerprintManagerCompat.AuthenticationCallback> {

    public WrapAuthenticationCallback(Mode mode, Crypto crypto, String value, @Nullable FingerprintManagerCompat.AuthenticationCallback callback) {
        super(mode, crypto, value, callback);
    }

    @Override
    public void onAuthenticationError(int errMsgId, CharSequence errString) {
        FingerprintCompat.d("onAuthenticationError-->");
        if (!shouldReactToError(errMsgId)) {
            return;
        }
        FingerprintCompat.d("<--onAuthenticationError");
        FingerprintCompat.e("Error " + errMsgId + " : " + errString);
        if (mCallback != null) {
            mCallback.onAuthenticationError(errMsgId, errString);
        }
    }

    @Override
    public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
        FingerprintCompat.d("onAuthenticationHelp-->");
        if (mCancellationSignal.isCanceled()) {
            return;
        }
        FingerprintCompat.d("<--onAuthenticationHelp");
        FingerprintCompat.d("Help " + helpMsgId + " : " + helpString);
        if (mCallback != null) {
            mCallback.onAuthenticationHelp(helpMsgId, helpString);
        }
    }

    @Override
    public void onAuthenticationSucceeded(FingerprintManagerCompat.AuthenticationResult result) {
        FingerprintCompat.d("onAuthenticationSucceeded-->");
        if (mCancellationSignal.isCanceled()) {
            return;
        }
        FingerprintCompat.d("<--Successful authentication");
        if (mCallback != null) {
            mCallback.onAuthenticationSucceeded(result);
        }
    }

    @Override
    public void onAuthenticationFailed() {
        FingerprintCompat.d("onAuthenticationFailed-->");
        if (mCancellationSignal.isCanceled()) {
            return;
        }
        FingerprintCompat.d("<--Failed authentication");
        FingerprintCompat.e("Error " + FINGERPRINT_NOT_RECOGNIZED
                + " : " + "Fingerprint not recognized. Try again.");
        if (mCallback != null) {
            mCallback.onAuthenticationFailed();
        }
    }
}
