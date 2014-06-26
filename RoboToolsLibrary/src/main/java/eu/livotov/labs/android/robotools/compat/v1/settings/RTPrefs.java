package eu.livotov.labs.android.robotools.compat.v1.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import eu.livotov.labs.android.robotools.compat.v1.crypt.RTBase64;

import java.util.StringTokenizer;

/**
 * Created with IntelliJ IDEA.
 * User: dlivotov
 * Date: 9/19/12
 * Time: 6:50 PM
 * To change this template use File | Settings | File Templates.
 */
public class RTPrefs
{

    private static RTPrefs defaultPreferences = null;

    protected SharedPreferences preferences;
    protected Context ctx;

    public static synchronized RTPrefs getDefault(final Context ctx)
    {
        if (defaultPreferences == null)
        {
            defaultPreferences = new RTPrefs(ctx, null);
        }

        return defaultPreferences;
    }

    public RTPrefs(final Context ctx)
    {
        this(ctx, null, false);
    }

    public RTPrefs(final Context ctx, final String preferenceStorageName)
    {
        this(ctx, preferenceStorageName, false);
    }

    public RTPrefs(final Context ctx, final String preferenceStorageName, boolean privateMode)
    {
        super();
        this.ctx = ctx;
        this.preferences = TextUtils.isEmpty(preferenceStorageName) ? PreferenceManager.getDefaultSharedPreferences(ctx) : ctx.getSharedPreferences(preferenceStorageName, getPrefsMode(privateMode));
    }

    public String getString(int key, String defaultValue)
    {
        return preferences.getString(ctx.getString(key), defaultValue);
    }

    public void setString(int key, String value)
    {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(ctx.getString(key), value);
        editor.commit();
    }

    public int getInt(int key, int defaultValue)
    {
        return preferences.getInt(ctx.getString(key), defaultValue);
    }

    public void setInt(int key, int value)
    {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(ctx.getString(key), value);
        editor.commit();
    }

    public long getLong(int key, long defaultValue)
    {
        try
        {
            return preferences.getLong(ctx.getString(key), defaultValue);
        } catch (Throwable err)
        {
            err.printStackTrace();
            return 0;
        }
    }

    public void setLong(int key, long value)
    {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong(ctx.getString(key), value);
        editor.commit();
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
        return preferences.getBoolean(ctx.getString(key), defaultValue);
    }

    public void setBoolean(int key, boolean value)
    {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(ctx.getString(key), value);
        editor.commit();
    }

    public void setIntArray(Context ctx, int key, int[] array)
    {
        setString(key, arrayToString(array));
    }

    public void setLongArray(Context ctx, int key, long[] array)
    {
        setString(key, arrayToString(array));
    }

    public void setByteArray(Context ctx, int key, byte[] array)
    {
        setString(key, RTBase64.encodeToString(array, RTBase64.NO_WRAP));
    }

    public int[] getIntArray(Context ctx, int key)
    {
        return stringToIntegerArray(getString(key, ""));
    }

    public long[] getLongArray(Context ctx, int key)
    {
        return stringToLongArray(getString(key, ""));
    }

    public byte[] getByteArray(Context ctx, int key)
    {
        return RTBase64.decode(getString(key, ""), RTBase64.NO_WRAP);
    }

    public void remove(final int key)
    {
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(ctx.getString(key));
        editor.commit();
    }

    public void clear()
    {
        preferences.edit().clear().commit();
    }

    public SharedPreferences getPreferences()
    {
        return preferences;
    }

    protected static int getPrefsMode(boolean privateMode)
    {
        if (Build.VERSION.SDK_INT >= 11 && !privateMode)
        {
            return Context.MODE_MULTI_PROCESS;
        } else
        {
            return Context.MODE_PRIVATE;
        }
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
        } else
        {
            return null;
        }
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
        } else
        {
            return null;
        }
    }
}
