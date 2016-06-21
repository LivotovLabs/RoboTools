package eu.livotov.labs.android.robotools.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import java.util.Set;

import eu.livotov.labs.android.robotools.crypt.RTCryptUtil;
import eu.livotov.labs.android.robotools.hardware.RTDevice;
import eu.livotov.labs.android.robotools.text.RTBase64;

/**
 * Created by dlivotov on 21/06/2016.
 */

public class RTSecurePrefs extends RTPrefs
{

    private static RTSecurePrefs defaultPreferences;
    private String customBindHash;
    private String keychainKey;

    private boolean lockToDevice = false;
    private boolean lockToWifiAddress = false;
    private boolean lockToTelephony = false;
    private boolean lockToSIM = false;

    public static synchronized RTSecurePrefs getDefault(final Context ctx)
    {
        if (defaultPreferences == null)
        {
            defaultPreferences = new RTSecurePrefs(ctx);
        }

        return defaultPreferences;
    }

    public RTSecurePrefs(final Context ctx)
    {
        this(ctx, "defaultsecure");
    }

    public RTSecurePrefs(final Context ctx, final String preferenceStorageName)
    {
        super(ctx, preferenceStorageName, true);
        lock();
    }

    public RTSecurePrefs(final Context ctx, final String preferenceStorageName, final boolean bindToDevice, boolean bindToWifi, boolean bindToTelephony, boolean bindToSim)
    {
        super(ctx, preferenceStorageName, true);
        this.lockToDevice = bindToDevice;
        this.lockToSIM = bindToSim;
        this.lockToTelephony = bindToTelephony;
        this.lockToWifiAddress = bindToWifi;
        lock();
    }

    public RTSecurePrefs(final Context ctx, final String preferenceStorageName, final String customBindHash)
    {
        super(ctx, preferenceStorageName, true);
        this.lockToDevice = false;
        this.lockToSIM = false;
        this.lockToTelephony = false;
        this.lockToWifiAddress = false;

        this.customBindHash = customBindHash;
        lock();
    }

    public boolean isLocked()
    {
        try
        {
            ensureKeychainUnlocked();
            return false;
        }
        catch (Throwable err)
        {
            return true;
        }
    }

    public void unlock(final String password)
    {
        final String newKeychainKey = generateKeychainKey(password);

        // This is an empty keychain, so simply assign new password and create verification token.
        if (!preferences.contains(RTSecurePrefs.class.getCanonicalName()))
        {
            keychainKey = newKeychainKey;
            writeVerificationValue(keychainKey);
            return;
        }

        // This keychain is not empty or was already initialized previously but non yet unlocked
        // We simply trying to unlock it with the given password
        checkVerificationValue(newKeychainKey);
        keychainKey = newKeychainKey;
    }

