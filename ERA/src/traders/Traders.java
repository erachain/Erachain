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

public class Traders extends Observable {


    private static final Logger LOGGER = Logger.getLogger(Traders.class);
    private List<Rater> knownRaters;
    private SortedSet<String> handledMessages;
    private boolean run;

    public Traders() {
        this.knownRaters = new ArrayList<Rater>();
        this.run = true;

        this.start();
    }

    private void start() {

        this.handledMessages = Collections.synchronizedSortedSet(new TreeSet<String>());

        //START ConnectionCreator THREAD
        Rater raterForex = new Rater(this, 200);
        this.knownRaters.add(raterForex);
        raterForex.start();
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
    }
}
