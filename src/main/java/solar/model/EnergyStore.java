package solar.model;

/**
 * Models a battery specifically, but could be used for any similar energy store. 
 * Efficiency is charging efficiency, discharge efficiency is 100%.
 *
 * Charge and discharge currents are limited.
 *
 * For convenience, it logs the total amount of charge and discharge; this log
 * can be reset without affecting the stored energy.
 *
 * @author rocky
 */
public class EnergyStore {

    private final String name;
    private final double actualCapacity;
    private final double nominalCapacity;
    private final double efficiency;
    private final double pChargeMax;
    private final double pDischargeMax;

    private double currentEnergy = 0.0;
    private double charge = 0.0;
    private double discharge = 0;

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
    public EnergyStore(String name, String description, double nominalCapacity, double effectiveCapacity, double efficiency, double pChargeMax, double pDischargeMax) {
        this.name = name;
        this.nominalCapacity = nominalCapacity;
        this.actualCapacity = effectiveCapacity;
        this.efficiency = efficiency;
        this.pChargeMax = pChargeMax;
        this.pDischargeMax = pDischargeMax;

        if ("".equals(name)) {
            throw new IllegalArgumentException("name must not be blank");
        }
        if (effectiveCapacity > nominalCapacity) {
            throw new IllegalArgumentException("effective capacity must be less than nominal (" + effectiveCapacity + "," + nominalCapacity + ")");
        }
        if (effectiveCapacity < 0.0) {
            throw new IllegalArgumentException("effective capacity must be greater than or equal to 0 (" + effectiveCapacity + ")");
        }
        if (efficiency < 0.1) {
            throw new IllegalArgumentException("efficiency must be greater than or equal to 0.1 (" + efficiency + ")");
        }
        if (efficiency > 1.0) {
            throw new IllegalArgumentException("efficiency must be less than or equal to 1.0 (" + efficiency + ")");
        }
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
    public double store(double p, double t) {
        if (p <= 0.0) {
            throw new IllegalArgumentException("power demand must be +ve");
        }
        if (t <= 0.0) {
            throw new IllegalArgumentException("time step must be +ve");
        }
        double retPower = 0.0;
        // not more than the maximum allowed
        double p1 = Math.min(p, pChargeMax);
        // Can you accept it all?
        double energyOffered = p1 * t;
        double toFill = (getEffectiveCapacity() - getCurrentEnergy()) / efficiency;
        if (toFill > energyOffered) {
            // Yes, you can store it all
            currentEnergy += energyOffered * efficiency;
            retPower = p1;
        } else {
            // No, fill it and limit power
            retPower = toFill / t;
            currentEnergy = getEffectiveCapacity();
        }
        charge += retPower * t;
        return retPower;
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
    public double demand(double p, double t) {
        if (p <= 0.0) {
            throw new IllegalArgumentException("power demand must be +ve");
        }
        if (t <= 0.0) {
            throw new IllegalArgumentException("time step must be +ve");
        }
        double retPower = 0.0;

        double p1 = Math.min(p, pDischargeMax);

        // Can you supply it all?
        double energyRequested = p1 * t;
        if (getCurrentEnergy() > energyRequested) {
            // yes, you can have it all
            currentEnergy -= energyRequested;
            retPower = p1;
        } else {
            // no, empty it and return average power
            retPower = getCurrentEnergy() / t;
            currentEnergy = 0.0;
        }
        discharge += retPower * t;
        return retPower;
    }

    /**
     * What is the current amount of energy in the store?
     *
     * @return wh
     */
    public double getEnergy() {
        return getCurrentEnergy();
    }

    /**
     * Resets charge and discharge log. Does NOT affect energy.
     */
    void resetLog() {
        charge = 0.0;
        discharge = 0.0;
    }

    /**
     * Capacity from max discharge to max fill.
     *
     * @return Wh
     */
    public double getEffectiveCapacity() {
        return getActualCapacity();
    }

    /**
     * Rated (not useable) capacity
     *
     * @return Wh
     */
    public double getNominalCapacity() {
        return getActualCapacity();
    }

    public String getName() {
        return name;
    }

    /**
     * @return the actualCapacity Wh
     */
    public double getActualCapacity() {
        return actualCapacity;
    }

    /**
     * @return the currentEnergy Wh
     */
    public double getCurrentEnergy() {
        return currentEnergy;
    }

    /**
     * @return the total charge since logging started
     */
    public double getCharge() {
        return charge;
    }

    /**
     * @return the total discharge since logging started
     */
    public double getDischarge() {
        return discharge;
    }

    /**
     * State of charge (another view of current energy)
     *
     * @return 0.0-1.0
     */
    public double getSoc() {
        return currentEnergy / nominalCapacity;
    }
}