    public void changePassword(final String newPassword)
    {
        ensureKeychainUnlocked();

        final String newKeychainKey = TextUtils.isEmpty(newPassword) ? generateDefaultKeychainKeyPassword() : generateKeychainKey(newPassword);

        Set<String> keychainKeys = preferences.getAll().keySet();
        SharedPreferences.Editor editor = preferences.edit();

        for (String key : keychainKeys)
        {
            if (!RTSecurePrefs.class.getCanonicalName().equals(key))
            {
                editor.putString(key, RTCryptUtil.encrypt(RTCryptUtil.decryptAsText(preferences.getString(key, ""), keychainKey), newKeychainKey));
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
    public void clear()
    {
        Set<String> keychainKeys = preferences.getAll().keySet();
        SharedPreferences.Editor editor = preferences.edit();

        for (String key : keychainKeys)
        {
            if (!RTSecurePrefs.class.getCanonicalName().equals(key))
            {
                editor.remove(key);
            }
        }

        editor.commit();
    }

    /**
     * Completely resets the keychain with all data and keys. After calling this method
     * keychain becomes empty and next unlock() call may be used to set a new password.
     */
    public void reset()
    {
        lock();
        super.clear();
    }

    public void lock()
    {
        keychainKey = generateDefaultKeychainKeyPassword();
    }

    protected String generateDefaultKeychainKeyPassword()
    {
        return generateKeychainKey(RTSecurePrefs.class.getSimpleName());
    }

    private String generateKeychainKey(final String password)
    {
        try
        {
            if (!TextUtils.isEmpty(customBindHash))
            {
                return RTCryptUtil.md5(String.format("%s%s", customBindHash, password));
            }
            else
            {
                return lockToDevice ? RTCryptUtil.generateDeviceBoundEncryptionKeyForPassword(ctx, password, lockToWifiAddress, lockToTelephony, lockToSIM) : password;
            }
        }
        catch (Throwable err)
        {
            throw new RuntimeException(err);
        }
    }

    private void writeVerificationValue(final String encryptionKey)
    {
        preferences.edit().putString(RTSecurePrefs.class.getCanonicalName(), RTCryptUtil.encrypt(RTDevice.getDeviceUID(ctx), encryptionKey)).commit();
    }

    private void checkVerificationValue(final String encryptionKey)
    {
        boolean ok = true;

        if (preferences.contains(RTSecurePrefs.class.getCanonicalName()))
        {
            try
            {
                final String uid = RTDevice.getDeviceUID(ctx);
                final String euid = RTCryptUtil.decryptAsText(preferences.getString(RTSecurePrefs.class.getCanonicalName(), ""), encryptionKey);
                ok = uid.equals(euid);
            }
            catch (Throwable err)
            {
                err.printStackTrace();
                ok = false;
            }
        }

        if (!ok)
        {
            throw new RuntimeException("Keychain is locked or incorrect encryption key set.");
        }
    }

    public String getString(int key, final String defaultValue)
    {
        ensureKeychainUnlocked();

        try
        {
            return RTCryptUtil.decryptAsText(super.getString(key, ""), keychainKey);
        }
        catch (Throwable err)
        {
            return defaultValue;
        }
    }

    public void setString(int key, String value)
    {
        ensureKeychainUnlocked();

        if (TextUtils.isEmpty(value))
        {
            remove(key);
        }
        else
        {
            super.setString(key, RTCryptUtil.encrypt(value, keychainKey));
        }
    }

    public int getInt(int key, int defaultValue)
    {
        try
        {
            return Integer.parseInt(getString(key, "" + defaultValue));
        }
        catch (Throwable err)
        {
            return defaultValue;
        }
    }

    public void setInt(int key, int value)
    {
        setString(key, "" + value);
    }

    public long getLong(int key, long defaultValue)
    {
        try
        {
            return Long.parseLong(getString(key, "" + defaultValue));
        }
        catch (Throwable err)
        {
            return defaultValue;
        }
    }

    public void setIntArray(int key, int[] array)
    {
        setString(key, arrayToString(array));
    }

    public void setLongArray(int key, long[] array)
    {
        setString(key, arrayToString(array));
    }

    public void setByteArray(int key, byte[] array)
    {
        setString(key, RTBase64.encodeToString(array, RTBase64.NO_WRAP));
    }

    public int[] getIntArray(int key)
    {
        return stringToIntegerArray(getString(key, ""));
    }

    public long[] getLongArray(int key)
    {
        return stringToLongArray(getString(key, ""));
    }

    public byte[] getByteArray(int key)
    {
        return RTBase64.decode(getString(key, ""), RTBase64.NO_WRAP);
    }

    public void setLong(int key, long value)
    {
        setString(key, "" + value);
    }

    public void setDouble(int key, double value)
    {
        setString(key, "" + value);
    }

    public double getDouble(int key, double defaultValue)
    {
        try
        {
            return Double.parseDouble(getString(key, "" + defaultValue));
        }
        catch (Throwable err)
        {
            return defaultValue;
        }
    }

    public boolean getBoolean(int key, boolean defaultValue)
    {
        try
        {
            return "1".equals(getString(key, defaultValue ? "1" : "0"));
        }
        catch (Throwable err)
        {
            return defaultValue;
        }
    }

    public void setBoolean(int key, boolean value)
    {
        setString(key, value ? "1" : "0");
    }

    protected void ensureKeychainUnlocked()
    {
        checkVerificationValue(keychainKey);
    }
}
