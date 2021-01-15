package solar.model;

/**
 *
 * @author rocky
 */
public class Costs {
    private String name;
    private double standingCharge;
    private double importPrice;
    private double exportPrice;
    private double fits;

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the standingCharge
     */
    public double getStandingCharge() {
        return standingCharge;
    }

    /**
     * @param standingCharge the standingCharge to set
     */
    public void setStandingCharge(double standingCharge) {
        this.standingCharge = standingCharge;
    }

    /**
     * @return the importPrice
     */
    public double getImportPrice() {
        return importPrice;
    }

    /**
     * @param importPrice the importPrice to set
     */
    public void setImportPrice(double importPrice) {
        this.importPrice = importPrice;
    }

    /**
     * @return the exportPrice
     */
    public double getExportPrice() {
        return exportPrice;
    }

    /**
     * @param exportPrice the exportPrice to set
     */
    public void setExportPrice(double exportPrice) {
        this.exportPrice = exportPrice;
    }

    /**
     * @return the fits
     */
    public double getFits() {
        return fits;
    }

    /**
     * @param fits the fits to set
     */
    public void setFits(double fits) {
        this.fits = fits;
    }
    
}
