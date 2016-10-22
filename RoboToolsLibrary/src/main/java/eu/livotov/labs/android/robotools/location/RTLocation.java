package eu.livotov.labs.android.robotools.location;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.NonNull;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

/**
 * Created by dlivotov on 10/07/2016.
 */

public class RTLocation
{
    private static final double PK = 180 / 3.1415926;
    private static final double EARTH_RADIUS = 6371;

    /**
     * Computes the closest distance between 2 points
     *
     * @param lat1
     * @param lon1
     * @param lat2
     * @param lon2
     * @return Distance in km.
     */
    public static double findDistance(double lat1, double lon1, double lat2, double lon2)
    {
        return Math.acos(Math.sin(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2)) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * (Math.cos(Math.toRadians(lon1)) * Math.cos(Math.toRadians(lon2)) + Math.sin(Math.toRadians(lon1)) * Math.sin(Math.toRadians(lon2)))) * EARTH_RADIUS;
    }

    /**
     * * Computes distance (in meters) between two points
     *
     * @param fromLocation start point location
     * @param toLocation   end point location
     * @return distance in meters between two points provided
     */
    public static float findDistance(@NonNull Location fromLocation, @NonNull Location toLocation)
    {
        return Math.abs(fromLocation.distanceTo(toLocation));
    }

    /**
     * Checks the location services availability on this device
     * @param ctx context
     * @return <code>true</code> if location services are enabled on this device
     */
    public static boolean isLocationEnabled(Context ctx)
    {
        LocationManager lm = null;
        boolean gps_enabled = false;
        boolean network_enabled = false;

        if (lm == null)
        {
            lm = (LocationManager) ctx.getSystemService(Context.LOCATION_SERVICE);
        }
        try
        {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        }
        catch (Exception ignored)
        {
        }

        try
        {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        }
        catch (Exception ignored)
        {
        }

        return gps_enabled || network_enabled;
    }

    /**
     * Checks the google services availability on this device
     * @param ctx context
     * @return <code>true</code> if google services are present
     */
    public static boolean areGoogleServicesPresent(Context ctx)
    {
        try
        {
            return GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(ctx) == ConnectionResult.SUCCESS;
        }
        catch (Throwable err)
        {
            return false;
        }
    }
}
