package test;





import org.junit.jupiter.api.*;

import SmartAirport.src.main.java.AGV;
import SmartAirport.src.main.java.Baggage;
import SmartAirport.src.main.java.LogService;
import SmartAirport.src.main.java.StorageArea;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for AGV class.
 */
public class AGVTest {

    private AGV agv;
    private StorageArea storage;
    private LogService log;

    @BeforeEach
    void setup() {
        log = new LogService();
        agv = new AGV(1, "AGV-1", log);
        storage = new StorageArea(1, "Main Storage", 10);
    }

    /**
     * 1️⃣ Test if battery level decreases when moving.
     */
    @Test
    void testBatteryReduction() {
        double before = agv.getBatteryLevel();
        agv.moveToDestination("Gate B");
        assertTrue(agv.getBatteryLevel() < before,
                "Battery should reduce after moving.");
    }

    /**
     * 2️⃣ Test loading and unloading baggage.
     */
    @Test
    void testLoadAndUnloadBaggage() {
        Baggage bag = new Baggage(1, "Gate A");
        agv.loadBaggage(bag);
        assertFalse(agv.isAvailable(), "AGV should not be available while carrying baggage.");

        agv.unloadBaggage(storage);
        assertTrue(agv.isAvailable(), "AGV should be available after unloading baggage.");
        assertEquals(1, storage.getStoredCount(), "Storage should contain 1 baggage after unloading.");
    }

    /**
     * 3️⃣ Test that battery does not drop below 0%.
     */
    @Test
    void testBatteryLowerLimit() {
        agv.setBatteryLevel(5);
        agv.moveToDestination("Gate Z");
        assertEquals(0, agv.getBatteryLevel(), "Battery should not drop below 0.");
    }

    /**
     * 4️⃣ Test that battery cannot exceed 100%.
     */
    @Test
    void testBatteryUpperLimit() {
        agv.setBatteryLevel(150);
        assertEquals(100, agv.getBatteryLevel(), "Battery should not exceed 100%.");
    }

    /**
     * 5️⃣ Test showStatus() output contains AGV name and battery info.
     */
    @Test
    void testShowStatusOutput() {
        String status = agv.showStatus();
        assertTrue(status.contains("AGV-1"), "Status should include AGV name.");
        assertTrue(status.contains("%"), "Status should include battery info.");
    }
}