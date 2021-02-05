package solar.model;

import java.util.Collection;
import java.util.Date;
import java.util.Map;
import solar.model.DatedNumberLogger;
import solar.model.Period;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author rocky
 */
public class DatedNumberLoggerTest {


    public DatedNumberLoggerTest() {
    }

    @Test
    public void testUniqueEntriesDay() {
    DatedNumberLogger instance = new DatedNumberLogger();
        instance.add(new Date(100, 0, 1), 2.0); // day = 0
        instance.add(new Date(100, 0, 2), 3.0); // day = 1
        instance.add(new Date(100, 0, 3), 7.0); // day = 2 
        instance.add(new Date(100, 2, 4), 23.0); // day = 62
        instance.add(new Date(100, 0, 5), 33.0); // day = 4 
        instance.add(new Date(100, 0, 6), 5.0); // day = 5

        Collection<Integer> result = instance.uniqueEntries(Period.Day);
        String s = result.toString();
        assertEquals(6, result.size());
        assertTrue(result.contains(0));
        assertTrue(result.contains(1));
        assertTrue(result.contains(2));
        assertTrue(result.contains(62));
        assertTrue(result.contains(4));
        assertTrue(result.contains(5));
    }

    @Test
    public void testUniqueEntriesMonth() {
    DatedNumberLogger instance = new DatedNumberLogger();
        instance.add(new Date(100, 0, 1), 2.0);
        instance.add(new Date(100, 2, 4), 23.0);
        instance.add(new Date(100, 0, 5), 33.0);
        instance.add(new Date(100, 11, 6), 5.0);

        Collection<Integer> result = instance.uniqueEntries(Period.Month);
        String s = result.toString();
        assertEquals(3, result.size());
        assertTrue(result.contains(0));
        assertTrue(result.contains(2));
        assertTrue(result.contains(11));
    }

    @Test
    public void testVals() {
    }

    @Test
    public void testSlidingMean() {
    DatedNumberLogger instance = new DatedNumberLogger();
        instance.add(new Date(100, 0, 1), 1.0); // day = 0
        instance.add(new Date(100, 0, 2), 2.0); // day = 1 mean of 1,2,3 = 6/3 = 2
        instance.add(new Date(100, 0, 3), 3.0);
        instance.add(new Date(100, 0, 4), 7.0); // day = 3 mean of 3,7,23 = 33/3 = 11
        instance.add(new Date(100, 0, 5), 23.0);
        instance.add(new Date(100, 0, 6), 33.0); // day = 5 mean of 23,33,5= 61/3 = 20.333
        instance.add(new Date(100, 0, 7), 5.0); // day = 6
 
        Map<Integer, Double> result = instance.slidingMean(3);
        String s = result.toString();
        assertEquals(5, result.size());
        assertEquals(2.0, result.get(1), 0.001);
        assertEquals(11.0, result.get(3), 0.001);
        assertEquals(20.333, result.get(5), 0.001);
    }

    @Test
    public void testSize() {
    DatedNumberLogger instance = new DatedNumberLogger();
        instance.add(new Date(100, 0, 1), 1.0);
        instance.add(new Date(100, 0, 2), 2.0);
        instance.add(new Date(100, 0, 9), 3.0);
        assertEquals(3, instance.size());
    }

    @Test
    public void testReset() {
    DatedNumberLogger instance = new DatedNumberLogger();
        instance.add(new Date(100, 0, 0), 1.0);
        instance.add(new Date(100, 0, 1), 2.0);
        instance.add(new Date(100, 0, 2), 3.0);
        assertEquals(3, instance.size());
        instance.reset();
        assertEquals(0, instance.size());
    }

    @Test
    public void testTotal() {
    DatedNumberLogger instance = new DatedNumberLogger();
        instance.add(new Date(100, 0, 0), 1.0);
        instance.add(new Date(100, 0, 1), 2.0);
        instance.add(new Date(100, 0, 2), 3.9);
        assertEquals(6.9, instance.total(), 0.001);
    }
}
