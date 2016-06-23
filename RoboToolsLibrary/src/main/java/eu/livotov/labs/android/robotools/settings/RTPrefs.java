package eu.livotov.labs.android.robotools.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.text.TextUtils;

import com.google.gson.Gson;

import java.util.StringTokenizer;

import eu.livotov.labs.android.robotools.text.RTBase64;

/**
 * Created by dlivotov on 30/08/2015.
 */
public class RTPrefs
{
    protected static RTPrefs defaultPreferences = null;

    protected SharedPreferences preferences;
    protected Context ctx;
    protected Gson gson = new Gson();

    public RTPrefs(@NonNull final Context ctx)
    {
        this(ctx, null, false);
    }

    public RTPrefs(@NonNull final Context ctx, @Nullable final String preferenceStorageName, boolean privateMode)
    {
        super();
        this.ctx = ctx.getApplicationContext();
        this.preferences = TextUtils.isEmpty(preferenceStorageName) ? PreferenceManager.getDefaultSharedPreferences(ctx) : ctx.getSharedPreferences(preferenceStorageName, getPrefsMode(privateMode));
    }

    protected static int getPrefsMode(boolean privateMode)
    {
        if (Build.VERSION.SDK_INT >= 11 && !privateMode)
        {
            return Context.MODE_MULTI_PROCESS;
        }
        else
        {
            return Context.MODE_PRIVATE;
        }
    }

    public RTPrefs(@NonNull final Context ctx, @Nullable final String preferenceStorageName)
    {
        this(ctx, preferenceStorageName, false);
    }

    public static synchronized RTPrefs getDefault(@NonNull final Context ctx)
    {
        if (defaultPreferences == null)
        {
            defaultPreferences = new RTPrefs(ctx, null);
        }

        return defaultPreferences;
    }

    public int getInt(@StringRes int key, int defaultValue)
    {
        return getInt(ctx.getString(key), defaultValue);
    }

    public int getInt(@NonNull String key, int defaultValue)
    {
        return preferences.getInt(key, defaultValue);
    }

    public void setInt(@StringRes int key, int value)
    {
        setInt(ctx.getString(key), value);
    }

