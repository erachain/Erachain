package org.erachain.network;

import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.core.crypto.Base58;
import org.erachain.datachain.DCSet;
import org.erachain.network.message.*;
import org.erachain.ntp.NTP;
import org.erachain.utils.ObserverMessage;
import org.json.simple.JSONObject;
import org.mapdb.Fun.Tuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

/**
 * основной класс модуля Сети
 */
public class Network extends Observable implements ConnectionCallback {


    public static final int PEER_SLEEP_TIME = BlockChain.HARD_WORK ? 0 : 1;
    private static final int MAX_HANDLED_MESSAGES_SIZE = BlockChain.HARD_WORK ? 1024 << 8 : 1024<<4;
    private static final int PINGED_MESSAGES_SIZE = BlockChain.HARD_WORK ? 1024 << 12 : 1024 << 8;
    private static final Logger LOGGER = LoggerFactory.getLogger(Network.class);
    private static InetAddress myselfAddress;
    private ConnectionCreator creator;
    private ConnectionAcceptor acceptor;
    private TelegramManager telegramer;
    private List<Peer> knownPeers;
    private SortedSet<String> handledMessages;
    private boolean run;

    public Network() {
        this.knownPeers = new ArrayList<Peer>();
        this.run = true;

        this.start();
    }

    public static InetAddress getMyselfAddress() {
        return myselfAddress;
    }

