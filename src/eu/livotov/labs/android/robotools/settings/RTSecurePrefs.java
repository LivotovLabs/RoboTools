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

    public static String getString(Context ctx, int key)
    {
        return RTCryptUtil.decrypt(ekey,RTPrefs.getString(ctx,key,""));
    }

    public static void setString(Context ctx, int key, String value)
    {
        RTPrefs.setString(ctx,key,RTCryptUtil.encrypt(ekey,value));
    }

    public static int getInt(Context ctx, int key)
    {
        return Integer.parseInt(getString(ctx,key));
    }

    public static void setInt(Context ctx, int key, int value)
    {
        setString(ctx,key,""+value);
    }

    public static long getLong(Context ctx, int key)
    {
        return Long.parseLong(getString(ctx,key));
    }

    public static void setLong(Context ctx, int key, long value)
    {
        setString(ctx,key,""+value);
    }

    public static void setDouble(Context ctx, int key, double value)
    {
        setString(ctx,key,""+value);
    }

    public static double getDouble(Context ctx, int key)
    {
        return Double.parseDouble(getString(ctx,key));
    }

    public static boolean getBoolean(Context ctx, int key)
    {
        return "1".equals(getString(ctx,key));
    }

    public static void setBoolean(Context ctx, int key, boolean value)
    {
        setString(ctx,key,value ? "1":"0");
    }
}
