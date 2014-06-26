package eu.livotov.labs.android.robotools.hardware;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import eu.livotov.labs.android.robotools.security.CryptUtil;

import java.io.File;
import java.io.FileFilter;
import java.util.regex.Pattern;

@SuppressWarnings("unused")
public class DeviceInfo {

    private static int cachedCoresCount = 0;

    private DeviceInfo() {
    }

    public static boolean isTablet(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();

        int shortSizeDp = (int) (displayMetrics.density * Math.min(displayMetrics.widthPixels, displayMetrics.heightPixels));

        return shortSizeDp > 1024;
    }

    public static float dp2px(Context ctx, float px) {
        return px / ctx.getResources().getDisplayMetrics().density;
    }

    public static float px2dp(Context ctx, float dp) {
        return dp * ctx.getResources().getDisplayMetrics().density;
    }

    public static String getDeviceUID(final Context ctx, boolean countOnNetworkInterfaces, boolean countOnTelephony,
                                      boolean countOnSim) {
        StringBuilder id = new StringBuilder();

        // requires android.permission.ACCESS_WIFI_STATE
        if (countOnNetworkInterfaces && ctx != null) {
            WifiManager wm = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
            id.append(wm.getConnectionInfo().getMacAddress());
        }

        // requires android.permission.READ_PHONE_STATE
        if (countOnTelephony && ctx != null) {
            TelephonyManager telephonyManager = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
            if (telephonyManager != null) {
                id.append(telephonyManager.getDeviceId() != null ? telephonyManager.getDeviceId() : "");
            }
        }

        // requires android.permission.READ_PHONE_STATE
        if (countOnSim && ctx != null) {
            TelephonyManager telephonyManager = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
            id.append(telephonyManager.getSimSerialNumber() != null ? telephonyManager.getSimSerialNumber() : "");
        }

        if (ctx != null) {
            id.append(Settings.Secure.getString(ctx.getContentResolver(), Settings.Secure.ANDROID_ID));
        }

        id.append(Build.BOARD.length() % 10 + Build.BRAND.length() % 10 +
                Build.CPU_ABI.length() % 10 + Build.DEVICE.length() % 10 +
                Build.DISPLAY.length() % 10 + Build.HOST.length() % 10 +
                Build.ID.length() % 10 + Build.MANUFACTURER.length() % 10 +
                Build.MODEL.length() % 10 + Build.PRODUCT.length() % 10 +
                Build.TAGS.length() % 10 + Build.TYPE.length() % 10 +
                Build.USER.length() % 10);

        if (Build.VERSION.SDK_INT > 8) {
            id.append(Build.SERIAL);
        }

        return CryptUtil.md5(id.toString());
    }

    public static boolean supportsCamera(Context ctx) {
        return ctx.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

    public static boolean supportsTelephony(Context ctx) {
        return ctx.getPackageManager().hasSystemFeature(PackageManager.FEATURE_TELEPHONY);
    }

    public static boolean supportsGps(Context ctx) {
        return ctx.getPackageManager().hasSystemFeature(PackageManager.FEATURE_LOCATION);
    }

    public static boolean supportsSms(Context ctx) {
        try {
            if (ctx.getPackageManager().hasSystemFeature(PackageManager.FEATURE_TELEPHONY)) {
                TelephonyManager telMgr = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
                int simState = telMgr.getSimState();
                switch (simState) {
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
        } catch (Throwable err) {
            err.printStackTrace();
            return false;
        }
    }

    public synchronized static int getCpuCoresCount() {
        if (cachedCoresCount == 0) {
            try {
                File dir = new File("/sys/devices/system/cpu/");
                File[] files = dir.listFiles(new CpuFilter());
                cachedCoresCount = files.length;
            } catch (Exception e) {
                Log.e(DeviceInfo.class.getSimpleName(), e.getMessage(), e);
                cachedCoresCount = 1;
            }
        }

        return cachedCoresCount;
    }

    private static class CpuFilter implements FileFilter {

        public boolean accept(File pathname) {
            return Pattern.matches("cpu[0-9]", pathname.getName());
        }
    }

    public static boolean isExternalStorageReady() {
        final String state = Environment.getExternalStorageState();
        return !(Environment.MEDIA_REMOVED.equals(state)
                || Environment.MEDIA_BAD_REMOVAL.equals(state)
                || Environment.MEDIA_UNMOUNTABLE.equals(state)
                || Environment.MEDIA_UNMOUNTED.equals(state));
    }

    public static File getExternalStorage() {
        return getExternalStorage(null);
    }

    public static File getExternalStorage(final String type) {
        File file = Environment.getExternalStoragePublicDirectory(type);
        if (!file.exists()) {
            file.mkdirs();
        }
        return file;
    }

    public static boolean isConnected(final Context ctx) {
        final ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected();
    }

    public static boolean isConnectedToWiFi(final Context ctx) {
        final ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected() && netInfo.getType() == ConnectivityManager.TYPE_WIFI;
    }

    public static boolean isConnectedToCellular(final Context ctx) {
        final ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected() && netInfo.getType() != ConnectivityManager.TYPE_WIFI;
    }

    public static boolean isInRoaming(final Context ctx) {
        final ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.getType() != ConnectivityManager.TYPE_WIFI && netInfo.isRoaming();
    }

    public static CharSequence getOwnerEmail(Context context) {
        Account[] accounts = AccountManager.get(context).getAccountsByType("com.google");
        return accounts != null && accounts.length > 0 ? accounts[0].name : null;
    }
}