    public static boolean isPortAvailable(int port) {
        try {
            ServerSocket socket = new ServerSocket(port);
            socket.close();

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isMyself(InetAddress address) {

        if (myselfAddress != null
                && myselfAddress.getHostAddress().equals(address.getHostAddress())) {
            return true;
        }
        return false;
    }

    private void start() {
        this.handledMessages = Collections.synchronizedSortedSet(new TreeSet<String>());

        //START ConnectionCreator THREAD
        creator = new ConnectionCreator(this);
        creator.start();

        //START ConnectionAcceptor THREAD
        acceptor = new ConnectionAcceptor(this);
        acceptor.start();

        telegramer = new TelegramManager(this);
        telegramer.start();
    }

    @Override
    public void onConnect(Peer peer) {

        //LOGGER.info(Lang.getInstance().translate("Connection successfull : ") + peer);

        // WAIT start PINGER
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
        }

        boolean asNew = true;
        for (Peer peerKnown: this.knownPeers) {
            if (peer.equals(peerKnown)) {
                asNew = false;
                break;
            }
        }
        if (asNew) {
            //ADD TO CONNECTED PEERS
            synchronized (this.knownPeers) {
                this.knownPeers.add(peer);
            }
        }

        peer.setName("Peer: " + peer
                + (asNew? " as new" : " reconnected")
                + (peer.isWhite()?" is White" : ""));

        //ADD TO DATABASE
        PeerManager.getInstance().addPeer(peer, 0);

        if (Controller.getInstance().isOnStopping())
            return;

        //NOTIFY OBSERVERS
        this.setChanged();
        this.notifyObservers(new ObserverMessage(ObserverMessage.ADD_PEER_TYPE, peer));

        //this.setChanged();
        //this.notifyObservers(new ObserverMessage(ObserverMessage.LIST_PEER_TYPE, this.knownPeers));

        Controller.getInstance().onConnect(peer);

    }

    @Override
    public void tryDisconnect(Peer peer, int banForMinutes, String error) {

        if (!peer.isUsed())
            return;

        //CLOSE CONNECTION
        peer.close();

        if (banForMinutes != 0) {
            //ADD TO BLACKLIST
            PeerManager.getInstance().addPeer(peer, banForMinutes);
        }

        if (error != null && error.length() > 0) {
            if (banForMinutes != 0) {
                LOGGER.info(peer + " ban for minutes: " + banForMinutes + " - " + error);
            } else {
                LOGGER.info("tryDisconnect : " + peer + " - " + error);
            }
        }


        //PASS TO CONTROLLER
        Controller.getInstance().afterDisconnect(peer);

        //NOTIFY OBSERVERS
        this.setChanged();
        this.notifyObservers(new ObserverMessage(ObserverMessage.REMOVE_PEER_TYPE, peer));

        this.setChanged();
        this.notifyObservers(new ObserverMessage(ObserverMessage.LIST_PEER_TYPE, this.knownPeers));
    }

    @Override
    public boolean isKnownAddress(InetAddress address, boolean andUsed) {

        try {
            synchronized (this.knownPeers) {
                //FOR ALL connectedPeers
                for (Peer knownPeer : knownPeers) {
                    //CHECK IF ADDRESS IS THE SAME
                    if (address.equals(knownPeer.getAddress())) {
                        if (andUsed) {
                            return knownPeer.isUsed();
                        }
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            //LOGGER.error(e.getMessage(),e);
        }

        return false;
    }

	/*@Override
	public List<Peer> getKnownPeers() {
		
		return this.knownPeers;
	}
	*/

    @Override
    // IF PEER in exist in NETWORK - get it
    public Peer getKnownPeer(Peer peer) {

        try {
            byte[] address = peer.getAddress().getAddress();
            synchronized (this.knownPeers) {
                //FOR ALL connectedPeers
                for (Peer knownPeer : knownPeers) {
                    //CHECK IF ADDRESS IS THE SAME
                    if (Arrays.equals(address, knownPeer.getAddress().getAddress())) {
                        return knownPeer;
                    }
                }
            }
        } catch (Exception e) {
            //LOGGER.error(e.getMessage(),e);
        }

        return peer;
    }

    @Override
    public boolean isKnownPeer(Peer peer, boolean andUsed) {

        return this.isKnownAddress(peer.getAddress(), andUsed);
    }

    //
    public List<Peer> getActivePeers(boolean onlyWhite) {

        List<Peer> activePeers = new ArrayList<Peer>();
        synchronized (this.knownPeers) {
            for (Peer peer : this.knownPeers) {
                if (peer.isUsed())
                    if (!onlyWhite || peer.isWhite())
                        activePeers.add(peer);
            }
        }
        return activePeers;
    }

    public int getActivePeersCounter(boolean onlyWhite) {

        int counter = 0;
        synchronized (this.knownPeers) {
            for (Peer peer : this.knownPeers) {
                if (peer.isUsed())
                    if (!onlyWhite || peer.isWhite())
                        counter++;
            }
        }
        return counter;
    }

    public List<Peer> getIncomedPeers() {

        List<Peer> incomedPeers = new ArrayList<Peer>();
        synchronized (this.knownPeers) {
            for (Peer peer : this.knownPeers) {
                if (peer.isUsed())
                    if (!peer.isWhite())
                        incomedPeers.add(peer);
            }
        }
        return incomedPeers;
    }

    public boolean addTelegram(TelegramMessage telegram) {
        return this.telegramer.pipeAddRemove(telegram, null, 0);
    }

    public List<TelegramMessage> getTelegramsForAddress(String address, long timestamp, String filter) {
        return this.telegramer.getTelegramsForAddress(address, timestamp, filter);
    }

    public List<String> deleteTelegram(List<String> telegramSignatures) {
        return this.telegramer.deleteList(telegramSignatures);
    }

    public long deleteTelegramsToTimestamp(long timestamp, String recipient, String title) {
        return this.telegramer.deleteToTimestamp(timestamp, recipient, title);
    }
    public long deleteTelegramsForRecipient(String recipient, long timestamp, String title) {
        return this.telegramer.deleteForRecipient(recipient, timestamp, title);
    }

    public List<TelegramMessage> getTelegramsFromTimestamp(long timestamp, String recipient, String filter) {
        return this.telegramer.getTelegramsFromTimestamp(timestamp, recipient, filter);
    }
    //public TelegramMessage getTelegram64(String signature) {
    //	return this.telegramer.getTelegram64(signature);
    //}

    public TelegramMessage getTelegram(byte[] signature) {
        return this.telegramer.getTelegram(Base58.encode(signature));
    }

    public Integer TelegramInfo() {
        return this.telegramer.telegramCount();
    }
    public TelegramMessage getTelegram(String signature) {
        return this.telegramer.getTelegram(signature);
    }

    public Peer startPeer(Socket socket) {

        // REUSE known peer
        byte[] addressIP = socket.getInetAddress().getAddress();
        synchronized (this.knownPeers) {
            //FOR ALL connectedPeers
            for (Peer knownPeer : knownPeers) {
                //CHECK IF ADDRESS IS THE SAME
                if (Arrays.equals(addressIP, knownPeer.getAddress().getAddress())) {
                    knownPeer.reconnect(socket, "connected by restore!!! ");
                    return knownPeer;
                }
            }
        }

        // use UNUSED peers
        synchronized (this.knownPeers) {
            for (Peer knownPeer : this.knownPeers) {
                if (!knownPeer.isUsed()
                    //|| !Network.isMyself(knownPeer.getAddress())
                        ) {
                    knownPeer.reconnect(socket, "connected by recircle!!! ");
                    return knownPeer;
                }
            }
        }

        // ADD new peer
        // make NEW PEER and use empty slots

        Peer peer = new Peer(this, socket, "connected as new!!! ");
        LOGGER.info("connected as new!!! " + peer);
        // при коннекте во вне связь может порваться поэтому тут по runed
        ///onConnect(peer);

        return peer;

    }

    private void addHandledMessage(String hash) {
        try {
            synchronized (this.handledMessages) {
                //CHECK IF LIST IS FULL
                if (this.handledMessages.size() > MAX_HANDLED_MESSAGES_SIZE) {
                    this.handledMessages.remove(this.handledMessages.first());
                    ///LOGGER.error("handledMessages size OVERHEAT! ");
                }

                this.handledMessages.add(hash);
            }
        } catch (Exception e) {
            //LOGGER.error(e.getMessage(),e);
        }
    }

    @Override
    public void onMessage(Message message) {

        //CHECK IF WE ARE STILL PROCESSING MESSAGES
        if (!this.run) {
            return;
        }

        if (message.getType() == Message.TELEGRAM_TYPE) {

            if (!this.telegramer.pipeAddRemove((TelegramMessage) message, null, 0)) {
                // BROADCAST
                List<Peer> excludes = new ArrayList<Peer>();
                excludes.add(message.getSender());
                this.asyncBroadcast(message, excludes, false);
            }

            return;
        }
        
        // GET telegrams
        if(message.getType()== Message.TELEGRAM_GET_TYPE){
          //address
             JSONObject address = ((TelegramGetMessage) message).getAddress();
             // create ansver
             ArrayList<String> ca = new ArrayList<String>();
             Set keys = address.keySet();
             for(int i = 0; i<keys.size(); i++){
                  
                 ca.add((String) address.get(i));
             }
            Message answer = MessageFactory.getInstance().createTelegramGetAnswerMessage(ca);
            answer.setId(message.getId());
            // send answer
            message.getSender().sendMessage(answer);
           return;
           }
        // Ansver to get transaction
        if ( message.getType() == Message.TELEGRAM_GET_ANSWER_TYPE){
           ((TelegramGetAnswerMessage) message).saveToWallet();
            
            return; 
        }
        //ONLY HANDLE BLOCK AND TRANSACTION MESSAGES ONCE
        if (message.getType() == Message.TRANSACTION_TYPE
                || message.getType() == Message.BLOCK_TYPE
                || message.getType() == Message.WIN_BLOCK_TYPE
                ) {
            synchronized (this.handledMessages) {
                //CHECK IF NOT HANDLED ALREADY
                String key = new String(message.getHash());
                if (this.handledMessages.contains(key)) {
                    return;
                }

                //ADD TO HANDLED MESSAGES
                this.addHandledMessage(key);
            }
        }

        long timeCheck = System.currentTimeMillis();
        switch (message.getType()) {
            case Message.GET_HWEIGHT_TYPE:

                Tuple2<Integer, Long> HWeight = Controller.getInstance().getBlockChain().getHWeightFull(DCSet.getInstance());
                if (HWeight == null)
                    HWeight = new Tuple2<Integer, Long>(-1, -1L);

                Message response = MessageFactory.getInstance().createHWeightMessage(HWeight);
                // CREATE RESPONSE WITH SAME ID
                response.setId(message.getId());

                timeCheck = System.currentTimeMillis() - timeCheck;
                if (timeCheck > 10) {
                    LOGGER.debug(this + " : " + message + "["
                            + message.getId() + "] solved by period: " + timeCheck);
                }
                timeCheck = System.currentTimeMillis();

                //SEND BACK TO SENDER
                boolean result = message.getSender().sendMessage(response);
                if (!result) {
                    LOGGER.debug("error on response GET_HWEIGHT_TYPE to " + message.getSender());
                }

                timeCheck = System.currentTimeMillis() - timeCheck;
                if (timeCheck > 10) {
                    LOGGER.debug(this + " : " + message + "["
                            + message.getId() + "] solved by period: " + timeCheck);
                }

                break;

            //GETPEERS
            case Message.GET_PEERS_TYPE:

                //CREATE NEW PEERS MESSAGE WITH PEERS
                Message answer = MessageFactory.getInstance().createPeersMessage(PeerManager.getInstance().getBestPeers());
                answer.setId(message.getId());

                //SEND TO SENDER
                message.getSender().sendMessage(answer);

                timeCheck = System.currentTimeMillis() - timeCheck;
                if (timeCheck > 10) {
                    LOGGER.debug(this + " : " + message + "["
                            + message.getId() + "] solved by period: " + timeCheck);
                }

                break;


            case Message.FIND_MYSELF_TYPE:

                FindMyselfMessage findMyselfMessage = (FindMyselfMessage) message;

                if (Arrays.equals(findMyselfMessage.getFoundMyselfID(), Controller.getInstance().getFoundMyselfID())) {
                    //LOGGER.info("network.onMessage - Connected to self. Disconnection.");

                    Network.myselfAddress = message.getSender().getAddress();
                    tryDisconnect(message.getSender(), 99999, null);
                }

                break;

            //SEND TO CONTROLLER
            default:

                Controller.getInstance().onMessage(message);
                break;
        }
    }

    public void pingAllPeers(List<Peer> exclude, boolean onlySynchronized) {
        //LOGGER.debug("Broadcasting PING ALL");

        Controller cnt = Controller.getInstance();
        BlockChain chain = cnt.getBlockChain();
        Integer myHeight = chain.getHWeightFull(DCSet.getInstance()).a;
        Peer peer;
        Tuple2<Integer, Long> peerHWeight;

        for (int i = 0; i < this.knownPeers.size(); i++) {

            if (!this.run)
                return;

            peer = this.knownPeers.get(i);
            if (peer == null || !peer.isUsed()) {
                continue;
            }

            if (onlySynchronized) {
                // USE PEERS than SYNCHRONIZED to ME
                peerHWeight = cnt.getHWeightOfPeer(peer);
                if (peerHWeight == null || !peerHWeight.a.equals(myHeight)) {
                    continue;
                }
            }

            //EXCLUDE PEERS
            if (exclude == null || !exclude.contains(peer)) {
                peer.setNeedPing();
            }
        }

        peer = null;
        //LOGGER.debug("Broadcasting PING ALL end");
    }

    public void asyncBroadcastPing(Message message, List<Peer> exclude) {

        //LOGGER.debug("ASYNC Broadcasting with Ping before " + message.viewType());

        for (int i = 0; i < this.knownPeers.size(); i++) {

            if (!this.run)
                return;

            Peer peer = this.knownPeers.get(i);
            if (peer == null || !peer.isUsed()) {
                continue;
            }

            //EXCLUDE PEERS
            if (exclude == null || !exclude.contains(peer)) {
                if (true || message.getDataLength() > PINGED_MESSAGES_SIZE) {
                    //LOGGER.debug("PEER rty + Ping " + peer);
                    peer.setMessageQueuePing(message);
                } else {
                    //LOGGER.debug("PEER rty " + peer);
                    peer.setMessageQueue(message);
                }

            }
        }

        //LOGGER.debug("ASYNC Broadcasting with Ping before ENDED " + message.viewType());
    }

    public void asyncBroadcast(Message message, List<Peer> exclude, boolean onlySynchronized) {

        //LOGGER.debug("ASYNC Broadcasting " + message.viewType());
        Controller cnt = Controller.getInstance();
        BlockChain chain = cnt.getBlockChain();
        Integer myHeight = chain.getHWeightFull(DCSet.getInstance()).a;

        for (int i = 0; i < this.knownPeers.size(); i++) {

            if (!this.run)
                return;

            Peer peer = this.knownPeers.get(i);
            if (peer == null || !peer.isUsed()) {
                continue;
            }

            if (onlySynchronized) {
                // USE PEERS than SYNCHRONIZED to ME
                Tuple2<Integer, Long> peerHWeight = Controller.getInstance().getHWeightOfPeer(peer);
                if (peerHWeight == null || !peerHWeight.a.equals(myHeight)) {
                    continue;
                }
            }

            //EXCLUDE PEERS
            if (exclude == null || !exclude.contains(peer)) {
                peer.setMessageQueue(message);
            }
        }

        //LOGGER.debug("ASYNC Broadcasting end " + message.viewType());
    }

    public void broadcast(Message message, List<Peer> exclude, boolean onlySynchronized) {
        Controller cnt = Controller.getInstance();
        BlockChain chain = cnt.getBlockChain();
        Integer myHeight = chain.getHWeightFull(DCSet.getInstance()).a;

        for (int i = 0; i < this.knownPeers.size(); i++) {

            if (!this.run)
                return;

            Peer peer = this.knownPeers.get(i);
            if (peer == null || !peer.isUsed()) {
                continue;
            }

            if (onlySynchronized) {
                // USE PEERS than SYNCHRONIZED to ME
                Tuple2<Integer, Long> peerHWeight = Controller.getInstance().getHWeightOfPeer(peer);
                if (peerHWeight == null || !peerHWeight.a.equals(myHeight)) {
                    continue;
                }
            }

            //EXCLUDE PEERS
            if (exclude == null || !exclude.contains(peer)) {
                try {
                    peer.sendMessage(message);
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
        }
    }

    public void asyncBroadcastWinBlock(Message message, List<Peer> exclude, boolean onlySynchronized) {

        //LOGGER.debug("ASYNC Broadcasting " + message.viewType());
        Controller cnt = Controller.getInstance();
        BlockChain chain = cnt.getBlockChain();
        Integer myHeight = chain.getHWeightFull(DCSet.getInstance()).a;

        for (int i = 0; i < this.knownPeers.size(); i++) {

            if (!this.run)
                return;

            Peer peer = this.knownPeers.get(i);
            if (peer == null || !peer.isUsed()) {
                continue;
            }

            if (onlySynchronized) {
                // USE PEERS than SYNCHRONIZED to ME
                Tuple2<Integer, Long> peerHWeight = Controller.getInstance().getHWeightOfPeer(peer);
                if (peerHWeight == null || !peerHWeight.a.equals(myHeight)) {
                    continue;
                }
            }

            //EXCLUDE PEERS
            if (exclude == null || !exclude.contains(peer)) {
                peer.setMessageWinBlock(message);
            }
        }

        //LOGGER.debug("ASYNC Broadcasting end " + message.viewType());
    }

    @Override
    public void addObserver(Observer o) {
        super.addObserver(o);

        //SEND CONNECTEDPEERS ON REGISTER
        o.update(this, new ObserverMessage(ObserverMessage.LIST_PEER_TYPE, this.knownPeers));
    }

    public void notifyObserveUpdatePeer(Peer peer) {
        //NOTIFY OBSERVERS
        this.setChanged();
        this.notifyObservers(new ObserverMessage(ObserverMessage.UPDATE_PEER_TYPE, peer));

    }

    public void stop() {

        // stop thread
        this.creator.halt();

        // stop thread
        this.acceptor.halt();

        this.telegramer.halt();

        this.run = false;
        this.onMessage(null);
        int size = knownPeers.size();

        for (int i =0; i<size; i++){
           // HALT Peer
            knownPeers.get(i).halt();
            if (false) {
                try {
                    knownPeers.get(i).join();
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
        }

        knownPeers.clear();
        // wait for thread stop;
        while (this.acceptor.isAlive()) ;
    }
}
