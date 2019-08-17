 package org.erachain.network;
// 30/03

import org.erachain.controller.Controller;
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
    private static final int QUEUE_LENGTH = 256 << (Controller.HARD_WORK >> 1);
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

    public boolean offer(Message message) {
        boolean result = blockingQueue.offer(message);
        if (!result) {
            this.peer.network.missedSendes.incrementAndGet();
        }
        return result;
    }

    public void sendGetHWeight(GetHWeightMessage getHWeightMessage) {
        if (true) {
            if (this.blockingQueue.isEmpty()) {
                blockingQueue.offer(getHWeightMessage);
            } else {
                this.getHWeightMessage = getHWeightMessage;
            }
        } else
            blockingQueue.offer(getHWeightMessage);
    }

    public void sendHWeight(HWeightMessage hWeightMessage) {
        if (true) {
            if (this.blockingQueue.isEmpty()) {
                blockingQueue.offer(hWeightMessage);
            } else {
                this.hWeightMessage = hWeightMessage;
            }
        } else
            blockingQueue.offer(hWeightMessage);
    }

    public void sendWinBlock(BlockWinMessage winBlock) {
        if (this.blockingQueue.isEmpty()) {
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
                if (needFlush
                        || blockingQueue.isEmpty() ?
                        System.currentTimeMillis() - out_flush_time > MAX_FLUSH_TIME
                                || out_flush_length > MAX_FLUSH_LENGTH_EMPTY
                        :
                        out_flush_length > MAX_FLUSH_LENGTH

                ) {
                    this.out.flush();
                    out_flush_time = System.currentTimeMillis();
                    out_flush_length = 0;
                }

            } catch (java.lang.OutOfMemoryError e) {
                LOGGER.error(e.getMessage(), e);
                Controller.getInstance().stopAll(85);
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

        if (logPings && (message.getType() == Message.GET_HWEIGHT_TYPE || message.getType() == Message.HWEIGHT_TYPE)) {
            LOGGER.debug(this.peer + message.viewPref(true) + message);
        }

        if (peer.getPing() < -10 && message.getType() == Message.WIN_BLOCK_TYPE) {
            // если пинг хреновый то ничего не шлем кроме пингования
            return false;
        }

        if (USE_MONITOR) this.setMonitorStatusBefore("write");

        byte[] bytes = message.toBytes();

        // проверим - может уже такое сообщение было нами принято, или
        // если нет - то оно будет запомнено уже в списке обработанных входящих сообщений
        // и не будет повторно обрабатываться при прилете к нас опять
        if (message.isHandled()) {
            switch (message.getId()) {
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

        if (!writeAndFlush(bytes, message.getType() == Message.GET_HWEIGHT_TYPE
                || message.getType() == Message.HWEIGHT_TYPE
                || message.getType() == Message.WIN_BLOCK_TYPE))
            return false;

        checkTime = System.currentTimeMillis() - checkTime;
        if (checkTime - 3 > (bytes.length >> 3) && loggedPoint - System.currentTimeMillis() > 1000
                || logPings && (message.getType() == Message.GET_HWEIGHT_TYPE || message.getType() == Message.HWEIGHT_TYPE)) {
            loggedPoint = System.currentTimeMillis();
            LOGGER.debug(this.peer + message.viewPref(true) + message + " sended by period: " + checkTime);
        }

        if (USE_MONITOR) this.setMonitorStatusAfter();


        return true;
    }

    public void run() {

        //Controller cnt = Controller.getInstance();

        Message message = null;

        while (this.peer.network.run) {

            if (this.out == null) {
                // очистить остатки запросов если обнулили вывод
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

            if (message.isRequest() && !this.peer.messages.containsKey(message.getId())) {
                // просроченный запрос - можно не отправлять его
                continue;
            }

            if (!sendMessage(message))
                continue;


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
