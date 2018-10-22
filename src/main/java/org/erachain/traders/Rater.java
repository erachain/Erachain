package org.erachain.traders;
// 30/03

import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.core.blockexplorer.BlockExplorer;
import org.erachain.datachain.DCSet;
import org.erachain.network.Peer;
import org.erachain.network.message.HWeightMessage;
import org.erachain.network.message.Message;
import org.erachain.network.message.MessageFactory;
import org.apache.log4j.Logger;
import org.mapdb.Fun;
import org.mapdb.Fun.Tuple2;
import org.erachain.settings.Settings;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;


public abstract class Rater extends Thread {

    private static final Logger LOGGER = Logger.getLogger(Rater.class);

    // HAVE KEY + WANT KEY + COURSE NAME
    private static TreeMap<Fun.Tuple3<Long, Long, String>, BigDecimal> rates = new TreeMap<Fun.Tuple3<Long, Long, String>, BigDecimal>();

    private TradersManager tradersManager;
    private long sleepTimestep;

    protected Controller cnt;
    protected CallRemoteApi caller;

    // https://api.livecoin.net/exchange/ticker?currencyPair=EMC/BTC
    // https://poloniex.com/public?command=returnTradeHistory&currencyPair=BTC_DOGE
    // https://wex.nz/api/3/ticker/btc_rur
    protected String courseName; // course name
    protected String apiURL;
    protected BigDecimal shiftRate = BigDecimal.ONE;
    private boolean run = true;


    public Rater(TradersManager tradersManager, String courseName, int sleepSec) {

        this.cnt = Controller.getInstance();
        this.caller = new CallRemoteApi();

        this.tradersManager = tradersManager;
        this.sleepTimestep = sleepSec * 1000;
        this.courseName = courseName;

        this.setName("Thread Rater - " + this.getClass().getName() + ": " + this.courseName);
        this.start();
    }

    protected abstract void parse(String result);

    public static TreeMap<Fun.Tuple3<Long, Long, String>, BigDecimal> getRates() {
        return rates;
    }

    public boolean tryGetRate() {

        String callerResult = null;
        try {
            callerResult = caller.ResponseValueAPI(this.apiURL, "GET", "");
            this.parse(callerResult);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return false;
        }

        return true;
    }

    public void run() {

        int sleepTimeFull = Settings.getInstance().getPingInterval();

        Controller cntr = Controller.getInstance();

        while (true) {

            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                //FAILED TO SLEEP
            }

            if (!cntr.isStatusOK() ||
                    !this.run) {
                continue;
            }

            try {
                this.tryGetRate();
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }

            //SLEEP
            try {
                Thread.sleep(sleepTimestep);
            } catch (InterruptedException e) {
                //FAILED TO SLEEP
            }

        }

    }

    protected static synchronized void setRate(Long haveKey, Long wantKey, String courseName, BigDecimal rate) {
            Rater.rates.put(new Fun.Tuple3<Long, Long, String>(haveKey, wantKey, courseName), rate);
    }

    public void setRun(boolean status) {
        this.run = status;
    }

    public void close() {
        this.run = false;
    }
}
