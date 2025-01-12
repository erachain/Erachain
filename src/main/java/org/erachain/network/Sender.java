 package org.erachain.network;
// 30/03

import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.network.message.BlockWinMessage;
import org.erachain.network.message.GetHWeightMessage;
import org.erachain.network.message.HWeightMessage;
import org.erachain.network.message.Message;
import org.erachain.utils.MonitoredThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.EOFException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * тут можно сделать несколько очередей с приоритетом
 * и отказаться от synchronized (this.out)
 */
public class Sender extends MonitoredThread {

    private final static boolean USE_MONITOR = false;
    private final static boolean logPings = false;

    private static final Logger LOGGER = LoggerFactory.getLogger(Sender.class.getSimpleName());
    private static final int QUEUE_LENGTH = 1024 + (256 << (Controller.HARD_WORK >> 1));
    BlockingQueue<Message> blockingQueue = new ArrayBlockingQueue<Message>(QUEUE_LENGTH);

    private Peer peer;
    public OutputStream out;

    private GetHWeightMessage getHWeightMessage;
    private HWeightMessage hWeightMessage;
    private BlockWinMessage winBlockToSend;

    static final int MAX_FLUSH_LENGTH = 5000;
    static final int MAX_FLUSH_LENGTH_EMPTY = 1000;
    static final int MAX_FLUSH_TIME = 500;
    private int out_flush_length;
    private long out_flush_time;

    private long loggedPoint;

    public Sender(Peer peer) {
        this.peer = peer;
        this.setName("Sender-" + this.getId() + " for: " + peer.getName());

        this.start();
    }

    public Sender(Peer peer, OutputStream out) {
        this.peer = peer;
        this.setName("Sender-" + this.getId() + " for: " + peer.getName());
        this.out = out;

        this.start();
    }

    public void setOut(OutputStream out) {
        this.out = out;
        this.setName("Sender-" + this.getId() + " for: " + peer.getName());
    }

    public synchronized boolean offer(Message message) {
        boolean result = blockingQueue.offer(message);
        if (!result) {
            this.peer.network.missedSendes.incrementAndGet();
        }
        return result;
    }

    public synchronized void sendGetHWeight(GetHWeightMessage getHWeightMessage) {
        if (this.blockingQueue.isEmpty() || this.getHWeightMessage != null) {
            if (logPings)
                LOGGER.debug(this.peer + " to blockingQueue " + getHWeightMessage.viewPref(true) + getHWeightMessage);
            if (USE_MONITOR)
                this.setMonitorStatus("to blockingQueue " + getHWeightMessage.viewPref(true) + getHWeightMessage);
            blockingQueue.offer(getHWeightMessage);
        } else {
            if (logPings)
                LOGGER.debug(this.peer + " to getHWeightMessage " + getHWeightMessage.viewPref(true) + getHWeightMessage);
            if (USE_MONITOR)
                this.setMonitorStatus("to getHWeightMessage " + getHWeightMessage.viewPref(true) + getHWeightMessage);
            this.getHWeightMessage = getHWeightMessage;
        }
    }

    public synchronized void sendHWeight(HWeightMessage hWeightMessage) {
        if (this.blockingQueue.isEmpty() || this.hWeightMessage != null) {
            if (logPings)
                LOGGER.debug(this.peer + " to blockingQueue " + hWeightMessage.viewPref(true) + getHWeightMessage);
            if (USE_MONITOR)
                this.setMonitorStatus("to blockingQueue " + hWeightMessage.viewPref(true) + getHWeightMessage);
            blockingQueue.offer(hWeightMessage);
        } else {
            if (logPings)
                LOGGER.debug(this.peer + " to getHWeightMessage " + hWeightMessage.viewPref(true) + getHWeightMessage);
            if (USE_MONITOR)
                this.setMonitorStatus("to getHWeightMessage " + hWeightMessage.viewPref(true) + getHWeightMessage);
            this.hWeightMessage = hWeightMessage;
        }
    }

    public synchronized void sendWinBlock(BlockWinMessage winBlock) {
        if (winBlock.getHeight() < BlockChain.ALL_VALID_BEFORE)
            return;

        if (this.blockingQueue.isEmpty()
                || this.winBlockToSend != null // если там уже занято
        ) {
            blockingQueue.offer(winBlock);
        } else {
            this.winBlockToSend = winBlock;
        }
    }

    /**
     * копит буфер а потом его разом выплевывает - так чтобы слишком маньникие пакеты TCP ну были
     * - так как у них заголовок в 200 сразу
     *
     * @param bytes
     * @param needFlush
     * @return
     */
    private boolean writeAndFlush(byte[] bytes, boolean needFlush) {
        // пока есть входы по sendMessage (org.erachain.network.Peer.directSendMessage) - нужно ждать синхрон
        if (this.out == null)
            return false;

        String error = null;

        synchronized (this.out) {
            try {
                //SEND MESSAGE

                if (bytes != null) {
                    this.out.write(bytes);
                    out_flush_length += bytes.length;
                }

                // FLUSH if NEED
                long currentTime = System.currentTimeMillis();
                if (needFlush
                        || blockingQueue.isEmpty() ?
                        currentTime - out_flush_time > MAX_FLUSH_TIME
                                || out_flush_length > MAX_FLUSH_LENGTH_EMPTY
                        :
                        out_flush_length > MAX_FLUSH_LENGTH

                ) {
                    this.out.flush();
                    if (logPings)
                        LOGGER.debug(peer + " FLUSHED OUT sizs:" + out_flush_length + " time: " + (currentTime - out_flush_time));
                    if (USE_MONITOR)
                        this.setMonitorStatus("FLUSHED OUT size: " + out_flush_length + " time: " + (currentTime - out_flush_time));
                    out_flush_time = System.currentTimeMillis();
                    out_flush_length = 0;
                }

            } catch (java.lang.OutOfMemoryError e) {
                LOGGER.error(e.getMessage(), e);
                Controller.getInstance().stopAndExit(277);
                return false;
            } catch (java.lang.NullPointerException e) {
                return false;
            } catch (EOFException e) {
                if (this.out == null)
                    // это наш дисконект
                    return false;

                error = "try out.write 1a - " + e.getMessage();
            } catch (java.net.SocketException eSock) {
                if (this.out == null)
                    // это наш дисконект
                    return false;

                error = "try out.write 1 - " + eSock.getMessage();
            } catch (IOException e) {
                if (this.out == null)
                    // это наш дисконект
                    return false;

                error = "try out.write 2 - " + e.getMessage();
            }
        }

        if (error != null) {
            if (peer.isUsed()) {
                peer.ban(error);
            }
            return false;
        }

        return true;

    }

