package eu.livotov.labs.android.robotools.geo;

import android.content.Context;
import android.content.Intent;
import android.location.*;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import eu.livotov.labs.android.robotools.ui.RTDialogs;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;

/**
 * (c) Livotov Labs Ltd. 2012
 * Date: 20.03.13
 */
public class RTGps implements LocationListener
{

    private Context ctx;
    private double lastLat = 0;
    private double lastLon = 0;
    private double minimumDistance = 0.1;
    private float lastLocationAccuracy = 100000.0f; // some large number
    private long lastFixTime = 0L;
    private String lastProvider = "";
    private Location lastLocation;
    private boolean enabledGPSListener;
    private LocationManagerEventListener locationManagerEventListener;

    public RTGps(Context ctx)
    {
        this.ctx = ctx;
    }

    public double getMinimumDistance()
    {
        return minimumDistance;
    }

    public void setMinimumDistance(final double minimumDistance)
    {
        this.minimumDistance = minimumDistance;
    }

    public double getLastLat()
    {
        return lastLat;
    }

    public void setLastLat(final double lastLat)
    {
        this.lastLat = lastLat;
    }

    public double getLastLon()
    {
        return lastLon;
    }

    public void setLastLon(final double lastLon)
    {
        this.lastLon = lastLon;
    }

    public float getLastLocationAccuracy()
    {
        return lastLocationAccuracy;
    }

    public void setLastLocationAccuracy(final float lastLocationAccuracy)
    {
        this.lastLocationAccuracy = lastLocationAccuracy;
    }

    public long getLastFixTime()
    {
        return lastFixTime;
    }

    public void setLastFixTime(final long lastFixTime)
    {
        this.lastFixTime = lastFixTime;
    }

    public String getLastProvider()
    {
        return lastProvider;
    }

    public void setLastProvider(final String lastProvider)
    {
        this.lastProvider = lastProvider;
    }

    public LocationManagerEventListener getLocationManagerEventListener()
    {
        return locationManagerEventListener;
    }

    public void setLocationManagerEventListener(final LocationManagerEventListener locationManagerEventListener)
    {
        this.locationManagerEventListener = locationManagerEventListener;
    }

