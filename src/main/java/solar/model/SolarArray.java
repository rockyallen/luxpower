package solar.model;

/**
 * A solar array, modelled as a single large panel.
 *
 * @threadsafety Immutable
 *
 * @author rocky
 */
public class SolarArray {

    private static Calculator calculator = new Calculator();
    private final double latitude;
    private final double longitude;
    private final boolean azimuthTracking;
    private final boolean elevationTracking;
    /**
     * Sum of area of all modules
     */
    private final double area;
    /**
     * Degrees
     */
    private final double tilt;
    /**
     * Degrees
     */
    private final double azimuth;
    /**
     * Module efficiency from data sheet 0-1
     */
    private final double efficiency;
    /**
     * Power under standard illumination. Effectively kWp for the array in Watts
     */
    private final double power;
    private final String name;
    private final String description;

    private final double standardMeridian;
    
    /**
     *
     * @param name
     * @param description
     * @param area Total area from data sheet. This is the effective area on
     * which efficiency is calculated, m2
     * @param tilt Tilt from vertical, degrees. Ignored if elevationTracking is
     * true.
     * @param azimuth Azimuth, degrees. Ignored if azimuthTracking is true.
     * @param efficiency Conversion efficiency based on total array area, 0-1
     * @param latitude Latitude of the installation, degrees
     * @param longitude Longitude of the installation, degrees
     * @param azimuthTracking True if the array can track the sun in azimuth.
     * @param elevationTracking True if the array can track the sun in
     * elevation.
     */
    public SolarArray(String name, String description, double area, double tilt,
            double azimuth, double efficiency, double latitude, double longitude, boolean azimuthTracking, boolean elevationTracking) {
        if ("".equals(name)) {
            throw new IllegalArgumentException("name must not be blank");
        }
        this.name = name;
        this.description = description;
        this.area = area;
        this.tilt = tilt;
        this.azimuth = azimuth;
        this.efficiency = efficiency;
        this.latitude = latitude;
        this.longitude = longitude;
        this.azimuthTracking = azimuthTracking;
        this.elevationTracking = elevationTracking;
        this.power = area * efficiency * 1000.0; // 1 kW/m2 is the standard illumination, apparently.
        this.standardMeridian = 0.0; // Wrong in general, but OK for the UK
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Array: '%s' %s power=%4.0f W ", name, description, getRatedPower()));
        sb.append(" tilt=");
        if (elevationTracking) {
            sb.append("tracking");
        } else {
            sb.append(String.format("%3f", tilt));
        }
        sb.append(" azimuth=");
        if (azimuthTracking) {
            sb.append("tracking");
        } else {
            sb.append(String.format("%3f", azimuth));
        }
        return sb.toString();
    }

    /**
     * The sunny day power of the array.
     *
     * This value must be scaled back to allow for bad weather, then potentially
     * throttled back by the inverter to get the yield.
     *
     * @param dayNumber Year day, 0-364
     * @param hour Solar hour
     *
     * @return Power, Watts
     */
    public double availablePower(int dayNumber, double hour) {
        double arrayElevation = elevationTracking ? calculator.sunElevation(latitude, calculator.solarDeclination(dayNumber), hour) : tilt;
        double arrayAzimuth = azimuthTracking ? calculator.sunAzimuth(latitude, calculator.solarDeclination(dayNumber), hour) : azimuth;
        double insolation = calculator.insSolarRadiation(
                hour, 
                latitude, 
                longitude, 
                standardMeridian,
                arrayElevation,
                arrayAzimuth,
                dayNumber, 
                Calculator.CN, 
                Calculator.SURFACE_REFLECTIVITY);
        return Math.max(0, efficiency * area * insolation);
    }

    /**
     * @return the power
     */
    public double getRatedPower() {
        return power;
    }
}
