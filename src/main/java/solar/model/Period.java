package solar.model;

/**
 * Calendar periods. Replace by Java built in?
 * @author rocky
 */
public enum Period {
    /**
     * Day number 0-364.
     */
    DAY(1), 
    /**
     * Day in month number 1-31. Why not 0 based?
     */
    DATE(0), 
    /**
     * Week number 0-51.
     */
    WEEK(7), 
    /**
     * Month number 0-11.
     */
     MONTH(30), 
     /**
      * Hour number 0-23.
      */
     HOUR(0), 
     /**
      * All records?.
      */
     ALL(365); //, YEAR  

     /**
      * HACK - delete me
      * 
      * @return 
      */
    public double days() {
        return days;
    }
    final int days;
    
    Period(int days){this.days = days;}
}
