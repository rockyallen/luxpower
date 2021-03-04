package solar.model;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static solar.model.SolarUnitsAndConstants.*;
import static tech.units.indriya.quantity.Quantities.*;
import static tech.units.indriya.unit.Units.*;

/**
 *
 * @author rocky
 */
public class EnergyStoreTest {

    public EnergyStoreTest() {
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
    public void testStore() {
        EnergyStore instance = new EnergyStore("vvv", "",
                getQuantity(10.0, KILO_WATT_HOUR),
                getQuantity(10.0, KILO_WATT_HOUR),
                getQuantity(100, PERCENT),
                getQuantity(1.0, KILO_WATT),
                getQuantity(1.0, KILO_WATT));

        assertEquals(ZERO_ENERGY, instance.getEnergy());

        // enough space
        instance.store(getQuantity(700, WATT), getQuantity(10, HOUR));
        assertEquals(getQuantity(7000, WATT_HOUR), instance.getEnergy().to(WATT_HOUR));
        // filled
        instance.store(getQuantity(700, WATT), getQuantity(10, HOUR));
        assertEquals(getQuantity(10.0, KILO_WATT_HOUR), instance.getEnergy());
        // over filled
        instance.store(getQuantity(700, WATT), getQuantity(10, HOUR));
        assertEquals(getQuantity(10.0, KILO_WATT_HOUR), instance.getEnergy());
    }

    @Test
    public void testDemandAndLog() {
        EnergyStore instance = new EnergyStore("vvv", "",
                getQuantity(10.0, KILO_WATT_HOUR),
                getQuantity(10.0, KILO_WATT_HOUR),
                getQuantity(100, PERCENT),
                getQuantity(10.0, KILO_WATT),
                getQuantity(10.0, KILO_WATT));

        instance.store(getQuantity(8, KILO_WATT), getQuantity(1, HOUR));
        instance.demand(getQuantity(2, KILO_WATT), getQuantity(1, HOUR));

        assertEquals(getQuantity(6, KILO_WATT_HOUR), instance.getEnergy().to(KILO_WATT_HOUR));

        instance.store(getQuantity(3, KILO_WATT), getQuantity(1, HOUR));
        assertEquals(getQuantity(9, KILO_WATT_HOUR), instance.getEnergy().to(KILO_WATT_HOUR));

        assertEquals(getQuantity(11, KILO_WATT_HOUR), instance.getCharge().to(KILO_WATT_HOUR));
        assertEquals(getQuantity(2, KILO_WATT_HOUR), instance.getDischarge().to(KILO_WATT_HOUR));

        instance.resetLog();
        assertEquals(ZERO_ENERGY, instance.getCharge().to(JOULE));
        assertEquals(ZERO_ENERGY, instance.getDischarge().to(JOULE));
    }

    @Test
    public void testEfficiency() {

        EnergyStore instance = new EnergyStore("vvv", "",
                getQuantity(10.0, KILO_WATT_HOUR),
                getQuantity(10.0, KILO_WATT_HOUR),
                getQuantity(90, PERCENT),
                getQuantity(10.0, KILO_WATT),
                getQuantity(10.0, KILO_WATT));

        instance.store(getQuantity(9, KILO_WATT), getQuantity(1, HOUR));

        assertEquals(getQuantity(8.1, KILO_WATT_HOUR), instance.getEnergy().to(KILO_WATT_HOUR));
        assertEquals(getQuantity(9, KILO_WATT_HOUR), instance.getCharge());

        assertEquals(getQuantity(3, KILO_WATT), instance.demand(getQuantity(3, KILO_WATT), getQuantity(1, HOUR)));
        assertEquals(getQuantity(3, KILO_WATT_HOUR), instance.getDischarge());
        assertEquals(getQuantity(5.1, KILO_WATT_HOUR), instance.getCurrentEnergy());
    }

    @Test
    public void testChargeLimiting() {

        EnergyStore instance = new EnergyStore("vvv", "",
                getQuantity(10.0, KILO_WATT_HOUR),
                getQuantity(10.0, KILO_WATT_HOUR),
                getQuantity(90, PERCENT),
                getQuantity(1.0, KILO_WATT),
                getQuantity(10.0, KILO_WATT));

        // limits to 1 kW for 2 h
        assertEquals(getQuantity(1.0, KILO_WATT), instance.store(getQuantity(3, KILO_WATT), getQuantity(2, HOUR)).to(KILO_WATT));
        // 2 kw becomes 1.8 allowing for efficiency
        assertEquals(getQuantity(1.8, KILO_WATT_HOUR), instance.getEnergy().to(KILO_WATT_HOUR));
    }

    @Test
    public void testDishargeLimiting() {

        EnergyStore instance = new EnergyStore("vvv", "",
                getQuantity(10.0, KILO_WATT_HOUR),
                getQuantity(10.0, KILO_WATT_HOUR),
                getQuantity(90, PERCENT),
                getQuantity(10.0, KILO_WATT),
                getQuantity(1.0, KILO_WATT));

        instance.setCharge(0.5);
        // limits to 1 kW for 2 h
        assertEquals(getQuantity(1.0, KILO_WATT), instance.demand(getQuantity(3, KILO_WATT), getQuantity(2, HOUR)));
        assertEquals(getQuantity(3, KILO_WATT_HOUR), instance.getEnergy().to(KILO_WATT_HOUR));
    }
}
