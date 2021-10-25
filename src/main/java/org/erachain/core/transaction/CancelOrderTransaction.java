package org.erachain.core.transaction;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Longs;
import org.erachain.core.BlockChain;
import org.erachain.core.account.Account;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.block.Block;
import org.erachain.core.crypto.Base58;
import org.erachain.core.exdata.exLink.ExLink;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.item.assets.Order;
import org.erachain.core.item.assets.Trade;
import org.erachain.datachain.DCSet;
import org.erachain.smartcontracts.SmartContract;
import org.json.simple.JSONObject;
import org.mapdb.Fun;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;

public class CancelOrderTransaction extends Transaction {

    static Logger LOGGER = LoggerFactory.getLogger(CancelOrderTransaction.class.getName());

    // TODO убрать в новой цепочке
    public static final byte[][] VALID_REC = new byte[][]{
    };
    // TODO - reference to ORDER - by recNor INT+INT - not 64xBYTE[] !!!
    public static final byte TYPE_ID = (byte) CANCEL_ORDER_TRANSACTION;
    public static final String TYPE_NAME = "Cancel Order";

    private static final int ORDER_SIGN_LENGTH = SIGNATURE_LENGTH;

    private static final int LOAD_LENGTH = SIGNATURE_LENGTH;
    private static final int BASE_LENGTH_AS_MYPACK = Transaction.BASE_LENGTH_AS_MYPACK + LOAD_LENGTH;
    private static final int BASE_LENGTH_AS_PACK = Transaction.BASE_LENGTH_AS_PACK + LOAD_LENGTH;
    private static final int BASE_LENGTH = Transaction.BASE_LENGTH + LOAD_LENGTH;
    private static final int BASE_LENGTH_AS_DBRECORD = Transaction.BASE_LENGTH_AS_DBRECORD + LOAD_LENGTH
            + SEQ_NO_LENGTH;

    private byte[] orderSignature;
    private long orderID;


    public CancelOrderTransaction(byte[] typeBytes, PublicKeyAccount creator, byte[] orderSignature, byte feePow, long timestamp, long flags) {
        super(typeBytes, TYPE_NAME, creator, null, null, feePow, timestamp, flags);
        this.orderSignature = orderSignature;
    }

    public CancelOrderTransaction(byte[] typeBytes, PublicKeyAccount creator, byte[] orderSignature, byte feePow, long timestamp, long flags, byte[] signature) {
        this(typeBytes, creator, orderSignature, feePow, timestamp, flags);
        this.signature = signature;
    }

    public CancelOrderTransaction(byte[] typeBytes, PublicKeyAccount creator, byte[] orderSignature, long orderID, byte feePow, long timestamp, long flags, byte[] signature, long seqNo, long feeLong) {
        this(typeBytes, creator, orderSignature, feePow, timestamp, flags);
        this.signature = signature;
        this.orderID = orderID;
        this.fee = BigDecimal.valueOf(feeLong, BlockChain.FEE_SCALE);
        if (seqNo > 0)
            this.setHeightSeq(seqNo);
    }

    public CancelOrderTransaction(PublicKeyAccount creator, byte[] orderSignature, byte feePow, long timestamp, long flags, byte[] signature) {
        this(new byte[]{TYPE_ID, 0, 0, 0}, creator, orderSignature, feePow, timestamp, flags, signature);
    }

    public CancelOrderTransaction(PublicKeyAccount creator, byte[] orderSignature, byte feePow, long timestamp, long flags) {
        this(new byte[]{TYPE_ID, 0, 0, 0}, creator, orderSignature, feePow, timestamp, flags);
    }

    //GETTERS/SETTERS

    public void setDC(DCSet dcSet, int forDeal, int blockHeight, int seqNo, boolean andUpdateFromState) {
        super.setDC(dcSet, forDeal, blockHeight, seqNo, false);

        if (orderID == 0L) {
            Long createDBRef = this.dcSet.getTransactionFinalMapSigns().get(this.orderSignature);
            if (createDBRef == null && blockHeight > BlockChain.CANCEL_ORDERS_ALL_VALID && height > BlockChain.ALL_VALID_BEFORE) {
                LOGGER.error("ORDER transaction not found: " + Base58.encode(this.orderSignature));
                errorValue = Base58.encode(this.orderSignature);
                if (BlockChain.CHECK_BUGS > 3) {
                    Long error = null;
                    error++;
                }
            }
            this.orderID = createDBRef;
        }

        if (false && andUpdateFromState && !isWiped())
            updateFromStateDB();

    }

