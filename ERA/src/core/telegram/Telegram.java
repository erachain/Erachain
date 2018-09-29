package core.telegram;
// 09/03

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.apache.log4j.Logger;

import controller.Controller;
import core.transaction.Transaction;
import database.telegram.TelegramSet;
import network.Peer;
import network.message.Message;
import network.message.MessageFactory;



public class Telegram extends Observable implements Observer {
    public static String TYPE_ANSWER_GET_LIST_TELEGRAMS = "get list";
	
	static Logger LOGGER = Logger.getLogger(Telegram.class.getName());
	public TelegramSet database;
    // time alive telegram
	protected long aliveTime = 24*60*60*1000;
   private static  Telegram th;
	
	public static Telegram getInstanse(){
	 if (th== null) th = new Telegram();
	 return th;
	}

	// CONSTRUCTORS

	private Telegram() {
		// OPEN db
			this.database = new TelegramSet();
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
	private void clearOldTelegtams(){
	 new Thread(){
	     @Override
         public void run() {
             try {
                sleep(60*1000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
           for ( Transaction transaction:database.getTelegramsMap().getValuesAll()){
             if ( transaction.getTimestamp()< (System.currentTimeMillis()-aliveTime)) 
                 database.getTelegramsMap().delete(transaction.viewSignature());
            }
            
         }
	     
	 }.start();
	    
	}
	
	 /**
	 * get telegrams from address 
	 * @param address
	 * @return List (Transaction)
	 */
	public ArrayList<Transaction> getFromCreator(String address){
	        
	     ArrayList<Transaction> list = new ArrayList<Transaction>();
	     for ( Transaction transaction:this.database.getTelegramsMap().getValuesAll()){
	      if ( transaction.getCreator().getAddress().equals(address)) list.add(transaction);
	     }
	      return list;
	    }
	 
	 
	 // delete telegrams from sender
	 /**
	  * delete Telegrams from address
	 * @param address
	 */
	public void deleteFromCreator(String address){
	     for ( Transaction transaction:database.getTelegramsMap().getValuesAll()){
             if ( transaction.getCreator().getAddress().equals(address)) 
                 database.getTelegramsMap().delete(transaction.viewSignature());
            }
        
     }
    /**
     * get and delete!!!<br> telegrams from address 
     * @param address
     * @return List (Transaction)
     */
    public ArrayList<Transaction> getAndDeleteFromCreator(String address){
            
         ArrayList<Transaction> list = new ArrayList<Transaction>();
         for ( Transaction transaction:this.database.getTelegramsMap().getValuesAll()){
          if ( transaction.getCreator().getAddress().equals(address)) {
              list.add(transaction);
              database.getTelegramsMap().delete(transaction.viewSignature());
          }
         }
          return list;
        }
    
    public void getTelegrams(String adderess){
    // CREATE MESSAGE
    Message telegram = MessageFactory.getInstance().createTelegramGetMessage(adderess);

  
        // BROADCAST MESSAGE
        List<Peer> excludes = new ArrayList<Peer>();
        excludes = Controller.getInstance().getActivePeers();
        Controller.getInstance().network.asyncBroadcast(telegram, excludes, false);
   
    }

}
