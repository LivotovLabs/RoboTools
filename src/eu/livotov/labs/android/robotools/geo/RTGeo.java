package eu.livotov.labs.android.robotools.geo;

import android.location.Location;

/**
 * (c) Livotov Labs Ltd. 2012
 * Date: 15/01/2013
 *
 * Set of helper methods for calculation of various geographic data
 */
public class RTGeo {

    private static final double PK = 180 / 3.1415926;
    private static final double EARTH_RADIUS = 6371;

    /**
     * Computes distance (in kilometers) between two points
     * @param lat1 start point latitude
     * @param lon1 start point longitude
     * @param lat2 end point latitude
     * @param lon2 end point longitude
     * @return distance in kilometers between two points provided
     */
    public static double findDistance(double lat1, double lon1, double lat2, double lon2) {
        return Math.acos(Math.sin(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2)) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * (Math.cos(Math.toRadians(lon1)) * Math.cos(Math.toRadians(lon2)) + Math.sin(Math.toRadians(lon1)) * Math.sin(Math.toRadians(lon2)))) * EARTH_RADIUS;
    }

}
