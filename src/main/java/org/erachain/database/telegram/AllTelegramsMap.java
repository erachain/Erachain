package org.erachain.database.telegram;

import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.transaction.Transaction;
import org.erachain.database.DBMap;
import org.erachain.database.serializer.TransactionSerializer;
import org.erachain.utils.ObserverMessage;
import org.mapdb.BTreeKeySerializer;
import org.mapdb.DB;

public class AllTelegramsMap extends DBMap<String, Transaction> {

    public AllTelegramsMap(DGSet dWSet, DB database) {
        super(dWSet, database);

        if (databaseSet.isWithObserver()) {
            this.observableData.put(DBMap.NOTIFY_RESET, ObserverMessage.ALL_TELEGRAM_RESET_TYPE);
            this.observableData.put(DBMap.NOTIFY_LIST, ObserverMessage.ALL_TELEGRAMT_LIST_TYPE);
            this.observableData.put(DBMap.NOTIFY_ADD, ObserverMessage.ALL_TELEGRAMT_ADD_TYPE);
            this.observableData.put(DBMap.NOTIFY_REMOVE, ObserverMessage.ALL_TELEGRAMT_REMOVE_TYPE);
        }
    }

   
    @SuppressWarnings("unchecked")
    @Override
    protected void getMap() {
      //OPEN MAP
       map = database.createTreeMap("telegrams")
               .keySerializer(BTreeKeySerializer.BASIC)
               .valueSerializer(new TransactionSerializer())
               .counterEnable()
               .makeOrGet();
    }

    @Override
    protected void getMemoryMap() {
    }

    @Override
    protected Transaction getDefaultValue() {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    protected void createIndexes() {
        // TODO Auto-generated method stub
              
    }
    
    public boolean add(String signature, Transaction telegramMessage) {
        return this.set(signature, telegramMessage);
    }

    public void deleteFromAccount(PublicKeyAccount account) {
        // TODO Auto-generated method stub
        
    }
    
   
}