    public byte[] getorderSignature() {
        return this.orderSignature;
    }

    public Long getOrderID() {
        return this.orderID;
    }

    @Override
    public boolean hasPublicText() {
        return false;
    }

    //PARSE CONVERT

    public static Transaction Parse(byte[] data, int forDeal) throws Exception {

        int test_len;
        if (forDeal == FOR_MYPACK) {
            test_len = BASE_LENGTH_AS_MYPACK;
        } else if (forDeal == Transaction.FOR_PACK) {
            test_len = BASE_LENGTH_AS_PACK;
        } else if (forDeal == Transaction.FOR_DB_RECORD) {
            test_len = BASE_LENGTH_AS_DBRECORD;
        } else {
            test_len = BASE_LENGTH;
        }

        if (data.length < test_len) {
            throw new Exception("Data does not match RAW length " + data.length + " < " + test_len);
        }

        // READ TYPE
        byte[] typeBytes = Arrays.copyOfRange(data, 0, TYPE_LENGTH);
        int position = TYPE_LENGTH;

        long timestamp = 0;
        if (forDeal > FOR_MYPACK) {
            //READ TIMESTAMP
            byte[] timestampBytes = Arrays.copyOfRange(data, position, position + TIMESTAMP_LENGTH);
            timestamp = Longs.fromByteArray(timestampBytes);
            position += TIMESTAMP_LENGTH;
        }

        //READ FLAGS
        byte[] flagsBytes = Arrays.copyOfRange(data, position, position + FLAGS_LENGTH);
        long flags = Longs.fromByteArray(flagsBytes);
        position += FLAGS_LENGTH;

        //READ CREATOR
        byte[] creatorBytes = Arrays.copyOfRange(data, position, position + CREATOR_LENGTH);
        PublicKeyAccount creator = new PublicKeyAccount(creatorBytes);
        position += CREATOR_LENGTH;

        ExLink exLink;
        if ((typeBytes[2] & HAS_EXLINK_MASK) > 0) {
            exLink = ExLink.parse(data, position);
            position += exLink.length();
        } else {
            exLink = null;
        }

        SmartContract smartContract;
        if ((typeBytes[2] & HAS_SMART_CONTRACT_MASK) > 0) {
            smartContract = SmartContract.Parses(data, position, forDeal);
            position += smartContract.length(forDeal);
        } else {
            smartContract = null;
        }

        byte feePow = 0;
        if (forDeal > Transaction.FOR_PACK) {
            //READ FEE POWER
            byte[] feePowBytes = Arrays.copyOfRange(data, position, position + 1);
            feePow = feePowBytes[0];
            position += 1;
        }

        //READ SIGNATURE
        byte[] signatureBytes = Arrays.copyOfRange(data, position, position + SIGNATURE_LENGTH);
        position += SIGNATURE_LENGTH;

        long feeLong = 0;
        long seqNo = 0;
        if (forDeal == FOR_DB_RECORD) {
            //READ SEQ_NO
            byte[] seqNoBytes = Arrays.copyOfRange(data, position, position + TIMESTAMP_LENGTH);
            seqNo = Longs.fromByteArray(seqNoBytes);
            position += TIMESTAMP_LENGTH;

            // READ FEE
            byte[] feeBytes = Arrays.copyOfRange(data, position, position + FEE_LENGTH);
            feeLong = Longs.fromByteArray(feeBytes);
            position += FEE_LENGTH;
        }

        //READ ORDER SIGNATURE
        byte[] orderSignature = Arrays.copyOfRange(data, position, position + ORDER_SIGN_LENGTH);
        position += ORDER_SIGN_LENGTH;

        long orderID = 0;
        if (forDeal == FOR_DB_RECORD) {
            //READ ORDER ID
            byte[] orderIDBytes = Arrays.copyOfRange(data, position, position + SEQ_NO_LENGTH);
            orderID = Longs.fromByteArray(orderIDBytes);
            position += SEQ_NO_LENGTH;
        }

        return new CancelOrderTransaction(typeBytes, creator, orderSignature, orderID, feePow, timestamp, flags, signatureBytes, seqNo, feeLong);
    }

