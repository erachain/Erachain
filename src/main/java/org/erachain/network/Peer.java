package org.erachain.network;

import org.erachain.controller.Controller;
import org.erachain.core.BlockBuffer;
import org.erachain.core.BlockChain;
import org.erachain.database.DLSet;
import org.erachain.database.PeerMap;
import org.erachain.lang.Lang;
import org.erachain.network.message.*;
import org.erachain.ntp.NTP;
import org.erachain.settings.Settings;
import org.erachain.utils.DateTimeFormat;
import org.erachain.utils.MonitoredThread;
import org.json.simple.JSONObject;
import org.mapdb.Fun;
import org.mapdb.Fun.Tuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * верт (процесс)
 * вертает общение с внешним пиром - чтение и запись
 */
public class Peer extends MonitoredThread {

    private final static boolean USE_MONITOR = false;
    /**
     * <..... - receive просроченный ответ на Мо запрос<br>
     * see -  org.erachain.network.message.Message#viewPref(boolean)
     */
    public final static boolean LOG_GET_HWEIGHT_TYPE = false; // "185.195.26.245"
    private byte[] DEBUG_PEER = new byte[]{(byte) 185, (byte) 195, (byte) 26, (byte) 245};

    static Logger LOGGER = LoggerFactory.getLogger(Peer.class.getSimpleName());
    // Слишком бльшой буфер позволяет много посылок накидать не ожидая их приема. Но запросы с возратом остаются в очереди на долго
    // поэтому нужно ожидание дольще делать
    private static int SOCKET_BUFFER_SIZE = 1024 << (10 + Controller.HARD_WORK);
    private static int MAX_BEFORE_PING = SOCKET_BUFFER_SIZE >> 2;
    public Network network;
    private InetAddress address;
    public Socket socket;
    public BlockBuffer blockBuffer;

    BlockingQueue<Object> startReading = new ArrayBlockingQueue<Object>(1);

    private Sender sender;
    private Pinger pinger;

    private boolean white;
    private long pingCounter;
    private long connectionTime;
    private boolean runed;
    private int errors;
    Map<Integer, BlockingQueue<Message>> requests;
    private int requestKey;

    private String version = "";
    private JSONObject nodeInfo;
    private long buildDateTime;
    private String banMessage;
    private Tuple2<Integer, Long> hWeight;
    private long correctWeight;
    private int mute;
    private AtomicInteger requestKeyAtomic = new AtomicInteger(0);

    public Peer(InetAddress address) {
        this.address = address;
        this.requests = new ConcurrentHashMap<Integer, BlockingQueue<Message>>(256, 1);
        this.setName("Peer-" + this.getId() + " as address " + address.getHostAddress());

    }

    /**
     *  при коннекте во вне связь может порваться поэтому надо
     *  сделать проверку песле выхода по isUsed
     * @param network
     * @param socket
     * @param description
     */

    public Peer(Network network, Socket socket, String description) {

        //logger.debug("@@@ new Peer(ConnectionCallback callback, Socket socket) : " + socket.getInetAddress().getHostAddress());

        try {
            this.network = network;
            this.socket = socket;
            this.address = socket.getInetAddress();
            this.requests = new ConcurrentHashMap<Integer, BlockingQueue<Message>>(256, 1);
            this.white = false;
            this.pingCounter = 0;
            this.connectionTime = NTP.getTime();
            this.errors = 0;

            //ENABLE KEEPALIVE
            this.socket.setKeepAlive(true);

            //TIMEOUT - нужно для разрыва соединения если на том конце не стали нам слать нужные байты
            this.socket.setSoTimeout(Settings.getInstance().getConnectionTimeout());

            this.socket.setReceiveBufferSize(SOCKET_BUFFER_SIZE);
            this.socket.setSendBufferSize(SOCKET_BUFFER_SIZE);

            // NEED for NEW SENDER and PINGER
            this.setName("Peer-" + this.getId() + " new <<< " + address.getHostAddress());

            // START SENDER
            this.sender = new Sender(this, socket.getOutputStream());

            // START PINGER
            this.pinger = new Pinger(this);

            //START COMMUNICATION THREAD
            this.start();

            // IT is STARTED
            this.runed = true;

            // START READING
            this.startReading.offer(new DataInputStream(socket.getInputStream()));

            LOGGER.info(description + address.getHostAddress());

        } catch (Exception e) {
            //FAILED TO CONNECT NO NEED TO BLACKLIST

            this.runed = false;

            LOGGER.info("Failed to connect to : " + address.getHostAddress());
            LOGGER.error(e.getMessage(), e);

        }

    }

