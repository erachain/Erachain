package org.erachain.core.transCalculated;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import org.erachain.core.account.Account;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.datachain.DCSet;
import org.json.simple.JSONObject;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashSet;

// import org.slf4j.LoggerFactory;

/*
 * Вычисленные транзакции (вычисления) - нужны для показа причин изменения остатов и состояний
 * Заносятся в таблицу datachain/TransactionFinalCalculatedMap.java
 * по ключу из номера блока, номера трнзакции внем и порядковому номеру вычисления
 * число вычисленний для блока хранится в ключе Номер_Блока + 0 + 0
 * число вычислений для транзакции хранится в ключе Номер_Блока_Номер_Транзакции_в_Блоке + 0
 * номера вычислений начинаются с 1
 * Число вычислений это тоже вычисление - счетчик: core/transCalculated/CalculatedCounter.java
 *
 * поидее в базу не нужно их номера катать - они и так есть в ключах
 * 
 * сериализатор для базы данных - database/serializer/CalculatedSerializer.java
 * 
 */
public abstract class Calculated {

    // 
    // TYPES *******
    // universal
    public static final int COUNTER_CALCULATED = 0;
    // CHANGE BALANCE
    public static final int CHANGE_BALANCE_CALCULATED = 1;

    // LENGTH
    protected static final int TYPE_LENGTH = 4;
    protected static final int BLOCK_NO_LENGTH = 4;
    protected static final int TRANS_NO_LENGTH = 4;
    protected static final int SEQ_NO_LENGTH = 8;
    protected static final int BASE_LENGTH = TYPE_LENGTH + BLOCK_NO_LENGTH + TRANS_NO_LENGTH + SEQ_NO_LENGTH;

    static Logger LOGGER = LoggerFactory.getLogger(Calculated.class.getName());
    protected String TYPE_NAME = "unknown";

    protected DCSet dcSet;
    protected byte[] typeBytes;
    protected int blockNo;
    protected int transNo;
    protected long seqNo;

    protected Calculated(byte[] typeBytes, String type_name, Integer blockNo, Integer transNo, long seqNo) {
        this.typeBytes = typeBytes;
        this.TYPE_NAME = type_name;
        this.blockNo = blockNo;
        this.transNo = transNo;
        this.seqNo = seqNo;
    }

    // GETTERS/SETTERS

    public void setDC(DCSet dcSet) {
        this.dcSet = dcSet;
    }

    public static int getVersion(byte[] typeBytes) {
        return Byte.toUnsignedInt(typeBytes[1]);
    }

    public static Calculated findByHeightSeqNo(DCSet db, int blockNo, int transNo, long seq) {
        return db.getTransactionFinalCalculatedMap().getCalculated(blockNo, transNo, seq);
    }

    // reference in Map - or as signatire or as BlockHeight + seqNo
    public static Calculated findByDBRef(DCSet db, byte[] dbRef, long seq) {

        if (dbRef == null)
            return null;

        int blockNo = Ints.fromByteArray(Arrays.copyOfRange(dbRef, 0, 4));
        int transNo = Ints.fromByteArray(Arrays.copyOfRange(dbRef, 4, 8));
        Tuple3<Integer, Integer, Long> key = new Tuple3<Integer, Integer, Long>(blockNo, transNo, seq);

        return db.getTransactionFinalCalculatedMap().get(key);

    }

    public int getType() {
        return Byte.toUnsignedInt(this.typeBytes[0]);
    }

    public int getVersion() {
        return Byte.toUnsignedInt(this.typeBytes[1]);
    }

    public byte[] getTypeBytes() {
        return this.typeBytes;
    }

    public Account getSender() {
        return null;
    }
    
    public Account getRecipient() {
        return null;
    }
    
    public HashSet<Account> getRecipientAccounts() {
        HashSet<Account> accounts = new HashSet<Account>();
        return accounts;
    }

