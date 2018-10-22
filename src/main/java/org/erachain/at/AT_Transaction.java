/*
 * Copyright (c) 2014 CIYAM Developers

 Distributed under the MIT/X11 software license, please refer to the file license.txt
 in the root project directory or http://www.opensource.org/licenses/mit-license.php.

 */

package org.erachain.at;

import org.erachain.core.crypto.Base58;
import org.json.simple.JSONObject;
import org.erachain.utils.Converter;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;


public class AT_Transaction {

    private final int BASE_SIZE = 2 * AT_Constants.AT_ID_SIZE + 8 + 4 + 4 + 4;
    private byte[] senderId = new byte[AT_Constants.AT_ID_SIZE];
    private byte[] recipientId = new byte[AT_Constants.AT_ID_SIZE];
    private long key;
    private long amount;
    private byte[] message;
    private int blockHeight;
    private int seq;

    AT_Transaction(byte[] senderId, byte[] recipientId, long key, long amount, byte[] message) {
        this.senderId = senderId.clone();
        this.recipientId = recipientId.clone();
        this.key = key;
        this.amount = amount;
        this.message = (message != null) ? message.clone() : null;
    }

    public AT_Transaction(int blockHeight, int seq, byte[] senderId,
                          byte[] recipientId, long key, long amount, byte[] message) {
        this.blockHeight = blockHeight;
        this.seq = seq;
        this.senderId = senderId;
        this.recipientId = recipientId;
        this.key = key;
        this.amount = amount;
        this.message = message;
    }

    public static AT_Transaction fromBytes(byte[] data) {
        ByteBuffer bf = ByteBuffer.wrap(data);
        bf.order(ByteOrder.LITTLE_ENDIAN);
        bf.clear();

        int blockHeight = bf.getInt();
        int seq = bf.getInt();
        byte[] senderId = new byte[AT_Constants.AT_ID_SIZE];
        byte[] recipientId = new byte[AT_Constants.AT_ID_SIZE];
        bf.get(senderId, 0, senderId.length);
        bf.get(recipientId, 0, recipientId.length);
        long key = bf.getLong();
        long amount = bf.getLong();
        int messageLength = bf.getInt();
        byte[] message = null;
        if (messageLength > 0) {
            message = new byte[messageLength];
            bf.get(message, 0, messageLength);
        }

        return new AT_Transaction(blockHeight, seq, senderId, recipientId, key, amount, message);

    }

    public Long getKey() {
        return this.key;
    }

    public Long getAmount() {
        return this.amount;
    }

    public byte[] getSenderId() {
        return this.senderId;
    }

    public byte[] getRecipientId() {
        return this.recipientId;
    }

    public byte[] getMessage() {
        return this.message;
    }

    public int getBlockHeight() {
        return this.blockHeight;
    }

    public void setBlockHeight(Integer blockHeight) {
        this.blockHeight = blockHeight;
    }

    public int getSeq() {
        return this.seq;
    }

    public void setSeq(Integer seq) {
        this.seq = seq;
    }

    public int getSize() {
        return (this.message != null) ? BASE_SIZE + this.message.length : BASE_SIZE;
    }

    public byte[] toBytes() {
        int size = BASE_SIZE;
        if (message != null) {
            size += message.length;
        }

        ByteBuffer bf = ByteBuffer.allocate(size);
        bf.order(ByteOrder.LITTLE_ENDIAN);
        bf.clear();

        bf.putInt(blockHeight);
        bf.putInt(seq);
        bf.put(senderId);
        bf.put((recipientId != null) ? recipientId : new byte[AT_Constants.AT_ID_SIZE]);
        bf.putLong(key);
        bf.putLong(amount);
        bf.putInt((message != null) ? message.length : 0);
        if (message != null) {
            bf.put(message);
        }


        return bf.array().clone();

    }

    public String getRecipient() {
        return Base58.encode(recipientId);
    }

    public String getSender() {
        return Base58.encode(senderId);
    }

    @SuppressWarnings("unchecked")
    public JSONObject toJSON() {
        JSONObject ob = new JSONObject();
        ob.put("blockHeight", blockHeight);
        ob.put("seq", seq);
        ob.put("sender", getSender());
        ob.put("recipient", getRecipient());
        ob.put("key", key);
        ob.put("amount", BigDecimal.valueOf(amount).toPlainString());
        ob.put("message", (message != null) ? Converter.toHex(message) : "");
        return ob;
    }

    @Override
    public boolean equals(Object otherObject) {
        if (otherObject instanceof AT_Transaction) {
            AT_Transaction otherAtTx = (AT_Transaction) otherObject;

            return (this.getBlockHeight() == otherAtTx.getBlockHeight())
                    && (this.getSeq() == otherAtTx.getSeq())
                    && (Arrays.equals(this.getSenderId(), otherAtTx.getSenderId()))
                    && (Arrays.equals(this.getRecipientId(), otherAtTx.getRecipientId()))
                    && (Arrays.equals(this.getMessage(), otherAtTx.getMessage()))
                    && (this.getKey() == otherAtTx.getKey())
                    && (this.getAmount() == otherAtTx.getAmount());
        }

        return false;
    }
}
