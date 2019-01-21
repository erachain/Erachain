package org.erachain.utils;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import sun.reflect.generics.tree.Tree;

import java.util.*;
import java.util.concurrent.BlockingQueue;

public class MonitoredThread extends Thread {
    long counter;
    long frequencySec;
    long frequencyMin;
    long frequencyHour;

    long point;

    /**
     * среднее значение срока оборота
     */
    long periodAvg;

    Object[] status;
    List<Object[]> statusLog = new ArrayList<Object[]>();

    private Set<Long> logKeys;
    private long logKey;

    public MonitoredThread() {
        super();
        this.logKeys = Collections.synchronizedSet(new TreeSet<Long>());

    }

    /**
     * перед запуском цикла ставим
     */
    public void initMonitor() {
        point = System.nanoTime();
    }

    /**
     *  ставим внутри цикла run сразу после while
     *  тут ловим скорость оборота цикла
     */
    public void setMonitorPoint() {

        counter++;

        long pointNew = System.nanoTime();
        long period;
        if (pointNew < point) {
            // переполнение случилось
            period = Long.MAX_VALUE - point + Long.MIN_VALUE - pointNew;

        } else {
            period = pointNew - point;
        }

        point = pointNew;

        periodAvg = ((periodAvg << 5) - periodAvg + period) >> 5;

    }

    private synchronized long upKey() {
        return this.logKey++;
    }

    public synchronized void setMonitorStatus(String status) {
        this.status = new Object[]{status, System.currentTimeMillis()};

        if (this.statusLog.size() > 100) {
            try {
                this.statusLog.remove(100);
            } catch (Exception e) {
            }
        }
        this.statusLog.add(this.status);

    }

    private long statusPoint;
    public synchronized long setMonitorStatusBefore(String status) {
        //long key = upKey();
        //this.logKeys.add(key);

        this.status = new Object[]{status, System.currentTimeMillis()};
        if (this.statusLog.size() > 100)
            this.statusLog.remove(100);

        this.statusLog.add(this.status);

        statusPoint = System.nanoTime();
        //return key;
        return 0;

    }

    public synchronized void setMonitorStatusAfter() {

        long pointNew = System.nanoTime();
        long period;
        if (pointNew < statusPoint) {
            // переполнение случилось
            period = Long.MAX_VALUE - statusPoint + Long.MIN_VALUE - pointNew;

        } else {
            period = pointNew - statusPoint;
        }

        this.status = new Object[]{this.status[0], this.status[1], period};

        this.statusLog.add(this.statusLog.size(), this.status);

    }

    public JSONObject monitorToJson(boolean withLog) {
        JSONObject info = new JSONObject();

        info.put("counter", counter);
        info.put("periodAvg [ns]", periodAvg);

        JSONArray statusJson = new JSONArray();
        statusJson.add(0, this.status[0]);
        statusJson.add(1, this.status[1]);
        info.put("status", statusJson);

        if (withLog) {
            JSONArray statusLogJson = new JSONArray();
            for (Object[] item : this.statusLog) {
                JSONArray statusItemLogJson = new JSONArray();
                statusItemLogJson.add(0, item[0]);
                statusItemLogJson.add(1, item[1]);

                statusLogJson.add(statusItemLogJson);

            }

            info.put("statusLog", statusLogJson);
        }

        return  info;

    }
}
