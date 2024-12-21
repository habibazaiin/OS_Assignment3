package org.example;

public class Process {
    String name;
    String color;
    int arrivalTime;
    int burstTime;
    int remainingBurstTime;
    int priority;
    int waitingTime;
    int turnaroundTime;
    double FCAIFactor;
    int countaging = 0;
    int quantum;
    int FinishTime = 0;

    public Process(String name, String color, int arrivalTime, int burstTime, int priority, int quantum) {
        this.name = name;
        this.color = color;
        this.arrivalTime = arrivalTime;
        this.burstTime = burstTime;
        this.remainingBurstTime = burstTime;
        this.priority = priority;
        this.waitingTime = 0;
        this.turnaroundTime = 0;
        this.FCAIFactor = 1;
        this.quantum = quantum;
    }

    public int getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(int arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public int getBurstTime() {
        return burstTime;
    }

    public void setBurstTime(int burstTime) {
        this.burstTime = burstTime;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public int getRemainingBurstTime() {
        return remainingBurstTime;
    }

    public void setRemainingBurstTime(int remainingBurstTime) {
        this.remainingBurstTime = remainingBurstTime;
    }

    public int getWaitingTime() {
        return waitingTime;
    }

    public void setWaitingTime(int waitingTime) {
        this.waitingTime = waitingTime;
    }

    public int getTurnaroundTime() {
        return turnaroundTime;
    }

    public void setTurnaroundTime(int turnaroundTime) {
        this.turnaroundTime = turnaroundTime;
    }

    public double getFCAIFactor() {
        return FCAIFactor;
    }

    public void setFCAIFactor(int FCAIFactor) {
        this.FCAIFactor = FCAIFactor;
    }

    public int getCountaging() {
        return countaging;
    }
    public void setCountaging(int countaging) {
        this.countaging = countaging;
    }
    public void resetcountaging() {this.countaging = 0;}

    public int getQuantum() {
        return quantum;
    }

    public void setQuantum(int quantum) {
        this.quantum = quantum;
    }

    public void calculateTimes(int currentTime) {
        this.turnaroundTime = currentTime - arrivalTime + burstTime;
        this.waitingTime = turnaroundTime - burstTime;
    }

    @Override
    public String toString() {
        return String.format("%-10s %-10d %-10d %-10d %-10d %-10d",
                name, arrivalTime, burstTime, waitingTime, turnaroundTime, priority);
    }

    public String getName() {
        return name;
    }

    public void setFinishTime(int currentTime)
    {
        this.FinishTime = currentTime;
    }

    public int getFinishTime() {
        return FinishTime;
    }

    public void updateFcaiFactor(double v1, double v2) {
        this.FCAIFactor = (10 - priority) + Math.ceil(arrivalTime / v1) + Math.ceil(remainingBurstTime / v2);
    }
}

//processes:4
//context switching: 2
// p1, #FF0000, 0, 17, 4, 4
//p2, #00FF00, 2, 6, 9, 3
//p3, #0000FF, 3, 3, 9, 5
//p4,#FFFF00, 29, 4, 10, 2

