package com.d.lib.fingerprintcompat.crypto;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.d.lib.fingerprintcompat.base.FingerprintManagerCompat;
import com.d.lib.fingerprintcompat.base.Mode;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class AsyncCryptoFactory {
    private final CryptoFactory mCryptoFactory;
    private final ExecutorService mExecutor;
    private Future mTask;

    public AsyncCryptoFactory(CryptoFactory cryptoFactory) {
        this.mCryptoFactory = cryptoFactory;
        this.mExecutor = Executors.newSingleThreadExecutor();
    }

    public void createCryptoObject(Mode mode, String keyName,
                                   @NonNull AsyncCryptoFactory.Callback callback) {
        if (mTask != null && !mTask.isDone()) {
            mTask.cancel(true);
        }

        this.mTask = mExecutor.submit(new CryptoObjectInitRunnable(mode, mCryptoFactory, keyName, callback));
    }

    public static abstract class Callback {

        public abstract void onCryptoObjectCreated(@Nullable FingerprintManagerCompat.CryptoObject cryptoObject);

        private boolean canceled = false;

        public void cancel() {
            canceled = true;
        }

        public boolean isCanceled() {
            return canceled;
        }
    }
}
