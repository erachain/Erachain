package traders;
// 30/03 ++

import controller.Controller;
import core.BlockChain;
import core.account.Account;
import org.apache.log4j.Logger;
import utils.ObserverMessage;

import java.math.BigDecimal;
import java.util.*;

// import org.apache.log4j.Logger;
//import core.BlockChain;
//import database.DBSet;
//import database.TransactionMap;
//import lang.Lang;

public class TradersManager extends Observable {

    protected static final String WALLET_PASSWORD = "123456789";

    private static final Logger LOGGER = Logger.getLogger(TradersManager.class);
    private List<Rater> knownRaters;
    private List<Trader> knownTraders;
    //private boolean run;

    public TradersManager() {
        this.knownRaters = new ArrayList<Rater>();
        this.knownTraders = new ArrayList<Trader>();
        //this.run = true;

        this.start();
    }

    private void start() {

        Controller cnt = Controller.getInstance();

        try {
            Thread.sleep(3000);
        } catch (Exception e) {
            //FAILED TO SLEEP
        }

        Account account = new Account("7NhZBb8Ce1H2S2MkPerrMnKLZNf9ryNYtP");

        if (true) {
            //START RATERs THREADs
            RaterWEX raterForex = new RaterWEX(this, 300);
            this.knownRaters.add(raterForex);
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
            }
        }

