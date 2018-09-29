package network.message;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.activetree.common.utils.Base64;
import com.google.common.primitives.Bytes;

import controller.Controller;
import core.transaction.R_Send;
import core.transaction.Transaction;

public class TelegramAnswerGetMessage extends Message {

     private String address;
     private JSONObject result;
    

    @SuppressWarnings("unchecked")
    public TelegramAnswerGetMessage(String address, String typeTelegramanswer) {
        super(TELEGRAM_ANSWER_GET_TYPE);
       JSONObject list = new JSONObject();
       result = new JSONObject();
       
        this.address = address;
       ArrayList<Transaction> array = Controller.getInstance().telegtamm.getFromCreator(address);
       int i = 0;
       for (Transaction transaction:array){
          // save in Base64 
           list.put(i+"", Base64.encode(((R_Send)transaction).toBytes(Transaction.FOR_NETWORK, true)));
          i++;
       }
       result.put("type", typeTelegramanswer);
       result.put("list",list);
    }

    public static ArrayList<Transaction> parse(byte[] data) throws Exception {
        //PARSE TO JSON
        ArrayList<Transaction> list = new ArrayList<Transaction>();
        JSONObject json = (JSONObject) JSONValue
                .parseWithException(new String(data, Charset.forName("UTF-8")));
        @SuppressWarnings("rawtypes")
        Iterator it = json.keySet().iterator();
        while(it.hasNext()){
            String stringBase64 = (String) it.next();
           byte[] bytes = Base64.decode(stringBase64);
           list.add(R_Send.Parse(bytes, Transaction.FOR_NETWORK));
        }

        return list;
    }

    public String getAddress() {
        return this.address;
    }
   
    public JSONObject getResult() {
        return this.result;
    }

    public boolean isRequest() {
        return false;
    }

    public byte[] toBytes() {
        byte[] data = new byte[0];

        //WRITE JSON
        byte[] addressBytes = this.result.toJSONString().getBytes();
        data = Bytes.concat(data, addressBytes);

        //ADD CHECKSUM
        data = Bytes.concat(super.toBytes(), this.generateChecksum(data), data);

        return data;
    }


    @Override
    public int getDataLength() {
        return this.result.toJSONString().getBytes().length;
    }

}
