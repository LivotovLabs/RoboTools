package eu.livotov.labs.android.robotools.compat.v1.telephony;

import android.content.Context;
import eu.livotov.labs.android.robotools.compat.v1.device.RTDevice;

/**
 * (c) Livotov Labs Ltd. 2012
 * Date: 15.04.13
 */
public class RTTelephony
{
    public static boolean supportsTelephony(Context ctx)
    {
        return RTDevice.supportsTelephony(ctx);
    }

    public static boolean supportsSms(Context ctx)
    {
        return RTDevice.supportsSms(ctx);
    }


}
