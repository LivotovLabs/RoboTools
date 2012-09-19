package eu.livotov.labs.android.robotools.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created with IntelliJ IDEA.
 * User: dlivotov
 * Date: 9/19/12
 * Time: 6:50 PM
 * To change this template use File | Settings | File Templates.
 */
public class RTPrefs
{
    public static SharedPreferences getPreferences(Context ctx)
    {
        return PreferenceManager.getDefaultSharedPreferences(ctx);
    }

    public static String getString(Context ctx, int key, String defaultValue)
    {
        return getPreferences(ctx).getString(ctx.getString(key), defaultValue);
    }

    public static void setString(Context ctx, int key, String value)
    {
        SharedPreferences.Editor editor = getPreferences(ctx).edit();
        editor.putString(ctx.getString(key), value);
        editor.commit();
    }

    public static int getInt(Context ctx, int key, int defaultValue)
    {
        return getPreferences(ctx).getInt(ctx.getString(key), defaultValue);
    }

    public static void setInt(Context ctx, int key, int value)
    {
        SharedPreferences.Editor editor = getPreferences(ctx).edit();
        editor.putInt(ctx.getString(key), value);
        editor.commit();
    }

    public static long getLong(Context ctx, int key, long defaultValue)
    {
        try
        {
            return getPreferences(ctx).getLong(ctx.getString(key), defaultValue);
        } catch (Throwable err)
        {
            err.printStackTrace();
            return 0;
        }
    }

    public static void setLong(Context ctx, int key, long value)
    {
        SharedPreferences.Editor editor = getPreferences(ctx).edit();
        editor.putLong(ctx.getString(key), value);
        editor.commit();
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
        return getPreferences(ctx).getBoolean(ctx.getString(key), defaultValue);
    }

    public static void setBoolean(Context ctx, int key, boolean value)
    {
        SharedPreferences.Editor editor = getPreferences(ctx).edit();
        editor.putBoolean(ctx.getString(key), value);
        editor.commit();
    }
}
