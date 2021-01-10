package solar.model;

import java.util.ArrayList;
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
public class RecordFilterTest {

    public RecordFilterTest() {
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
     * Test of month method, of class RecordFilter.
     */
    @Test
    public void testMonth() {
        List<DateProvider> input = new ArrayList<>();

        Record r1 = makeRecord(new Date(121, 01, 01), 3, 34.5f);
        Record r2 = makeRecord(new Date(121, 03, 04), 4, 0.1f);
        Record r3 = makeRecord(new Date(121, 03, 04), 5, 0.6f);
        Record r4 = makeRecord(new Date(121, 05, 04), 6, 0.5f);
        input.add(r1);
        input.add(r2);
        input.add(r3);
        input.add(r4);

        List<DateProvider> result = null;

        RecordFilter<DateProvider> instance = null;

        instance = new RecordFilter<>(input);
        result = instance.period(9,Period.MONTH).result();
        assertEquals(0, result.size());

        instance = new RecordFilter<>(input);
        result = instance.period(3,Period.MONTH).result();
        assertEquals(2, result.size());
        compareRecord(r2, result.get(0));
        compareRecord(r3, result.get(1));

        instance = new RecordFilter<>(input);
        result = instance.period(1,Period.MONTH).result();
        assertEquals(1, result.size());
        compareRecord(r1, result.get(0));

        instance = new RecordFilter<>(input);
        result = instance.period(5,Period.MONTH).result();
        assertEquals(1, result.size());
        compareRecord(r4, result.get(0));
    }

    /**
     * Test of month method, of class RecordFilter.
     */
    @Test
    public void testChaining() {
        List<DateProvider> input = new ArrayList<>();

        Record r1 = makeRecord(new Date(121, 01, 01), 3, 34.5f);
        Record r2 = makeRecord(new Date(121, 03, 04), 4, 0.1f);
        Record r3 = makeRecord(new Date(121, 03, 06), 5, 0.6f);
        Record r4 = makeRecord(new Date(121, 05, 04), 6, 0.5f);
        input.add(r1);
        input.add(r2);
        input.add(r3);
        input.add(r4);

        List<DateProvider> result = null;

        RecordFilter<DateProvider> instance = null;

        instance = new RecordFilter<>(input);

        result = instance.period(3,Period.MONTH).result();
        assertEquals(2, result.size());
        compareRecord(r2, result.get(0));
        compareRecord(r3, result.get(1));

        result = instance.period(4,Period.HOUR).result();
        assertEquals(1, result.size());
        compareRecord(r2, result.get(0));

        result = instance.period(5,Period.MONTH).result();
        assertEquals(0, result.size());
    }

    @Test
    public void testLastDayRecordsOnlyNoDuplicates() {
        RecordFilter<DateProvider> instance = null;
        List<DateProvider> input = new ArrayList<>();
        input.add(makeRecord(new Date(121, 01, 01), 1, 34.5f));
        input.add(makeRecord(new Date(121, 03, 04), 1, 0.5f));
        input.add(makeRecord(new Date(121, 03, 05), 1, 0.5f));
        instance = new RecordFilter(input);

        List<DateProvider> result = instance.endOfPeriod(Period.DAY).result();
        assertEquals(3, result.size());
    }

    @Test
    public void testLastDayRecordsOnlyMiddleDuplicates() {
        RecordFilter<DateProvider> instance = null;
        List<DateProvider> input = new ArrayList<>();

        input.add(makeRecord(new Date(121, 01, 01), 1, 34.5f));
        input.add(makeRecord(new Date(121, 03, 04), 1, 0.5f));
        input.add(makeRecord(new Date(121, 03, 04), 2, 0.1f));
        input.add(makeRecord(new Date(121, 03, 04), 3, 0.6f));
        input.add(makeRecord(new Date(121, 03, 05), 1, 0.5f));

        instance = new RecordFilter<>(input);

        List<DateProvider> result = instance.endOfPeriod(Period.DAY).result();
        assertEquals(3, result.size());
    }

    @Test
    public void testLastDayRecordsOnlyEndDuplicates() {
        RecordFilter<DateProvider> instance = null;
        List<DateProvider> input = new ArrayList<>();

        // MUST be sorted on input. The Record filter relies on it.
        Record r1 = makeRecord(new Date(121, 01, 01), 3, 34.5f);
        Record r2 = makeRecord(new Date(121, 03, 04), 4, 0.1f);
        Record r3 = makeRecord(new Date(121, 03, 04), 5, 0.6f);
        Record r4 = makeRecord(new Date(121, 03, 04), 6, 0.5f);
        input.add(r1);
        input.add(r2);
        input.add(r3);
        input.add(r4);
        instance = new RecordFilter<>(input);

        // r1, r3
        List<DateProvider> result = instance.endOfPeriod(Period.DAY).result();
        assertEquals(2, result.size());
        compareRecord(r1, result.get(0));
        compareRecord(r4, result.get(1));
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
