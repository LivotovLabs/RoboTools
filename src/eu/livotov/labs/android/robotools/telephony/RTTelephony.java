package eu.livotov.labs.android.robotools.telephony;

import android.content.Context;
import android.content.pm.PackageManager;
import android.telephony.TelephonyManager;

/**
 * (c) Livotov Labs Ltd. 2012
 * Date: 15.04.13
 */
public class RTTelephony
{

    private static boolean canSendSMS(Context ctx)
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
}
