package eu.livotov.labs.android.robotools.gcm;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.util.Log;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import eu.livotov.labs.android.robotools.settings.RTPrefs;

import java.io.IOException;

/**
 * (c) Livotov Labs Ltd. 2014
 * Date: 18/03/2014
 *
 * Helper controller for easy Google GCM service registration handling. The class usage is simple:
 * <br/>
 * <or>
 *     <li>Create your own class, extending RTGcmController</li>
 *     <li>Override three abstract methods (see methods javadoc for more info)</li>
 *     <li>Instantiate instance of this class somewhere in you App object or other singleton</li>
 *     <li>Call register() in non-ui thread to register with google and your own server</li>
 *     <li>That's all.</li>
 * </or>
 */
public abstract class RTGcmController
{

    private final static class SettingKey
    {

        public final static String GcmLastRegisteredAppVersion = "appversion";
        public final static String GcmLastRegisteredId = "regid";
        public final static String GcmLastDeveloperServerRegisteredId = "devserverregid";
    }

    private final static class PushData
    {

        public final static int GooglePlayServicesIntentResolutionRequest = 800;
        public final static String gcmPrefsFile = "rtgcm";
    }

    private Context ctx;
    private RTPrefs pushSettings;
    private boolean registered = false;
    private String lastError = null;

    public RTGcmController(final Context ctx)
    {
        this.ctx = ctx;
        pushSettings = new RTPrefs(ctx, PushData.gcmPrefsFile);
        registered = checkGoogleServices(null) && !TextUtils.isEmpty(getGcmServerLastRegisteredId()) && getGcmServerLastRegisteredId().equals(getDeveloperServerLastRegId());
    }

    /**
     *
     * @return
     */
    public boolean register()
    {
        if (checkGoogleServices(null))
        {
            lastError = null;
            String regId = getGcmServerLastRegisteredId();

            try
            {
                if (TextUtils.isEmpty(regId))
                {
                    regId = requestNewRegistrationTokenFromGcmServer();

                    if (!TextUtils.isEmpty(regId))
                    {
                        storeGcmServerLastRegisteredId(regId);
                        lastError = null;
                    } else
                    {
                        lastError = "Google returned empty registration token without specific error.";
                    }
                }

                if (lastError == null && regId != null)
                {
                    if (!regId.equals(getDeveloperServerLastRegId()))
                    {
                        onSendRegistrationIdToDeveloperServer(regId);
                        storeDeveloperServerLastRegId(regId);
                    }

                    registered = true;
                }
            } catch (Throwable err)
            {
                Log.e(getClass().getSimpleName(), err.getMessage(), err);
                lastError = err.getClass().getCanonicalName() + " : " + err.getMessage();
                registered = false;
            }
        } else
        {
            registered = false;
        }

        return registered;
    }

    public void unregister()
    {
        if (registered)
        {
            try
            {
                onCancelRegistrationIdToDeveloperServer(getGcmServerLastRegisteredId());
            } catch (Throwable ignored)
            {
            }

            try
            {
                cancelGcmServerRegistration();
            } catch (Throwable ignored)
            {
            }

            pushSettings.getPreferences().edit().clear().commit();
            registered = false;
        }
    }

    public boolean checkGoogleServices(final Activity screen)
    {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(screen != null ? screen : ctx);

        if (resultCode != ConnectionResult.SUCCESS)
        {
            if (screen != null && GooglePlayServicesUtil.isUserRecoverableError(resultCode))
            {
                GooglePlayServicesUtil.getErrorDialog(resultCode, screen, PushData.GooglePlayServicesIntentResolutionRequest).show();
            } else
            {
                lastError = "This device is not supported. Google error " + resultCode;
            }
            return false;
        }
        return true;
    }

    protected String getGcmServerLastRegisteredId()
    {
        SharedPreferences prefs = pushSettings.getPreferences();

        try
        {
            final int lastVersionCode = prefs.getInt(SettingKey.GcmLastRegisteredAppVersion, Integer.MIN_VALUE);

            PackageManager pm = ctx.getPackageManager();
            PackageInfo info = pm.getPackageInfo(ctx.getPackageName(), 0);

            if (lastVersionCode != info.versionCode)
            {
                prefs.edit().putString(SettingKey.GcmLastRegisteredId, "").commit();
            }
        } catch (Throwable err)
        {
            Log.e(getClass().getSimpleName(), err.getMessage(), err);
        }

        return prefs.getString(SettingKey.GcmLastRegisteredId, "");
    }

    protected void storeGcmServerLastRegisteredId(final String regId)
    {
        SharedPreferences prefs = pushSettings.getPreferences();

        try
        {
            PackageManager pm = ctx.getPackageManager();
            PackageInfo info = pm.getPackageInfo(ctx.getPackageName(), 0);

            prefs.edit().putString(SettingKey.GcmLastRegisteredId, regId).putInt(SettingKey.GcmLastRegisteredAppVersion, info.versionCode).commit();
        } catch (Throwable err)
        {
            Log.e(getClass().getSimpleName(), err.getMessage(), err);
        }
    }

    protected void storeDeveloperServerLastRegId(final String regId)
    {
        SharedPreferences prefs = pushSettings.getPreferences();
        prefs.edit().putString(SettingKey.GcmLastDeveloperServerRegisteredId, regId).commit();
    }

    protected String getDeveloperServerLastRegId()
    {
        SharedPreferences prefs = pushSettings.getPreferences();
        return prefs.getString(SettingKey.GcmLastDeveloperServerRegisteredId, "");
    }

    protected String requestNewRegistrationTokenFromGcmServer() throws IOException
    {
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(ctx);
        return gcm.register(onProvideGcmSenderId());
    }

    protected void cancelGcmServerRegistration() throws IOException
    {
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(ctx);
        gcm.unregister();
    }

    public abstract void onSendRegistrationIdToDeveloperServer(final String id);

    public abstract void onCancelRegistrationIdToDeveloperServer(final String id);

    public abstract String onProvideGcmSenderId();
}
