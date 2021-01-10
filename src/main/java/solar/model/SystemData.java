package solar.model;

import solar.model.Inverter;
import solar.model.SolarArray;

/**
 *
 * @author rocky
 */
public class SystemData {

    // Installation location
    public static final double latitude = 50.6; // degrees google maps
    public static final double longitude = -2.5; // degrees google maps

    // Array of 9 PV-TD185MF5 on garage
    // Angle measured from google maps as 2.0 degrees
    // tilt from memory of design
    public static final SolarArray south = new SolarArray("South: 9 off 185 W", 9 * 1.65 * 0.83, 35, 2.0, 13.4 / 100);

    // Array of 10 JAM60S10 340/MR on house East and West
    // Are azimuth the right way round?
    // House angle measured from google maps as 2.0 degrees
    // tilt estimated from H2 ECO site visit
    public static final SolarArray east = new SolarArray("East: 10 off 340 W", 10 * 1.669 * 0.996, 32, 272.0, 20.0 / 100);
    public static final SolarArray west = new SolarArray("West: 10 off 340 W", 10 * 1.669 * 0.996, 32, 92.0, 20.0 / 100);

    // LUX Power inverter
    // Efficiency from manual
    public static final Inverter LuxPower = Inverter.valueOf("LUX Power", 3600, 0.96);

    // LUX Power inverter-the one I should have been given
    // Efficiency from manual
    public static final Inverter LuxPower5 = Inverter.valueOf("LUX Power 5", 5000, 0.96);

    // Sunny Boy 1700
    public static final Inverter SunnyBoy = Inverter.valueOf("Sunny Boy 1700", 1550, 0.92);

    // Perfect inverter for comparison
    public static final Inverter perfectInverter = Inverter.valueOf("Perfect", 100000, 1.0);

    // Total 9 kWh per day, guessed profile per hour in kW
    public static final double[] HOURLY_CONSUMPTION = {0.2, 0.2, 0.2, 0.2, 0.2, 0.2, 0.2, 0.2, 0.2, 0.2, 0.2, 0.2, 0.2, 0.2, 0.2, 0.2, 0.2, 1.0, 2.8, 1.0, 0.2, 0.2, 0.2};

    // sum of above
    //public static final double DAILY_CONSUMPTION = 9.0;

    // Sunny day fractions by month. See Excel
    // Treat them as a constant value for the month? Or a random number with the specified mean?
    // Use triangular: https://en.wikipedia.org/wiki/Triangular_distribution 
    // https://commons.apache.org/proper/commons-math/javadocs/api-3.5/org/apache/commons/math3/distribution/TriangularDistribution.html
    // no-not triangular because its mean is between 1/3 and 2/3
    public static final double[] sunnyDays = {0.27, 0.31, 0.37, 0.48, 0.49, 0.47, 0.47, 0.47, 0.41, 0.34, 0.31, 0.25};

    /**
     * Number of identical storage batteries
     */
    public static final int NBATTERIES = 3;

    /**
     * Capacity of each battery in kWh
     */
    public static final double BATTERY_CAPACITY = 2.4;

    /**
     * Depth of discharge (inverter controlled)
     */
    //public static final double BATTERY_DOD = 0.8;

    /**
     * kWh Thermal store size in kWh (310 litre tank 10C in to 55C out = 16 kWh,
     * but you never completely empty it)
     */
    //double THERMAL_STORE_EFFECTIVE_CAPACITY = 8.0;

    /**
     * £/kWh
     */
    public static final double IMPORT_RATE = 0.150;

    /**
     * £/kWh cost of exported electricity, £/kWh
     */
    public static final double EXPORT_RATE = 0.055;

    private SystemData() {
    }

    
    
    public static final double ratedPower = (SystemData.south.power + SystemData.east.power + SystemData.west.power) / 1000.0; // kW
    public static final double ratedCapacity = ratedPower * 365 * 24; // kW

}
