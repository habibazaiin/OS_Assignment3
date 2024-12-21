package org.example;

import java.util.*;

import static java.lang.Math.min;

public class SRTFWithAging {
    static int cnt = 0;

    public static void scheduleProcesses(List<Process> processes, int contextSwitching, int agingInterval, TimingGUI gui) {
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
                cnt++;
                currentProcess = readyQueue.remove(0);// Get the process with the shortest remaining burst time
                currentProcess.setRemainingBurstTime(currentProcess.getRemainingBurstTime() + currentProcess.getCountaging());
                currentProcess.resetcountaging();
                if (cnt > 1) {
                    currentTime += contextSwitching;  // Account for context switching time
                }
                if (!readyQueue.isEmpty()) {
                    Process p = readyQueue.get(0);
                    int arr = p.arrivalTime;
                    if (arr < (currentTime + currentProcess.getRemainingBurstTime()) && (p.getRemainingBurstTime() < currentProcess.getRemainingBurstTime())) {
                        //Update the Gantt chart with the preemption
                        gui.updateGanttChart(currentTime, currentProcess, arr);
                    }
                }
                else {
                    //Update the Gantt chart with the preemption
                    gui.updateGanttChart(currentTime, currentProcess, currentProcess.getRemainingBurstTime());
                }
                System.out.println(currentProcess.name + " (Started, Remaining Burst Time: " + currentProcess.remainingBurstTime + ")");
                gui.updateDetails(currentProcess.name + " (Started, Remaining Burst Time: " + currentProcess.remainingBurstTime + ")");

            }

            // Check if we need to preempt the current process
            if (currentProcess != null) {
                // Compare the current process with the process having the shortest burst time in the ready queue
                if (!readyQueue.isEmpty() && readyQueue.get(0).remainingBurstTime < currentProcess.remainingBurstTime) {
                    // Preempt current process
                    Process nextProcess = readyQueue.remove(0);  // Process with shortest burst time
                    readyQueue.add(currentProcess);  // Put current process back in the queue
                    currentProcess = nextProcess;  // Start the new process
                    currentProcess.setRemainingBurstTime(currentProcess.getRemainingBurstTime() + currentProcess.getCountaging());
                    currentProcess.resetcountaging();
                    currentTime += contextSwitching;  // Account for context switching time
                    if (!readyQueue.isEmpty()) {
                        Process p = readyQueue.get(0);
                        int arr = p.arrivalTime;
                        if (arr < (currentTime + currentProcess.getRemainingBurstTime()) && (p.getRemainingBurstTime() < currentProcess.getRemainingBurstTime())) {
                            //Update the Gantt chart with the preemption
                            gui.updateGanttChart(currentTime, currentProcess, arr);
                        }
                    }
                    else {
                        //Update the Gantt chart with the preemption
                        gui.updateGanttChart(currentTime, currentProcess, currentProcess.getRemainingBurstTime());
                    }
//                    gui.updateGanttChart(currentTime, currentProcess, currentTime + 1);
                    System.out.println(currentProcess.name + " (Preempted, Remaining Burst Time: " + currentProcess.remainingBurstTime + ")");
                    gui.updateDetails(currentProcess.name + " (Preempted, Remaining Burst Time: " + currentProcess.remainingBurstTime + ")");

                }
            }

            // Execute one unit of time for the current process
            if (currentProcess != null) {
                // Execute one unit of time for the current process
//                //Update the Gantt chart with the preemption
//                gui.updateGanttChart(currentTime, currentProcess, currentTime + 1);
                currentProcess.remainingBurstTime--;
                System.out.println(currentProcess.name + " (Remaining Burst Time: " + currentProcess.remainingBurstTime + ")");
                gui.updateDetails(currentProcess.name + " (Remaining Burst Time: " + currentProcess.remainingBurstTime + ")");

            }

            // If the current process is finished, update its turnaround and waiting times
            if (currentProcess != null && currentProcess.remainingBurstTime == 0) {
                int finishTime = currentTime + 1;  // Finish time is the current time + 1 for the last cycle

                // Turnaround Time = Finish Time - Arrival Time
                currentProcess.setTurnaroundTime(finishTime - currentProcess.arrivalTime);
                // Waiting Time = Turnaround Time - Burst Time
                currentProcess.setWaitingTime(currentProcess.getTurnaroundTime() - currentProcess.getBurstTime());

                totalWaitingTime += currentProcess.getWaitingTime();
                totalTurnaroundTime += currentProcess.getTurnaroundTime();
                executedProcesses.add(currentProcess);
                currentProcess = null;

            }

            // Add the context switching time after each cycle
            currentTime++;
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
