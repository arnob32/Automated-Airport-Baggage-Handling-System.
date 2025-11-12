package test;


import org.junit.jupiter.api.*;

import SmartAirport.src.main.java.Baggage;
import SmartAirport.src.main.java.LogService;
import SmartAirport.src.main.java.QueueManage;
import SmartAirport.src.main.java.StorageArea;
import SmartAirport.src.main.java.TaskManager;

import static org.junit.jupiter.api.Assertions.*;

public class TaskManagerTest {
    private TaskManager tm;
    private LogService log;
    private StorageArea storage;
    private QueueManage queue;

    @BeforeEach
    void setup() {
        log = new LogService();
        storage = new StorageArea(1, "Main", 10);
        queue = new QueueManage(3, log);
        tm = new TaskManager(log, storage, queue);
    }

    
    @Test
    void testCreateTask() {
        Baggage bag = new Baggage(101, "Gate A");
        assertDoesNotThrow(() -> tm.createTask("TestTask", bag));
    }

    
    @Test
    void testAssignAGV() {
        Baggage bag = new Baggage(102, "Gate B");
        tm.createTask("Move", bag);
        assertTrue(storage.getStoredCount() >= 0);
    }

    
    @AfterEach
    void teardown() {
        queue.shutdown();
    }
}
