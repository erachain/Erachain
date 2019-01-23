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

    private final static boolean USE_MONITOR = true;
    private final static boolean logPings = true;

    private static final Logger LOGGER = LoggerFactory.getLogger(Sender.class);
    private static final int QUEUE_LENGTH = 20;
    BlockingQueue<Message> blockingQueue = new ArrayBlockingQueue<Message>(QUEUE_LENGTH);

    private Peer peer;
    public OutputStream out;

    private boolean needPing = false;
    private int ping;

    private boolean stoped;

    private GetHWeightMessage getHWeightMessage;
    private HWeightMessage hWeightMessage;
    private BlockWinMessage winBlockToSend;

    public Sender(Peer peer) {
        this.peer = peer;
        this.ping = Integer.MAX_VALUE;
        this.setName("Sender - " + this.getId() + " for: " + peer.getAddress().getHostAddress());

        this.start();
    }

    public Sender(Peer peer, OutputStream out) {
        this.peer = peer;
        this.ping = Integer.MAX_VALUE;
        this.setName("Sender - " + this.getId() + " for: " + peer.getAddress().getHostAddress());
        this.out = out;

        this.start();
    }

    public void setOut(OutputStream out) {
        this.out = out;
    }

    public long getPing() {
        return this.ping;
    }

    public boolean offer(Message message) {
        return blockingQueue.offer(message);
    }

    public boolean offer(Message message, long SOT) {
        try {
            return blockingQueue.offer(message, SOT, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            return false;
        }
    }

    public void put(Message message) {
        try {
            blockingQueue.put(message);
        } catch (InterruptedException e) {
        }
    }

    public void sendHGetWeight(GetHWeightMessage GetHWeightMessage) {
        this.getHWeightMessage = GetHWeightMessage;
    }

    public void sendHWeight(HWeightMessage hWeightMessage) {
        this.hWeightMessage = hWeightMessage;
    }

    public void sendWinBlock(BlockWinMessage winBlock) {
        this.winBlockToSend = winBlock;
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

        if (logPings && (message.getType() != Message.TRANSACTION_TYPE
                && message.getType() != Message.TELEGRAM_TYPE
                || message.getType() == Message.HWEIGHT_TYPE)) {
            LOGGER.debug(message + " try SEND to " + this);
        }

        if (this.getPing() < -10 && message.getType() == Message.WIN_BLOCK_TYPE) {
            // если пинг хреновый то ничего не шлем кроме пингования
            return false;
        }

        if (USE_MONITOR) this.setMonitorStatusBefore("write");

        //SEND MESSAGE
        long checkTime = System.currentTimeMillis();
        if (this.out == null)
            return false;

        byte[] bytes = message.toBytes();
        String error = null;

        // пока есть входы по sendMessage - нужно ждать синхрон
        synchronized (this.out) {
            try {
                this.out.write(bytes);
                this.out.flush();
            } catch (java.lang.OutOfMemoryError e) {
                Controller.getInstance().stopAll(85);
                return false;
            } catch (java.net.SocketException eSock) {
                if (this.out == null)
                    // это наш дисконект
                    return false;

                checkTime = System.currentTimeMillis() - checkTime;
                if (checkTime > bytes.length >> 3) {
                    LOGGER.debug(this + " >> " + message + " sended by period: " + checkTime);
                }
                error = "try out.write 1 - " + eSock.getMessage();
            } catch (IOException e) {
                if (this.out == null)
                    // это наш дисконект
                    return false;

                checkTime = System.currentTimeMillis() - checkTime;
                if (checkTime > bytes.length >> 3) {
                    LOGGER.debug(this + " >> " + message + " sended by period: " + checkTime);
                }
                error = "try out.write 2 - " + e.getMessage();
            } catch (Exception e) {
                checkTime = System.currentTimeMillis() - checkTime;
                if (checkTime > bytes.length >> 3) {
                    LOGGER.debug(this + " >> " + message + " sended by period: " + checkTime);
                }
                error = "try out.write 3 - " + e.getMessage();
            }
        }

        if (error != null) {
            peer.ban(error);
            return false;
        }

        if (USE_MONITOR) this.setMonitorStatusAfter();

        checkTime = System.currentTimeMillis() - checkTime;
        if (checkTime > (bytes.length >> 3)
                || logPings && (message.getType() == Message.GET_HWEIGHT_TYPE || message.getType() == Message.HWEIGHT_TYPE)) {
            LOGGER.debug(this + " >> " + message + " sended by period: " + checkTime);
        }

        return true;
    }

    public void run() {

        //Controller cnt = Controller.getInstance();

        Message message = null;

        while (!this.stoped) {

            if (this.out == null) {
                // очистить остатки запросов если обнулили вывод
                blockingQueue.clear();
            }

            try {
                message = blockingQueue.poll(500, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                //if (this.stoped)
                break;
            }

            if (getHWeightMessage != null) {
                if (!sendMessage(getHWeightMessage)) {
                    getHWeightMessage = null;
                    continue;
                }
                getHWeightMessage = null;
            }

            if (hWeightMessage != null) {
                if (!sendMessage(hWeightMessage)) {
                    hWeightMessage = null;
                    continue;
                }
                hWeightMessage = null;
            }

            if (winBlockToSend != null) {
                if (!sendMessage(winBlockToSend)) {
                    winBlockToSend = null;
                    continue;
                }
                winBlockToSend = null;
            }

            if (message == null)
                continue;

            if (message.isRequest() && !this.peer.messages.containsKey(message.getId())) {
                // просроченный запрос - можно не отправлять его
                continue;
            }

            if (!sendMessage(message))
                continue;
        }
    }

    public void close() {
        try {
            this.out.close();
        } catch (Exception e) {
        }
        this.out = null;
    }

    public void halt() {
        this.stoped = true;
        this.close();
    }

}
