package traders;
// 30/03

import controller.Controller;
import org.apache.log4j.Logger;
import org.mapdb.Fun.Tuple2;
import settings.Settings;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.TreeMap;


public abstract class Trader extends Thread {

    private static final Logger LOGGER = Logger.getLogger(Trader.class);

    protected static TreeMap<BigInteger, BigDecimal> orders = new TreeMap<BigInteger, BigDecimal>();

    private TradersManager tradersManager;
    private long sleepTimestep;

    protected Controller cnt;
    protected CallRemoteApi caller;

    protected String apiURL;
    protected BigDecimal shiftRate = BigDecimal.ONE;
    protected Long have;
    protected Long want;
    protected BigDecimal rate;

    private boolean run = true;


    public Trader(TradersManager tradersManager, int sleepSec) {

        this.cnt = Controller.getInstance();
        this.caller = new CallRemoteApi();

        this.tradersManager = tradersManager;
        this.sleepTimestep = sleepSec * 1000;

        this.setName("Thread Trader - " + this.getClass().getName());
        this.start();
    }

    protected abstract void parse(String result);

    public TreeMap<BigInteger, BigDecimal> getOrders() {
        return this.orders;
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
