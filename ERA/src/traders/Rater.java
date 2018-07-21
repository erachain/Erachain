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

public class Rater extends Thread {

    private static final Logger LOGGER = Logger.getLogger(Rater.class);

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

    private List<Fun.Tuple3<Long, Long, BigDecimal>> rates = new ArrayList<Fun.Tuple3<Long, Long, BigDecimal>>();;

    public Rater(Traders trader, int sleepSec) {

        this.cnt = Controller.getInstance();
        this.caller = new CallRemoteApi();

        this.trader = trader;
        this.sleepTimestep = sleepSec * 1000;

        this.setName("Thread Rater - " + this.getId());
        this.start();
    }

    public List<Fun.Tuple3<Long, Long, BigDecimal>> getRates() {
        return this.rates;
    }

    public boolean tryGetRate() {

        try {
            String resultAsset = caller.ResponseValueAPI
                    (this.apiURL, "GET", "");
        } catch (Exception e) {
            //FAILED TO SLEEP
            return false;
        }

        return true;
    }

    public void run() {


        int sleepTimeFull = Settings.getInstance().getPingInterval();

        while (true) {

            //SLEEP
            try {
                Thread.sleep(sleepTimestep);
            } catch (InterruptedException e) {
                //FAILED TO SLEEP
            }

            this.tryGetRate();

        }
    }

}
