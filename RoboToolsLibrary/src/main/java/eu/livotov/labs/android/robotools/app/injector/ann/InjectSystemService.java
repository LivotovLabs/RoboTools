package eu.livotov.labs.android.robotools.app.injector.ann;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.DownloadManager;
import android.app.KeyguardManager;
import android.app.NotificationManager;
import android.app.SearchManager;
import android.app.UiModeManager;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.MediaRouter;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.PowerManager;
import android.os.Vibrator;
import android.os.storage.StorageManager;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.HashMap;
import java.util.Map;

import static android.content.Context.ACTIVITY_SERVICE;
import static android.content.Context.ALARM_SERVICE;
import static android.content.Context.AUDIO_SERVICE;
import static android.content.Context.CONNECTIVITY_SERVICE;
import static android.content.Context.DOWNLOAD_SERVICE;
import static android.content.Context.INPUT_METHOD_SERVICE;
import static android.content.Context.KEYGUARD_SERVICE;
import static android.content.Context.LAYOUT_INFLATER_SERVICE;
import static android.content.Context.LOCATION_SERVICE;
import static android.content.Context.MEDIA_ROUTER_SERVICE;
import static android.content.Context.NOTIFICATION_SERVICE;
import static android.content.Context.POWER_SERVICE;
import static android.content.Context.SEARCH_SERVICE;
import static android.content.Context.SENSOR_SERVICE;
import static android.content.Context.STORAGE_SERVICE;
import static android.content.Context.TELEPHONY_SERVICE;
import static android.content.Context.UI_MODE_SERVICE;
import static android.content.Context.VIBRATOR_SERVICE;
import static android.content.Context.WIFI_SERVICE;
import static android.content.Context.WINDOW_SERVICE;

/**
 * Injects system service
 *
 * Annotated field must be one of supported system service classes:
 * <ul>
 * <li> {@link android.view.WindowManager}</li>
 * <li> {@link android.view.LayoutInflater}</li>
 * <li> {@link android.app.ActivityManager}</li>
 * <li> {@link android.os.PowerManager}</li>
 * <li> {@link android.app.AlarmManager}</li>
 * <li> {@link android.app.NotificationManager}</li>
 * <li> {@link android.app.KeyguardManager}</li>
 * <li> {@link android.location.LocationManager}</li>
 * <li> {@link android.app.SearchManager}</li>
 * <li> {@link android.hardware.SensorManager}</li>
 * <li> {@link android.os.storage.StorageManager}</li>
 * <li> {@link android.os.Vibrator}</li>
 * <li> {@link android.net.ConnectivityManager}</li>
 * <li> {@link android.net.wifi.WifiManager}</li>
 * <li> {@link android.media.AudioManager}</li>
 * <li> {@link android.media.MediaRouter}</li>
 * <li> {@link android.telephony.TelephonyManager}</li>
 * <li> {@link android.view.inputmethod.InputMethodManager}</li>
 * <li> {@link android.app.UiModeManager}</li>
 * <li> {@link android.app.DownloadManager}</li>
 * </ul>
 * Applicable to {@link android.app.Activity}, {@link android.app.Fragment}, {@link android.app.Service}, {@link android.app.Application}
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface InjectSystemService {

    final static Map<Class, String> SERVICES_MAP = new HashMap<Class, String>() {{
        put(WindowManager.class, WINDOW_SERVICE);
        put(LayoutInflater.class, LAYOUT_INFLATER_SERVICE);
        put(ActivityManager.class, ACTIVITY_SERVICE);
        put(PowerManager.class, POWER_SERVICE);
        put(AlarmManager.class, ALARM_SERVICE);
        put(NotificationManager.class, NOTIFICATION_SERVICE);
        put(KeyguardManager.class, KEYGUARD_SERVICE);
        put(LocationManager.class, LOCATION_SERVICE);
        put(SearchManager.class, SEARCH_SERVICE);
        put(SensorManager.class, SENSOR_SERVICE);
        put(StorageManager.class, STORAGE_SERVICE);
        put(Vibrator.class, VIBRATOR_SERVICE);
        put(ConnectivityManager.class, CONNECTIVITY_SERVICE);
        put(WifiManager.class, WIFI_SERVICE);
        put(AudioManager.class, AUDIO_SERVICE);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            put(MediaRouter.class, MEDIA_ROUTER_SERVICE);
        }
        put(TelephonyManager.class, TELEPHONY_SERVICE);
        put(InputMethodManager.class, INPUT_METHOD_SERVICE);
        put(UiModeManager.class, UI_MODE_SERVICE);
        put(DownloadManager.class, DOWNLOAD_SERVICE);
    }};

}
