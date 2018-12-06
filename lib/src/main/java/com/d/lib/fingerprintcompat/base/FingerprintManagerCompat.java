package com.d.lib.fingerprintcompat.base;

import android.content.Context;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build.VERSION;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.annotation.RequiresPermission;
import android.support.v4.os.CancellationSignal;

import java.security.Signature;

import javax.crypto.Cipher;
import javax.crypto.Mac;

public final class FingerprintManagerCompat {
    private final Context mContext;

    @NonNull
    public static FingerprintManagerCompat from(@NonNull Context context) {
        return new FingerprintManagerCompat(context);
    }

    private FingerprintManagerCompat(Context context) {
        this.mContext = context;
    }

    @RequiresPermission("android.permission.USE_FINGERPRINT")
    public boolean hasEnrolledFingerprints() {
        if (VERSION.SDK_INT < 23) {
            return false;
        } else {
            FingerprintManager fp = getFingerprintManagerOrNull(this.mContext);
            return fp != null && fp.hasEnrolledFingerprints();
        }
    }

    @RequiresPermission("android.permission.USE_FINGERPRINT")
    public boolean isHardwareDetected() {
        if (VERSION.SDK_INT < 23) {
            return false;
        } else {
            FingerprintManager fp = getFingerprintManagerOrNull(this.mContext);
            return fp != null && fp.isHardwareDetected();
        }
    }

    @RequiresPermission("android.permission.USE_FINGERPRINT")
    public void authenticate(@Nullable FingerprintManagerCompat.CryptoObject crypto, int flags, @Nullable CancellationSignal cancel, @NonNull FingerprintManagerCompat.AuthenticationCallback callback, @Nullable Handler handler) {
        if (VERSION.SDK_INT >= 23) {
            FingerprintManager fp = getFingerprintManagerOrNull(this.mContext);
            if (fp != null) {
                android.os.CancellationSignal cancellationSignal = cancel != null ? (android.os.CancellationSignal) cancel.getCancellationSignalObject() : null;
                fp.authenticate(wrapCryptoObject(crypto), cancellationSignal, flags, wrapCallback(callback), handler);
            }
        }

    }

    @Nullable
    @RequiresApi(23)
    private static FingerprintManager getFingerprintManagerOrNull(@NonNull Context context) {
        try {
            return (FingerprintManager) context.getSystemService(FingerprintManager.class);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    @RequiresApi(23)
    private static android.hardware.fingerprint.FingerprintManager.CryptoObject wrapCryptoObject(FingerprintManagerCompat.CryptoObject cryptoObject) {
        if (cryptoObject == null) {
            return null;
        } else if (cryptoObject.getCipher() != null) {
            return new android.hardware.fingerprint.FingerprintManager.CryptoObject(cryptoObject.getCipher());
        } else if (cryptoObject.getSignature() != null) {
            return new android.hardware.fingerprint.FingerprintManager.CryptoObject(cryptoObject.getSignature());
        } else {
            return cryptoObject.getMac() != null ? new android.hardware.fingerprint.FingerprintManager.CryptoObject(cryptoObject.getMac()) : null;
        }
    }

    @RequiresApi(23)
    static FingerprintManagerCompat.CryptoObject unwrapCryptoObject(android.hardware.fingerprint.FingerprintManager.CryptoObject cryptoObject) {
        if (cryptoObject == null) {
            return null;
        } else if (cryptoObject.getCipher() != null) {
            return new FingerprintManagerCompat.CryptoObject(cryptoObject.getCipher());
        } else if (cryptoObject.getSignature() != null) {
            return new FingerprintManagerCompat.CryptoObject(cryptoObject.getSignature());
        } else {
            return cryptoObject.getMac() != null ? new FingerprintManagerCompat.CryptoObject(cryptoObject.getMac()) : null;
        }
    }

    @RequiresApi(23)
    private static android.hardware.fingerprint.FingerprintManager.AuthenticationCallback wrapCallback(final FingerprintManagerCompat.AuthenticationCallback callback) {
        return new android.hardware.fingerprint.FingerprintManager.AuthenticationCallback() {
            public void onAuthenticationError(int errMsgId, CharSequence errString) {
                callback.onAuthenticationError(errMsgId, errString);
            }

            public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
                callback.onAuthenticationHelp(helpMsgId, helpString);
            }

            public void onAuthenticationSucceeded(android.hardware.fingerprint.FingerprintManager.AuthenticationResult result) {
                callback.onAuthenticationSucceeded(new FingerprintManagerCompat.AuthenticationResult(FingerprintManagerCompat.unwrapCryptoObject(result.getCryptoObject())));
            }

            public void onAuthenticationFailed() {
                callback.onAuthenticationFailed();
            }
        };
    }

    public abstract static class AuthenticationCallback {
        public AuthenticationCallback() {
        }

        public void onAuthenticationError(int errMsgId, CharSequence errString) {
        }

        public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
        }

        public void onAuthenticationSucceeded(FingerprintManagerCompat.AuthenticationResult result) {
        }

        public void onAuthenticationFailed() {
        }
    }

    public static final class AuthenticationResult {
        private final FingerprintManagerCompat.CryptoObject mCryptoObject;

        public AuthenticationResult(FingerprintManagerCompat.CryptoObject crypto) {
            this.mCryptoObject = crypto;
        }

        public FingerprintManagerCompat.CryptoObject getCryptoObject() {
            return this.mCryptoObject;
        }
    }

    public static class CryptoObject {
        private final Signature mSignature;
        private final Cipher mCipher;
        private final Mac mMac;

        public CryptoObject(@NonNull Signature signature) {
            this.mSignature = signature;
            this.mCipher = null;
            this.mMac = null;
        }

        public CryptoObject(@NonNull Cipher cipher) {
            this.mCipher = cipher;
            this.mSignature = null;
            this.mMac = null;
        }

        public CryptoObject(@NonNull Mac mac) {
            this.mMac = mac;
            this.mCipher = null;
            this.mSignature = null;
        }

        @Nullable
        public Signature getSignature() {
            return this.mSignature;
        }

        @Nullable
        public Cipher getCipher() {
            return this.mCipher;
        }

        @Nullable
        public Mac getMac() {
            return this.mMac;
        }
    }
}
