package org.erachain.network;

import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.network.message.Message;
import org.erachain.network.message.MessageFactory;
import org.erachain.ntp.NTP;
import org.erachain.settings.Settings;
import org.erachain.utils.MonitoredThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.OutputStream;
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

    private final static boolean USE_MONITOR = true;

    private final static boolean need_wait = false;
    private final static boolean logPings = false;
    static Logger LOGGER = LoggerFactory.getLogger(Peer.class.getName());
    // Слишком бльшой буфер позволяет много посылок накидать не ожидая их приема. Но запросы с возратом остаются в очереди на долго
    // поэтому нужно ожидание дольще делать
    private static int SOCKET_BUFFER_SIZE = BlockChain.HARD_WORK ? 1024 << 11 : 1024 << 11;
    private static int MAX_BEFORE_PING = SOCKET_BUFFER_SIZE >> 2;
    public Network network;
    private InetAddress address;
    private Socket socket;
    private OutputStream out;
    private DataInputStream in;
    private Pinger pinger;
    private boolean white;
    private long pingCounter;
    private long connectionTime;
    private boolean runed;
    private boolean stoped;
    private int errors;
    private int requestKey = 0;
    private long sendedBeforePing = 0l;
    private long maxBeforePing;
    private Map<Integer, BlockingQueue<Message>> messages;

    public Peer(InetAddress address) {
        this.address = address;
        this.messages = Collections.synchronizedMap(new HashMap<Integer, BlockingQueue<Message>>());
        //LOGGER.debug("@@@ new Peer(InetAddress address) : " + address.getHostAddress());
        this.setName("Peer: " + this.getAddress().getHostAddress() + " as address");

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
            this.sendedBeforePing = 0l;
            this.maxBeforePing = MAX_BEFORE_PING;

            //ENABLE KEEPALIVE
            this.socket.setKeepAlive(true);

            //TIMEOUT - нужно для разрыва соединения если на том конце не стали нам слать нужные байты
            this.socket.setSoTimeout(Settings.getInstance().getConnectionTimeout());

            this.socket.setReceiveBufferSize(SOCKET_BUFFER_SIZE);
            this.socket.setSendBufferSize(SOCKET_BUFFER_SIZE);

            //CREATE STRINGWRITER
            this.out = socket.getOutputStream();
            this.in = new DataInputStream(socket.getInputStream());

            //START PINGER
            if (this.pinger == null)
                this.pinger = new Pinger(this);
            else {
                this.pinger.setPing(Integer.MAX_VALUE);
                this.pinger.setName("Pinger - " + this.pinger.getId() + " for: " + this.getAddress().getHostAddress());
            }

            this.setName("Peer: " + this.address.getHostAddress() + " as socket"
                    + (this.isWhite()?" is White" : ""));

            // IT is STARTED
            this.runed = true;

            //START COMMUNICATON THREAD
            this.start();

            LOGGER.info(description + address.getHostAddress());

            // при коннекте во вне связь может порваться поэтому тут по runed
            network.onConnect(this);

        } catch (Exception e) {
            //FAILED TO CONNECT NO NEED TO BLACKLIST

            this.runed = false;

            LOGGER.info("Failed to connect to : " + address.getHostAddress());
            LOGGER.error(e.getMessage(), e);

        }

    }

    /**
     * Accept connection to reuse brocken peer
     * @param socket
     * @param description
     * @return
     */
    public boolean reconnect(Socket socket, String description) {

        LOGGER.debug("@@@ reconnect(socket) : " + socket.getInetAddress().getHostAddress());

        try {

            if (this.socket != null) {
                this.close();
            }

            this.socket = socket;
            this.address = socket.getInetAddress();
            this.messages = Collections.synchronizedMap(new HashMap<Integer, BlockingQueue<Message>>());
            this.white = false;
            this.pingCounter = 0;
            this.connectionTime = NTP.getTime();
            this.errors = 0;
            this.sendedBeforePing = 0l;
            this.maxBeforePing = MAX_BEFORE_PING;

            //ENABLE KEEPALIVE
            this.socket.setKeepAlive(true);

            //TIMEOUT - нужно для разрыва соединения если на том конце не стали нам слать нужные байты
            this.socket.setSoTimeout(Settings.getInstance().getConnectionTimeout());

            this.socket.setReceiveBufferSize(SOCKET_BUFFER_SIZE);
            this.socket.setSendBufferSize(SOCKET_BUFFER_SIZE);

            //CREATE STRINGWRITER
            this.out = socket.getOutputStream();
            this.in = new DataInputStream(socket.getInputStream());

            this.pinger.setPing(Integer.MAX_VALUE);
            this.pinger.setName("Pinger - " + this.pinger.getId() + " for: " + this.address.getHostAddress());

            // IT is STARTED
            this.runed = true;

            //ON SOCKET CONNECT
            this.setName("Peer: " + this.getAddress().getHostAddress() + " reconnected"
                + (this.isWhite()?" is White" : ""));

            LOGGER.info(description + address.getHostAddress());
            network.onConnect(this);

        } catch (Exception e) {
            //FAILED TO CONNECT NO NEED TO BLACKLIST
            LOGGER.debug("Failed to connect to : " + address.getHostAddress());
            LOGGER.error(e.getMessage(), e);

            return false;
        }

        return this.runed;

    }

    /**
     * Приконнектиться к Премнику
     * @param network
     * @param description
     * @return
     */
    public boolean connect(Network network, String description) {
        if (Controller.getInstance().isOnStopping()) {
            return false;
        }

        if (this.network == null)
            this.network = network;

        this.messages = Collections.synchronizedMap(new HashMap<Integer, BlockingQueue<Message>>());
        this.white = true;
        this.pingCounter = 0;
        this.connectionTime = NTP.getTime();
        this.errors = 0;
        this.sendedBeforePing = 0l;
        this.maxBeforePing = MAX_BEFORE_PING;

        int step = 0;
        try {
            //OPEN SOCKET
            step++;
            if (this.socket != null) {
                this.socket.close();
                this.out.close();
                this.in.close();
            }

            this.socket = new Socket(address, Controller.getInstance().getNetworkPort());

            //ENABLE KEEPALIVE
            step++;
            this.socket.setKeepAlive(true);

            //TIMEOUT - нужно для разрыва соединения если на том конце не стали нам слать нужные байты
            this.socket.setSoTimeout(Settings.getInstance().getConnectionTimeout());

            this.socket.setReceiveBufferSize(SOCKET_BUFFER_SIZE);
            this.socket.setSendBufferSize(SOCKET_BUFFER_SIZE);

            //CREATE STRINGWRITER
            step++;
            this.out = socket.getOutputStream();
            this.in = new DataInputStream(socket.getInputStream());

        } catch (Exception e) {
            //FAILED TO CONNECT NO NEED TO BLACKLIST
            if (step != 1) {
                LOGGER.error(e.getMessage(), e);
                LOGGER.debug("Failed to connect to : " + address.getHostAddress() + " on step: " + step);
            }

            return false;

        }

        if (this.pinger == null) {
            //START PINGER
            this.pinger = new Pinger(this);

            // IT is STARTED
            this.runed = true;

            this.start();

        } else {
            this.pinger.setPing(Integer.MAX_VALUE);
            this.pinger.setName("Pinger - " + this.pinger.getId() + " for: " + this.getAddress().getHostAddress());

            // IT is STARTED
            this.runed = true;

        }

        LOGGER.info(description + address.getHostAddress());
        network.onConnect(this);

        // при коннекте во вне связь может порваться поэтому тут по runed
        return this.runed;
    }

    // connect and run
    public boolean connect(String description) {
        return connect(Controller.getInstance().network, description);
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
        return this.socket != null || this.runed;
    }

    public boolean isUsed() {
        return this.socket != null && this.socket.isConnected() && this.runed;
    }

    public void run() {
        byte[] messageMagic = null;

        this.initMonitor();
        while (!stoped) {
            if (USE_MONITOR) this.setMonitorPoint();

            if (false)
                LOGGER.info(this.getAddress().getHostAddress()
                    + (this.isUsed()?" is Used" : "")
                    + (this.isBanned()?" is Banned" : "")
                    + (this.isBad()?" is Bad" : "")
                    + (this.isWhite()?" is White" : ""));


            // CHECK connection
            if (socket == null || !socket.isConnected() || socket.isClosed()
                    || !runed
            ) {

                try {
                    Thread.sleep(100);
                } catch (Exception e) {
                }

                if (in != null) {
                    if (USE_MONITOR) this.setMonitorStatusBefore("in.close");
                    try {
                        in.close();
                        in = null;
                    } catch (Exception e) {
                        in = null;
                    }
                    if (USE_MONITOR) this.setMonitorStatusAfter();
                }
                continue;

            }

            // CHECK stream
            if (in == null) {
                if (USE_MONITOR) this.setMonitorStatusBefore("in = DataInputStream");
                try {
                    in = new DataInputStream(socket.getInputStream());
                } catch (java.lang.OutOfMemoryError e) {
                    Controller.getInstance().stopAll(81);
                    break;
                } catch (Exception e) {
                    //DISCONNECT
                    network.tryDisconnect(this, network.banForActivePeersCounter(), e.getMessage());
                    continue;
                }
                if (USE_MONITOR) this.setMonitorStatusAfter();
            }

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
                break;
            } catch (EOFException e) {
                // на там конце произошло отключение - делаем тоже дисконект
                network.tryDisconnect(this, network.banForActivePeersCounter(), "read-0 peer is shutdownInput");
                continue;
            } catch (java.net.SocketException e) {
                // если туда уже не лезет иликанал побился
                network.tryDisconnect(this, network.banForActivePeersCounter(), "read-2 " + e.getMessage());
                continue;
            } catch (java.io.IOException e) {
                network.tryDisconnect(this, network.banForActivePeersCounter(), "read-3 " + e.getMessage());
                continue;
            } catch (Exception e) {
                //DISCONNECT and BAN
                network.tryDisconnect(this, network.banForActivePeersCounter(), "read-4 " + e.getMessage());
                continue;
            }

            if (!Arrays.equals(messageMagic, Controller.getInstance().getMessageMagic())) {
                //ERROR and BAN
                network.tryDisconnect(this, 3600, "parse - received message with wrong magic");
                continue;
            }

            //PROCESS NEW MESSAGE
            Message message;
            try {
                message = MessageFactory.getInstance().parse(this, in);
            } catch (java.net.SocketTimeoutException timeOut) {
                // если сдесь по времени ожидания пришло то значит на том конце что-то не так и пора разрывать соединение
                if (this.getPing() < -1) {
                    network.tryDisconnect(this, network.banForActivePeersCounter(), "peer in TimeOut and -ping");
                } else {
                    network.tryDisconnect(this, network.banForActivePeersCounter(), "peer in TimeOut");
                }
                continue;
            } catch (java.lang.OutOfMemoryError e) {
                Controller.getInstance().stopAll(83);
                break;
            } catch (EOFException e) {
                // на там конце произошло отключение - делаем тоже дисконект
                network.tryDisconnect(this, network.banForActivePeersCounter(), "peer is shutdownInput");
                continue;
            } catch (Exception e) {
                //DISCONNECT and BAN
                network.tryDisconnect(this, network.banForActivePeersCounter(), "parse message wrong - " + e.getMessage());
                continue;
            }

            if (message == null) {
                // unknowm message
                LOGGER.debug(this + " : NULL message!!!");
                continue;
            }

            if (logPings && message.getType() == Message.GET_HWEIGHT_TYPE) {
                LOGGER.debug(this + " : " + message + " RECEIVED");
            }

            if (USE_MONITOR) this.setMonitorStatus("in.message process");

            //CHECK IF WE ARE WAITING FOR A RESPONSE WITH THAT ID
            if (!message.isRequest() && message.hasId()) {

                if (!this.messages.containsKey(message.getId())) {
                    // может еще не внесли ключ запроса в список? подождем и еще раз проверим
                    try {
                        Thread.sleep(3);
                    } catch (Exception e) {
                    }
                    // уже посроченный ответ словили - ничего не делаем
                    if (!this.messages.containsKey(message.getId())) {
                        continue;
                    }
                }

                // это ответ на наш запрос с ID
                if (logPings && message.getType() == Message.HWEIGHT_TYPE)
                    LOGGER.debug(this + " >> " + message + " receive as RESPONSE for me & add to messages Queue");

                try {

                    // get WAITING POLL
                    BlockingQueue<Message> poll = this.messages.remove(message.getId());
                    // invoke WAITING POLL
                    poll.add(message);

                } catch (java.lang.OutOfMemoryError e) {
                    Controller.getInstance().stopAll(84);
                    break;

                } catch (Exception e) {
                    LOGGER.debug("received message " + message.viewType() + " from " + this.address.toString());
                    LOGGER.debug("isRequest " + message.isRequest() + " hasId " + message.hasId());
                    LOGGER.debug(" Id " + message.getId());
                    LOGGER.error(e.getMessage(), e);
                }

            } else {
                //CALLBACK
                // see in network.Network.onMessage(Message)
                // and then see controller.Controller.onMessage(Message)

                if (logPings && (message.getType() == Message.GET_HWEIGHT_TYPE || message.getType() == Message.HWEIGHT_TYPE))
                    LOGGER.debug(this + " : " + message + " >> received");

                long timeStart = System.currentTimeMillis();
                ///LOGGER.debug(this + " : " + message + " receive, go solve");

                this.network.onMessage(message);

                timeStart = System.currentTimeMillis() - timeStart;
                if (timeStart > 1000
                        || logPings && (message.getType() == Message.GET_HWEIGHT_TYPE || message.getType() == Message.HWEIGHT_TYPE)) {
                    LOGGER.debug(this + " : " + message + " >> solved by period: " + timeStart);
                }
            }
        }

        if (USE_MONITOR) this.setMonitorStatus("halted");
        LOGGER.info("halted");

    }

    public boolean sendMessage(Message message) {
        //CHECK IF SOCKET IS STILL ALIVE
        if (!this.runed || this.socket == null) {
            ////callback.tryDisconnect(this, network.banForActivePeersCounter(), "SEND - not runned");
            return false;
        }

        if (!this.socket.isConnected()) {
            //ERROR
            network.tryDisconnect(this, network.banForActivePeersCounter(), "SEND - socket not still alive");

            return false;
        }

        byte[] bytes = message.toBytes();

        if (logPings && message.getType() == Message.HWEIGHT_TYPE) {
            LOGGER.debug(message + " try SEND to " + this);
        }

        if (this.getPing() < -5 && message.getType() != Message.HWEIGHT_TYPE
                && message.getType() != Message.GET_HWEIGHT_TYPE) {
            // если пинг хреновый то ничего не шлем кроме пингования
            return false;
        }

        if (USE_MONITOR) this.setMonitorStatusBefore("write");

        //SEND MESSAGE
        long checkTime = System.currentTimeMillis();
        synchronized (this.out) {

            try {
                this.out.write(bytes);
                this.out.flush();
            } catch (java.lang.OutOfMemoryError e) {
                Controller.getInstance().stopAll(85);
                return false;
            } catch (java.net.SocketException eSock) {
                checkTime = System.currentTimeMillis() - checkTime;
                if (checkTime > bytes.length >> 3) {
                    LOGGER.debug(this + " >> " + message + " sended by period: " + checkTime);
                }
                network.tryDisconnect(this, network.banForActivePeersCounter(), "try out.write 1 - " + eSock.getMessage());
                return false;
            } catch (IOException e) {
                checkTime = System.currentTimeMillis() - checkTime;
                if (checkTime > bytes.length >> 3) {
                    LOGGER.debug(this + " >> " + message + " sended by period: " + checkTime);
                }
                network.tryDisconnect(this, network.banForActivePeersCounter(), "try out.write 2 - " + e.getMessage());
                return false;
            } catch (Exception e) {
                checkTime = System.currentTimeMillis() - checkTime;
                if (checkTime > bytes.length >> 3) {
                    LOGGER.debug(this + " >> " + message + " sended by period: " + checkTime);
                }
                network.tryDisconnect(this, network.banForActivePeersCounter(), "try out.write 3 - " + e.getMessage());
                return false;
            }

        }

        if (USE_MONITOR) this.setMonitorStatusAfter();

        checkTime = System.currentTimeMillis() - checkTime;
        if (checkTime > (bytes.length >> 3)
                || logPings && (message.getType() == Message.GET_HWEIGHT_TYPE || message.getType() == Message.HWEIGHT_TYPE)) {
            LOGGER.debug(this + " >> " + message + " sended by period: " + checkTime);
        }

        return true;
    }

    /**
     * synchronized - дает большую задержку - прямо видно что медленне начинают запросы лететь
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
        if (!this.sendMessage(message)) {
            if (USE_MONITOR) this.setMonitorStatusAfter();
            //WHEN FAILED TO SEND MESSAGE
            //blockingQueue = null;
            LOGGER.debug(this + " >> " + message + " send ERROR by period: " + (System.currentTimeMillis() - checkTime));
            ////this.messages.remove(localRequestKey);
            return null;
        }
        if (USE_MONITOR) this.setMonitorStatusAfter();

        //PUT QUEUE INTO MAP SO WE KNOW WE ARE WAITING FOR A RESPONSE
        this.messages.put(localRequestKey, blockingQueue);

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

    public void ban(int banForMinutes, String mess) {
        this.setName("Peer: " + this.getAddress().getHostAddress()
                + " banned for " + banForMinutes + " " + mess);

        this.network.tryDisconnect(this, banForMinutes, mess);
    }
    public void ban(String mess) {
        this.setName("Peer: " + this.getAddress().getHostAddress()
                + " banned - " + mess);

        this.network.tryDisconnect(this, network.banForActivePeersCounter(), mess);
    }


    public boolean isStoped() {
        return stoped;
    }

    public void close() {

        if (!runed) {
            return;
        }

        runed = false;

        LOGGER.info("Try close peer : " + this);

        if (false) {
            // new style
            try {
                this.out.close();
            } catch (Exception ignored) {
            }
            if (this.in != null) {
                try {
                this.in.close();
                } catch (Exception ignored) {
                }
            }
            if (socket != null) {
                //CHECK IF SOCKET IS CONNECTED
                if (socket.isConnected()) {
                    //CLOSE SOCKET
                    try {
                    this.socket.shutdownInput();
                    } catch (Exception ignored) {
                    }
                }
            }

        } else {
            // OLD style

            //CHECK IS SOCKET EXISTS
            if (socket != null) {
                //CHECK IF SOCKET IS CONNECTED
                if (socket.isConnected()) {
                    //CLOSE SOCKET
                    try {
                        // and close IN and OUT
                        this.socket.close();
                    } catch (Exception e) {
                        LOGGER.error(e.getMessage(), e);
                    }
                }
            }
        }

        this.socket = null;
        this.out = null;
        this.in = null;
    }

    public void halt() {

        this.stoped = true;
        this.close();
        this.setName("Peer: " + this.getAddress().getHostAddress() + " halted");

    }

    @Override
    public String toString() {
        return this.address.getHostAddress()
                + (getPing() >= 0? " ping: " + this.getPing() + "ms" : " try" + getPing())
                + (isWhite()? " [White]" : "");
    }
}
