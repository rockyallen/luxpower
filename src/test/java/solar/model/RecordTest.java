package solar.model;

import java.io.IOException;
import java.sql.Date;
import solar.model.Record;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import tech.units.indriya.quantity.Quantities;
import tech.units.indriya.unit.Units;

/**
 *
 * @author rocky
 */
public class RecordTest {

    public RecordTest() {
    }

    @Test
    public void testToString() {
    }

    @Test
    public void testSerialisation() throws IOException, ClassNotFoundException {
        Record instance = new Record();

        instance.setDate(new Date(0,01,2021));
        instance.seteChgDay(Quantities.getQuantity(34.5f,Units.JOULE));
        Record result = (Record) SerialisationHelper.testRoundTrip(instance);
        assertEquals(34.5, result.geteChgDay().getValue().doubleValue(), 0.0001);
        assertEquals(new Date(0,01,2021), result.getDate());
    }
}
