package SmartAirport.src.main.java;




public class AGV {
    private final int id;
    private final String name;
    private double batteryLevel = 100.0;
    private Baggage carryingBaggage;
    private boolean available = true;
    private final LogService logService;

    public AGV(int id, String name, LogService logService) {
        this.id = id;
        this.name = name;
        this.logService = logService;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public boolean isAvailable() { return available; }

    public void moveToDestination(String destination) {
        logService.writeRecord(name + " moving to " + destination);
        batteryLevel -= 10;
    }

    public void loadBaggage(Baggage baggage) {
        carryingBaggage = baggage;
        available = false;
        logService.writeRecord(name + " loaded baggage " + baggage.getId());
    }

    public void unloadBaggage(StorageArea storage) {
        if (carryingBaggage != null) {
            storage.storeBaggage(carryingBaggage);
            logService.writeRecord(name + " unloaded baggage " + carryingBaggage.getId());
            carryingBaggage = null;
        }
        available = true;
    }

    public void chargeBattery(ChargingStation station) {
        station.chargeAGV(this);
    }

    public String showStatus() {
        return name + " | Battery: " + batteryLevel + "% | Available: " + available;
    }

    public void setBatteryLevel(double level) {
        this.batteryLevel = Math.min(100, level);
    }

	public int getBatteryLevel() {
		// TODO Auto-generated method stub
		return 0;
	}
}