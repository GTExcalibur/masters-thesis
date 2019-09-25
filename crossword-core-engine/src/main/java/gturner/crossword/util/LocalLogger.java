package gturner.crossword.util;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by IntelliJ IDEA.
 * User: George Turner
 * Date: 4/3/11
 * Time: 12:31 AM
 * To change this template use File | Settings | File Templates.
 */
public class LocalLogger {
    private static final ExecutorService sout_thread = Executors.newSingleThreadExecutor();

    private static final Map<String, String> lazyStatusUpdates = new ConcurrentHashMap<String, String>();

    private static final Timer lazy_status_timer = new Timer();
    static {
        lazy_status_timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                for (final Map.Entry<String, String> entry : lazyStatusUpdates.entrySet()) {
                    sout_thread.submit(new Runnable() {
                        @Override
                        public void run() {
                            System.out.println(entry.getKey() + ": " + entry.getValue());
                        }
                    });
                }
                lazyStatusUpdates.clear();
            }
        }, 1000, 1000);
    }

    public static void outputLazyStatusMessage(String category, String message) {
        lazyStatusUpdates.put(category, message);
    }

    public static void outputDebugMessage(final String message) {
        sout_thread.submit(new Runnable(){
            @Override
            public void run() {
                System.out.println(message);
            }
        });
    }
}
