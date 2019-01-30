package org.erachain.network;

import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.network.message.*;
import org.erachain.ntp.NTP;
import org.erachain.settings.Settings;
import org.erachain.utils.MonitoredThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/** верт (процесс)
 * вертает общение с внешним пиром - чтение и запись
 *
 */
public class Peer extends MonitoredThread {

    private final static boolean USE_MONITOR = false;
    private final static boolean logPings = true;

    static Logger LOGGER = LoggerFactory.getLogger(Peer.class.getName());
    // Слишком бльшой буфер позволяет много посылок накидать не ожидая их приема. Но запросы с возратом остаются в очереди на долго
    // поэтому нужно ожидание дольще делать
    private static int SOCKET_BUFFER_SIZE = BlockChain.HARD_WORK ? 1024 << 11 : 1024 << 9;
    private static int MAX_BEFORE_PING = SOCKET_BUFFER_SIZE >> 2;
    public Network network;
    private InetAddress address;
    public Socket socket;

    BlockingQueue<DataInputStream> startReading = new ArrayBlockingQueue<DataInputStream>(1);

    private Sender sender;
    private Pinger pinger;

    private boolean white;
    private long pingCounter;
    private long connectionTime;
    private boolean runed;
    private int errors;
    private int requestKey = 0;

    Map<Integer, BlockingQueue<Message>> messages;

    public Peer(InetAddress address) {
        this.address = address;
        this.messages = Collections.synchronizedMap(new HashMap<Integer, BlockingQueue<Message>>());
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

        //LOGGER.debug("@@@ new Peer(ConnectionCallback callback, Socket socket) : " + socket.getInetAddress().getHostAddress());

        try {
            this.network = network;
            this.socket = socket;
            this.address = socket.getInetAddress();
            this.messages = Collections.synchronizedMap(new HashMap<Integer, BlockingQueue<Message>>());
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

            //START COMMUNICATON THREAD
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

        this.messages = Collections.synchronizedMap(new HashMap<Integer, BlockingQueue<Message>>());
        this.pingCounter = 0;
        this.connectionTime = NTP.getTime();
        this.errors = 0;

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
                this.socket = new Socket(address, Controller.getInstance().getNetworkPort());
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

            this.pinger.setPing(Integer.MAX_VALUE);
            this.pinger.setName("Pinger-" + this.pinger.getId() + " for: " + this.getName());

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

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Peer) {
            return Arrays.equals(((Peer)obj).address.getAddress(),
                    this.address.getAddress());
        }
        return false;
    }

