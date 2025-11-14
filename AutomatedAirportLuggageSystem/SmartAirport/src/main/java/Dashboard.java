package SmartAirport.src.main.java;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Dashboard extends JFrame {

    private final JTextArea console = new JTextArea(15, 80);
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
    //private TaskManager taskManager;

    private boolean simulationStarted = false;

    private final ExecutorService agvExecutor = Executors.newFixedThreadPool(5);

    public Dashboard() {

        super("Airport Luggage Handling System");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(Color.DARK_GRAY);

     
        
        JLabel title = new JLabel("Airport Luggage Handling System", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setForeground(Color.WHITE);
        title.setOpaque(true);
        title.setBackground(new Color(52, 73, 94));
        title.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        add(title, BorderLayout.NORTH);


        
        log = new LogService();
        queueManage = new QueueManage(5, log);
        storageArea = new StorageArea(1, "Main Storage", 50);
       // taskManager = new TaskManager(log, storageArea, queueManage);

        // AGVs
        for (int i = 1; i <= 5; i++) {
            agvs.add(new AGV(i, "AGV-" + i, log));
        }
        refreshAGVList();
        
        for (int i = 1; i <= 5; i++) {
            ChargingStation station = new ChargingStation(i, "Station-" + i, log);
            stations.add(station);
            stationListModel.addElement("Station-" + i + " | Available");
        }

        // Baggage
        for (int i = 1; i <= 20; i++) {
            baggageList.add(new Baggage(i, "Gate " + (char) ('A' + (i % 26))));
            luggageListModel.addElement("Baggage-" + i + " → Gate " + (char) ('A' + (i % 26)));
        }


     
        JList<String> agvList = new JList<>(agvListModel);
        agvList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int index = agvList.getSelectedIndex();
                if (index >= 0) selectedAGV = agvs.get(index);
            }
        });

        JList<String> luggageList = new JList<>(luggageListModel);
        luggageList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int index = luggageList.getSelectedIndex();
                if (index >= 0) selectedBaggage = baggageList.get(index);
            }
        });


   
        JPanel centerPanel = new JPanel(new BorderLayout());

        JSplitPane splitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                new JScrollPane(agvList),
                new JScrollPane(luggageList)
        );
        splitPane.setDividerLocation(350);

        centerPanel.add(splitPane, BorderLayout.CENTER);


   
        console.setEditable(false);
        JScrollPane consolePane = new JScrollPane(console);
        consolePane.setPreferredSize(new Dimension(300, 200));

        centerPanel.add(consolePane, BorderLayout.SOUTH);

        add(centerPanel, BorderLayout.CENTER);


      
        JButton startBtn = new JButton("Start Simulation");
        JButton loadMoveBtn = new JButton("Load & Move to Storage");
        JButton chargeBtn = new JButton("Send to Charge");
        JButton selectStationBtn = new JButton("Select Charging Station");
        JButton showLogBtn = new JButton("Show Logs");
        JButton exitBtn = new JButton("Exit");

        // Button styling 
        for (JButton btn : Arrays.asList(startBtn, loadMoveBtn, chargeBtn, selectStationBtn, showLogBtn, exitBtn)) {
            btn.setBackground(new Color(52, 73, 94));
            btn.setForeground(Color.WHITE);
            btn.setFont(new Font("Arial", Font.BOLD,  16));
            btn.setMargin(new Insets(8, 10, 8, 10));
        }

        JPanel controlPanel = new JPanel(new GridLayout(2, 3, 10, 10));
        controlPanel.setBackground(Color.BLACK);
        controlPanel.add(startBtn);
        controlPanel.add(loadMoveBtn);
        controlPanel.add(chargeBtn);
        controlPanel.add(selectStationBtn);
        controlPanel.add(showLogBtn);
        controlPanel.add(exitBtn);

        add(controlPanel, BorderLayout.SOUTH);


 
        startBtn.addActionListener(this::startSimulation);
        loadMoveBtn.addActionListener(e -> runIfStarted(this::loadAndMove));
        chargeBtn.addActionListener(e -> runIfStarted(this::sendToCharge));
        selectStationBtn.addActionListener(e -> runIfStarted(this::selectStation));
        showLogBtn.addActionListener(e -> runIfStarted(this::showLogs));
        exitBtn.addActionListener(e -> System.exit(0));


        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }


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


    private void loadAndMove() {

        if (selectedAGV == null || selectedBaggage == null) {
            console.append("⚠ Select AGV & baggage first.\n");
            return;
        }

        AGV agv = selectedAGV;
        Baggage bag = selectedBaggage;

        if (agv.getBatteryLevel() <= 0) {
            JOptionPane.showMessageDialog(this,
                    agv.getName() + " battery is 0%. Please charge it.");
            return;
        }

        // if  AGV busy
        agv.setAvailable(false);
        refreshAGVList();

        // Remove baggage from list
        baggageList.remove(bag);
        luggageListModel.removeElement("Baggage-" + bag.getId() + " → " + bag.getDestination());

        console.append("🚀 " + agv.getName() + " transporting baggage "
                + bag.getId() + "\n");

        // Run task concurrently
        agvExecutor.submit(() -> {

            agv.loadBaggage(bag);
            simulateBatteryDrain(agv, 5);

            agv.moveToDestination(bag.getDestination());
            simulateBatteryDrain(agv, 5);

            if (agv.getBatteryLevel() <= 0) {
                agv.setAvailable(false);
                console.append("❌ " + agv.getName() + " battery empty.\n");
            } else {
                agv.unloadBaggage(storageArea);
                console.append("✅ " + agv.getName() + " delivered baggage.\n");
                agv.setAvailable(true);
            }

            SwingUtilities.invokeLater(() -> {
                refreshAGVList();
                refreshLuggageList();
            });
        });

        selectedAGV = null;
        selectedBaggage = null;
    }


    private void sendToCharge() {

        if (selectedAGV == null) {
            JOptionPane.showMessageDialog(this, "Select an AGV!");
            return;
        }
        if (selectedStation == null) {
            JOptionPane.showMessageDialog(this, "Select a charging station!");
            return;
        }

        console.append("🔋 " + selectedAGV.getName() + " going to charge...\n");

        new Thread(() -> {
            selectedStation.chargeAGV(selectedAGV);
            selectedAGV.setAvailable(true);

            SwingUtilities.invokeLater(this::refreshAGVList);
        }).start();
    }

    private void selectStation() {
        String s = (String) JOptionPane.showInputDialog(
                this, "Choose station:", "Charging Station",
                JOptionPane.PLAIN_MESSAGE, null,
                stationListModel.toArray(),
                stationListModel.get(0));

        if (s != null)
            selectedStation = stations.get(stationListModel.indexOf(s));
    }


  
    private void showLogs() {
        console.append("Log files stored in /data/logs\n");
    }

    private void refreshAGVList() {
        agvListModel.clear();
        for (AGV a : agvs) {

            String status =
                    (a.getBatteryLevel() <= 0)
                            ? "Needs Charging ⚠"
                            : (a.isAvailable() ? "Free" : "Busy");

            agvListModel.addElement(
                    a.getName() + " | Battery: " + (int) a.getBatteryLevel()
                            + "% | Status: " + status
            );
        }
    }

    private void refreshLuggageList() {
        luggageListModel.clear();
        for (Baggage b : baggageList) {
            luggageListModel.addElement("Baggage-" + b.getId() +
                    " → " + b.getDestination());
        }
    }

    private void simulateBatteryDrain(AGV agv, int steps) {
        for (int i = 0; i < steps; i++) {
            agv.setBatteryLevel(agv.getBatteryLevel() - 1);
            try { Thread.sleep(300); }
            catch (InterruptedException ignored) {}
        }
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(Dashboard::new);
    }
}
