package solar.model;

import javax.measure.Quantity;
import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Energy;
import javax.measure.quantity.Power;
import javax.measure.quantity.Time;
import static solar.model.SolarUnitsAndConstants.*;
import static tech.units.indriya.AbstractUnit.ONE;
import tech.units.indriya.ComparableQuantity;
import static tech.units.indriya.unit.Units.*;

/**
 * Models a battery specifically, but could be used for any similar energy
 * store. Efficiency is charging efficiency, discharge efficiency is 100%.
 *
 * Charge and discharge currents are limited.
 *
 * For convenience, it logs the total amount of charge and discharge; this log
 * can be reset without affecting the stored energy.
 *
 * @threadsafety Not thread safe
 *
 * @author rocky
 */
public class EnergyStore {

    private final String name;
    private final String description;
    private final ComparableQuantity<Energy> effectiveCapacity;
    private final ComparableQuantity<Energy> nominalCapacity;
    private final ComparableQuantity<Dimensionless> efficiency;
    private final ComparableQuantity<Power> pChargeMax;
    private final ComparableQuantity<Power> pDischargeMax;

    private ComparableQuantity<Energy> currentEnergy = ZERO_ENERGY;
    private ComparableQuantity<Energy> charge = ZERO_ENERGY;
    private ComparableQuantity<Energy> discharge = ZERO_ENERGY;

    /**
     * @param name Short descriptive name
     * @param description Full description
     * @param nominalCapacity Nominal battery capacity Wh
     * @param effectiveCapacity Useful battery capacity Wh
     * @param efficiency Round trip efficiency 0.1 to 1.0
     * @param pChargeMax Maximum charge power, W
     * @param pDischargeMax Maximum discharge power, W
     * @throws IllegalArgumentException if: name is blank; all other parameters
     * are not positive; effectiveCapacity is greater than nominalCapacity;
     * efficiency is not between 0.1 and 1.0.
     */
    public EnergyStore(String name, String description,
            ComparableQuantity<Energy> nominalCapacity,
            ComparableQuantity<Energy> effectiveCapacity,
            ComparableQuantity<Dimensionless> efficiency,
            ComparableQuantity<Power> pChargeMax,
            ComparableQuantity<Power> pDischargeMax) {

        if ("".equals(name)) {
            throw new IllegalArgumentException("name must not be blank");
        }
        if (efficiency.compareTo(ZERO_NUMBER) <= 0) {
            throw new IllegalArgumentException("efficiency must be positive");
        }
        if (efficiency.compareTo(ONE_NUMBER) > 0) {
            throw new IllegalArgumentException("efficiency must be less than 1");
        }
        if ("".equals(name)) {
            throw new IllegalArgumentException("name must not be blank");
        }
        if (effectiveCapacity.compareTo(nominalCapacity) > 0) {
            throw new IllegalArgumentException("effective capacity must be less than nominal (" + effectiveCapacity + "," + nominalCapacity + ")");
        }
        if (effectiveCapacity.compareTo(ZERO_ENERGY) < 0) {
            throw new IllegalArgumentException("effective capacity must be greater than or equal to 0 (" + effectiveCapacity + ")");
        }
        this.name = name;
        this.description = description;
        this.nominalCapacity = nominalCapacity;
        this.effectiveCapacity = effectiveCapacity;
        this.efficiency = efficiency;
        this.pChargeMax = pChargeMax;
        this.pDischargeMax = pDischargeMax;
    }

    /**
     * Try to add some energy in it. Returns the mean power consumed during the
     * time interval, which will be less than or equal to the maximum charge
     * power and may be further limited if the battery is full or nearly full.
     *
     * @param p Power available, W
     * @param t Time over which it is available, hours
     * @return The mean power consumed of the time interval, which will be
     * between 0 and the lower of p and the maximum charge power and may be
     * further limited if the battery is full or nearly full.
     */
    public ComparableQuantity<Power> store(ComparableQuantity<Power> p, ComparableQuantity<Time> t) {
        if (p.compareTo(ZERO_POWER) <= 0) {
            throw new IllegalArgumentException("power demand must be +ve");
        }
        if (t.compareTo(ZERO_TIME) <= 0) {
            throw new IllegalArgumentException("time step must be +ve");
        }

        ComparableQuantity<Power> retPower = ZERO_POWER;
        // not more than the maximum allowed
        ComparableQuantity<Power> p1 = minimum(p, pChargeMax);
        // Can you store it all?
        ComparableQuantity<Energy> energyOffered = p1.multiply(t).asType(Energy.class);

        ComparableQuantity<Energy> toFill = effectiveCapacity.subtract(currentEnergy).divide(efficiency).asType(Energy.class);
        if (toFill.compareTo(energyOffered) > 0) {
            // Yes, you can store it all
            currentEnergy = currentEnergy.add(energyOffered.multiply(efficiency).asType(Energy.class));
            retPower = p1;
        } else {
            // No, top it up and limit power
            retPower = toFill.divide(t).asType(Power.class);
            currentEnergy = effectiveCapacity;
        }
        charge = charge.add(retPower.multiply(t).asType(Energy.class));
        return retPower.to(WATT);
    }

