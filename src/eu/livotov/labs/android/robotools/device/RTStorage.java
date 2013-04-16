package eu.livotov.labs.android.robotools.device;

import android.os.Environment;

import java.io.File;

/**
 * (c) Livotov Labs Ltd. 2012
 * Date: 16.04.13
 */
public class RTStorage
{

    public static boolean isExternalStorageReady()
    {
        final String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_REMOVED.equals(state)
                    || Environment.MEDIA_BAD_REMOVAL.equals(state)
                    || Environment.MEDIA_UNMOUNTABLE.equals(state)
                    || Environment.MEDIA_UNMOUNTED.equals(state))
        {
            return false;
        }
        return true;
    }

    public static File getExternalStorage()
    {
        return getExternalStorage(null);
    }

    public static File getExternalStorage(final String type)
    {
        File file = Environment.getExternalStoragePublicDirectory(type);

        if (!file.exists())
        {
            file.mkdirs();
        }

        return file;
    }
}
