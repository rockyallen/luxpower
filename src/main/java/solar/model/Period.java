package solar.model;

/**
 *
 * @author rocky
 */
public enum Period {
    /**
     * Day number 0-364.
     */
    DAY, 
    /**
     * Day in month number 1-31. Why not 0 based?
     */
    DATE, 
    /**
     * Week number 0-51.
     */
    WEEK, 
    /**
     * Month number 0-11.
     */
     MONTH, 
     /**
      * Hour number 0-23.
      */
     HOUR, 
     /**
      * All records?.
      */
     ALL //, YEAR  
}
