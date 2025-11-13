package SmartAirport.src.main.java;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AGV {

    private final int id;
    private final String name;
    private double batteryLevel = 100.0;
    private Baggage carryingBaggage;
    private boolean available = true;
    private final LogService logService;
 // Thread pool to run AGVs concurrently
    private ExecutorService agvExecutor = Executors.newFixedThreadPool(5);

    public AGV(int id, String name, LogService logService) {
        this.id = id;
        this.name = name;
        this.logService = logService;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    /**
     * Moves the AGV to a destination, consuming 20% battery per trip.
     * If battery reaches 0%, AGV becomes unavailable.
     */
    public void moveToDestination(String destination) {
        logService.writeRecord("agv", "agv" + id, name + " moving to " + destination);

        // Simulate battery drain
        batteryLevel -= 20.0;
        if (batteryLevel < 0) batteryLevel = 0;

        // Automatically mark as unavailable if battery is drained
        if (batteryLevel <= 0) {
            available = false;
            logService.writeRecord("agv", "agv" + id,
                    name + " battery depleted to 0%. Marked as unavailable.");
        }

        logService.writeRecord("agv", "agv" + id,
                name + " reached " + destination + " | Battery: " + batteryLevel + "%");
    }

    /**
     * Load a baggage onto the AGV.
     */
    public void loadBaggage(Baggage baggage) {
        carryingBaggage = baggage;
        available = false;
        logService.writeRecord("agv", "agv" + id, name + " loaded baggage " + baggage.getId());
    }

    /**
     * Unload baggage into the storage area.
     */
    public void unloadBaggage(StorageArea storage) {
        if (carryingBaggage != null) {
            storage.storeBaggage(carryingBaggage);
            logService.writeRecord("agv", "agv" + id,
                    name + " unloaded baggage " + carryingBaggage.getId() + " into storage.");
            carryingBaggage = null;
        }
        available = true;
    }

    /**
     * Charge the AGV at a given charging station.
     */
    public void chargeBattery(ChargingStation station) {
        logService.writeRecord("agv", "agv" + id,
                name + " sent for charging at " + station.showStatus());
        station.chargeAGV(this);
        available = true;
    }

    /**
     * Returns a summary of AGV status for UI display.
     */
    public String showStatus() {
        String status = available ? "Free" : "Busy";
        if (batteryLevel <= 0) status = "Needs Charging ⚠️";
        return name + " | Battery: " + String.format("%.0f", batteryLevel) + "% | Status: " + status;
    }

    /**
     * Sets battery level safely between 0–100%.
     * If battery reaches 0, AGV becomes unavailable.
     */
    public void setBatteryLevel(double level) {
        this.batteryLevel = Math.max(0, Math.min(100, level));
        if (this.batteryLevel <= 0) {
            this.available = false;
            logService.writeRecord("agv", "agv" + id,
                    name + " battery reached 0%. AGV set to unavailable.");
        } else if (this.batteryLevel == 100 && !available) {
            this.available = true;
            logService.writeRecord("agv", "agv" + id,
                    name + " fully charged. Now available again.");
        }
    }

    /**
     * Returns the current battery level.
     */
    public double getBatteryLevel() {
        return batteryLevel;
    }
}