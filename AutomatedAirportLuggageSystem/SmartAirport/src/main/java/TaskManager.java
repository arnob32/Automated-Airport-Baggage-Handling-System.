package SmartAirport.src.main.java;



import java.util.*;

public class TaskManager {
    private final List<AGV> agvs = new ArrayList<>();
    private final StorageArea storage;
    private final QueueManage station;
    private final LogService logService;

    public TaskManager(LogService logService, StorageArea storage, QueueManage station) {
        this.logService = logService;
        this.storage = storage;
        this.station = station;
        for (int i = 1; i <= 3; i++) {
            agvs.add(new AGV(i, "AGV-" + i, logService));
        }
    }

    public void createTask(String type, Baggage baggage) {
        logService.writeRecord("Creating new task: " + type + " for baggage " + baggage.getId());
        assignAGV(type, baggage);
    }

    public void assignAGV(String type, Baggage baggage) {
        for (AGV agv : agvs) {
            if (agv.isAvailable()) {
                agv.loadBaggage(baggage);
                agv.moveToDestination(baggage.getDestination());
                agv.unloadBaggage(storage);
                logService.writeRecord("AGV " + agv.getName() + " completed task: " + type);
                return;
            }
        }
        logService.writeRecord("No available AGV for task: " + type);
    }

    public void showSystemStatus() {
        logService.writeRecord("=== SYSTEM STATUS ===");
        for (AGV agv : agvs) {
            logService.writeRecord(agv.showStatus());
        }
        logService.writeRecord("Stored baggage count: " + storage.getStoredCount());
    }
}
