package org.erachain.network.message;

import com.google.common.primitives.Bytes;
import org.bouncycastle.util.Strings;
import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.core.transaction.RSend;
import org.erachain.core.transaction.Transaction;
import org.erachain.utils.StrJSonFine;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.util.ArrayList;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;

public class TelegramAnswerMessage extends Message {

    private ArrayList<String> senderAccount;
    private ArrayList<Transaction> telegransList;
    private JSONObject json;
    private byte[] dataBytes;


    public TelegramAnswerMessage(JSONObject json) {
        super(TELEGRAM_ANSWER_TYPE);
        // get list Telegrams
        telegransList = new ArrayList<Transaction>();
        if(json.containsKey("list")){
            JSONObject jsonTelegrams = (JSONObject) json.get("list");
            @SuppressWarnings("unchecked")
            Set<Integer> telegKeySet = jsonTelegrams.keySet();
            for (int i = 0; i<telegKeySet.size(); i++){
                byte[] transactionByte = Base64.getDecoder().decode((String) jsonTelegrams.get(i + ""));
                Transaction trans = null;
                try {
                    trans = RSend.Parse(transactionByte, Transaction.FOR_NETWORK);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                telegransList.add(trans);
            };
        } 
         
    }
    @SuppressWarnings("unchecked")
    public TelegramAnswerMessage(ArrayList<String> accounts) {
        // TODO Auto-generated constructor stub
        super(TELEGRAM_ANSWER_TYPE);
     // TODO Auto-generated method stub
        telegransList = new ArrayList<Transaction>();

        this.senderAccount = accounts;
                
        // read telegram
        json = new JSONObject();

        Set<String> telegramKeys = Controller.getInstance().telegramStore.database.getTelegramsMap().keySet();
        // add List
        for(String key:telegramKeys){
            // senders
            Transaction tran = Controller.getInstance().telegramStore.database.getTelegramsMap().get(key);
            if (senderAccount.contains(tran.viewCreator()) && !telegransList.contains(tran)) {
                telegransList.add(tran);
                continue;
            }
            // receivers
            if (Controller.getInstance().doesWalletKeysExists()) {
                HashSet<Account> recipients = tran.getRecipientAccounts();
                for (Account recipient : recipients) {
                    if (Controller.getInstance().getWallet().accountExists(recipient) && !telegransList.contains(tran))
                        telegransList.add(tran);
                }
            }
        }
        // add JSON
        JSONObject jsonList = new JSONObject();
        for(int i = 0;i<telegransList.size(); i++){
            Transaction tran = telegransList.get(i);
            // convert Base64
            String base64 = Base64.getEncoder().encodeToString(tran.toBytes(Transaction.FOR_NETWORK, true));
            jsonList.put(i, base64);
          }

        json.put("list", jsonList);
        
    }


    public static TelegramAnswerMessage parse(byte[] data) throws Exception {
        //PARSE TRANSACTION
        
        // convert byte to  String
        String jsonString = Strings.fromByteArray(data);
         // convert String to JSONOblect       
        JSONObject jsonObj = (JSONObject) JSONValue.parse(jsonString);
        return new TelegramAnswerMessage(jsonObj);
    }

    public boolean isRequest() {
        return false;
    }

    public byte[] toBytes() {
        dataBytes = new byte[0];

        //WRITE BLOCK
        
        
        // convert to bytes
        byte[] telegramBytes = StrJSonFine.convert(json).getBytes();
        
        dataBytes = Bytes.concat(dataBytes, telegramBytes);

        //ADD CHECKSUM
        dataBytes = Bytes.concat(super.toBytes(), this.generateChecksum(dataBytes), dataBytes);
        return dataBytes;
    }


  

    @Override
    public int getDataLength() {
        return dataBytes.length;
    }
    /**
     * @return the telegransList
     */
    public ArrayList<Transaction> getTelegransList() {
        return telegransList;
    }

    public void saveToWallet() {
        for (Transaction transaction : telegransList) {
            Controller.getInstance().addTelegramToWallet(transaction, transaction.viewSignature());
        }
    }

}
