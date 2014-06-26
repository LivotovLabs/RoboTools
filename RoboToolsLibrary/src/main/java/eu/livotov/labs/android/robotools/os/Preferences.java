package eu.livotov.labs.android.robotools.os;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import eu.livotov.labs.android.robotools.security.CryptUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.StringTokenizer;

@SuppressWarnings("unused")
public class Preferences {

    private static Preferences defaultPreferences = null;

    protected SharedPreferences preferences;
    protected Context ctx;

    public static synchronized Preferences getDefault(final Context ctx) {
        if (defaultPreferences == null) {
            defaultPreferences = new Preferences(ctx, null);
        }

        return defaultPreferences;
    }

    public Preferences(final Context ctx) {
        this(ctx, null, false);
    }

    public Preferences(final Context ctx, final String preferenceStorageName) {
        this(ctx, preferenceStorageName, false);
    }

    public Preferences(final Context ctx, final String preferenceStorageName, boolean privateMode) {
        super();
        this.ctx = ctx.getApplicationContext();
        this.preferences = TextUtils.isEmpty(preferenceStorageName) ? PreferenceManager.getDefaultSharedPreferences(ctx) : ctx.getSharedPreferences(preferenceStorageName, getPrefsMode(privateMode));
    }

    public String getString(int key, String defaultValue) {
        return preferences.getString(ctx.getString(key), defaultValue);
    }

    public void setString(int key, String value) {
        preferences.edit().
                putString(ctx.getString(key), value).
                commit();
    }

    public int getInt(int key, int defaultValue) {
        return preferences.getInt(ctx.getString(key), defaultValue);
    }

    public void setInt(int key, int value) {
        preferences.edit().
                putInt(ctx.getString(key), value).
                commit();
    }

    public long getLong(int key, long defaultValue) {
        try {
            return preferences.getLong(ctx.getString(key), defaultValue);
        } catch (Throwable err) {
            err.printStackTrace();
            return 0;
        }
    }

    public void setLong(int key, long value) {
        preferences.edit().
                putLong(ctx.getString(key), value).
                commit();
    }

    public void setDouble(int key, double value) {
        setString(key, String.valueOf(value));
    }

    public double getDouble(int key, double defaultValue) {
        try {
            return Double.parseDouble(getString(key, String.valueOf(defaultValue)));
        } catch (Throwable err) {
            return defaultValue;
        }
    }

    public boolean getBoolean(int key, boolean defaultValue) {
        return preferences.getBoolean(ctx.getString(key), defaultValue);
    }

    public void setBoolean(int key, boolean value) {
        preferences.edit().
                putBoolean(ctx.getString(key), value).
                commit();
    }

    public void setIntArray(int key, int[] array) {
        setString(key, arrayToString(array));
    }

    public void setLongArray(int key, long[] array) {
        setString(key, arrayToString(array));
    }

    public void setByteArray(int key, byte[] array) {
        setString(key, CryptUtil.toBase64(array));
    }

    public int[] getIntArray(int key) {
        return stringToIntegerArray(getString(key, ""));
    }

    public long[] getLongArray(int key) {
        return stringToLongArray(getString(key, ""));
    }

    public byte[] getByteArray(int key) {
        return CryptUtil.fromBase64(getString(key, ""));
    }

    public void setSerializable(int key, Serializable serializable) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(bos);
            out.writeObject(serializable);
            setByteArray(key, bos.toByteArray());
            out.close();
            bos.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Serializable getSerializable(int key) {
        try {
            byte[] array = getByteArray(key);
            if(array == null || array.length == 0) {
                return null;
            }
            ByteArrayInputStream bis = new ByteArrayInputStream(array);
            ObjectInput in = new ObjectInputStream(bis);
            Serializable result = (Serializable) in.readObject();
            in.close();
            bis.close();
            return result;
        } catch (IllegalArgumentException e) {
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    public void remove(final int key) {
        preferences.edit().
                remove(ctx.getString(key)).
                commit();
    }

    public void clear() {
        preferences.edit().clear().commit();
    }

    public SharedPreferences getPreferences() {
        return preferences;
    }

    protected static int getPrefsMode(boolean privateMode) {
        if (Build.VERSION.SDK_INT >= 11 && !privateMode) {
            return Context.MODE_MULTI_PROCESS;
        } else {
            return Context.MODE_PRIVATE;
        }
    }

    protected String arrayToString(int[] array) {
        StringBuilder str = new StringBuilder();
        for (int a : array) {
            if (str.length() > 0) {
                str.append("|");
            }
            str.append(a);
        }

        return str.toString();
    }

    protected String arrayToString(long[] array) {
        StringBuilder str = new StringBuilder();
        for (long a : array) {
            if (str.length() > 0) {
                str.append("|");
            }
            str.append(a);
        }
        return str.toString();
    }

    protected int[] stringToIntegerArray(final String str) {
        StringTokenizer tok = new StringTokenizer(str, "|", false);
        if (tok.countTokens() > 0) {
            int[] arr = new int[tok.countTokens()];
            int index = 0;
            while (tok.hasMoreTokens()) {
                arr[index] = Integer.parseInt(tok.nextToken());
                index++;
            }

            return arr;
        } else {
            return null;
        }
    }

    protected long[] stringToLongArray(final String str) {
        StringTokenizer tok = new StringTokenizer(str, "|", false);
        if (tok.countTokens() > 0) {
            long[] arr = new long[tok.countTokens()];
            int index = 0;
            while (tok.hasMoreTokens()) {
                arr[index] = Long.parseLong(tok.nextToken());
                index++;
            }

            return arr;
        } else {
            return null;
        }
    }
}