    //@Override
    @Override
    public byte[] toBytes(int forDeal, boolean withSignature) {
        byte[] data = super.toBytes(forDeal, withSignature);

        //WRITE ORDER
        data = Bytes.concat(data, this.orderSignature);

        if (forDeal == FOR_DB_RECORD) {
            // WRITE ORDER ID
            byte[] orderIDBytes = Longs.toByteArray(this.orderID);
            data = Bytes.concat(data, orderIDBytes);
        }

        return data;
    }


    @SuppressWarnings("unchecked")
    @Override
    public JSONObject toJson() {
        //GET BASE
        JSONObject transaction = this.getJsonBase();

        transaction.put("orderID", this.orderID);
        transaction.put("orderSignature", Base58.encode(this.orderSignature));

        return transaction;
    }

    //VALIDATE
    //@Override
    @Override
    public int isValid(int forDeal, long checkFlags) {

        if (height < BlockChain.ALL_VALID_BEFORE) {
            return VALIDATE_OK;
        }

        for (byte[] valid_item : VALID_REC) {
            if (Arrays.equals(this.signature, valid_item)) {
                return VALIDATE_OK;
            }
        }

        //CHECK IF ORDER EXISTS
        boolean emptyOrder = false;
        if (this.orderID == 0L || !this.dcSet.getOrderMap().contains(this.orderID)) {
            if (this.height > BlockChain.CANCEL_ORDERS_ALL_VALID && height > BlockChain.ALL_VALID_BEFORE) {

                if (true) {
                    if (this.orderID == 0L) {
                        errorValue = "orderID == null";
                        LOGGER.debug("INVALID: " + errorValue);
                    } else {
                        errorValue = "orderID: " + Transaction.viewDBRef(orderID);
                        // 3qUAUPdifyWYg7ABYa5TiWmyssHH1gJtKDatATS6UeKMnSzEwpuPJN5QFKCPHtUWpDYbK7fceFyDGhc51CuhiJ3
                        LOGGER.debug("INVALID: this.sign = " + Base58.encode(signature));
                        LOGGER.debug("INVALID: " + errorValue);
                        LOGGER.debug("INVALID: this.orderSign == " + Base58.encode(orderSignature));
                        if (this.dcSet.getCompletedOrderMap().contains(this.orderID)) {
                            errorValue += " already Completed";
                            LOGGER.debug("INVALID: already Completed");
                        } else {
                            errorValue += " not exist in chain";
                            LOGGER.debug("INVALID: not exist in chain");
                        }
                    }
                }

                return ORDER_DOES_NOT_EXIST;
            } else {
                emptyOrder = true;
            }
        }

        if (!emptyOrder) {
            Order order = this.dcSet.getOrderMap().get(this.orderID);

            //CHECK IF CREATOR IS CREATOR
            if (!order.getCreator().equals(this.creator)) {
                return INVALID_ORDER_CREATOR;
            }
        }

        return super.isValid(forDeal, checkFlags);
    }


    //PROCESS/ORPHAN

    @Override
    public int getDataLength(int forDeal, boolean withSignature) {
        int base_len;
        if (forDeal == FOR_MYPACK)
            base_len = BASE_LENGTH_AS_MYPACK;
        else if (forDeal == FOR_PACK)
            base_len = BASE_LENGTH_AS_PACK;
        else if (forDeal == FOR_DB_RECORD)
            base_len = BASE_LENGTH_AS_DBRECORD;
        else
            base_len = BASE_LENGTH;

        if (exLink != null)
            base_len += exLink.length();

        if (smartContract != null) {
            if (forDeal == FOR_DB_RECORD || !smartContract.isEpoch()) {
                base_len += smartContract.length(forDeal);
            }
        }

        if (!withSignature)
            base_len -= SIGNATURE_LENGTH;

        return base_len;

    }

