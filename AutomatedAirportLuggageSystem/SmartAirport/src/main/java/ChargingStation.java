package SmartAirport.src.main.java;

import javax.swing.*;

/**
 * ChargingStation class simulates an individual charging station.
 * Only one AGV can charge at a time.
 * If another AGV tries to charge while busy, a warning message is shown.
 */
public class ChargingStation {
    private final int id;
    private final String location;
    private boolean isAvailable = true;
    private final LogService logService;

    public ChargingStation(int id, String location, LogService logService) {
        this.id = id;
        this.location = location;
        this.logService = logService;
    }

    /**
     * Charge the given AGV if this station is available.
     * Otherwise, display a warning dialog.
     */
    public synchronized void chargeAGV(AGV agv) {
        if (!isAvailable) {
            // 🚫 Station busy — show message box
            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(
                    null,
                    "⚠️ Charging Station " + id + " (" + location + ") is currently busy.\nPlease select another station.",
                    "Station Busy",
                    JOptionPane.WARNING_MESSAGE
            ));
            logService.writeRecord("charging", "station" + id,
                    "Attempted to charge " + agv.getName() + " but station was busy.");
            return;
        }

        // ✅ Mark station as busy
        isAvailable = false;
        logService.writeRecord("charging", "station" + id,
                "Charging started for " + agv.getName() + " at " + location);

        try {
            // Simulate gradual charging
            for (int i = 0; i < 10; i++) {
                Thread.sleep(5000); // each step = 0.5 sec
                double newBattery = agv.getBatteryLevel() + 10;
                agv.setBatteryLevel(Math.min(100, newBattery));

                // Update UI battery level while charging
                SwingUtilities.invokeLater(() ->
                        System.out.println(agv.getName() + " charging... " + agv.getBatteryLevel() + "%"));
            }

            // Full charge complete
            agv.setBatteryLevel(100);
            logService.writeRecord("charging", "station" + id,
                    agv.getName() + " fully charged at " + location);
            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(
                    null,
                    agv.getName() + " fully charged at " + location,
                    "Charging Complete",
                    JOptionPane.INFORMATION_MESSAGE
            ));

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logService.writeRecord("charging", "station" + id, "Charging interrupted for " + agv.getName());
        } finally {
            // ✅ Mark available again
            isAvailable = true;
            logService.writeRecord("charging", "station" + id,
                    "Charging station " + id + " now available.");
        }
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public String showStatus() {
        return "Station " + id + " | Location: " + location + " | Status: " + (isAvailable ? "Available" : "Busy");
    }
}