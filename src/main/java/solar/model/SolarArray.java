package solar.model;

/**
 * An array, modelled as a single large panel.
 *
 * @author rocky
 */
public class SolarArray {

    private static Calculator calc = new Calculator();
    /**
     * Sum of area of all modules
     */
    public final double area;
    /**
     * Degrees
     */
    public final double tilt;
    /**
     * Degrees
     */
    public final double azimuth;
    /**
     * Module efficiency from data sheet 0-1
     */
    public final double efficiency;
    /**
     * Power under standard illumination. Effectively kWp for the array in Watts
     */
    public final double power;
    public final String name;
    public final String description;

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
        return String.format("Array: '%s' %s power=%4.0f", name, description, power);
    }

    /**
     * The maximum (sunny day) amount of power the array could produce.
     *
     * This value must be scaled back to allow for bad weather, then potentially
     * throttled back by the inverter,
     *
     * @param dday Year day 0-364
     * @param hour Solar hour
     * @param array Array to be calculated
     *
     * @return Power Watts
     */
    public double availablePower(int dday, double hour) {
        double insolation = calc.insSolarRadiation(hour, SystemData.latitude, SystemData.longitude, 0, tilt, azimuth, dday, Calculator.CN, Calculator.SURFACE_REFLECTIVITY);
        return Math.max(0, efficiency * area * insolation);
    }

}
