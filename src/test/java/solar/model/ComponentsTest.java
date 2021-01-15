package solar.model;

import java.io.File;
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
public class ComponentsTest {
    
    public ComponentsTest() {
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
     * Test of load method, of class Components.
     */
    @Test
    public void testLoad() throws Exception {
        System.out.println("load");
        File file = new File("components.xls");
        Components instance = new Components();
        assertTrue(instance.load(file));
        assertEquals(4,instance.getArrays().size());
        assertEquals(4,instance.getBatteries().size());
        assertEquals(4,instance.getInverters().size());
    }
}
