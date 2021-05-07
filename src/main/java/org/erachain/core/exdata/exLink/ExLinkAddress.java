package org.erachain.core.exdata.exLink;

import com.google.common.primitives.Ints;
import org.erachain.core.account.Account;
import org.erachain.core.crypto.Base58;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;
import org.erachain.lang.Lang;
import org.json.simple.JSONObject;

import java.nio.charset.StandardCharsets;

public class ExLinkAddress {

    public static final byte SIMPLE_TYPE = 0;

    public static final byte BASE_LENGTH = 30 + 1;

    /**
     * 0 - дополнение, см. LINK_APPENDIX_TYPE...
     */
    protected final byte type;

    /**
     * 0 - transaction, 1.. - ITEM
     */
    protected final byte flags;

    /**
     * Уровень связи. Например для Отзыва-Оценки - оценка, для Поручительство - доля поручительства
     */
    protected final int value1;
    protected final int value2;

    /**
     * Ссылка на основной документ или сущность
     */
    protected final Account account;

    protected final byte[] memoBytes;
    protected String memo;

    public ExLinkAddress(byte type, byte flags, int value1, int value2, Account account, byte[] memoBytes) {
        this.type = type;
        this.flags = flags;
        this.value1 = value1;
        this.value2 = value2;
        this.account = account;
        this.memoBytes = memoBytes;
    }

    public ExLinkAddress(byte type, byte flags, byte value, Account account, byte[] memoBytes) {
        this.type = type;
        this.flags = flags;
        this.value1 = value;
        value2 = 0;
        this.account = account;
        this.memoBytes = memoBytes;
    }

    public ExLinkAddress(byte type, byte flags, Account account, byte[] memoBytes) {
        this.type = type;
        this.flags = flags;
        this.value1 = value2 = 0;
        this.account = account;
        this.memoBytes = memoBytes;
    }

    public ExLinkAddress(Account account, int value, String memo) {
        this.type = 0;
        this.flags = 0;
        this.value1 = value;
        value2 = 0;
        this.account = account;
        this.memo = memo;
        this.memoBytes = memo.getBytes(StandardCharsets.UTF_8);
    }

    public ExLinkAddress(byte type, Account account, byte[] memoBytes) {
        this.type = type;
        flags = 0;
        value1 = value2 = 0;
        this.account = account;
        this.memoBytes = memoBytes;
    }

    public ExLinkAddress(byte[] data) {
        this.type = data[0];
        this.flags = data[1];
        this.value1 = Ints.fromBytes(data[2], data[3], data[4], data[5]);
        this.value2 = Ints.fromBytes(data[6], data[7], data[8], data[9]);
        byte[] accBuf = new byte[Account.ADDRESS_SHORT_LENGTH];
        System.arraycopy(data, 10, accBuf, 0, Account.ADDRESS_SHORT_LENGTH);
        account = new Account(accBuf);

        int memoSize = data[BASE_LENGTH - 1];
        if (memoSize > 0) {
            this.memoBytes = new byte[memoSize];
            System.arraycopy(data, BASE_LENGTH, memoBytes, 0, memoSize);
        } else {
            memoBytes = null;
        }
    }

    public ExLinkAddress(byte[] data, int position) {
        this.type = data[position];
        this.flags = data[position + 1];
        this.value1 = Ints.fromBytes(data[position + 2], data[position + 3], data[position + 4], data[position + 5]);
        this.value2 = Ints.fromBytes(data[position + 6], data[position + 7], data[position + 8], data[position + 9]);
        byte[] accBuf = new byte[Account.ADDRESS_SHORT_LENGTH];
        System.arraycopy(data, position + 10, accBuf, 0, Account.ADDRESS_SHORT_LENGTH);
        account = new Account(accBuf);

        int memoSize = data[position + BASE_LENGTH - 1];
        if (memoSize > 0) {
            this.memoBytes = new byte[memoSize];
            System.arraycopy(data, position + BASE_LENGTH, memoBytes, 0, memoSize);
        } else {
            memoBytes = null;
        }
    }

