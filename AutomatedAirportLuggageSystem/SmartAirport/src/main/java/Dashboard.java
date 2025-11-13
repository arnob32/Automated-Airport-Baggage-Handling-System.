package SmartAirport.src.main.java;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Dashboard extends JFrame {

    private final JTextArea console = new JTextArea(10, 80);
    private final DefaultListModel<String> agvListModel = new DefaultListModel<>();
    private final DefaultListModel<String> luggageListModel = new DefaultListModel<>();
    private final DefaultListModel<String> stationListModel = new DefaultListModel<>();

    private final List<AGV> agvs = new ArrayList<>();
    private final List<Baggage> baggageList = new ArrayList<>();
    private final List<ChargingStation> stations = new ArrayList<>();

    private AGV selectedAGV;
    private Baggage selectedBaggage;
    private ChargingStation selectedStation;

    private LogService log;
    private QueueManage queueManage;
    private StorageArea storageArea;
    private TaskManager taskManager;

    private boolean simulationStarted = false;

    // Thread pool for AGV concurrency
    private final ExecutorService agvExecutor = Executors.newFixedThreadPool(5);

    public Dashboard() {
        super("Airport Smart Luggage – Simulation Dashboard");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // --- System Components ---
        log = new LogService();
        queueManage = new QueueManage(5, log);
        storageArea = new StorageArea(1, "Main Storage", 50);
        taskManager = new TaskManager(log, storageArea, queueManage);

        // --- AGVs ---
        for (int i = 1; i <= 5; i++) {
            agvs.add(new AGV(i, "AGV-" + i, log));
            agvListModel.addElement("AGV-" + i + " | Battery: 100% | Status: Free");
        }

        // --- Charging Stations ---
        for (int i = 1; i <= 5; i++) {
            stations.add(new ChargingStation(i, "Station-" + i, log));
            stationListModel.addElement("Station-" + i + " | Available");
        }

        // --- Baggage List ---
        for (int i = 1; i <= 6; i++) {
            baggageList.add(new Baggage(i, "Gate " + (char) ('A' + i)));
            luggageListModel.addElement("Baggage-" + i + " → Gate " + (char) ('A' + i));
        }

        // --- AGV List UI ---
        JList<String> agvList = new JList<>(agvListModel);
        agvList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int index = agvList.getSelectedIndex();
                if (index >= 0) selectedAGV = agvs.get(index);
            }
        });

        // --- Luggage List UI ---
        JList<String> luggageList = new JList<>(luggageListModel);
        luggageList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int index = luggageList.getSelectedIndex();
                if (index >= 0) selectedBaggage = baggageList.get(index);
            }
        });

        // --- Buttons ---
        JButton startBtn = new JButton("Start Simulation");
        JButton loadMoveBtn = new JButton("Load & Move to Storage");
        JButton chargeBtn = new JButton("Send to Charge");
        JButton selectStationBtn = new JButton("Select Charging Station");
        JButton showLogBtn = new JButton("Show Logs");
        JButton exitBtn = new JButton("Exit");

        JPanel controlPanel = new JPanel(new GridLayout(2, 3, 8, 8));
        controlPanel.add(startBtn);
        controlPanel.add(loadMoveBtn);
        controlPanel.add(chargeBtn);
        controlPanel.add(selectStationBtn);
        controlPanel.add(showLogBtn);
        controlPanel.add(exitBtn);

        // --- Console ---
        console.setEditable(false);
        JScrollPane consolePane = new JScrollPane(console);

        // --- Layout ---
        add(new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                new JScrollPane(agvList), new JScrollPane(luggageList)), BorderLayout.CENTER);

        add(controlPanel, BorderLayout.SOUTH);
        add(consolePane, BorderLayout.NORTH);

        // --- Button Listeners ---
        startBtn.addActionListener(this::startSimulation);
        loadMoveBtn.addActionListener(e -> runIfStarted(() -> loadAndMove()));
        chargeBtn.addActionListener(e -> runIfStarted(() -> sendToCharge()));
        selectStationBtn.addActionListener(e -> runIfStarted(() -> selectStation()));
        showLogBtn.addActionListener(e -> runIfStarted(this::showLogs));
        exitBtn.addActionListener(e -> System.exit(0));

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    // ========================================================================
    // Simulation Controls
    // ========================================================================

    private void startSimulation(ActionEvent e) {
        simulationStarted = true;
        console.append("Simulation Started!\n");
    }

    private void runIfStarted(Runnable action) {
        if (!simulationStarted) {
            JOptionPane.showMessageDialog(this, "Start the simulation first!");
            return;
        }
        action.run();
    }

    // ========================================================================
    // AGV Load & Move
    // ========================================================================

    private void loadAndMove() {
        if (selectedAGV == null || selectedBaggage == null) {
            console.append("⚠ Select AGV & baggage first.\n");
            return;
        }

        AGV agv = selectedAGV;
        Baggage bag = selectedBaggage;

        // Battery check
        if (agv.getBatteryLevel() <= 0) {
            JOptionPane.showMessageDialog(this,
                    agv.getName() + " battery is 0%. Please charge it.");
            return;
        }

        // Remove baggage
        baggageList.remove(bag);
        luggageListModel.removeElement("Baggage-" + bag.getId() + " → " + bag.getDestination());

        console.append("🚀 " + agv.getName() + " is transporting baggage " + bag.getId() + "\n");

        // CONCURRENT AGV TASK
        agvExecutor.submit(() -> {

            agv.loadBaggage(bag);
            simulateBatteryDrain(agv, 5);

            agv.moveToDestination(bag.getDestination());
            simulateBatteryDrain(agv, 5);

            if (agv.getBatteryLevel() <= 0) {
                agv.setAvailable(false);
                console.append("❌ " + agv.getName() + " ran out of battery!\n");
            } else {
                agv.unloadBaggage(storageArea);
                console.append("✅ " + agv.getName() + " delivered baggage.\n");
            }

            SwingUtilities.invokeLater(() -> {
                refreshAGVList();
                refreshLuggageList();
            });

        });

        selectedAGV = null;
        selectedBaggage = null;
    }

    // ========================================================================
    // Charging System
    // ========================================================================

    private void sendToCharge() {
        if (selectedAGV == null) {
            JOptionPane.showMessageDialog(this, "Select an AGV first!");
            return;
        }
        if (selectedStation == null) {
            JOptionPane.showMessageDialog(this, "Select a charging station!");
            return;
        }

        // Check if station busy
        if (!selectedStation.isAvailable()) {
            JOptionPane.showMessageDialog(this, "Station busy. Choose another.");
            return;
        }

        AGV agv = selectedAGV;

        console.append("🔋 " + agv.getName() + " going to charge...\n");

        new Thread(() -> {
            selectedStation.chargeAGV(agv);
            SwingUtilities.invokeLater(this::refreshAGVList);
        }).start();
    }

    private void selectStation() {
        String s = (String) JOptionPane.showInputDialog(this,
                "Choose station:", "Charging Station",
                JOptionPane.PLAIN_MESSAGE, null,
                stationListModel.toArray(),
                stationListModel.get(0));

        if (s != null) {
            selectedStation = stations.get(stationListModel.indexOf(s));
        }
    }

    // ========================================================================
    // Helper Methods
    // ========================================================================

    private void showLogs() {
        console.append("Log files stored in /data/logs\n");
    }

    private void refreshAGVList() {
        agvListModel.clear();
        for (AGV a : agvs) {
            String status = a.getBatteryLevel() <= 0 ? "Needs Charging ⚠" :
                    (a.isAvailable() ? "Free" : "Busy");

            agvListModel.addElement(a.getName() + " | Battery: " +
                    (int) a.getBatteryLevel() + "% | Status: " + status);
        }
    }

    private void refreshLuggageList() {
        luggageListModel.clear();
        for (Baggage b : baggageList) {
            luggageListModel.addElement("Baggage-" + b.getId() + " → " + b.getDestination());
        }
    }

    private void simulateBatteryDrain(AGV agv, int drainAmount) {
        for (int i = 0; i < drainAmount; i++) {
            agv.setBatteryLevel(agv.getBatteryLevel() - 1);
            try {
                Thread.sleep(300);
            } catch (InterruptedException ignored) {}
        }
    }

    // ========================================================================

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Dashboard::new);
    }
}
