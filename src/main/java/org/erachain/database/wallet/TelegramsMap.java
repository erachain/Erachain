package org.erachain.database.wallet;

import java.util.HashMap;
import java.util.Map;

import org.mapdb.BTreeKeySerializer;
import org.mapdb.DB;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.transaction.Transaction;
import org.erachain.database.DBMap;
import org.erachain.database.serializer.NameSerializer;
import org.erachain.database.serializer.TransactionSerializer;
import org.erachain.datachain.DCMap;
import org.erachain.network.message.TelegramMessage;
import org.erachain.utils.ObserverMessage;

public class TelegramsMap extends  DCMap<String , Transaction> {
    
    private Map<Integer, Integer> observableData = new HashMap<Integer, Integer>();

    public TelegramsMap(DWSet dWSet, DB database) {
        super(dWSet, database);

        this.observableData.put(DBMap.NOTIFY_RESET, ObserverMessage.WALLET_RESET_TELEGRAM_TYPE);
        this.observableData.put(DBMap.NOTIFY_ADD, ObserverMessage.WALLET_ADD_TELEGRAM_TYPE);
        this.observableData.put(DBMap.NOTIFY_REMOVE, ObserverMessage.WALLET_REMOVE_TELEGRAM_TYPE);
        this.observableData.put(DBMap.NOTIFY_LIST, ObserverMessage.WALLET_LIST_TELEGRAM_TYPE);
    }

   
    @Override
    protected Map<String, Transaction> getMap(DB database) {
      //OPEN MAP
        return database.createTreeMap("telegrams1")
                .keySerializer(BTreeKeySerializer.STRING)
                .valueSerializer(new  TransactionSerializer())
                .counterEnable()
                .makeOrGet();
    }

    @Override
    protected Map<String, Transaction> getMemoryMap() {
        // TODO Auto-generated method stub
        return getMemoryMap();
    }

    @Override
    protected Transaction getDefaultValue() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected Map<Integer, Integer> getObservableData() {
        // TODO Auto-generated method stub
        return this.observableData;
    }

    @Override
    protected void createIndexes(DB database) {
        // TODO Auto-generated method stub
        
    }
    
    public boolean add(String signature, Transaction telegramMessage) {
        return this.set(signature, telegramMessage);
    }

    public void deleteFromAccount(PublicKeyAccount account) {
        // TODO Auto-generated method stub
        
    }

}