    public ExLinkAddress(byte[] data, Account account, byte[] memoBytes) {
        this.type = data[0];
        this.flags = data[1];
        this.value1 = Ints.fromBytes(data[2], data[3], data[4], data[5]);
        this.value2 = Ints.fromBytes(data[6], data[7], data[8], data[9]);
        this.account = account;
        this.memoBytes = memoBytes;
    }

    public byte getFlags() {
        return flags;
    }

    public Account getAccount() {
        return account;
    }

    public byte getType() {
        return type;
    }

    public int getValue1() {
        return value1;
    }

    public int getValue2() {
        return value2;
    }

    public String getMemo() {
        if (memo == null) {
            if (memoBytes == null || memoBytes.length == 0)
                return null;
            else
                memo = new String(this.memoBytes, StandardCharsets.UTF_8);
        }
        return memo;
    }

    public String viewTypeName() {
        return viewTypeName(type);
    }

    public static String viewTypeName(int type) {
        switch (type) {
            default:
                return "Account";
        }
    }

    public JSONObject makeJSONforHTML(JSONObject langObj) {
        JSONObject json = new JSONObject();
        json.put("type", type);
        json.put("typeName", Lang.T(viewTypeName(type), langObj));
        json.put("flags", flags);
        json.put("value1", value1);
        json.put("value2", value2);
        json.put("account", account.getAddress());

        if (getMemo() != null) {
            json.put("memoBytes", Base58.encode(memoBytes));
            json.put("memo", memo);
        }

        return json;
    }

    public byte[] toBytes() {
        int memoSize = memoBytes == null ? 0 : memoBytes.length;
        byte[] data = new byte[BASE_LENGTH + memoSize];
        data[0] = type;
        data[1] = flags;
        System.arraycopy(Ints.toByteArray(value1), 0, data, 2, Integer.BYTES);
        System.arraycopy(Ints.toByteArray(value2), 0, data, 6, Integer.BYTES);
        System.arraycopy(account.getShortAddressBytes(), 0, data, 10, Account.ADDRESS_SHORT_LENGTH);

        data[BASE_LENGTH - 1] = (byte) memoSize;
        if (memoSize > 0)
            System.arraycopy(memoBytes, 0, data, BASE_LENGTH, memoSize);

        return data;
    }

    // TODO доледать из JSON
    public static ExLinkAddress parse(JSONObject json) throws Exception {
        int type = (int) (long) (Long) json.get("type");

        String memoStr = (String) json.get("memo");
        byte[] memo;
        if (memoStr == null) {
            memoStr = (String) json.get("memoBytes");
            if (memoStr == null) {
                memo = null;
            } else {
                memo = Base58.decode(memoStr);
            }
        } else {
            memo = memoStr.getBytes(StandardCharsets.UTF_8);
        }

        switch (type) {
            case SIMPLE_TYPE:
                return new ExLinkAddress((byte) type, (byte) (long) json.get("flags"),
                        (int) (long) json.get("value1"), (int) (long) json.get("value2"),
                        new Account(json.get("account").toString()),
                        memo
                );
        }
        throw new Exception("wrong type: " + type);
    }

    public static ExLinkAddress parse(byte[] data) throws Exception {
        switch (data[0]) {
            case SIMPLE_TYPE:
                return new ExLinkAddress(data);
        }

        throw new Exception("wrong type: " + data[0]);
    }

    public int length() {
        return BASE_LENGTH + (memoBytes == null ? 0 : memoBytes.length);
    }

    public int isValid(DCSet dcSet) {
        if (memoBytes != null && memoBytes.length > 255)
            return Transaction.INVALID_DATA_LENGTH;

        return Transaction.VALIDATE_OK;
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("type", type);
        //json.put("typeName", viewTypeName(type, hasRecipients));
        json.put("flags", flags);
        json.put("value1", value1);
        json.put("value2", value2);
        json.put("address", account.getAddress());
        if (memoBytes != null && memoBytes.length > 0) {
            json.put("memoBytes", Base58.encode(memoBytes));
            json.put("memo", getMemo());
        }

        return json;
    }

}
