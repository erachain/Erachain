package traders;
// 30/03 ++

import controller.Controller;
import core.BlockChain;
import core.account.Account;
import core.crypto.Base58;
import datachain.DCSet;
import network.*;
import network.message.FindMyselfMessage;
import network.message.Message;
import network.message.MessageFactory;
import network.message.TelegramMessage;
import org.apache.log4j.Logger;
import org.mapdb.Fun.Tuple2;
import settings.Settings;
import utils.ObserverMessage;

import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
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

        //START RATERs THREADs
        RaterWEX raterForex = new RaterWEX(this, 300);
        this.knownRaters.add(raterForex);
        try {
            Thread.sleep(1000);
        } catch (Exception e) {
        }
        RaterLiveCoin raterLiveCoin = new RaterLiveCoin(this, 600);
        this.knownRaters.add(raterLiveCoin);
        try {
            Thread.sleep(1000);
        } catch (Exception e) {
        }
        RaterPolonex raterPolonex = new RaterPolonex(this, 600);
        this.knownRaters.add(raterPolonex);


        Controller cnt = Controller.getInstance();
        // WAIT START WALLET
        while(!cnt.doesWalletDatabaseExists() ||  Rater.getRates().isEmpty()) {
            try {
                Thread.sleep(3000);
            } catch (Exception e) {
                //FAILED TO SLEEP
            }
        }

        Account account = Controller.getInstance().wallet.getAccounts().get(1);
        if (!account.equals("7NhZBb8Ce1H2S2MkPerrMnKLZNf9ryNYtP"))
            return;

        BigDecimal limit1 = new BigDecimal("0.01");
        BigDecimal limit2 = new BigDecimal("0.5");
        if (true) {
            //START TRADERs THREADs
            TreeMap<BigDecimal, BigDecimal> schemeUSD_RUB = new TreeMap<>();
            schemeUSD_RUB.put(new BigDecimal(10000), new BigDecimal("1"));
            schemeUSD_RUB.put(new BigDecimal(1000), new BigDecimal("0.5"));
            schemeUSD_RUB.put(new BigDecimal(100), new BigDecimal("0.2"));
            schemeUSD_RUB.put(new BigDecimal(10), new BigDecimal("0.1"));
            schemeUSD_RUB.put(new BigDecimal(-10), new BigDecimal("0.1"));
            schemeUSD_RUB.put(new BigDecimal(-100), new BigDecimal("0.2"));
            schemeUSD_RUB.put(new BigDecimal(-1000), new BigDecimal("0.5"));
            schemeUSD_RUB.put(new BigDecimal(-10000), new BigDecimal("1"));
            Trader trader1 = new TraderA(this, account.getAddress(), 100,
                    1077, 1078, schemeUSD_RUB, limit2, limit2,true);
            this.knownTraders.add(trader1);

            try {
                Thread.sleep(100);
            } catch (Exception e) {
            }

        }

        if (true) {
            TreeMap<BigDecimal, BigDecimal> schemeBTC_USD = new TreeMap<>();
            schemeBTC_USD.put(new BigDecimal(10), new BigDecimal("1"));
            schemeBTC_USD.put(new BigDecimal(1), new BigDecimal("0.5"));
            schemeBTC_USD.put(new BigDecimal("0.1"), new BigDecimal("0.2"));
            schemeBTC_USD.put(new BigDecimal("0.01"), new BigDecimal("0.1")); // !!!! FIR GOOD SCALE USE STRING - not DOUBLE
            schemeBTC_USD.put(new BigDecimal("-0.01"), new BigDecimal("0.1"));
            schemeBTC_USD.put(new BigDecimal("-0.1"), new BigDecimal("0.2"));
            schemeBTC_USD.put(new BigDecimal(-1), new BigDecimal("0.5"));
            schemeBTC_USD.put(new BigDecimal(-10), new BigDecimal("1"));
            Trader trader2 = new TraderA(this, account.getAddress(), 100,
                    1079, 1077, schemeBTC_USD, limit1, limit1, true);
            this.knownTraders.add(trader2);

            try {
                Thread.sleep(100);
            } catch (Exception e) {
            }
        }

        if (true) {
            //START TRADER COMPU <> ERA
            TreeMap<BigDecimal, BigDecimal> schemeCOMPU_ERA = new TreeMap<>();
            schemeCOMPU_ERA.put(new BigDecimal("10"), new BigDecimal("10"));
            schemeCOMPU_ERA.put(new BigDecimal("1"), new BigDecimal("5"));
            schemeCOMPU_ERA.put(new BigDecimal("0.1"), new BigDecimal("2"));
            schemeCOMPU_ERA.put(new BigDecimal("0.01"), new BigDecimal("1"));
            schemeCOMPU_ERA.put(new BigDecimal("-0.01"), new BigDecimal("1"));
            schemeCOMPU_ERA.put(new BigDecimal("-0.1"), new BigDecimal("2"));
            schemeCOMPU_ERA.put(new BigDecimal("-1"), new BigDecimal("5"));
            schemeCOMPU_ERA.put(new BigDecimal("-10"), new BigDecimal("10"));
            Trader trader = new TraderA(this, account.getAddress(), 100,
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
