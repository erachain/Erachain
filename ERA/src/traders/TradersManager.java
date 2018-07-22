package traders;
// 30/03 ++

import controller.Controller;
import core.BlockChain;
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

    protected static final String WALLET_PASSWORD = "1";

    private static final Logger LOGGER = Logger.getLogger(TradersManager.class);
    private List<Rater> knownRaters;
    private List<Trader> knownTraders;
    private boolean run;

    public TradersManager() {
        this.knownRaters = new ArrayList<Rater>();
        this.knownTraders = new ArrayList<Trader>();
        this.run = true;

        this.start();
    }

    private void start() {

        //START RATERs THREADs
        RaterWEX raterForex = new RaterWEX(this, 600);
        this.knownRaters.add(raterForex);
        RaterLiveCoin raterLiveCoin = new RaterLiveCoin(this, 600);
        this.knownRaters.add(raterLiveCoin);
        RaterPolonex raterPolonex = new RaterPolonex(this, 600);
        this.knownRaters.add(raterPolonex);

        // WAIT START WALLET
        try {
            Thread.sleep(10000);
        } catch (Exception e) {
            //FAILED TO SLEEP
        }

        //START TRADERs THREADs
        Trader trader1 = new TraderA(this, 1000);
        this.knownTraders.add(trader1);

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

    public void stop() {
        this.run = false;

        for (Rater rater: this.knownRaters) {
            //rater.close();
        }
    }
}
