package solar.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
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
public class DataStoreCacheTest {
    
    public DataStoreCacheTest() {
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
    
    
        @Test
    public void testPutAndGet() throws IOException, ClassNotFoundException {

    DataStoreCache instance = new DataStoreCache();
    
    List<Record> input = new ArrayList<>();
        Record r1 = makeRecord(new Date(120, 03, 04), 1, 0.1f);
        Record r2 = makeRecord(new Date(121, 01, 01), 1, 34.5f);
        Record r3 = makeRecord(new Date(121, 03, 02), 1, 0.6f);
        Record r4 = makeRecord(new Date(121, 03, 04), 7, 0.5f);
        input.add(r1);
        input.add(r2);
        input.add(r3);
        input.add(r4);

        assertTrue(new DataStoreCache().put(input));

        Collection<Record> results = new DataStoreCache().get();
        
        assertNotNull(results);
        
        // move into a list for easier checking
        List<DateProvider> result = new ArrayList<>();
        result.addAll(results);
        assertEquals(4, result.size());
        compareRecord(r1,result.get(0));
        compareRecord(r2,result.get(1));
        compareRecord(r3,result.get(2));
        compareRecord(r4,result.get(3));
    }

    @Test
    public void testRecordsAreSorted() {

    List<Record> input = new ArrayList<>();
        Record r1 = makeRecord(new Date(121, 01, 01), 1, 34.5f);
        Record r2 = makeRecord(new Date(121, 03, 04), 7, 0.5f);
        Record r3 = makeRecord(new Date(120, 03, 04), 1, 0.1f);
        Record r4 = makeRecord(new Date(121, 03, 02), 1, 0.6f);
        
        input.add(r1);
        input.add(r2);
        input.add(r3);
        input.add(r4);


        assertTrue(new DataStoreCache().put(input));

        Collection<Record> results = new DataStoreCache().get();
        
        assertNotNull(results);
        
        // should be 3, 1, 4, 2

        // move into a list for easier checking
        List<Record> result = new ArrayList<>();
        result.addAll(results);
        assertEquals(4, result.size());
        compareRecord(r3,result.get(0));
        compareRecord(r1,result.get(1));
        compareRecord(r4,result.get(2));
        compareRecord(r2,result.get(3));
    }

    private Record makeRecord(Date date, int hour, float f) {
        Record r1 = new Record();
        r1.setDate(date);
        r1.getDate().setHours(hour);
        r1.seteChgDay(f);
        return r1;
    }
    
    private void compareRecord(DateProvider r1, DateProvider r2) {
        assertEquals(r1.getDate(), r2.getDate());
    }


}
