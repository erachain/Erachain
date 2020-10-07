package org.erachain.core.exdata;

import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import org.erachain.controller.Controller;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;
import org.json.simple.JSONObject;

import java.nio.charset.StandardCharsets;

public class ExAuthor {

    public static final byte BASE_LENGTH = 12;

    /**
     *
     */
    protected final byte flags;

    /**
     * 0 - дополнение, см. LINK_APPENDIX_TYPE...
     */
    protected final byte[] memoBytes;
    protected String memo;

    /**
     * Ссылка на персону
     */
    protected final long key;

    /**
     * Доля или вклад
     */
    protected final int share;

    public ExAuthor() {
        this.flags = 0;
        this.share = 0;
        this.key = 0L;
        this.memo = null;
        memoBytes = null;
    }

    public ExAuthor(byte flags, int share, long key, String memo) {
        this.flags = flags;
        this.share = share;
        this.key = key;
        this.memo = memo;
        memoBytes = memo == null || memo.isEmpty() ? null : memo.getBytes(StandardCharsets.UTF_8);
    }

    public ExAuthor(byte flags, int share, long key, byte[] memoBytes) {
        this.memoBytes = memoBytes;
        this.flags = flags;
        this.share = share;
        this.key = key;
    }

    public ExAuthor(byte[] data, int position) {
        this.flags = data[position];
        this.share = Ints.fromBytes((byte) 0, (byte) 0, data[position + 2], data[position + 3]);

        byte[] keyBuf = new byte[Longs.BYTES];
        System.arraycopy(data, position + 4, keyBuf, 0, Long.BYTES);
        key = Longs.fromByteArray(keyBuf);

        int memoLen = data[position + 1];
        this.memoBytes = new byte[memoLen];
        System.arraycopy(data, position + BASE_LENGTH, memoBytes, 0, memoLen);
    }

    public byte getFlags() {
        return flags;
    }

    public long getKey() {
        return key;
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

    public int getShare() {
        return share;
    }

    public JSONObject makeJSONforHTML() {
        JSONObject json = toJson();
        json.put("name", Controller.getInstance().getPerson(key).getName());
        if (memo == null) {
            json.put("memo", "");
        }

        return json;
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("memo", getMemo());
        json.put("flags", flags);
        json.put("share", share);
        json.put("key", Transaction.viewDBRef(key));
        return json;
    }

    public byte[] toBytes() {
        int memoSize = memoBytes == null ? 0 : memoBytes.length;
        byte[] data = new byte[BASE_LENGTH + memoSize];
        data[0] = flags;
        data[1] = (byte) memoSize;
        data[2] = (byte) (share >> 8);
        data[3] = (byte) share;
        System.arraycopy(Longs.toByteArray(key), 0, data, 4, Long.BYTES);
        if (memoSize > 0)
            System.arraycopy(memoBytes, 0, data, BASE_LENGTH, memoSize);

        return data;
    }

    public static ExAuthor parse(byte[] data, int position) throws Exception {
        return new ExAuthor(data, position);
    }

    public int length() {
        return BASE_LENGTH + (memoBytes == null ? 0 : memoBytes.length);
    }

    public int isValid(DCSet dcSet) {
        if (share > 1000 || share < 0) {
            return Transaction.INVALID_AMOUNT;
        }

        if (memoBytes != null && memoBytes.length > 255)
            return Transaction.INVALID_DATA_LENGTH;

        if (!dcSet.getItemPersonMap().contains(key))
            return Transaction.ITEM_PERSON_NOT_EXIST;

        return Transaction.VALIDATE_OK;
    }
}
