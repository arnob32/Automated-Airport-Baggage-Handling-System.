package SmartAirport.src.main.java;

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

    public void chargeAGV(AGV agv) {
        isAvailable = false;
        logService.writeRecord("Charging started for " + agv.getName());
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignored) {}
        agv.setBatteryLevel(100);
        isAvailable = true;
        logService.writeRecord("Charging completed for " + agv.getName());
    }

    public boolean isAvailable() { return isAvailable; }
    public String showStatus() { return "Station " + id + " | Available: " + isAvailable; }
}
