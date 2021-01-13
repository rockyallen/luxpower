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

    /**
     * Test of store method, of class EnergyStore.
     */
    @Test
    public void testStore() {
        EnergyStore instance = new EnergyStore("", "", 10, 10, 1);
        assertEquals(instance.getEnergy(), 0.0, 0.0001);

        double energy = 7.0;
        // enough space
        assertEquals(7.0, instance.store(7.0), 0.0001);
        assertEquals(7.0, instance.getCurrentEnergy(), 0.0001);
        // filled
        assertEquals(3.0, instance.store(7.0), 0.0001);
        assertEquals(10.0, instance.getCurrentEnergy(), 0.0001);
        // over filled
        assertEquals(0.0, instance.store(7.0), 0.0001);
        assertEquals(10.0, instance.getCurrentEnergy(), 0.0001);
    }

    /**
     * Test of demand method, of class EnergyStore.
     */
    @Test
    public void testDemandAndLog() {
        EnergyStore instance = new EnergyStore("", "", 10, 10, 1);

        instance.store(8.0);

        assertEquals(2.0, instance.demand(2.0), 0.0001);
        assertEquals(6.0, instance.getCurrentEnergy(), 0.0001);

        assertEquals(3.0, instance.store(3.0), 0.0001);
        assertEquals(9.0, instance.getCurrentEnergy(), 0.0001);

        assertEquals(11.0, instance.getCharge(), 0.0001);
        assertEquals(2.0, instance.getDischarge(), 0.0001);

        instance.resetLog();
        assertEquals(0.0, instance.getCharge(), 0.0001);
        assertEquals(0.0, instance.getDischarge(), 0.0001);
    }

    @Test
    public void testEfficiency() {
        EnergyStore instance = new EnergyStore("", "", 10, 10, 0.9);

        instance.store(9.0);

        assertEquals(8.1, instance.getCurrentEnergy(), 0.0001);
        assertEquals(9.0, instance.getCharge(), 0.0001);

        assertEquals(3.0, instance.demand(3.0), 0.0001);
        assertEquals(3.0, instance.getDischarge(), 0.0001);
        assertEquals(5.1, instance.getCurrentEnergy(), 0.0001);
    }

    /**
     * Test of getEnergy method, of class EnergyStore.
     */
    @Test
    public void testGetEnergy() {
//        System.out.println("getEnergy");
//        EnergyStore instance = null;
//        double expResult = 0.0;
//        double result = instance.getEnergy();
    }

    /**
     * Test of resetLog method, of class EnergyStore.
     */
    @Test
    public void testResetLog() {
        System.out.println("resetLog");
        EnergyStore instance = null;
    }

    /**
     * Test of getEffectiveCapacity method, of class EnergyStore.
     */
    @Test
    public void testGetEffectiveCapacity() {
//        System.out.println("getEffectiveCapacity");
//        EnergyStore instance = null;
//        double expResult = 0.0;
//        double result = instance.getEffectiveCapacity();
    }

    /**
     * Test of getNominalCapacity method, of class EnergyStore.
     */
    @Test
    public void testGetNominalCapacity() {
//        System.out.println("getNominalCapacity");
//        EnergyStore instance = null;
//        double expResult = 0.0;
//        double result = instance.getNominalCapacity();
    }

    /**
     * Test of name method, of class EnergyStore.
     */
    @Test
    public void testName() {
//        System.out.println("name");
//        EnergyStore instance = null;
//        String expResult = "";
//        String result = instance.name();
    }

    /**
     * Test of getActualCapacity method, of class EnergyStore.
     */
    @Test
    public void testGetActualCapacity() {
//        System.out.println("getActualCapacity");
//        EnergyStore instance = null;
//        double expResult = 0.0;
//        double result = instance.getActualCapacity();
    }

    /**
     * Test of getCurrentEnergy method, of class EnergyStore.
     */
    @Test
    public void testGetCurrentEnergy() {
//        System.out.println("getCurrentEnergy");
//        EnergyStore instance = null;
//        double expResult = 0.0;
//        double result = instance.getCurrentEnergy();
    }

    /**
     * Test of getCharge method, of class EnergyStore.
     */
    @Test
    public void testGetCharge() {
//        System.out.println("getCharge");
//        EnergyStore instance = null;
//        double expResult = 0.0;
//        double result = instance.getCharge();
    }

    /**
     * Test of getDischarge method, of class EnergyStore.
     */
    @Test
    public void testGetDischarge() {
//        System.out.println("getDischarge");
//        EnergyStore instance = null;
//        double expResult = 0.0;
//        double result = instance.getDischarge();
    }

}
