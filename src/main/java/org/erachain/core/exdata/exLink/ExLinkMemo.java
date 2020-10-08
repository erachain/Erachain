package org.erachain.core.exdata.exLink;

import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
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
        super(data, position);
        int memoSize = data[position + BASE_LENGTH];
        this.memoBytes = new byte[memoSize];
        System.arraycopy(data, position + BASE_LENGTH + 1, memoBytes, 0, memoSize);
    }

    public ExLinkMemo(byte type, byte flags, int value, long ref, byte[] memoBytes) {
        super(type, flags, (byte) (value >> 8), (byte) value, ref);
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

    public int getValue() {
        return Ints.fromBytes((byte) 0, (byte) 0, value1, value2);
    }

    public JSONObject makeJSONforHTML() {
        JSONObject json = super.makeJSONforHTML(false);
        if (getMemo() != null) {
            json.put("memo", memo);
        }

        return json;
    }

    public JSONObject toJson() {
        JSONObject json = super.toJson(false);
        if (getMemo() != null) {
            json.put("memo", memo);
        }
        return json;
    }

    public byte[] toBytes() {
        int memoSize = memoBytes == null ? 0 : memoBytes.length;
        byte[] data = new byte[BASE_LENGTH + memoSize];
        data[0] = flags;
        data[1] = (byte) memoSize;
        data[2] = value1;
        data[3] = value2;
        System.arraycopy(Longs.toByteArray(ref), 0, data, 4, Long.BYTES);
        if (memoSize > 0)
            System.arraycopy(memoBytes, 0, data, BASE_LENGTH, memoSize);

        return data;
    }

    public static ExLinkMemo parse(byte[] data, int position) throws Exception {
        switch (data[0]) {
            case ExData.LINK_SOURCE_TYPE:
                return new ExLinkSource(data, position);
            case ExData.LINK_AUTHOR_TYPE:
                return new ExLinkAuthor(data, position);
        }

        throw new Exception("wrong type: " + data[0]);
    }

    public int length() {
        return BASE_LENGTH + (memoBytes == null ? 0 : memoBytes.length);
    }

    public int isValid(DCSet dcSet) {
        if (memoBytes != null && memoBytes.length > 255)
            return Transaction.INVALID_DATA_LENGTH;

        if (!dcSet.getItemPersonMap().contains(ref))
            return Transaction.ITEM_PERSON_NOT_EXIST;

        return Transaction.VALIDATE_OK;
    }

}
