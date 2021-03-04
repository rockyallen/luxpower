package solar.model;

/**
 * Efficiency model is: Ploss = parasiticpower + I^2 R.
 *
 * @threadsafety Immutable
 *
 * @author rocky
 */
public class Inverter {

    public final double inverterExportLimit;
    // Ohms
    public final double resistance;
    // Watts
    public final double parasiticPower;
    // Descriptive name
    public final String name;

    public static final double GRID_VOLTAGE = 240.0;

    /**
     *
     * @param name
     * @param powerLimit Watts
     * @param parasiticPowerLoss Watts
     * @param resistance Ohms (assumes 240V output)
     */
    public Inverter(String name, String description, double powerLimit, double parasiticPowerLoss, double resistance) {
        this.name = name;
        this.inverterExportLimit = powerLimit;
        this.parasiticPower = parasiticPowerLoss;
        this.resistance = resistance;
        if ("".equals(name)) {
            throw new IllegalArgumentException("name must not be blank");
        }
    }

    /**
     * Convenience for the default model. Assume 1/2 the loss is due to
     * parasitic power, and 1/2 due to resistance (at 50% load)
     *
     * @param name Description
     * @param powerLimit Rated power output
     * @param halfLoadEfficiency Efficiency at half maximum output. Usually near
     * maximum and the same as the 'european' efficiency.
     * @return new Inverter
     */
    public static Inverter valueOf(String name, String description, double powerLimit, double halfLoadEfficiency) {
        double halfLoadPowerLoss = 0.5 * (1 - halfLoadEfficiency) * powerLimit;
        double parasiticpower = halfLoadPowerLoss / 2.0;

        double resistanceLoss = halfLoadPowerLoss - parasiticpower;
        double IHalfLoad = (0.5 * powerLimit) / GRID_VOLTAGE;
        double resistance = resistanceLoss / (IHalfLoad * IHalfLoad);
        return new Inverter(name, description, powerLimit, parasiticpower, resistance);
    }

    /**
     * Output power for a given input
     *
     * @param pin Input power, Watts
     * @return Output power, Watts
     */
    public double pout(double pin) {
        double I = pin / GRID_VOLTAGE;
        double ploss = parasiticPower + I * I * resistance;
        double pout = Math.max(Math.min(inverterExportLimit, pin - ploss), 0);
        return pout;
    }

    @Override
    public String toString() {
        return String.format("Inverter: name='%s' power=%4.0f W parasitic=%4.1f W resistance=%3.2f Ohm", name, inverterExportLimit, parasiticPower, resistance);
    }
}
