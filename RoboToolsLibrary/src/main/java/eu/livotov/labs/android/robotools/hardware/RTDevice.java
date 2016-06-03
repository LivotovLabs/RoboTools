package eu.livotov.labs.android.robotools.hardware;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;

import java.io.File;
import java.io.FileFilter;
import java.util.regex.Pattern;

@SuppressWarnings("unused")
public class RTDevice
{

    private static int cachedCoresCount = 0;

    private RTDevice()
    {
    }

    public static boolean isTablet(Context context)
    {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();

        int shortSizeDp = (int) (displayMetrics.density * Math.min(displayMetrics.widthPixels, displayMetrics.heightPixels));

        return shortSizeDp > 1024;
    }

    public static float dp2px(Context ctx, float px)
    {
        return px / ctx.getResources().getDisplayMetrics().density;
    }

    public static float px2dp(Context ctx, float dp)
    {
        return dp * ctx.getResources().getDisplayMetrics().density;
    }

    public static boolean supportsCamera(Context ctx)
    {
        return ctx.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

    public static boolean supportsTelephony(Context ctx)
    {
        return ctx.getPackageManager().hasSystemFeature(PackageManager.FEATURE_TELEPHONY);
    }

    public static boolean supportsGps(Context ctx)
    {
        return ctx.getPackageManager().hasSystemFeature(PackageManager.FEATURE_LOCATION);
    }

    public static boolean supportsSms(Context ctx)
    {
        try
        {
            if (ctx.getPackageManager().hasSystemFeature(PackageManager.FEATURE_TELEPHONY))
            {
                TelephonyManager telMgr = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
                int simState = telMgr.getSimState();
                switch (simState)
                {
                    case TelephonyManager.SIM_STATE_ABSENT:
                    case TelephonyManager.SIM_STATE_NETWORK_LOCKED:
                    case TelephonyManager.SIM_STATE_PIN_REQUIRED:
                    case TelephonyManager.SIM_STATE_PUK_REQUIRED:
                    case TelephonyManager.SIM_STATE_UNKNOWN:
                        return false;

                    case TelephonyManager.SIM_STATE_READY:
                        return true;

                    default:
                        return true;
                }
            }

            return false;
        }
        catch (Throwable err)
        {
            err.printStackTrace();
            return false;
        }
    }

    public synchronized static int getCpuCoresCount()
    {
        if (cachedCoresCount == 0)
        {
            try
            {
                File dir = new File("/sys/devices/system/cpu/");
                File[] files = dir.listFiles(new CpuFilter());
                cachedCoresCount = files.length;
            }
            catch (Exception e)
            {
                Log.e(RTDevice.class.getSimpleName(), e.getMessage(), e);
                cachedCoresCount = 1;
            }
        }

        return cachedCoresCount;
    }

    public static boolean isExternalStorageReady()
    {
        final String state = Environment.getExternalStorageState();
        return !(Environment.MEDIA_REMOVED.equals(state) || Environment.MEDIA_BAD_REMOVAL.equals(state) || Environment.MEDIA_UNMOUNTABLE.equals(state) || Environment.MEDIA_UNMOUNTED.equals(state));
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

    public static boolean isConnected(final Context ctx)
    {
        final ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected();
    }

    public static boolean isConnectedToWiFi(final Context ctx)
    {
        final ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected() && netInfo.getType() == ConnectivityManager.TYPE_WIFI;
    }

    public static boolean isConnectedToCellular(final Context ctx)
    {
        final ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected() && netInfo.getType() != ConnectivityManager.TYPE_WIFI;
    }

    public static boolean isInRoaming(final Context ctx)
    {
        final ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.getType() != ConnectivityManager.TYPE_WIFI && netInfo.isRoaming();
    }

    public static CharSequence getOwnerEmail(Context context)
    {
        Account[] accounts = AccountManager.get(context).getAccountsByType("com.google");
        return accounts != null && accounts.length > 0 ? accounts[0].name : null;
    }

    private static class CpuFilter implements FileFilter
    {

        public boolean accept(File pathname)
        {
            return Pattern.matches("cpu[0-9]", pathname.getName());
        }
    }

    public static boolean isBlackberryDevice()
    {
        if (!System.getProperty("os.name").equals("qnx"))
        {
            return android.os.Build.BRAND.toLowerCase().contains("blackberry");
        }
        else
        {
            return true;
        }
    }
}