package solar.model;

import java.util.Date;

/**
 * Date+number
 *
 * @threadsafety Immutable
 * @author rocky
 */
public class DatedValue implements DateProvider {

    private final Date d;
    private final double value;

    @Override
    public Date getDate() {
        return getD();
    }

    public DatedValue(Date d, double value) {
        this.d = d;
        this.value = value;
    }

    /**
     * @return the d
     */
    public Date getD() {
        return d;
    }

    /**
     * @return the value
     */
    public double getValue() {
        return value;
    }
}
