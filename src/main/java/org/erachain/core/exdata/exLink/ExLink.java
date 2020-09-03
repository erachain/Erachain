package org.erachain.core.exdata.exLink;

import org.json.simple.JSONObject;

public class ExLink {

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
    private final byte value;

    public ExLink(byte flags, byte type, long ref, byte value) {
        this.flags = flags;
        this.type = type;
        this.ref = ref;
        this.value = value;
    }

    public ExLink(byte type, long ref, byte value) {
        this.flags = 0;
        this.type = type;
        this.ref = ref;
        this.value = value;
    }

    public ExLink(byte flags, byte type, long ref) {
        this.flags = flags;
        this.type = type;
        this.ref = ref;
        value = 0;
    }

    public ExLink(byte type, long ref) {
        this.flags = 0;
        this.type = type;
        this.ref = ref;
        value = 0;
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

    public byte getValue() {
        return value;
    }

    public JSONObject makeJSONforHTML() {
        JSONObject json = new JSONObject();
        return json;
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        return json;
    }

    public byte[] toByte() throws Exception {
        return null;
    }

}
