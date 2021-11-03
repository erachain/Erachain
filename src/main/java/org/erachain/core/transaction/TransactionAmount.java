package org.erachain.core.transaction;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.core.account.Account;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.block.Block;
import org.erachain.core.crypto.Crypto;
import org.erachain.core.exdata.exLink.ExLink;
import org.erachain.core.item.ItemCls;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.item.persons.PersonCls;
import org.erachain.dapp.DAPP;
import org.erachain.datachain.DCSet;
import org.erachain.utils.BigDecimalUtil;
import org.erachain.utils.DateTimeFormat;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.mapdb.Fun;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**

## typeBytes
0 - record type
1 - record version
2 - property 1
3 = property 2

## version 0
 // typeBytes[2] = -128 if NO AMOUNT (NO_AMOUNT_MASK)
 // typeBytes[3] = -128 if NO DATA

## version 1
if backward - CONFISCATE CREDIT

## version 2

 #### PROPERTY 1
 typeBytes[2].7 = -128 if NO AMOUNT - check sign (NO_AMOUNT_MASK)
 typeBytes[2].6 = 64 if backward (CONFISCATE CREDIT, ...)

#### PROPERTY 2
typeBytes[3].7 = -128 if NO DATA - check sign

## version 3

 #### PROPERTY 1
 typeBytes[2].7 = -128 if NO AMOUNT - check sign (NO_AMOUNT_MASK)
 typeBytes[2].6 = 64 if backward (CONFISCATE CREDIT, ...)

#### PROPERTY 2
typeBytes[3].7 = 128 if NO DATA - check sign = '10000000' = Integer.toBinaryString(128)
typeBytes[3].4-0 = point accuracy: -16..16 = BYTE - 16

 Transaction.FLAGS = 1 (USE_PACKET_MASK) - use PACET instead single Amount+AssetKey
 */

public abstract class TransactionAmount extends Transaction implements Itemable{
    public static final byte[][] VALID_REC = new byte[][]{
    };

    static Logger LOGGER = LoggerFactory.getLogger(TransactionAmount.class.getName());

    public static final int SCALE_MASK = 31;
    public static final int SCALE_MASK_HALF = (SCALE_MASK + 1) >> 1;
    public static final int maxSCALE = TransactionAmount.SCALE_MASK_HALF + BlockChain.AMOUNT_DEDAULT_SCALE - 1;
    public static final int minSCALE = BlockChain.AMOUNT_DEDAULT_SCALE - TransactionAmount.SCALE_MASK_HALF;

    /**
     * used over typeBytes[2]
     */
    public static final byte NO_AMOUNT_MASK = -128; // == (byte) 128
    public static final byte BACKWARD_MASK = 64;

    /**
     * used in tx FLATS - set
     */
    public static final long USE_PACKET_MASK = 1; //

    // BALANCES types and ACTION with IT
    // 0 - not used
    public static final int ACTION_SEND = Account.BALANCE_POS_OWN;
    public static final int ACTION_DEBT = Account.BALANCE_POS_DEBT;
    public static final int ACTION_REPAY_DEBT = -Account.BALANCE_POS_DEBT; // чисто для другого отображения а так = ACTION_DEBT
    public static final int ACTION_HOLD = Account.BALANCE_POS_HOLD;
    public static final int ACTION_SPEND = Account.BALANCE_POS_SPEND;
    public static final int ACTION_PLEDGE = Account.BALANCE_POS_PLEDGE;
    public static final int ACTION_RESERVED_6 = Account.BALANCE_POS_6;

    public static final int[] ACTIONS_LIST = new int[]{
            ACTION_SEND,
            ACTION_DEBT,
            ACTION_REPAY_DEBT, // чисто для другого отображения а так = ACTION_DEBT
            ACTION_HOLD,
            ACTION_SPEND,
            ACTION_PLEDGE
            // ACTION_RESERVED_6
    };

    public static final String NAME_ACTION_SEND = "SEND";
    public static final String NAME_ACTION_SEND_WAS = "Send # was";
    public static final String NAME_ACTION_DEBT = "CREDIT";
    public static final String NAME_ACTION_DEBT_WAS = "Credit # was";
    public static final String NAME_ACTION_HOLD = "HOLD";
    public static final String NAME_ACTION_HOLD_WAS = "Hold # was";
    public static final String NAME_ACTION_SPEND = "SPEND";
    public static final String NAME_ACTION_SPEND_WAS = "Spend # was";
    public static final String NAME_ACTION_PLEDGE = "PLEDGE";
    public static final String NAME_ACTION_PLEDGE_WAS = "Pledge # was";
    public static final int AMOUNT_LENGTH = 8;
    public static final int RECIPIENT_LENGTH = Account.ADDRESS_LENGTH;

    protected static final int LOAD_LENGTH = RECIPIENT_LENGTH + KEY_LENGTH + AMOUNT_LENGTH;
    protected static final int BASE_LENGTH_AS_MYPACK = Transaction.BASE_LENGTH_AS_MYPACK + LOAD_LENGTH;
    protected static final int BASE_LENGTH_AS_PACK = Transaction.BASE_LENGTH_AS_PACK + LOAD_LENGTH;
    protected static final int BASE_LENGTH = Transaction.BASE_LENGTH + LOAD_LENGTH;
    protected static final int BASE_LENGTH_AS_DBRECORD = Transaction.BASE_LENGTH_AS_DBRECORD + LOAD_LENGTH;

    protected Account recipient;
    protected Fun.Tuple4<Long, Integer, Integer, Integer> recipientPersonDuration;
    protected PersonCls recipientPerson;

    protected BigDecimal amount;
    protected long key;
    protected AssetCls asset; // or price Asset for packet

    /**
     * 0: (long) AssetKey, 1: Amount, 2: Price, 3: Discounted Price, 4: Tax as percent, 5: Fee as absolute value, 6: memo, 7: Asset (after setDC())
     */
    protected Object[][] packet;
    // + 1 to len for memo
    protected static final int PACKET_ROW_LENGTH = KEY_LENGTH + 5 * (1 + AMOUNT_LENGTH) + 1;
    protected static final int PACKET_ROW_MEMO_NO = 6;

    /**
     * see Account.BALANCE_POS_OWN etc. BACKWARD < 0. Used in case PACKET
     */
    protected int balancePos;

    // need for calculate fee by feePow into GUI
    protected TransactionAmount(byte[] typeBytes, String name, PublicKeyAccount creator, ExLink exLink, DAPP dapp, byte feePow, Account recipient,
                                BigDecimal amount, long key, long timestamp, long flags) {
        super(typeBytes, name, creator, exLink, dapp, feePow, timestamp, flags);
        this.recipient = recipient;

        if (amount == null || amount.signum() == 0) {
            // SET 7 bit - HAS NO AMOUNT
            typeBytes[2] = (byte) (typeBytes[2] | NO_AMOUNT_MASK);
        } else {
            // RESET 7 bit
            typeBytes[2] = (byte) (typeBytes[2] & ~NO_AMOUNT_MASK);

            this.amount = amount;
            this.key = key;
        }

        if (BlockChain.CHECK_BUGS > 1 && flags < 0L) {
            LOGGER.error("TX FLAG NEGATE!");
            Controller.getInstance().stopAndExit(6765);
        }
    }

    /**
     * packet
     *
     * @param typeBytes
     * @param name
     * @param creator
     * @param exLink
     * @param dapp
     * @param feePow
     * @param recipient
     * @param packet
     * @param timestamp
     * @param extFlags
     */
    protected TransactionAmount(byte[] typeBytes, String name, PublicKeyAccount creator, ExLink exLink, DAPP dapp, byte feePow, Account recipient,
                                int balancePos, Long priceAssetKey, Object[][] packet, long timestamp, long extFlags) {
        super(typeBytes, name, creator, exLink, dapp, feePow, timestamp, extFlags);
        this.recipient = recipient;

        // SET 7 bit - HAS NO AMOUNT
        typeBytes[2] = (byte) (typeBytes[2] | NO_AMOUNT_MASK);

        assert (packet != null);

        // SET EXTENDED FLAGS MASK + USE_PACKET_MASK
        this.extFlags = extFlags | FLAGS_USED_MASK | USE_PACKET_MASK;
        this.packet = packet;
        this.balancePos = balancePos;
        this.key = priceAssetKey;
    }

    // GETTERS/SETTERS

    @Override
    public void setDC(DCSet dcSet, boolean andUpdateFromState) {
        super.setDC(dcSet, false);
        if (BlockChain.TEST_DB == 0 && recipient != null) {
            recipientPersonDuration = recipient.getPersonDuration(dcSet);
            if (recipientPersonDuration != null) {
                recipientPerson = (PersonCls) dcSet.getItemPersonMap().get(recipientPersonDuration.a);
            }
        }

        if (this.key != 0L) {
            this.asset = this.dcSet.getItemAssetMap().get(this.getAbsKey());
        }
        if (packet != null) {
            for (Object[] row : packet) {
                row[7] = this.dcSet.getItemAssetMap().get((Long) row[0]);
            }
        }

        if (false && andUpdateFromState && !isWiped())
            updateFromStateDB();

    }

