package org.example;

import java.util.*;

public class SJFWithAging {

    public static void scheduleProcesses(List<Process> processes, int agingInterval, TimingGUI gui) {
        // Sort processes by their arrival time initially
        processes.sort(Comparator.comparingInt(Process::getArrivalTime));

        int currentTime = 0;
        int totalWaitingTime = 0;
        int totalTurnaroundTime = 0;

        List<Process> readyQueue = new ArrayList<>();
        List<Process> executedProcesses = new ArrayList<>();
        int[] waitingTimeTracker = new int[processes.size()];

        Process currentProcess = null;

        while (!processes.isEmpty() || !readyQueue.isEmpty() || currentProcess != null) {
            // Add processes that have arrived by the current time to the ready queue
            Iterator<Process> iterator = processes.iterator();
            while (iterator.hasNext()) {
                Process process = iterator.next();
                if (process.arrivalTime <= currentTime) {
                    readyQueue.add(process);
                    iterator.remove();
                }
            }

            // If no process is running and the queue is empty, move forward to the next arrival time
            if (currentProcess == null && readyQueue.isEmpty()) {
                if (!processes.isEmpty()) {
                    currentTime = processes.get(0).arrivalTime;
                }
                continue;
            }

            // Apply aging: Decrease burst time for processes that have been waiting too long
            for (int i = 0; i < readyQueue.size(); i++) {
                Process p = readyQueue.get(i);
                waitingTimeTracker[i]++;
                if (waitingTimeTracker[i] >= agingInterval) {
                    // Apply aging (decrease burst time, but not below 1)
                    p.setRemainingBurstTime(Math.max(1, p.getRemainingBurstTime() - 1));
                    waitingTimeTracker[i] = 0;
                    p.setCountaging(p.getCountaging() + 1);
                    System.out.println("Aging applied to " + p.name + ". New Remaining Burst Time: " + p.remainingBurstTime);
                    gui.updateDetails("Aging applied to " + p.name + ". New Remaining Burst Time: " + p.remainingBurstTime);
                }
            }

            // Sort ready queue by remaining burst time (SRTF principle)
            readyQueue.sort(Comparator.comparingInt(Process::getRemainingBurstTime));

            // If there's no current process, pick the one with the shortest remaining burst time
            if (currentProcess == null && !readyQueue.isEmpty()) {
                currentProcess = readyQueue.remove(0); // Get the process with the shortest remaining burst time
                currentProcess.setRemainingBurstTime(currentProcess.getRemainingBurstTime() + currentProcess.getCountaging());
                currentProcess.resetcountaging();
                // Update the Gantt chart for the execution of the current process
                gui.updateGanttChart(currentTime, currentProcess, currentProcess.getBurstTime());
                System.out.println(currentProcess.name + " (Started, Remaining Burst Time: " + currentProcess.remainingBurstTime + ")");
                gui.updateDetails(currentProcess.name + " (Started, Remaining Burst Time: " + currentProcess.remainingBurstTime + ")");
            }

            // Execute one unit of time for the current process
            if (currentProcess != null) {

                // Execute one unit of time for the current process
                currentProcess.remainingBurstTime--;
                System.out.println(currentProcess.name + " (Remaining Burst Time: " + currentProcess.remainingBurstTime + ")");
                gui.updateDetails(currentProcess.name + " (Remaining Burst Time: " + currentProcess.remainingBurstTime + ")");

                currentTime++; // Move time forward by 1 unit
            }

            // If the current process is finished, update its turnaround and waiting times
            if (currentProcess != null && currentProcess.remainingBurstTime == 0) {
                int finishTime = currentTime;  // Finish time is the current time

                // Turnaround Time = Finish Time - Arrival Time
                currentProcess.setTurnaroundTime(finishTime - currentProcess.arrivalTime);

                // Waiting Time = Turnaround Time - Burst Time
                currentProcess.setWaitingTime(currentProcess.getTurnaroundTime() - currentProcess.getBurstTime());

                totalWaitingTime += currentProcess.getWaitingTime();
                totalTurnaroundTime += currentProcess.getTurnaroundTime();


                executedProcesses.add(currentProcess);
                currentProcess = null;  // Process is done, so set current process to null
            }
        }

        // Output process details to terminal
        System.out.println("\nProcess Details:");
        System.out.printf("%-10s %-10s %-10s %-10s %-10s %-10s\n", "Name", "Arrival", "Burst", "Waiting", "Turnaround", "Priority");
        for (Process process : executedProcesses) {
            System.out.println(process);
            gui.updateDetails(process.toString());
        }

        // Calculate and print average waiting time and turnaround time to terminal
        if (executedProcesses.size() > 0) {
            double avgWaitingTime = (double) totalWaitingTime / executedProcesses.size();
            double avgTurnaroundTime = (double) totalTurnaroundTime / executedProcesses.size();

            System.out.println("\nAverage Waiting Time: " + avgWaitingTime);
            System.out.println("Average Turnaround Time: " + avgTurnaroundTime);
            gui.updateDetails("Average Waiting Time: " + avgWaitingTime);
            gui.updateDetails("Average Turnaround Time: " + avgTurnaroundTime);
        } else {
            System.out.println("No processes executed.");
            gui.updateDetails("No processes executed.");
        }
    }
}
