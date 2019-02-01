package org.erachain.network;

import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.core.crypto.Base58;
import org.erachain.datachain.DCSet;
import org.erachain.network.message.*;
import org.erachain.ntp.NTP;
import org.erachain.settings.Settings;
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
public class Network extends Observable {

    public static final int SEND_WAIT = 20000;
    public static final int PEER_SLEEP_TIME = BlockChain.HARD_WORK ? 0 : 1;
    private static final int MAX_HANDLED_MESSAGES_SIZE = BlockChain.HARD_WORK ? 1024 << 8 : 1024<<4;
    private static final int PINGED_MESSAGES_SIZE = BlockChain.HARD_WORK ? 1024 << 12 : 1024 << 8;
    private static final Logger LOGGER = LoggerFactory.getLogger(Network.class);
    private static InetAddress myselfAddress;
    private ConnectionCreator creator;
    private ConnectionAcceptor acceptor;
    PeerManager peerManager;
    private TelegramManager telegramer;
    List<Peer> knownPeers;
    private SortedSet<String> handledMessages;
    //boolean tryRun; // попытка запуска
    boolean run;

    public static final int WHITE_TYPE = 1;
    public static final int NON_WHITE_TYPE = -1;
    public static final int ANY_TYPE = 0;


    public Network() {
        this.knownPeers = new ArrayList<Peer>();
        this.run = true;

        this.start();
    }

    public static InetAddress getMyselfAddress() {
        return myselfAddress;
    }

    public ConnectionAcceptor getAcceptor() {
        return acceptor;
    }

    public ConnectionCreator getCreator() {
        return creator;
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

        peerManager = new PeerManager(this);
        peerManager.start();

        telegramer = new TelegramManager(this);
        telegramer.start();
    }

    public void onConnect(Peer peer) {

        if (!run)
            return;

        //LOGGER.info(Lang.getInstance().translate("Connection successfull : ") + peer);

        boolean asNew = true;
        synchronized (this.knownPeers) {
            for (Peer peerKnown : this.knownPeers) {
                if (//peer.equals(peerKnown)
                        // новый поток мог быть создан - поэтому хдесь провереи его
                        //peer.isAlive()
                        peer.getId() == peerKnown.getId()) {
                    asNew = false;
                    break;
                }
            }
            if (asNew) {
                //ADD TO CONNECTED PEERS
                this.knownPeers.add(peer);
            }
        }

        //ADD TO DATABASE
        peerManager.addPeer(peer, 0);

        if (!run)
            return;

        //NOTIFY OBSERVERS
        this.setChanged();
        this.notifyObservers(new ObserverMessage(ObserverMessage.ADD_PEER_TYPE, peer));

    }

    public void afterDisconnect(Peer peer, int banForMinutes, String message) {

        if (!run)
            return;

        if (message != null && message.length() > 0) {
            if (banForMinutes > 0) {
                LOGGER.info("BANed: " + peer + " for: " + banForMinutes + "[min] - " + message);
            } else {
                LOGGER.info("disconnected: " + peer + " - " + message);
            }
        }

        if (banForMinutes > peer.getBanMinutes()) {
            //ADD TO BLACKLIST
            peerManager.addPeer(peer, banForMinutes);
        }

        //PASS TO CONTROLLER
        Controller.getInstance().afterDisconnect(peer);

        //NOTIFY OBSERVERS
        this.setChanged();
        this.notifyObservers(new ObserverMessage(ObserverMessage.REMOVE_PEER_TYPE, peer));

        this.setChanged();
        this.notifyObservers(new ObserverMessage(ObserverMessage.LIST_PEER_TYPE, this.knownPeers));
    }

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

    // IF PEER in exist in NETWORK - get it
    public Peer getKnownPeer(Peer peer, int type) {

        Peer knowmPeer = null;
        try {
            byte[] address = peer.getAddress().getAddress();
            synchronized (this.knownPeers) {
                //FOR ALL connectedPeers
                for (Peer knownPeer : knownPeers) {
                    //CHECK IF ADDRESS IS THE SAME
                    if (Arrays.equals(address, knownPeer.getAddress().getAddress())
                            && (type == ANY_TYPE || type == WHITE_TYPE && knownPeer.isWhite()
                                    || !knowmPeer.isWhite())
                    ) {
                        // иначе тут не сработате правильно org.erachain.network.Network.onConnect
                        // поэтому сразу выдаем первый что нашли без каких либо условий
                        return knownPeer;
                    }
                }
            }
        } catch (Exception e) {
            //LOGGER.error(e.getMessage(),e);
        }

        return peer;
    }

