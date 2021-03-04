package solar.model;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Area;
import javax.measure.quantity.Power;
import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Energy;
import static solar.model.SolarUnitsAndConstants.*;
import static tech.units.indriya.AbstractUnit.ONE;
import tech.units.indriya.ComparableQuantity;
import tech.units.indriya.quantity.Quantities;
import static tech.units.indriya.quantity.Quantities.getQuantity;
import static tech.units.indriya.unit.Units.*;

/**
 * A solar array, modelled as a single large panel.
 *
 * @threadsafety Immutable
 *
 * @author rocky
 */
public class SolarArray {

    private static final Calculator calculator =  Calculator.getInstance();
    private final String name;
    private final String description;
    private final ComparableQuantity<Angle> latitude;
    private final ComparableQuantity<Angle> longitude;
    private final boolean azimuthTracking;
    private final boolean elevationTracking;
    /**
     * Sum of area of all modules
     */
    private final ComparableQuantity<Area> area;
    /**
     * Degrees
     */
    private final ComparableQuantity<Angle> tilt;
    /**
     * Degrees
     */
    private final ComparableQuantity<Angle> azimuth;
    /**
     * Module efficiency from data sheet 0-1
     */
    private final ComparableQuantity<Dimensionless> efficiency;
    /**
     * Power under standard illumination. Effectively kWp for the array in Watts
     */
    private final ComparableQuantity<Power> power;
    /**
     *
     */
    private final ComparableQuantity<Angle> standardMeridian;

    /**
     *
     * @param name
     * @param description
     * @param area Total area from data sheet. This is the effective area on
     * which efficiency is calculated, m2
     * @param tilt Tilt from vertical, degrees. Ignored if elevationTracking is
     * true.
     * @param azimuth Azimuth, degrees. Ignored if azimuthTracking is true.
     * @param efficiency Conversion efficiency based on total array area, 0-1
     * @param latitude Latitude of the installation, degrees
     * @param longitude Longitude of the installation, degrees
     * @param azimuthTracking True if the array can track the sun in azimuth.
     * @param elevationTracking True if the array can track the sun in
     * elevation.
     */
    public SolarArray(String name,
            String description,
            ComparableQuantity<Area> area,
            ComparableQuantity<Angle> tilt,
            ComparableQuantity<Angle> azimuth,
            ComparableQuantity<Dimensionless> efficiency,
            ComparableQuantity<Angle> latitude,
            ComparableQuantity<Angle> longitude,
            boolean azimuthTracking,
            boolean elevationTracking
    ) {
        if ("".equals(name)) {
            throw new IllegalArgumentException("name must not be blank");
        }
        if (efficiency.compareTo(ZERO_NUMBER) <= 0 ){
            throw new IllegalArgumentException("efficiency must be positive");
        }
        if (efficiency.compareTo(ONE_NUMBER) > 0 ){
            throw new IllegalArgumentException("efficiency must be less than 1");
        }
        this.name = name;
        this.description = description;
        this.area = area.to(SQUARE_METRE);
        this.tilt = tilt.to(DEGREE_ANGLE);
        this.azimuth = azimuth.to(DEGREE_ANGLE);
        this.efficiency = efficiency.to(ONE);
        this.latitude = latitude.to(DEGREE_ANGLE);
        this.longitude = longitude.to(DEGREE_ANGLE);
        this.azimuthTracking = azimuthTracking;
        this.elevationTracking = elevationTracking;
        this.power = area.multiply(SolarUnitsAndConstants.STANDARD_IRRADIANCE).multiply(efficiency).asType(Power.class).to(WATT);
        this.standardMeridian = Quantities.getQuantity(0.0, SolarUnitsAndConstants.DEGREE_ANGLE); // Wrong in general, but OK for the UK
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Array: '%s' '%s' kWp=%s", name, description, power.to(KILO_WATT).toString()));
        sb.append(" tilt=");
        if (elevationTracking) {
            sb.append("tracking");
        } else {
            sb.append(tilt);
        }
        sb.append(" azimuth=");
        if (azimuthTracking) {
            sb.append("tracking");
        } else {
            sb.append(azimuth);
        }
        return sb.toString();
    }

    /**
     * The sunny day power of the array.
     *
     * This value must be scaled back to allow for bad weather, then potentially
     * throttled back by the inverter to get the yield.
     *
     * @param dayNumber Year day, 0-364
     * @param hour Solar hour
     *
     * @return Power, Watts
     */
    public ComparableQuantity<Power> availablePower(int dayNumber, double hour) {

        double arrayElevation = elevationTracking ? calculator.sunElevation(
                latitude.getValue().doubleValue(),
                calculator.solarDeclination(dayNumber), hour)
                : tilt.getValue().doubleValue();

        double arrayAzimuth = azimuthTracking ? calculator.sunAzimuth(
                latitude.getValue().doubleValue(),
                calculator.solarDeclination(dayNumber), hour)
                : azimuth.getValue().doubleValue();

        ComparableQuantity<?> insolation = Quantities.getQuantity(calculator.insSolarRadiation(
                hour,
                latitude.getValue().doubleValue(),
                longitude.getValue().doubleValue(),
                azimuth.getValue().doubleValue(),
                arrayElevation,
                arrayAzimuth,
                dayNumber,
                Calculator.CN,
                Calculator.SURFACE_REFLECTIVITY), IRRADIANCE);

        ComparableQuantity<Power> p = insolation.multiply(area).multiply(efficiency).asType(Power.class).to(KILO_WATT);
        return p.getValue().doubleValue() <= 0.0
                ? SolarUnitsAndConstants.ZERO_POWER : p;
    }

    /**
     * @return the power in Watts
     */
    public ComparableQuantity<Power> getRatedPower() {
        return power;
    }

    /**
     * @return Energy in kWh if it runs at the rated power for 1 year.
     */
    public ComparableQuantity<Energy> getRatedCapacity() {
        return power.multiply(getQuantity(1,YEAR)).asType(Energy.class).to(KILO_WATT_HOUR);
    }
}
