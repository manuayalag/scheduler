package scheduler;

public class GanttEntry {
    private String pid;
    private int start;
    private int end;

    public GanttEntry(String pid, int start, int end) {
        this.pid = pid;
        this.start = start;
        this.end = end;
    }
    public String getPid() { return pid; }
    public int getStart() { return start; }
    public int getEnd() { return end; }
}