    public boolean enable(boolean canUseCachedDataOnly)
    {
        Log.i("RTGps", "ENABLE GeoManager, using cached only: " + canUseCachedDataOnly);

        try
        {
            android.location.LocationManager lm = (android.location.LocationManager) ctx.getSystemService(Context.LOCATION_SERVICE);

            if (lm == null)
            {
                return false;
            }

            boolean networkLocationEnabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            boolean gpsLocationEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);

            Log.i("RTGps", "Location sources - network: " + networkLocationEnabled + " gps: " + gpsLocationEnabled);

            if (!networkLocationEnabled && !gpsLocationEnabled)
            {
                return false;
            }

            final Location lastProviderLocation = lm.getLastKnownLocation(android.location.LocationManager.PASSIVE_PROVIDER);

            boolean nearSameTime = canUseCachedDataOnly;
            if (lastProviderLocation != null)
            {
                nearSameTime = (lastProviderLocation.getTime() - lastFixTime) < 3600000; // 60 minutes

                // If no location found previous, find it again
                // Or if time elapsed is more than one hour
                if (lastLat == 0 || lastLon == 0)
                {
                    nearSameTime = false;
                }

                Log.d("RTGps", "[GEO MGR] Last location provided found, setting cached values, location fairly latest: " + nearSameTime);

                // Don't set this, as we want to refresh the location in case the app shutdown and restarted
                lastLat = lastProviderLocation.getLatitude();
                lastLon = lastProviderLocation.getLongitude();
                lastLocation = lastProviderLocation;
                lastProvider = lastProviderLocation.getProvider();
                lastFixTime = lastProviderLocation.getTime();
                lastLocationAccuracy = lastProviderLocation.getAccuracy();

                if (locationManagerEventListener != null)
                {
                    locationManagerEventListener.onLocationChanged(lastProviderLocation.getLatitude(), lastProviderLocation.getLongitude(), true);
                }

                // If we don't really need to find the new location and can use the cached
                // data, disable GPS and return
                if (nearSameTime && canUseCachedDataOnly)
                {
                    disable();
                    return true;
                }
            } else
            {
                nearSameTime = false;
            }

            // In case we find a last known location which was retrieved fairly recently, ignore
            if (!nearSameTime)
            {
                enabledGPSListener = true;

                try
                {
                    lm.requestLocationUpdates(android.location.LocationManager.PASSIVE_PROVIDER, 20000, 500, this);
                    Log.d("RTGps", "Enabled PASSIVE");
                } catch (Throwable err)
                {
                    Log.e("RTGps", "Error getting Passive Locations: " + err.getMessage());
                    err.printStackTrace();
                }

                try
                {
                    lm.requestLocationUpdates(android.location.LocationManager.NETWORK_PROVIDER, 5000, 500, this);
                    Log.d("RTGps", "Enabled NETWORK");
                } catch (Throwable err)
                {
                    Log.e("RTGps", "Error getting Network Locations: " + err.getMessage());
                    err.printStackTrace();
                }

                try
                {
                    lm.requestLocationUpdates(android.location.LocationManager.GPS_PROVIDER, 5000, 1000, this);
                    Log.d("RTGps", "Enabled GPS");
                } catch (Throwable err)
                {
                    Log.e("RTGps", "Error getting GPS Locations: " + err.getMessage());
                    err.printStackTrace();
                }
            }
        } catch (Throwable err)
        {
            Log.e("RTGps", "Error enabling GPS: " + err.getMessage());
            err.printStackTrace();
        }
        return true;
    }

    public void disable()
    {
        Log.i("RTGps", "DISABLE GeoManager");
        try
        {
            android.location.LocationManager lm = (android.location.LocationManager) ctx.getSystemService(Context.LOCATION_SERVICE);
            lm.removeUpdates(this);
        } catch (Exception err)
        {
            Log.e("RTGps", "Error disabling GPS: " + err.getMessage());
        }
        enabledGPSListener = false;
    }

    public void onLocationChanged(Location location)
    {
        if (location != null && enabledGPSListener)
        {
            synchronized (this)
            {
                Log.d("RTGps", "Got location update from OS: " + location.getProvider() + " >>> accuracy: " + location.getAccuracy() + "m, lat: " + location.getLatitude() + " , lon: " + location.getLongitude());

                boolean sameProvider = (lastProvider == null) ? false : lastProvider.equalsIgnoreCase(location.getProvider());
                boolean betterAccuracy = location.hasAccuracy() && location.getAccuracy() < lastLocationAccuracy;
                boolean nearSameTime = (location.getTime() - lastFixTime) < 60000; // 1 minute

                double distanceBetweenLastAndNew = RTGeo.findDistance(lastLat, lastLon, location.getLatitude(), location.getLongitude());

                if (lastLat == 0 || lastLon == 0)
                {
                    distanceBetweenLastAndNew = 10000;
                }

                Log.d("RTGps", String.format("Same provider: %s (old: %s, new: %s) , better accuracy: %s (old: %s, new: %s) , near same time: %s (old: %s, new: %s), distance since last: %s", sameProvider, lastProvider, location.getProvider(), betterAccuracy, lastLocationAccuracy, location.getAccuracy(), nearSameTime, lastFixTime, location.getTime(), distanceBetweenLastAndNew));


                // Only checking the distance should be good enough
                if (!sameProvider || ((betterAccuracy && distanceBetweenLastAndNew >= minimumDistance) || (betterAccuracy && Math.abs(lastLocationAccuracy - location.getAccuracy()) >= 10.0)) || (!betterAccuracy && !nearSameTime && distanceBetweenLastAndNew >= minimumDistance))
                {
                    Log.d("RTGps", String.format("Taking this location update into account, Lat: %s, Lon: %s", location.getLatitude(), location.getLongitude()));
                    lastLat = location.getLatitude();
                    lastLon = location.getLongitude();
                    lastLocation = location;
                    lastProvider = location.getProvider();
                    lastLocationAccuracy = location.getAccuracy();
                    lastFixTime = location.getTime();

                    if (locationManagerEventListener != null)
                    {
                        locationManagerEventListener.onLocationChanged(lastLat, lastLon, false);
                    }
                } else
                {
                    Log.d("RTGps", "Ignoring this location update, cached one is good enough or we did not move");
                }
            }


        }
    }

    public void onStatusChanged(String s, int i, Bundle bundle)
    {
    }

    public void onProviderEnabled(String s)
    {
    }

    public void onProviderDisabled(final String s)
    {
    }

    public static boolean checkLocationServicesEnabled(final Context ctx)
    {
        try
        {
            boolean locationEnabled = true;

            if (Build.VERSION.SDK_INT<19)
            {
                locationEnabled = !TextUtils.isEmpty(Settings.Secure.getString(ctx.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED));
            } else
            {
                final int mode = Settings.Secure.getInt(ctx.getContentResolver(), Settings.Secure.LOCATION_MODE);
                locationEnabled = mode != Settings.Secure.LOCATION_MODE_OFF;
            }

            return locationEnabled;
        } catch (Throwable err)
        {
            throw new RuntimeException(err);
        }
    }

    public static void openLocationSettings(Context ctx)
    {
        ctx.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
    }

    public double getLastKnownLattitude()
    {
        return lastLat;
    }

    public double getLastKnownLongtitude()
    {
        return lastLon;
    }

    private Address geoCodeLocationUsingHttp(final double lat, final double lon)
    {
        Log.d("RTGps", " Getting address from http...");
        Address retAddress = null;
        HttpGet httpGet = new HttpGet("http://maps.google.com/maps/api/geocode/json?sensor=false&latlng=" + lat + "%2C" + lon);
        HttpClient client = new DefaultHttpClient();
        HttpResponse response;
        StringBuilder stringBuilder = new StringBuilder();

        try
        {
            response = client.execute(httpGet);
            HttpEntity entity = response.getEntity();
            InputStream stream = entity.getContent();
            int b;
            while ((b = stream.read()) != -1)
            {
                stringBuilder.append((char) b);
            }
        } catch (ClientProtocolException e)
        {
        } catch (IOException e)
        {
        }

        JSONObject jsonObj = new JSONObject();
        try
        {
            jsonObj = new JSONObject(stringBuilder.toString());

            if (jsonObj != null)
            {

                String Status = jsonObj.getString("status");
                if (Status.equalsIgnoreCase("OK"))
                {


                    JSONArray Results = jsonObj.getJSONArray("results");
                    JSONObject zero = Results.getJSONObject(0);
                    JSONArray address_components = zero.getJSONArray("address_components");

                    if (address_components != null && address_components.length() > 0)
                    {
                        Log.d("RTGps", "Found address, returning");
                        retAddress = new Address(Locale.getDefault());
                    } else
                    {
                        Log.d("RTGps", "Found nothing... " + Results);
                    }

                    retAddress.setLatitude(lat);
                    retAddress.setLongitude(lon);
                    for (int i = 0; i < address_components.length(); i++)
                    {
                        JSONObject zero2 = address_components.getJSONObject(i);
                        String long_name = zero2.getString("long_name");
                        JSONArray mtypes = zero2.getJSONArray("types");
                        String Type = mtypes.getString(0);

                        Log.d("RTGps", " " + i + "  Long: " + long_name + "   Type: " + Type);
                        if (TextUtils.isEmpty(long_name) == false || !long_name.equals(null) || long_name.length() > 0 || long_name != "")
                        {
                            if (Type.equalsIgnoreCase("street_number"))
                            {
                            } else if (Type.equalsIgnoreCase("route"))
                            {
                                retAddress.setAddressLine(0, long_name);
                            } else if (Type.equalsIgnoreCase("sublocality"))
                            {
                                retAddress.setSubLocality(long_name);
                            } else if (Type.equalsIgnoreCase("locality"))
                            {
                                retAddress.setLocality(long_name);
                            } else if (Type.equalsIgnoreCase("administrative_area_level_2"))
                            {
                                retAddress.setAdminArea(long_name);
                            } else if (Type.equalsIgnoreCase("administrative_area_level_1"))
                            {
                                //retAddress.setAdminArea(long_name);
                            } else if (Type.equalsIgnoreCase("country"))
                            {
                                retAddress.setCountryName(long_name);
                            } else if (Type.equalsIgnoreCase("postal_code"))
                            {
                                retAddress.setPostalCode(long_name);
                            } else if (Type.equalsIgnoreCase("postal_code_prefix"))
                            {
                                if (retAddress.getPostalCode() == null)
                                {
                                    retAddress.setPostalCode(long_name);
                                }
                            } else if (Type.equalsIgnoreCase("postal_town"))
                            {
                                if (retAddress.getLocality() == null)
                                {
                                    retAddress.setLocality(long_name);
                                } else
                                {
                                    retAddress.setSubLocality(long_name);
                                }
                            }
                        }

                    }
                }
            }
        } catch (JSONException e)
        {
            Log.e("RTGps", "JSON Error: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e)
        {
            Log.e("RTGps", "JSON Error: " + e.getMessage());
            e.printStackTrace();
        }

        return retAddress;
    }

    public void resolveCurrentAddress()
    {
        resolveAddress(lastLat, lastLon);
    }

    public void resolveAddress(final double lat, final double lon)
    {
        new AsyncTask()
        {
            protected Object doInBackground(final Object... objects)
            {
                // Try again using google's direct reverse geocoder
                Address retAdd = geoCodeLocationUsingHttp(lat, lon);
                if (retAdd != null)
                {
                    Log.d("RTGps", "GOT: " + retAdd);
                    return retAdd;
                }

                try
                {
                    Log.d("RTGps", "Got city, resolving address... " + lat + ":" + lon);
                    Geocoder geoCoder = new Geocoder(ctx);
                    if (geoCoder != null)
                    {
                        List<Address> list = geoCoder.getFromLocation((Double) objects[0], (Double) objects[1], 1);
                        if (list != null & list.size() > 0)
                        {
                            return list.get(0);
                        }
                    } else
                    {
                        Log.e("RTGps", "Geo-coder could NOT be found");
                    }
                } catch (Exception err)
                {
                    Log.e("RTGps", "Geo-coder error: " + err.getMessage());
                    err.printStackTrace();
                }

                return true;
            }

            protected void onPostExecute(final Object o)
            {
                if (o instanceof Address && locationManagerEventListener != null)
                {
                    locationManagerEventListener.onAddressResolved((Address) o, lat, lon);
                } else
                {
                    Log.e("RTGps", "Could not find address, however we'll mark it as an Unknown location based only on lat and long");
                    locationManagerEventListener.onAddressResolved(null, lat, lon);
                }
            }
        }.execute(lat, lon);
    }

    public Location getLastLocation()
    {
        return lastLocation;
    }

    public interface LocationManagerEventListener
    {

        void onLocationChanged(double lat, double lon, boolean usingCachedCity);

        void onAddressResolved(Address address, double lat, double lon);
    }
}