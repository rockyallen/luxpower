package solar.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
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
public class DatedValueFilterTest {
    
    public DatedValueFilterTest() {
    }
    
    @BeforeAll
    public static void setUpClass() {
    }
    
    @AfterAll
    public static void tearDownClass() {
    }
    
    @BeforeEach
    public void setUp() {
    }
    
    @AfterEach
    public void tearDown() {
    }

    /**
     * Test of uniqueEntries method, of class DatedValueFilter.
     */
//    @Test
//    public void testUniqueEntries() {
//        System.out.println("uniqueEntries");
//        Period p = null;
//        DatedValueFilter instance = null;
//        Collection<Integer> expResult = null;
//        Collection<Integer> result = instance.uniqueEntries(p);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

    /**
     * Test of slidingMean method, of class DatedValueFilter.
     */
    @Test
    public void testSlidingMean() {
        List<DatedValue> dv = new ArrayList<>();
        dv.add(new DatedValue(new Date(100, 0, 0), 1.0)); // day = 0
        dv.add(new DatedValue(new Date(100, 0, 1), 2.0)); // day = 1 mean of 1,2,3 = 6/3 = 2
        dv.add(new DatedValue(new Date(100, 0, 2), 3.0));
        dv.add(new DatedValue(new Date(100, 0, 3), 7.0)); // day = 3 mean of 3,7,23 = 33/3 = 11
        dv.add(new DatedValue(new Date(100, 0, 4), 23.0));
        dv.add(new DatedValue(new Date(100, 0, 5), 33.0)); // day = 5 mean of 23,33,5= 61/3 = 20.333
        dv.add(new DatedValue(new Date(100, 0, 6), 5.0)); // day = 6
 
        DatedValueFilter instance = new DatedValueFilter(dv);
        Map<Integer, Double> result = instance.slidingMean(3);
        assertEquals(5, result.size());
        assertEquals(2.0, result.get(1), 0.001);
        assertEquals(11.0, result.get(3), 0.001);
        assertEquals(20.333, result.get(5), 0.001);
    }

    /**
     * Test of total method, of class DatedValueFilter.
     */
    @Test
    public void testTotal() {
        List<DatedValue> dv = new ArrayList<>();
        dv.add(new DatedValue(new Date(100, 0, 0), 1.0));
        dv.add(new DatedValue(new Date(100, 0, 1), 2.0));
        dv.add(new DatedValue(new Date(100, 0, 2), 3.9));
        DatedValueFilter instance = new DatedValueFilter(dv);
        assertEquals(6.9, instance.total(), 0.001);
    }

    /**
     * Test of uniqueEntries method, of class DatedValueFilter.
     */
//    @Test
//    public void testUniqueEntries() {
//        System.out.println("uniqueEntries");
//        Period p = null;
//        DatedValueFilter instance = null;
//        Collection<Integer> expResult = null;
//        Collection<Integer> result = instance.uniqueEntries(p);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of size method, of class DatedValueFilter.
//     */
//    @Test
//    public void testSize() {
//        System.out.println("size");
//        DatedValueFilter instance = null;
//        int expResult = 0;
//        int result = instance.size();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
  
}
