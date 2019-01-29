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

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class Pinger extends Thread {

    private static final Logger LOGGER = LoggerFactory.getLogger(Pinger.class);
    private static final int DEFAULT_PING_TIMEOUT = BlockChain.GENERATING_MIN_BLOCK_TIME_MS;
    private static final int DEFAULT_QUICK_PING_TIMEOUT = 5000; // BlockChain.GENERATING_MIN_BLOCK_TIME_MS >> 4;

    private Peer peer;
    private boolean needPing = false;
    private int ping;

    BlockingQueue<Integer> startPinging = new ArrayBlockingQueue<Integer>(1);

    public Pinger(Peer peer) {
        this.peer = peer;
        this.ping = Integer.MAX_VALUE;
        this.setName("Pinger - " + this.getId() + " for: " + peer.getName());

        this.start();
    }

    public void init() {
        this.ping = Integer.MAX_VALUE;
        startPinging.offer(0);
    }

    public long getPing() {
        return this.ping;
    }

    public void setPing(int ping) {
        this.ping = ping;
        startPinging.offer(-1);
    }

    public void setNeedPing() {
        this.needPing = true;
    }

    private boolean tryPing(long timeSOT) {

        //LOGGER.info("try PING " + this.peer);

        peer.addPingCounter();

        //CREATE PING
        Message pingMessage = MessageFactory.getInstance().createGetHWeightMessage();

        //GET RESPONSE
        long start = System.currentTimeMillis();
        Message response = peer.getResponse(pingMessage, timeSOT);

        if (Controller.getInstance().isOnStopping()
                || !this.peer.network.run
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
            // чем меньше пиров на связи тем дольше пингуем перед разрвом
            if (this.ping < -10 -20/(1 + peer.network.banForActivePeersCounter())) {
                // если полный отказ уже больше чем ХХХ секнд то ИМЕННО БАН
                this.peer.ban("on PING FAILES");
            }

            return false;

        } else {
            //UPDATE PING
            this.ping = (int) (System.currentTimeMillis() - start) + 1;
        }

        //LOGGER.info("PING " + this.peer);
        Controller.getInstance().getDBSet().getPeerMap().addPeer(peer, 0);

        if (response.getType() == Message.HWEIGHT_TYPE) {
            HWeightMessage hWeightMessage = (HWeightMessage) response;
            Tuple2<Integer, Long> hW = hWeightMessage.getHWeight();

            Controller.getInstance().setWeightOfPeer(peer, hW);
        }

        return ping >= 0;

    }

    public boolean tryPing() {
        return tryPing(DEFAULT_QUICK_PING_TIMEOUT);
    }

    public void run() {

        Controller cnt = Controller.getInstance();
        BlockChain chain = cnt.getBlockChain();

        int sleepTimestep = 100;
        int sleepsteps = DEFAULT_PING_TIMEOUT / sleepTimestep;
        int sleepStepTimeCounter;
        boolean resultSend;

        Integer deal = 0;
        while (this.peer.network.run) {

            try {
                startPinging.take();
            } catch (InterruptedException e) {
                break;
            }

            Controller.getInstance().onConnect(this.peer);

            while (this.peer.isUsed() && this.peer.network.run) {

                try {
                    deal = startPinging.poll(DEFAULT_PING_TIMEOUT, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    break;
                }

                if (deal != null && deal < 0)
                    // сбросить ожидание счетчика
                    continue;

                if (!this.peer.isUsed() || !this.peer.network.run)
                    break;

                tryPing();

                if (!this.peer.isUsed() || !this.peer.network.run)
                    break;

            }
        }

        LOGGER.info(this + " - halted");

    }

    public void close() {
        startPinging.offer(-1);
    }

}
