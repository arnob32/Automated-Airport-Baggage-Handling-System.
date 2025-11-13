package SmartAirport.src.main.java;
import java.io.*;
import java.util.regex.*;


public class RegexSearch {
    public void searchLogs(String pattern) {
        File dir = new File("logs");
        if (!dir.exists()) {
            System.out.println("No logs found.");
            return;
        }

        Pattern p = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
        for (File f : dir.listFiles()) {
            try (BufferedReader br = new BufferedReader(new FileReader(f))) {
                String line;
                while ((line = br.readLine()) != null) {
                    Matcher m = p.matcher(line);
                    if (m.find()) System.out.println("Found in " + f.getName() + ": " + line);
                }
            } catch (IOException e) { e.printStackTrace(); }
        }
    }
    }
