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

    private static final Logger LOGGER = LoggerFactory.getLogger(Sender.class);
    private static final int QUEUE_LENGTH = BlockChain.DEVELOP_USE? 200 : 40;
    BlockingQueue<Message> blockingQueue = new ArrayBlockingQueue<Message>(QUEUE_LENGTH);

    private Peer peer;
    public OutputStream out;

    private GetHWeightMessage getHWeightMessage;
    private HWeightMessage hWeightMessage;
    private BlockWinMessage winBlockToSend;

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
        return blockingQueue.offer(message);
    }

    public boolean offer(Message message, long SOT) {
        try {
            return blockingQueue.offer(message, SOT, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            return false;
        }
    }

    //public void put(Message message) {
    //    try {
    //        blockingQueue.put(message);
    //    } catch (InterruptedException e) {
    //    }
    //}

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
        String error = null;

        //SEND MESSAGE
        long checkTime = System.currentTimeMillis();

        // пока есть входы по sendMessage (org.erachain.network.Peer.directSendMessage) - нужно ждать синхрон
        if (this.out == null)
            return false;
        synchronized (this.out) {
            try {
                this.out.write(bytes);
                this.out.flush();
            } catch (java.lang.OutOfMemoryError e) {
                Controller.getInstance().stopAll(85);
                return false;
            } catch (java.lang.NullPointerException e) {
                return false;
            } catch (EOFException e) {
                if (this.out == null)
                    // это наш дисконект
                    return false;

                checkTime = System.currentTimeMillis() - checkTime;
                if (checkTime - 3 > bytes.length >> 3) {
                    LOGGER.debug(this.peer + message.viewPref(true)
                            + message + " sended by period: " + checkTime);
                }
                error = "try out.write 1a - " + e.getMessage();
            } catch (java.net.SocketException eSock) {
                if (this.out == null)
                    // это наш дисконект
                    return false;

                checkTime = System.currentTimeMillis() - checkTime;
                if (checkTime - 3 > bytes.length >> 3) {
                    LOGGER.debug(this.peer + message.viewPref(true) + message + " sended by period: " + checkTime);
                }
                error = "try out.write 1 - " + eSock.getMessage();
            } catch (IOException e) {
                if (this.out == null)
                    // это наш дисконект
                    return false;

                checkTime = System.currentTimeMillis() - checkTime;
                if (checkTime - 3 > bytes.length >> 3) {
                    LOGGER.debug(this.peer + message.viewPref(true) + message + " sended by period: " + checkTime);
                }
                error = "try out.write 2 - " + e.getMessage();
            }
        }

        if (error != null) {
            peer.ban(error);
            return false;
        }

        if (USE_MONITOR) this.setMonitorStatusAfter();

        checkTime = System.currentTimeMillis() - checkTime;
        if (checkTime - 3 > (bytes.length >> 3)
                || logPings && (message.getType() == Message.GET_HWEIGHT_TYPE || message.getType() == Message.HWEIGHT_TYPE)) {
            LOGGER.debug(this.peer + message.viewPref(true) + message + " sended by period: " + checkTime);
        }

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

        LOGGER.info(this + " - halted");
    }

    public void close() {
        this.out = null;
    }

    public void halt() {
        this.close();
    }

}
