package solar.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Fluent style reductive filter for records.
 *
 * eg to get the records for 10AM-11AM during May:
 *
 * @pre List<Record> f = new
 * RecordFilter(OriginalData).endOfDay().hour(10).get();
 *
 * All filter method return a new list (the original list is NEVER changed), but
 * records are added without copying.
 *
 * This is (probably?) not very efficient compared to in-place filtering, but it
 * lets you reuse partial outputs, eg do end-of-day filtering once, then use the
 * result many times to filter into month groups.
 *
 * @author rocky
 */
public class RecordFilter<E extends DateProvider> {

    private List<E> current = new ArrayList<>();

    /**
     * @param recs MUST be sorted by ascending Record.date.
     * @param recs
     */
    public RecordFilter(Collection<E> recs) {
        current.addAll(recs);
    }

    /**
     * Get a filtered set of records containing only the final reading of each
     * period.
     *
     * @design Assumes that the records are sorted. Really this should be in
     * DataSource, but it is easier to test here.
     *
     * @return
     */
    public RecordFilter endOfPeriod(Period p) {
        List<E> result = new ArrayList<>();
        E lastRecord = null;
        int lastTime = -1;
        for (E r : current) {

            // edge case: first record
            if (lastRecord == null) {
                lastRecord = r;
                switch (p) {
                    case DAY:
                        lastTime = Calculator.dayNumber(r.getDate().getMonth(), r.getDate().getDate());
                        break;
                    case WEEK:
                        lastTime = Calculator.dayNumber(r.getDate().getMonth(), r.getDate().getDate()) / 52;
                        break;
                    case DATE:
                        lastTime = r.getDate().getDate();
                        break;
                    case MONTH:
                        lastTime = r.getDate().getMonth();
                        break;
                    case HOUR:
                        lastTime = r.getDate().getHours();
                        break;
                    default:
                        throw new IllegalArgumentException("Not implemented for period " + p);
                }
            }
            // all other records
            int day;
            switch (p) {
                case DAY:
                    day = Calculator.dayNumber(r.getDate().getMonth(), r.getDate().getDate());
                    break;
                case WEEK:
                    day = Calculator.dayNumber(r.getDate().getMonth(), r.getDate().getDate()) / 52;
                    break;
                case DATE:
                    day = r.getDate().getDate();
                    break;
                case MONTH:
                    day = r.getDate().getMonth();
                    break;
                case HOUR:
                    day = r.getDate().getHours();
                    break;
                default:
                    throw new IllegalArgumentException("Not implemented for period " + p);
            }
            if (day != lastTime) {
                result.add(lastRecord);
                // prep for next cycle
            }
            lastTime = day;
            lastRecord = r;
        }
        // edge case: last record
        result.add(lastRecord);
        current = result;
        return this;
    }

    public List<E> result() {
        return current;
    }

    /**
     * Return all records where the specified period has the specified value.
     *
     * @design ALL does nothing. Should it be an error to call it?
     *
     * @param i
     * @param p
     * @return
     */
    public RecordFilter period(int i, Period p) {
        if (p == Period.ALL) {
        } else {
            List<E> result = new ArrayList<>();
            for (E r : current) {
                int is;
                switch (p) {
                    case MONTH:
                        is = r.getDate().getMonth();
                        break;
                    case WEEK:
                        is = Calculator.dayNumber(r.getDate().getMonth(), r.getDate().getDate()) / 52;
                        break;
                    case DAY:
                        is = Calculator.dayNumber(r.getDate().getMonth(), r.getDate().getDate());
                        break;
                    case DATE:
                        is = r.getDate().getDate();
                        break;
                    case HOUR:
                        is = r.getDate().getHours();
                        break;
                    default:
                        throw new IllegalStateException("Unhandled case?" + p);
                }
                if (is == i) {
                    result.add(r);
                }
            }
            current = result;
        }
        return this;
    }

    @Override
    public String toString() {
        return "RecordFilter: size=" + current.size();
    }
}
