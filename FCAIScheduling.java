package org.example;

import java.util.*;

public class FCAIScheduling {

    private static List<Process> processList;

    public FCAIScheduling(List<Process> processes) {
        processList = processes;
    }

    public static Process addProcessToQueue(int time) {
        if (processList.size() > 0 && processList.get(0).getArrivalTime() <= time) {
            Process process = processList.get(0);
            processList.remove(0);
            return process;
        } else {
            return null;
        }
    }

    public static Process selectPreemptingProcess(Deque<Process> readyQueue, int thresholdFactor) {
        int minFactor = (int) 1e9;
        Process selectedProcess = null;
        for (Process p : readyQueue) {
            if (p.getFCAIFactor() < thresholdFactor) {
                if (p.getFCAIFactor() < minFactor) {
                    selectedProcess = p;
                    minFactor = (int) p.getFCAIFactor();
                }
            }
        }
        return selectedProcess;
    }

    public static void scheduleProcesses(List<Process> processes, int contextSwitchTime, TimingGUI gui) {
        List<Process> completedProcesses = new ArrayList<>();
        int totalWaitTime = 0;
        int totalTurnaroundTime = 0;
        updateBoth("Execution started.", gui);

        int totalProcesses = processes.size();
        StringBuilder executionOrder = new StringBuilder();

        int lastArrivalTime = processes.stream().mapToInt(Process::getArrivalTime).max().orElse(0);
        int maxBurstTime = processes.stream().mapToInt(Process::getBurstTime).max().orElse(0);
        double factorV1 = lastArrivalTime / 10.0;
        double factorV2 = maxBurstTime / 10.0;

        for (int i = 0; i < totalProcesses; i++) {
            processes.get(i).updateFcaiFactor(factorV1, factorV2);
            updateBoth(String.format("Updated FCAI factor for process %s. FCAI Factor: %.2f",
                    processes.get(i).getName(), processes.get(i).getFCAIFactor()), gui);
        }

        Process currentProcess = processes.get(0);
        Deque<Process> readyQueue = new ArrayDeque<>();
        int currentTime = processes.get(0).getArrivalTime();

        readyQueue.push(currentProcess);
        processes.remove(currentProcess);
        int completedCount = 0;

        while (completedCount != totalProcesses) {
            while (completedCount != totalProcesses) {
                if (readyQueue.isEmpty()) {
                    readyQueue.addFirst(processes.get(0));
                    processes.remove(0);
                    updateBoth(String.format("Process %s added to ready queue at time %d", readyQueue.getFirst().getName(), currentTime), gui);
                }

                updateBoth(String.format("Process %s started execution at time %d", readyQueue.getFirst().getName(), currentTime), gui);

                int previousQuantum = readyQueue.getFirst().getQuantum();
                int executionTime = (int) Math.ceil(0.4 * readyQueue.getFirst().getQuantum());
                executionTime = Math.min(executionTime, readyQueue.getFirst().getRemainingBurstTime());
                readyQueue.getFirst().setRemainingBurstTime(readyQueue.getFirst().getRemainingBurstTime() - executionTime);
                currentTime += executionTime;

                executionOrder.append(readyQueue.getFirst().getName()).append(" ");
                updateBoth(String.format("Current Execution Order: %s", executionOrder.toString().trim()), gui);

                int unusedQuantum = readyQueue.getFirst().getQuantum() - executionTime;

                if (readyQueue.getFirst().getRemainingBurstTime() <= 0) {
                    updateBoth(String.format("Remaining Burst Time is Reduced: %d", readyQueue.getFirst().getRemainingBurstTime()), gui);
                    gui.updateGanttChart(currentTime - executionTime, currentProcess, currentTime);
                    currentProcess = readyQueue.getFirst();
                    currentProcess.setTurnaroundTime(currentTime - currentProcess.getArrivalTime());
                    currentProcess.setWaitingTime(currentProcess.getTurnaroundTime() - currentProcess.getBurstTime());
                    gui.updateGanttChart(currentProcess.getArrivalTime(), currentProcess, currentTime);
                    totalWaitTime += currentProcess.getWaitingTime();
                    totalTurnaroundTime += currentProcess.getTurnaroundTime();

                    completedProcesses.add(currentProcess);

                    updateBoth(String.format("Process %s completed at time %d", readyQueue.getFirst().getName(), currentTime), gui);
                    updateBoth(String.format("Process %s Waiting Time: %d", currentProcess.getName(), currentProcess.getWaitingTime()), gui);
                    updateBoth(String.format("Process %s Turnaround Time: %d", currentProcess.getName(), currentProcess.getTurnaroundTime()), gui);

                    completedCount++;
                    readyQueue.remove(readyQueue.getFirst());
                    break;
                }

                boolean preempted = false;

                Process preemptingProcess = selectPreemptingProcess(readyQueue, (int) readyQueue.getFirst().getFCAIFactor());
                if (preemptingProcess != null) {
                    updateBoth(String.format("Remaining Burst Time is Reduced: %d", readyQueue.getFirst().getRemainingBurstTime()), gui);
                    gui.updateGanttChart(currentTime - executionTime, currentProcess, currentTime);
                    updateBoth(String.format("Preemption occurred at time %d - Process %s is preempted by %s", currentTime, readyQueue.getFirst().getName(), preemptingProcess.getName()), gui);
                    readyQueue.getFirst().updateFcaiFactor(factorV1, factorV2);
                    readyQueue.getFirst().setQuantum(previousQuantum + unusedQuantum);
                    updateBoth(String.format("Updated quantum for process %s to %d", readyQueue.getFirst().getName(), readyQueue.getFirst().getQuantum()), gui);
                    updateBoth(String.format("Updated FCAI factor for process %s after preemption. FCAI Factor: %.2f", readyQueue.getFirst().getName(), readyQueue.getFirst().getFCAIFactor()), gui);
                    Process temp = readyQueue.getFirst();
                    readyQueue.remove(preemptingProcess);
                    readyQueue.remove(readyQueue.getFirst());
                    readyQueue.addFirst(preemptingProcess);
                    readyQueue.addLast(temp);
                } else {
                    while (unusedQuantum > 0 && readyQueue.getFirst().getRemainingBurstTime() > 0) {
                        readyQueue.getFirst().setRemainingBurstTime(readyQueue.getFirst().getRemainingBurstTime() - 1);
                        updateBoth(String.format("Remaining Burst Time is Reduced: %d", readyQueue.getFirst().getRemainingBurstTime()), gui);
                        gui.updateGanttChart(currentTime - executionTime, currentProcess, currentTime + 1);
                        unusedQuantum--;
                        currentTime++;
                        Process addedProcess = addProcessToQueue(currentTime);
                        if (addedProcess != null) {
                            readyQueue.addLast(addedProcess);
                            Process tempProcess = selectPreemptingProcess(readyQueue, (int) readyQueue.getFirst().getFCAIFactor());
                            if (tempProcess != null) {
                                Process temp2 = readyQueue.getFirst();
                                readyQueue.remove(tempProcess);
                                readyQueue.remove(readyQueue.getFirst());
                                readyQueue.addFirst(tempProcess);
                                readyQueue.addLast(temp2);
                                preempted = true;
                                break;
                            }
                        }
                    }
                    if (readyQueue.getFirst().getRemainingBurstTime() <= 0) {
                        currentProcess = readyQueue.getFirst();
                        currentProcess.setTurnaroundTime(currentTime - currentProcess.getArrivalTime());
                        currentProcess.setWaitingTime(currentProcess.getTurnaroundTime() - currentProcess.getBurstTime());
                        totalWaitTime += currentProcess.getWaitingTime();
                        totalTurnaroundTime += currentProcess.getTurnaroundTime();
                        completedProcesses.add(currentProcess);
                        updateBoth(String.format("Process %s completed at time %d", readyQueue.getFirst().getName(), currentTime), gui);
                        updateBoth(String.format("Process %s Waiting Time: %d", currentProcess.getName(), currentProcess.getWaitingTime()), gui);
                        updateBoth(String.format("Process %s Turnaround Time: %d", currentProcess.getName(), currentProcess.getTurnaroundTime()), gui);

                        completedCount++;
                        readyQueue.remove(readyQueue.getFirst());
                        break;
                    }

                    if (unusedQuantum == 0) {
                        if (!preempted) {
                            updateBoth(String.format("Process %s finished its quantum, added back to the queue with new quantum", readyQueue.getFirst().getName()), gui);
                            readyQueue.addLast(readyQueue.getFirst());
                            readyQueue.remove(readyQueue.getFirst());
                        }
                        readyQueue.getLast().setQuantum(readyQueue.getLast().getQuantum() + 2);
                        updateBoth(String.format("Updated quantum for process %s to %d", readyQueue.getLast().getName(), readyQueue.getLast().getQuantum()), gui);
                    } else {
                        readyQueue.getLast().setQuantum(readyQueue.getLast().getQuantum() + unusedQuantum);
                        updateBoth(String.format("Updated quantum for process %s to %d", readyQueue.getLast().getName(), readyQueue.getLast().getQuantum()), gui);
                    }
                    readyQueue.getLast().updateFcaiFactor(factorV1, factorV2);
                    updateBoth(String.format("Updated FCAI factor for process %s. FCAI Factor: %.2f", readyQueue.getLast().getName(), readyQueue.getLast().getFCAIFactor()), gui);
                }
            }
        }
        updateBoth(String.format("Execution completed at time %d", currentTime), gui);
        updateBoth("\nProcess Details:", gui);
        updateBoth(String.format("%-10s %-10s %-10s %-10s %-10s %-10s", "Name", "Arrival", "Burst", "Waiting", "Turnaround", "Priority"), gui);
        for (Process process : completedProcesses) {
            updateBoth(process.toString(), gui);
        }

        double avgWaitTime = (double) totalWaitTime / completedProcesses.size();
        double avgTurnaroundTime = (double) totalTurnaroundTime / completedProcesses.size();

        updateBoth(String.format("\nAverage Waiting Time: %.2f", avgWaitTime), gui);
        updateBoth(String.format("Average Turnaround Time: %.2f", avgTurnaroundTime), gui);
    }

    private static void updateBoth(String message, TimingGUI gui) {
        System.out.println(message);
        gui.updateDetails(message);
    }
}