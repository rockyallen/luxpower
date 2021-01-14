package solar.model;

/**
 * A battery. Efficiency is charging efficiency, discharge efficiency is 100%.
 *
 * Charge and discharge currents are not limited.
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
     * Maximum storage capacity, Wh
     *
     * @param name Short descriptive name
     * @param description Full description
     * @param nominalCapacity Nominal battery capacity Wh
     * @param effectiveCapacity Wh Useful battery capacity
     * @param efficiency Round trip efficiency
     * @param pChargeMax Maximum charge power, W
     * @param pDischargeMax Maximum discharge power, W
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
     * Try to add some energy in it.
     *
     * @param energy Wh
     * @return the actual amount added.
     */
    public double store(double energy) {
        double ret = 0.0;
        double toFill = (getEffectiveCapacity() - getCurrentEnergy()) / efficiency;
        if (toFill < energy) {
            // top it up
            currentEnergy = getEffectiveCapacity();
            ret = toFill;
        } else {
            // store it all
            currentEnergy += energy * efficiency;
            ret = energy;
        }
        charge += ret;
        return ret;
    }

    /**
     * Try to draw energy from it.
     *
     * @param energy Wh
     * @return the actual amount removed.
     */
    public double demand(double energy) {
        double ret = 0.0;
        if (getCurrentEnergy() > energy) {
            // you can have it all
            currentEnergy -= energy;
            ret = energy;
        } else {
            // empty it
            ret = getCurrentEnergy();
            currentEnergy = 0.0;
        }
        discharge += ret;
        return ret;
    }

    /**
     * What is the current amount of energy in the store?
     *
     * @return
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

    public String name() {
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
     * @return the discharge since logging started
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