        if (true) {
            RaterLiveCoin raterLiveCoin = new RaterLiveCoin(this, 600);
            this.knownRaters.add(raterLiveCoin);
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
            }

        }

        if (true) {
            RaterPolonex raterPolonex = new RaterPolonex(this, 600);
            this.knownRaters.add(raterPolonex);
        }

        BigDecimal limit1 = new BigDecimal("0.01");
        BigDecimal limit2 = new BigDecimal("0.02");
        if (true) {
            //START TRADERs THREADs
            HashMap<BigDecimal, BigDecimal> schemeUSD_RUB = new HashMap<>();
            schemeUSD_RUB.put(new BigDecimal(1000), new BigDecimal("0.1"));
            schemeUSD_RUB.put(new BigDecimal(100), new BigDecimal("0.03"));
            schemeUSD_RUB.put(new BigDecimal(10), new BigDecimal("0.01"));
            schemeUSD_RUB.put(new BigDecimal(-10), new BigDecimal("0.01"));
            schemeUSD_RUB.put(new BigDecimal(-100), new BigDecimal("0.03"));
            schemeUSD_RUB.put(new BigDecimal(-1000), new BigDecimal("0.1"));
            Trader trader1 = new StoneGuardAbs(this, account.getAddress(),
                    BlockChain.GENERATING_MIN_BLOCK_TIME_MS,
            1077, 1078, schemeUSD_RUB, limit1, limit1,true);
            this.knownTraders.add(trader1);

            try {
                Thread.sleep(100);
            } catch (Exception e) {
            }

        }

        if (true) {
            BigDecimal limit = new BigDecimal("0.3");
            //START TRADERs THREADs
            HashMap<BigDecimal, BigDecimal> schemeUSD_RUB = new HashMap<>();
            schemeUSD_RUB.put(new BigDecimal(30000), new BigDecimal("1.0"));
            schemeUSD_RUB.put(new BigDecimal(10000), new BigDecimal("0.7"));
            schemeUSD_RUB.put(new BigDecimal(100), new BigDecimal("0.3"));
            schemeUSD_RUB.put(new BigDecimal(-100), new BigDecimal("0.3"));
            schemeUSD_RUB.put(new BigDecimal(-10000), new BigDecimal("0.7"));
            schemeUSD_RUB.put(new BigDecimal(-30000), new BigDecimal("1.0"));
            Trader trader1 = new StoneGuardAbs(this, account.getAddress(),
                    BlockChain.GENERATING_MIN_BLOCK_TIME_MS<<1,
            1077, 1078, schemeUSD_RUB, limit, limit,true);
            this.knownTraders.add(trader1);

            try {
                Thread.sleep(100);
            } catch (Exception e) {
            }

        }

        if (true) {
            HashMap<BigDecimal, BigDecimal> schemeBTC_USD = new HashMap<>();
            schemeBTC_USD.put(new BigDecimal(1), new BigDecimal("0.5"));
            schemeBTC_USD.put(new BigDecimal("0.1"), new BigDecimal("0.2"));
            schemeBTC_USD.put(new BigDecimal("0.01"), new BigDecimal("0.05")); // !!!! FOR GOOD SCALE USE STRING - not DOUBLE
            schemeBTC_USD.put(new BigDecimal("-0.01"), new BigDecimal("0.05"));
            schemeBTC_USD.put(new BigDecimal("-0.1"), new BigDecimal("0.2"));
            schemeBTC_USD.put(new BigDecimal(-1), new BigDecimal("0.5"));
            Trader trader2 = new StoneGuard(this, account.getAddress(),
                    BlockChain.GENERATING_MIN_BLOCK_TIME_MS,
                    1079, 1077, schemeBTC_USD, limit1, limit1, true);
            this.knownTraders.add(trader2);

            try {
                Thread.sleep(100);
            } catch (Exception e) {
            }
        }

        if (true) {
            BigDecimal limit = new BigDecimal("0.5");
            HashMap<BigDecimal, BigDecimal> schemeBTC_USD = new HashMap<>();
            schemeBTC_USD.put(new BigDecimal(7), new BigDecimal("1"));
            schemeBTC_USD.put(new BigDecimal(3), new BigDecimal("0.8"));
            schemeBTC_USD.put(new BigDecimal(-3), new BigDecimal("0.8"));
            schemeBTC_USD.put(new BigDecimal(-7), new BigDecimal("1.0"));
            Trader trader2 = new StoneGuard(this, account.getAddress(),
                    BlockChain.GENERATING_MIN_BLOCK_TIME_MS<<1,
                    1079, 1077, schemeBTC_USD, limit, limit, true);
            this.knownTraders.add(trader2);

            try {
                Thread.sleep(100);
            } catch (Exception e) {
            }
        }

        if (true) {
            //START TRADER COMPU <> ERA
            HashMap<BigDecimal, BigDecimal> schemeCOMPU_ERA = new HashMap<>();
            schemeCOMPU_ERA.put(new BigDecimal("0.1"), new BigDecimal("2"));
            schemeCOMPU_ERA.put(new BigDecimal("0.01"), new BigDecimal("1"));
            schemeCOMPU_ERA.put(new BigDecimal("-0.01"), new BigDecimal("1"));
            schemeCOMPU_ERA.put(new BigDecimal("-0.1"), new BigDecimal("2"));
            Trader trader = new StoneGuard(this, account.getAddress(),
                    BlockChain.GENERATING_MIN_BLOCK_TIME_MS,
                    2, 1, schemeCOMPU_ERA, limit2, limit2, true);
            this.knownTraders.add(trader);

            try {
                Thread.sleep(1000);
            } catch (Exception e) {
            }
        }

    }

    @Override
    public void addObserver(Observer o) {
        super.addObserver(o);

        //SEND CONNECTEDPEERS ON REGISTER
        o.update(this, new ObserverMessage(ObserverMessage.TRADERS_UPDATE_TYPE, this.knownRaters));
    }

    public void notifyObserveUpdateRater(Rater rater) {
        //NOTIFY OBSERVERS
        this.setChanged();
        this.notifyObservers(new ObserverMessage(ObserverMessage.TRADERS_UPDATE_TYPE, rater));

    }

    public void setRun(boolean status) {

        for (Rater rater: this.knownRaters) {
            rater.setRun(status);
        }
    }

    public void stop() {

        for (Rater rater: this.knownRaters) {
            rater.setRun(false);
        }
    }
}
