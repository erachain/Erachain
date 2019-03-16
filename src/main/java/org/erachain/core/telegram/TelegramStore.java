package org.erachain.core.telegram;


import org.erachain.controller.Controller;
import org.erachain.core.transaction.Transaction;
import org.erachain.database.telegram.DGSet;
import org.erachain.network.message.Message;
import org.erachain.network.message.MessageFactory;
import org.erachain.settings.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import java.util.Timer;


/**
 *
 */
public class TelegramStore extends Observable implements Observer {

    static Logger LOGGER = LoggerFactory.getLogger(TelegramStore.class.getName());
	public DGSet database;
    private static TelegramStore th;

    Timer timer;

    public static TelegramStore getInstanse(boolean withObserver, boolean dynamicGUI) {
        if (th == null) th = new TelegramStore(withObserver, dynamicGUI);
        return th;
    }
    public static TelegramStore getInstanse() {
        return th;
    }

	// CONSTRUCTORS

    private TelegramStore(boolean withObserver, boolean dynamicGUI) {
        // OPEN db
        this.database = DGSet.reCreateDB(withObserver, dynamicGUI);
        // start clear olf telegram
        clearOldTelegtams();
    }

	

    @Override
    public void update(Observable arg0, Object arg1) {
        // TODO Auto-generated method stub
        
    }

    
	/**
	 * Thread delete old telegrams<br>
	 * interval 60 sec<br>
	 * alive - aliveTime
	 */
    private void clearOldTelegtams() {
        new Thread() {
            @Override
            public void run() {

                int i = 0;
                while (i++ < 60) {
                    try {
                        sleep(1000);
                    } catch (InterruptedException e) {
                        // need for EXIT
                        return;
                    }

                    if (Controller.getInstance().isOnStopping())
                        return;

                }

                if (Controller.getInstance().isOnStopping())
                    return;

                if (Settings.getInstance().getTelegramStoreUse() && Settings.getInstance().getTelegramStorePeriod() > 0) {
                    int aliveTime = Settings.getInstance().getTelegramStorePeriod() * 60 * 60 * 24 * 1000;
                    for (Transaction transaction : database.getTelegramsMap().getValues()) {
                        if (transaction.getTimestamp() < (System.currentTimeMillis() - aliveTime))
                            database.getTelegramsMap().delete(transaction.viewSignature());
                    }
                }

            }

        }.start();

    }
	
	 /**
	 * get telegrams from address
	 * @param address
	 * @return List (Transaction)
	 */
     public ArrayList<Transaction> getFromCreator(String address) {

         ArrayList<Transaction> list = new ArrayList<Transaction>();
         for (Transaction transaction : this.database.getTelegramsMap().getValues()) {
             if (transaction.getCreator().getAddress().equals(address)) list.add(transaction);
         }
         return list;
     }
	 
	 
	 // delete telegrams from sender
	 /**
	  * delete Telegrams from address
	 * @param address
	 */
	public void deleteFromCreator(String address){
        for (Transaction transaction : database.getTelegramsMap().getValues()) {
             if ( transaction.getCreator().getAddress().equals(address)) 
                 database.getTelegramsMap().delete(transaction.viewSignature());
            }
        
     }
    /**
     * get and delete!!!<br> telegrams from address
     * @param address
     * @return List (Transaction)
     */
    public ArrayList<Transaction> getAndDeleteFromCreator(String address) {

        ArrayList<Transaction> list = new ArrayList<Transaction>();
        for (Transaction transaction : this.database.getTelegramsMap().getValues()) {
            if (transaction.getCreator().getAddress().equals(address)) {
                list.add(transaction);
                database.getTelegramsMap().delete(transaction.viewSignature());
            }
        }
        return list;
    }
     
    public void close() {
        if (this.database != null) {
            this.database.close();
        }
    }

    public void commit() {
        if (this.database != null) {
            this.database.commit();
        }
    }

    public void broadcastGetTelegram(String address) {
        // CREATE MESSAGE
        Message telegram = MessageFactory.getInstance().createTelegramGetMessage();
        // BROADCAST MESSAGE
        Controller.getInstance().network.broadcast(telegram, false);
    }

}
