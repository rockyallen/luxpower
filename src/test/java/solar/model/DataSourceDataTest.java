package solar.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author rocky
 */
public class DataSourceDataTest {


    public DataSourceDataTest() {
    }

    @Test
    public void testSerialisation() throws IOException, ClassNotFoundException {

    DataSourceData instance = new DataSourceData();
        Record r1 = makeRecord(new Date(120, 03, 04), 1, 0.1f);
        Record r2 = makeRecord(new Date(121, 01, 01), 1, 34.5f);
        Record r3 = makeRecord(new Date(121, 03, 02), 1, 0.6f);
        Record r4 = makeRecord(new Date(121, 03, 04), 7, 0.5f);
        instance.records.add(r1);
        instance.records.add(r2);
        instance.records.add(r3);
        instance.records.add(r4);


        DataSourceData result1 = (DataSourceData) SerialisationHelper.testRoundTrip(instance);
        // move into a list for easier checking
        List<DateProvider> result = new ArrayList<>();
        result.addAll(result1.records);
        assertEquals(4, result.size());
        compareRecord(r1,result.get(0));
        compareRecord(r2,result.get(1));
        compareRecord(r3,result.get(2));
        compareRecord(r4,result.get(3));
    }

    @Test
    public void testRecordsAreSorted() {

    DataSourceData instance = new DataSourceData();
        Record r1 = makeRecord(new Date(121, 01, 01), 1, 34.5f);
        Record r2 = makeRecord(new Date(121, 03, 04), 7, 0.5f);
        Record r3 = makeRecord(new Date(120, 03, 04), 1, 0.1f);
        Record r4 = makeRecord(new Date(121, 03, 02), 1, 0.6f);
        
        instance.records.add(r1);
        instance.records.add(r2);
        instance.records.add(r3);
        instance.records.add(r4);

        // should be 3, 1, 4, 2
        // move into a list for easier checking
        List<DateProvider> result = new ArrayList<>();
        result.addAll(instance.records);
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
