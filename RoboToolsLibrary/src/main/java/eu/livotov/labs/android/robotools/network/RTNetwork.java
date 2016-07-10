package eu.livotov.labs.android.robotools.network;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Build;

/**
 * Collection of handy tools for daily networking tasks
 */
public class RTNetwork
{
    /**
     * Checks if device is connected to at least one network of any type
     * @param ctx
     * @return
     */
    public static boolean isConnected(final Context ctx)
    {
        final ConnectivityManager connectivityManager = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            Network[] networks = connectivityManager.getAllNetworks();
            if (null != networks)
            {
                for (Network network : networks)
                {
                    NetworkInfo networkInfo = connectivityManager.getNetworkInfo(network);
                    if (networkInfo != null && NetworkInfo.State.CONNECTED == networkInfo.getState())
                    {
                        return true;
                    }
                }
            }
        }
        else
        {
            final NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
            return netInfo != null && netInfo.isConnected();
        }
        return false;
    }

    /**
     * Checks if we're currently on WiFi connection
     * @param ctx
     * @return
     */
    public static boolean isConnectedToWiFi(final Context ctx)
    {
        final ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected() && netInfo.getType() == ConnectivityManager.TYPE_WIFI;
    }

    /**
     * Checks if we're currenctly have mobile data connection
     * @param ctx
     * @return
     */
    public static boolean isConnectedToCellular(final Context ctx)
    {
        final ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected() && netInfo.getType() != ConnectivityManager.TYPE_WIFI;
    }

    /**
     * Checks if we're in data roaming now
     * @param ctx
     * @return
     */
    public static boolean isInRoaming(final Context ctx)
    {
        final ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.getType() != ConnectivityManager.TYPE_WIFI && netInfo.isRoaming();
    }
}
