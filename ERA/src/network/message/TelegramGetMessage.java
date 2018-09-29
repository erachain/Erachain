package network.message;

import java.util.Arrays;

import org.bouncycastle.util.Strings;
import org.json.simple.JSONObject;

import com.google.common.primitives.Bytes;

public class TelegramGetMessage extends Message {

    private String address;

    public TelegramGetMessage(String address) {
        super(TELEGRAM_GET_TYPE);
        this.address = address;
    }

    public static TelegramGetMessage parse(byte[] data) throws Exception {
        //PARSE TRANSACTION
       String address1 =Strings.fromByteArray(data);

        return new TelegramGetMessage(address1);
    }

    public String getAddress() {
        return this.address;
    }

    public boolean isRequest() {
        return false;
    }

    public byte[] toBytes() {
        byte[] data = new byte[0];

        //WRITE Address
        byte[] addressBytes = this.address.getBytes();
        data = Bytes.concat(data, addressBytes);

        //ADD CHECKSUM
        data = Bytes.concat(super.toBytes(), this.generateChecksum(data), data);

        return data;
    }


    public TelegramGetMessage copy() {
        try {
            byte[] data = this.toBytes();
            int position = Message.MAGIC_LENGTH + TYPE_LENGTH + 1 + MESSAGE_LENGTH + CHECKSUM_LENGTH;
            data = Arrays.copyOfRange(data, position, data.length);
            return TelegramGetMessage.parse(data);
        } catch (Exception e) {
            return null;
        }
    }


    @SuppressWarnings("unchecked")
    public JSONObject toJson() {

        //DCSet localDCSet = DCSet.getInstance();
        JSONObject jSON = new JSONObject();

        jSON.put("address", this.address);

        return jSON;

    }

    @Override
    public int getDataLength() {
        return this.address.getBytes().length;
    }

}
