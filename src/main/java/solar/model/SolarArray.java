package solar.model;

import javax.measure.Quantity;
import javax.measure.quantity.Angle;
import javax.measure.quantity.Area;
import javax.measure.quantity.Power;
import javax.measure.quantity.Dimensionless;
import tech.units.indriya.quantity.Quantities;
import static tech.units.indriya.unit.Units.RADIAN;
import static tech.units.indriya.unit.Units.SQUARE_METRE;
import static tech.units.indriya.unit.Units.WATT;

/**
 * A solar array, modelled as a single large panel.
 *
 * @threadsafety Immutable
 *
 * @author rocky
 */
public class SolarArray {

    private static final Calculator calculator = new Calculator();
    private final String name;
    private final String description;
    private final Quantity<Angle> latitude;
    private final Quantity<Angle> longitude;
    private final boolean azimuthTracking;
    private final boolean elevationTracking;
    /**
     * Sum of area of all modules
     */
    private final Quantity<Area> area;
    /**
     * Degrees
     */
    private final Quantity<Angle> tilt;
    /**
     * Degrees
     */
    private final Quantity<Angle> azimuth;
    /**
     * Module efficiency from data sheet 0-1
     */
    private final Quantity<Dimensionless> efficiency;
    /**
     * Power under standard illumination. Effectively kWp for the array in Watts
     */
    private final Quantity<Power> power;
    private final Quantity<Angle> standardMeridian;

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
            Quantity<Area> area,
            Quantity<Angle> tilt,
            Quantity<Angle> azimuth,
            Quantity<Dimensionless> efficiency,
            Quantity<Angle> latitude,
            Quantity<Angle> longitude,
            boolean azimuthTracking,
            boolean elevationTracking
    ) {
        if ("".equals(name)) {
            throw new IllegalArgumentException("name must not be blank");
        }
        this.name = name;
        this.description = description;
        this.area = area;
        this.tilt = tilt;
        this.azimuth = azimuth;
        this.efficiency = efficiency;
        this.latitude = latitude;
        this.longitude = longitude;
        this.azimuthTracking = azimuthTracking;
        this.elevationTracking = elevationTracking;
        double p = area.to(SQUARE_METRE).getValue().doubleValue()
                * Calculator.STANDARD_IRRADIANCE.getValue().doubleValue()
                * efficiency.getValue().doubleValue();
        this.power = Quantities.getQuantity(p, WATT);
        this.standardMeridian = Quantities.getQuantity(0.0, Calculator.DEGREE_ANGLE); // Wrong in general, but OK for the UK
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Array: '%s' %s power=%4.0f W ", name, description, getRatedPower()));
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
    public double availablePower(int dayNumber, double hour) {
        double arrayElevation = elevationTracking ? calculator.sunElevation(
                latitude.to(Calculator.DEGREE_ANGLE).getValue().doubleValue(),
                calculator.solarDeclination(dayNumber), hour)
                : tilt.to(Calculator.DEGREE_ANGLE).getValue().doubleValue();

        double arrayAzimuth = azimuthTracking ? calculator.sunAzimuth(
                latitude.to(Calculator.DEGREE_ANGLE).getValue().doubleValue(),
                calculator.solarDeclination(dayNumber), hour)
                : azimuth.to(Calculator.DEGREE_ANGLE).getValue().doubleValue();

        double insolation = calculator.insSolarRadiation(
                hour,
                latitude.to(Calculator.DEGREE_ANGLE).getValue().doubleValue(),
                longitude.to(Calculator.DEGREE_ANGLE).getValue().doubleValue(),
                azimuth.to(Calculator.DEGREE_ANGLE).getValue().doubleValue(),
                arrayElevation,
                arrayAzimuth,
                dayNumber,
                Calculator.CN,
                Calculator.SURFACE_REFLECTIVITY);
        return Math.max(0, efficiency.getValue().doubleValue()
                * area.getValue().doubleValue() * insolation);
    }

    /**
     * @return the power in Watts
     */
    public double getRatedPower() {
        return power.to(WATT).getValue().doubleValue();
    }
}
