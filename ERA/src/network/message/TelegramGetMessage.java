package network.message;

import java.nio.charset.Charset;

import com.google.common.primitives.Bytes;

public class TelegramGetMessage extends Message {

    String address;
   

    public TelegramGetMessage(String address) {
        super(TELEGRAM_GET_TYPE);
       this.address = address;
    }

    public static TelegramGetMessage parse(byte[] data){
      
       return new TelegramGetMessage(new String(data, Charset.forName("UTF-8")));
    }
    
    public byte[] toBytes() {
        byte[] data = new byte[0];

        //WRITE BLOCK
        byte[] bytes = this.address.getBytes();
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
        return address.getBytes().length;
    }
   public String getAddress(){
       return this.address;
   }

}
