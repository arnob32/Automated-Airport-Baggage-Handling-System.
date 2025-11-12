package SmartAirport.src.main.java;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Handles all logging for the Smart Airport System.
 * Creates separate folders for different types of logs under /data/logs.
 */
public class LogService {

    // ✅ Always create log folder inside project root
    private static final String ROOT_PATH = System.getProperty("user.dir") + File.separator + "data" + File.separator + "logs";

    /**
     * Writes a general system log entry.
     * Example: logService.writeRecord("System initialized");
     */
    public synchronized void writeRecord(String message) {
        try {
            String date = LocalDate.now().toString();

            // Create /data/logs/system directory if missing
            File dir = new File(ROOT_PATH + File.separator + "system");
            if (!dir.exists()) dir.mkdirs();

            // File for current day
            File logFile = new File(dir, "system_" + date + ".log");

            // Timestamp
            String timestamp = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                    .format(LocalDateTime.now());

            // Write entry
            try (FileWriter fw = new FileWriter(logFile, true)) {
                fw.write("[" + timestamp + "] " + message + System.lineSeparator());
            }

            // Print path once to help you locate it in Eclipse
            System.out.println("✅ Log written to: " + logFile.getAbsolutePath());

        } catch (IOException e) {
            System.out.println("⚠️ Error writing log: " + e.getMessage());
        }
    }

    /**
     * Overloaded log method for module-specific logs.
     * Example: logService.writeRecord("charging", "station1", "Charging started for AGV-3");
     */
    public synchronized void writeRecord(String folder, String fileName, String message) {
        try {
            String date = LocalDate.now().toString();

            // Create /data/logs/<folder>
            File dir = new File(ROOT_PATH + File.separator + folder);
            if (!dir.exists()) dir.mkdirs();

            // Create a file named e.g., station1_2025-11-13.log
            File logFile = new File(dir, fileName + "_" + date + ".log");

            // Timestamp
            String timestamp = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                    .format(LocalDateTime.now());

            // Write log entry
            try (FileWriter fw = new FileWriter(logFile, true)) {
                fw.write("[" + timestamp + "] " + message + System.lineSeparator());
            }

            // Print full log path
            System.out.println("✅ Log written to: " + logFile.getAbsolutePath());

        } catch (IOException e) {
            System.out.println("⚠️ Error writing " + folder + " log: " + e.getMessage());
        }
    }
}