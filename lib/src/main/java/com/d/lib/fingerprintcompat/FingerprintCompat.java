package com.d.lib.fingerprintcompat;

import android.annotation.SuppressLint;
import android.app.KeyguardManager;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.widget.Toast;

import com.d.lib.fingerprintcompat.base.FingerprintException;
import com.d.lib.fingerprintcompat.base.FingerprintManagerCompat;
import com.d.lib.fingerprintcompat.base.IFingerprint;
import com.d.lib.fingerprintcompat.base.Mode;
import com.d.lib.fingerprintcompat.callback.CancellableAuthenticationCallback;
import com.d.lib.fingerprintcompat.callback.SimpleAuthenticationCallback;
import com.d.lib.fingerprintcompat.callback.WrapAuthenticationCallback;
import com.d.lib.fingerprintcompat.crypto.AsyncCryptoFactory;
import com.d.lib.fingerprintcompat.crypto.Crypto;
import com.d.lib.fingerprintcompat.crypto.CryptoFactory;

/**
 * FingerprintCompat
 * Created by D on 2018/11/2.
 **/
public class FingerprintCompat implements IFingerprint {
    public final static String TAG = "FingerprintCompat";
    private static boolean DEBUG = false;

    private static final String KEY_AUTH_MODE = "<FingerprintCompat authentication mode>";

    private final Handler mMainHandler = new Handler(Looper.getMainLooper());

    private Context mContext;
    private FingerprintManagerCompat mFingerprintManagerCompat;
    private AsyncCryptoFactory mAsyncCryptoFactory;
    private Crypto mCrypto;
    private AsyncCryptoFactory.Callback mAsyncCryptoFactoryCallback;
    private CancellableAuthenticationCallback mCancellableAuthenticationCallback;

    public static FingerprintCompat create(Context context) {
        return new Builder(context).build();
    }

    FingerprintCompat(Context context) {
        this.mContext = context;
    }

    FingerprintCompat(Context context, AsyncCryptoFactory asyncCryptoFactory, Crypto crypto) {
        this.mContext = context;
        this.mAsyncCryptoFactory = asyncCryptoFactory;
        this.mCrypto = crypto;
        this.mFingerprintManagerCompat = FingerprintManagerCompat.from(context);
    }

    @Override
    public boolean isHardwareDetected() {
        return isHardwareDetected(mContext);
    }

    @Override
    public boolean hasEnrolledFingerprints() {
        return hasEnrolledFingerprints(mContext);
    }

