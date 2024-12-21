package org.example;

import java.util.*;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);


        System.out.println("Enter the number of processes:");
        int numProcesses = sc.nextInt();


        System.out.println("Enter the context switching time:");
        int contextSwitching = sc.nextInt();

        // List to hold the processes
        List<Process> processes = new ArrayList<>();

        // Input details for each process
        for (int i = 0; i < numProcesses; i++) {
            System.out.println("Enter details for Process " + (i + 1) + ":");
            System.out.print("Name: ");
            String name = sc.next();
            System.out.print("Color (for GUI representation): ");
            String color = sc.next();
            System.out.print("Arrival Time: ");
            int arrivalTime = sc.nextInt();
            System.out.print("Burst Time: ");
            int burstTime = sc.nextInt();
            System.out.print("Priority (1-10, 1 being highest): ");
            int priority = sc.nextInt();
            System.out.print("Quantum: ");
            int Quantum = sc.nextInt();
            processes.add(new Process(name, color, arrivalTime, burstTime, priority, Quantum));
        }

        // Initialize the TimingGUI object
        TimingGUI gui = new TimingGUI(numProcesses);

        // Provide scheduling options to the user
        System.out.println("\nChoose a scheduling algorithm:");
        System.out.println("1. Priority Scheduling");
        System.out.println("2. Non-Preemptive Shortest Job First (SJF)");
        System.out.println("3. Shortest Remaining Time First (SRTF with Aging)");
        System.out.println("4. FCAI Scheduling");

        int choice = sc.nextInt();

        // Handle the selected scheduling algorithm
        switch (choice) {
            case 1:
                PriorityScheduling.scheduleProcesses(processes, contextSwitching, gui);
                break;

            case 2:
                SJFWithAging.scheduleProcesses(processes, 5, gui);
                break;

            case 3:
                SRTFWithAging.scheduleProcesses(processes, contextSwitching, 5, gui);
                break;

            case 4:
                FCAIScheduling scheduler = new FCAIScheduling(processes);
                scheduler.scheduleProcesses(processes, contextSwitching, gui);
                break;

            default:
                System.out.println("Invalid choice. Please run the program again.");
                break;
        }

        // Close the scanner
        sc.close();
    }
}
