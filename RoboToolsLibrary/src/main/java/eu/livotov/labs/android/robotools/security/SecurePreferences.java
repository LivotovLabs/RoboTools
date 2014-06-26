package eu.livotov.labs.android.robotools.security;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import eu.livotov.labs.android.robotools.hardware.DeviceInfo;
import eu.livotov.labs.android.robotools.os.Preferences;

import javax.crypto.SecretKey;
import java.security.GeneralSecurityException;
import java.util.Set;

@SuppressWarnings("unused")
public class SecurePreferences extends Preferences {

    public static final String WRAPPED_KEY = "wrapped_key";
    private static final String TAG = SecurePreferences.class.getCanonicalName();
    private static final boolean IS_JB43 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2;

    private static SecurePreferences defaultPreferences;
    private String keychainKey;

    private boolean lockToDevice = false;
    private boolean lockToWifiAddress = false;
    private boolean lockToTelephony = false;
    private boolean lockToSIM = false;

    private Context ctx;
    private SecretKeyWrapper kw;
    private SharedPreferences privatePrefs;


    public static synchronized SecurePreferences getDefault(final Context ctx) {
        if (defaultPreferences == null) {
            defaultPreferences = new SecurePreferences(ctx);
        }
        return defaultPreferences;
    }

    public SecurePreferences(final Context ctx) {
        this(ctx, "defaultsecure");
    }

    public SecurePreferences(final Context ctx, final String preferenceStorageName) {
        super(ctx, preferenceStorageName, true);
        this.ctx = ctx;
        privatePrefs = ctx.getSharedPreferences("RTSecureStoragePrefs", Context.MODE_PRIVATE);
        if (IS_JB43) {
            try {
                String keyName = ctx.getPackageName() + "_rt_secure_key";
                kw = new SecretKeyWrapper(ctx, keyName);
            } catch (Exception e) {
                kw = null;
            }
        }
        lock();
    }

    public SecurePreferences(final Context ctx, final String preferenceStorageName, final boolean bindToDevice, boolean bindToWifi, boolean bindToTelephony, boolean bindToSim) {
        super(ctx, preferenceStorageName, true);
        this.lockToDevice = bindToDevice;
        this.lockToSIM = bindToSim;
        this.lockToTelephony = bindToTelephony;
        this.lockToWifiAddress = bindToWifi;
        lock();
    }

    public boolean isKWInit() {
        return kw != null;
    }

    private String encryptWithSecretKey(String plaintext) {
        try {
            Log.d(TAG, "encryptWithSecretKey");
            return CryptUtil.encryptAesCbc(plaintext, getKey(true));
        } catch (Exception e) {
            Log.d(TAG, e.getMessage(), e);
            return "";
        }
    }

    private String decryptWithSecretKey(String ciphertext) {
        try {
            Log.d(TAG, "decryptWithSecretKey");
            return CryptUtil.decryptAesCbc(ciphertext, getKey(false));
        } catch (Exception e) {
            Log.d(TAG, e.getMessage(), e);
            return "";
        }
    }

    private SecretKey getKey(boolean generateIfNeeded) throws GeneralSecurityException {
        SecretKey key = null;
        String wrappedkey = getWrappedKey();
        if (TextUtils.isEmpty(wrappedkey) && generateIfNeeded) {
            Log.d(TAG, "generate key and wrap");
            key = CryptUtil.generateAesKey();
            wrappedkey = Base64.encodeToString(kw.wrap(key), Base64.NO_WRAP);
            saveWrappedKey(wrappedkey);
        } else if (!TextUtils.isEmpty(wrappedkey)) {
            Log.d(TAG, "unwrap key");
            key = kw.unwrap(Base64.decode(wrappedkey, Base64.NO_WRAP));
        }
        return key;
    }

    private void saveWrappedKey(String wrappedkey) {
        SharedPreferences.Editor editor = privatePrefs.edit();
        editor.putString(WRAPPED_KEY, wrappedkey);

        // TODO: only to HoneyComb and highter
        editor.apply();
    }

    private String getWrappedKey() {
        return privatePrefs.getString(WRAPPED_KEY, "");
    }

    public boolean isLocked() {
        try {
            ensureKeychainUnlocked();
            return false;
        } catch (Throwable err) {
            return true;
        }
    }

    public void unlock(final String password) {
        final String newKeychainKey = generateKeychainKey(password);

        // This is an empty keychain, so simply assign new password and create verification token.
        if (!preferences.contains(SecurePreferences.class.getCanonicalName())) {
            keychainKey = newKeychainKey;
            writeVerificationValue(keychainKey);
            return;
        }

        // This keychain is not empty or was already initialized previously but non yet unlocked
        // We simply trying to unlock it with the given password
        checkVerificationValue(newKeychainKey);
        keychainKey = newKeychainKey;
    }

