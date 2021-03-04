package solar.model;

/**
 * Data only structure to hold financial parameters foe the model.
 * @threadsafety Immutable
 * @author rocky
 */
public class Costs {

    private final String name;
    private final double standingCharge;
    private final double importPrice;
    private final double exportPrice;
    private final double fits;

    Costs(String name, double standingCharge, double importPrice, double exportPrice, double fits) {
        this.name = name;
        this.standingCharge = standingCharge;
        this.importPrice = importPrice;
        this.exportPrice = exportPrice;
        this.fits = fits;
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
    public double getStandingCharge() {
        return standingCharge;
    }

    /**
     * @return the import rate £/kWh
     */
    public double getImportPrice() {
        return importPrice;
    }

    /**
     * @return the export rate £/kWh
     */
    public double getExportPrice() {
        return exportPrice;
    }

    /**
     * @return the FITS rate £/kWh
     */
    public double getFits() {
        return fits;
    }
}