    /**
     * synchronized - дает результат хоть и медленный
     * Приконнектиться к Премнику или принять на этот Пир новый входящий Сокет
     * @param acceptedSocket если задан то это прием в данный Пир соединение извне
     * @param networkIn
     * @param description
     * @return
     */
    public synchronized boolean connect(Socket acceptedSocket, Network networkIn, String description) {
        if (Controller.getInstance().isOnStopping()) {
            return false;
        }

        if (this.socket != null) {
            LOGGER.debug("ALREADY connected: " + this);
            return this.runed;
        }

        if (networkIn != null)
            this.network = networkIn;

        this.requests = new ConcurrentHashMap<Integer, BlockingQueue<Message>>(256, 1);
        this.pingCounter = 0;
        this.connectionTime = NTP.getTime();
        this.errors = 0;

        this.hWeight = null;
        this.correctWeight = 0;
        this.mute = 0;

        int step = 0;
        try {
            //OPEN SOCKET
            step++;

            if (acceptedSocket != null) {
                // ME is ACCEPTOR
                this.socket = acceptedSocket;
                this.address = this.socket.getInetAddress();
                this.white = false;
                this.setName("Peer-" + this.getId() + " <<< " + address.getHostAddress());
            } else {
                this.socket = new Socket(address, BlockChain.NETWORK_PORT);
                this.white = true;
                this.setName("Peer-" + this.getId() + " >>> " + address.getHostAddress());

            }

            //ENABLE KEEPALIVE
            step++;
            this.socket.setKeepAlive(true);

            //TIMEOUT - нужно для разрыва соединения если на том конце не стали нам слать нужные байты
            this.socket.setSoTimeout(Settings.getInstance().getConnectionTimeout());

            this.socket.setReceiveBufferSize(SOCKET_BUFFER_SIZE);
            this.socket.setSendBufferSize(SOCKET_BUFFER_SIZE);

        } catch (Exception e) {
            //FAILED TO CONNECT NO NEED TO BLACKLIST
            if (step != 1) {
                LOGGER.error(e.getMessage(), e);
                LOGGER.debug("Failed to connect to : " + address.getHostAddress() + " on step: " + step);
            }

            return false;

        }

        //START SENDER and PINGER
        if (this.sender == null) {
            // если они еще не созданы - значит это пустой объект и его тоже нужно стартануть
            try {
                this.sender = new Sender(this, this.socket.getOutputStream());
            } catch (IOException e) {
                this.sender = null;
                return false;
            }

            this.pinger = new Pinger(this);

            //START COMMUNICATON THREAD
            this.start();

        } else {
            try {
                this.sender.setOut(this.socket.getOutputStream());
            } catch (IOException e) {
                return false;
            }

            this.pinger.setName("Pinger-" + this.pinger.getId() + " for: " + this.getName());
            this.pinger.setPing(Integer.MAX_VALUE);

        }

        this.runed = true;

        // START READING
        try {
            this.startReading.offer(new DataInputStream(socket.getInputStream()));
        } catch (IOException e) {
            return false;
        }

        LOGGER.info(this + description);

        // запомним в базе данных
        network.onConnect(this);

        // при коннекте во вне связь может порваться поэтому тут по runed
        return this.runed;
    }

    public InetAddress getAddress() {
        return address;
    }

    public long getPingCounter() {
        return this.pingCounter;
    }


    public int compareTo(Peer peer) {

        if (this.address.getAddress()[0] > peer.address.getAddress()[0])
            return 1;
        else if (this.address.getAddress()[0] < peer.address.getAddress()[0])
            return -1;
        else if (this.address.getAddress()[1] > peer.address.getAddress()[1])
            return 1;
        else if (this.address.getAddress()[1] < peer.address.getAddress()[1])
            return -1;
        else if (this.address.getAddress()[2] > peer.address.getAddress()[2])
            return 1;
        else if (this.address.getAddress()[2] < peer.address.getAddress()[2])
            return -1;
        else if (this.address.getAddress()[3] > peer.address.getAddress()[3])
            return 1;
        else if (this.address.getAddress()[3] < peer.address.getAddress()[3])
            return -1;
        else
            return 0;
    }

