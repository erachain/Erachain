package org.erachain.network.message;

import java.util.Arrays;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;

import org.erachain.controller.Controller;
import org.erachain.core.crypto.Crypto;
import org.erachain.network.Peer;

public abstract class Message {

    public static final byte[] MAINNET_MAGIC = {0x19, 0x66, 0x08, 0x21};

    public static final int MAGIC_LENGTH = 4;

    public static final int TYPE_LENGTH = 4;
    public static final int ID_LENGTH = 4;
    public static final int MESSAGE_LENGTH = 4;
    public static final int CHECKSUM_LENGTH = 4;

    public static final int GET_PEERS_TYPE = 1;
    public static final int PEERS_TYPE = 2;
    public static final int GET_HWEIGHT_TYPE = 3;
    public static final int HWEIGHT_TYPE = 4;
    public static final int GET_SIGNATURES_TYPE = 5;
    public static final int SIGNATURES_TYPE = 6;
    public static final int GET_BLOCK_TYPE = 7;
    public static final int WIN_BLOCK_TYPE = 8;
    public static final int BLOCK_TYPE = 9;
    public static final int TRANSACTION_TYPE = 10;
    public static final int GET_PING_TYPE = 11;
    public static final int VERSION_TYPE = 12;
    public static final int FIND_MYSELF_TYPE = 13;
    public static final int TELEGRAM_TYPE = 14;
    public static final int TELEGRAM_GET_TYPE = 15;
    public static final int TELEGRAM_GET_ANSWER_TYPE = 16;

    private int type;
    private Peer sender;
    private int id;

    public Message(int type) {
        this.type = type;

        this.id = -1;
    }

    public static String viewType(int type) {
        switch (type) {
            case 1:
                return "GET_PEERS_TYPE";
            case 2:
                return "PEERS_TYPE";
            case 3:
                return "GET_HWEIGHT_TYPE";
            case 4:
                return "HWEIGHT_TYPE";
            case 5:
                return "GET_SIGNATURES_TYPE";
            case 6:
                return "SIGNATURES_TYPE";
            case 7:
                return "GET_BLOCK_TYPE";
            case 8:
                return "WIN_BLOCK_TYPE";
            case 9:
                return "BLOCK_TYPE";
            case 10:
                return "TRANSACTION_TYPE";
            case 11:
                return "PING_TYPE";
            case 12:
                return "VERSION_TYPE";
            case 13:
                return "FIND_MYSELF_TYPE";
            case 14:
                return "TELEGRAM_TYPE";
            default:
                return "!!!" + type;
        }
    }

    public String toString() {
        return viewType(this.type) + (this.id < 0?"":"[" + this.id + "]");
    }

    public abstract boolean isRequest();

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean hasId() {
        return this.id > 0;
    }

    public int getType() {
        return this.type;
    }

    public String viewType() {
        return viewType(this.type);
    }

    public Peer getSender() {
        return this.sender;
    }

    public void setSender(Peer sender) {
        this.sender = sender;
    }

    public byte[] getHash() {
        return Crypto.getInstance().digest(this.toBytes());
    }

    public byte[] toBytes() {
        byte[] data = new byte[0];

        //WRITE MAGIC
        data = Bytes.concat(data, Controller.getInstance().getMessageMagic());

        //WRITE MESSAGE TYPE
        byte[] typeBytes = Ints.toByteArray(this.type);
        typeBytes = Bytes.ensureCapacity(typeBytes, TYPE_LENGTH, 0);
        data = Bytes.concat(data, typeBytes);

        //WRITE HASID
        if (this.hasId()) {
            byte[] hasIdBytes = new byte[]{1};
            data = Bytes.concat(data, hasIdBytes);

            //WRITE ID
            byte[] idBytes = Ints.toByteArray(this.id);
            idBytes = Bytes.ensureCapacity(idBytes, ID_LENGTH, 0);
            data = Bytes.concat(data, idBytes);
        } else {
            byte[] hasIdBytes = new byte[]{0};
            data = Bytes.concat(data, hasIdBytes);
        }

        //WRITE LENGTH
        byte[] lengthBytes = Ints.toByteArray(this.getDataLength());
        data = Bytes.concat(data, lengthBytes);

        return data;
    }

    protected byte[] generateChecksum(byte[] data) {
        byte[] checksum = Crypto.getInstance().digest(data);
        checksum = Arrays.copyOfRange(checksum, 0, CHECKSUM_LENGTH);
        return checksum;
    }

    public int getDataLength() {
        return 0;
    }
}
