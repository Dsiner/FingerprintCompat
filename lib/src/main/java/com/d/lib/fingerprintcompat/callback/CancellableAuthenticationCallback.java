package com.d.lib.fingerprintcompat.callback;

import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.CancellationSignal;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;

import com.d.lib.fingerprintcompat.base.Mode;
import com.d.lib.fingerprintcompat.crypto.Crypto;

@RequiresApi(api = Build.VERSION_CODES.M)
public class CancellableAuthenticationCallback<T> extends FingerprintManager.AuthenticationCallback {
    protected final Mode mMode;
    protected final Crypto mCrypto;
    protected final String mValue;
    protected final T mCallback;
    protected final CancellationSignal mCancellationSignal;

    public CancellableAuthenticationCallback(Mode mode, Crypto crypto, String value,
                                             @Nullable T callback) {
        this.mMode = mode;
        this.mCrypto = crypto;
        this.mValue = value;
        this.mCallback = callback;
        this.mCancellationSignal = new CancellationSignal();
    }

    @NonNull
    public CancellationSignal getCancellationSignal() {
        return mCancellationSignal;
    }

    public void cancel() {
        if (!mCancellationSignal.isCanceled()) {
            mCancellationSignal.cancel();
        }
    }

    protected boolean shouldReactToError(int error) {
        return !mCancellationSignal.isCanceled()
                && error != FingerprintManager.FINGERPRINT_ERROR_CANCELED;
    }
}
