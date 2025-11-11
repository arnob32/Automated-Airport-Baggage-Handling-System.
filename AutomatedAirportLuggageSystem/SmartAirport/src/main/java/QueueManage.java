package SmartAirport.src.main.java;



import java.util.concurrent.*;
import java.util.*;

public class QueueManage {
    private final ExecutorService chargers;
    private final BlockingQueue<AGV> waitQueue = new LinkedBlockingQueue<>();
    private final LogService logService;
    private final long SIM_MINUTE_MS = 200;
    private volatile boolean running = true;

    public QueueManage(int stations, LogService logService) {
        this.logService = logService;
        this.chargers = Executors.newFixedThreadPool(stations);
        startDispatcher();
    }

    public void requestCharge(AGV agv) {
        waitQueue.offer(agv);
        logService.writeRecord(agv.getName() + " joined charging queue.");
    }

    private void startDispatcher() {
        Thread dispatcher = new Thread(() -> {
            while (running) {
                try {
                    AGV agv = waitQueue.take();
                    chargers.submit(() -> doCharge(agv));
                } catch (InterruptedException ignored) {}
            }
        });
        dispatcher.setDaemon(true);
        dispatcher.start();
    }

    private void doCharge(AGV agv) {
        try {
            logService.writeRecord("Charging START for " + agv.getName());
            Thread.sleep(20 * SIM_MINUTE_MS);
            agv.setBatteryLevel(100);
            logService.writeRecord("Charging END for " + agv.getName());
        } catch (InterruptedException ignored) {}
    }

    public void shutdown() {
        running = false;
        chargers.shutdownNow();
    }
}
