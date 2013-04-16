package eu.livotov.labs.android.robotools.geo;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.LatLng;

/**
 * (c) Livotov Labs Ltd. 2012
 * Date: 16.04.13
 */
public class RTGoogleMaps
{

    public static boolean intiMap(GoogleMap map)
    {
        return intiMap(map, null);
    }

    public static boolean intiMap(GoogleMap map, Activity activity)
    {
        if (activity != null && isGoogleMapsAvailable(activity))
        {
            return false;
        }

        map.setMyLocationEnabled(true);
        map.getUiSettings().setMyLocationButtonEnabled(true);
        map.getUiSettings().setRotateGesturesEnabled(true);
        map.getUiSettings().setAllGesturesEnabled(true);
        map.getUiSettings().setTiltGesturesEnabled(true);
        map.getUiSettings().setZoomControlsEnabled(false);
        map.getUiSettings().setZoomGesturesEnabled(true);
        map.getUiSettings().setCompassEnabled(true);

        if (activity != null)
        {
            try
            {
                MapsInitializer.initialize(activity);
            } catch (GooglePlayServicesNotAvailableException e)
            {
                return false;
            }
        }

        return true;
    }

    public static boolean intiMapForInlinePreview(GoogleMap map)
    {
        return intiMapForInlinePreview(map, null);
    }

    public static boolean intiMapForInlinePreview(GoogleMap map, Activity activity)
    {
        if (activity != null && isGoogleMapsAvailable(activity))
        {
            return false;
        }

        map.setMyLocationEnabled(true);
        map.getUiSettings().setMyLocationButtonEnabled(false);
        map.getUiSettings().setRotateGesturesEnabled(false);
        map.getUiSettings().setAllGesturesEnabled(false);
        map.getUiSettings().setTiltGesturesEnabled(false);
        map.getUiSettings().setZoomControlsEnabled(false);
        map.getUiSettings().setZoomGesturesEnabled(false);
        map.getUiSettings().setCompassEnabled(false);
        map.getUiSettings().setScrollGesturesEnabled(false);

        if (activity != null)
        {
            try
            {
                MapsInitializer.initialize(activity);
            } catch (GooglePlayServicesNotAvailableException e)
            {
                return false;
            }
        }

        return true;
    }

    public static void toggleSatelliteMode(GoogleMap map, boolean satellite)
    {
        if (map == null)
        {
            return;
        }

        map.setMapType(satellite ? GoogleMap.MAP_TYPE_SATELLITE : GoogleMap.MAP_TYPE_NORMAL);
    }

    public static boolean isSatelliteMode(GoogleMap map)
    {
        if (map == null)
        {
            return false;
        }

        return map.getMapType() == GoogleMap.MAP_TYPE_SATELLITE;
    }

    public static void moveMapTo(GoogleMap map, double lat, double lon, float zoom, boolean animate)
    {
        if (map == null)
        {
            return;
        }

        CameraUpdate position = CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lon), zoom == 0.0 ? 14.0f : zoom);
        if (animate)
        {
            map.animateCamera(position);
        } else
        {
            map.moveCamera(position);
        }
    }

    public static boolean isGoogleMapsAvailable(Context ctx)
    {
        return GooglePlayServicesUtil.isGooglePlayServicesAvailable(ctx) == ConnectionResult.SUCCESS;
    }

    public static void installGoogleMapsIfRequired(Activity activity)
    {
        int code = GooglePlayServicesUtil.isGooglePlayServicesAvailable(activity);
        if (code != ConnectionResult.SUCCESS)
        {
            Dialog d = GooglePlayServicesUtil.getErrorDialog(code, activity, 987);
            if (d != null)
            {
                d.show();
            }
        }
    }
}
