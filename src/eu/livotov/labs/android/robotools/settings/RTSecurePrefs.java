package eu.livotov.labs.android.robotools.settings;

import android.content.Context;
import eu.livotov.labs.android.robotools.crypt.RTCryptUtil;

/**
 * Created with IntelliJ IDEA.
 * User: dlivotov
 * Date: 9/19/12
 * Time: 6:50 PM
 * To change this template use File | Settings | File Templates.
 */
public class RTSecurePrefs
{

    private static byte[] ekey;

    public static void setPassword(final Context ctx,
                                   final String password,
                                   boolean lockToDevice,
                                   boolean lockToWifiAddress,
                                   boolean lockToTelephony,
                                   boolean lockToSIM)
    {
        try
        {
            ekey = lockToDevice ? RTCryptUtil.generateDeviceBoundEncryptionKeyForPassword(ctx,
                                                                                          password,
                                                                                          lockToWifiAddress,
                                                                                          lockToTelephony,
                                                                                          lockToSIM) :
                           RTCryptUtil.generateDeviceBoundEncryptionKeyForPassword(password);
        } catch (Throwable err)
        {
            throw new RuntimeException(err);
        }
    }

    public static String getString(Context ctx, int key, final String defaultValue)
    {
        try
        {
            return RTCryptUtil.decrypt(ekey, RTPrefs.getString(ctx, key, ""));
        } catch (Throwable err)
        {
            return defaultValue;
        }
    }

    public static void setString(Context ctx, int key, String value)
    {
        RTPrefs.setString(ctx, key, RTCryptUtil.encrypt(ekey, value));
    }

    public static int getInt(Context ctx, int key, int defaultValue)
    {
        try
        {
            return Integer.parseInt(getString(ctx, key, "" + defaultValue));
        } catch (Throwable err)
        {
            return defaultValue;
        }
    }

    public static void setInt(Context ctx, int key, int value)
    {
        setString(ctx, key, "" + value);
    }

    public static long getLong(Context ctx, int key, long defaultValue)
    {
        try
        {
            return Long.parseLong(getString(ctx, key, "" + defaultValue));
        } catch (Throwable err)
        {
            return defaultValue;
        }
    }

    public static void setLong(Context ctx, int key, long value)
    {
        setString(ctx, key, "" + value);
    }

    public static void setDouble(Context ctx, int key, double value)
    {
        setString(ctx, key, "" + value);
    }

    public static double getDouble(Context ctx, int key, double defaultValue)
    {
        try
        {
            return Double.parseDouble(getString(ctx, key, "" + defaultValue));
        } catch (Throwable err)
        {
            return defaultValue;
        }
    }

    public static boolean getBoolean(Context ctx, int key, boolean defaultValue)
    {
        try
        {
            return "1".equals(getString(ctx, key, defaultValue ? "1" : "0"));
        } catch (Throwable err)
        {
            return defaultValue;
        }
    }

    public static void setBoolean(Context ctx, int key, boolean value)
    {
        setString(ctx, key, value ? "1" : "0");
    }
}
