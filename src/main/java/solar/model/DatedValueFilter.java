package solar.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 *
 * @author rocky
 */
public class DatedValueFilter extends RecordFilter<DatedValue> {
    
    public DatedValueFilter(Collection<DatedValue> recs){ super(recs);}
    
    /**
     * Get the unique entries of this tag
     */
//    public Collection<Integer> uniqueEntries(Period p) {
//        Set<Integer> ret = new TreeSet<Integer>();
//        for (DatedValue r : result()) {
//            switch (p) {
//                case DAY:
//                    ret.add(Calculator.dayNumber(r.getDate().getMonth(), r.getDate().getDate()));
//                    break;
//                case WEEK:
//                    ret.add(Calculator.dayNumber(r.getDate().getMonth(), r.getDate().getDate()) / 7);
//                    break;
//                case MONTH:
//                    ret.add(r.getDate().getMonth());
//                    break;
//                case ALL:
//                    throw new IllegalArgumentException("");
//            }
//        }
//        return ret;
//    }

    /**
     * Get the unique entries with this tag (ie "where p = i")
     */
//    public Collection<Double> values(int i, Period p) {
//        List<Double> ret = new ArrayList<>();
//
//        DataStore.RecordFilter<DatedValue> filter = new DataStore.RecordFilter<>(recs);
//
//        List<DatedValue> results = filter.period(i, p).result();
//        for (DatedValue r : results) {
//            ret.add(r.value);
//        }
//        return ret;
//    }

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
