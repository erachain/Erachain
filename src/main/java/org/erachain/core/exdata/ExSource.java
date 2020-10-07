package org.erachain.core.exdata;

import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import org.erachain.controller.Controller;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;
import org.json.simple.JSONObject;

import java.nio.charset.StandardCharsets;

public class ExSource {

    public static final byte BASE_LENGTH = 12;

    /**
     *
     */
    protected final byte flags;

    protected final byte[] memoBytes;
    protected String memo;

    /**
     * Ссылка на источник
     */
    protected final long ref;

    /**
     * Доля или вклад
     */
    protected final int share;

    public ExSource() {
        this.flags = 0;
        this.share = 0;
        this.ref = 0L;
        this.memo = null;
        memoBytes = null;
    }

    public ExSource(byte flags, int share, long ref, String memo) {
        this.flags = flags;
        this.share = share;
        this.ref = ref;
        this.memo = memo;
        memoBytes = memo == null || memo.isEmpty() ? null : memo.getBytes(StandardCharsets.UTF_8);
    }

    public ExSource(byte flags, int share, long ref, byte[] memoBytes) {
        this.memoBytes = memoBytes;
        this.flags = flags;
        this.share = share;
        this.ref = ref;
    }

    public ExSource(byte[] data, int position) {
        this.flags = data[position];
        this.share = Ints.fromBytes((byte) 0, (byte) 0, data[position + 2], data[position + 3]);

        byte[] keyBuf = new byte[Longs.BYTES];
        System.arraycopy(data, position + 4, keyBuf, 0, Long.BYTES);
        ref = Longs.fromByteArray(keyBuf);

        int memoLen = data[position + 1];
        this.memoBytes = new byte[memoLen];
        System.arraycopy(data, position + BASE_LENGTH, memoBytes, 0, memoLen);
    }

    public byte getFlags() {
        return flags;
    }

    public long getRef() {
        return ref;
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
        json.put("title", Controller.getInstance().getTransaction(ref).getTitle());
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
        json.put("ref", Transaction.viewDBRef(ref));
        return json;
    }

    public byte[] toBytes() {
        int memoSize = memoBytes == null ? 0 : memoBytes.length;
        byte[] data = new byte[BASE_LENGTH + memoSize];
        data[0] = flags;
        data[1] = (byte) memoSize;
        data[2] = (byte) (share >> 8);
        data[3] = (byte) share;
        System.arraycopy(Longs.toByteArray(ref), 0, data, 4, Long.BYTES);
        if (memoSize > 0)
            System.arraycopy(memoBytes, 0, data, BASE_LENGTH, memoSize);

        return data;
    }

    public static ExSource parse(byte[] data, int position) throws Exception {
        return new ExSource(data, position);
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

        if (!dcSet.getItemPersonMap().contains(ref))
            return Transaction.ITEM_PERSON_NOT_EXIST;

        return Transaction.VALIDATE_OK;
    }
}
