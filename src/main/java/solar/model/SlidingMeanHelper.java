package solar.model;

/**
 * A sliding mean assistant. You can add up to MAXLONG values before it goes
 * wrong. Use the reset method if necessary.
 *
 * @author rocky
 */
public class SlidingMeanHelper {

    private long i = 0;
    private final int windowSize;
    private final double[] lastFewValues;

    public SlidingMeanHelper(int windowSize) {
        if (windowSize < 1) {
            throw new IllegalArgumentException("Must be positive integer");
        }
        this.windowSize = windowSize;
        lastFewValues = new double[windowSize];
    }

    public SlidingMeanHelper add(double val) {
        int trunc = (int) (i % (long) windowSize);
        lastFewValues[trunc] = val;
        i++;
        return this;
    }

    public int getWindowSize() {
        return windowSize;
    }

    public long getCount() {
        return i;
    }

    public double getMean() {
        if (i==0) throw new IllegalStateException("Cant find mean of 0 values");
        double sum = 0.0;
        int n = windowSize;
        if (i<n) n = (int)i;
        for (int j = 0; j < n; j++) {
            sum += lastFewValues[j];
        }
        return sum / n;
    }

    public SlidingMeanHelper reset() {
        i = 0;
        return this;
    }
}