    @Override
    public int hashCode() {
        int hash = new BigInteger(this.address.getAddress()).hashCode();
        return white ? -hash : hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Peer && obj.hashCode() == hashCode()) {
            return Arrays.equals(((Peer) obj).getAddress().getAddress(),
                    address.getAddress());
        }
        return false;
    }

    public int getErrors() {
        return this.errors;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setNodeInfo(JSONObject nodeInfo) {
        this.nodeInfo = nodeInfo;
    }

    public String getVersion() {
        return version;
    }

    public JSONObject getNodeInfo() {
        return nodeInfo;
    }

    public Long getWEBPort() {
        if (nodeInfo != null) {
            return (Long) nodeInfo.get("port");
        }
        return null;
    }

    public String getHostName() {
        return address.getHostName();
    }

    public String getScheme() {
        if (nodeInfo != null) {
            return (String) nodeInfo.getOrDefault("scheme", "http");
        }
        return "http";
    }

    public String getBanMessage() {
        return banMessage;
    }

    public void setBuildTime(long build) {
        this.buildDateTime = build;
    }

    public long getBuildTime() {
        return buildDateTime;
    }

    public Tuple2<Integer, Long> getHWeight(boolean useCorrection) {
        if (hWeight == null)
            hWeight = new Tuple2<Integer, Long>(0, 0L);

        if (useCorrection && correctWeight != 0)
            return new Tuple2<>(hWeight.a, hWeight.b + correctWeight);

        return hWeight;
    }

    public void setHWeight(Tuple2<Integer, Long> hWeight) {
        if (hWeight == null)
            this.hWeight = new Tuple2<Integer, Long>(0, 0L);
        else
            this.hWeight = hWeight;
    }

    public void setCorrectionWeight(Tuple2<Integer, Long> myHWeight) {
        this.correctWeight = myHWeight.b - this.hWeight.b;
    }

    public int getMute() {
        return mute;
    }

    public void setMute(int mute) {
        this.mute = mute;
    }

    public long resetErrors() {
        return this.errors = 0;
    }

    public void setNeedPing() {
        this.pinger.setNeedPing();
    }

    public void addPingCounter() {
        this.pingCounter++;
    }

    public void addError() {
        this.errors++;
    }

    public long getPing() {
        if (this.pinger == null)
            return 1999999;

        return this.pinger.getPing();
    }

    public void setPing(int ping) {
        this.pinger.setPing(ping);
    }

    //public boolean tryPing(long timer) {
    //    return this.pinger.tryPing(timer);
    //}
    public boolean tryPing() {
        return this.pinger.tryPing();
    }
    //public boolean tryQuickPing() {
    //    return this.pinger.tryQuickPing();
    //}

    public boolean isPinger() {
        return this.pinger != null;
    }

    /**
     * если хоть что-то есть то это используемый пир
     * @return
     */
    public boolean isOnUsed() {
        return this.socket != null && !this.runed;
    }

    public boolean isUsed() {
        return this.socket != null && this.socket.isConnected() && this.runed;
    }

    /**
     * for all peers ONE
     */
    private static long countAlarmMess = 0;
    public void run() {
        byte[] messageMagic;

        long parsePoint;

        this.initMonitor();
        try {
            while (this.network.run) {
                if (USE_MONITOR) this.setMonitorPoint();

                DataInputStream in;
                Object starter;
                try {
                    starter = startReading.take();
                    if (starter instanceof DataInputStream) {
                        in = (DataInputStream) starter;
                    } else
                        break;

                    // INIT PINGER
                    pinger.init();
                } catch (InterruptedException e) {
                    break;
                }

                MessageFactory messageFactory = MessageFactory.getInstance();
                byte[] messageMagicChain = Controller.getInstance().getMessageMagic();
                while (this.runed && this.network.run) {

                    //READ FIRST 4 BYTES
                    messageMagic = new byte[Message.MAGIC_LENGTH];

                    if (USE_MONITOR) this.setMonitorStatus("in.readFully(messageMagic)");

                    // MORE EFFECTIVE
                    // в этом случае просто ожидаем прилета байтов в течении заданного времени ожидания
                    // java.net.Socket.setSoTimeout(30000)
                    // после чего ловим событие SocketTimeoutException т поаторяем ожидание
                    // это работает без задержек и более эффективно и не ест время процессора
                    try {
                        in.readFully(messageMagic);
                    } catch (java.net.SocketTimeoutException timeOut) {
                        /// просто дальше ждем - все нормально
                        continue;
                    } catch (java.lang.OutOfMemoryError e) {
                        LOGGER.error(e.getMessage(), e);
                        Controller.getInstance().stopAll(250);
                        return;
                    } catch (EOFException e) {
                        if (this.runed)
                            // на ТОМ конце произошло отключение - делаем тоже дисконект
                            ban(0, "peer is shutdownInput");

                        break;
                    } catch (java.net.SocketException e) {
                        if (this.runed)
                            ban(network.banForActivePeersCounter(), "read-2 " + e.getMessage());
                        break;
                    } catch (java.io.IOException e) {
                        if (this.runed)
                            // это наш дисконект
                            ban(network.banForActivePeersCounter(), "read-3 " + e.getMessage());
                        break;
                    }

                    if (!Arrays.equals(messageMagic, messageMagicChain)) {
                        //ERROR and BAN
                        ban(30, "parse - received message with wrong magic");
                        break;
                    }

                    parsePoint = System.nanoTime();
                    //PROCESS NEW MESSAGE
                    Message message;
                    try {
                        if (USE_MONITOR) this.setMonitorStatus("messageFactory.parse");
                        message = messageFactory.parse(this, in);
                    } catch (java.net.SocketTimeoutException timeOut) {
                        ban(network.banForActivePeersCounter(), "peer in TimeOut and -ping");
                        break;
                    } catch (java.lang.OutOfMemoryError e) {
                        LOGGER.error(e.getMessage(), e);
                        Controller.getInstance().stopAll(252);
                        break;
                    } catch (EOFException e) {
                        if (this.runed)
                            // на ТОМ конце произошло отключение - делаем тоже дисконект
                            ban(network.banForActivePeersCounter(), "peer is shutdownInput");
                        break;
                    } catch (IOException e) {
                        if (this.runed)
                            // на ТОМ конце произошло отключение - делаем тоже дисконект
                            ban(network.banForActivePeersCounter(), e.getMessage());
                        break;
                    } catch (Exception e) {
                        //DISCONNECT and BAN
                        LOGGER.error(e.getMessage(), e);
                        //ban(network.banForActivePeersCounter(), "parse message wrong - " + e.getMessage());
                        continue;
                    }

                    if (message == null) {
                        // уже обрабатывали такое сообщение - игнорируем
                        if (false) {
                            String mess = " ALREADY processed!";
                            if (USE_MONITOR) this.setMonitorStatus(mess);
                            LOGGER.debug(this + mess);
                        }
                        continue;
                    }

                    parsePoint = (System.nanoTime() - parsePoint) / 1000;
                    if (System.currentTimeMillis() - countAlarmMess > 1000 && parsePoint < 999999999l) {
                        if ((message.getType() == Message.TELEGRAM_TYPE || message.getType() == Message.TRANSACTION_TYPE) && parsePoint > 1000
                                || parsePoint > 1009000
                        ) {
                            LOGGER.debug(this + message.viewPref(false) + message
                                    + " PARSE: " + parsePoint + "[us]");
                        }
                        countAlarmMess = System.currentTimeMillis();
                    }

                    if (USE_MONITOR) this.setMonitorStatus("in.message process: " + message.viewPref(false) + message);

                    if (LOG_GET_HWEIGHT_TYPE && (//message.getType() == Message.GET_HWEIGHT_TYPE ||
                            message.getType() == Message.HWEIGHT_TYPE)
                    ) {
                        if (Arrays.equals(address.getAddress(), DEBUG_PEER)) {
                            boolean debug = true;
                        }
                        LOGGER.debug(this + message.viewPref(false) + message);
                    }

                    //CHECK IF WE ARE WAITING FOR A RESPONSE WITH THAT ID
                    if (!message.isRequest() && message.hasId()) {

                        if (!this.requests.containsKey(message.getId())) {
                            // просроченное сообщение
                            // это ответ на наш запрос с ID
                            if (LOG_GET_HWEIGHT_TYPE && (//message.getType() == Message.GET_HWEIGHT_TYPE ||
                                    message.getType() == Message.HWEIGHT_TYPE)
                            ) {
                                if (USE_MONITOR) this.setMonitorStatus(" << LATE " + message);
                                LOGGER.debug(this + " << LATE " + message);
                            }
                            continue;
                        }

                        // это ответ на наш запрос с ID

                        try {

                            // get WAITING POLL
                            BlockingQueue<Message> poll = this.requests.remove(message.getId());
                            // invoke WAITING POLL
                            poll.add(message);

                        } catch (java.lang.OutOfMemoryError e) {
                            LOGGER.error(e.getMessage(), e);
                            Controller.getInstance().stopAll(254);
                            break;

                        } catch (Exception e) {
                            LOGGER.error(this + message.viewPref(false) + message);
                            LOGGER.error(e.getMessage(), e);
                        }

                    } else {

                        long timeStart = System.currentTimeMillis();

                        try {
                            if (USE_MONITOR)
                                this.setMonitorStatus("network.onMessage: " + message.viewPref(false) + message);
                            this.network.onMessage(message);
                        } catch (java.lang.OutOfMemoryError e) {
                            LOGGER.error(e.getMessage(), e);
                            Controller.getInstance().stopAll(256);
                            break;
                        }

                        timeStart = System.currentTimeMillis() - timeStart;
                        if (System.currentTimeMillis() - countAlarmMess > 1000
                                && (timeStart > 1
                                || message.getType() == Message.WIN_BLOCK_TYPE && timeStart > 100)) {
                            countAlarmMess = System.currentTimeMillis();
                            LOGGER.debug(this + message.viewPref(false) + message + " solved by period: " + timeStart);
                        }
                    }
                }
            }
        } finally {
            if (USE_MONITOR) {
                String mess = " HALTED by " + (network.run ? "error" : "network.STOP");
                this.setMonitorStatus(mess);
                LOGGER.debug(this + mess);
            }
        }
    }

    public void sendGetHWeight(GetHWeightMessage getHWeightMessage) {
        this.sender.sendGetHWeight(getHWeightMessage);
    }

    public void sendHWeight(HWeightMessage hWeightMessage) {
        this.sender.sendHWeight(hWeightMessage);
    }

    public void sendWinBlock(BlockWinMessage winBlock) {
        this.sender.sendWinBlock(winBlock);
    }

    public boolean offerMessage(Message message) {
        if (LOG_GET_HWEIGHT_TYPE && message.hasId()
                && (message.getType() == Message.GET_HWEIGHT_TYPE || message.getType() == Message.HWEIGHT_TYPE)) {
            boolean isSend = this.sender.offer(message);
            LOGGER.debug(message.viewPref(true) + message + (isSend ? "" : " NOT SEND "));
            return isSend;
        }
        return this.sender.offer(message);
    }

    /**
     * прямая пересылка - без очереди
     * @param message
     * @return
     */
    public boolean directSendMessage(Message message) {
        long point = System.currentTimeMillis();
        if (this.sender.sendMessage(message)) {
            point = ((System.currentTimeMillis() - point) << 1) + 1 ;
            this.pinger.setPing((int) point);
        }
        return this.runed;
    }

    /**
     * synchronized - дает задержку но работает четко
     * @return
     */
    private synchronized int incrementKey() {

        if (this.requestKey > 9999) {
            this.requestKey = 1;
        } else {
            this.requestKey++;
        }

        return this.requestKey;

    }

    public Message getResponse(Message message, long timeSOT) {

        Integer localRequestKey;
        BlockingQueue<Message> blockingQueue;
        long checkTime;

        // ЭТО ТОЖЕ НЕ ПОМОГАЕТ
        ///synchronized (blockingQueue = new ArrayBlockingQueue<Message>(1)) {
        blockingQueue = new ArrayBlockingQueue<Message>(1);

        if (false) {
            // неа - иногда без увеличения на 1 делает 2 запроса с одинаковым Номером
            // проверено - и обмен запросами встает и пинг не проходит! И Бан
            localRequestKey = incrementKey(); // быстро и без колллизий
        } else {
            // тут тоже по 2 раза печатает в лог
            // "response.write GET_HWEIGHT_TYPE[5], messages.size: 0",
            localRequestKey = requestKeyAtomic.get();
            if (localRequestKey > 100000) {
                requestKeyAtomic.set(0);
            }
            localRequestKey = requestKeyAtomic.incrementAndGet();
        }

        if (USE_MONITOR) {
            this.setMonitorStatus("incrementKey: " + localRequestKey);
        }
        if (LOG_GET_HWEIGHT_TYPE) {
            LOGGER.debug(this + " incrementKey: " + localRequestKey);
        }
        message.setId(localRequestKey);

        checkTime = System.currentTimeMillis();

        //PUT QUEUE INTO MAP SO WE KNOW WE ARE WAITING FOR A RESPONSE
        this.requests.put(localRequestKey, blockingQueue);

        if (USE_MONITOR) {
            this.setMonitorStatusBefore("response.write " + message.toString() + ", requests.size: " + requests.size());
        }
        if (LOG_GET_HWEIGHT_TYPE && message.getType() == Message.GET_HWEIGHT_TYPE) {
            LOGGER.debug(this + " response.write " + message.toString() + ", requests.size: " + requests.size());
        }

        if (message.getType() == Message.GET_HWEIGHT_TYPE) {
            sender.sendGetHWeight((GetHWeightMessage) message);
        } else {
            if (!this.offerMessage(message)) {
                //WHEN FAILED TO SEND MESSAGE
                this.requests.remove(localRequestKey);
                if (USE_MONITOR) this.setMonitorStatusAfter();
                return null;
            }
        }

        if (USE_MONITOR) this.setMonitorStatusAfter();


        Message response;
        try {
            response = blockingQueue.poll(timeSOT, TimeUnit.MILLISECONDS);
        } catch (java.lang.OutOfMemoryError e) {
            LOGGER.error(e.getMessage(), e);
            Controller.getInstance().stopAll(260);
            return null;
        } catch (InterruptedException e) {
            this.requests.remove(localRequestKey);
            return null;
        } catch (Exception e) {
            this.requests.remove(localRequestKey);
            LOGGER.error(e.getMessage(), e);
            return null;
        }

        if (response == null) {
            // НУЛЬ значит не дождались - пора удалять идентификатор запроса из списка
            this.requests.remove(localRequestKey);
        } else if (this.getPing() < 0) {
            // если ответ есть то в читателе уже удалили его из очереди
            // SET PING by request period
            this.setPing((int) (System.currentTimeMillis() - checkTime));
        }

        if (USE_MONITOR)
            this.setMonitorStatus("response.done for: " + message.toString() + " RESPONSE: " + response);

        return response;
        ///}
    }

    public Message getResponse(Message message) {
        return getResponse(message, Settings.getInstance().getConnectionTimeout());
    }

    // TRUE = You;  FALSE = Remote
    public boolean isWhite() {
        return this.white;
    }

    public long getConnectionTime() {
        return this.connectionTime;
    }

    public boolean isBad() {
        return Controller.getInstance().getDLSet().getPeerMap().isBad(this.getAddress());
    }

    public boolean isBanned() {
        return Controller.getInstance().getDLSet().getPeerMap().isBanned(address.getAddress());
    }
    public int getBanMinutes() {
        DLSet dlSet = Controller.getInstance().getDLSet();
        if (dlSet == null) return 0;
        return dlSet.getPeerMap().getBanMinutes(this);
    }

    /**
     * Ban for PEER. <br>
     * (Tip: deadlock if set synchronized - Sender + Pinger + ban + setName)
     * @param banForMinutes
     * @param message
     */
    public /* synchronized */ void ban(int banForMinutes, String message) {

        if (USE_MONITOR) this.setMonitorStatus("BAN: " + message);

        if (blockBuffer != null) {
            blockBuffer.stopThread();
            blockBuffer = null;
        }

        if (!runed) {
            if (banForMinutes > this.getBanMinutes()) {
                banMessage = message;
                this.network.afterDisconnect(this, banForMinutes, message);
            }

            return;
        }

        /// этот метод блокирует доступ к пиру - и его нельзя делать внутри synchronized методов
        //// this.setName(this.getName()
        ////         + " banned for " + banForMinutes + " " + message);

        banMessage = message;

        // если там уже было закрыто то не вызывать After
        // или если нужно забанить
        if (this.close(message) || banForMinutes > this.getBanMinutes())
            this.network.afterDisconnect(this, banForMinutes, message);

    }

    public void ban(String message) {
        if (this.isUsed()) {
            ban(network.banForActivePeersCounter(), message);
        }
    }


    /**
     * синхронизированное закрытие чтобы по несольку раз не входило
     *  и если уже закрывается то выход с FALSE
     *
     * @param message
     * @return
     */
    public synchronized boolean close(String message) {

        if (!runed) {
            return false;
        }

        runed = false;

        LOGGER.info("Try close peer : " + this + " - " + message);

        this.pinger.close();

        this.sender.close();

        if (socket != null) {
            //logger.debug(this + " SOCKET: \n"
            //        + (this.socket.isBound()? " isBound " : "")
            //        + (this.socket.isConnected()? " isConnected " : "")
            //        + (this.socket.isInputShutdown()? " isInputShutdown " : "")
            //        + (this.socket.isOutputShutdown()? " isOutputShutdown " : "")
            //        + (this.socket.isClosed()? " isClosed " : "")
            //);

            //CHECK IF SOCKET IS CONNECTED
            if (socket.isConnected()) {

                //CLOSE SOCKET
                try {
                    // this close IN and OUT streams
                    // and notyfy receiver with EOFException
                    //this.socket.shutdownInput(); - закрывает канал так что его нужно потом 2-й раз открывать
                    //this.socket.shutdownOutput(); - не дает переконнектиться
                    //this.in.close(); - не дает пототм переконнектиться
                    //this.out.close(); - не дает пототм переконнектиться

                    // тут нельзя закрывать Стримы у Сокета так как при встречном переконнекте
                    // иначе Стримы больше не откроются
                    // и нужно просто сокет закрыть

                    // сообщим в свой цикл что закрыли соединение
                    this.socket.shutdownInput();

                    // сообщим что закрыли соединение другому узлу
                    this.socket.shutdownOutput();

                    this.socket.close();

                } catch (Exception ignored) {
                    LOGGER.error(this + " - " + ignored.getMessage(), ignored);
                }
            }
        }

        this.socket = null;

        return true;
    }

    public void halt() {

        //this.pinger.close();
        this.close("halt");
        this.startReading.offer(-1);
        ////this.setName(this.getName() + " halted"); // может блокировать

    }

    public JSONObject toJson() {

        Controller cnt = Controller.getInstance();
        PeerMap.PeerInfo peerInfo = cnt.dlSet.getPeerMap().getInfo(getAddress());

        if (peerInfo == null)
            return null;

        JSONObject json = new JSONObject();

        json.put("ip", getAddress().getHostAddress());

        JSONObject info = getNodeInfo();
        if (info != null) {
            json.put("info", info);
        }

        json.put("hostName", getHostName());

        Fun.Tuple2<Integer, Long> res = getHWeight(true);
        if (res == null || res.a == 0) {
            if (isUsed()) {
                json.put("hw", Lang.T("Waiting..."));
            }
        } else {
            json.put("height", res.a);
            json.put("weight", res.b);
        }

        if (banMessage != null)
            json.put("brokenMessage", banMessage);

        if (isUsed()) {
            if (getPing() > 1000000) {
                json.put("status", "Waiting...");
            } else {
                json.put("status", "connect");
                json.put("ping", getPing());
            }
        } else {
            int banMinutes = cnt.getDLSet().getPeerMap().getBanMinutes(this);
            if (banMinutes > 0) {
                json.put("status", "banned");
                json.put("period", banMinutes);
            } else if (mute > 0) {
                json.put("status", "muted");
                json.put("period", mute);
            } else {
                json.put("status", "broken");
            }
        }

        json.put("pingCounter", peerInfo.getWhitePingCouner());
        json.put("white", isWhite());

        json.put("findingTime", DateTimeFormat.timeAgo(peerInfo.getFindingTime()));
        json.put("connectionTime", DateTimeFormat.timeAgo(connectionTime));

        json.put("buildTime", DateTimeFormat.timestamptoString(buildDateTime, "yyyy-MM-dd", "UTC"));
        json.put("version", version);

        return json;

    }

    @Override
    public String toString() {

        return this.getName()
                + (getPing() >= 0 && getPing() < 99999 ? " " + this.getPing() + "ms" : (getPing() < 0 ? " try" + getPing() : "")
        );
    }
}
