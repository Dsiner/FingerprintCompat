package com.d.lib.fingerprintcompat.callback;

import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat;

import com.d.lib.fingerprintcompat.FingerprintCompat;
import com.d.lib.fingerprintcompat.base.Mode;
import com.d.lib.fingerprintcompat.crypto.Crypto;

@RequiresApi(api = Build.VERSION_CODES.M)
public class WrapAuthenticationCallback extends CancellableAuthenticationCallback<FingerprintManagerCompat.AuthenticationCallback> {

    public WrapAuthenticationCallback(Mode mode, Crypto crypto, String value, @Nullable FingerprintManagerCompat.AuthenticationCallback callback) {
        super(mode, crypto, value, callback);
    }

    @Override
    public void onAuthenticationError(int errMsgId, CharSequence errString) {
        if (!shouldReactToError(errMsgId)) {
            return;
        }
        if (mCallback != null) {
            mCallback.onAuthenticationError(errMsgId, errString);
        }
    }

    @Override
    public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
        if (mCancellationSignal.isCanceled()) {
            return;
        }
        if (mCallback != null) {
            mCallback.onAuthenticationHelp(helpMsgId, helpString);
        }
    }

    @Override
    public void onAuthenticationSucceeded(FingerprintManagerCompat.AuthenticationResult result) {
        if (mCancellationSignal.isCanceled()) {
            return;
        }
        FingerprintCompat.d("Successful authentication");
        if (mCallback != null) {
            mCallback.onAuthenticationSucceeded(result);
        }
    }

    @Override
    public void onAuthenticationFailed() {
        if (mCancellationSignal.isCanceled()) {
            return;
        }
        if (mCallback != null) {
            mCallback.onAuthenticationFailed();
        }
    }
}