    /**
     * Try to draw power from it.
     *
     * The returned power is initially the lower of the discharge limit and the
     * demanded power. Then check if there is enough energy. If not, all the
     * remaining energy is used, and the returned power is the average power
     * over t.
     *
     * @param p power demanded W
     * @param t time it is demanded for hr
     *
     * @return the average power delivered over time t.
     */
    public ComparableQuantity<Power> demand(ComparableQuantity<Power> p, ComparableQuantity<Time> t) {
        if (p.compareTo(ZERO_POWER) <= 0) {
            throw new IllegalArgumentException("power demand must be +ve");
        }
        if (t.compareTo(ZERO_TIME) <= 0) {
            throw new IllegalArgumentException("time step must be +ve");
        }

        ComparableQuantity<Power> retPower = ZERO_POWER;

        ComparableQuantity<Power> p1 = minimum(p, pDischargeMax);

        // Can you supply it all?
        ComparableQuantity<Energy> energyRequested = p1.multiply(t).asType(Energy.class);

        if (currentEnergy.compareTo(energyRequested) > 0) {
            // yes, you can have it all
            currentEnergy = currentEnergy.subtract(energyRequested);
            retPower = p1;
        } else {
            // no, empty it and return average power
            retPower = currentEnergy.divide(t).asType(Power.class);
            currentEnergy = ZERO_ENERGY;
        }
        discharge = discharge.add(retPower.multiply(t).asType(Energy.class));
        return retPower;
    }

    /**
     * What is the current amount of energy in the store?
     *
     * @return wh
     */
    public ComparableQuantity<Energy> getEnergy() {
        return currentEnergy;
    }

    /**
     * Resets charge and discharge log. Does NOT affect energy.
     */
    void resetLog() {
        charge = ZERO_ENERGY;
        discharge = ZERO_ENERGY;
    }

    /**
     * Initialise battery capacity without affecting charge log
     *
     * @param fraction
     */
    void setCharge(double fraction) {
        currentEnergy = effectiveCapacity.multiply(fraction);
    }

    /**
     * Capacity from max discharge to max fill.
     *
     * @return Wh
     */
    public ComparableQuantity<Energy> getEffectiveCapacity() {
        return effectiveCapacity;
    }

    /**
     * Rated (not useable) capacity
     *
     * @return Wh
     */
    public ComparableQuantity<Energy> getNominalCapacity() {
        return nominalCapacity;
    }

    public String getName() {
        return name;
    }

    /**
     * @return the currentEnergy Wh
     */
    public ComparableQuantity<Energy> getCurrentEnergy() {
        return currentEnergy;
    }

    /**
     * @return the total charge since logging started
     */
    public ComparableQuantity<Energy> getCharge() {
        return charge;
    }

    /**
     * @return the total discharge since logging started
     */
    public ComparableQuantity<Energy> getDischarge() {
        return discharge;
    }

    /**
     * State of charge (another view of current energy)
     *
     * @return 0.0-1.0
     */
    public Quantity<Dimensionless> getSoc() {
        return nominalCapacity.getValue().doubleValue() < 0.001 ? ZERO_NUMBER : currentEnergy.divide(nominalCapacity).asType(Dimensionless.class);
    }

    @Override
    public String toString() {
        return "EnergyStore: name='" + name + "' nominal=" + nominalCapacity + " effective=" + effectiveCapacity + " charge=" + pChargeMax + " discharge=" + pDischargeMax + " SOC=" + getSoc();
    }

}
