package solar.model;

/**
 * An array, modelled as a single large panel.
 *
 * @author rocky
 */
public class SolarArray {

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

    public SolarArray(String name, double area, double tilt, double azimuth, double efficiency) {
        this.name=name;
        this.area = area;
        this.tilt = tilt;
        this.azimuth = azimuth;
        this.efficiency = efficiency;
        this.power = area * efficiency * 1000.0;
    }
    
   @Override
    public String toString() {
        return String.format("Array: name='%s' power=%4.0f", name, power);
    }}
