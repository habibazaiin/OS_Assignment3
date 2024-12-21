package org.example;

import javax.swing.*;
import java.util.*;

public class PriorityScheduling {

    public static void scheduleProcesses(List<Process> processes, int contextSwitching, TimingGUI gui) {
        // Sort processes by arrival time initially
        processes.sort(Comparator.comparingInt(Process::getArrivalTime)
                .thenComparingInt(Process::getPriority));

        int currentTime = 0;
        int totalWaitingTime = 0;
        int totalTurnaroundTime = 0;

        List<Process> readyQueue = new ArrayList<>(); // Ready queue to store processes that are ready to execute
        List<Process> executedProcesses = new ArrayList<>(); // To track the executed processes

        System.out.println("\nExecution Order:");

        while (!processes.isEmpty() || !readyQueue.isEmpty()) {
            // Add all processes that have arrived by the current time to the ready queue
            Iterator<Process> iterator = processes.iterator();
            while (iterator.hasNext()) {
                Process process = iterator.next();
                if (process.arrivalTime <= currentTime) {
                    readyQueue.add(process);
                    iterator.remove();
                }
            }

            // If no processes are in the ready queue, move time forward to the next arrival
            if (readyQueue.isEmpty()) {
                if (!processes.isEmpty()) {
                    currentTime = processes.get(0).arrivalTime;
                }
                continue;
            }

            // Sort the ready queue first by priority (higher priority first), then by arrival time
            readyQueue.sort(Comparator.comparingInt((Process p) -> p.priority).thenComparingInt(p -> p.arrivalTime));

            // Select the process with the highest priority (earliest arrival in case of a tie)
            Process currentProcess = readyQueue.remove(0);

            // Calculate waiting time and turnaround time
            currentProcess.waitingTime = currentTime - currentProcess.arrivalTime;
            currentProcess.turnaroundTime = currentProcess.waitingTime + currentProcess.burstTime;

            // Update total waiting time and turnaround time
            totalWaitingTime += currentProcess.waitingTime;
            totalTurnaroundTime += currentProcess.turnaroundTime;

            // Store the current process's starting time (for GUI)
            int processStartTime = currentTime;

            // Increment the current time by burst time + context switching time
            int executionTime = currentProcess.burstTime;
            currentTime += executionTime + contextSwitching;

            // Print the name of the process executed
            System.out.println(currentProcess.name + " (Priority: " + currentProcess.priority + ")");

            // Update the GUI: Gantt chart and details area
            gui.updateGanttChart(processStartTime, currentProcess, executionTime); // Pass start time to the GUI
            gui.updateDetails(String.format("Process: %s | Waiting Time: %d | Turnaround Time: %d",
                    currentProcess.name, currentProcess.waitingTime, currentProcess.turnaroundTime));

            // Add the executed process to the executedProcesses list
            executedProcesses.add(currentProcess);
        }

        // Output the details of all processes (from executedProcesses, not the original list)
        System.out.println("\nProcess Details:");
        System.out.printf("%-10s %-10s %-10s %-10s %-10s %-10s\n", "Name", "Arrival", "Burst", "Waiting", "Turnaround", "Priority");
        for (Process process : executedProcesses) {
            System.out.println(process);
        }

        // Calculate average waiting time and turnaround time based on the executed processes
        double avgWaitingTime = (double) totalWaitingTime / executedProcesses.size();
        double avgTurnaroundTime = (double) totalTurnaroundTime / executedProcesses.size();

        System.out.println("\nAverage Waiting Time: " + avgWaitingTime);
        System.out.println("Average Turnaround Time: " + avgTurnaroundTime);

        // Update the GUI with final statistics
        gui.displayStatistics(executedProcesses);
    }

}
