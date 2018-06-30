package core.transCalculated;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashSet;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple3;

// import org.apache.log4j.Logger;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;

import core.account.Account;
import core.account.PublicKeyAccount;
import core.crypto.Crypto;
import core.item.assets.AssetCls;
import core.transaction.Transaction;
import datachain.DCSet;

//import java.math.RoundingMode;
//import java.math.MathContext;
//import java.util.Comparator;
//import javax.swing.JFrame;
//import javax.swing.JOptionPane;
//import lang.Lang;
//import settings.Settings;

public abstract class Calculated {

    // 
    // TYPES *******
    // universal
    public static final int COUNTER_CALCULATED = 0;
    // CHANGE BALANCE
    public static final int CHANGE_BALANCE_CALCULATED = 1;

    // LENGTH
    public static final int COUNTER_LENGTH = 8;
    public static final int KEY_LENGTH = 8;
    public static final int SIGNATURE_LENGTH = Crypto.SIGNATURE_LENGTH;
    protected static final int TYPE_LENGTH = 4;
    protected static final int HEIGHT_LENGTH = 4;
    protected static final int SEQ_LENGTH = 4;
    protected static final int DATA_SIZE_LENGTH = 4;
    protected static final int CREATOR_LENGTH = PublicKeyAccount.PUBLIC_KEY_LENGTH;
    protected static final int BASE_LENGTH = TYPE_LENGTH + CREATOR_LENGTH + SIGNATURE_LENGTH;
    // in pack toByte and Parse - reference not included

    static Logger LOGGER = Logger.getLogger(Calculated.class.getName());
    protected String TYPE_NAME = "unknown";
    protected byte[] typeBytes;
    protected int blockNo;
    protected int transNo;
    protected long seqNo;

    protected Account sender;
    protected Account recipient;
    protected BigDecimal amount;
    protected long assetKey = Transaction.FEE_KEY;
    protected AssetCls asset;

    protected Calculated(byte[] typeBytes, String type_name, Account sender,) {
        this.typeBytes = typeBytes;
        this.TYPE_NAME = type_name;
    }

    public static int getVersion(byte[] typeBytes) {
        return Byte.toUnsignedInt(typeBytes[1]);
    }

    public static Calculated findByHeightSeqNo(DCSet db, int blockNo, int transNo, Long seq) {
        return db.getTransactionFinalCalculatedMap().getCalculated(blockNo, transNo, seq);
    }

    // reference in Map - or as signatire or as BlockHeight + seqNo
    public static Calculated findByDBRef(DCSet db, byte[] dbRef, Long seq) {

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
        return this.sender;
    }
    
    public Account getRecipient() {
        return this.recipient;
    }
    
    public HashSet<Account> getRecipientAccounts() {
        HashSet<Account> accounts = new HashSet<Account>();
        accounts.add(this.recipient);
        return accounts;
    }

    public HashSet<Account> getInvolvedAccounts() {
        HashSet<Account> accounts = new HashSet<Account>();
        accounts.add(this.sender);
        accounts.addAll(this.getRecipientAccounts());
        return accounts;
    }

    public long getAssetKey() {
        return this.assetKey;
    }

    public BigDecimal getAmount() {
        return this.amount;
    }

    public AssetCls getAsset() {
        return this.asset;
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

    public String viewProperies() {
        return Byte.toUnsignedInt(typeBytes[2]) + "." + Byte.toUnsignedInt(typeBytes[3]);
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

    public String viewAmount(Account account) {
        return account == null ? "" : viewAmount(account.getAddress());
    }

    public String viewAmount(String address) {
        return "";
    }

    public int viewSize(boolean asPack) {
        return getDataLength(asPack);
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
        transaction.put("sender", this.sender.getAddress());
        transaction.put("recipient", this.recipient.getAddress());
        transaction.put("assetKey", this.assetKey);
        transaction.put("amount", this.amount);

        transaction.put("size", this.viewSize(false));
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

    public abstract int getDataLength(boolean asPack);

    // PROCESS/ORPHAN


    public Calculated copy() {
        try {
            return CalculatedFactory.getInstance().parse(this.toBytes());
        } catch (Exception e) {
            return null;
        }
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
