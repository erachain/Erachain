package org.erachain.network.message;

import com.google.common.primitives.Bytes;
import org.bouncycastle.util.Strings;
import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.util.List;
import java.util.Set;

public class TelegramGetMessage extends Message {

    JSONObject address;
   

    @SuppressWarnings("unchecked")
    public TelegramGetMessage() {
        super(TELEGRAM_GET_TYPE);
        address = new JSONObject();
        if (Controller.getInstance().doesWalletKeysExists()) {
            List<Account> acounts = Controller.getInstance().getWallet().getAccounts();
            for (int i = 0; i < acounts.size(); i++) {
                this.address.put(i, acounts.get(i).getAddress());
            }
        }
    }
      
       

    public TelegramGetMessage(JSONObject account) {
        super(TELEGRAM_GET_TYPE);
            // TODO Auto-generated constructor stub
        address = new JSONObject();
        Set<Integer> telegKeySet = account.keySet();
        for (int i = 0; i<telegKeySet.size(); i++){
            
            address.put(i, account.get(i+""));
        };
        }

    public static TelegramGetMessage parse(byte[] data){
        
        // convert byte to  String
        String jsonString = Strings.fromByteArray(data);
         // convert String to JSONOblect       
       
       return new TelegramGetMessage((JSONObject) JSONValue.parse(jsonString));
    }
    
    public byte[] toBytes() {
        byte[] data = new byte[0];

        //WRITE BLOCK
        byte[] bytes = this.address.toJSONString().getBytes();
        data = Bytes.concat(data, bytes);

        //ADD CHECKSUM
        data = Bytes.concat(super.toBytes(), this.generateChecksum(data), data);
        
        return data;
    }

    
    public boolean isRequest() {
        return false;
    }

   @Override
    public int getDataLength() {
        return address.toJSONString().getBytes().length;
    }
   public  JSONObject getAddress(){
       return this.address;
   }

}
