package test;

import de.tum.i13.aqi.AQICalc;
import de.tum.i13.aqi.EPAEntry;
import de.tum.i13.query.P;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AQICalcTests {

    private AQICalc aqiCalc;

    @BeforeEach
    public void setup() {
        this.aqiCalc = new AQICalc();
    }

    @Test
    public void testAQIP1Good() {
        EPAEntry entry = aqiCalc.getEPAEntry(6.0, P.P1);
        assertEquals("good", entry.getDescription());
    }

    @Test
    public void testAQIP1Moderate() {
        EPAEntry entry = aqiCalc.getEPAEntry(55.0, P.P1);
        assertEquals("moderate", entry.getDescription());
    }

    @Test
    public void testAQIMiddleUpRounding() {
        EPAEntry entry = aqiCalc.getEPAEntry(54.6, P.P1);
        assertEquals("moderate", entry.getDescription());
    }

    @Test
    public void testAQIMiddleDownRounding() {
        EPAEntry entry = aqiCalc.getEPAEntry(54.4, P.P1);
        assertEquals("good", entry.getDescription());
    }


    @Test
    public void testAQIQ25MiddleUpRounding() {
        EPAEntry entry = aqiCalc.getEPAEntry(12.0500001, P.P25);
        assertEquals("moderate", entry.getDescription());
    }

    @Test
    public void testAQIQ25MiddleDownRounding() {
        EPAEntry entry = aqiCalc.getEPAEntry(12.049, P.P25);
        assertEquals("good", entry.getDescription());
    }

}
