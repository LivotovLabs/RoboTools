package eu.livotov.labs.android.robotools.device;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.view.Display;
import eu.livotov.labs.android.robotools.crypt.RTCryptUtil;

/**
 * Created with IntelliJ IDEA.
 * User: dlivotov
 * Date: 28.10.12
 * Time: 20:35
 * To change this template use File | Settings | File Templates.
 */
public class RTDevice {

    public static boolean isTablet(Activity activity) {
        Display display = activity.getWindowManager().getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getMetrics(displayMetrics);

        int width = displayMetrics.widthPixels / displayMetrics.densityDpi;
        int height = displayMetrics.heightPixels / displayMetrics.densityDpi;

        double screenDiagonal = Math.sqrt(width * width + height * height);
        return (screenDiagonal >= 6);
    }

    public static String getDeviceUID(final Context ctx, boolean countOnNetworkInterfaces, boolean countOnTelephony,
                                      boolean countOnSim) {
        StringBuffer id = new StringBuffer();

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

        return RTCryptUtil.md5(id.toString());
    }
}
