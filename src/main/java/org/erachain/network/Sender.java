package org.erachain.network;
// 30/03

import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.core.block.Block;
import org.erachain.datachain.BlockMap;
import org.erachain.network.message.BlockMessage;
import org.erachain.network.message.HWeightMessage;
import org.erachain.network.message.Message;
import org.erachain.network.message.MessageFactory;
import org.erachain.utils.MonitoredThread;
import org.mapdb.Fun.Tuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class Sender extends MonitoredThread {

    private final static boolean USE_MONITOR = true;
    private final static boolean logPings = false;

    private static final Logger LOGGER = LoggerFactory.getLogger(Sender.class);
    private static final int QUEUE_LENGTH = 20;
    BlockingQueue<Message> blockingQueue = new ArrayBlockingQueue<Message>(QUEUE_LENGTH);

    private Peer peer;
    public OutputStream out;

    private boolean needPing = false;
    private int ping;

    private boolean stoped;

    private HWeightMessage hWeightMessage;
    private BlockMessage winBlockToSend;

    public Sender(Peer peer) {
        this.peer = peer;
        this.ping = Integer.MAX_VALUE;
        this.setName("Sender - " + this.getId() + " for: " + peer.getAddress().getHostAddress());

        this.start();
    }

    public long getPing() {
        return this.ping;
    }

    public void putMessage(Message message) {
        this.ping = ping;
    }

    public void setNeedPing() {
        // пингуем только те что еще нормальные
        if (ping > 0)
            this.needPing = true;
    }

    public boolean sendMessage(Message message) {
        //CHECK IF SOCKET IS STILL ALIVE
        if (this.out == null) {
            return false;
        }

        if (!this.peer.socket.isConnected()) {
            //ERROR
            peer.ban(0, "SEND - socket not still alive");

            return false;
        }

        byte[] bytes = message.toBytes();

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
            peer.ban("try out.write 1 - " + eSock.getMessage());
            return false;
        } catch (IOException e) {
            if (this.out == null)
                // это наш дисконект
                return false;

            checkTime = System.currentTimeMillis() - checkTime;
            if (checkTime > bytes.length >> 3) {
                LOGGER.debug(this + " >> " + message + " sended by period: " + checkTime);
            }
            peer.ban("try out.write 2 - " + e.getMessage());
            return false;
        } catch (Exception e) {
            checkTime = System.currentTimeMillis() - checkTime;
            if (checkTime > bytes.length >> 3) {
                LOGGER.debug(this + " >> " + message + " sended by period: " + checkTime);
            }
            peer.ban("try out.write 3 - " + e.getMessage());
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

        Controller cnt = Controller.getInstance();

        while (!this.stoped) {

            Message message = null;
            try {
                message = blockingQueue.poll(500, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                //if (this.stoped)
                break;
            }

            if (hWeightMessage != null)
                if (!sendMessage(hWeightMessage))
                    continue;

            if (winBlockToSend != null)
                if (!sendMessage(winBlockToSend))
                    continue;

            if (message == null)
                continue;

            sendMessage(message);
        }
    }

    public void close() {
        try {
            this.out.close();
        } catch (IOException e) {
        }
        this.out = null;
    }

    public void halt() {
        this.stoped = true;
    }

}
