package org.erachain.network;
// 30/03

import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.network.message.HWeightMessage;
import org.erachain.network.message.Message;
import org.erachain.network.message.MessageFactory;
import org.mapdb.Fun.Tuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Pinger extends Thread {

    private static final Logger LOGGER = LoggerFactory.getLogger(Pinger.class);
    private static final int DEFAULT_PING_TIMEOUT = BlockChain.GENERATING_MIN_BLOCK_TIME_MS >> 1;
    private static final int DEFAULT_QUICK_PING_TIMEOUT = 2000; // BlockChain.GENERATING_MIN_BLOCK_TIME_MS >> 4;

    private Peer peer;
    private boolean needPing = false;
    private int ping;

    public Pinger(Peer peer) {
        this.peer = peer;
        //this.run = true;
        this.ping = Integer.MAX_VALUE;
        this.setName("Pinger - " + this.getId() + " for: " + peer.getAddress().getHostAddress());

        this.start();
    }

    public long getPing() {
        return this.ping;
    }

    public void setPing(int ping) {
        this.ping = ping;
    }

    public void setNeedPing() {
        // пингуем только те что еще нормальные
        if (ping > 0)
            this.needPing = true;
    }

    public boolean tryPing(long timeSOT) {

        //LOGGER.info("try PING " + this.peer);

        peer.addPingCounter();

        //CREATE PING
        Message pingMessage = MessageFactory.getInstance().createGetHWeightMessage();

        if (this.ping >= 0) {
            // на время пингования поставим сразу -1
            this.ping = -1;
        }

        //GET RESPONSE
        long start = System.currentTimeMillis();
        Message response = peer.getResponse(pingMessage, timeSOT);

        if (Controller.getInstance().isOnStopping()
                || this.peer.isStoped()
                || !this.peer.isUsed())
            return false;

            //CHECK IF VALID PING
        if (response == null) {

            //UPDATE PING
            if (this.ping < 0) {
                // учет отказа в секундах
                this.ping -= timeSOT / 1000;
            } else
                this.ping = -1;

            //PING FAILES
            if (this.ping < -20) {
                // если полный отказ уже больше чем ХХХ секнд то ИМЕННО БАН
                this.peer.ban(5, "on PING FAILES");
            }

            return false;

        } else {
            //UPDATE PING
            this.ping = (int) (System.currentTimeMillis() - start) + 1;
        }

        //LOGGER.info("PING " + this.peer);
        Controller.getInstance().getDBSet().getPeerMap().addPeer(peer, 0);

        if (response != null && response.getType() == Message.HWEIGHT_TYPE) {
            HWeightMessage hWeightMessage = (HWeightMessage) response;
            Tuple2<Integer, Long> hW = hWeightMessage.getHWeight();

            Controller.getInstance().setWeightOfPeer(peer, hW);
        }

        return ping >= 0;

    }

    public boolean tryPing() {
        return tryPing(DEFAULT_QUICK_PING_TIMEOUT);
    }
    //public boolean tryQuickPing() {
    //    return tryPing(DEFAULT_QUICK_PING_TIMEOUT);
    //}

    public void run() {

        Controller cnt = Controller.getInstance();
        BlockChain chain = cnt.getBlockChain();

        //int sleepTimeFull = DEFAULT_PING_TIMEOUT; // Settings.getInstance().getPingInterval();
        int sleepTimestep = DEFAULT_QUICK_PING_TIMEOUT >> 3;
        int sleepsteps = DEFAULT_PING_TIMEOUT / sleepTimestep;
        int sleepStepTimeCounter;
        boolean resultSend;
        while (!this.peer.isStoped()) {

            if (this.ping < 0) {
                sleepStepTimeCounter = sleepsteps >> 3;
            } else {
                sleepStepTimeCounter = sleepsteps;
            }

            while (sleepStepTimeCounter > 0) {

                //SLEEP
                try {
                    Thread.sleep(sleepTimestep);
                } catch (InterruptedException e) {
                    //FAILED TO SLEEP
                }

                if (this.peer.isStoped())
                    return;

                // если нужно пингануь - но не часто все равно - так как там могут быстро блоки собираться
                // чтобы не запинговать канал
                if (this.needPing && sleepStepTimeCounter > (sleepsteps >> 4))
                    break;

                sleepStepTimeCounter--;

            }

            if (this.peer.isUsed()) {
                this.needPing = false;
                tryPing();
            } else {
                this.ping = 999999;
            }
        }
    }

}
