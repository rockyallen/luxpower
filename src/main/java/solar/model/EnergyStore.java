package solar.model;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author rocky
 */
public class EnergyStore {

    private final double capacity;
    double currentEnergy = 0.0;
    List<Double> log = new ArrayList<Double>();
    private double max;

    /**
     * Maximum storage capacity, kWh
     *
     * @param capacity
     */
    public EnergyStore(double capacity) {
        this.capacity = capacity;
    }

    /**
     * Try to add some energy in it.
     *
     * @param energy kWh
     * @return the actual amount added.
     */
    public double store(double energy) {
        double toFill = getCapacity() - currentEnergy;
        if (toFill < energy) {
            // top it up
            currentEnergy = getCapacity();
            max = getCapacity();
            return toFill;
        } else {
            // store it all
            currentEnergy += energy;
            max = Math.max(max, currentEnergy);
            return energy;
        }
    }

    /**
     * Try to remove energy from it.
     *
     * @param energy kWh
     * @return the actual amount removed.
     */
    public double demand(double energy) {
        if (currentEnergy > energy) {
            // you can have it all
            currentEnergy -= energy;
            return energy;
        } else {
            // empty it
            double ret = currentEnergy;
            currentEnergy = 0.0;
            return ret;
        }
    }

    /**
     * What is the current amount of energy in the store?
     *
     * @return
     */
    public double getEnergy() {
        return currentEnergy;
    }

    public double getMax() {
        return max;
    }

    public Double[] log() {
        return log.toArray(new Double[0]);
    }

    void reset() {
        currentEnergy = 0.0;
        max = 0.0;
    }

    /**
     * @return the capacity
     */
    public double getCapacity() {
        return capacity;
    }

}
