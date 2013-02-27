package eu.livotov.labs.android.robotools.settings;

import android.content.Context;
import android.text.TextUtils;
import eu.livotov.labs.android.robotools.crypt.RTBase64;
import eu.livotov.labs.android.robotools.crypt.RTCryptUtil;

import java.util.StringTokenizer;

/**
 * Created with IntelliJ IDEA.
 * User: dlivotov
 * Date: 9/19/12
 * Time: 6:50 PM
 * To change this template use File | Settings | File Templates.
 */
public class RTSecurePrefs {

    private static String ekey;

    public static void setPassword(final Context ctx,
                                   final String password,
                                   boolean lockToDevice,
                                   boolean lockToWifiAddress,
                                   boolean lockToTelephony,
                                   boolean lockToSIM) {
        try {
            ekey = lockToDevice ? RTCryptUtil.generateDeviceBoundEncryptionKeyForPassword(ctx,
                    password,
                    lockToWifiAddress,
                    lockToTelephony,
                    lockToSIM) : password;
        } catch (Throwable err) {
            throw new RuntimeException(err);
        }
    }

    public static String getString(Context ctx, int key, final String defaultValue) {
        try {
            return RTCryptUtil.decryptAsText(RTPrefs.getString(ctx, key, ""), ekey);
        } catch (Throwable err) {
            return defaultValue;
        }
    }

    public static void setString(Context ctx, int key, String value) {
        if (TextUtils.isEmpty(value)) {
            RTPrefs.remove(ctx, key);
        } else {
            RTPrefs.setString(ctx, key, RTCryptUtil.encrypt(value, ekey));
        }
    }

    public static int getInt(Context ctx, int key, int defaultValue) {
        try {
            return Integer.parseInt(getString(ctx, key, "" + defaultValue));
        } catch (Throwable err) {
            return defaultValue;
        }
    }

    public static void setInt(Context ctx, int key, int value) {
        setString(ctx, key, "" + value);
    }

    public static long getLong(Context ctx, int key, long defaultValue) {
        try {
            return Long.parseLong(getString(ctx, key, "" + defaultValue));
        } catch (Throwable err) {
            return defaultValue;
        }
    }

    public static void setIntArray(Context ctx, int key, int[] array) {
        setString(ctx, key, arrayToString(array));
    }

    public static void setLongArray(Context ctx, int key, long[] array) {
        setString(ctx, key, arrayToString(array));
    }

    public static void setByteArray(Context ctx, int key, byte[] array) {
        setString(ctx, key, RTBase64.encodeToString(array, RTBase64.NO_WRAP));
    }

    public static int[] getIntArray(Context ctx, int key) {
        return stringToIntegerArray(getString(ctx, key, ""));
    }

    public static long[] getLongArray(Context ctx, int key) {
        return stringToLongArray(getString(ctx, key, ""));
    }

    public static byte[] getByteArray(Context ctx, int key) {
        return RTBase64.decode(getString(ctx, key, ""), RTBase64.NO_WRAP);
    }

    public static void setLong(Context ctx, int key, long value) {
        setString(ctx, key, "" + value);
    }

    public static void setDouble(Context ctx, int key, double value) {
        setString(ctx, key, "" + value);
    }

    public static double getDouble(Context ctx, int key, double defaultValue) {
        try {
            return Double.parseDouble(getString(ctx, key, "" + defaultValue));
        } catch (Throwable err) {
            return defaultValue;
        }
    }

    public static boolean getBoolean(Context ctx, int key, boolean defaultValue) {
        try {
            return "1".equals(getString(ctx, key, defaultValue ? "1" : "0"));
        } catch (Throwable err) {
            return defaultValue;
        }
    }

    public static void setBoolean(Context ctx, int key, boolean value) {
        setString(ctx, key, value ? "1" : "0");
    }

    private static String arrayToString(int[] array) {
        StringBuffer str = new StringBuffer();

        for (int a : array) {
            if (str.length() > 0) {
                str.append("|");
            }

            str.append("" + a);
        }

        return str.toString();
    }

    private static String arrayToString(long[] array) {
        StringBuffer str = new StringBuffer();

        for (long a : array) {
            if (str.length() > 0) {
                str.append("|");
            }

            str.append("" + a);
        }

        return str.toString();
    }

    private static int[] stringToIntegerArray(final String str) {
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

    private static long[] stringToLongArray(final String str) {
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
