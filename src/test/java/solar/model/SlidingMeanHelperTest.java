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
public class SlidingMeanHelperTest {

    final double SMALL = 0.001;

    public SlidingMeanHelperTest() {
    }

//    @Test(expected=IllegalArgumentException.class)
//    public void testArgumentEven() {
//        SlidingMean instance = new SlidingMean(2);
//    }
    @Test
    public void testArgumentNegative() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new SlidingMeanHelper(-1);
        });
    }

    @Test
    public void testGetWindowSize() {
        SlidingMeanHelper instance = new SlidingMeanHelper(9);
        assertEquals(9, instance.getWindowSize());
    }

    @Test
    public void testGetCount() {
        SlidingMeanHelper instance = new SlidingMeanHelper(1);
        assertEquals(0, instance.getCount());
        instance.add(1.0).add(2.0).add(3.0).add(4.0);
        assertEquals(4, instance.getCount());
    }

    @Test
    public void testGetMeanNormal() {
        SlidingMeanHelper instance = new SlidingMeanHelper(1);
        assertEquals(0, instance.getCount());
        instance.add(1.0).add(2.0).add(3.0).add(7.0);
        assertEquals(7.0, instance.getMean(), SMALL);
    }

    @Test
    public void testGetMeanNormal2() {
        SlidingMeanHelper instance = new SlidingMeanHelper(3);
        instance.add(1.0).add(2.0).add(3.0).add(7.0).add(23.0);
        assertEquals(11.0, instance.getMean(), SMALL);
    }

    @Test
    public void testGetMeanNotEnoughValues1() {
        SlidingMeanHelper instance = new SlidingMeanHelper(3);
        instance.add(3.9);
        assertEquals(3.9, instance.getMean(), SMALL);
    }

    @Test
    public void testGetMeanNotEnoughValues2() {
        SlidingMeanHelper instance = new SlidingMeanHelper(3);
        instance.add(1.0).add(2.0);
        assertEquals(1.5, instance.getMean(), SMALL);
    }

    @Test
    public void testGetMeanNoValues() {
        SlidingMeanHelper instance = new SlidingMeanHelper(3);
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            instance.getMean();
        });
    }

    @Test
    public void testReset() {
        SlidingMeanHelper instance = new SlidingMeanHelper(1);
        instance.add(1.0).add(2.0).add(3.0).add(4.0);
        assertEquals(4, instance.getCount());
        instance.reset();
        assertEquals(0, instance.getCount());
    }
}
