package org.erachain.network;

import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.core.crypto.Base58;
import org.erachain.datachain.DCSet;
import org.erachain.network.message.*;
import org.erachain.ntp.NTP;
import org.erachain.settings.Settings;
import org.erachain.utils.ObserverMessage;
import org.mapdb.Fun.Tuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

/**
 * основной класс модуля Сети
 */
public class Network extends Observable {

    public static final int SEND_WAIT = 20000;
    public static final int PEER_SLEEP_TIME = BlockChain.HARD_WORK ? 0 : 1;
    private static final int MAX_HANDLED_TELEGRAM_MESSAGES_SIZE = BlockChain.HARD_WORK ? 1024 << 8 : 1024 << 3;
    private static final int MAX_HANDLED_TRANSACTION_MESSAGES_SIZE = BlockChain.HARD_WORK ? 1024 << 6 : 1024 << 1;
    private static final int MAX_HANDLED_WIN_BLOCK_MESSAGES_SIZE = BlockChain.HARD_WORK ? 100 : 300;
    private static final Logger LOGGER = LoggerFactory.getLogger(Network.class);

    private Controller controller;
    private static InetAddress myselfAddress;
    private ConnectionCreator creator;
    private ConnectionAcceptor acceptor;
    private MessagesProcessor messagesProcessor;
    PeerManager peerManager;
    public TelegramManager telegramer;
    CopyOnWriteArrayList<Peer> knownPeers;

    //private SortedSet<String> handledTelegramMessages;
    private HandledMap<Long, Set<Peer>> handledTelegramMessages;
    private HandledMap<Long, Set<Peer>> handledTransactionMessages;
    private HandledMap<Integer, Set<Peer>> handledWinBlockMessages;

    public AtomicLong missedSendes = new AtomicLong(0);
    public AtomicLong missedTelegrams = new AtomicLong(0);
    public AtomicLong missedTransactions = new AtomicLong(0);
    public AtomicLong missedWinBlocks = new AtomicLong(0);
    public AtomicLong missedMessages = new AtomicLong(0);

    //boolean tryRun; // попытка запуска
    boolean run;

    public static final int WHITE_TYPE = 1;
    public static final int NON_WHITE_TYPE = -1;
    public static final int ANY_TYPE = 0;


    public Network(Controller controller) {
        this.controller = controller;

        this.knownPeers = new CopyOnWriteArrayList<Peer>();

        this.handledTelegramMessages = new HandledMap<Long, Set<Peer>>(MAX_HANDLED_TELEGRAM_MESSAGES_SIZE);
        this.handledTransactionMessages = new HandledMap<Long, Set<Peer>>(MAX_HANDLED_TRANSACTION_MESSAGES_SIZE);
        this.handledWinBlockMessages = new HandledMap<Integer, Set<Peer>>(MAX_HANDLED_WIN_BLOCK_MESSAGES_SIZE);

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


        //START ConnectionCreator THREAD
        creator = new ConnectionCreator(this);
        creator.start();

        //START ConnectionAcceptor THREAD
        acceptor = new ConnectionAcceptor(this);
        acceptor.start();

        peerManager = new PeerManager(this);
        peerManager.start();

        telegramer = new TelegramManager(controller,
                controller.getBlockChain(),
                DCSet.getInstance(),
                this);

        this.messagesProcessor = new MessagesProcessor(this);

    }

    public void onConnect(Peer peer) {

        if (!run) {
            peer.close("network is stopped");
            return;
        }

        //LOGGER.info(Lang.getInstance().translate("Connection successfull : ") + peer);

        boolean asNew = true;
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
        controller.afterDisconnect(peer);

        //NOTIFY OBSERVERS
        this.setChanged();
        this.notifyObservers(new ObserverMessage(ObserverMessage.REMOVE_PEER_TYPE, peer));

        this.setChanged();
        this.notifyObservers(new ObserverMessage(ObserverMessage.LIST_PEER_TYPE, this.knownPeers));
    }

    public boolean isKnownAddress(InetAddress address, boolean andUsed) {

        try {
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
        } catch (Exception e) {
            //LOGGER.error(e.getMessage(),e);
        }

        return peer;
    }

    // IF PEER in exist in NETWORK - get it
    public Peer getKnownWhitePeer(byte[] addressIP) {

        //FOR ALL connectedPeers
        for (Peer knownPeer : knownPeers) {
            //CHECK IF ADDRESS IS THE SAME
            if (knownPeer.isWhite() && Arrays.equals(addressIP, knownPeer.getAddress().getAddress())) {
                return knownPeer;
            }
        }

        return null;
    }

