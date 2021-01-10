package solar.model;

/**
 *
 * @author rocky
 */
public class Grid {

    private double imported;
    private double exported;
    private final double exportRate;
    private final double importRate;

    public Grid(double exportRate, double importRate) {
        this.exportRate = exportRate;
        this.importRate = importRate;
    }

    public void export(double d) {
        exported += d;
    }

    public void importFrom(double d) {
        imported += d;
    }

    /**
     * @return the imported
     */
    public double getImported() {
        return imported;
    }

    /**
     * @return the exported
     */
    public double getExported() {
        return exported;
    }

    public double getCost() {
        return imported * importRate - exported * exportRate;
    }

    void reset() {
        imported = 0.0;
        exported = 0.0;
    }
}
