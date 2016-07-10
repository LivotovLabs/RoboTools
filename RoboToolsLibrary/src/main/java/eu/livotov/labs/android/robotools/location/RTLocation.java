package eu.livotov.labs.android.robotools.location;

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
    public double findDistance(double lat1, double lon1, double lat2, double lon2)
    {
        return Math.acos(Math.sin(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2)) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * (Math.cos(Math.toRadians(lon1)) * Math.cos(Math.toRadians(lon2)) + Math.sin(Math.toRadians(lon1)) * Math.sin(Math.toRadians(lon2)))) * EARTH_RADIUS;
    }
}
