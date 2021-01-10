package solar.model;

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
public class SlidingMeanTest {
    
    public SlidingMeanTest() {
    }

    @Test
    public void testAdd() {
        SlidingMeanHelper sm = new SlidingMeanHelper(3);
        sm.add(3);
        sm.add(7);
        sm.add(2);
        assertEquals(12.0/3.0, sm.getMean(), 0.0001);
        sm.add(1);
        assertEquals(10.0/3.0, sm.getMean(), 0.0001);
    }

    @Test
    public void testMean() {
    }
    
}
