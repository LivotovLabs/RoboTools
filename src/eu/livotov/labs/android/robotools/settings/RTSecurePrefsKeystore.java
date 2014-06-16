package eu.livotov.labs.android.robotools.settings;

import android.content.Context;
import android.text.TextUtils;
import eu.livotov.labs.android.robotools.crypt.RTBase64;
import eu.livotov.labs.android.robotools.crypt.RTDataCryptEngine;
/**
 * Created with IntelliJ IDEA.
 * User: alex askerov
 * Date: 9/19/12
 * Time: 6:50 PM
 */

/**
 * Use this class for new projects and you are not use RTSecurePrefs.
 * It provide encrypt preferences via random generated key stored in system keystore (18+ api only).
 * For pre 18 api use same approach like in RTSecurePrefs with some limitations (no way to use custom key
 * and device or sim lock)
 */
public class RTSecurePrefsKeystore extends RTPrefs
{


    private static RTSecurePrefsKeystore defaultPreferences;
    private RTDataCryptEngine dataCryptEngine;


    public static synchronized RTSecurePrefsKeystore getDefault(final Context ctx)
    {
        if (defaultPreferences == null)
        {
            defaultPreferences = new RTSecurePrefsKeystore(ctx);
        }
        return defaultPreferences;
    }

    public RTSecurePrefsKeystore(final Context ctx)
    {
        this(ctx, "defaultsecure");
    }

    public RTSecurePrefsKeystore(final Context ctx, final String preferenceStorageName)
    {
        super(ctx, preferenceStorageName, true);
        this.ctx = ctx;
        dataCryptEngine = new RTDataCryptEngine(ctx);
    }

    public String getString(int key, final String defaultValue)
    {

        try
        {
            String value = super.getString(key, "");
            return dataCryptEngine.decryptString(value);
        } catch (Throwable err)
        {
            return defaultValue;
        }
    }

    public void setString(int key, String value)
    {
        if (TextUtils.isEmpty(value))
        {
            remove(key);
        } else
        {
            super.setString(key, dataCryptEngine.encryptString(value));
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
}
