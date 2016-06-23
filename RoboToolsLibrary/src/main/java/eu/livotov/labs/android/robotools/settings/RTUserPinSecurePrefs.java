package eu.livotov.labs.android.robotools.settings;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.text.TextUtils;
import android.util.Log;

import java.util.Set;

import eu.livotov.labs.android.robotools.crypt.RTCryptUtil;
import eu.livotov.labs.android.robotools.text.RTBase64;

/**
 * RTSecurePrefs, enchanced with the user-specified pin or password. The password is used as additional data encryption, so without specifying the password it will be impossible to
 * read them back later. The preferences file created by this class is transferable, e.g. if you know the PIN code, you'll be able to read it on another device or after the google's backup/restore process
 */
public class RTUserPinSecurePrefs extends RTSecurePrefs
{
    private final static String PIN_PRESENT_KEY = "pincheck";
    private final static String MASK = "f8989fuojJKLJLKS09-0983";

    private String locker;

    /**
     * Creates transferable prefs objects.
     *
     * @param ctx
     * @param preferenceStorageName custom file name for prefs
     */
    public RTUserPinSecurePrefs(Context ctx, @NonNull String preferenceStorageName)
    {
        super(ctx, preferenceStorageName, true);
    }

    /**
     * Checks if the preferences are locked and password need to be specified in order to read or write the values. If no password was set, preferences are treated as unlocked.
     *
     * @return <code>true</code> if preferences are locked and password required to start using them.
     */
    public boolean isLocked()
    {
        return prefs.getBoolean(PIN_PRESENT_KEY, false) && !TextUtils.isEmpty(locker);
    }

    /**
     * Unlocks the preferences with the user password. If no-password was set, this method will return success on any password. Preferences will be unlocked until you call the <code>lock()</code> method or recreate the instance of RTUserPinSecurePrefs
     *
     * @param password user password to unlock the preferences
     */
    public boolean unlock(final String password)
    {
        if (isLocked())
        {
            try
            {
                Set<String> keys = prefs.getPreferences().getAll().keySet();

                for (String key : keys)
                {
                    if (!PIN_PRESENT_KEY.equalsIgnoreCase(key))
                    {
                        final String originalTest = "" + super.getString(key, "");

                        if (!TextUtils.isEmpty(originalTest))
                        {
                            RTCryptUtil.decryptAsText(super.getString(key, ""), password);
                            break;
                        }
                    }
                }

                locker = RTCryptUtil.encrypt(password, MASK);
                return true;
            }
            catch (Throwable err)
            {
                Log.d(RTUserPinSecurePrefs.class.getSimpleName(), err.getMessage());
                return false;
            }
        }
        else
        {
            return true;
        }
    }

    /**
     * Locks the previously unlocked preferences, so one have to unlock it again with the user-password in order to start using them again.
     * If no password is set, simply does nothing.
     */
    public void lock()
    {
        if (!TextUtils.isEmpty(locker))
        {
            locker = null;
        }
    }

    /**
     * Sets new, changes old one or removes the user-password from the preferences. After the operation, preferences file stays in unlocked state.
     *
     * @param oldPassword old password. Use <code>null</code> if no password was previously set, e.g.  you're assigning a password for the first time.
     * @param newPassword new password. Use <code>null</code> in case you want to remove the password at all.
     */
    public void setPassword(final String oldPassword, final String newPassword)
    {
        final boolean markerPresent = prefs.getBoolean(PIN_PRESENT_KEY, false);

        if (markerPresent)
        {
            // Password change
            if (TextUtils.isEmpty(oldPassword))
            {
                throw new IllegalArgumentException("Preferences are encrypted. In order to change the password you must provide an old one for security purposes !");
            }

            lock();

            if (unlock(oldPassword))
            {
                Set<String> keys = prefs.getPreferences().getAll().keySet();
                for (String key : keys)
                {
                    if (!PIN_PRESENT_KEY.equalsIgnoreCase(key))
                    {
                        if (TextUtils.isEmpty(newPassword))
                        {
                            super.setString(key, RTCryptUtil.decryptAsText(super.getString(key, ""), oldPassword));
                        }
                        else
                        {
                            super.setString(key, RTCryptUtil.encrypt(RTCryptUtil.decryptAsText(super.getString(key, ""), oldPassword), newPassword));
                        }
                    }
                }

                if (TextUtils.isEmpty(newPassword))
                {
                    locker = null;
                    prefs.remove(PIN_PRESENT_KEY);
                }
                else
                {
                    locker = RTCryptUtil.encrypt(newPassword, MASK);
                }
            }
            else
            {
                throw new IllegalArgumentException("Preferences are encrypted. Current password is invalid !");
            }
        }
        else
        {
            // New password assignment
            if (TextUtils.isEmpty(newPassword))
            {
                throw new IllegalArgumentException("When setting up the new password, it cannot be emoty !");
            }

            Set<String> keys = prefs.getPreferences().getAll().keySet();
            for (String key : keys)
            {
                if (!PIN_PRESENT_KEY.equalsIgnoreCase(key))
                {
                    super.setString(key, RTCryptUtil.encrypt(super.getString(key, ""), newPassword));
                    locker = RTCryptUtil.encrypt(newPassword, MASK);
                    prefs.setBoolean(PIN_PRESENT_KEY, true);
                }
            }
        }
    }

