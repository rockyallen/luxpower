package solar.model;

import javax.measure.quantity.Power;
import static solar.model.SolarUnitsAndConstants.KILO_WATT;
import tech.units.indriya.ComparableQuantity;
import tech.units.indriya.quantity.Quantities;

/**
 * Eventually suck this in from the XLS file.
 * 
 * @author rocky
 */
public class Consumption {

    // Total 8 kWh per day, guessed profile per hour in kW
    public static final float[] HOURLY_CONSUMPTION = {0.2f, 0.2f, 0.2f, 0.2f, 0.2f, 0.2f, 0.2f, 0.2f, 0.2f, 0.2f, 0.2f, 0.2f, 0.2f, 0.2f, 0.2f, 0.2f, 0.2f, 1.0f, 1.8f, 1.0f, 0.2f, 0.2f, 0.2f, 0.2f};

    /**
     * Instantaneous power demand in Watts
     *
     * @param solarHour
     * @return
     */
    public ComparableQuantity<Power> getDemand(double solarHour) {
        return Quantities.getQuantity(HOURLY_CONSUMPTION[(int) solarHour], KILO_WATT);
    }
}
