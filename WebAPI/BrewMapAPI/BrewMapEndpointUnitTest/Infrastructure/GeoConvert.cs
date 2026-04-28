namespace BrewMapEndpointUnitTest.Infrastructure;

public static class GeoConvert
{
    private const double MetersPerDegreeLatitude = 111320.0;

    /// <summary>
    /// Converts a distance in kilometres to an approximate degree delta for latitude.
    /// Use the overload with a reference latitude for more accurate longitude deltas.
    /// </summary>
    public static double KmToDegrees(double km) => km * 1000.0 / MetersPerDegreeLatitude;

    /// <summary>
    /// Converts a distance in metres to an approximate degree delta for latitude.
    /// </summary>
    public static double MetersToDegrees(double meters) => meters / MetersPerDegreeLatitude;
}
