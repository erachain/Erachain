package org.erachain.core.exdata.exLink;

import com.google.common.primitives.Longs;
import org.erachain.core.exdata.ExData;
import org.erachain.core.transaction.Transaction;
import org.json.simple.JSONObject;

public class ExLink {

    public static final byte BASE_LENGTH = 12;

    /**
     * 0 - transaction, 1.. - ITEM
     */
    protected final byte flags;
    /**
     * 0 - дополнение, см. LINK_APPENDIX_TYPE...
     */
    protected final byte type;

    /**
     * Ссылка на основной документ или сущность
     */
    protected final long ref;

    /**
     * Уровень связи. Например для Отзыва-Оценки - оценка, для Поручителтсво - доля поручителтсва
     */
    protected final byte value1;
    protected final byte value2;

    public ExLink(byte type, byte flags, byte value, long ref) {
        this.type = type;
        this.flags = flags;
        this.value1 = value;
        value2 = 0;
        this.ref = ref;
    }

    public ExLink(byte type, byte flags, long ref) {
        this.type = type;
        this.flags = flags;
        this.value1 = value2 = 0;
        this.ref = ref;
    }

    public ExLink(byte type, long ref) {
        this.type = type;
        this.flags = 0;
        value1 = value2 = 0;
        this.ref = ref;
    }

    public ExLink(byte[] data) {
        this.type = data[0];
        this.flags = data[1];
        this.value1 = data[2];
        this.value2 = data[3];
        byte[] refBuf = new byte[Longs.BYTES];
        System.arraycopy(data, 4, refBuf, 0, Long.BYTES);
        ref = Longs.fromByteArray(refBuf);
    }

    public ExLink(byte[] type, long ref) {
        this.type = type[0];
        this.flags = type[1];
        this.value1 = type[2];
        this.value2 = type[3];
        this.ref = ref;
    }

    public byte getFlags() {
        return flags;
    }

    public long getRef() {
        return ref;
    }

    public byte getType() {
        return type;
    }

    public byte getValue1() {
        return value1;
    }

    public byte getValue2() {
        return value2;
    }

    public String viewTypeName() {
      return viewTypeName(type);
    }

    public static String viewTypeName(int ss) {
        switch (ss) {
            case ExData.LINK_SIMPLE_TYPE:
                return "Common";
            case ExData.LINK_APPENDIX_TYPE:
                return "Appendix";
            case ExData.LINK_REPLY_TYPE:
                return "Reply";
            case ExData.LINK_COMMENT_TYPE:
                return "Comment";
            case ExData.LINK_SURELY_TYPE:
                return "Surely";
            default:
                return "Unknown";
        }
    }

    public JSONObject makeJSONforHTML() {
        JSONObject json = new JSONObject();
        json.put("type", type);
        json.put("typeName", viewTypeName());
        json.put("flags", flags);
        json.put("value1", value1);
        json.put("value2", value2);
        json.put("ref", Transaction.viewDBRef(ref));

        return json;
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("type", type);
        json.put("typeName", viewTypeName());
        json.put("flags", flags);
        json.put("value1", value1);
        json.put("value2", value2);
        json.put("ref", Transaction.viewDBRef(ref));
        return json;
    }

    public byte[] toBytes() {
        byte[] data = new byte[BASE_LENGTH];
        data[0] = type;
        data[1] = flags;
        data[2] = value1;
        data[3] = value2;
        System.arraycopy(Longs.toByteArray(ref), 0, data, 4, Long.BYTES);

        return data;
    }

    public static ExLink parse(byte[] data) throws Exception {
        switch (data[0]) {
            case ExData.LINK_REPLY_TYPE:
                return new ExLinkReply(data);
            case ExData.LINK_APPENDIX_TYPE:
                return new ExLinkAppendix(data);
            case ExData.LINK_COMMENT_TYPE:
                return new ExLinkComment(data);
        }

        throw new Exception("wrong type");
    }

    public static ExLink parse(byte[] type, byte[] refLinkBytes) throws Exception {


        long refLink = Longs.fromByteArray(refLinkBytes);
        switch (type[0]) {
            case ExData.LINK_REPLY_TYPE:
                return new ExLinkReply(type, refLink);
            case ExData.LINK_APPENDIX_TYPE:
                return new ExLinkAppendix(type, refLink);
            case ExData.LINK_COMMENT_TYPE:
                return new ExLinkComment(type, refLink);
        }

        throw new Exception("wrong type");
    }

    public static ExLink parse(byte[] data, int position) throws Exception {

        byte[] typeBuffer = new byte[4];
        System.arraycopy(data, position, typeBuffer, 0, 4);
        byte[] refBuffer = new byte[Long.BYTES];
        System.arraycopy(data, position + 4, refBuffer, 0, Long.BYTES);
        long refLink = Longs.fromByteArray(refBuffer);
        switch (typeBuffer[0]) {
            case ExData.LINK_REPLY_TYPE:
                return new ExLinkReply(typeBuffer, refLink);
            case ExData.LINK_APPENDIX_TYPE:
                return new ExLinkAppendix(typeBuffer, refLink);
            case ExData.LINK_COMMENT_TYPE:
                return new ExLinkComment(typeBuffer, refLink);
        }

        throw new Exception("wrong type:" + typeBuffer[0]);
    }

    public int length() {
        return BASE_LENGTH;
    }

    public void process(Transaction transaction) {
        transaction.getDCSet().getExLinksMap().put(transaction.getDBRef(), this);
    }

    public void orphan(Transaction transaction) {
        transaction.getDCSet().getExLinksMap().remove(transaction.getDBRef());
    }

}