    @Override
    public void setDC(DCSet dcSet, int forDeal, int blockHeight, int seqNo, boolean andUpdateFromState) {
        super.setDC(dcSet, forDeal, blockHeight, seqNo, false);

        if (false && andUpdateFromState && !isWiped())
            updateFromStateDB();
    }

    public Account getRecipient() {
        return this.recipient;
    }
    
    @Override
    public long getKey() {
        return this.key;
    }

    @Override
    public long getAssetKey() {
        return this.key;
    }

    @Override
    public ItemCls getItem() {
        return this.asset;
    }

    public boolean hasPacket() {
        return packet != null;
    }

    public Object[][] getPacket() {
        return packet;
    }

    @Override
    public void makeItemsKeys() {

        if (isWiped()) {
            itemsKeys = new Object[0][0];

        } else {

            // запомним что тут две сущности
            if (key != 0) {
                if (creatorPersonDuration != null) {
                    if (recipientPersonDuration != null) {
                        itemsKeys = new Object[][]{
                                new Object[]{ItemCls.PERSON_TYPE, creatorPersonDuration.a, creatorPerson.getTags()},
                                new Object[]{ItemCls.PERSON_TYPE, recipientPersonDuration.a, recipientPerson.getTags()},
                                new Object[]{ItemCls.ASSET_TYPE, getAbsKey(), asset.getTags()}
                        };
                    } else {
                        itemsKeys = new Object[][]{
                                new Object[]{ItemCls.PERSON_TYPE, creatorPersonDuration.a, creatorPerson.getTags()},
                                new Object[]{ItemCls.ASSET_TYPE, getAbsKey(), asset.getTags()}
                        };
                    }
                } else {
                    if (recipientPersonDuration != null) {
                        itemsKeys = new Object[][]{
                                new Object[]{ItemCls.PERSON_TYPE, recipientPersonDuration.a, recipientPerson.getTags()},
                                new Object[]{ItemCls.ASSET_TYPE, getAbsKey(), asset.getTags()}
                        };
                    } else {
                        itemsKeys = new Object[][]{
                                new Object[]{ItemCls.ASSET_TYPE, getAbsKey(), asset.getTags()}
                        };
                    }
                }
            } else {
                if (creatorPersonDuration != null) {
                    if (recipientPersonDuration != null) {
                        itemsKeys = new Object[][]{
                                new Object[]{ItemCls.PERSON_TYPE, creatorPersonDuration.a, creatorPerson.getTags()},
                                new Object[]{ItemCls.PERSON_TYPE, recipientPersonDuration.a, recipientPerson.getTags()},
                        };
                    } else {
                        itemsKeys = new Object[][]{
                                new Object[]{ItemCls.PERSON_TYPE, creatorPersonDuration.a, creatorPerson.getTags()},
                        };
                    }
                } else {
                    if (recipientPersonDuration != null) {
                        itemsKeys = new Object[][]{
                                new Object[]{ItemCls.PERSON_TYPE, recipientPersonDuration.a, recipientPerson.getTags()},
                        };
                    } else {
                        // need initalize in all cases
                        itemsKeys = new Object[0][0];
                    }
                }
            }

            // TODO добавить список в накладной
            if (packet != null) {
                Object[][] itemsKeysFull;
                if (itemsKeys.length > 0) {
                    itemsKeysFull = new Object[itemsKeys.length + packet.length][];
                    System.arraycopy(itemsKeys, 0, itemsKeysFull, 0, itemsKeys.length);
                    for (int count = 0; count < packet.length; count++) {
                        itemsKeysFull[itemsKeys.length + count] = new Object[]{ItemCls.ASSET_TYPE, packet[count][0], ((AssetCls) packet[count][7]).getTags()};
                    }
                } else {
                    itemsKeysFull = new Object[packet.length][];
                    for (int count = 0; count < packet.length; count++) {
                        itemsKeysFull[count] = new Object[]{ItemCls.ASSET_TYPE, packet[count][0], ((AssetCls) packet[count][7]).getTags()};
                    }
                }

                itemsKeys = itemsKeysFull;
            }
        }

    }

    @Override
    public AssetCls getAsset() {
        return this.asset;
    }

    @Override
    public BigDecimal getAmount() {
        // return this.amount == null? BigDecimal.ZERO: this.amount;
        return this.amount;
    }

    @Override
    public BigDecimal getAmount(Account account) {
        if (this.amount == null)
            return BigDecimal.ZERO;

        if (this.creator.equals(account)) {
            // IF SENDER
            return amount.negate();
        } else if (this.recipient.equals(account)) {
            // IF RECIPIENT
            return amount;
        }

        return BigDecimal.ZERO;
    }

    /**
     * // только для передачи в собственность!
     * call only if (balancePosition() == ACTION_SEND && !creator.equals(asset.getMaker()) && !recipient.equals(asset.getMaker())
     * && !BlockChain.ASSET_TRANSFER_PERCENTAGE.isEmpty()
     */
    public static Tuple2<BigDecimal, BigDecimal> calcSendTAX(Long key, AssetCls asset, BigDecimal amount) {

        if (key == null || !BlockChain.ASSET_TRANSFER_PERCENTAGE.containsKey(key) || asset == null || amount == null)
            return null;

        BigDecimal assetFee;
        BigDecimal assetFeeBurn;

        Fun.Tuple2<BigDecimal, BigDecimal> percItem = BlockChain.ASSET_TRANSFER_PERCENTAGE.get(key);
        assetFee = amount.abs().multiply(percItem.a).setScale(asset.getScale(), RoundingMode.DOWN);
        if (assetFee.compareTo(percItem.b) < 0) {
            // USE MINIMAL VALUE
            assetFee = percItem.b.setScale(asset.getScale(), RoundingMode.DOWN);
        }
        if (!BlockChain.ASSET_BURN_PERCENTAGE.isEmpty()
                && BlockChain.ASSET_BURN_PERCENTAGE.containsKey(key)) {
            assetFeeBurn = assetFee.multiply(BlockChain.ASSET_BURN_PERCENTAGE.get(key)).setScale(asset.getScale(), RoundingMode.UP);
        } else
            assetFeeBurn = BigDecimal.ZERO;

        return new Tuple2<>(assetFee, assetFeeBurn);

    }

    @Override
    public long calcBaseFee(boolean withFreeProtocol) {

        if (creator == null)
            return 0L;

        long long_fee = super.calcBaseFee(withFreeProtocol);

        // TODO packet
        if (packet != null) {
            long_fee += 25 * packet.length * BlockChain.FEE_PER_BYTE;
        }

        assetFEE = null;
        assetsPacketFEE = null;

        // ПРОЦЕНТЫ в любом случае посчитаем - даже если халявная транзакция
        // только для передачи в собственность!
        if (balancePosition() == ACTION_SEND && !BlockChain.ASSET_TRANSFER_PERCENTAGE.isEmpty()) {
            if (packet != null) {
                assetsPacketFEE = new HashMap<>();
                Tuple2<BigDecimal, BigDecimal> assetFee;
                for (Object[] row : packet) {
                    assetFee = calcSendTAX((Long) row[0], (AssetCls) row[7], (BigDecimal) row[1]);
                    if (assetFee != null)
                        assetsPacketFEE.put((AssetCls) row[7], assetFee);
                }

            } else if (hasAmount() && !creator.equals(asset.getMaker()) && !recipient.equals(asset.getMaker())) {
                assetFEE = calcSendTAX(key, asset, amount);
                if (assetFEE != null) {
                    long_fee -= 256L;
                    if (long_fee < 0L)
                        long_fee = 0L;
                }
            }
        }

        return long_fee;
    }

    public boolean hasAmount() {
        return amount != null && amount.signum() != 0 || packet != null;
    }

    public int balancePosition() {
        if (!hasAmount())
            return 0;
        return packet == null ? Account.balancePosition(this.key, this.amount, this.isBackward(), asset.isSelfManaged())
                : balancePos;
    }

    // BACKWARD AMOUNT
    public boolean isBackward() {
        return packet == null ? typeBytes[1] == 1 || typeBytes[1] > 1 && (typeBytes[2] & BACKWARD_MASK) > 0
                : balancePos < 0;
    }

    /*
     * ************** VIEW
     */

    @Override
    public String viewRecipient() {
        return recipient.getPersonAsString();
    }

    @Override
    public String viewTypeName() {
        return viewTypeName(packet == null ? this.amount : BigDecimal.ONE, isBackward());
    }

    public static String viewTypeName(BigDecimal amount, boolean isBackward) {
        if (amount == null || amount.signum() == 0)
            return "LETTER";

        if (isBackward) {
            return "backward";
        } else {
            return "SEND";
        }
    }

