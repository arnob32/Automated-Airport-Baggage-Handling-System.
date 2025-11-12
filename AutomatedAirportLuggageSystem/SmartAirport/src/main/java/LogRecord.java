package SmartAirport.src.main.java;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LogRecord {

	   private String equipmentId;
	    private String message;
	    private LocalDateTime timestamp;

	    public LogRecord(String equipmentId, String message) {
	        this.equipmentId = equipmentId;
	        this.message = message;
	        this.timestamp = LocalDateTime.now();
	    }

	    public String toString() {
	        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	        return fmt.format(timestamp) + " | ID: " + equipmentId + " | " + message;
	    }
}