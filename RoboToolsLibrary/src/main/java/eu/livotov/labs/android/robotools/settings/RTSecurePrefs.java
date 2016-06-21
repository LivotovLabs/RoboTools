package eu.livotov.labs.android.robotools.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import java.util.Set;

import eu.livotov.labs.android.robotools.crypt.RTCryptUtil;
import eu.livotov.labs.android.robotools.crypt.RTDataCryptEngine;
import eu.livotov.labs.android.robotools.text.RTBase64;

/**
 * Created by dlivotov on 21/06/2016.
 */

public class RTSecurePrefs extends RTPrefs {

    private static RTSecurePrefs defaultPreferences;
    private RTDataCryptEngine cryptEngine;
    private String password;
    private static final String PASSWORD_KEY = "RTSecurePrefs_user_password";

    public static synchronized RTSecurePrefs getDefault(final Context ctx) {
        if (defaultPreferences == null) {
            defaultPreferences = new RTSecurePrefs(ctx);
        }
        return defaultPreferences;
    }

    public static synchronized RTSecurePrefs getDefault(final Context ctx, String password) {
        if (defaultPreferences == null) {
            defaultPreferences = new RTSecurePrefs(ctx, "defaultsecure", password);
        }
        return defaultPreferences;
    }

    public RTSecurePrefs(final Context ctx) {
        this(ctx, "defaultsecure");
    }

    public RTSecurePrefs(final Context ctx, final String preferenceStorageName) {
        super(ctx, preferenceStorageName, true);
        init(ctx, null);
    }

    public RTSecurePrefs(final Context ctx, final String preferenceStorageName, final String customPassword) {
        super(ctx, preferenceStorageName, true);
        init(ctx, customPassword);
    }

    private void init(Context ctx, String customPassword) {
        cryptEngine = new RTDataCryptEngine(ctx, customPassword);
    }

    public void lock(String userPassword) {
        setString(PASSWORD_KEY, RTCryptUtil.md5(userPassword));
        Set<String> keychainKeys = preferences.getAll().keySet();
        SharedPreferences.Editor editor = preferences.edit();
        for (String key : keychainKeys) {
            if (!PASSWORD_KEY.equals(key)) {
                try {
                    editor.putString(key, RTCryptUtil.encrypt(preferences.getString(key, ""), userPassword));
                } catch (Throwable throwable) {
                    reset();
                }
            }
        }
        editor.apply();
        password = userPassword;
    }

    public void unlock(String password) {
        this.password = password;
    }

    public boolean isLocked() {
        return !TextUtils.isEmpty(getString(PASSWORD_KEY, null));
    }

    public void resetPassword(String userPassword) throws Exception {
        if (isLocked()) {
            if (!verifyPassword(userPassword))
                throw new SecurityException("userPassword is incorrect! Access denied for operation [resetPassword]");
            remove(PASSWORD_KEY);
            Set<String> keychainKeys = preferences.getAll().keySet();
            SharedPreferences.Editor editor = preferences.edit();
            for (String key : keychainKeys) {
                if (!PASSWORD_KEY.equals(key)) {
                    try {
                        editor.putString(key, RTCryptUtil.decryptAsText(preferences.getString(key, ""), userPassword));
                    } catch (Throwable throwable) {
                        reset();
                    }
                }
            }
            editor.apply();
            password = null;
        }
    }

    public void changePassword(String oldPassword, String newPassword) {
        if (isLocked()) {
            if (!verifyPassword(oldPassword))
                throw new SecurityException("oldPassword is incorrect! Access denied for operation [changePassword]");

            setString(PASSWORD_KEY, RTCryptUtil.md5(newPassword));
            Set<String> keychainKeys = preferences.getAll().keySet();
            SharedPreferences.Editor editor = preferences.edit();
            for (String key : keychainKeys) {
                if (!PASSWORD_KEY.equals(key)) {
                    try {
                        String decryptedWithOldPasswordValue = RTCryptUtil.decryptAsText(preferences.getString(key, ""), oldPassword);
                        editor.putString(key, RTCryptUtil.encrypt(decryptedWithOldPasswordValue, newPassword));
                    } catch (Throwable throwable) {
                        reset();
                    }
                }
            }
            editor.apply();
            password = null;
        }
    }

    private boolean verifyPassword(String password) {
        return RTCryptUtil.md5(password).equalsIgnoreCase(getString(PASSWORD_KEY, null));
    }


    /**
     * Completely resets the keychain with all data and keys. After calling this method
     * keychain becomes empty
     */
    public void reset() {
        super.clear();
        cryptEngine.reset();
        remove(PASSWORD_KEY);
        password = null;
    }


    public String getString(int key, final String defaultValue) {
        try {
            String encryptedValue = getString(key, "");
            if (TextUtils.isEmpty(encryptedValue))
                return encryptedValue;
            if (isLocked()) {
                if (!verifyPassword(password))
                    throw new SecurityException("password is incorrect! Access denied for operation [getData]");
                return cryptEngine.decryptString(RTCryptUtil.decryptAsText(encryptedValue, password));
            }
            return cryptEngine.decryptString(encryptedValue);
        } catch (Throwable err) {
            reset();
            return null;
        }
    }

    public void setString(int key, String value) {

        if (TextUtils.isEmpty(value)) {
            remove(key);
        } else {
            try {
                if (isLocked()) {
                    if (!verifyPassword(password))
                        throw new SecurityException("password is incorrect! Access denied for operation [setData]");
                    value = RTCryptUtil.encrypt(value, password);
                }
                super.setString(key, cryptEngine.encryptString(value));
            } catch (Throwable throwable) {
                reset();
            }
        }
    }

    public int getInt(int key, int defaultValue) {
        try {
            return Integer.parseInt(getString(key, "" + defaultValue));
        } catch (Throwable err) {
            return defaultValue;
        }
    }

    public void setInt(int key, int value) {
        setString(key, "" + value);
    }

    public long getLong(int key, long defaultValue) {
        try {
            return Long.parseLong(getString(key, "" + defaultValue));
        } catch (Throwable err) {
            return defaultValue;
        }
    }

    public void setIntArray(int key, int[] array) {
        setString(key, arrayToString(array));
    }

    public void setLongArray(int key, long[] array) {
        setString(key, arrayToString(array));
    }

    public void setByteArray(int key, byte[] array) {
        setString(key, RTBase64.encodeToString(array, RTBase64.NO_WRAP));
    }

    public int[] getIntArray(int key) {
        return stringToIntegerArray(getString(key, ""));
    }

    public long[] getLongArray(int key) {
        return stringToLongArray(getString(key, ""));
    }

    public byte[] getByteArray(int key) {
        return RTBase64.decode(getString(key, ""), RTBase64.NO_WRAP);
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

}
