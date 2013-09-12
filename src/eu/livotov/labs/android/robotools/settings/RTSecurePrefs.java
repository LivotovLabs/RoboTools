package eu.livotov.labs.android.robotools.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import eu.livotov.labs.android.robotools.crypt.RTBase64;
import eu.livotov.labs.android.robotools.crypt.RTCryptUtil;
import eu.livotov.labs.android.robotools.device.RTDevice;

import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: dlivotov
 * Date: 9/19/12
 * Time: 6:50 PM
 * To change this template use File | Settings | File Templates.
 */
public class RTSecurePrefs extends RTPrefs
{

    private static RTSecurePrefs defaultPreferences;
    private String keychainPassword;

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
    }

    public boolean isLocked()
    {
        return TextUtils.isEmpty(keychainPassword);
    }

    public void unlock(final String password, boolean lockToDevice, boolean lockToWifiAddress, boolean lockToTelephony, boolean lockToSIM)
    {
        String newKeychainPassword = null;

        try
        {
            newKeychainPassword = lockToDevice ? RTCryptUtil.generateDeviceBoundEncryptionKeyForPassword(ctx,
                                                                                                         password,
                                                                                                         lockToWifiAddress,
                                                                                                         lockToTelephony,
                                                                                                         lockToSIM) : password;
        } catch (Throwable err)
        {
            throw new RuntimeException(err);
        }

        if (TextUtils.isEmpty(keychainPassword))
        {
            checkVerificationValue(newKeychainPassword);
            keychainPassword = password;
        } else
        {
            checkVerificationValue(keychainPassword);

            // Now we need to check if there was previous key set and used - if so, we'll need to recrypt old data for a new key.
            Set<String> keychainKeys = preferences.getAll().keySet();
            SharedPreferences.Editor editor = preferences.edit();

            for (String key : keychainKeys)
            {
                if (!RTSecurePrefs.class.getCanonicalName().equals(key))
                {
                    editor.putString(key, RTCryptUtil.encrypt(RTCryptUtil.decryptAsText(preferences.getString(key, ""), keychainPassword), newKeychainPassword));
                }
            }

            editor.commit();
            writeVerificationValue(newKeychainPassword);
            keychainPassword = newKeychainPassword;
        }
    }

    public void lock()
    {
        keychainPassword = null;
    }

    private void writeVerificationValue(final String password)
    {
        preferences.edit().putString(RTSecurePrefs.class.getCanonicalName(), RTCryptUtil.encrypt(RTDevice.getDeviceUID(ctx, false, false, false), password)).commit();
    }

    private void checkVerificationValue(final String password)
    {
        boolean ok = true;

        if (preferences.contains(RTSecurePrefs.class.getCanonicalName()))
        {
            try
            {
                final String uid = RTDevice.getDeviceUID(ctx, false, false, false);
                final String euid = RTCryptUtil.decryptAsText(preferences.getString(RTSecurePrefs.class.getCanonicalName(), ""), password);
                ok = uid.equals(euid);
            } catch (Throwable err)
            {
                err.printStackTrace();
                ok = false;
            }
        }

        if (!ok)
        {
            throw new RuntimeException("Incorrect password for existing keychain");
        }
    }

    public String getString(int key, final String defaultValue)
    {
        ensureKeychainUnlocked();

        try
        {
            return RTCryptUtil.decryptAsText(super.getString(key, ""), keychainPassword);
        } catch (Throwable err)
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
        } else
        {
            super.setString(key, RTCryptUtil.encrypt(value, keychainPassword));
        }
    }

    public int getInt(int key, int defaultValue)
    {
        try
        {
            return Integer.parseInt(getString(key, "" + defaultValue));
        } catch (Throwable err)
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
        } catch (Throwable err)
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
        } catch (Throwable err)
        {
            return defaultValue;
        }
    }

    public boolean getBoolean(int key, boolean defaultValue)
    {
        try
        {
            return "1".equals(getString(key, defaultValue ? "1" : "0"));
        } catch (Throwable err)
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
        if (TextUtils.isEmpty(keychainPassword))
        {
            throw new RuntimeException("Keychain in locked. Please either unlock it with your password.");
        }
    }
}