    public void authenticate(final FingerprintManagerCompat.AuthenticationCallback callback) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return;
        }
        cancel();
        FingerprintCompat.d("Creating CryptoObject");
        mAsyncCryptoFactoryCallback = new AsyncCryptoFactory.Callback() {
            @SuppressLint("NewApi")
            @Override
            public void onCryptoObjectCreated(@Nullable FingerprintManagerCompat.CryptoObject cryptoObject) {
                if (cryptoObject != null) {
                    FingerprintCompat.d("Starting authentication [keyName= " + KEY_AUTH_MODE + "]; value= [" + "" + "]");
                    mCancellableAuthenticationCallback = new WrapAuthenticationCallback(Mode.AUTHENTICATION,
                            mCrypto, "", callback);
                    mFingerprintManagerCompat.authenticate(cryptoObject,
                            0,
                            mCancellableAuthenticationCallback.getCancellationSignal(),
                            mCancellableAuthenticationCallback,
                            mMainHandler);
                } else {
                    if (callback != null) {
                        callback.onAuthenticationError(-1, "Fingerprint did not start due to initialization failure, " +
                                "probably because of android.security.keystore.KeyPermanentlyInvalidatedException");
                    }
                }
            }
        };
        mAsyncCryptoFactory.createCryptoObject(Mode.AUTHENTICATION, KEY_AUTH_MODE, mAsyncCryptoFactoryCallback);
    }

    @Override
    public void authenticate(Callback callback) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return;
        }
        startFingerprintAuthentication(Mode.AUTHENTICATION, KEY_AUTH_MODE, "", callback);
    }

    @Override
    public void decrypt(String keyName, String value, Callback callback) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return;
        }
        startFingerprintAuthentication(Mode.DECRYPTION, keyName, value, callback);
    }

    @Override
    public void encrypt(String keyName, String value, Callback callback) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return;
        }
        startFingerprintAuthentication(Mode.ENCRYPTION, keyName, value, callback);
    }

    @Override
    public void cancel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return;
        }
        if (mCancellableAuthenticationCallback != null) {
            mCancellableAuthenticationCallback.cancel();
            mCancellableAuthenticationCallback = null;
        }
        if (mAsyncCryptoFactoryCallback != null) {
            mAsyncCryptoFactoryCallback.cancel();
            mAsyncCryptoFactoryCallback = null;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void startFingerprintAuthentication(final Mode mode,
                                                final String keyName, final String value,
                                                @Nullable final Callback callback) {
        cancel();
        FingerprintCompat.d("Creating CryptoObject");
        mAsyncCryptoFactoryCallback = new AsyncCryptoFactory.Callback() {
            @Override
            public void onCryptoObjectCreated(@Nullable FingerprintManagerCompat.CryptoObject cryptoObject) {
                if (cryptoObject != null) {
                    fingerprintAuthenticationImp(mode, cryptoObject, keyName, value, callback);
                } else {
                    if (callback != null) {
                        callback.onError(new FingerprintException(-1, "Fingerprint did not start due to initialization failure, " +
                                "probably because of android.security.keystore.KeyPermanentlyInvalidatedException"));
                    }
                }
            }
        };
        mAsyncCryptoFactory.createCryptoObject(mode, keyName, mAsyncCryptoFactoryCallback);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void fingerprintAuthenticationImp(Mode mode,
                                              @Nullable FingerprintManagerCompat.CryptoObject cryptoObject,
                                              String keyName, String value,
                                              @Nullable Callback callback) {
        FingerprintCompat.d("Starting authentication [keyName= " + keyName + "]; value= [" + value + "]");
        if (callback != null) {
            callback.onReady();
        }
        mCancellableAuthenticationCallback = new SimpleAuthenticationCallback(mode,
                mCrypto, value, callback);
        mFingerprintManagerCompat.authenticate(cryptoObject,
                0,
                mCancellableAuthenticationCallback.getCancellationSignal(),
                mCancellableAuthenticationCallback,
                mMainHandler);
    }

    public static boolean enable(Context context) {
        if (!isHardwareDetected(context)) {
            Toast.makeText(context.getApplicationContext(),
                    "Fingerprint hardware is not present and functional",
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!isKeyguardSecure(context)) {
            // Show a message that the user hasn't set up a fingerprint or lock screen.
            Toast.makeText(context.getApplicationContext(),
                    "Secure lock screen hasn't set up.\n"
                            + "Go to 'Settings -> Security -> Fingerprint' to set up a fingerprint",
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!hasEnrolledFingerprints(context)) {
            // This happens when no fingerprints are registered.
            Toast.makeText(context.getApplicationContext(),
                    "Go to 'Settings -> Security -> Fingerprint' and register at least one fingerprint",
                    Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    /**
     * @param context Context
     * @return if false Show a message that the user hasn't set up a fingerprint or lock screen.
     * Secure lock screen hasn't set up. Go to 'Settings -> Security -> Fingerprint' to set up a fingerprint
     */
    public static boolean isKeyguardSecure(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return false;
        }
        KeyguardManager keyguardManager = context.getSystemService(KeyguardManager.class);
        return keyguardManager != null
                && keyguardManager.isKeyguardSecure();
    }

    public static boolean isHardwareDetected(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return false;
        }
        // The line below prevents the false positive inspection from Android Studio
        // noinspection ResourceType
        FingerprintManagerCompat fingerprintManagerCompat = FingerprintManagerCompat.from(context);
        return fingerprintManagerCompat.isHardwareDetected();
    }

    /**
     * Now the protection level of USE_FINGERPRINT permission is normal instead of dangerous.
     * See http://developer.android.com/reference/android/Manifest.permission.html#USE_FINGERPRINT
     * The line below prevents the false positive inspection from Android Studio
     * noinspection ResourceType
     *
     * @param context Context
     * @return false This happens when no fingerprints are registered.
     * Go to 'Settings -> Security -> Fingerprint' and register at least one fingerprint.
     */
    public static boolean hasEnrolledFingerprints(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return false;
        }
        FingerprintManagerCompat fingerprintManagerCompat = FingerprintManagerCompat.from(context);
        return fingerprintManagerCompat.hasEnrolledFingerprints();
    }

    public static boolean isFingerprintAuthAvailable(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return false;
        }
        // The line below prevents the false positive inspection from Android Studio
        // noinspection ResourceType
        FingerprintManagerCompat fingerprintManagerCompat = FingerprintManagerCompat.from(context);
        return fingerprintManagerCompat.isHardwareDetected()
                && fingerprintManagerCompat.hasEnrolledFingerprints()
                && isKeyguardSecure(context);
    }

    public static void d(String message) {
        if (!DEBUG) {
            return;
        }
        Log.d(TAG, message);
    }

    public static void e(String message) {
        if (!DEBUG) {
            return;
        }
        Log.e(TAG, message);
    }

    public static void setDebug(boolean debug) {
        DEBUG = debug;
    }

    /**
     * Become Bob the builder.
     */
    public static class Builder {
        private final Context context;
        private Crypto crypto;
        private CryptoFactory cryptoFactory;

        Builder(Context context) {
            this.context = context;
        }

        public Builder setCrypto(Crypto crypto) {
            this.crypto = crypto;
            return this;
        }

        public Builder setCryptoFactory(CryptoFactory cryptoFactory) {
            this.cryptoFactory = cryptoFactory;
            return this;
        }

        public Builder setDebug(boolean debug) {
            FingerprintCompat.setDebug(debug);
            return this;
        }

        @RequiresApi(Build.VERSION_CODES.M)
        private FingerprintCompat buildMarshmallowInstance() {
            Crypto finalCrypto = crypto != null ? crypto : new Crypto.Default();
            CryptoFactory finalCryptoFactory =
                    cryptoFactory != null ? cryptoFactory : new CryptoFactory.Default(context);
            AsyncCryptoFactory asyncCryptoFactory = new AsyncCryptoFactory(finalCryptoFactory);
            return new FingerprintCompat(context, asyncCryptoFactory, finalCrypto);
        }

        public FingerprintCompat build() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return buildMarshmallowInstance();
            } else {
                return new FingerprintCompat(context);
            }
        }
    }
}
