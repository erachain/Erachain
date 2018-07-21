package traders;
// 30/03

import controller.Controller;
import core.BlockChain;
import core.blockexplorer.BlockExplorer;
import datachain.DCSet;
import network.Peer;
import network.message.HWeightMessage;
import network.message.Message;
import network.message.MessageFactory;
import org.apache.log4j.Logger;
import org.mapdb.Fun;
import org.mapdb.Fun.Tuple2;
import settings.Settings;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;


public abstract class Rater extends Thread {

    private static final Logger LOGGER = Logger.getLogger(Rater.class);

    protected static TreeMap<Fun.Tuple2<Long, Long>, BigDecimal> rates = new TreeMap<Fun.Tuple2<Long, Long>, BigDecimal>();

    private Traders trader;
    private long sleepTimestep;

    protected Controller cnt;
    protected CallRemoteApi caller;

    // https://api.livecoin.net/exchange/ticker?currencyPair=EMC/BTC
    // 'https://' + exchg.url + '/' + exchg.API + '?currencyPair=' + t1 + '/' + t2
    // https://poloniex.com/public?command=returnTradeHistory&currencyPair=BTC_DOGE
    //   cryp_url = 'https://' + exchg.url + '/public?command=returnOrderBook&depth=1&currencyPair=' + pair.ticker
    // https://wex.nz/api/3/ticker/btc_rur
    protected String apiURL;
    protected BigDecimal shiftRate = BigDecimal.ONE;
    private boolean run = true;


    public Rater(Traders trader, int sleepSec) {

        this.cnt = Controller.getInstance();
        this.caller = new CallRemoteApi();

        this.trader = trader;
        this.sleepTimestep = sleepSec * 1000;

        this.setName("Thread Rater - " + this.getClass().getName());
        this.start();
    }

    protected abstract void parse(String result);

    public TreeMap<Fun.Tuple2<Long, Long>, BigDecimal> getRates() {
        return this.rates;
    }

    public boolean tryGetRate() {

        String callerResult = null;
        try {
            callerResult = caller.ResponseValueAPI(this.apiURL, "GET", "");
            this.parse(callerResult);
        } catch (Exception e) {
            //FAILED TO SLEEP
            return false;
        }

        return true;
    }

    public void run() {


        int sleepTimeFull = Settings.getInstance().getPingInterval();

        while (this.run) {

            try {
                this.tryGetRate();
                Thread.sleep(1000);
            } catch (Exception e) {
                //FAILED TO SLEEP
            }

            //SLEEP
            try {
                Thread.sleep(sleepTimestep);
            } catch (InterruptedException e) {
                //FAILED TO SLEEP
            }

        }
    }

    public void close() {
        this.run = false;
    }
}