    boolean sendMessage(Message message) {

        //CHECK IF SOCKET IS STILL ALIVE
        if (this.out == null) {
            return false;
        }

        if (!this.peer.socket.isConnected()) {
            //ERROR
            //peer.ban(0, "SEND - socket not still alive");

            return false;
        }

        int messageType = message.getType();

        if (peer.getPing() < -10 && messageType == Message.WIN_BLOCK_TYPE) {
            // если пинг хреновый то ничего не шлем кроме пингования
            return false;
        }

        if (USE_MONITOR) this.setMonitorStatusBefore("write");
        try {

            byte[] bytes = message.toBytes();
            if (logPings && message.hasId() && (messageType == Message.GET_HWEIGHT_TYPE || messageType == Message.HWEIGHT_TYPE))
                LOGGER.debug(this.peer + message.viewPref(true) + message + " to BYTES");

            // проверим - может уже такое сообщение было нами принято, или
            // если нет - то оно будет запомнено уже в списке обработанных входящих сообщений
            // и не будет повторно обрабатываться при прилете к нам опять
            if (message.isHandled()) {
                switch (message.getType()) {
                    case Message.TELEGRAM_TYPE:
                        // может быть это повтор?

                        if (!this.peer.network.checkHandledTelegramMessages(message.getLoadBytes(), this.peer, true)) {
                            LOGGER.debug(this.peer + " --> Telegram ALREADY SENDED...");
                            return true;
                        }
                        break;
                    case Message.TRANSACTION_TYPE:
                        // может быть это повтор?
                        if (!this.peer.network.checkHandledTransactionMessages(message.getLoadBytes(), this.peer, true)) {
                            LOGGER.debug(this.peer + " --> Transaction ALREADY SENDED...");
                            return true;
                        }
                        break;
                    case Message.WIN_BLOCK_TYPE:
                        // может быть это повтор?
                        if (!this.peer.network.checkHandledWinBlockMessages(message.getLoadBytes(), this.peer, true)) {
                            LOGGER.debug(this.peer + " --> Win Block ALREADY SENDED...");
                            return true;
                        }
                        break;
                }
            }

            long checkTime = System.currentTimeMillis();

            if (!writeAndFlush(bytes,
                    message.quickSend() // все что нужно быстро отправлять
            )) {
                LOGGER.debug(this.peer + message.viewPref(true) + message + " NOT send ((");
                return false;
            }

            checkTime = System.currentTimeMillis() - checkTime;
            if (logPings && message.hasId() && (messageType == Message.GET_HWEIGHT_TYPE || messageType == Message.HWEIGHT_TYPE)
                    || checkTime - 3 > (bytes.length >> 3) && loggedPoint - System.currentTimeMillis() > 1000
            ) {
                loggedPoint = System.currentTimeMillis();
                LOGGER.debug(this.peer + message.viewPref(true) + message + " sended by ms: " + checkTime);
            }

            return true;

        } finally {
            if (USE_MONITOR) this.setMonitorStatusAfter();
        }

    }

    public void run() {

        Message message;

        while (this.peer.network.run) {

            try {
                if (this.out == null) {
                    // очистить остатки запросов если обнулили вывод
                    // и стандартное ожидание пуска дальше. То есть Сендер должен ожидать прилета на вход чего-то
                    blockingQueue.clear();
                }

                try {
                    message = blockingQueue.poll(100, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    break;
                }

                if (getHWeightMessage != null) {
                    if (!sendMessage(getHWeightMessage)) {
                        getHWeightMessage = null;
                    }
                    getHWeightMessage = null;
                }

                if (hWeightMessage != null) {
                    if (!sendMessage(hWeightMessage)) {
                        hWeightMessage = null;
                    }
                    hWeightMessage = null;
                }

                if (winBlockToSend != null) {
                    if (!sendMessage(winBlockToSend)) {
                        winBlockToSend = null;
                    }
                    winBlockToSend = null;
                }

                if (message == null) {
                    // FLUSH if NEED
                    if (out_flush_length > 0 && System.currentTimeMillis() - out_flush_time > MAX_FLUSH_TIME) {
                        writeAndFlush(null, true);
                    }
                    continue;
                }

                if (message.isRequest() && !this.peer.requests.containsKey(message.getId())) {
                    // просроченный запрос - можно не отправлять его
                    continue;
                }

                if (!sendMessage(message))
                    continue;

            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        }

        //logger.debug(this + " - halted");
    }

    public void close() {
        this.out = null;
    }

    public void halt() {
        this.close();
    }

}
