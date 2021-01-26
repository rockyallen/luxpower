package solar.model;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

/**
 * Extended filter to deal with DatedValues
 * 
 * @author rocky
 */
public class DatedValueFilter extends RecordFilter<DatedValue> {
    
    public DatedValueFilter(Collection<DatedValue> recs){ super(recs);}
    
    /**
     * Apply a top-hat sliding mean filter of width window.
     *
     * @param window
     * @return Map of day number vs value
     */
    public Map<Integer, Double> slidingMean(int window) {
        if (window < 1 || window % 2 != 1) {
            throw new IllegalArgumentException("window must be odd and positive" + window);
        }
        Map<Integer, Double> ret = new TreeMap<>();
        SlidingMeanHelper sm = new SlidingMeanHelper(window);
        int offset = window / 2;
        for (DatedValue r : result()) {
            sm.add(r.getValue());
            if (sm.getCount() >= window) {
                ret.put(Calculator.dayNumber(r.getDate().getMonth(), r.getDate().getDate()) - offset, sm.getMean());
            }
        }
        return ret;
    }

   public double total() {
        double s = 0.0;
        for (DatedValue rec : result()) {
            s += rec.getValue();
        }
        return s;
    }   

    public int size() {
        return result().size();
    }
}
