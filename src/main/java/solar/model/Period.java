package solar.model;

/**
 * Calendar periods. Replace by Java built in?
 *
 * @author rocky
 */
public enum Period {
    /**
     * Day number 0-364.
     */
    Day(1),
    /**
     * Day in month number 1-31. Why not 0 based?
     */
    Date(0),
    /**
     * Week number 0-51.
     */
    Week(7),
    /**
     * Month number 0-11.
     */
    Month(30),
    /**
     * Hour number 0-23.
     */
    Hour(0),
    /**
     * All records?.
     */
    All(365); //, YEAR  

    /**
     * HACK - delete me
     *
     * @return
     */
    public double days() {
        return days;
    }
    final int days;

    private Period(int days) {
        this.days = days;
    }
}
