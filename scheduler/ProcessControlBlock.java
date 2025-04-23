package scheduler;

public class ProcessControlBlock {
    private String pid;
    private int arrivalTime;
    private int burstTime;
    private int remainingTime;
    private int priority;
    private int startTime;
    private int finishTime;
    private int waitingTime;
    private int turnaroundTime;

    public ProcessControlBlock(String pid, int arrivalTime, int burstTime, int priority) {
        this.pid = pid;
        this.arrivalTime = arrivalTime;
        this.burstTime = burstTime;
        this.remainingTime = burstTime;
        this.priority = priority;
    }

    // Getters y setters
    public String getPid() { return pid; }
    public int getArrivalTime() { return arrivalTime; }
    public int getBurstTime() { return burstTime; }
    public int getRemainingTime() { return remainingTime; }
    public void setRemainingTime(int t) { this.remainingTime = t; }
    public int getPriority() { return priority; }
    public int getStartTime() { return startTime; }
    public void setStartTime(int t) { this.startTime = t; }
    public int getFinishTime() { return finishTime; }
    public void setFinishTime(int t) { this.finishTime = t; }
    public int getWaitingTime() { return waitingTime; }
    public void setWaitingTime(int t) { this.waitingTime = t; }
    public int getTurnaroundTime() { return turnaroundTime; }
    public void setTurnaroundTime(int t) { this.turnaroundTime = t; }
}

