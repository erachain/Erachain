package network.message;

import java.util.ArrayList;
import java.util.Set;

import org.bouncycastle.util.Strings;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.google.common.primitives.Bytes;

import controller.Controller;
import core.crypto.Base64;
import core.transaction.R_Send;
import core.transaction.Transaction;
import utils.StrJSonFine;

public class TelegramGetAnswerMessage extends Message {

    private String senderAccount;
    private ArrayList<Transaction> telegransList;
    private JSONObject json;

   

    public TelegramGetAnswerMessage(JSONObject json) {
        super(TELEGRAM_GET_ANSVER_TYPE);
        // get list Telegrams
        if(json.containsKey("list")){
            JSONObject jsonTelegrams = (JSONObject) json.get("list");
            @SuppressWarnings("unchecked")
            Set<Integer> telegKeySet = jsonTelegrams.keySet();
            for (Integer tel:telegKeySet){
                byte[] transactionByte = Base64.decode((String) jsonTelegrams.get(tel));
                Transaction trans = null;
                try {
                    trans = R_Send.Parse(transactionByte, Transaction.FOR_NETWORK);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                telegransList.add(trans);
            };
        } 
         
    }
    @SuppressWarnings("unchecked")
    public TelegramGetAnswerMessage(String account) {
        // TODO Auto-generated constructor stub
        super(TELEGRAM_GET_ANSVER_TYPE);
     // TODO Auto-generated method stub
        telegransList = new ArrayList<Transaction>();
        
         this.senderAccount = account;
        // read telegram
        json = new JSONObject();
        
        Set<String> telegramKeys = Controller.getInstance().telegram.database.getTelegramsMap().getKeys();
        // add List
        for(String key:telegramKeys){
            telegransList.add(Controller.getInstance().telegram.database.getTelegramsMap().get(key));
            
        }
        // add JSON
        JSONObject jsonList = new JSONObject();
        for(int i = 0;i<telegransList.size(); i++){
            Transaction tran = telegransList.get(i);
            // convert Base64
            String base64 = Base64.encode(tran.toBytes(Transaction.FOR_NETWORK, true));
            jsonList.put(i, base64);
          }
        // add lict in resut
        json.put("list", jsonList);
        
    }

   

    public static TelegramGetAnswerMessage parse(byte[] data) throws Exception {
        //PARSE TRANSACTION
        int length = data.length;
        // convert byte to  String
        String jsonString = Strings.fromByteArray(data);
         // convert String to JSONOblect       
        JSONObject jsonObj = (JSONObject) JSONValue.parse(jsonString);
        return new TelegramGetAnswerMessage(jsonObj);
    }

    public boolean isRequest() {
        return false;
    }

    public byte[] toBytes() {
        byte[] data = new byte[0];

        //WRITE BLOCK
        
        
        // convert to bytes
        byte[] telegramBytes = StrJSonFine.convert(json).getBytes();
        
        data = Bytes.concat(data, telegramBytes);

        //ADD CHECKSUM
        data = Bytes.concat(super.toBytes(), this.generateChecksum(data), data);

        return data;
    }


  

    @Override
    public int getDataLength() {
        return toBytes().length;
    }
    /**
     * @return the telegransList
     */
    public ArrayList<Transaction> getTelegransList() {
        return telegransList;
    }

}