    public static String viewSubTypeName(int actionType) {

        switch (actionType) {
            case ACTION_SEND:
                return NAME_ACTION_SEND;
            case ACTION_DEBT:
                return NAME_ACTION_DEBT;
            case ACTION_HOLD:
                return NAME_ACTION_HOLD;
            case ACTION_SPEND:
                return NAME_ACTION_SPEND;
            case ACTION_PLEDGE:
                return NAME_ACTION_PLEDGE;
        }

        return "???";

    }

    public static String viewSubTypeName(long assetKey, BigDecimal amount, boolean isBackward, boolean isDirect) {

        if (amount == null || amount.signum() == 0)
            return "";

        int actionType = Account.balancePosition(assetKey, amount, isBackward, isDirect);

        return viewSubTypeName(actionType);

    }

    @Override
    public String viewSubTypeName() {
        if (packet == null && (amount == null || amount.signum() == 0))
            return "";

        if (packet == null) {
            return viewSubTypeName(key, amount, isBackward(), asset.isDirectBalances());
        } else {
            return viewSubTypeName(balancePos);
        }
    }

    @Override
    public String viewAmount() {

        if (hasPacket())
            return "package";

        if (amount == null || amount.signum() == 0)
            return "";

        if (this.amount.signum() < 0) {
            return this.amount.negate().toPlainString();
        } else {
            return this.amount.toPlainString();
        }
    }

    @Override
    public String viewFullTypeName() {
        return viewActionType();
    }

    public String viewActionType() {
        if (hasAmount()) {
            if (hasPacket()) {
                return Account.balancePositionName(balancePosition())
                        + (isBackward() ? " backward" : "");
            }

            return asset.viewAssetTypeAction(isBackward(), balancePosition(), creator == null ? false : asset.getMaker().equals(creator));
        } else
            return "Mail";

    }

    // PARSE/CONVERT

