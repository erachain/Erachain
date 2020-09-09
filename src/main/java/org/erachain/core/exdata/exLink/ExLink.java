package org.erachain.core.exdata.exLink;

import com.google.common.primitives.Longs;
import org.json.simple.JSONObject;

public class ExLink {

    public static final byte BASE_LENGTH = 12;

    public static final byte REPLY_TYPE = 1;
    public static final byte APPENDIX_TYPE = 2;
    public static final byte LIKE_TYPE = 3;

    /**
     * 0 - transaction, 1.. - ITEM
     */
    private final byte flags;
    /**
     * 0 - дополнение, см. LINK_APPENDIX_TYPE...
     */
    private final byte type;

    /**
     * Ссылка на основной документ или сущность
     */
    private final long ref;

    /**
     * Уровень связи. Например для Отзыва-Оценки - оценка, для Поручителтсво - доля поручителтсва
     */
    private final byte value1;
    private final byte value2;

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

    public JSONObject makeJSONforHTML() {
        JSONObject json = new JSONObject();
        return json;
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        return json;
    }

    public byte[] toByte() {
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
            case REPLY_TYPE:
                return new ExLinkReply(data);
            case APPENDIX_TYPE:
                return new ExLinkAppendix(data);
            case LIKE_TYPE:
                return new ExLinkLike(data);
        }

        throw new Exception("wrong type");
    }

    public static ExLink parse(byte[] type, byte[] refLinkBytes) throws Exception {


        long refLink = Longs.fromByteArray(refLinkBytes);
        switch (type[0]) {
            case REPLY_TYPE:
                return new ExLinkReply(type, refLink);
            case APPENDIX_TYPE:
                return new ExLinkAppendix(type, refLink);
            case LIKE_TYPE:
                return new ExLinkLike(type, refLink);
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
            case REPLY_TYPE:
                return new ExLinkReply(typeBuffer, refLink);
            case APPENDIX_TYPE:
                return new ExLinkAppendix(typeBuffer, refLink);
            case LIKE_TYPE:
                return new ExLinkLike(typeBuffer, refLink);
        }

        throw new Exception("wrong type");
    }

    public int length() {
        return BASE_LENGTH;
    }
}
