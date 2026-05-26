package server.utils;

import server.logic.BlackjackTable;

public class TableTimer implements Runnable {
    private int seconds;
    private String timerType;
    private boolean running;
    private BlackjackTable blackjackTable;

    public TableTimer(int seconds, String type, BlackjackTable blackjackTable) {
        this.seconds = seconds;
        this.timerType = type;
        this.running = true;
        this.blackjackTable = blackjackTable;
    }

    @Override
    public void run() {
        try {
            while (seconds > 0 && running) {
                if (timerType.equals("PLAY") && seconds == 10) {
                    blackjackTable.sendMessageToAll("TIMER:10 seconds left");
                }

                Thread.sleep(1000);
                seconds--;
            }

            if (running) {
                blackjackTable.processEndTimer(timerType);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        this.running = false;
    }
}
