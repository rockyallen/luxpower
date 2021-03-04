package solar.model;

import javax.measure.MetricPrefix;
import javax.measure.Quantity;
import javax.measure.Unit;
import javax.measure.quantity.Angle;
import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.ElectricPotential;
import javax.measure.quantity.ElectricResistance;
import javax.measure.quantity.Energy;
import javax.measure.quantity.Power;
import javax.measure.quantity.Time;
import static tech.units.indriya.AbstractUnit.ONE;
import tech.units.indriya.ComparableQuantity;
import tech.units.indriya.function.MultiplyConverter;
import tech.units.indriya.quantity.Quantities;
import tech.units.indriya.unit.TransformedUnit;
import static tech.units.indriya.unit.Units.DAY;
import static tech.units.indriya.unit.Units.HOUR;
import static tech.units.indriya.unit.Units.JOULE;
import static tech.units.indriya.unit.Units.OHM;
import static tech.units.indriya.unit.Units.RADIAN;
import static tech.units.indriya.unit.Units.SECOND;
import static tech.units.indriya.unit.Units.SQUARE_METRE;
import static tech.units.indriya.unit.Units.VOLT;
import static tech.units.indriya.unit.Units.WATT;

/**
 * Units and constants for the Solar PV domain.
 *
 * Gathered here for efficiency. I hope.
 *
 * @author rocky
 */
public class SolarUnitsAndConstants {

    public static final Unit<Angle> DEGREE_ANGLE = new TransformedUnit<>("\u00b0", RADIAN, MultiplyConverter.of(180.0 / Math.PI));
    public static final Unit<?> IRRADIANCE = WATT.divide(SQUARE_METRE);
    public static final Unit<Power> KILO_WATT = MetricPrefix.KILO(WATT);
    public static final Unit<Energy> WATT_HOUR = WATT.multiply(HOUR).asType(Energy.class);
    public static final Unit<Energy> KILO_WATT_HOUR = MetricPrefix.KILO(WATT_HOUR);
    public static final Unit<Dimensionless> POUND = ONE.alternate("£");
    public static final Unit<?> DAILY_CHARGE = POUND.divide(DAY);
    public static final Unit<?> ENERGY_PRICE = POUND.divide(KILO_WATT_HOUR);

    public static final ComparableQuantity<ElectricResistance> ZERO_RESISTANCE = Quantities.getQuantity(0.0, OHM);
    public static final ComparableQuantity<Time> ZERO_TIME = Quantities.getQuantity(0.0, SECOND);
    public static final ComparableQuantity<Energy> ZERO_ENERGY = Quantities.getQuantity(0.0, JOULE);
    public static final ComparableQuantity<Power> ZERO_POWER = Quantities.getQuantity(0.0, WATT);
    public static final ComparableQuantity<Dimensionless> ZERO_NUMBER = Quantities.getQuantity(0, ONE);
    public static final ComparableQuantity<Dimensionless> ONE_NUMBER = Quantities.getQuantity(1, ONE);

    public static final ComparableQuantity<?> STANDARD_IRRADIANCE = Quantities.getQuantity(1000.0, IRRADIANCE);
    public static final ComparableQuantity<Angle> defaultLatitude = Quantities.getQuantity(50.6, DEGREE_ANGLE); // degrees google maps
    public static final ComparableQuantity<Angle> defaultLongitude = Quantities.getQuantity(-2.5, DEGREE_ANGLE); // degrees google maps

    public static final Quantity<ElectricPotential> GRID_VOLTAGE = Quantities.getQuantity(240.0, VOLT);

    /**
     * Complements the one in Quantities, but for Strings to be converted to
     * double.
     *
     * @param <T>
     * @param s
     * @param T
     * @return
     */
    public static <T extends Quantity<T>> ComparableQuantity<T> getQuantity(String s, Unit T) {
        return Quantities.getQuantity(Float.valueOf(s), T);
    }

    /**
     * Why cant I make this generic?
     */
    public static ComparableQuantity<Power> minimum(ComparableQuantity<Power> a, ComparableQuantity<Power> b) {
        return a.compareTo(b) < 0 ? a : b;
    }

    /**
     * Why cant I make this generic?
     */
    public static ComparableQuantity<Power> maximum(ComparableQuantity<Power> a, ComparableQuantity<Power> b) {
        return a.compareTo(b) > 0 ? a : b;
    }

//    public static <T extends ComparableQuantity<T>> ComparableQuantity<T> max(ComparableQuantity<T> a, ComparableQuantity<T> b) {
//        return a.compareTo(b) > 0 ? a : b;
//    }
    
    /**
     * Or should it return double?
     * @param a
     * @return 
     */
    public Quantity<Dimensionless> sin(Quantity<Angle> a) {
        return Quantities.getQuantity(Math.sin(a.to(RADIAN).getValue().doubleValue()), ONE);
    }

    /**
     * Or should it return double?
     * @param a
     * @return 
     */
    public Quantity<Dimensionless> cos(Quantity<Angle> a) {
        return Quantities.getQuantity(Math.cos(a.to(RADIAN).getValue().doubleValue()), ONE);
    }

    /**
     * Or should it return double?
     * @param a
     * @return 
     */
    public Quantity<Dimensionless> tan(Quantity<Angle> a) {
        return Quantities.getQuantity(Math.tan(a.to(RADIAN).getValue().doubleValue()), ONE);
    }
}
