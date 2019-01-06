package org.erachain.network;
// 30/03

import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.datachain.DCSet;
import org.erachain.network.message.HWeightMessage;
import org.erachain.network.message.Message;
import org.erachain.network.message.MessageFactory;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.mapdb.Fun.Tuple2;
import org.erachain.settings.Settings;

public class Pinger extends Thread {

    private static final Logger LOGGER = LoggerFactory.getLogger(Pinger.class);
    private static final int DEFAULT_PING_TIMEOUT = 15000;
    private static final int DEFAULT_QUICK_PING_TIMEOUT = BlockChain.GENERATING_MIN_BLOCK_TIME_MS >> 4;

    private Peer peer;
    private boolean needPing = false;
    //private boolean run;
    private int ping;
    private Message messageWinBlock;
    private Message messageQueue;
    private Message messageQueuePing;

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
        this.needPing = true;
    }

    public synchronized void setMessageQueue(Message message) {
        while (messageQueue != null) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
            }
        }
        this.messageQueue = message;
    }

    public synchronized void setMessageWinBlock(Message message) {
        while (messageWinBlock != null) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
            }
        }
        this.messageWinBlock = message;
    }

    public synchronized void setMessageQueuePing(Message message) {
        while (messageQueuePing != null) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
            }
        }
        this.messageQueuePing = message;
    }
	
	/*
	public boolean isRun()
	{
		return this.run;
	}
	*/

    public boolean tryPing(long timeSOT) {

        //LOGGER.info("try PING " + this.peer);

        peer.addPingCounter();

        //CREATE PING
        //Message pingMessage = MessageFactory.getInstance().createPingMessage();
        // TODO remove it and set HWeigtn response
        // TODO make wait SoTome 10 when ping
        Message pingMessage = MessageFactory.getInstance().createGetHWeightMessage();

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
                if (DEFAULT_PING_TIMEOUT <= timeSOT) {
                    // если пинги не частые были то учтем как попытку
                    this.ping -= 1;
                }
            } else
                this.ping = -1;

            //PING FAILES
            if (false && this.ping < -4) {
                this.peer.ban(10, "on PING FAILES");
                return false;
            }

        } else {
            //UPDATE PING
            this.ping = (int) (System.currentTimeMillis() - start);
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
        return tryPing(DEFAULT_PING_TIMEOUT);
    }
    public boolean tryQuickPing() {
        return tryPing(DEFAULT_QUICK_PING_TIMEOUT);
    }

    public void run() {

        Controller cnt = Controller.getInstance();
        BlockChain chain = cnt.getBlockChain();

        int sleepTimeFull = Settings.getInstance().getPingInterval();
        int sleepTimestep = 1;
        int sleepsteps = sleepTimeFull / sleepTimestep;
        int sleepStepTimeCounter;
        boolean resultSend;
        while (!this.peer.isStoped()) {

            if (this.ping < 0) {
                sleepStepTimeCounter = (DEFAULT_PING_TIMEOUT<<2) / sleepTimestep;
            } else if (this.ping > DEFAULT_PING_TIMEOUT) {
                sleepStepTimeCounter = (DEFAULT_PING_TIMEOUT<<2) / sleepTimestep;
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

                if (!this.peer.isUsed()) {

                    sleepStepTimeCounter = sleepsteps;

                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        //FAILED TO SLEEP
                    }

                    if (this.peer.isStoped())
                        return;

                    continue;
                }

                sleepStepTimeCounter--;

                if (messageQueue != null) {
                    //LOGGER.debug("try ASYNC sendMessage " + messageQueue.viewType() + " - " + this.peer);

                    resultSend = this.peer.sendMessage(messageQueue);
                    messageQueue = null;

                    if (!resultSend)
                        continue;

                    //LOGGER.debug("try ASYNC send " + messageQueue.viewType() + " " + this.peer.getAddress() + " @ms " + (System.currentTimeMillis() - start));

                }

                if (messageWinBlock != null) {
                    //LOGGER.debug("try ASYNC send WINblock " + messageQueue.viewType() + " - " + this.peer);

                    resultSend = this.peer.sendMessage(messageWinBlock);
                    messageWinBlock = null;

                    if (!resultSend)
                        continue;

                    //LOGGER.debug("try ASYNC send WINblock " + messageQueue.viewType() + " " + this.peer + " @ms " + (System.currentTimeMillis() - start));

                }

                if (this.messageQueuePing != null) {
                    // PING before and THEN send
                    //LOGGER.debug("try ASYNC PING sendMessage " + messageQueuePing.viewType() + " - " + this.peer);
                    ///this.peer.so(message);

                    this.tryQuickPing();
                    this.peer.sendMessage(this.messageQueuePing);

                    this.needPing = false;
                    this.messageQueuePing = null;
                    sleepStepTimeCounter = sleepsteps;
                    continue;

                } else if (this.needPing) {
                    // PING NOW
                    this.needPing = false;
                    this.tryPing();
                    sleepStepTimeCounter = sleepsteps;
                    continue;
                }

            }

            if (this.peer.isUsed()) {
                tryPing();
            }

        }
    }

}