    @Override
    public byte[] toBytes(int forDeal, boolean withSignature) {

        byte[] data = super.toBytes(forDeal, withSignature);

        // WRITE RECIPIENT
        data = Bytes.concat(data, this.recipient.getAddressBytes());

        if (this.amount != null) {

            // WRITE KEY
            byte[] keyBytes = Longs.toByteArray(this.key);
            keyBytes = Bytes.ensureCapacity(keyBytes, KEY_LENGTH, 0);
            data = Bytes.concat(data, keyBytes);

            // CALCULATE ACCURACY of AMMOUNT
            int different_scale = this.amount.scale() - BlockChain.AMOUNT_DEDAULT_SCALE;
            BigDecimal amountBase;
            if (different_scale != 0) {
                // RESCALE AMOUNT
                amountBase = this.amount.scaleByPowerOfTen(different_scale);
                if (different_scale < 0)
                    different_scale += TransactionAmount.SCALE_MASK + 1;

                // WRITE ACCURACY of AMMOUNT
                data[3] = (byte) (data[3] | different_scale);
            } else {
                amountBase = this.amount;
            }

            // WRITE AMOUNT
            byte[] amountBytes = Longs.toByteArray(amountBase.unscaledValue().longValue());
            //amountBytes = Bytes.ensureCapacity(amountBytes, AMOUNT_LENGTH, 0);
            data = Bytes.concat(data, amountBytes);

        } else if (packet != null) {
            // WRITE PRICE ASSET KEY
            byte[] keyBytes = Longs.toByteArray(this.key);
            keyBytes = Bytes.ensureCapacity(keyBytes, KEY_LENGTH, 0);
            data = Bytes.concat(data, keyBytes);

            byte[] packetLenBytes = Ints.toByteArray(packet.length);
            // 0 byte - ?
            // 1 byte - action
            packetLenBytes[1] = (byte) balancePos;
            data = Bytes.concat(data, packetLenBytes);

            byte[][] memoBytes = new byte[packet.length][];
            int count = 0;
            int additionalLen = 0;
            for (Object[] row : packet) {
                if (row[PACKET_ROW_MEMO_NO] == null) {
                    memoBytes[count++] = new byte[0];
                } else {
                    additionalLen += (memoBytes[count++] = ((String) row[PACKET_ROW_MEMO_NO]).getBytes(StandardCharsets.UTF_8)).length;
                }
            }

            byte[] buff = new byte[packet.length * PACKET_ROW_LENGTH + additionalLen];
            int pos = 0;
            count = 0;
            for (Object[] row : packet) {
                /**
                 * 0: (long) AssetKey, 1: Amount, 2: Price, 3: Discounted Price, 4: Tax as percent, 5: Fee as absolute value, 6: memo, 7: Asset (after setDC())
                 */

                // WRITE ASSET KEY
                System.arraycopy(Longs.toByteArray((Long) row[0]), 0, buff, pos, Long.BYTES);
                pos += Long.BYTES;

                // WRITE AMOUNT
                BigDecimalUtil.toBytes9(buff, pos, (BigDecimal) row[1]);
                pos += 9;

                // WRITE PRICE
                if (row[2] != null && ((BigDecimal) row[2]).signum() != 0)
                    BigDecimalUtil.toBytes9(buff, pos, (BigDecimal) row[2]);
                pos += 9;

                // WRITE DISCOUNT PRICE
                if (row[3] != null && ((BigDecimal) row[3]).signum() != 0)
                    BigDecimalUtil.toBytes9(buff, pos, (BigDecimal) row[3]);
                pos += 9;

                // WRITE TAX
                if (row[4] != null && ((BigDecimal) row[4]).signum() != 0)
                    BigDecimalUtil.toBytes9(buff, pos, (BigDecimal) row[4]);
                pos += 9;

                // WRITE FEE
                if (row[5] != null && ((BigDecimal) row[5]).signum() != 0)
                    BigDecimalUtil.toBytes9(buff, pos, (BigDecimal) row[5]);
                pos += 9;

                // WRITE MEMO LEN
                buff[pos++] = (byte) memoBytes[count].length;
                // WRITE MEMO
                System.arraycopy(memoBytes[count], 0, buff, pos, memoBytes[count].length);
                pos += memoBytes[count++].length;
            }

            data = Bytes.concat(data, buff);

        }
        
        return data;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    protected JSONObject getJsonBase() {
        JSONObject transaction = super.getJsonBase();

        recipient.toJsonPersonInfo(transaction, "recipient");
        if (amount != null && amount.signum() != 0) {
            transaction.put("asset", this.getAbsKey()); // deprecated
            transaction.put("assetKey", this.getAbsKey());
            if (asset == null) {
                setDC(DCSet.getInstance(), false);
            }
            asset.toJsonInfo(transaction, "asset");

            transaction.put("amount", this.amount.toPlainString());
            transaction.put("balancePos", this.balancePosition());
            transaction.put("actionName", viewActionType());
            if (this.isBackward())
                transaction.put("backward", this.isBackward());

        } else if (packet != null) {
            transaction.put("priceAssetKey", this.getAbsKey());
            if (asset == null) {
                setDC(DCSet.getInstance(), false);
            }
            asset.toJsonInfo(transaction, "priceAsset");

            transaction.put("balancePos", Math.abs(balancePos));
            transaction.put("actionName", viewSubTypeName(balancePos));
            if (this.isBackward())
                transaction.put("backward", this.isBackward());

            JSONArray packetArray = new JSONArray();
            for (Object[] row : packet) {
                JSONArray rowArray = new JSONArray();
                rowArray.add(row[0]); // asset Key
                rowArray.add(((BigDecimal) row[1]).stripTrailingZeros().toPlainString()); // volume
                rowArray.add(row[2] == null ? "0" : ((BigDecimal) row[2]).stripTrailingZeros().toPlainString()); // price
                rowArray.add(row[3] == null ? "0" : ((BigDecimal) row[3]).stripTrailingZeros().toPlainString()); // dicconted price
                rowArray.add(row[4] == null ? "0" : ((BigDecimal) row[4]).stripTrailingZeros().toPlainString()); // tax %
                rowArray.add(row[5] == null ? "0" : ((BigDecimal) row[5]).stripTrailingZeros().toPlainString()); // fee
                rowArray.add(row[6] == null ? "" : row[6]); // memo
                rowArray.add(((AssetCls) row[7]).toJsonInfo()); // asset
                packetArray.add(rowArray);
            }
            transaction.put("packet", packetArray);

        }

        return transaction;
    }
    
    @Override
    public HashSet<Account> getInvolvedAccounts() {
        HashSet<Account> accounts = new HashSet<Account>(3, 1);
        if (this.creator != null)
            accounts.add(this.creator);

        accounts.addAll(this.getRecipientAccounts());
        return accounts;
    }
    
    @Override
    public HashSet<Account> getRecipientAccounts() {
        HashSet<Account> accounts = new HashSet<Account>(2,1);
        accounts.add(this.recipient);
        return accounts;
    }
    
    @Override
    public boolean isInvolved(Account account) {
        if (account.equals(creator)
                || account.equals(recipient)) {
            return true;
        }

        return false;
    }
    
    @Override
    public int getDataLength(int forDeal, boolean withSignature) {
        // IF VERSION 1 (amount = null)

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

        if (dapp != null) {
            if (forDeal == FOR_DB_RECORD || !dapp.isEpoch()) {
                base_len += dapp.length(forDeal);
            }
        }

        if (!withSignature)
            base_len -= SIGNATURE_LENGTH;

        if (packet == null) {
            if (this.typeBytes[2] < 0)
                base_len -= KEY_LENGTH + AMOUNT_LENGTH;

            return base_len;

        } else {
            int additionalLen = 0;
            for (Object[] row : packet) {
                if (row[PACKET_ROW_MEMO_NO] == null)
                    continue;

                additionalLen += ((String) row[PACKET_ROW_MEMO_NO]).getBytes(StandardCharsets.UTF_8).length;
            }

            return base_len - AMOUNT_LENGTH + Integer.BYTES + packet.length * PACKET_ROW_LENGTH + additionalLen;

        }
    }

    private static long pointLogg;

    public static boolean isValidPersonProtect(DCSet dcSet, int height, Account recipient,
                                               boolean creatorIsPerson, long absKey, int actionType,
                                               AssetCls asset) {
        if (BlockChain.PERSON_SEND_PROTECT && creatorIsPerson && absKey != FEE_KEY
                && actionType != ACTION_DEBT && actionType != ACTION_HOLD && actionType != ACTION_SPEND
                && asset.isSendPersonProtected()
        ) {
            if (!recipient.isPerson(dcSet, height)
                    && !BlockChain.ANONYMASERS.contains(recipient.getAddress())) {

                boolean recipient_admin = false;
                for (String admin : BlockChain.GENESIS_ADMINS) {
                    if (recipient.equals(admin)) {
                        recipient_admin = true;
                        break;
                    }
                }
                if (!recipient_admin)
                    return false;
            }
        }
        return true;
    }

    public static Fun.Tuple2<Integer, String> isValidAction(DCSet dcSet, int height, Account creator, byte[] signature,
                                                            int actionType,
                                                            long key, AssetCls asset, BigDecimal amount, Account recipient,
                                                            boolean backward, BigDecimal fee, BigDecimal assetFee,
                                                            boolean creatorIsPerson, long checkFlags, long timestamp) {

        boolean wrong;

        if (!asset.isActive(timestamp)) {
            return new Fun.Tuple2<>(INVALID_OUTSIDE_VALIDATY_PERIOD, asset.errorValue);
        }

        if (asset.isUnique() && !amount.abs().stripTrailingZeros().equals(BigDecimal.ONE)) {
            return new Fun.Tuple2<>(INVALID_AMOUNT, "unique amount 1 != " + amount.toPlainString());
        }

        if (asset.isUnTransferable(asset.getMaker().equals(creator))) {
            return new Fun.Tuple2<>(NOT_TRANSFERABLE_ASSET, null);
        }

        // CHECK IF AMOUNT AND ASSET
        if ((checkFlags & NOT_VALIDATE_FLAG_BALANCE) == 0L
                && amount != null) {

            int amount_sign = amount.signum();
            if (amount_sign != 0) {

                long absKey = key;
                if (absKey < 0)
                    absKey = -absKey;

                if (absKey == AssetCls.LIA_KEY) {
                    return new Fun.Tuple2<>(INVALID_TRANSFER_TYPE, "LIA");
                }

                if (asset == null) {
                    return new Fun.Tuple2<>(ITEM_ASSET_NOT_EXIST, "key: " + key);
                }

                // for PARSE and toBYTES need only AMOUNT_LENGTH bytes
                if (absKey > BlockChain.AMOUNT_SCALE_FROM) {
                    byte[] amountBytes = amount.unscaledValue().toByteArray();
                    if (amountBytes.length > AMOUNT_LENGTH) {
                        return new Fun.Tuple2<>(AMOUNT_LENGHT_SO_LONG, "amountBytes.length = " + amountBytes.length);
                    }
                    // SCALE wrong
                    int scale = amount.scale();
                    if (scale < minSCALE
                            || scale > maxSCALE
                            || scale > asset.getScale()) {
                        return new Fun.Tuple2<>(AMOUNT_SCALE_WRONG, "scale: " + scale);
                    }
                }

                if (height > BlockChain.ALL_BALANCES_OK_TO) {

                    int assetType = asset.getAssetType();
                    BigDecimal balance;

                    // условия для особых счетных единиц
                    switch ((int) absKey) {
                        case 111:
                        case 222:
                        case 333:
                        case 444:
                        case 888:
                        case 999:
                            return new Fun.Tuple2<>(ITEM_ASSET_NOT_EXIST, null);
                        case 555:
                            if (actionType != ACTION_SEND)
                                return new Fun.Tuple2<>(INVALID_TRANSFER_TYPE, "555 - not spend");

                            if (amount.compareTo(BigDecimal.ZERO.subtract(BigDecimal.ONE)) < 0)
                                return new Fun.Tuple2<>(NO_BALANCE, "< 1");

                            break;
                        case 666:
                            if (actionType != ACTION_SEND)
                                return new Fun.Tuple2<>(INVALID_TRANSFER_TYPE, "666 - not spend");

                            if (amount.compareTo(BigDecimal.ZERO.subtract(BigDecimal.ONE)) < 0)
                                return new Fun.Tuple2<>(NO_BALANCE, "< 1");

                            break;
                        case 777:
                            if (actionType != ACTION_SEND)
                                return new Fun.Tuple2<>(INVALID_TRANSFER_TYPE, "777 - not spend");

                            if (amount.compareTo(BigDecimal.ZERO.subtract(BigDecimal.ONE)) < 0)
                                return new Fun.Tuple2<>(NO_BALANCE, "< 1");

                            break;
                    }

                    if (asset.isSelfManaged()) {
                        // учетная единица - само контролируемая
                        if (!creator.equals(asset.getMaker())) {
                            return new Fun.Tuple2<>(CREATOR_NOT_MAKER, "creator != asset maker");
                        }
                        if (creator.equals(recipient)) {
                            return new Fun.Tuple2<>(INVALID_ADDRESS, "Creator equal recipient");
                        }

                        // TRY FEE
                        if ((checkFlags & NOT_VALIDATE_FLAG_FEE) == 0
                                && !BlockChain.isFeeEnough(height, creator)
                                && creator.getForFee(dcSet).compareTo(fee) < 0) {
                            return new Fun.Tuple2<>(NOT_ENOUGH_FEE, null);
                        }

                    } else {

                        // самому себе нельзя пересылать
                        if (height > BlockChain.VERS_4_11 && creator.equals(recipient)
                                && actionType != ACTION_SPEND) {
                            return new Fun.Tuple2<>(INVALID_ADDRESS, "Creator equal recipient");
                        }

                        // VALIDATE by ASSET TYPE
                        switch (assetType) {
                            // HOLD GOODS, CHECK myself DEBT for CLAIMS
                            case AssetCls.AS_INSIDE_OTHER_CLAIM:
                                break;
                            case AssetCls.AS_ACCOUNTING:
                                //if (actionType == ACTION_SEND && absKey >= 1000 && !creator.equals(asset.getOwner())) {
                                //    return INVALID_CREATOR;
                                //}
                                break;
                        }

                        boolean unLimited;
                        // VALIDATE by ACTION
                        switch (actionType) {
                            // HOLD GOODS, CHECK myself DEBT for CLAIMS
                            case ACTION_HOLD:

                                if (asset.isUnHoldable()) {
                                    return new Fun.Tuple2<>(NOT_HOLDABLE_ASSET, null);
                                }

                                if (backward) {
                                    // if asset is unlimited and me is creator of this
                                    // asset - for RECIPIENT !
                                    unLimited = asset.isUnlimited(recipient, false);

                                    if (!unLimited && (checkFlags & NOT_VALIDATE_FLAG_BALANCE) == 0) {
                                        balance = recipient.getBalance(dcSet, absKey, actionType).b;
                                        ////BigDecimal amountOWN = recipient.getBalance(dcSet, absKey, ACTION_SEND).b;
                                        // amontOWN, balance and amount - is
                                        // negative
                                        if (balance.compareTo(amount.abs()) < 0) {
                                            return new Fun.Tuple2<>(NO_HOLD_BALANCE, null);
                                        }
                                    }
                                } else {
                                    return new Fun.Tuple2<>(INVALID_HOLD_DIRECTION, null);
                                }

                                if ((checkFlags & NOT_VALIDATE_FLAG_FEE) == 0
                                        && !BlockChain.isFeeEnough(height, creator)
                                        && creator.getForFee(dcSet).compareTo(fee) < 0) {
                                    return new Fun.Tuple2<>(NOT_ENOUGH_FEE, null);
                                }

                                break;

                            case ACTION_DEBT: // DEBT, CREDIT and BORROW

                                if (asset.isUnDebtable()) {
                                    if (height > BlockChain.HOLD_VALID_START + 20000)
                                        return new Fun.Tuple2<>(NOT_DEBTABLE_ASSET, null);
                                }

                                // CLAIMs DEBT - only for OWNER
                                if (asset.isOutsideType()) {
                                    if (!recipient.equals(asset.getMaker())) {
                                        return new Fun.Tuple2<>(INVALID_CLAIM_DEBT_RECIPIENT, "recipient != asset maker");
                                    } else if (creator.equals(asset.getMaker())) {
                                        return new Fun.Tuple2<>(INVALID_CLAIM_DEBT_CREATOR, "creator == asset maker");
                                    }
                                }

                                if (backward) {

                                    // BACKWARD - BORROW - CONFISCATE CREDIT
                                    Tuple3<String, Long, String> creditKey = new Tuple3<String, Long, String>(
                                            creator.getAddress(), absKey, recipient.getAddress());
                                    BigDecimal creditAmount = dcSet.getCredit_AddressesMap().get(creditKey);
                                    if (creditAmount.compareTo(amount) < 0) {
                                        // NOT ENOUGH DEBT from recipient to THIS creator
                                        return new Fun.Tuple2<>(NO_DEBT_BALANCE, null);
                                    }

                                    // тут проверим и по [В ИСПОЛЬЗОВАНИИ] сколько мы можем забрать
                                    // так как он мог потратить из forFEE - долговые
                                    if (!asset.isUnlimited(recipient, false)
                                            && recipient.getBalanceUSE(absKey, dcSet)
                                            .compareTo(amount) < 0) {
                                        return new Fun.Tuple2<>(NO_BALANCE, null);
                                    }

                                } else {
                                    // CREDIT - GIVE CREDIT OR RETURN CREDIT

                                    if (!asset.isUnlimited(creator, false)) {

                                        if (creator.getBalanceUSE(absKey, dcSet)
                                                .compareTo(amount) < 0) {

                                            return new Fun.Tuple2<>(NO_BALANCE, null);
                                        }

                                        Tuple3<String, Long, String> creditKey = new Tuple3<String, Long, String>(
                                                recipient.getAddress(), absKey, creator.getAddress());
                                        // TRY RETURN
                                        BigDecimal creditAmount = dcSet.getCredit_AddressesMap().get(creditKey);
                                        if (creditAmount.compareTo(amount) < 0) {

                                            // TODO: найти ошибку когда возвращаем больше чем на счету
                                            // и идет переворот выдачи займа в dcSet.getCredit_AddressesMap().get(creditKey);
                                            if (false)
                                                return new Fun.Tuple2<>(NO_BALANCE, null);

                                            BigDecimal leftAmount = amount.subtract(creditAmount);
                                            BigDecimal balanceOwn = creator.getBalance(dcSet, absKey, ACTION_SEND).b; // OWN
                                            // balance
                                            // NOT ENOUGHT DEBT from recipient to
                                            // creator
                                            // TRY CREDITN OWN
                                            if (balanceOwn.compareTo(leftAmount) < 0) {
                                                // NOT ENOUGHT DEBT from recipient to
                                                // creator
                                                return new Fun.Tuple2<>(NO_BALANCE, null);
                                            }
                                        }
                                    }
                                }

                                if ((checkFlags & NOT_VALIDATE_FLAG_FEE) == 0
                                        && !BlockChain.isFeeEnough(height, creator)
                                        && creator.getForFee(dcSet).compareTo(fee) < 0) {
                                    return new Fun.Tuple2<>(NOT_ENOUGH_FEE, null);
                                }

                                break;

                            case ACTION_SEND: // SEND ASSET

                                if (absKey == RIGHTS_KEY) {

                                    // byte[] ss = creator.getAddress();
                                    if (height > BlockChain.FREEZE_FROM
                                            && BlockChain.FOUNDATION_ADDRESSES.contains(creator.getAddress())) {
                                        // LOCK PAYMENTS
                                        wrong = true;
                                        for (String address : BlockChain.TRUE_ADDRESSES) {
                                            if (recipient.equals(address)
                                                // || creator.equals(address)
                                            ) {
                                                wrong = false;
                                                break;
                                            }
                                        }

                                        if (wrong) {
                                            // int balance =
                                            // creator.getBalance(dcSet,
                                            // absKey, 1).b.intValue();
                                            // if (balance > 3000)
                                            return new Fun.Tuple2<>(INVALID_CREATOR, "freeze");
                                        }
                                    }
                                }

                                // CLAIMs - invalid for backward to CREATOR - need use SPEND instead
                                if (asset.isOutsideType() && recipient.equals(asset.getMaker())) {
                                    // ERROR
                                    return new Fun.Tuple2<>(INVALID_CLAIM_RECIPIENT, "recipient == asset maker");
                                }


                                if (absKey == FEE_KEY) {

                                    BigDecimal forSale = creator.getForSale(dcSet, FEE_KEY, height, true);

                                    if ((checkFlags & NOT_VALIDATE_FLAG_FEE) == 0) {
                                        forSale = forSale.subtract(fee);
                                        if (assetFee != null && assetFee.signum() != 0) {
                                            // учтем что еще процент с актива
                                            forSale = forSale.subtract(assetFee);
                                        }
                                    }

                                    if (!BlockChain.isFeeEnough(height, creator)
                                            && forSale.compareTo(amount) < 0) {

                                        /// если это девелоп то не проверяем ниже особые счета
                                        if (BlockChain.CLONE_MODE || BlockChain.TEST_MODE)
                                            return new Fun.Tuple2<>(NOT_ENOUGH_FEE, null);

                                        wrong = true;
                                        for (byte[] valid_item : BlockChain.VALID_BAL) {
                                            if (Arrays.equals(signature, valid_item)) {
                                                wrong = false;
                                                break;
                                            }
                                        }

                                        if (wrong)
                                            return new Fun.Tuple2<>(NOT_ENOUGH_FEE, null);
                                    }

                                } else {

                                    // if asset is unlimited and me is creator of this asset
                                    unLimited = asset.isUnlimited(creator, false);
                                    if (unLimited) {
                                        // TRY FEE
                                        if ((checkFlags & NOT_VALIDATE_FLAG_FEE) == 0
                                                && !BlockChain.isFeeEnough(height, creator)
                                                && creator.getForFee(dcSet).compareTo(fee) < 0) {
                                            return new Fun.Tuple2<>(NOT_ENOUGH_FEE, null);
                                        }

                                    } else {

                                        // ALL OTHER ASSET

                                        // проверим баланс по КОМПУ
                                        if ((checkFlags & NOT_VALIDATE_FLAG_FEE) == 0
                                                && !BlockChain.ERA_COMPU_ALL_UP
                                                && !BlockChain.isFeeEnough(height, creator)
                                                && creator.getForFee(dcSet).compareTo(fee) < 0) {
                                            if (BlockChain.CLONE_MODE || BlockChain.TEST_MODE)
                                                return new Fun.Tuple2<>(NOT_ENOUGH_FEE, null);

                                            // TODO: delete wrong check in new CHAIN
                                            // SOME PAYMENTs is WRONG
                                            wrong = true;
                                            for (byte[] valid_item : BlockChain.VALID_BAL) {
                                                if (Arrays.equals(signature, valid_item)) {
                                                    wrong = false;
                                                    break;
                                                }
                                            }

                                            if (wrong)
                                                return new Fun.Tuple2<>(NOT_ENOUGH_FEE, null);
                                        }

                                        BigDecimal forSale = creator.getForSale(dcSet, absKey, height,
                                                true);

                                        if (assetFee != null && assetFee.signum() != 0) {
                                            // учтем что еще процент с актива
                                            forSale = forSale.subtract(assetFee);
                                        }

                                        if (amount.compareTo(forSale) > 0) {
                                            if (BlockChain.CLONE_MODE || BlockChain.TEST_MODE)
                                                return new Fun.Tuple2<>(NO_BALANCE, null);

                                            // TODO: delete wrong check in new CHAIN
                                            // SOME PAYMENTs is WRONG
                                            wrong = true;
                                            for (byte[] valid_item : BlockChain.VALID_BAL) {
                                                if (Arrays.equals(signature, valid_item)) {
                                                    wrong = false;
                                                    break;
                                                }
                                            }

                                            if (wrong)
                                                return new Fun.Tuple2<>(NO_BALANCE, null);
                                        }

                                    }
                                }

                                if (height > BlockChain.FREEZE_FROM) {
                                    String unlock = BlockChain.LOCKED__ADDRESSES.get(creator.getAddress());
                                    if (unlock != null && !recipient.equals(unlock))
                                        return new Fun.Tuple2<>(INVALID_CREATOR, "locked");

                                    Tuple3<String, Integer, Integer> unlockItem = BlockChain.LOCKED__ADDRESSES_PERIOD
                                            .get(creator.getAddress());
                                    if (unlockItem != null && unlockItem.b > height && height < unlockItem.c
                                            && !recipient.equals(unlockItem.a))
                                        return new Fun.Tuple2<>(INVALID_CREATOR, "locked");

                                }

                                break;

                            case ACTION_SPEND: // PRODUCE - SPEND

                                if (asset.isUnSpendable()) {
                                    if (height > BlockChain.HOLD_VALID_START)
                                        return new Fun.Tuple2<>(NOT_SPENDABLE_ASSET, null);
                                }

                                if (backward) {
                                    // PRODUCE is denied - only SPEND
                                    return new Fun.Tuple2<>(INVALID_BACKWARD_ACTION, null);
                                } else {

                                    if (asset.isOutsideType() && !recipient.equals(asset.getMaker())) {
                                        return new Fun.Tuple2<>(INVALID_RECEIVER, "recipient != asset maker");
                                    }

                                    // if asset is unlimited and me is creator of this asset
                                    unLimited = asset.isUnlimited(creator, false);

                                    if (!unLimited) {

                                        BigDecimal forSale = creator.getForSale(dcSet, absKey, height,
                                                false);

                                        if (amount.abs().compareTo(forSale) > 0) {
                                            return new Fun.Tuple2<>(NO_BALANCE, null);
                                        }
                                    }
                                }

                                // TRY FEE
                                if ((checkFlags & NOT_VALIDATE_FLAG_FEE) == 0
                                        && !BlockChain.isFeeEnough(height, creator)
                                        && creator.getForFee(dcSet).compareTo(fee) < 0) {
                                    return new Fun.Tuple2<>(NOT_ENOUGH_FEE, null);
                                }

                                break;

                            case ACTION_PLEDGE: // Учесть передачу в залог и возврат из залога

                                // пока отключим
                                if (true) {
                                    return new Fun.Tuple2<>(INVALID_TRANSFER_TYPE, null);
                                }

                                if (asset.isOutsideType()) {
                                    return new Fun.Tuple2<>(INVALID_TRANSFER_TYPE, null);
                                }

                                if (backward) {
                                    if (!asset.getMaker().equals(recipient))
                                        return new Fun.Tuple2<>(INVALID_BACKWARD_ACTION, null);
                                } else {
                                    if (!asset.getMaker().equals(creator))
                                        return new Fun.Tuple2<>(CREATOR_NOT_MAKER, "asset maker != creator");
                                }

                                // if asset is unlimited and me is creator of this
                                // asset
                                unLimited = asset.isUnlimited(creator, false);

                                if (!unLimited) {

                                    BigDecimal forSale = creator.getForSale(dcSet, absKey, height,
                                            false);

                                    if (amount.abs().compareTo(forSale) > 0) {
                                        return new Fun.Tuple2<>(NO_BALANCE, null);
                                    }
                                }

                                // TRY FEE
                                if ((checkFlags & NOT_VALIDATE_FLAG_FEE) == 0
                                        && !BlockChain.isFeeEnough(height, creator)
                                        && creator.getForFee(dcSet).compareTo(fee) < 0) {
                                    return new Fun.Tuple2<>(NOT_ENOUGH_FEE, null);
                                }

                                break;

                            default:
                                return new Fun.Tuple2<>(INVALID_TRANSFER_TYPE, null);
                        }

                        // IF send from PERSON to ANONYMOUS
                        if (!isValidPersonProtect(dcSet, height, recipient,
                                creatorIsPerson, absKey, actionType,
                                asset))
                            return new Fun.Tuple2<>(RECEIVER_NOT_PERSONALIZED, null);
                    }
                }
            }

        } else {
            // TODO first org.erachain.records is BAD already ((
            // CHECK IF CREATOR HAS ENOUGH FEE MONEY
            if (height > BlockChain.ALL_BALANCES_OK_TO
                    && (checkFlags & NOT_VALIDATE_FLAG_FEE) == 0
                    && !BlockChain.isFeeEnough(height, creator)
                    && creator.getForFee(dcSet).compareTo(fee) < 0) {
                return new Fun.Tuple2<>(NOT_ENOUGH_FEE, null);
            }

        }

        return new Fun.Tuple2<>(VALIDATE_OK, null);
    }

    public static Fun.Tuple2<Integer, String> isValidAction(DCSet dcSet, int height, Account creator, byte[] signature,
                                                            long key, AssetCls asset, BigDecimal amount, Account recipient,
                                                            boolean backward, BigDecimal fee, BigDecimal assetFee,
                                                            boolean creatorIsPerson, long checkFlags, long timestamp) {

        boolean isDirect = asset.isDirectBalances();
        int actionType = Account.balancePosition(key, amount, backward, isDirect);

        return isValidAction(dcSet, height, creator, signature, actionType,
                key, asset, amount, recipient,
                backward, fee, assetFee,
                creatorIsPerson, checkFlags, timestamp);

    }


    public int isValid(int forDeal, long checkFlags) {

        if (height < BlockChain.ALL_VALID_BEFORE) {
            return VALIDATE_OK;
        }

        if (false && (!BlockChain.MAIN_MODE || height > 1000) && getVersion() < 2) {
            return INVALID_BACKWARD_ACTION;
        }

        if (false) {
            for (byte[] valid_item : VALID_REC) {
                if (Arrays.equals(this.signature, valid_item)) {
                    return VALIDATE_OK;
                }
            }
        }

        if (false) {
            int height = this.height > 0 ? this.height : this.getBlockHeightByParentOrLast(dcSet);
        }

        boolean wrong = true;

        // CHECK IF RECIPIENT IS VALID ADDRESS
        if (!Crypto.getInstance().isValidAddress(this.recipient.getAddressBytes())) {
            if (true || height == 120000) {
                wrong = true;
                for (byte[] valid_address : BlockChain.VALID_ADDRESSES) {
                    if (Arrays.equals(this.recipient.getAddressBytes(), valid_address)) {
                        wrong = false;
                        break;
                    }
                }

                if (wrong) {
                    errorValue = this.recipient.getAddress();
                    return INVALID_ADDRESS;
                }
            }
        }

        // CHECK IF REFERENCE TIMESTAMP IS OK
        if (forDeal > FOR_PACK) {
            if (BlockChain.CHECK_DOUBLE_SPEND_DEEP < 0) {
                /// вообще не проверяем в тесте
                if (BlockChain.TEST_DB == 0 && timestamp < Controller.getInstance().getBlockChain().getTimestamp(height - 1)) {
                    // тут нет проверок на двойную трату поэтому только в текущем блоке транзакции принимаем
                    if (BlockChain.CHECK_BUGS > 2)
                        LOGGER.debug(" diff sec: " + (Controller.getInstance().getBlockChain().getTimestamp(height) - timestamp) / 1000);
                    errorValue = "diff sec: " + (Controller.getInstance().getBlockChain().getTimestamp(height) - timestamp) / 1000;
                    return INVALID_TIMESTAMP;
                }
            } else if (BlockChain.CHECK_DOUBLE_SPEND_DEEP > 0) {
                if (timestamp < Controller.getInstance().getBlockChain().getTimestamp(height - BlockChain.CHECK_DOUBLE_SPEND_DEEP)) {
                    // тут нет проверок на двойную трату поэтому только в текущем блоке транзакции принимаем
                    if (BlockChain.CHECK_BUGS > 2)
                        LOGGER.debug(" diff sec: " + (Controller.getInstance().getBlockChain().getTimestamp(height) - timestamp) / 1000);
                    errorValue = "diff sec: " + (Controller.getInstance().getBlockChain().getTimestamp(height) - timestamp) / 1000;
                    return INVALID_TIMESTAMP;
                }

            } else {
                long[] reference = this.creator.getLastTimestamp(dcSet);
                if (reference != null && reference[0] >= this.timestamp
                    // при откатах для нового счета который первый раз сделал транзакцию
                    // из нулевого баланса - Референс будет ошибочный
                    // поэтому отключим эту проверку тут
                    /////   && !(BlockChain.DEVELOP_USE && height < 897144)
                ) {

                    if (height > 0 || BlockChain.CHECK_BUGS > 7
                            || BlockChain.CHECK_BUGS > 1 && System.currentTimeMillis() - pointLogg > 1000) {
                        if (BlockChain.TEST_DB == 0) {
                            pointLogg = System.currentTimeMillis();
                            if (BlockChain.CHECK_BUGS > 2)
                                LOGGER.debug("INVALID TIME!!! REF TIMESTAMP: " + viewCreator() + " " + DateTimeFormat.timestamptoString(reference[0])
                                        + "  TX[timestamp]: " + viewTimestamp() + " diff: " + (this.timestamp - reference[0])
                                        + " BLOCK time diff: " + (Controller.getInstance().getBlockChain().getTimestamp(height) - this.timestamp));
                        }
                    }
                    errorValue = "INVALID TIME!!! REF TIMESTAMP: " + viewCreator() + " " + DateTimeFormat.timestamptoString(reference[0])
                            + "  TX[timestamp]: " + viewTimestamp() + " diff: " + (this.timestamp - reference[0])
                            + " BLOCK time diff: " + (Controller.getInstance().getBlockChain().getTimestamp(height) - this.timestamp);
                    return INVALID_TIMESTAMP;
                }
            }
        }

        boolean isPerson = this.creator.isPerson(dcSet, height, creatorPersonDuration);

        // PUBLIC TEXT only from PERSONS
        if ((checkFlags & NOT_VALIDATE_FLAG_PUBLIC_TEXT) == 0
                && this.hasPublicText() && !isPerson) {
            if (BlockChain.MAIN_MODE && height < 800000 // TODO: remove on new CHAIN
                /// wrong: "1ENwbUNQ7Ene43xWgN7BmNzuoNmFvBxBGjVot3nCRH4fiiL9FaJ6Fxqqt9E4zhDgJADTuqtgrSThp3pqWravkfg")
            ) {
                ;
            } else {
                return CREATOR_NOT_PERSONALIZED;
            }
        }

        if (creatorPerson != null && !creatorPerson.isAlive(this.timestamp)) {
            return ITEM_PERSON_IS_DEAD;
        }

        //////////////////////////////
        // CHECK IF AMOUNT AND ASSET

        if (extFlags < 0L
                && (extFlags & USE_PACKET_MASK) != 0) {
            if (amount != null) {
                errorValue = "amount != null && packet != null";
                return INVALID_AMOUNT;
            } else if (packet == null) {
                errorValue = "packet == null";
                return INVALID_PACKET_SIZE;
            }
        } else if ((typeBytes[2] & NO_AMOUNT_MASK) == 0) {
            if (amount == null) {
                errorValue = "amount == null";
                return INVALID_AMOUNT;
            } else if (packet != null) {
                errorValue = "packet != null";
                return INVALID_PACKET_SIZE;
            }
        }

        if (this.amount != null) {
            BigDecimal assetFee;
            if (assetFEE == null) {
                assetFee = null;
            } else
                assetFee = assetFEE.a;

            Fun.Tuple2<Integer, String> result = isValidAction(dcSet, height, creator, signature, key, asset, amount, recipient,
                    isBackward(), fee, assetFee, isPerson, checkFlags, timestamp);
            if (result.a != VALIDATE_OK) {
                errorValue = result.b;
                return result.a;
            }

        } else if (packet != null) {
            if (packet.length == 0) {
                errorValue = "=0";
                return INVALID_PACKET_SIZE;
            } else if (packet.length > 1000) {
                errorValue = "> " + 1000;
                return INVALID_PACKET_SIZE;
            } else if (balancePos < ACTION_SEND || balancePos > Account.BALANCE_POS_PLEDGE) {
                return INVALID_BALANCE_POS;
            } else if (asset == null) {
                errorValue = "" + key;
                return ITEM_ASSET_NOT_EXIST;
            }

            int count = 0;
            byte[] temp;
            Long rowAssetKey;

            Fun.Tuple2<BigDecimal, BigDecimal> rowAssetFEE;

            HashSet<Long> keys = new HashSet<>();
            for (Object[] row : packet) {
                rowAssetKey = (Long) row[0];
                if (rowAssetKey == null) {
                    errorValue = "[" + count + "] = null";
                    return INVALID_ITEM_KEY;
                } else if (row[7] == null) {
                    errorValue = "[" + count + "] : " + rowAssetKey;
                    return ITEM_ASSET_NOT_EXIST;
                } else if (row[1] == null || ((BigDecimal) row[1]).signum() <= 0) {
                    errorValue = "Amount[" + count + "] = " + row[1];
                    return INVALID_AMOUNT;
                } else if (row[2] != null && ((BigDecimal) row[2]).signum() < 0) {
                    errorValue = "Price[" + count + "] = " + row[2];
                    return INVALID_AMOUNT;
                } else if (row[3] != null && ((BigDecimal) row[3]).signum() < 0) {
                    errorValue = "DiscontedPrice[" + count + "] = " + row[3];
                    return INVALID_AMOUNT;
                } else if (row[PACKET_ROW_MEMO_NO] != null && ((String) row[PACKET_ROW_MEMO_NO]).length() > 0) {
                    temp = ((String) row[PACKET_ROW_MEMO_NO]).getBytes(StandardCharsets.UTF_8);
                    if (temp.length > 255) {
                        errorValue = "Memo[" + count + "].length > 255";
                        return INVALID_MESSAGE_LENGTH;
                    }
                }

                if (keys.contains(rowAssetKey)) {
                    errorValue = "[" + count + "] : " + rowAssetKey;
                    return ITEM_DUPLICATE_KEY;
                } else {
                    keys.add(rowAssetKey);
                }

                // GET ROW ASSET FEE
                if (assetsPacketFEE == null)
                    rowAssetFEE = null;
                else
                    rowAssetFEE = assetsPacketFEE.get(rowAssetKey);

                Fun.Tuple2<Integer, String> result = isValidAction(dcSet, height, creator, signature, rowAssetKey,
                        (AssetCls) row[7], (BigDecimal) row[1], recipient,
                        isBackward(), fee, rowAssetFEE == null ? null : rowAssetFEE.a, isPerson, checkFlags, timestamp);

                if (result.a != VALIDATE_OK) {
                    errorValue = "[" + count + "] : " + result.b;
                    return result.a;
                }

                count++;
            }

        } else {
            // TODO first org.erachain.records is BAD already ((
            // CHECK IF CREATOR HAS ENOUGH FEE MONEY
            if (height > BlockChain.ALL_BALANCES_OK_TO
                    && (checkFlags & NOT_VALIDATE_FLAG_FEE) == 0L
                    && !BlockChain.isFeeEnough(height, creator)
                    && this.creator.getForFee(dcSet).compareTo(this.fee) < 0) {
                return NOT_ENOUGH_FEE;
            }

        }

        // так как мы не лезем в супер класс то тут проверим тоже ее
        if (false && // теперь не проверяем так как ключ сделал длинный dbs.rocksDB.TransactionFinalSignsSuitRocksDB.KEY_LEN
                (checkFlags & NOT_VALIDATE_KEY_COLLISION) == 0l
                && !checkedByPool // транзакция не существует в ожидании - иначе там уже проверили
                && BlockChain.CHECK_DOUBLE_SPEND_DEEP == 0 && this.dcSet.getTransactionFinalMapSigns().contains(this.signature)) {
            // потому что мы ключ урезали до 12 байт - могут быть коллизии
            return KEY_COLLISION;
        }

        return VALIDATE_OK;
    }

    public static void processAction(DCSet dcSet, boolean asOrphan, PublicKeyAccount creator, Account recipient,
                                     int balancePos, long absKey, AssetCls asset, long key, BigDecimal amount,
                                     boolean backward, boolean incomeReverse) {

        boolean subtract = asOrphan ^ backward;
        boolean isDirect = asset.isDirectBalances();

        // STANDARD ACTION PROCESS
        // UPDATE SENDER
        if (absKey == 666L) {
            creator.changeBalance(dcSet, subtract, backward, key, amount, isDirect, false, !incomeReverse);
        } else {
            creator.changeBalance(dcSet, !subtract, backward, key, amount, isDirect, false, !incomeReverse);
        }
        // UPDATE RECIPIENT
        recipient.changeBalance(dcSet, subtract, backward, key, amount, isDirect, true, incomeReverse);

        if (balancePos == ACTION_DEBT) {
            String creatorStr = creator.getAddress();
            String recipientStr = recipient.getAddress();
            Tuple3<String, Long, String> creditKey = new Tuple3<>(creatorStr, absKey, recipientStr);
            Tuple3<String, Long, String> creditKeyRecipient = new Tuple3<>(recipientStr, absKey, creatorStr);

            if (asOrphan) {
                if (backward) {
                    // BORROW
                    dcSet.getCredit_AddressesMap().add(creditKey, amount);
                } else {
                    // in BACK order - RETURN CREDIT << CREDIT
                    // GET CREDIT for left AMOUNT
                    BigDecimal leftAmount = dcSet.getCredit_AddressesMap().get(creditKey);
                    if (leftAmount.compareTo(amount) < 0) {
                        dcSet.getCredit_AddressesMap().sub(creditKey, leftAmount);
                        // RETURN my DEBT and make reversed DEBT
                        dcSet.getCredit_AddressesMap().add(creditKeyRecipient, amount.subtract(leftAmount));
                    } else {
                        // ONLY RETURN CREDIT
                        dcSet.getCredit_AddressesMap().sub(creditKey, amount);
                    }
                }
            } else {
                if (backward) {
                    // BORROW
                    dcSet.getCredit_AddressesMap().sub(creditKey, amount);
                } else {
                    // CREDIT or RETURN CREDIT
                    BigDecimal creditAmount = dcSet.getCredit_AddressesMap().get(creditKeyRecipient);
                    if (creditAmount.compareTo(amount) >= 0) {
                        // ALL CREDIT RETURN
                        dcSet.getCredit_AddressesMap().sub(creditKeyRecipient, amount);
                    } else {
                        // update creditAmount to 0
                        BigDecimal leftAmount;
                        if (creditAmount.signum() != 0) {
                            dcSet.getCredit_AddressesMap().sub(creditKeyRecipient, creditAmount);
                            // GET CREDIT for left AMOUNT
                            leftAmount = amount.subtract(creditAmount);
                        } else {
                            leftAmount = amount;
                        }

                        dcSet.getCredit_AddressesMap().add(creditKey, leftAmount);
                    }
                }
            }
        }

        if (balancePos == ACTION_SEND && asset.isChangeDebtBySendActions()) {
            // если это актив который должен поменять и балансы Долговые то
            // тут не важно какое направление и какой остаток - все одинаково - учетный же

            processAction(dcSet, asOrphan, creator, recipient, ACTION_DEBT,
                    absKey, asset, -key, amount, backward, incomeReverse);
        } else if (balancePos == ACTION_SPEND && amount.signum() < 0 && asset.isChangeDebtBySpendActions()) {
            // если это актив в Требованием Исполнения - то подтверждение Исполнения уменьшит и Требование Исполнения
            // Но ПОЛУЧАТЕЛЬ - у нас создатель Актива

            // смотрим какой там долг (он отрицательный)
            BigDecimal debtBalance = creator.getBalance(dcSet, absKey, ACTION_DEBT).b;
            // и берем наибольший из них (там оба отрицательные) - так чтобы если Требование меньше Чем  текущее Действие - чтобы в минус не ушло
            debtBalance = debtBalance.max(amount);

            if (debtBalance.signum() != 0) {
                processAction(dcSet, !asOrphan, creator, asset.getMaker(), ACTION_DEBT,
                        absKey, asset, key, debtBalance.negate(), backward, incomeReverse);
            }
        }

    }

    public static void processActionBody(DCSet dcSet, boolean asOrphan, Block block, long dbRef,
                                         PublicKeyAccount creator, Account recipient,
                                         int balancePosition, boolean backward, BigDecimal amount,
                                         long key, long absKey, AssetCls asset, BigDecimal assetFEE) {

        // BACKWARD - CONFISCATE
        boolean incomeReverse = balancePosition == ACTION_HOLD;

        // STANDARD ACTION ORPHAN
        processAction(dcSet, asOrphan, creator, recipient, balancePosition, absKey, asset, key, amount, backward, incomeReverse);

        if (absKey == RIGHTS_KEY && block != null) {
            block.addForgingInfoUpdate(recipient);
        }

        if (assetFEE != null && assetFEE.signum() != 0) {
            // proc: this.creator.changeBalance(dcSet, !backward, backward, absKey, this.assetFEE.a, false, false, !incomeReverse);
            // orph: this.creator.changeBalance(dcSet, backward, backward, absKey, this.assetFEE.a, false, false, !incomeReverse);

            creator.changeBalance(dcSet, !asOrphan ^ !backward, backward, absKey, assetFEE, false, false, !incomeReverse);

            if (block != null && !asOrphan) {
                block.addCalculated(creator, absKey,
                        assetFEE.negate(), "Asset Fee", dbRef);
            }
        }

    }

    @Override
    public void processBody(Block block, int forDeal) {

        super.processBody(block, forDeal);

        if (this.amount == null) {
            if (packet == null)
                return;

            boolean backward = isBackward();
            BigDecimal assetFeeRow;
            BigDecimal amountRow;
            Long assetKeyRow;
            Fun.Tuple2<Integer, Integer> signs;
            // ROW:
            // 0: (long) AssetKey, 1: Amount, 2: Price, 3: Discounted Price, 4: Tax as percent, 5: Fee as absolute value, 6: memo, 7: Asset (after setDC())
            for (Object[] row : packet) {

                // see core.exdata.exActions.ExFilteredPays.makeFilterPayList
                signs = Account.getSignsForBalancePos(balancePos);
                assetKeyRow = signs.a * (Long) row[0];
                amountRow = signs.b > 0 ? (BigDecimal) row[1] : ((BigDecimal) row[1]).negate();


                if (assetsPacketFEE != null && assetsPacketFEE.containsKey((AssetCls) row[7]))
                    assetFeeRow = assetsPacketFEE.get((AssetCls) row[7]).a;
                else
                    assetFeeRow = null;

                // STANDARD ACTION PROCESS
                processActionBody(dcSet, false, block, dbRef, creator, recipient,
                        balancePos, backward, amountRow,
                        assetKeyRow, (Long) row[0], (AssetCls) row[7], assetFeeRow);

            }

            return;

        }

        int amount_sign = this.amount.compareTo(BigDecimal.ZERO);
        if (amount_sign == 0)
            return;

        // BACKWARD - CONFISCATE
        boolean backward = isBackward();
        boolean isDirect = asset.isDirectBalances();
        int balancePosition = Account.balancePosition(key, amount, backward, isDirect);
        long absKey = getAbsKey();
        boolean incomeReverse = balancePosition == ACTION_HOLD;

        // STANDARD ACTION PROCESS
        processAction(dcSet, false, creator, recipient, balancePosition, absKey, asset, key, amount, backward, incomeReverse);

        if (absKey == RIGHTS_KEY && block != null) {
            block.addForgingInfoUpdate(this.recipient);
        }

        if (assetFEE != null && assetFEE.a.signum() != 0) {
            // учтем что он еще заплатил комиссию с суммы
            this.creator.changeBalance(dcSet, !backward, backward, absKey, this.assetFEE.a, false, false, !incomeReverse);
            if (block != null) {
                block.addCalculated(this.creator, absKey,
                        this.assetFEE.a.negate(), "Asset Fee", this.dbRef);
            }
        }

    }

    @Override
    public void orphanBody(Block block, int forDeal) {

        super.orphanBody(block, forDeal);

        if (this.amount == null) {
            if (packet == null)
                return;

            boolean backward = isBackward();
            BigDecimal assetFeeRow;
            BigDecimal amountRow;
            Long assetKeyRow;
            Fun.Tuple2<Integer, Integer> signs;
            // ROW:
            // 0: (long) AssetKey, 1: Amount, 2: Price, 3: Discounted Price, 4: Tax as percent, 5: Fee as absolute value, 6: memo, 7: Asset (after setDC())
            for (Object[] row : packet) {

                // see core.exdata.exActions.ExFilteredPays.makeFilterPayList
                signs = Account.getSignsForBalancePos(balancePos);
                assetKeyRow = signs.a * (Long) row[0];
                amountRow = signs.b > 0 ? (BigDecimal) row[1] : ((BigDecimal) row[1]).negate();


                if (assetsPacketFEE.containsKey((AssetCls) row[7]))
                    assetFeeRow = assetsPacketFEE.get((AssetCls) row[7]).a;
                else
                    assetFeeRow = null;

                // STANDARD ACTION PROCESS
                processActionBody(dcSet, true, block, dbRef, creator, recipient,
                        balancePos, backward, amountRow,
                        assetKeyRow, (Long) row[0], (AssetCls) row[7], assetFeeRow);

            }

            return;
        }

        int amount_sign = this.amount.compareTo(BigDecimal.ZERO);
        if (amount_sign == 0)
            return;

        // BACKWARD - CONFISCATE
        boolean backward = isBackward();
        boolean isDirect = asset.isDirectBalances();
        long absKey = getAbsKey();
        int balancePosition = Account.balancePosition(key, amount, backward, isDirect);
        boolean incomeReverse = balancePosition == ACTION_HOLD;

        // STANDARD ACTION ORPHAN
        processAction(dcSet, true, creator, recipient, balancePosition, absKey, asset, key, amount, backward, incomeReverse);

        if (absKey == RIGHTS_KEY && block != null) {
            block.addForgingInfoUpdate(this.recipient);
        }

        if (assetFEE != null && assetFEE.a.signum() != 0) {
            this.creator.changeBalance(dcSet, backward, backward, absKey, this.assetFEE.a, false, false, !incomeReverse);
        }

    }

    public Map<String, Map<Long, BigDecimal>> getAssetAmount() {
        Map<String, Map<Long, BigDecimal>> assetAmount = new LinkedHashMap<>();
        
        if (this.amount != null) {
            
            assetAmount = subAssetAmount(assetAmount, this.creator.getAddress(), FEE_KEY, this.fee);
            
            assetAmount = subAssetAmount(assetAmount, this.creator.getAddress(), this.key, this.amount);
            assetAmount = addAssetAmount(assetAmount, this.recipient.getAddress(), this.key, this.amount);
        }
        
        return assetAmount;
    }

}