    public static void processBody(DCSet dcSet, long dbRef, long orderID, Block block,
                                   String calcMess, boolean ignoreNull) {

        Order order = dcSet.getOrderMap().get(orderID);

        if (order == null) {
            if (ignoreNull)
                return;
            Long error = null;
            error++;
        }

        //DELETE FROM DATABASE FIRST - иначе сработает проверка внутри
        dcSet.getOrderMap().delete(orderID);

        //SET ORPHAN DATA
        dcSet.getCompletedOrderMap().put(orderID, order);

        AssetCls assetHave = dcSet.getItemAssetMap().get(order.getHaveAssetKey());
        AssetCls assetWant = dcSet.getItemAssetMap().get(order.getWantAssetKey());

        // ADD CANCEL as TRADE
        Trade trade = new Trade(Trade.TYPE_CANCEL, dbRef, orderID, order.getHaveAssetKey(), order.getWantAssetKey(),
                order.getAmountWantLeft(), order.getAmountHaveLeft(),
                assetWant.getScale(), assetHave.getScale(), -1);
        //ADD TRADE TO DATABASE
        dcSet.getTradeMap().put(trade);

        //UPDATE BALANCE OF CREATOR
        BigDecimal left = order.getAmountHaveLeft();
        order.getCreator().changeBalance(dcSet, false, false, order.getHaveAssetKey(), left,
                false, false,
                // accounting on PLEDGE position
                true, Account.BALANCE_POS_PLEDGE);

        if (block != null) {
            block.addCalculated(order.getCreator(), order.getHaveAssetKey(), left, calcMess, dbRef);
        }
    }

    @Override
    public void processBody(Block block, int forDeal) {
        //UPDATE CREATOR
        super.processBody(block, forDeal);

        boolean ignoreNull = height < BlockChain.CANCEL_ORDERS_ALL_VALID || height < BlockChain.ALL_VALID_BEFORE;
        if (this.orderID == 0L) {
            if (ignoreNull)
                return;
            Long error = null;
            error++;
        }

        processBody(dcSet, dbRef, orderID, block, "Cancel Order @" + Transaction.viewDBRef(orderID), ignoreNull);
    }

    public static void orphanBody(DCSet dcSet, long dbRef, long orderID, boolean ignoreNull) {

        //REMOVE ORDER DATABASE
        Order order = dcSet.getCompletedOrderMap().get(orderID);

        if (order == null) {
            if (ignoreNull)
                return;
            Long error = null;
            error++;
        }

        if (Transaction.viewDBRef(orderID).equals("776446-1")) {
            boolean debug = true;
        }

        // ROLLBACK CANCEL as TRADE
        dcSet.getTradeMap().delete(new Fun.Tuple2<Long, Long>(dbRef, order.getId()));

        //DELETE ORPHAN DATA FIRST - иначе ошибка будет при добавлении в таблицу ордеров
        dcSet.getCompletedOrderMap().delete(order.getId());

        dcSet.getOrderMap().put(order.getId(), order);

        //REMOVE BALANCE OF CREATOR
        order.getCreator().changeBalance(dcSet, true, false, order.getHaveAssetKey(),
                order.getAmountHaveLeft(), false, false,
                // accounting on PLEDGE position
                true, Account.BALANCE_POS_PLEDGE);
    }

    @Override
    public void orphanBody(Block block, int forDeal) {

        // ORPHAN
        super.orphanBody(block, forDeal);

        boolean ignoreNull = height < BlockChain.CANCEL_ORDERS_ALL_VALID || height < BlockChain.ALL_VALID_BEFORE;
        if (this.orderID == 0L) {
            if (ignoreNull)
                return;
            Long error = null;
            error++;
        }

        orphanBody(dcSet, dbRef, orderID, ignoreNull);

    }

    @Override
    public HashSet<Account> getInvolvedAccounts() {
        HashSet<Account> accounts = new HashSet<>(2, 1);
        accounts.add(this.creator);
        return accounts;
    }

    @Override
    public HashSet<Account> getRecipientAccounts() {
        return new HashSet<>(1, 1);
    }

    @Override
    public boolean isInvolved(Account account) {
        if (account.equals(this.creator)) {
            return true;
        }

        return false;
    }


    public Map<String, Map<Long, BigDecimal>> getAssetAmount() {
        Map<String, Map<Long, BigDecimal>> assetAmount = new LinkedHashMap<>();

        assetAmount = subAssetAmount(assetAmount, this.creator.getAddress(), FEE_KEY, this.fee);

        Order order;

        if (this.dcSet.getCompletedOrderMap().contains(this.orderID)) {
            order = this.dcSet.getCompletedOrderMap().get(this.orderID);
        } else {
            order = this.dcSet.getOrderMap().get(this.orderID);
        }

        assetAmount = addAssetAmount(assetAmount, this.creator.getAddress(), order.getHaveAssetKey(), order.getAmountHave());

        return assetAmount;
    }

}
