package org.example;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.*;

public class TimingGUI {
    private final JFrame frame;
    private final JPanel chartPanel;
    private final JTextArea detailsArea;
    private final Map<Integer, JPanel> processLines;
    private final Map<Integer, Integer> trackProcesses = new HashMap<>();

    public TimingGUI(int numberOfProcesses) {
        frame = new JFrame("Scheduling Algorithm Visualization");
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        chartPanel = new JPanel();
        chartPanel.setLayout(new GridLayout(numberOfProcesses, 1, 5, 5));
        chartPanel.setPreferredSize(new Dimension(800, numberOfProcesses * 50));
        chartPanel.setBackground(Color.WHITE);
        frame.add(new JScrollPane(chartPanel), BorderLayout.NORTH);

        detailsArea = new JTextArea();
        detailsArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(detailsArea);
        frame.add(scrollPane, BorderLayout.CENTER);

        processLines = new HashMap<>();

        for (int i = 1; i <= numberOfProcesses; i++) {
            JPanel processLine = new JPanel();
            processLine.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
            processLine.setBackground(Color.WHITE);

            JLabel nameLabel = new JLabel("P" + i);
            nameLabel.setOpaque(true);
            nameLabel.setBackground(Color.LIGHT_GRAY);
            nameLabel.setForeground(Color.BLACK);
            nameLabel.setPreferredSize(new Dimension(50, 30));

            processLine.add(nameLabel);
            chartPanel.add(processLine);

            processLines.put(i, processLine);
        }

        frame.setVisible(true);
    }

    public void updateGanttChart(int timeBefore, Process p, int execTime) {
        int pNumber = Integer.parseInt(p.name.substring(1));
        JPanel processLine = processLines.get(pNumber);
        int x = 0;

        if (processLine != null) {
            if (timeBefore > 0) {
                JLabel whiteSpace = new JLabel();
                whiteSpace.setOpaque(true);
                whiteSpace.setBackground(Color.WHITE);
                x = timeBefore * 10;
                if (trackProcesses.containsKey(pNumber)) {
                    x -= trackProcesses.get(pNumber);
                }
                whiteSpace.setPreferredSize(new Dimension(x, 30));
                processLine.add(whiteSpace);
            }

            JLabel processBlock = new JLabel();
            processBlock.setOpaque(true);

            try {
                processBlock.setBackground(Color.decode(p.color));
            } catch (NumberFormatException e) {
                processBlock.setBackground(Color.GRAY);
            }

            processBlock.setPreferredSize(new Dimension(execTime * 10, 30));
            processLine.add(processBlock);

            trackProcesses.put(pNumber, trackProcesses.getOrDefault(pNumber, 0) + x + execTime * 10);

            processLine.revalidate();
            processLine.repaint();
        }
    }

    public void updateDetails(String details) {
        detailsArea.append(details + "\n");
    }

    public void displayStatistics(List<Process> processes) {
        int totalWaitingTime = 0;
        int totalTurnaroundTime = 0;

        detailsArea.append("\nProcess Details (Waiting Time and Turnaround Time):\n");
        detailsArea.append(String.format("%-10s %-10s %-10s %-10s %-10s %-10s\n", "Name", "Arrival", "Burst", "Waiting", "Turnaround", "Priority"));

        for (Process process : processes) {
            totalWaitingTime += process.waitingTime;
            totalTurnaroundTime += process.turnaroundTime;
            detailsArea.append(String.format("%-10s %-10d %-10d %-10d %-10d %-10d\n",
                    process.name, process.arrivalTime, process.burstTime, process.waitingTime, process.turnaroundTime, process.priority));
        }

        int numProcesses = processes.size();
        double avgWaitingTime = (double) totalWaitingTime / numProcesses;
        double avgTurnaroundTime = (double) totalTurnaroundTime / numProcesses;

        detailsArea.append("\nAverage Waiting Time: " + avgWaitingTime);
        detailsArea.append("\nAverage Turnaround Time: " + avgTurnaroundTime + "\n");
    }
}
