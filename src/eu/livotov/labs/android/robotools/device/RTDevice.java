package eu.livotov.labs.android.robotools.device;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import eu.livotov.labs.android.robotools.crypt.RTCryptUtil;

import java.io.File;
import java.io.FileFilter;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: dlivotov
 * Date: 28.10.12
 * Time: 20:35
 * To change this template use File | Settings | File Templates.
 */
public class RTDevice
{

    private static int cachedCoresCount = 0;

    public static boolean isTablet(Activity activity)
    {
        Display display = activity.getWindowManager().getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getMetrics(displayMetrics);

        int shortSizeDp = (int) px2dp(activity, Math.min(displayMetrics.widthPixels, displayMetrics.heightPixels));

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

    public static String getDeviceUID(final Context ctx, boolean countOnNetworkInterfaces, boolean countOnTelephony,
                                      boolean countOnSim)
    {
        StringBuffer id = new StringBuffer();

        // requires android.permission.ACCESS_WIFI_STATE
        if (countOnNetworkInterfaces && ctx != null)
        {
            WifiManager wm = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
            id.append(wm.getConnectionInfo().getMacAddress());
        }

        // requires android.permission.READ_PHONE_STATE
        if (countOnTelephony && ctx != null)
        {
            TelephonyManager telephonyManager = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
            if (telephonyManager != null)
            {
                id.append(telephonyManager.getDeviceId() != null ? telephonyManager.getDeviceId() : "");
            }
        }

        // requires android.permission.READ_PHONE_STATE
        if (countOnSim && ctx != null)
        {
            TelephonyManager telephonyManager = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
            id.append(telephonyManager.getSimSerialNumber() != null ? telephonyManager.getSimSerialNumber() : "");
        }

        if (ctx != null)
        {
            id.append(Settings.Secure.getString(ctx.getContentResolver(), Settings.Secure.ANDROID_ID));
        }

        id.append(Build.BOARD.length() % 10 + Build.BRAND.length() % 10 +
                          Build.CPU_ABI.length() % 10 + Build.DEVICE.length() % 10 +
                          Build.DISPLAY.length() % 10 + Build.HOST.length() % 10 +
                          Build.ID.length() % 10 + Build.MANUFACTURER.length() % 10 +
                          Build.MODEL.length() % 10 + Build.PRODUCT.length() % 10 +
                          Build.TAGS.length() % 10 + Build.TYPE.length() % 10 +
                          Build.USER.length() % 10);

        if (Build.VERSION.SDK_INT > 8)
        {
            id.append(Build.SERIAL);
        }

        return RTCryptUtil.md5(id.toString());
    }

    public static boolean supportsCamera(Context ctx)
    {
        PackageManager pm = ctx.getPackageManager();

        if (pm.hasSystemFeature(PackageManager.FEATURE_CAMERA))
        {
            return true;
        }
        return false;
    }

    public static boolean supportsTelephony(Context ctx)
    {
        PackageManager pm = ctx.getPackageManager();

        if (pm.hasSystemFeature(PackageManager.FEATURE_TELEPHONY))
        {
            return true;
        }
        return false;
    }

    public static boolean supportsGps(Context ctx)
    {
        PackageManager pm = ctx.getPackageManager();

        if (pm.hasSystemFeature(PackageManager.FEATURE_LOCATION))
        {
            return true;
        }
        return false;
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
        } catch (Throwable err)
        {
            err.printStackTrace();
            return false;
        }
    }

    public static boolean isRooted() {

        // get from build info
        String buildTags = android.os.Build.TAGS;
        if (buildTags != null && buildTags.contains("test-keys")) {
            return true;
        }

        // check if /system/app/Superuser.apk is present
        try {
            File file = new File("/system/app/Superuser.apk");
            if (file.exists()) {
                return true;
            }
        } catch (Exception e1) {
            // ignore
        }

        // try executing commands
        return canExecuteCommand("/system/xbin/which su")
                       || canExecuteCommand("/system/bin/which su") || canExecuteCommand("which su");
    }

    // executes a command on the system
    private static boolean canExecuteCommand(String command) {
        boolean executedSuccesfully;
        try {
            Runtime.getRuntime().exec(command);
            executedSuccesfully = true;
        } catch (Exception e) {
            executedSuccesfully = false;
        }

        return executedSuccesfully;
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
            } catch (Exception e)
            {
                Log.e(RTDevice.class.getSimpleName(), e.getMessage(), e);
                cachedCoresCount = 1;
            }
        }

        return cachedCoresCount;
    }

    private static class CpuFilter implements FileFilter
    {

        public boolean accept(File pathname)
        {
            if (Pattern.matches("cpu[0-9]", pathname.getName()))
            {
                return true;
            }
            return false;
        }
    }
}