    // IF PEER in exist in NETWORK - get it
    public Peer getKnownNonWhitePeer(byte[] addressIP) {

        //FOR ALL connectedPeers
        for (Peer knownPeer : knownPeers) {
            //CHECK IF ADDRESS IS THE SAME
            if (!knownPeer.isWhite() && Arrays.equals(addressIP, knownPeer.getAddress().getAddress())) {
                return knownPeer;
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

        return true;
    }

    //
    public List<Peer> getActivePeers(boolean onlyWhite) {

        List<Peer> activePeers = new ArrayList<Peer>();
        for (Peer peer : this.knownPeers) {
            if (peer.isUsed())
                if (!onlyWhite || peer.isWhite())
                    activePeers.add(peer);
        }
        return activePeers;
    }

    public int getActivePeersCounter(boolean onlyWhite) {

        int counter = 0;
        for (Peer peer : this.knownPeers) {
            if (peer.isUsed())
                if (!onlyWhite || peer.isWhite())
                    counter++;
        }
        return counter;
    }

    public List<Peer> getBestPeers() {
        return this.peerManager.getBestPeers();
    }

    public List<Peer> getKnownPeers() {
        List<Peer> knownPeers = new ArrayList<Peer>();
        //ASK DATABASE FOR A LIST OF PEERS
        if (!controller.isOnStopping()) {
            knownPeers = controller.getDBSet().getPeerMap().getBestPeers(
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
        for (Peer peer : this.knownPeers) {
            if (peer.isUsed())
                if (!peer.isWhite())
                    incomedPeers.add(peer);
        }
        return incomedPeers;
    }

    public boolean addTelegram(TelegramMessage telegram) {
        return this.telegramer.add(telegram);
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
        //FOR ALL connectedPeers
        for (Peer knownPeer : knownPeers) {
            //CHECK IF ADDRESS IS THE SAME
            if (Arrays.equals(addressIP, knownPeer.getAddress().getAddress())) {

                if (knownPeer.isUsed()) {
                    knownPeer.close("before accept anew");
                }
                // IF PEER not USED and not onUSED
                knownPeer.connect(socket, this, "connected by restore!!! ");
                return knownPeer;
            }
        }

        // Если пустых мест уже мало то начинаем переиспользовать
        if (this.getActivePeersCounter(false) + 3 > Settings.getInstance().getMaxConnections() ) {
            // use UNUSED peers
            for (Peer knownPeer : this.knownPeers) {
                if (!knownPeer.isOnUsed() && !knownPeer.isUsed()) {
                    knownPeer.connect(socket, this, "connected by recircle!!! ");
                    return knownPeer;
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

    // берем подпись с трнзакции и трансформируем в Целое  исразу проверяем - есть ли?
    public boolean checkHandledTelegramMessages(byte[] data, Peer sender, boolean forThisPeer) {

        Long key = TelegramMessage.getHandledID(data);

        //ADD TO HANDLED MESSAGES
        if (this.handledTelegramMessages.addHandledItem(key, sender, forThisPeer)) {
            return true;
        }

        return false;

    }

    // берем подпись с трнзакции и трансформируем в Целое  исразу проверяем - есть ли?
    public boolean checkHandledTransactionMessages(byte[] data, Peer sender, boolean forThisPeer) {

        Long key = TransactionMessage.getHandledID(data);

        //ADD TO HANDLED MESSAGES
        if (this.handledTransactionMessages.addHandledItem(key, sender, forThisPeer)) {
            return true;
        }

        return false;

    }

    // берем подпись с трнзакции и трансформируем в Целое  исразу проверяем - есть ли?
    public boolean checkHandledWinBlockMessages(byte[] data, Peer sender, boolean forThisPeer) {

        // KEY BY CREATOR
        Integer key = BlockWinMessage.getHandledID(data);

        //ADD TO HANDLED MESSAGES
        if (this.handledWinBlockMessages.addHandledItem(key, sender, forThisPeer)) {
            return true;
        }

        return false;

    }

    // очишаем потихоньку
    public void clearHandledTelegramMessages() {
        int size = handledTelegramMessages.size();
        if (size < MAX_HANDLED_TELEGRAM_MESSAGES_SIZE >> 4)
            size >>= 1;
        else
            size >>= 3;

        for (int i = 0; i < size; i++)
            this.handledTelegramMessages.removeFirst();

    }

    // очишаем потихоньку
    public void clearHandledTransactionMessages() {
        int size = handledTransactionMessages.size();
        if (size < MAX_HANDLED_TRANSACTION_MESSAGES_SIZE >> 4)
            size >>= 1;
        else
            size >>= 3;

        for (int i = 0; i < size; i++)
            this.handledTransactionMessages.removeFirst();

    }

    public void clearHandledWinBlockMessages() {
        handledWinBlockMessages.clear();
        clearHandledTransactionMessages();
        clearHandledTelegramMessages();
    }

    public void onMessagePeers(Peer sender, int messageID) {

        //CREATE NEW PEERS MESSAGE WITH PEERS
        Message answer = MessageFactory.getInstance().createPeersMessage(peerManager.getBestPeers());
        answer.setId(messageID);

        //SEND TO SENDER
        sender.offerMessage(answer);

    }

    public void onMessageMySelf(Peer sender, byte[] remoteID) {

        if (Arrays.equals(remoteID, controller.getFoundMyselfID())) {
            //LOGGER.info("network.onMessage - Connected to self. Disconnection.");

            Network.myselfAddress = sender.getAddress();
            sender.ban(99999, null);
        }

    }
    public void onMessage(Message message) {

        //CHECK IF WE ARE STILL PROCESSING MESSAGES
        if (!this.run) {
            return;
        }

        switch (message.getType()) {

            case Message.TELEGRAM_TYPE:

                this.telegramer.offerMessage(message);

                return;

            case Message.TRANSACTION_TYPE:

                controller.transactionsPool.offerMessage(message);

                return;

            case Message.WIN_BLOCK_TYPE:

                controller.winBlockSelector.offerMessage(message);

                return;

            case Message.GET_BLOCK_TYPE:

                controller.blockRequester.offerMessage(message);

                return;

            default:

                this.messagesProcessor.offerMessage(message);
        }
    }

    public void pingAllPeers(boolean onlySynchronized) {
        //LOGGER.debug("Broadcasting PING ALL");

        BlockChain chain = controller.getBlockChain();
        Integer myHeight = chain.getHWeightFull(DCSet.getInstance()).a;
        Tuple2<Integer, Long> peerHWeight;

        for (Peer peer : this.knownPeers) {

            if (!this.run)
                return;

            if (peer == null || !peer.isUsed()) {
                continue;
            }

            if (onlySynchronized) {
                // USE PEERS than SYNCHRONIZED to ME
                peerHWeight = controller.getHWeightOfPeer(peer);
                if (peerHWeight == null || !peerHWeight.a.equals(myHeight)) {
                    continue;
                }
            }

            peer.setNeedPing();

        }

        //LOGGER.debug("Broadcasting PING ALL end");
    }

    public void broadcast(Message message, boolean onlySynchronized) {
        BlockChain chain = controller.getBlockChain();
        Integer myHeight = chain.getHWeightFull(DCSet.getInstance()).a;

        HashSet exclude;
        if (message.isHandled()) {

            switch (message.getType()) {
                case Message.TELEGRAM_TYPE:
                    // может быть это повтор?
                    exclude = (HashSet<Peer>)this.handledTelegramMessages.get(message.getHandledID());
                    break;
                case Message.TRANSACTION_TYPE:
                    // может быть это повтор?
                    exclude = (HashSet<Peer>)this.handledTransactionMessages.get(message.getHandledID());
                    break;
                case Message.WIN_BLOCK_TYPE:
                    // может быть это повтор?
                    exclude = (HashSet<Peer>)this.handledWinBlockMessages.get(message.getHandledID());
                    break;
                default:
                    exclude = null;
            }
        } else {
            exclude = null;
        }

        //if (exclude != null && !exclude.isEmpty())
        //    LOGGER.debug(message + " exclude: " + exclude.size());

        for (Peer peer : this.knownPeers) {

            if (!this.run)
                return;

            if (peer == null || !peer.isUsed()) {
                continue;
            }

            if (onlySynchronized) {
                // USE PEERS than SYNCHRONIZED to ME
                Tuple2<Integer, Long> peerHWeight = controller.getHWeightOfPeer(peer);
                if (peerHWeight == null || !peerHWeight.a.equals(myHeight)) {
                    continue;
                }
            }

            //EXCLUDE PEERS
            if (exclude == null || exclude.isEmpty() || !exclude.contains(peer)) {
                peer.offerMessage(message);
            }
        }
    }

    public void broadcastWinBlock(BlockWinMessage winBlock, boolean onlySynchronized) {

        BlockChain chain = controller.getBlockChain();
        Integer myHeight = chain.getHWeightFull(DCSet.getInstance()).a;

        HashSet<Peer> exclude = (HashSet<Peer>)this.handledWinBlockMessages.get(winBlock.getHandledID());

        //if (exclude != null && !exclude.isEmpty())
        //    LOGGER.debug(winBlock + " exclude: " + exclude.size());

        for (Peer peer : this.knownPeers) {

            if (!this.run)
                return;

            if (peer == null || !peer.isUsed()) {
                continue;
            }

            if (onlySynchronized) {
                // USE PEERS than SYNCHRONIZED to ME
                Tuple2<Integer, Long> peerHWeight = controller.getHWeightOfPeer(peer);
                if (peerHWeight == null || !peerHWeight.a.equals(myHeight)) {
                    continue;
                }
            }

            //EXCLUDE PEERS
            if (exclude == null || exclude.isEmpty() || !exclude.contains(peer)) {
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

        // STOP MESSAGES PROCESSOR
        messagesProcessor.halt();

        // stop thread
        this.creator.halt();

        // stop thread
        this.acceptor.halt();

        //
        this.peerManager.halt();

        this.telegramer.halt();

        for (Peer peer : knownPeers) {
            // HALT Peer
            peer.halt();
        }

        knownPeers.clear();
        LOGGER.info("halted");

    }

}
