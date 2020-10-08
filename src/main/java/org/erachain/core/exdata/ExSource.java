package org.erachain.core.exdata;

import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import org.erachain.controller.Controller;
import org.erachain.core.exdata.exLink.ExLink;
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
    protected final int weight;

    public ExSource() {
        this.flags = 0;
        this.weight = 0;
        this.ref = 0L;
        this.memo = null;
        memoBytes = null;
    }

    public ExSource(byte flags, int weight, long ref, String memo) {
        this.flags = flags;
        this.weight = weight;
        this.ref = ref;
        this.memo = memo;
        memoBytes = memo == null || memo.isEmpty() ? null : memo.getBytes(StandardCharsets.UTF_8);
    }

    public ExSource(byte flags, int weight, long ref, byte[] memoBytes) {
        this.memoBytes = memoBytes;
        this.flags = flags;
        this.weight = weight;
        this.ref = ref;
    }

    public ExSource(byte[] data, int position) {
        this.flags = data[position];
        this.weight = Ints.fromBytes((byte) 0, (byte) 0, data[position + 2], data[position + 3]);

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

    public String viewRef() {
        return Transaction.viewDBRef(ref);
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

    public int getWeight() {
        return weight;
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
        json.put("weight", weight);
        json.put("ref", Transaction.viewDBRef(ref));
        return json;
    }

    public byte[] toBytes() {
        int memoSize = memoBytes == null ? 0 : memoBytes.length;
        byte[] data = new byte[BASE_LENGTH + memoSize];
        data[0] = flags;
        data[1] = (byte) memoSize;
        data[2] = (byte) (weight >> 8);
        data[3] = (byte) weight;
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
        if (weight > 1000 || weight < 0) {
            return Transaction.INVALID_AMOUNT;
        }

        if (memoBytes != null && memoBytes.length > 255)
            return Transaction.INVALID_DATA_LENGTH;

        if (!dcSet.getItemPersonMap().contains(ref))
            return Transaction.ITEM_PERSON_NOT_EXIST;

        return Transaction.VALIDATE_OK;
    }

    public void process(Transaction transaction) {
        // создадим связь в базе - как источник / пользователи + потребители + получатели +
        transaction.getDCSet().getExLinksMap().put(transaction.getDBRef(), new ExLink(ExData.LINK_SOURCE_TYPE, ref));
    }

    public void orphan(Transaction transaction) {
        transaction.getDCSet().getExLinksMap().remove(transaction.getDBRef());
    }

}
