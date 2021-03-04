package solar.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * This is really a poor man's database. Why not user a real one?
 *
 * @author rocky
 */
public class DatedNumberLogger {

    private final List<DatedValue> recs = new ArrayList<>();

    public void add(Date d, double value) {
        DatedValue r = new DatedValue(d,value);
        recs.add(r);
    }

   
    /**
     * Get the unique entries of this tag
     */
    public Collection<Integer> uniqueEntries(Period p) {
        Set<Integer> ret = new TreeSet<Integer>();
        for (DatedValue r : recs) {
            switch (p) {
                case Day:
                    ret.add(Calculator.dayNumber(r.getDate().getMonth(), r.getDate().getDate()));
                    break;
                case Week:
                    ret.add(Calculator.dayNumber(r.getDate().getMonth(), r.getDate().getDate()) / 7);
                    break;
                case Month:
                    ret.add(r.getDate().getMonth());
                    break;
                case All:
                    throw new IllegalArgumentException("");
            }
        }
        return ret;
    }

    /**
     * Get the unique entries with this tag (ie "where p = i")
     */
    public Collection<Double> vals(int i, Period p) {
        List<Double> ret = new ArrayList<>();

        RecordFilter<DatedValue> filter = new RecordFilter<>(recs);

        List<DatedValue> results = filter.period(i, p).result();
        for (DatedValue r : results) {
            ret.add(r.getValue());
        }
        return ret;
    }

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
        for (DatedValue r : recs) {
            sm.add(r.getValue());
            if (sm.getCount() >= window) {
                ret.put(Calculator.dayNumber(r.getDate().getMonth(), r.getDate().getDate()) - offset, sm.getMean());
            }
        }
        return ret;
    }

    /**
     * Total number of records
     *
     * @return
     */
    public int size() {
        return recs.size();
    }

    public void reset() {
        recs.clear();
    }

    public double total() {
        double s = 0.0;
        for (DatedValue rec : recs) {
            s += rec.getValue();
        }
        return s;
    }

    @Override
    public String toString() {
        return "DatedNumberLogger: records=" + recs.size();
    }
}
