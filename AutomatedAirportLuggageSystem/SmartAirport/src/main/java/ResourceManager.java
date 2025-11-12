package SmartAirport.src.main.java;



import java.io.*;

public class ResourceManager {
    public void clearOldLogs() {
        File logDir = new File("logs");
        if (logDir.exists()) deleteRecursive(logDir);
        System.out.println("Logs cleared successfully.");
    }

    private void deleteRecursive(File file) {
        if (file.isDirectory()) {
            for (File f : file.listFiles()) deleteRecursive(f);
        }
        file.delete();
    }
}
