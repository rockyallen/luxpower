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
public class CalculatorTest {

    public CalculatorTest() {
    }

    @Test
    public void testSolarDeclination() {
    }

    @Test
    public void testSolarTime() {
    }

    @Test
    public void testTimeEquation() {
    }

    @Test
    public void testHourAngle() {
    }

    @Test
    public void testSunElevation() {
    }

    @Test
    public void testSolarZenith() {
    }

    @Test
    public void testSunAzimuth() {
    }

    @Test
    public void testHssNhsr() {
    }

    @Test
    public void testSolarIrradiance() {
    }

    @Test
    public void testSunriseOrSunset() {
    }

    @Test
    public void testBeamNormalIrradiance() {
    }

    @Test
    public void testCosI() {
    }

    @Test
    public void testDiffuseIrradiance() {
    }

    @Test
    public void testGroundReflected() {
    }

    @Test
    public void testDayNumber() {
        assertEquals(4, Calculator.dayNumber(0, 4));
        assertEquals(35, Calculator.dayNumber(1, 4));
        assertEquals(31+28+4, Calculator.dayNumber(2, 4));
        assertEquals(365, Calculator.dayNumber(11, 31));
    }

    @Test
    public void testSunrise() {
    }

    @Test
    public void testSunset() {
    }

    @Test
    public void testInsSolarRadiation() {
    }

    @Test
    public void testTotalRad() {
    }

}
