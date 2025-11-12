package SmartAirport.src.main.java;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.*;
import java.util.List;


import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.*;
import java.util.List;


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

    public Dashboard() {
        super("Airport Smart Luggage – Simulation Dashboard");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // --- Initialize System Components ---
        log = new LogService();
        queueManage = new QueueManage(5, log);
        storageArea = new StorageArea(1, "Main Storage", 50);
        taskManager = new TaskManager(log, storageArea, queueManage);

        // --- Create AGVs and Charging Stations ---
        for (int i = 1; i <= 5; i++) {
            agvs.add(new AGV(i, "AGV-" + i, log));
            agvListModel.addElement("AGV-" + i + " | Battery: 100% | Status: Free");
        }
        for (int i = 1; i <= 5; i++) {
            stations.add(new ChargingStation(i, "Station-" + i, log));
            stationListModel.addElement("Station-" + i + " | Available");
        }

        // --- Create Luggage List ---
        for (int i = 1; i <= 6; i++) {
            baggageList.add(new Baggage(i, "Gate " + (char) ('A' + i)));
            luggageListModel.addElement("Baggage-" + i + " → Gate " + (char) ('A' + i));
        }

        // --- Left Panel (AGV List) ---
        JList<String> agvList = new JList<>(agvListModel);
        agvList.setBorder(new TitledBorder("AGVs"));
        agvList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int index = agvList.getSelectedIndex();
                if (index >= 0) selectedAGV = agvs.get(index);
            }
        });

        // --- Right Panel (Luggage List) ---
        JList<String> luggageList = new JList<>(luggageListModel);
        luggageList.setBorder(new TitledBorder("Available Luggage"));
        luggageList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int index = luggageList.getSelectedIndex();
                if (index >= 0) selectedBaggage = baggageList.get(index);
            }
        });

        // --- Bottom Panel (Buttons) ---
        JPanel controlPanel = new JPanel(new GridLayout(2, 3, 8, 8));
        JButton startBtn = new JButton("Start Simulation");
        JButton loadMoveBtn = new JButton("Load & Move to Storage");
        JButton chargeBtn = new JButton("Send to Charge");
        JButton selectStationBtn = new JButton("Select Station");
        JButton showLogBtn = new JButton("Show Logs");
        JButton exitBtn = new JButton("Exit");

        controlPanel.add(startBtn);
        controlPanel.add(loadMoveBtn);
        controlPanel.add(chargeBtn);
        controlPanel.add(selectStationBtn);
        controlPanel.add(showLogBtn);
        controlPanel.add(exitBtn);

        startBtn.addActionListener(this::startSimulation);
        loadMoveBtn.addActionListener(this::loadAndMove);
        chargeBtn.addActionListener(this::sendToCharge);
        selectStationBtn.addActionListener(this::selectStation);
        showLogBtn.addActionListener(e -> showLogs());
        exitBtn.addActionListener(e -> System.exit(0));

        // --- Console Output ---
        console.setEditable(false);
        JScrollPane consolePane = new JScrollPane(console);
        consolePane.setBorder(new TitledBorder("System Logs"));

        // --- Layout Assembly ---
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                new JScrollPane(agvList), new JScrollPane(luggageList));
        splitPane.setDividerLocation(350);

        add(splitPane, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.SOUTH);
        add(consolePane, BorderLayout.NORTH);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    // --- Button Actions ---

    private void startSimulation(ActionEvent e) {
        console.append("Simulation started!\n");
        log.writeRecord("Simulation started by Professor.");
        refreshAGVList();
    }

    private void loadAndMove(ActionEvent e) {
        if (selectedAGV == null || selectedBaggage == null) {
            console.append("⚠️ Please select an AGV and a baggage first.\n");
            return;
        }
        if (!selectedAGV.isAvailable()) {
            console.append("⚠️ " + selectedAGV.getName() + " is busy.\n");
            return;
        }

        console.append(selectedAGV.getName() + " loading " + selectedBaggage.getId() + "\n");
        selectedAGV.loadBaggage(selectedBaggage);
        selectedAGV.moveToDestination(selectedBaggage.getDestination());
        selectedAGV.unloadBaggage(storageArea);
        console.append(selectedAGV.getName() + " unloaded baggage at Storage Area.\n");
        selectedBaggage.updateStatus("Delivered");
        refreshAGVList();
    }

    private void sendToCharge(ActionEvent e) {
        if (selectedAGV == null) {
            console.append("⚠️ Please select an AGV first.\n");
            return;
        }
        if (selectedStation == null) {
            console.append("⚠️ Please select a Charging Station first.\n");
            return;
        }
        if (selectedAGV.getBatteryLevel() >= 30) {
            console.append("⚠️ Battery above 30%, charging not needed.\n");
            return;
        }
        new Thread(() -> selectedStation.chargeAGV(selectedAGV)).start();
        console.append(selectedAGV.getName() + " sent to " + selectedStation.showStatus() + "\n");
        refreshAGVList();
    }

    private void selectStation(ActionEvent e) {
        String station = (String) JOptionPane.showInputDialog(this,
                "Select a station:",
                "Charging Station",
                JOptionPane.PLAIN_MESSAGE,
                null,
                stationListModel.toArray(),
                stationListModel.get(0));
        if (station != null) {
            int index = stationListModel.indexOf(station);
            selectedStation = stations.get(index);
            console.append("Selected " + station + "\n");
        }
    }

    private void showLogs() {
        console.append("\n--- Recent Logs ---\n");
        log.writeRecord("Professor viewed logs from UI.");
        console.append("Logs are stored in /logs folder.\n");
    }

    private void refreshAGVList() {
        agvListModel.clear();
        for (AGV agv : agvs) {
            agvListModel.addElement(agv.getName() + " | Battery: " +
                    agv.getBatteryLevel() + "% | Status: " + (agv.isAvailable() ? "Free" : "Busy"));
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Dashboard::new);
    }
}