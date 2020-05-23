package org.erachain.core.transaction;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Longs;
import org.erachain.core.BlockChain;
import org.erachain.core.account.Account;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.block.Block;
import org.erachain.core.crypto.Base58;
import org.erachain.core.crypto.Crypto;
import org.erachain.core.item.assets.Order;
import org.erachain.datachain.DCSet;
import org.json.simple.JSONObject;
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
        //Base58.decode("2SEfiztfaj9wNE2k8h3Wiko3oVHtdjawosfua5PbjeAwPTFMHhFoJqVxpYvswZUdJFfQZ7i6xXep85UvCkZoxHqi"),
        //Base58.decode("34BaZfvWJpyEKAL7i3txFcTqRcVJt2GgumJm2ANqNcvBHCxngfoXBUKhm24uhqmZx1qvShj1KwUK6WHwHX2FQpfy"),
    };
    // TODO - reference to ORDER - by recNor INT+INT - not 64xBYTE[] !!!
    private static final byte TYPE_ID = (byte) CANCEL_ORDER_TRANSACTION;
    private static final String NAME_ID = "Cancel Order";
    private static final int ORDER_LENGTH = Crypto.SIGNATURE_LENGTH;

    private static final int BASE_LENGTH_AS_MYPACK = Transaction.BASE_LENGTH_AS_MYPACK + ORDER_LENGTH;
    private static final int BASE_LENGTH_AS_PACK = Transaction.BASE_LENGTH_AS_PACK + ORDER_LENGTH;
    private static final int BASE_LENGTH = Transaction.BASE_LENGTH + ORDER_LENGTH;
    private static final int BASE_LENGTH_AS_DBRECORD = Transaction.BASE_LENGTH_AS_DBRECORD + ORDER_LENGTH;

    private byte[] orderSignature;
    private Long orderID;


    public CancelOrderTransaction(byte[] typeBytes, PublicKeyAccount creator, byte[] orderSignature, byte feePow, long timestamp, Long reference) {
        super(typeBytes, NAME_ID, creator, feePow, timestamp, reference);
        this.orderSignature = orderSignature;
    }

    public CancelOrderTransaction(byte[] typeBytes, PublicKeyAccount creator, byte[] order, byte feePow, long timestamp, Long reference, byte[] signature) {
        this(typeBytes, creator, order, feePow, timestamp, reference);
        this.signature = signature;
    }
    public CancelOrderTransaction(byte[] typeBytes, PublicKeyAccount creator, byte[] order, byte feePow, long timestamp, Long reference, byte[] signature, long feeLong) {
        this(typeBytes, creator, order, feePow, timestamp, reference);
        this.signature = signature;
        this.fee = BigDecimal.valueOf(feeLong, BlockChain.FEE_SCALE);
    }

    public CancelOrderTransaction(PublicKeyAccount creator, byte[] orderSignature, byte feePow, long timestamp, Long reference, byte[] signature) {
        this(new byte[]{TYPE_ID, 0, 0, 0}, creator, orderSignature, feePow, timestamp, reference, signature);
    }

    public CancelOrderTransaction(PublicKeyAccount creator, byte[] orderSignature, byte feePow, long timestamp, Long reference) {
        this(new byte[]{TYPE_ID, 0, 0, 0}, creator, orderSignature, feePow, timestamp, reference);
    }

    //GETTERS/SETTERS

    public void setDC(DCSet dcSet, int asDeal, int blockHeight, int seqNo) {
        super.setDC(dcSet, asDeal, blockHeight, seqNo);

        Long createDBRef = this.dcSet.getTransactionFinalMapSigns().get(this.orderSignature);
        if (createDBRef == null && blockHeight > BlockChain.CANCEL_ORDERS_ALL_VALID) {
            LOGGER.error("ORDER transaction not found: " + Base58.encode(this.orderSignature));
            if (BlockChain.CHECK_BUGS > 8) {
                Long error = null;
                error++;
            }
        }
        this.orderID = createDBRef;

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

    public static Transaction Parse(byte[] data, int asDeal) throws Exception {

        int test_len;
        if (asDeal == Transaction.FOR_MYPACK) {
            test_len = BASE_LENGTH_AS_MYPACK;
        } else if (asDeal == Transaction.FOR_PACK) {
            test_len = BASE_LENGTH_AS_PACK;
        } else if (asDeal == Transaction.FOR_DB_RECORD) {
            test_len = BASE_LENGTH_AS_DBRECORD;
        } else {
            test_len = BASE_LENGTH;
        }

        if (data.length < test_len) {
            throw new Exception("Data does not match block length " + data.length);
        }

        // READ TYPE
        byte[] typeBytes = Arrays.copyOfRange(data, 0, TYPE_LENGTH);
        int position = TYPE_LENGTH;

        long timestamp = 0;
        if (asDeal > Transaction.FOR_MYPACK) {
            //READ TIMESTAMP
            byte[] timestampBytes = Arrays.copyOfRange(data, position, position + TIMESTAMP_LENGTH);
            timestamp = Longs.fromByteArray(timestampBytes);
            position += TIMESTAMP_LENGTH;
        }

        //READ REFERENCE
        byte[] referenceBytes = Arrays.copyOfRange(data, position, position + REFERENCE_LENGTH);
        Long reference = Longs.fromByteArray(referenceBytes);
        position += REFERENCE_LENGTH;

        //READ CREATOR
        byte[] creatorBytes = Arrays.copyOfRange(data, position, position + CREATOR_LENGTH);
        PublicKeyAccount creator = new PublicKeyAccount(creatorBytes);
        position += CREATOR_LENGTH;

        byte feePow = 0;
        if (asDeal > Transaction.FOR_PACK) {
            //READ FEE POWER
            byte[] feePowBytes = Arrays.copyOfRange(data, position, position + 1);
            feePow = feePowBytes[0];
            position += 1;
        }

        //READ SIGNATURE
        byte[] signatureBytes = Arrays.copyOfRange(data, position, position + SIGNATURE_LENGTH);
        position += SIGNATURE_LENGTH;

        long feeLong = 0;
        if (asDeal == FOR_DB_RECORD) {
            // READ FEE
            byte[] feeBytes = Arrays.copyOfRange(data, position, position + FEE_LENGTH);
            feeLong = Longs.fromByteArray(feeBytes);
            position += FEE_LENGTH;
        }

        //READ ORDER
        byte[] orderSignature = Arrays.copyOfRange(data, position, position + ORDER_LENGTH);
        position += ORDER_LENGTH;

        return new CancelOrderTransaction(typeBytes, creator, orderSignature, feePow, timestamp, reference, signatureBytes, feeLong);
    }

    //@Override
    @Override
    public byte[] toBytes(int forDeal, boolean withSignature) {
        byte[] data = super.toBytes(forDeal, withSignature);

        //WRITE ORDER
        //byte[] orderBytes = this.orderSignature;
        //byte[] fill = new byte[ORDER_LENGTH - orderBytes.length];
        //orderBytes = Bytes.concat(fill, orderBytes);
        data = Bytes.concat(data, this.orderSignature);

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
    public int isValid(int asDeal, long flags) {

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
        if (this.orderID == null || !this.dcSet.getOrderMap().contains(this.orderID)) {
            if (this.height > BlockChain.CANCEL_ORDERS_ALL_VALID) {

                if (true) {
                    if (this.orderID == null) {
                        LOGGER.debug("INVALID: this.orderID == null");
                    } else {
                        // 3qUAUPdifyWYg7ABYa5TiWmyssHH1gJtKDatATS6UeKMnSzEwpuPJN5QFKCPHtUWpDYbK7fceFyDGhc51CuhiJ3
                        LOGGER.debug("INVALID: this.sign = " + Base58.encode(signature));
                        LOGGER.debug("INVALID: this.orderID = " + Transaction.viewDBRef(orderID));
                        LOGGER.debug("INVALID: this.orderSign == " + Base58.encode(orderSignature));
                        if (this.dcSet.getCompletedOrderMap().contains(this.orderID)) {
                            LOGGER.debug("INVALID: already Completed");
                        } else {
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
            if (!order.getCreator().equals(this.creator.getAddress())) {
                return INVALID_ORDER_CREATOR;
            }
        }

        return super.isValid(asDeal, flags);
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

        if (!withSignature)
            base_len -= SIGNATURE_LENGTH;

        return base_len;

    }

    //@Override
    @Override
    public void process(Block block, int asDeal) {
        //UPDATE CREATOR
        super.process(block, asDeal);

        if (this.orderID == null) {
            if (height < BlockChain.CANCEL_ORDERS_ALL_VALID)
                return;
            Long error = null;
            error++;
        }

        // TODO - CANCEL для транзакции в том же блоке???
        //Transaction createOrder = this.dcSet.getTransactionFinalMap().get(this.orderSignature);
        //Tuple2<Integer, Integer> dbRefTuple2 = createOrder.getHeightSeqNo();
        //this.orderID = Transaction.makeDBRef(dbRefTuple2);

        Order order = this.dcSet.getOrderMap().get(this.orderID);

        if (order == null) {
            if (height < BlockChain.CANCEL_ORDERS_ALL_VALID)
                return;
            Long error = null;
            error++;
        }

        //DELETE FROM DATABASE FIRST - иначе сработает проверка внутри
        dcSet.getOrderMap().delete(order.getId());

        //SET ORPHAN DATA
        dcSet.getCompletedOrderMap().put(order.getId(), order);

        //UPDATE BALANCE OF CREATOR
        BigDecimal left = order.getAmountHaveLeft();
        order.getCreator().changeBalance(dcSet, false, false, order.getHaveAssetKey(), left, false, false);
        this.addCalculated(block, this.creator, order.getHaveAssetKey(), left,
                "Cancel Order @" + Transaction.viewDBRef(order.getId()));
    }

    //@Override
    @Override
    public void orphan(Block block, int asDeal) {

        // FIRST GET DB REF from FINAL

        // ORPHAN
        super.orphan(block, asDeal);

        if (this.orderID == null) {
            if (height < BlockChain.CANCEL_ORDERS_ALL_VALID)
                return;
            Long error = null;
            error++;
        }

        //REMOVE ORDER DATABASE
        Order order = this.dcSet.getCompletedOrderMap().get(this.orderID);

        if (order == null) {
            if (height < BlockChain.CANCEL_ORDERS_ALL_VALID)
                return;
            Long error = null;
            error++;
        }

        if (Transaction.viewDBRef(this.orderID).equals("776446-1")) {
            boolean debug = true;
        }

        //DELETE ORPHAN DATA FIRST - иначе ошибка будет при добавлении в таблицу ордеров
        dcSet.getCompletedOrderMap().delete(order.getId());

        dcSet.getOrderMap().put(order.getId(), order);

        //REMOVE BALANCE OF CREATOR
        order.getCreator().changeBalance(dcSet, true, false, order.getHaveAssetKey(), order.getAmountHaveLeft(), false, false);
    }

    @Override
    public HashSet<Account> getInvolvedAccounts() {
        HashSet<Account> accounts = new HashSet<>(2,1);
        accounts.add(this.creator);
        return accounts;
    }

    @Override
    public HashSet<Account> getRecipientAccounts() {
        return new HashSet<>(1,1);
    }

    @Override
    public boolean isInvolved(Account account) {
        String address = account.getAddress();

        if (address.equals(this.creator.getAddress())) {
            return true;
        }

        return false;
    }

	/*
	@Override
	public BigDecimal Amount(Account account)
	{
		String address = account.getAddress();

		if(address.equals(this.creator.getAddress()))
		{
			return BigDecimal.ZERO;
		}

		return BigDecimal.ZERO;
	}
	 */

    //@Override
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
