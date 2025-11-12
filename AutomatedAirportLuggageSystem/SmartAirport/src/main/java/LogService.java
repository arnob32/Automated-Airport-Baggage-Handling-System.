package SmartAirport.src.main.java;


import java.io.*;
import java.time.LocalDate;

public class LogService {
    public synchronized void writeRecord(String message) {
        try {
            String date = LocalDate.now().toString();
            File dir = new File("logs");
            if (!dir.exists()) dir.mkdirs();
            File logFile = new File(dir, "system_" + date + ".log");

            try (FileWriter fw = new FileWriter(logFile, true)) {
                fw.write("[" + date + "] " + message + System.lineSeparator());
            }
        } catch (IOException e) {
            System.out.println("Error writing log: " + e.getMessage());
        }
    }
}