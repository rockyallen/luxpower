package solar.model;

import javax.measure.Quantity;
import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Energy;
import javax.measure.quantity.Time;
import tech.units.indriya.ComparableQuantity;

/**
 * Data only structure to hold financial parameters foe the model.
 *
 * @threadsafety Immutable
 * @author rocky
 */
public class Costs {

    private final String name;
    private final ComparableQuantity<?> standingCharge;
    private final ComparableQuantity<?> importPrice;
    private final ComparableQuantity<?> exportPrice;
    private final ComparableQuantity<?> fits;

    /**
     * 
     * @param name
     * @param standingCharge £/day
     * @param importPrice £/kWh
     * @param exportPrice £/kWh
     * @param fits £/kWh
     */
    Costs(String name, ComparableQuantity<?> standingCharge, ComparableQuantity<?> importPrice, ComparableQuantity<?> exportPrice, ComparableQuantity<?> fits) {
        this.name = name;
        this.standingCharge = standingCharge;
        this.importPrice = importPrice;
        this.exportPrice = exportPrice;
        this.fits = fits;
    }

    /**
     * Calculate total bill for the period.
     * Standing charge and imports are charges; exports and fits are rebates.
     * Result can be positive or negative.
     * 
     * @param days Number of days the bill is for
     * @param imported imported energy in the period
     * @param exported exported energy in the period
     * @param generated generated energy in the period
     * @return 
     */
    public Quantity<Dimensionless> bill(ComparableQuantity<Time> days,
            ComparableQuantity<Energy> imported, ComparableQuantity<Energy> exported,
            ComparableQuantity<Energy> generated) {
        return (standingCharge.multiply(days).asType(Dimensionless.class))
                .add(importPrice.multiply(imported).asType(Dimensionless.class))
                .subtract(exportPrice.multiply(exported).asType(Dimensionless.class))
                .subtract(fits.multiply(generated).asType(Dimensionless.class)).asType(Dimensionless.class);
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the standing charge, £/day
     */
    public ComparableQuantity<?> getStandingCharge() {
        return standingCharge;
    }

    /**
     * @return the import rate £/kWh
     */
    public ComparableQuantity<?> getImportPrice() {
        return importPrice;
    }

    /**
     * @return the export rate £/kWh
     */
    public ComparableQuantity<?> getExportPrice() {
        return exportPrice;
    }

    /**
     * @return the FITS rate £/kWh
     */
    public ComparableQuantity<?> getFits() {
        return fits;
    }
}
