package org.erachain.core.exdata.exLink;

import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import org.erachain.controller.Controller;
import org.erachain.core.exdata.ExData;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;
import org.json.simple.JSONObject;

import java.nio.charset.StandardCharsets;

public abstract class ExLinkMemo extends ExLink {

    protected final byte[] memoBytes;
    protected String memo;

    public ExLinkMemo(byte type, long parentSeqNo, String memo) {
        super(type, parentSeqNo);
        this.memo = memo;
        memoBytes = memo == null || memo.isEmpty() ? null : memo.getBytes(StandardCharsets.UTF_8);
    }

    public ExLinkMemo(byte[] data, int position) {
        super(data);
        int memoSize = data[position + BASE_LENGTH];
        this.memoBytes = new byte[memoSize];
        System.arraycopy(data, position + BASE_LENGTH + 1, memoBytes, 0, memoSize);
    }

    public ExLinkMemo(byte type, int weight, long ref, byte[] memoBytes) {
        super(type, (byte) 0, (byte) 0, (byte) 0, ref);
        this.memoBytes = memoBytes;
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
        return Ints.fromBytes(0, 0, (byte) value1, (byte) value2);
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

    public static ExLinkMemo parse(byte[] data, int position) throws Exception {
        return new ExLinkMemo(data, position);
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
