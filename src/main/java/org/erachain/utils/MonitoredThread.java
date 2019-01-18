package org.erachain.utils;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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

    public void setMonitorStatus(String status) {
        this.status = new Object[]{status, System.currentTimeMillis()};
        if (this.statusLog.size() > 100)
            this.statusLog.remove(100);

        this.statusLog.add(this.status);

    }

    public JSONObject monitorToJson() {
        JSONObject info = new JSONObject();

        info.put("counter", counter);
        info.put("periodAvg", periodAvg);

        JSONArray statusJson = new JSONArray();
        statusJson.add(0, this.status[0]);
        statusJson.add(1, this.status[1]);

        JSONArray statusLogJson = new JSONArray();
        for (Object[] item: this.statusLog) {
            JSONArray statusItemLogJson = new JSONArray();
            statusItemLogJson.add(0, item[0]);
            statusItemLogJson.add(1, item[1]);

            statusLogJson.add(statusItemLogJson);

        }

        info.put("statusLog", statusLogJson);

        return  info;

    }
}
