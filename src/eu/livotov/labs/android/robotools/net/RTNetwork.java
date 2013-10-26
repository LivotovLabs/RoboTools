package eu.livotov.labs.android.robotools.net;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * (c) Livotov Labs Ltd. 2012
 * Date: 18/09/2013
 */
public class RTNetwork
{

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

}
