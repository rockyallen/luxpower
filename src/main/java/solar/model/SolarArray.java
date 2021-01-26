package solar.model;

/**
 * A solar array, modelled as a single large panel.
 *
 * @threadsafety Immutable
 *
 * @author rocky
 */
public class SolarArray {

    private static Calculator calc = new Calculator();
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

    /**
     *
     * @param name
     * @param description
     * @param area
     * @param tilt
     * @param azimuth
     * @param efficiency
     */
    public SolarArray(String name, String description, double area, double tilt, double azimuth, double efficiency) {
        this.name = name;
        this.description = description;
        this.area = area;
        this.tilt = tilt;
        this.azimuth = azimuth;
        this.efficiency = efficiency;
        this.power = area * efficiency * 1000.0;
        if ("".equals(name)) {
            throw new IllegalArgumentException("name must not be blank");
        }
    }

    @Override
    public String toString() {
        return String.format("Array: '%s' %s power=%4.0f", name, description, getRatedPower());
    }

    /**
     * The sunny day power of the array.
     *
     * This value must be scaled back to allow for bad weather, then potentially
     * throttled back by the inverter to get the yield.
     *
     * @param dday Year day 0-364
     * @param hour Solar hour
     *
     * @return Power Watts
     */
    public double availablePower(int dday, double hour) {
        double insolation = calc.insSolarRadiation(hour, Components.getLatitude(), Components.getLongitude(), 0, tilt, azimuth, dday, Calculator.CN, Calculator.SURFACE_REFLECTIVITY);
        return Math.max(0, efficiency * area * insolation);
    }

    /**
     * @return the power
     */
    public double getRatedPower() {
        return power;
    }

}
