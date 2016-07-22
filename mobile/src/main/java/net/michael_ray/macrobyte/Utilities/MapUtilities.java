package net.michael_ray.macrobyte.Utilities;

/**
 * Created by samsc on 7/21/2016.
 * Files for random map-related utilities
 */
public class MapUtilities {

    /**
     * Gets the distance (in miles???) between two coordinate points.
     * @param lat1 Latitude of first point
     * @param lon1 Longitude of first point
     * @param lat2 Latitude of second point
     * @param lon2 Longitude of second point
     * @return The distance (in miles???) between the points
     */
    public static double distance(double lat1, double lon1, double lat2, double lon2){
        double R = 6371f;
        double dLat = ((lat2-lat1)*Math.PI/180);
        double dLon = ((lon2 - lon1)*Math.PI/180);
        double lata = (lat1*Math.PI/180);
        double latb = (lat2*Math.PI/180);

        double a = Math.sin(dLat/2)*Math.sin(dLat/2) + Math.sin(dLon/2) * Math.sin(dLon/2)*Math.cos(lata)*Math.cos(latb);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return R * c;
    }
}
