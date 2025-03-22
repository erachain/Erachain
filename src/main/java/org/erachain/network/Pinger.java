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

    private static final Logger LOGGER = LoggerFactory.getLogger(Pinger.class.getSimpleName());
    private int DEFAULT_PING_TIMEOUT;
    private static final int DEFAULT_QUICK_PING_TIMEOUT = 5000;

    private Peer peer;
    private int ping;

    BlockingQueue<Integer> startPinging = new ArrayBlockingQueue<Integer>(1);

    public Pinger(Peer peer) {
        this.peer = peer;
        this.ping = Integer.MAX_VALUE;
        this.setName("Pinger-" + this.getId() + " for: " + peer.getName());

        DEFAULT_PING_TIMEOUT = 30000; //BlockChain.GENERATING_MIN_BLOCK_TIME_MS(Controller.getInstance().getMyHeight()) << 2;

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
        this.startPinging.offer(1);
    }

    private boolean tryPing(long timeSOT) {

        if (peer.LOG_GET_HWEIGHT_TYPE) {
            LOGGER.info("try PING " + this.peer);
        }

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

            //PING FAILS
            // чем меньше пиров на связи тем дольше пингуем перед разрывом
            if (this.ping < -30 - 60 / (1 + peer.network.banForActivePeersCounter())) {
                // если полный отказ уже больше чем ХХХ секнд то ИМЕННО БАН

                // И если тут не оборать, то на этапе получения подписей по блокам тогда разрыв будет - ответ не получается
                this.peer.ban("on PING FAILS");
            }

            return false;

        } else {
            //UPDATE PING
            this.ping = (int) (System.currentTimeMillis() - start) + 1;
        }

        //logger.info("PING " + this.peer);
        Controller.getInstance().getDLSet().getPeerMap().addPeer(peer, 0);

        if (response.getType() == Message.HWEIGHT_TYPE) {
            HWeightMessage hWeightMessage = (HWeightMessage) response;
            Tuple2<Integer, Long> hW = hWeightMessage.getHWeight();

            peer.setHWeight(hW);
        }

        return ping >= 0;

    }

    public boolean tryPing() {
        return tryPing(DEFAULT_QUICK_PING_TIMEOUT);
    }

    public void run() {

        Controller cnt = Controller.getInstance();
        BlockChain chain = cnt.getBlockChain();

        Integer deal = 0;
        while (this.peer.network.run) {

            try {
                startPinging.take();
            } catch (InterruptedException e) {
                break;
            }

            try {
                // дадм время на запуск с той тороны
                Thread.sleep(200);
            } catch (InterruptedException e) {
                return;
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

        //logger.debug(this + " - halted");

    }

    public void close() {
        startPinging.offer(-1);
    }

}