    // IF PEER in exist in NETWORK - get it
    public Peer getKnownWhitePeer(byte[] addressIP) {

        synchronized (this.knownPeers) {
            //FOR ALL connectedPeers
            for (Peer knownPeer : knownPeers) {
                //CHECK IF ADDRESS IS THE SAME
                if (knownPeer.isWhite() && Arrays.equals(addressIP, knownPeer.getAddress().getAddress())) {
                    return knownPeer;
                }
            }
        }

        return null;
    }

    // IF PEER in exist in NETWORK - get it
    public Peer getKnownNonWhitePeer(byte[] addressIP) {

        synchronized (this.knownPeers) {
            //FOR ALL connectedPeers
            for (Peer knownPeer : knownPeers) {
                //CHECK IF ADDRESS IS THE SAME
                if (!knownPeer.isWhite() && Arrays.equals(addressIP, knownPeer.getAddress().getAddress())) {
                    return knownPeer;
                }
            }
        }

        return null;
    }

    public boolean isKnownPeer(Peer peer, boolean andUsed) {

        return this.isKnownAddress(peer.getAddress(), andUsed);
    }

    public boolean isGoodForConnect(Peer peer) {

        if (peer.isOnUsed() || peer.isUsed() || peer.isBanned())
            return false;

        //CHECK IF ALREADY CONNECTED TO PEER
        byte[] address = peer.getAddress().getAddress();
        synchronized (this.knownPeers) {
            //FOR ALL connectedPeers
            for (Peer knownPeer : this.knownPeers) {
                //CHECK IF ADDRESS IS THE SAME
                if (!knownPeer.isAlive()
                        || peer.getId() == knownPeer.getId()
                        || !Arrays.equals(address, knownPeer.getAddress().getAddress())
                        || NTP.getTime() - knownPeer.getConnectionTime() < 1000
                )
                    continue;

                return false;
            }
        }

        return true;
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

    public List<Peer> getBestPeers() {
        return this.peerManager.getBestPeers();
    }

    public List<Peer> getKnownPeers() {
        List<Peer> knownPeers = new ArrayList<Peer>();
        //ASK DATABASE FOR A LIST OF PEERS
        if (!Controller.getInstance().isOnStopping()) {
            knownPeers = Controller.getInstance().getDBSet().getPeerMap().getBestPeers(
                    0, true);
        }

        //RETURN
        return knownPeers;
    }

    public void addPeer(Peer peer, int banForMinutes) {
        this.peerManager.addPeer(peer, banForMinutes);
    }

    /**
     * Выдает число минут для бана пира у котрого ошибка в канале:
     *  если соединений очень мало - то не банить, если сединений не максимум то банить на небольшой срок,
     *  иначе бан на 10 минут если осталось только 3 свободных места
     * @return
     */
    public int banForActivePeersCounter() {

        int active = getActivePeersCounter(false);
        int difference = Settings.getInstance().getMinConnections() - active;
        if (difference > 0)
            return 0;

        difference = Settings.getInstance().getMaxConnections() - active;
        if (difference < 3) {
            return 10;
        }
        return 3;
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

    /**
     * запускает Пир на входящее соединение
     *
     * @param socket
     * @return
     */
    public Peer startPeer(Socket socket) {

        byte[] addressIP = socket.getInetAddress().getAddress();
        // REUSE known peer
        synchronized (this.knownPeers) {
            //FOR ALL connectedPeers
            for (Peer knownPeer : knownPeers) {
                //CHECK IF ADDRESS IS THE SAME
                if (Arrays.equals(addressIP, knownPeer.getAddress().getAddress())) {

                    if (knownPeer.isUsed()) {
                        knownPeer.close("before accept anew");
                    }
                    // IF PEER not USED and not onUSED
                    knownPeer.connect(socket, this,"connected by restore!!! ");
                    return knownPeer;
                }
            }
        }

        // Если пустых мест уже мало то начинаем переиспользовать
        if (this.getActivePeersCounter(false) + 3 > Settings.getInstance().getMaxConnections() ) {
            // use UNUSED peers
            synchronized (this.knownPeers) {
                for (Peer knownPeer : this.knownPeers) {
                    if (!knownPeer.isOnUsed() && !knownPeer.isUsed()) {
                        knownPeer.connect(socket, this, "connected by recircle!!! ");
                        return knownPeer;
                    }
                }
            }
        }

        // ADD new peer
        // make NEW PEER and use empty slots

        Peer peer = new Peer(this, socket, "connected as new!!! ");
        // запомним в базе данных
        onConnect(peer);

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

    public void onMessagePeers(Peer sender, int messageID) {

        //CREATE NEW PEERS MESSAGE WITH PEERS
        Message answer = MessageFactory.getInstance().createPeersMessage(PeerManager.getInstance().getBestPeers());
        answer.setId(messageID);

        //SEND TO SENDER
        sender.sendMessage(answer);

    }

    public void onMessageMySelf(Peer sender, byte[] remoteID) {

        if (Arrays.equals(remoteID, Controller.getInstance().getFoundMyselfID())) {
            //LOGGER.info("network.onMessage - Connected to self. Disconnection.");

            Network.myselfAddress = sender.getAddress();
            tryDisconnect(sender, 99999, null);
        }

    }
    public void onMessage(Message message) {

        //CHECK IF WE ARE STILL PROCESSING MESSAGES
        if (!this.run) {
            return;
        }

        //ONLY HANDLE WINBLOCK, TELEGRAMS AND TRANSACTION MESSAGES ONCE
        if (
                message.getType() == Message.TELEGRAM_TYPE
                        || message.getType() == Message.TRANSACTION_TYPE
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
            case Message.TELEGRAM_TYPE:
                // telegram
                if (!this.telegramer.pipeAddRemove((TelegramMessage) message, null, 0)) {
                    // BROADCAST
                    List<Peer> excludes = new ArrayList<Peer>();
                    excludes.add(message.getSender());
                    this.broadcast(message, excludes, false);
                }

                return;
            case Message.TELEGRAM_GET_TYPE:
                // GET telegrams
                //address
                JSONObject address = ((TelegramGetMessage) message).getAddress();
                // create ansver
                ArrayList<String> addressFilter = new ArrayList<String>();
                Set keys = address.keySet();
                for (int i = 0; i < keys.size(); i++) {

                    addressFilter.add((String) address.get(i));
                }
                Message answer = MessageFactory.getInstance().createTelegramGetAnswerMessage(addressFilter);
                answer.setId(message.getId());
                // send answer
                message.getSender().offerMessage(answer);
                return;

            case Message.TELEGRAM_ANSWER_TYPE:
                // Answer to get telegrams
                ((TelegramAnswerMessage) message).saveToWallet();

                return;

            case Message.GET_HWEIGHT_TYPE:

                Tuple2<Integer, Long> HWeight = Controller.getInstance().getBlockChain().getHWeightFull(DCSet.getInstance());
                if (HWeight == null)
                    HWeight = new Tuple2<Integer, Long>(-1, -1L);

                HWeightMessage response = (HWeightMessage) MessageFactory.getInstance().createHWeightMessage(HWeight);
                // CREATE RESPONSE WITH SAME ID
                response.setId(message.getId());

                timeCheck = System.currentTimeMillis() - timeCheck;
                if (true || timeCheck > 10) {
                    LOGGER.debug(message.getSender() + ": " + message + " solver by period: " + timeCheck);
                }
                timeCheck = System.currentTimeMillis();

                //SEND BACK TO SENDER
                if (false)
                    message.getSender().sendHWeight(response);
                else
                    message.getSender().offerMessage(response);

                timeCheck = System.currentTimeMillis() - timeCheck;
                if (true || timeCheck > 10) {
                    LOGGER.debug(message.getSender() + ": " + message + " >>> by period: " + timeCheck);
                }

                break;

            //GETPEERS
            case Message.GET_PEERS_TYPE:

                onMessagePeers(message.getSender(), message.getId());

                break;


            case Message.FIND_MYSELF_TYPE:

                FindMyselfMessage findMyselfMessage = (FindMyselfMessage) message;

                onMessageMySelf(message.getSender(), findMyselfMessage.getFoundMyselfID());

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

        //LOGGER.debug("Broadcasting PING ALL end");
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
                    peer.offerMessage(message);
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
        }
    }

    public void asyncBroadcastWinBlock(BlockWinMessage winBlock, List<Peer> exclude, boolean onlySynchronized) {

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
                peer.sendWinBlock(winBlock);
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

        this.run = false;

        // stop thread
        this.creator.halt();

        // stop thread
        this.acceptor.halt();

        //
        this.peerManager.halt();

        this.telegramer.halt();

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
        LOGGER.info("halted");

    }

}
