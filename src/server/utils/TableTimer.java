package server.utils;

public class TableTimer implements Runnable {
    private int seconds;
    private String TimerType;
    private boolean running;

    public TableTimer(int seconds, String type) {
        this.seconds = seconds;
        this.TimerType = type;
        this.running = true;
    }

    @Override
    public void run() {

    }

    public void stop() {
        this.running = false;
    }
}