    public int getErrors() {
        return this.errors;
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

    public void run() {
        byte[] messageMagic = null;

        this.initMonitor();
        while (this.network.run) {
            if (USE_MONITOR) this.setMonitorPoint();

            DataInputStream in = null;
            try {
                in = startReading.take();
                // INIT PINGER
                pinger.init();
            } catch (InterruptedException e) {
                break;
            }

            while (this.runed && this.network.run) {

                //READ FIRST 4 BYTES
                messageMagic = new byte[Message.MAGIC_LENGTH];

                if (USE_MONITOR) this.setMonitorStatus("in.readFully");

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
                    Controller.getInstance().stopAll(82);
                    return;
                } catch (EOFException e) {
                    if (this.runed)
                        // на ТОМ конце произошло отключение - делаем тоже дисконект
                        ban(network.banForActivePeersCounter(), "read-0 peer is shutdownInput");

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

                if (!Arrays.equals(messageMagic, Controller.getInstance().getMessageMagic())) {
                    //ERROR and BAN
                    ban(3600, "parse - received message with wrong magic");
                    break;
                }

                //PROCESS NEW MESSAGE
                Message message;
                try {
                    message = MessageFactory.getInstance().parse(this, in);
                } catch (java.net.SocketTimeoutException timeOut) {
                    ban(network.banForActivePeersCounter(), "peer in TimeOut and -ping");
                    break;
                } catch (java.lang.OutOfMemoryError e) {
                    Controller.getInstance().stopAll(83);
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
                    ban(network.banForActivePeersCounter(), "parse message wrong - " + e.getMessage());
                    break;
                }

                /*
                if (message == null) {
                    // unknowm message
                    LOGGER.debug(this + " : NULL message!!!");
                    continue;
                }
                */

                if (USE_MONITOR) this.setMonitorStatus("in.message process");

                //CHECK IF WE ARE WAITING FOR A RESPONSE WITH THAT ID
                if (!message.isRequest() && message.hasId()) {

                    if (!this.messages.containsKey(message.getId())) {
                        // просроченное сообщение
                        // это ответ на наш запрос с ID
                        if (logPings && message.getType() != Message.TRANSACTION_TYPE
                                && message.getType() != Message.TELEGRAM_TYPE
                        ) {
                            LOGGER.debug(this + " <... " + message);
                        }
                        continue;
                    }

                    // это ответ на наш запрос с ID
                    if (logPings && message.getType() != Message.TRANSACTION_TYPE
                            && message.getType() != Message.TELEGRAM_TYPE
                    ) {
                        LOGGER.debug(this + message.viewPref(false) + message);
                    }

                    try {

                        // get WAITING POLL
                        BlockingQueue<Message> poll = this.messages.remove(message.getId());
                        // invoke WAITING POLL
                        poll.add(message);

                    } catch (java.lang.OutOfMemoryError e) {
                        Controller.getInstance().stopAll(84);
                        break;

                    } catch (Exception e) {
                        LOGGER.error(this + message.viewPref(false) + message);
                        LOGGER.error(e.getMessage(), e);
                    }

                } else {

                    if (logPings && message.getType() != Message.TRANSACTION_TYPE
                            && message.getType() != Message.TELEGRAM_TYPE
                    ) {
                        LOGGER.debug(this + message.viewPref(false) + message);
                    }

                    long timeStart = System.currentTimeMillis();

                    this.network.onMessage(message);

                    timeStart = System.currentTimeMillis() - timeStart;
                    if (timeStart > 100) {
                        LOGGER.debug(this + message.viewPref(false) + message + " solved by period: " + timeStart);
                    }
                }
            }
        }

        if (USE_MONITOR) this.setMonitorStatus("halted");
        LOGGER.info(this + " - halted");

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

    public void putMessage(Message message) {
        this.sender.put(message);
    }

    public boolean offerMessage(Message message, long SOT) {
        return this.sender.offer(message, SOT);
    }
    public boolean offerMessage(Message message) {
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

        if (this.requestKey == 99999) {
            this.requestKey = 1;
        }
        else
            this.requestKey++;

        if (USE_MONITOR) this.setMonitorStatus("incrementKey: " + this.requestKey);
        return this.requestKey;

    }

    public Message getResponse(Message message, long timeSOT) {

        BlockingQueue<Message> blockingQueue = new ArrayBlockingQueue<Message>(1);

        int localRequestKey = incrementKey(); // быстро и без колллизий

        message.setId(localRequestKey);

        long checkTime = System.currentTimeMillis();


        if (USE_MONITOR) this.setMonitorStatusBefore("response.write " + message.toString() + ", messages.size: " + messages.size());

        //PUT QUEUE INTO MAP SO WE KNOW WE ARE WAITING FOR A RESPONSE
        this.messages.put(localRequestKey, blockingQueue);
        boolean sended;
        if (false && message.getType() == Message.GET_HWEIGHT_TYPE) {
            this.sendGetHWeight((GetHWeightMessage) message);
            sended = true;
        } else
            sended = this.offerMessage(message, timeSOT<<1);

        if (!sended) {
            this.messages.remove(localRequestKey);
            if (USE_MONITOR) this.setMonitorStatusAfter();
            //WHEN FAILED TO SEND MESSAGE
            //blockingQueue = null;
            //LOGGER.debug(this + " >> " + message + " send ERROR by period: " + (System.currentTimeMillis() - checkTime));
            ////this.messages.remove(localRequestKey);
            return null;
        }
        if (USE_MONITOR) this.setMonitorStatusAfter();

        Message response = null;
        try {
            response = blockingQueue.poll(timeSOT, TimeUnit.MILLISECONDS);
        } catch (java.lang.OutOfMemoryError e) {
            Controller.getInstance().stopAll(86);
            return null;
        } catch (InterruptedException e) {
            this.messages.remove(localRequestKey);
            return null;
        } catch (Exception e) {
            this.messages.remove(localRequestKey);
            LOGGER.error(e.getMessage(), e);
            return null;
        }

        if (response == null) {
            // НУЛЬ значит не дождались - пора удалять идентификатор запроса из списка
            this.messages.remove(localRequestKey);
        } else if (this.getPing() < 0) {
            // если ответ есть то в читателе уже удалили его из очереди
            // SET PING by request period
            this.setPing((int)(System.currentTimeMillis() - checkTime));
        }

        if (USE_MONITOR) this.setMonitorStatus("response.done " + message.toString());

        return response;
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
        return Controller.getInstance().getDBSet().getPeerMap().isBad(this.getAddress());
    }

    public boolean isBanned() {
        return Controller.getInstance().getDBSet().getPeerMap().isBanned(address.getAddress());
    }
    public int getBanMinutes() {
        return Controller.getInstance().getDBSet().getPeerMap().getBanMinutes(this);
    }

    public void ban(int banForMinutes, String message) {

        if (!runed) {
            if (banForMinutes > this.getBanMinutes())
                this.network.afterDisconnect(this, banForMinutes, message);
            return;
        }

        this.setName(this.getName()
                + " banned for " + banForMinutes + " " + message);

        // если там уже было закрыто то не вызывать After
        // или если нужно забанить
        if (this.close(message) || banForMinutes > this.getBanMinutes())
            this.network.afterDisconnect(this, banForMinutes, message);

    }

    public void ban(String message) {
        ban(network.banForActivePeersCounter(), message);
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
            //LOGGER.debug(this + " SOCKET: \n"
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

        this.close("halt");
        this.setName(this.getName() + " halted");

    }

    @Override
    public String toString() {

        return this.getName()
                + (getPing() >= 0 && getPing() < 99999? " " + this.getPing() + "ms" : (getPing() < 0?" try" + getPing() : "")
        );
    }
}