    @Override
    public String getString(@StringRes int key, String defaultValue)
    {
        return userDecryptString(super.getString(key, defaultValue));
    }

    @Override
    public String getString(@NonNull String key, String defaultValue)
    {
        return userDecryptString(super.getString(key, defaultValue));
    }

    @Override
    public int getInt(@StringRes int key, int defaultValue)
    {
        try
        {
            return Integer.parseInt(getString(key, "" + defaultValue));
        }
        catch (NumberFormatException err)
        {
            return defaultValue;
        }
    }

    @Override
    public int getInt(@NonNull String key, int defaultValue)
    {
        try
        {
            return Integer.parseInt(getString(key, "" + defaultValue));
        }
        catch (NumberFormatException err)
        {
            return defaultValue;
        }
    }

    @Override
    public void setInt(@StringRes int key, int value)
    {
        setString(key, "" + value);
    }

    @Override
    public void setString(@StringRes int key, String value)
    {
        super.setString(key, userEncryptString(value));
    }

    @Override
    public void setString(@NonNull String key, String value)
    {
        super.setString(key, userEncryptString(value));
    }

    @Override
    public void reset()
    {
        //todo: keep secret markers
        super.reset();
    }

    @Override
    public void setInt(@NonNull String key, int value)
    {
        setString(key, "" + value);
    }

    @Override
    public long getLong(@StringRes int key, long defaultValue)
    {
        try
        {
            return Long.parseLong(getString(key, "" + defaultValue));
        }
        catch (NumberFormatException e)
        {
            return defaultValue;
        }
    }

    @Override
    public long getLong(@NonNull String key, long defaultValue)
    {
        try
        {
            return Long.parseLong(getString(key, "" + defaultValue));
        }
        catch (NumberFormatException e)
        {
            return defaultValue;
        }
    }

    @Override
    public void setIntArray(@StringRes int key, int[] array)
    {
        setString(key, RTPrefs.arrayToString(array));
    }

    @Override
    public void setIntArray(@NonNull String key, int[] array)
    {
        setString(key, RTPrefs.arrayToString(array));
    }

    @Override
    public void setLongArray(@StringRes int key, long[] array)
    {
        setString(key, RTPrefs.arrayToString(array));
    }

    @Override
    public void setLongArray(@NonNull String key, long[] array)
    {
        setString(key, RTPrefs.arrayToString(array));
    }

    @Override
    public void setByteArray(@StringRes int key, byte[] array)
    {
        setString(key, RTBase64.encodeToString(array, RTBase64.NO_WRAP));
    }

    @Override
    public void setByteArray(@NonNull String key, byte[] array)
    {
        setString(key, RTBase64.encodeToString(array, RTBase64.NO_WRAP));
    }

    @Override
    public int[] getIntArray(@StringRes int key)
    {
        return RTPrefs.stringToIntegerArray(getString(key, ""));
    }

    @Override
    public int[] getIntArray(@NonNull String key)
    {
        return RTPrefs.stringToIntegerArray(getString(key, ""));
    }

