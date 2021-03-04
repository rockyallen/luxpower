package solar.model;

import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.ElectricCurrent;
import javax.measure.quantity.ElectricResistance;
import javax.measure.quantity.Power;
import static solar.model.SolarUnitsAndConstants.*;
import static solar.model.SolarUnitsAndConstants.maximum;
import static solar.model.SolarUnitsAndConstants.minimum;
import static tech.units.indriya.AbstractUnit.ONE;
import tech.units.indriya.ComparableQuantity;
import static tech.units.indriya.unit.Units.*;

/**
 * Efficiency model is: Ploss = parasiticpower + I^2 R.
 *
 * @threadsafety Immutable
 *
 * @author rocky
 */
public class Inverter {

    public final ComparableQuantity<Power> inverterExportLimit;
    // Ohms
    public final ComparableQuantity<ElectricResistance> resistance;
    // Watts
    public final ComparableQuantity<Power> parasiticPower;
    // Descriptive name
    public final String name;

    /**
     *
     * @param name
     * @param powerLimit
     * @param parasiticPowerLoss
     * @param resistance
     */
    public Inverter(String name, String description, ComparableQuantity<Power> powerLimit, ComparableQuantity<Power> parasiticPowerLoss, ComparableQuantity<ElectricResistance> resistance) {
        this.name = name;
        this.inverterExportLimit = powerLimit.to(WATT);
        this.parasiticPower = parasiticPowerLoss.to(WATT);
        this.resistance = resistance.to(OHM);
        if ("".equals(name)) {
            throw new IllegalArgumentException("name must not be blank");
        }
    }

    /**
     * Convenience for the default model. Assume 1/2 the loss is due to
     * parasitic power, and 1/2 due to resistance (at 50% load)
     * 
     * UK grid output voltage is assumed.
     * 
     * Example:
     * 1000 W max, 90% 1/2 load efficiency
     * 
     * total loss at 500 W = 90% * 500 W = 45 W, parasitic = 22.5 W ohmic at 1/2 load = 22.5 W.
     * I at half load = (500/240), I^2 R = P, R = P/I^2 = 22.5/(500/240)^2 = 2.88 Ohm
     * 
     * @param name Description
     * @param powerLimit Rated power output
     * @param halfLoadEfficiency Efficiency at half maximum output. Usually near
     * maximum and the same as the 'european' efficiency.
     * @return new Inverter
     */
    public static Inverter valueOf(String name, String description, ComparableQuantity<Power> powerLimit, ComparableQuantity<Dimensionless> halfLoadEfficiency) {

        if (halfLoadEfficiency.to(ONE).getValue().doubleValue() > 0.999) {
            return new Inverter(name, description, powerLimit, ZERO_POWER, ZERO_RESISTANCE);
        } else {
            ComparableQuantity<Power> halfLoadPowerLoss = ONE_NUMBER.subtract(halfLoadEfficiency).multiply(powerLimit).multiply(0.5).asType(Power.class);
            ComparableQuantity<Power> parasiticpower = halfLoadPowerLoss.divide(2);

            ComparableQuantity<Power> resistanceLoss = halfLoadPowerLoss.subtract(parasiticpower);
            ComparableQuantity<ElectricCurrent> IHalfLoad = powerLimit.divide(GRID_VOLTAGE).divide(2).asType(ElectricCurrent.class);
            ComparableQuantity<ElectricResistance> resistance = resistanceLoss.divide(IHalfLoad.multiply(IHalfLoad)).asType(ElectricResistance.class);
            return new Inverter(name, description, powerLimit, parasiticpower, resistance);
        }
    }

    /**
     * Output power for a given input
     *
     * @param pin Input power, Watts
     * @return Output power, Watts
     */
    public ComparableQuantity<Power> pout(ComparableQuantity<Power> pin) {
        ComparableQuantity<ElectricCurrent> I = pin.divide(GRID_VOLTAGE).asType(ElectricCurrent.class);
        ComparableQuantity<Power> resistiveloss = I.multiply(I).multiply(resistance).asType(Power.class);
        ComparableQuantity<Power> ploss = parasiticPower.add(resistiveloss).asType(Power.class);
        ComparableQuantity<Power> pout = maximum(minimum(inverterExportLimit, pin.subtract(ploss)), ZERO_POWER);
        return pout;
    }

    @Override
    public String toString() {
        return String.format("Inverter: name='%s' power=%s parasitic=%s resistance=%s", name, inverterExportLimit, parasiticPower, resistance);
    }
}
