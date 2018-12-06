package com.d.lib.fingerprintcompat.crypto;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.Base64;

import com.d.lib.fingerprintcompat.FingerprintCompat;
import com.d.lib.fingerprintcompat.base.FingerprintManagerCompat;
import com.d.lib.fingerprintcompat.base.IFingerprint;
import com.d.lib.fingerprintcompat.base.Mode;

import java.security.Key;
import java.security.KeyStore;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.IvParameterSpec;

/**
 * Interface used for {@link com.d.lib.fingerprintcompat.base.FingerprintManagerCompat.CryptoObject}
 * creation. Has separate method for {@link IFingerprint#authenticate(IFingerprint.Callback)},
 * {@link IFingerprint#decrypt(String, String, IFingerprint.Callback)} and
 * {@link IFingerprint#encrypt(String, String, IFingerprint.Callback)}
 */
public interface CryptoFactory {

    /**
     * Create CryptoObject for authentication call. Return null if invalid.
     */
    @Nullable
    FingerprintManagerCompat.CryptoObject createAuthenticationCryptoObject(String keyName);

    /**
     * Create CryptoObject for encryption call. Return null if invalid.
     */
    @Nullable
    FingerprintManagerCompat.CryptoObject createEncryptionCryptoObject(String keyName);

    /**
     * Create CryptoObject for decryption call. Return null if invalid.
     */
    @Nullable
    FingerprintManagerCompat.CryptoObject createDecryptionCryptoObject(String keyName);

    @RequiresApi(Build.VERSION_CODES.M)
    class Default implements CryptoFactory {

        private static final String KEY_KEYSTORE = "AndroidKeyStore";
        private static final String KEY_SHARED_PREFS = "<FingerprintCompat IV>";

        private KeyGenerator keyGenerator;
        private KeyStore keyStore;
        private final SharedPreferences sharedPrefs;

        public Default(Context context) {
            this.sharedPrefs = context.getSharedPreferences(KEY_SHARED_PREFS, Context.MODE_PRIVATE);
            try {
                keyStore = KeyStore.getInstance(KEY_KEYSTORE);
                keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEY_KEYSTORE);
            } catch (Exception e) {
                FingerprintCompat.e(e.toString());
            }
        }

        @Nullable
        @Override
        public FingerprintManagerCompat.CryptoObject createAuthenticationCryptoObject(String keyName) {
            return createCryptoObject(keyName, Mode.AUTHENTICATION);
        }

        @Nullable
        @Override
        public FingerprintManagerCompat.CryptoObject createDecryptionCryptoObject(String keyName) {
            return createCryptoObject(keyName, Mode.DECRYPTION);
        }

        @Nullable
        @Override
        public FingerprintManagerCompat.CryptoObject createEncryptionCryptoObject(String keyName) {
            return createCryptoObject(keyName, Mode.ENCRYPTION);
        }

        private Cipher createCipher(String keyName, Mode mode, Key key) throws Exception {
            String transformation = String.format("%s/%s/%s",
                    KeyProperties.KEY_ALGORITHM_AES,
                    KeyProperties.BLOCK_MODE_CBC,
                    KeyProperties.ENCRYPTION_PADDING_PKCS7
            );
            Cipher cipher = Cipher.getInstance(transformation);
            if (mode == Mode.DECRYPTION) {
                byte[] iv = loadIv(keyName);
                cipher.init(mode.cipherMode(), key, new IvParameterSpec(iv));
            } else {
                cipher.init(mode.cipherMode(), key);
                saveIv(keyName, cipher.getIV());
            }
            return cipher;
        }

        private FingerprintManagerCompat.CryptoObject createCryptoObject(String keyName, Mode mode) {
            if (keyStore == null || keyGenerator == null) {
                return null;
            }

            try {
                Key key = (mode == Mode.DECRYPTION) ? loadKey(keyName) : createKey(keyName);
                Cipher cipher = createCipher(keyName, mode, key);
                return new FingerprintManagerCompat.CryptoObject(cipher);
            } catch (Exception e) {
                FingerprintCompat.e(e.toString());
                return null;
            }
        }

        private Key createKey(String keyName) throws Exception {
            KeyGenParameterSpec.Builder keyGenParamsBuilder =
                    new KeyGenParameterSpec.Builder(keyName, KeyProperties.PURPOSE_DECRYPT | KeyProperties.PURPOSE_ENCRYPT)
                            .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                            .setUserAuthenticationRequired(true);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                keyGenParamsBuilder.setInvalidatedByBiometricEnrollment(true);
            }
            keyGenerator.init(keyGenParamsBuilder.build());
            keyGenerator.generateKey();
            return loadKey(keyName);
        }

        private byte[] loadIv(String keyName) {
            return Base64.decode(sharedPrefs.getString(keyName, ""), Base64.DEFAULT);
        }

        private Key loadKey(String keyName) throws Exception {
            keyStore.load(null);
            return keyStore.getKey(keyName, null);
        }

        private void saveIv(String keyName, byte[] iv) {
            sharedPrefs.edit().putString(keyName, Base64.encodeToString(iv, Base64.DEFAULT)).apply();
        }
    }
}
