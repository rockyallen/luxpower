package solar.model;

/**
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
public double getDemand(double solarHour){
float consumptionPower = HOURLY_CONSUMPTION[(int)solarHour] * 1000;
    return consumptionPower;
}    
}