    @Override
    public long[] getLongArray(@StringRes int key)
    {
        return RTPrefs.stringToLongArray(getString(key, ""));
    }

    @Override
    public long[] getLongArray(@NonNull String key)
    {
        return RTPrefs.stringToLongArray(getString(key, ""));
    }

    @Override
    public byte[] getByteArray(@StringRes int key)
    {
        return RTBase64.decode(getString(key, ""), RTBase64.NO_WRAP);
    }

    @Override
    public byte[] getByteArray(@NonNull String key)
    {
        return RTBase64.decode(getString(key, ""), RTBase64.NO_WRAP);
    }

    @Override
    public void setLong(@StringRes int key, long value)
    {
        setString(key, "" + value);
    }

    @Override
    public void setLong(@NonNull String key, long value)
    {
        setString(key, "" + value);
    }

    @Override
    public void setDouble(@StringRes int key, double value)
    {
        setString(key, "" + value);
    }

    @Override
    public void setDouble(String key, double value)
    {
        setString(key, "" + value);
    }

    @Override
    public double getDouble(@StringRes int key, double defaultValue)
    {
        try
        {
            return Double.parseDouble(getString(key, "" + defaultValue));
        }
        catch (NumberFormatException e)
        {
            return defaultValue;
        }
    }

    @Override
    public double getDouble(@NonNull String key, double defaultValue)
    {
        try
        {
            return Double.parseDouble(getString(key, "" + defaultValue));
        }
        catch (NumberFormatException e)
        {
            return defaultValue;
        }
    }

    @Override
    public boolean getBoolean(@StringRes int key, boolean defaultValue)
    {
        final String dv = getString(key, "D");

        if ("D".equalsIgnoreCase(dv))
        {
            return defaultValue;
        }
        else
        {
            return "1".equalsIgnoreCase(dv);
        }
    }

    @Override
    public boolean getBoolean(@NonNull String key, boolean defaultValue)
    {
        final String dv = getString(key, "D");

        if ("D".equalsIgnoreCase(dv))
        {
            return defaultValue;
        }
        else
        {
            return "1".equalsIgnoreCase(dv);
        }
    }

    @Override
    public void setBoolean(@StringRes int key, boolean value)
    {
        setString(key, value ? "1" : "0");
    }

    @Override
    public void setBoolean(@NonNull String key, boolean value)
    {
        setString(key, value ? "1" : "0");
    }

    @Override
    public <T> T getObject(Class<T> clazz, @StringRes int key, T defaultValue)
    {
        final String json = getString(key, null);

        try
        {
            return prefs.gson.fromJson(json, clazz);
        }
        catch (Throwable err)
        {
            return defaultValue;
        }
    }

    @Override
    public <T> T getObject(Class<T> clazz, @NonNull String key, T defaultValue)
    {
        final String json = getString(key, null);

        try
        {
            return prefs.gson.fromJson(json, clazz);
        }
        catch (Throwable err)
        {
            return defaultValue;
        }
    }

    @Override
    public void setObject(@StringRes int key, Object object)
    {
        setString(key, prefs.gson.toJson(object));
    }

    @Override
    public void setObject(@NonNull String key, Object object)
    {
        setString(key, prefs.gson.toJson(object));
    }

    private String userEncryptString(final String src)
    {
        final boolean markerPresent = prefs.getBoolean(PIN_PRESENT_KEY, false);

        if (markerPresent)
        {
            if (!TextUtils.isEmpty(locker))
            {
                return RTCryptUtil.encrypt(src, RTCryptUtil.decryptAsText(locker, MASK));
            }
            else
            {
                throw new RuntimeException("Preferences are locked. Call unlock(password) first !");
            }
        }
        else
        {
            return src;
        }
    }

    private String userDecryptString(final String src)
    {
        final boolean markerPresent = prefs.getBoolean(PIN_PRESENT_KEY, false);

        if (markerPresent)
        {
            if (!TextUtils.isEmpty(locker))
            {
                return RTCryptUtil.decryptAsText(src, RTCryptUtil.decryptAsText(locker, MASK));
            }
            else
            {
                throw new RuntimeException("Preferences are locked. Call unlock(password) first !");
            }
        }
        else
        {
            return src;
        }
    }
}
