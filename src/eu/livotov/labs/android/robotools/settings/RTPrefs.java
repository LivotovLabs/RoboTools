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

    public static SharedPreferences getPreferences(final Context ctx)
    {
        return PreferenceManager.getDefaultSharedPreferences(ctx);
    }

    public static String getString(final Context ctx, int key, String defaultValue)
    {
        return getPreferences(ctx).getString(ctx.getString(key), defaultValue);
    }

    public static void setString(final Context ctx, int key, String value)
    {
        SharedPreferences.Editor editor = getPreferences(ctx).edit();
        editor.putString(ctx.getString(key), value);
        editor.commit();
    }

    public static int getInt(final Context ctx, int key, int defaultValue)
    {
        return getPreferences(ctx).getInt(ctx.getString(key), defaultValue);
    }

    public static void setInt(final Context ctx, int key, int value)
    {
        SharedPreferences.Editor editor = getPreferences(ctx).edit();
        editor.putInt(ctx.getString(key), value);
        editor.commit();
    }

    public static long getLong(final Context ctx, int key, long defaultValue)
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

    public static void setLong(final Context ctx, int key, long value)
    {
        SharedPreferences.Editor editor = getPreferences(ctx).edit();
        editor.putLong(ctx.getString(key), value);
        editor.commit();
    }

    public static void setDouble(final Context ctx, int key, double value)
    {
        setString(ctx, key, "" + value);
    }

    public static double getDouble(final Context ctx, int key, double defaultValue)
    {
        try
        {
            return Double.parseDouble(getString(ctx, key, "" + defaultValue));
        } catch (Throwable err)
        {
            return defaultValue;
        }
    }

    public static boolean getBoolean(final Context ctx, int key, boolean defaultValue)
    {
        return getPreferences(ctx).getBoolean(ctx.getString(key), defaultValue);
    }

    public static void setBoolean(final Context ctx, int key, boolean value)
    {
        SharedPreferences.Editor editor = getPreferences(ctx).edit();
        editor.putBoolean(ctx.getString(key), value);
        editor.commit();
    }

    public static void remove(final Context ctx, final int key)
    {
        SharedPreferences.Editor editor = getPreferences(ctx).edit();
        editor.remove(ctx.getString(key));
        editor.commit();
    }
}
