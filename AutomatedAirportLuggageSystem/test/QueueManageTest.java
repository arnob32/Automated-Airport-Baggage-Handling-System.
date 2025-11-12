package test;



import org.junit.jupiter.api.*;

import SmartAirport.src.main.java.AGV;
import SmartAirport.src.main.java.LogService;
import SmartAirport.src.main.java.QueueManage;

import static org.junit.jupiter.api.Assertions.*;

public class QueueManageTest {
    private QueueManage queue;
    private LogService log;

    @BeforeEach
    void setup() {
        log = new LogService();
        queue = new QueueManage(3, log);
    }

    
    @Test
    void testRequestCharge() {
        AGV agv = new AGV(1, "AGV-Test", log);
        assertDoesNotThrow(() -> queue.requestCharge(agv));
    }

    
    @AfterEach
    void tearDown() {
        queue.shutdown();
    }
}