    public void setInt(String key, int value)
    {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    public long getLong(@StringRes int key, long defaultValue)
    {
        return getLong(ctx.getString(key), defaultValue);
    }

    public long getLong(@NonNull String key, long defaultValue)
    {
        try
        {
            return preferences.getLong(key, defaultValue);
        }
        catch (Throwable err)
        {
            err.printStackTrace();
            return 0;
        }
    }

    public void setLong(@StringRes int key, long value)
    {
        setLong(ctx.getString(key), value);
    }

    public void setLong(@NonNull String key, long value)
    {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong(key, value);
        editor.commit();
    }

    public void setDouble(@StringRes int key, double value)
    {
        setDouble(ctx.getString(key), value);
    }

    public void setDouble(@NonNull String key, double value)
    {
        setString(key, "" + value);
    }

    public void setString(@NonNull String key, String value)
    {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public double getDouble(@StringRes int key, double defaultValue)
    {
        return getDouble(ctx.getString(key), defaultValue);
    }

    public double getDouble(@NonNull String key, double defaultValue)
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

    public String getString(@NonNull String key, String defaultValue)
    {
        return preferences.getString(key, defaultValue);
    }

    public boolean getBoolean(@StringRes int key, boolean defaultValue)
    {
        return getBoolean(ctx.getString(key), defaultValue);
    }

    public boolean getBoolean(@NonNull String key, boolean defaultValue)
    {
        return preferences.getBoolean(key, defaultValue);
    }

    public void setBoolean(@StringRes int key, boolean value)
    {
        setBoolean(ctx.getString(key), value);
    }

    public void setBoolean(@NonNull String key, boolean value)
    {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    public void setIntArray(@StringRes int key, int[] array)
    {
        setString(key, arrayToString(array));
    }

    public void setString(@StringRes int key, String value)
    {
        setString(ctx.getString(key), value);
    }

    protected String arrayToString(int[] array)
    {
        StringBuffer str = new StringBuffer();

        for (int a : array)
        {
            if (str.length() > 0)
            {
                str.append("|");
            }

            str.append("" + a);
        }

        return str.toString();
    }

    public void setIntArray(@NonNull String key, int[] array)
    {
        setString(key, arrayToString(array));
    }

    public void setLongArray(@StringRes int key, long[] array)
    {
        setString(key, arrayToString(array));
    }

    protected String arrayToString(long[] array)
    {
        StringBuffer str = new StringBuffer();

        for (long a : array)
        {
            if (str.length() > 0)
            {
                str.append("|");
            }

            str.append("" + a);
        }

        return str.toString();
    }

    public void setLongArray(@NonNull String key, long[] array)
    {
        setString(key, arrayToString(array));
    }

    public void setByteArray(@StringRes int key, byte[] array)
    {
        setString(key, RTBase64.encodeToString(array, RTBase64.NO_WRAP));
    }

    public void setByteArray(@NonNull String key, byte[] array)
    {
        setString(key, RTBase64.encodeToString(array, RTBase64.NO_WRAP));
    }

    public int[] getIntArray(@StringRes int key)
    {
        return stringToIntegerArray(getString(key, ""));
    }

    protected int[] stringToIntegerArray(final String str)
    {
        StringTokenizer tok = new StringTokenizer(str, "|", false);
        if (tok.countTokens() > 0)
        {
            int[] arr = new int[tok.countTokens()];
            int index = 0;
            while (tok.hasMoreTokens())
            {
                arr[index] = Integer.parseInt(tok.nextToken());
                index++;
            }

            return arr;
        }
        else
        {
            return null;
        }
    }

    public String getString(@StringRes int key, String defaultValue)
    {
        return getString(ctx.getString(key), defaultValue);
    }

    public int[] getIntArray(@NonNull String key)
    {
        return stringToIntegerArray(getString(key, ""));
    }

    public long[] getLongArray(@StringRes int key)
    {
        return stringToLongArray(getString(key, ""));
    }

    protected long[] stringToLongArray(final String str)
    {
        StringTokenizer tok = new StringTokenizer(str, "|", false);
        if (tok.countTokens() > 0)
        {
            long[] arr = new long[tok.countTokens()];
            int index = 0;
            while (tok.hasMoreTokens())
            {
                arr[index] = Long.parseLong(tok.nextToken());
                index++;
            }

            return arr;
        }
        else
        {
            return null;
        }
    }

    public long[] getLongArray(@NonNull String key)
    {
        return stringToLongArray(getString(key, ""));
    }

    public byte[] getByteArray(@StringRes int key)
    {
        return RTBase64.decode(getString(key, ""), RTBase64.NO_WRAP);
    }

    public byte[] getByteArray(@NonNull String key)
    {
        return RTBase64.decode(getString(key, ""), RTBase64.NO_WRAP);
    }

    public <T> T getObject(Class<T> clazz, @StringRes int key, T defaultValue)
    {
        return getObject(clazz, ctx.getString(key), defaultValue);
    }

    public <T extends Object> T getObject(Class<T> clazz, @NonNull String key, T defaultValue)
    {
        try
        {
            return gson.fromJson(getString(key, ""), clazz);
        }
        catch (Throwable ignored)
        {
            return defaultValue;
        }
    }

    public void setObject(@StringRes int key, Object object)
    {
        setObject(ctx.getString(key), object);
    }

    public void setObject(@NonNull String key, Object object)
    {
        try
        {
            if (object != null)
            {
                setString(key, gson.toJson(object));
            }
            else
            {
                remove(key);
            }
        }
        catch (Throwable err)
        {
            throw new IllegalArgumentException("Cannot convert to JSON: " + object.toString(), err);
        }
    }

    public void remove(@NonNull final String key)
    {
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(key);
        editor.apply();
    }

    public void remove(@StringRes final int key)
    {
        remove(ctx.getString(key));
    }

    public void clear()
    {
        preferences.edit().clear().apply();
    }

    public SharedPreferences getPreferences()
    {
        return preferences;
    }
}
