package fiitstu.gulis.cmsimulator.elements;

import android.os.CountDownTimer;

import java.sql.Time;

public class Timer {
    private static Timer instance = null;
    private static CountDownTimer timer;
    private static long milisLeft;
    private static OnTickListener listener;
    private static OnTimeRunOutListener listener2;
    private static boolean isRunning = false;

    public interface OnTickListener {
        void onTick(long millisUntilFinished);
    }

    public interface OnTimeRunOutListener {
        void onTimeRunOut();
    }

    public static void setOnTickListener(OnTickListener input) {
        listener = input;
    }

    public static void setOnTimeRunOutListener(OnTimeRunOutListener input) {
        listener2 = input;
    }

    private static final long tickLengthInMilis = 1000;

    private Timer(Time time) {
        milisLeft = convertToMilis(time);
        timer = new CountDownTimer(milisLeft, tickLengthInMilis) {
            @Override
            public void onTick(long millisUntilFinished) {
                milisLeft = millisUntilFinished;
                listener.onTick(millisUntilFinished);
            }

            @Override
            public void onFinish() {
                milisLeft = 0;
                listener2.onTimeRunOut();
            }
        };
    }

    public static Timer getInstance(Time time) {
        if (instance == null)
            instance = new Timer(time);
        return instance;
    }

    private long convertToMilis(Time time) {
        final int milisInSecond = 1000;

        int hours = time.getHours();
        int minutes = time.getMinutes();
        long seconds = time.getSeconds();

        minutes += hoursToMinutes(hours);
        seconds += minutesToSeconds(minutes);

        return seconds * milisInSecond;
    }

    private int hoursToMinutes(int hours) {
        final int minutesInAnHour = 60;

        return hours * minutesInAnHour;
    }

    private int minutesToSeconds(int minutes) {
        final int secondsInMinutes = 60;

        return minutes * secondsInMinutes;
    }

    public void startTimer() {
        if (timer != null) {
            timer.start();
        }

        isRunning = true;
    }

    public void pauseTimer() {
        if (timer != null) {
            timer.cancel();
        }
        isRunning = false;
    }

    public Time getCurrentTime() {
        int hours = (int) (milisLeft / 3600000);
        int minutes = (int) ((milisLeft - (hours * 3600000)) / 60000);
        int seconds = (int) ((milisLeft - (hours * 3600000) - (minutes * 60000)) / 1000);

        final String sTime = String.format("%02d:%02d:%02d", hours, minutes, seconds);
        final Time time = Time.valueOf(sTime);

        return time;
    }


    public void resetTimer() {
        milisLeft = 0;
        isRunning = false;
    }

    public static void deleteTimer() {
        if (timer != null) {
            timer.cancel();
        }
        milisLeft = 0;
        timer = null;
        listener = null;
        listener2 = null;
        instance = null;
        isRunning = false;
    }

    public static boolean isSet() {
        return isRunning;
    }

}
