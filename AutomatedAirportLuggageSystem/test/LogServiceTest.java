
package test;

import org.junit.jupiter.api.*;
import SmartAirport.src.main.java.LogService;

import java.io.File;
import static org.junit.jupiter.api.Assertions.*;

public class LogServiceTest {
    private LogService log;

    @BeforeEach
    void setup() {
        log = new LogService();
    }

    @Test
    void testWriteRecordCreatesFile() {
        log.writeRecord("JUnit Test Log");

        // ✅ Check for correct folder (data/logs/system)
        File dir = new File(System.getProperty("user.dir") + "/data/logs/system");
        assertTrue(dir.exists(), "Log directory should exist after writing a record");

        // ✅ Check that at least one .log file exists
        File[] files = dir.listFiles((d, name) -> name.endsWith(".log"));
        assertNotNull(files, "Log files array should not be null");
        assertTrue(files.length > 0, "At least one log file should be created");
    }
}