    public void changePassword(final String newPassword) {
        ensureKeychainUnlocked();

        final String newKeychainKey = TextUtils.isEmpty(newPassword) ? generateDefaultKeychainKeyPassword() : generateKeychainKey(newPassword);

        Set<String> keychainKeys = preferences.getAll().keySet();
        SharedPreferences.Editor editor = preferences.edit();

        for (String key : keychainKeys) {
            if (!SecurePreferences.class.getCanonicalName().equals(key)) {
                editor.putString(key, CryptUtil.encrypt(CryptUtil.decryptAsText(preferences.getString(key, ""), keychainKey), newKeychainKey));
            }
        }

        editor.commit();
        writeVerificationValue(newKeychainKey);
        keychainKey = newKeychainKey;
    }

    /**
     * Removes all user-specified properties from this keychain, but leaves
     * keychain initialized with the current password.
     */
    public void clear() {
        Set<String> keychainKeys = preferences.getAll().keySet();
        SharedPreferences.Editor editor = preferences.edit();

        for (String key : keychainKeys) {
            if (!SecurePreferences.class.getCanonicalName().equals(key)) {
                editor.remove(key);
            }
        }

        editor.commit();
    }

    /**
     * Completely resets the keychain with all data and keys. After calling this method
     * keychain becomes empty and next unlock() call may be used to set a new password.
     */
    public void reset() {
        lock();
        super.clear();
    }

    public void lock() {
        keychainKey = generateDefaultKeychainKeyPassword();
    }

    protected String generateDefaultKeychainKeyPassword() {
        return generateKeychainKey(SecurePreferences.class.getSimpleName());
    }

    private String generateKeychainKey(final String password) {
        try {
            return lockToDevice ? CryptUtil.generateDeviceBoundEncryptionKeyForPassword(ctx,
                    password,
                    lockToWifiAddress,
                    lockToTelephony,
                    lockToSIM) : password;
        } catch (Throwable err) {
            throw new RuntimeException(err);
        }
    }

    private void writeVerificationValue(final String encryptionKey) {
        preferences.edit().putString(SecurePreferences.class.getCanonicalName(), CryptUtil.encrypt(DeviceInfo.getDeviceUID(ctx, false, false, false), encryptionKey)).commit();
    }

    private void checkVerificationValue(final String encryptionKey) {
        boolean ok = true;

        if (preferences.contains(SecurePreferences.class.getCanonicalName())) {
            try {
                final String uid = DeviceInfo.getDeviceUID(ctx, false, false, false);
                final String euid = CryptUtil.decryptAsText(preferences.getString(SecurePreferences.class.getCanonicalName(), ""), encryptionKey);
                ok = uid.equals(euid);
            } catch (Throwable err) {
                err.printStackTrace();
                ok = false;
            }
        }

        if (!ok) {
            throw new RuntimeException("Keychain is locked or incorrect encryption key set.");
        }
    }

    @Override
    public String getString(int key, final String defaultValue) {
        ensureKeychainUnlocked();

        try {
            String value = super.getString(key, "");
            return isKWInit()
                    ? decryptWithSecretKey(value)
                    : CryptUtil.decryptAsText(value, keychainKey);
        } catch (Throwable err) {
            return defaultValue;
        }
    }

    @Override
    public void setString(int key, String value) {
        ensureKeychainUnlocked();

        if (TextUtils.isEmpty(value)) {
            remove(key);
        } else {
            super.setString(key, isKWInit()
                    ? encryptWithSecretKey(value)
                    : CryptUtil.encrypt(value, keychainKey));
        }
    }

    @Override
    public int getInt(int key, int defaultValue) {
        try {
            return Integer.parseInt(getString(key, "" + defaultValue));
        } catch (Throwable err) {
            return defaultValue;
        }
    }

    @Override
    public void setInt(int key, int value) {
        setString(key, "" + value);
    }

    @Override
    public long getLong(int key, long defaultValue) {
        try {
            return Long.parseLong(getString(key, "" + defaultValue));
        } catch (Throwable err) {
            return defaultValue;
        }
    }

    @Override
    public void setIntArray(int key, int[] array) {
        setString(key, arrayToString(array));
    }

    @Override
    public void setLongArray(int key, long[] array) {
        setString(key, arrayToString(array));
    }

    @Override
    public void setByteArray(int key, byte[] array) {
        setString(key, CryptUtil.toBase64(array));
    }

    @Override
    public int[] getIntArray(int key) {
        return stringToIntegerArray(getString(key, ""));
    }

    @Override
    public long[] getLongArray(int key) {
        return stringToLongArray(getString(key, ""));
    }

    @Override
    public byte[] getByteArray(int key) {
        return CryptUtil.fromBase64(getString(key, ""));
    }

    public void setLong(int key, long value) {
        setString(key, "" + value);
    }

    public void setDouble(int key, double value) {
        setString(key, "" + value);
    }

    public double getDouble(int key, double defaultValue) {
        try {
            return Double.parseDouble(getString(key, "" + defaultValue));
        } catch (Throwable err) {
            return defaultValue;
        }
    }

    public boolean getBoolean(int key, boolean defaultValue) {
        try {
            return "1".equals(getString(key, defaultValue ? "1" : "0"));
        } catch (Throwable err) {
            return defaultValue;
        }
    }

    public void setBoolean(int key, boolean value) {
        setString(key, value ? "1" : "0");
    }

    protected void ensureKeychainUnlocked() {
        checkVerificationValue(keychainKey);
    }
}