    public HashSet<Account> getInvolvedAccounts() {
        HashSet<Account> accounts = new HashSet<Account>();
        return accounts;
    }

    public long getAssetKey() {
        return 0L;
    }

    public long getAbsKey() {
        return 0L;
    }
    
    public BigDecimal getAmount() {
        return null;
    }
    
    public BigDecimal getAmount(String address) {
        return BigDecimal.ZERO;
    }

    public BigDecimal getAmount(Account account) {
        return BigDecimal.ZERO;
    }

    public AssetCls getAsset() {
        return null;
    }

    public Tuple3<Integer, Integer, Long> getBlockNoTransNoSeqNo() {
        return new Tuple3<Integer, Integer, Long>(this.blockNo, this.transNo, this.seqNo);
    }

    public Tuple2<Integer, Integer> getBlockNoTransNo() {
        return new Tuple2<Integer, Integer>(this.blockNo, this.transNo);
    }

    public int getBlockNo() {
        return this.blockNo;
    }

    public int getTransNo() {
        return this.transNo;
    }

    ////
    // VIEW
    public String viewType() {
        return Byte.toUnsignedInt(typeBytes[0]) + "." + Byte.toUnsignedInt(typeBytes[1]);
    }

    public String viewTypeName() {
        return TYPE_NAME;
    }

    public String viewSubTypeName() {
        return "";
    }

    public String viewFullTypeName() {
        String sub = viewSubTypeName();
        return sub.length() > 0 ? viewTypeName() + ":" + sub : viewTypeName();
    }

    public String viewlockTrans() {
        return this.blockNo + "-" + this.transNo;
    }

    public String viewSender() {
        return "-";
    }

    public String viewRecipient() {
        return "-";
    }

    public String viewAmount(Account account) {
        return account == null ? "" : viewAmount(account.getAddress());
    }

    public String viewAmount(String address) {
        return "";
    }

    public int viewSize() {
        return getDataLength();
    }

    // PARSE/CONVERT

    public String viewItemName() {
        return "";
    }

    public String viewAmount() {
        return "";
    }

    @SuppressWarnings("unchecked")
    protected JSONObject getJsonBase() {

        JSONObject transaction = new JSONObject();

        transaction.put("type", Byte.toUnsignedInt(this.typeBytes[0]));
        transaction.put("record_type", this.viewTypeName());
        transaction.put("type_name", this.viewTypeName());
        transaction.put("sub_type_name", this.viewSubTypeName());

        transaction.put("version", Byte.toUnsignedInt(this.typeBytes[1]));
        transaction.put("property1", Byte.toUnsignedInt(this.typeBytes[2]));
        transaction.put("property2", Byte.toUnsignedInt(this.typeBytes[3]));
        transaction.put("blockNo", this.blockNo);
        transaction.put("transactionNo", this.transNo);
        transaction.put("sequenceNo", this.seqNo);

        transaction.put("size", this.viewSize());
        return transaction;
    }

    public abstract JSONObject toJson();

    @SuppressWarnings("unchecked")
    public JSONObject rawToJson() {

        JSONObject transaction = new JSONObject();

        return transaction;
    }

    public byte[] toBytes() {

        byte[] data = new byte[0];

        // WRITE TYPE
        data = Bytes.concat(data, this.typeBytes);

        return data;

    }

    public abstract int getDataLength();

    // PROCESS/ORPHAN
    public abstract void process();
    public abstract void orphan();


    public Calculated copy() {
        try {
            return CalculatedFactory.getInstance().parse(this.toBytes());
        } catch (Exception e) {
            return null;
        }
    }

    public boolean isInvolved(Account account) {
        return false;
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof Calculated) {
            Calculated calculated = (Calculated) object;

            return calculated.blockNo == this.blockNo
                    && calculated.transNo == this.transNo
                    && calculated.seqNo == this.seqNo;
        }

        return false;
    }

}
