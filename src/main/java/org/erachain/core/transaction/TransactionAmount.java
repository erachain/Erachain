package org.erachain.core.transaction;

import com.google.common.primitives.Bytes;
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
import org.erachain.datachain.DCSet;
import org.erachain.utils.DateTimeFormat;
import org.erachain.utils.NumberAsString;
import org.json.simple.JSONObject;
import org.mapdb.Fun;
import org.mapdb.Fun.Tuple3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;

/*

## typeBytes
0 - record type
1 - record version
2 - property 1
3 = property 2

## version 0
// typeBytes[2] = -128 if NO AMOUNT
// typeBytes[3] = -128 if NO DATA

## version 1
if backward - CONFISCATE CREDIT

## version 2

#### PROPERTY 1
typeBytes[2].0 = -128 if NO AMOUNT - check sign
typeBytes[2].1 = 64 if backward (CONFISCATE CREDIT, ...)

#### PROPERTY 2
typeBytes[3].0 = -128 if NO DATA - check sign

## version 3

#### PROPERTY 1
typeBytes[2].0 = -128 if NO AMOUNT - check sign
typeBytes[2].1 = 64 if backward (CONFISCATE CREDIT, ...)

#### PROPERTY 2
typeBytes[3].0 = 128 if NO DATA - check sign = '10000000' = Integer.toBinaryString(128)
typeBytes[3].3-7 = point accuracy: -16..16 = BYTE - 16

 */

/**
 *
 */
public abstract class TransactionAmount extends Transaction implements Itemable{
    public static final byte[][] VALID_REC = new byte[][]{
        //Base58.decode("2PLy4qTVeYnwAiESvaeaSUTWuGcERQr14bpGj3qo83c4vTP8RRMjnmRXnd6USsbvbLwWUNtjErcdvs5KtZMpyREC"),
    };

    static Logger LOGGER = LoggerFactory.getLogger(TransactionAmount.class.getName());

    public static final int SCALE_MASK = 31;
    public static final int SCALE_MASK_HALF = (SCALE_MASK + 1) >> 1;
    public static final int maxSCALE = TransactionAmount.SCALE_MASK_HALF + BlockChain.AMOUNT_DEDAULT_SCALE - 1;
    public static final int minSCALE = BlockChain.AMOUNT_DEDAULT_SCALE - TransactionAmount.SCALE_MASK_HALF;

    public static final byte BACKWARD_MASK = 64;

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

    /*
     * public static final String NAME_ACTION_TYPE_BACKWARD_PROPERTY =
     * "backward PROPERTY"; public static final String
     * NAME_ACTION_TYPE_BACKWARD_HOLD = "backward HOLD"; public static final
     * String NAME_ACTION_TYPE_BACKWARD_CREDIT = "backward CREDIT"; public
     * static final String NAME_ACTION_TYPE_BACKWARD_SPEND = "backward SPEND";
     */
    public static final String NAME_ACTION_TYPE_PROPERTY = "SEND";
    public static final String NAME_ACTION_TYPE_PROPERTY_WAS = "Send # was";
    public static final String NAME_ACTION_TYPE_HOLD = "HOLD";
    public static final String NAME_ACTION_TYPE_HOLD_WAS = "Hold # was";
    public static final String NAME_CREDIT = "CREDIT";
    public static final String NAME_CREDIT_WAS = "Credit # was";
    public static final String NAME_SPEND = "SPEND";
    public static final String NAME_SPEND_WAS = "Spend # was";
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
    protected long key; //  = Transaction.FEE_KEY;
    protected AssetCls asset;

    // need for calculate fee by feePow into GUI
    protected TransactionAmount(byte[] typeBytes, String name, PublicKeyAccount creator, ExLink exLink, byte feePow, Account recipient,
                                BigDecimal amount, long key, long timestamp, Long reference) {
        super(typeBytes, name, creator, exLink, feePow, timestamp, reference);
        this.recipient = recipient;

        if (amount == null || amount.equals(BigDecimal.ZERO)) {
            // set version to 1
            typeBytes[2] = (byte) (typeBytes[2] | (byte) -128);
        } else {
            // RESET 0 bit
            typeBytes[2] = (byte) (typeBytes[2] & (byte) 127);

            this.amount = amount;
            this.key = key;
        }
    }

    // need for calculate fee
    protected TransactionAmount(byte[] typeBytes, String name, PublicKeyAccount creator, byte feePow, Account recipient,
                                BigDecimal amount, long key, long timestamp, Long reference, byte[] signature) {
        this(typeBytes, name, creator, null, feePow, recipient, amount, key, timestamp, reference);
        this.signature = signature;
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

        if (this.amount != null && dcSet != null) {
            this.asset = this.dcSet.getItemAssetMap().get(this.getAbsKey());
        }

        if (false && andUpdateFromState && !isWiped())
            updateFromStateDB();

    }

    @Override
    public void setDC(DCSet dcSet, int forDeal, int blockHeight, int seqNo, boolean andUpdateFromState) {
        super.setDC(dcSet, forDeal, blockHeight, seqNo, false);

        if (BlockChain.CHECK_BUGS > 3// && viewDBRef(dbRef).equals("18165-1")
        ) {
            boolean debug;
            debug = true;
        }

        if (false && andUpdateFromState && !isWiped())
            updateFromStateDB();
    }

    // public static String getName() { return "unknown subclass Amount"; }
    
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

    @Override
    public void makeItemsKeys() {
        if (isWiped()) {
            itemsKeys = new Object[][]{};
        }

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
                }
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

    public BigDecimal getAmountAndBackward() {
        // return this.amount == null? BigDecimal.ZERO: this.amount;
        if (isBackward()) {
            return this.amount.negate();
        } else {
            return this.amount;
        }
    }

    public String getStr() {
        return "transAmount";
    }
    
    // VIEW
    @Override
    public String viewRecipient() {
        return recipient.getPersonAsString();
    }
    
    @Override
    public BigDecimal getAmount(String address) {
        BigDecimal amount = BigDecimal.ZERO;
        
        if (this.amount != null) {
            
            if (address.equals(this.creator.getAddress())) {
                // IF SENDER
                amount = amount.subtract(this.amount);
            } else if (address.equals(this.recipient.getAddress())) {
                // IF RECIPIENT
                amount = amount.add(this.amount);
            }
        }

        return amount;
    }

    @Override
    public BigDecimal getAmount(Account account) {
        String address = account.getAddress();
        return getAmount(address);
    }

    @Override
    public long calcBaseFee(boolean withFreeProtocol) {

        long long_fee = super.calcBaseFee(withFreeProtocol);
        if (long_fee == 0)
            // если бесплатно то и процентную комиссию (ниже) не считаем!
            return 0L;

        // ПРОЦЕНТЫ в любом случае посчитаем - даже если халявная транзакция
        if (hasAmount() && balancePosition() == ACTION_SEND // только для передачи в собственность!
                && !BlockChain.ASSET_TRANSFER_PERCENTAGE.isEmpty()
                && BlockChain.ASSET_TRANSFER_PERCENTAGE.containsKey(key)
                && !isInvolved(asset.getMaker())) {
            Fun.Tuple2<BigDecimal, BigDecimal> percItem = BlockChain.ASSET_TRANSFER_PERCENTAGE.get(key);
            assetFee = amount.abs().multiply(percItem.a).setScale(asset.getScale(), RoundingMode.DOWN);
            if (assetFee.compareTo(percItem.b) < 0) {
                // USE MINIMAL VALUE
                assetFee = percItem.b.setScale(asset.getScale(), RoundingMode.DOWN);
            }
            if (!BlockChain.ASSET_BURN_PERCENTAGE.isEmpty()
                    && BlockChain.ASSET_BURN_PERCENTAGE.containsKey(key)) {
                assetFeeBurn = assetFee.multiply(BlockChain.ASSET_BURN_PERCENTAGE.get(key)).setScale(asset.getScale(), RoundingMode.UP);
            }
            return long_fee >> 1;
        }
        return long_fee;
    }

    public boolean hasAmount() {
        return amount != null && amount.signum() != 0;
    }

    public int balancePosition() {
        if (!hasAmount())
            return 0;
        return Account.balancePosition(this.key, this.amount, this.isBackward(), asset.isSelfManaged());
    }

    // BACKWARD AMOUNT
    public boolean isBackward() {
        return typeBytes[1] == 1 || typeBytes[1] > 1 && (typeBytes[2] & BACKWARD_MASK) > 0;
    }

    /*
     * ************** VIEW
     */

    @Override
    public String viewTypeName() {
        return viewTypeName(this.amount, isBackward());
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

    public static String viewSubTypeName(long assetKey, BigDecimal amount, boolean isBackward, boolean isDirect) {

        if (amount == null || amount.signum() == 0)
            return "";

        int actionType = Account.balancePosition(assetKey, amount, isBackward, isDirect);

        switch (actionType) {
            case ACTION_SEND:
                return NAME_ACTION_TYPE_PROPERTY;
            case ACTION_DEBT:
                return NAME_CREDIT;
            case ACTION_HOLD:
                return NAME_ACTION_TYPE_HOLD;
            case ACTION_SPEND:
                return NAME_SPEND;
        }

        return "???";

    }

    @Override
    public String viewSubTypeName() {
        if (amount == null || amount.signum() == 0)
            return "";

        return viewSubTypeName(key, amount, isBackward(), asset.isDirectBalances());
    }

    @Override
    public String viewAmount() {
        if (this.amount == null)
            return "";

        if (this.amount.signum() < 0) {
            return this.amount.negate().toPlainString();
        } else {
            return this.amount.toPlainString();
        }
    }
    
    @Override
    public String viewAmount(Account account) {
        if (this.amount == null)
            return "";
        String address = account.getAddress();
        return NumberAsString.formatAsString(getAmount(address));
    }
    
    @Override
    public String viewAmount(String address) {
        if (this.amount == null)
            return "";
        return NumberAsString.formatAsString(getAmount(address));
    }

    @Override
    public String viewFullTypeName() {
        return viewActionType();
    }

    public String viewActionType() {
        if (asset == null)
            return "Mail";

        //return viewActionType(this.key, this.amount, this.isBackward(), asset.isDirectBalances());
        return asset.viewAssetTypeAction(isBackward(), balancePosition(), creator == null ? false : asset.getMaker().equals(creator));
    }

    // PARSE/CONVERT
    // @Override
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
            amountBytes = Bytes.ensureCapacity(amountBytes, AMOUNT_LENGTH, 0);
            data = Bytes.concat(data, amountBytes);
        }
        
        return data;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    protected JSONObject getJsonBase() {
        JSONObject transaction = super.getJsonBase();
        
        transaction.put("recipient", this.recipient.getAddress());
        if (this.amount != null) {
            transaction.put("asset", this.getAbsKey());
            transaction.put("assetKey", this.getAbsKey());
            transaction.put("amount", this.amount.toPlainString());
            transaction.put("balancePos", this.balancePosition());
            transaction.put("actionName", viewActionType());
            if (this.isBackward())
                transaction.put("backward", this.isBackward());
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

        if (!withSignature)
            base_len -= SIGNATURE_LENGTH;

        return base_len - (this.typeBytes[2] < 0 ? (KEY_LENGTH + AMOUNT_LENGTH) : 0);
    }

    //@Override // - fee + balance - calculate here
    private static long pointLogg;

    public static boolean isValidPersonProtect(DCSet dcSet, int height, Account recipient,
                                               boolean creatorIsPerson, long absKey, int actionType,
                                               AssetCls asset) {
        if (BlockChain.PERSON_SEND_PROTECT && creatorIsPerson && absKey != FEE_KEY
                && actionType != ACTION_DEBT && actionType != ACTION_HOLD && actionType != ACTION_SPEND
                && asset.isPersonProtected()
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

    public static int isValidAction(DCSet dcSet, int height, Account creator, byte[] signature,
                                    long key, AssetCls asset, BigDecimal amount, Account recipient,
                                    boolean backward, BigDecimal fee, BigDecimal assetFee,
                                    boolean creatorIsPerson, long flags) {

        boolean wrong;

        if (asset.isUnique() && !amount.abs().equals(BigDecimal.ONE)) {
            return Transaction.INVALID_AMOUNT;
        }

        if (asset.isUnTransferable()) {
            return Transaction.NOT_TRANSFERABLE_ASSET;
        }

        // CHECK IF AMOUNT AND ASSET
        if ((flags & NOT_VALIDATE_FLAG_BALANCE) == 0L
                && amount != null) {

            int amount_sign = amount.signum();
            if (amount_sign != 0) {

                long absKey = key;
                if (absKey < 0)
                    absKey = -absKey;

                if (absKey == AssetCls.LIA_KEY) {
                    return INVALID_TRANSFER_TYPE;
                }

                if (asset == null) {
                    return ITEM_ASSET_NOT_EXIST;
                }

                // самому себе нельзя пересылать
                if (height > BlockChain.VERS_4_11 && creator.equals(recipient)) {
                    return Transaction.INVALID_ADDRESS;
                }

                // for PARSE and toBYTES need only AMOUNT_LENGTH bytes
                if (absKey > BlockChain.AMOUNT_SCALE_FROM) {
                    byte[] amountBytes = amount.unscaledValue().toByteArray();
                    if (amountBytes.length > AMOUNT_LENGTH) {
                        return AMOUNT_LENGHT_SO_LONG;
                    }
                    // SCALE wrong
                    int scale = amount.scale();
                    if (scale < minSCALE
                            || scale > maxSCALE
                            || scale > asset.getScale()) {
                        return AMOUNT_SCALE_WRONG;
                    }
                }

                if (height > BlockChain.ALL_BALANCES_OK_TO) {

                    // BACKWARD - CONFISCATE
                    boolean isDirect = asset.isDirectBalances();

                    int actionType = Account.balancePosition(key, amount, backward, isDirect);
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
                            return ITEM_ASSET_NOT_EXIST;
                        case 555:
                            if (actionType != ACTION_SEND)
                                return INVALID_TRANSFER_TYPE;

                            if (amount.compareTo(BigDecimal.ZERO.subtract(BigDecimal.ONE)) < 0)
                                return NO_BALANCE;

                            break;
                        case 666:
                            if (actionType != ACTION_SEND)
                                return INVALID_TRANSFER_TYPE;

                            if (amount.compareTo(BigDecimal.ZERO.subtract(BigDecimal.ONE)) < 0)
                                return NO_BALANCE;

                            break;
                        case 777:
                            if (actionType != ACTION_SEND)
                                return INVALID_TRANSFER_TYPE;

                            if (amount.compareTo(BigDecimal.ZERO.subtract(BigDecimal.ONE)) < 0)
                                return NO_BALANCE;

                            break;
                    }

                    if (asset.isSelfManaged()) {
                        // учетная единица - само контролируемая
                        if (!creator.equals(asset.getMaker())) {
                            return CREATOR_NOT_OWNER;
                        }
                        if (creator.equals(recipient)) {
                            return Transaction.INVALID_ADDRESS;
                        }

                        // TRY FEE
                        if ((flags & Transaction.NOT_VALIDATE_FLAG_FEE) == 0
                                && !BlockChain.isFeeEnough(height, creator)
                                && creator.getForFee(dcSet).compareTo(fee) < 0) {
                            return NOT_ENOUGH_FEE;
                        }

                    } else {

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
                                    return NOT_HOLDABLE_ASSET;
                                }

                                if (backward) {
                                    // if asset is unlimited and me is creator of this
                                    // asset - for RECIPIENT !
                                    unLimited = asset.isUnlimited(recipient, false);

                                    if (!unLimited && (flags & Transaction.NOT_VALIDATE_FLAG_BALANCE) == 0) {
                                        balance = recipient.getBalance(dcSet, absKey, actionType).b;
                                        ////BigDecimal amountOWN = recipient.getBalance(dcSet, absKey, ACTION_SEND).b;
                                        // amontOWN, balance and amount - is
                                        // negative
                                        if (balance.compareTo(amount.abs()) < 0) {
                                            return NO_HOLD_BALANCE;
                                        }
                                    }
                                } else {
                                    return INVALID_HOLD_DIRECTION;
                                }

                                if ((flags & Transaction.NOT_VALIDATE_FLAG_FEE) == 0
                                        && !BlockChain.isFeeEnough(height, creator)
                                        && creator.getForFee(dcSet).compareTo(fee) < 0) {
                                    return NOT_ENOUGH_FEE;
                                }

                                break;

                            case ACTION_DEBT: // DEBT, CREDIT and BORROW

                                if (asset.isUnDebtable()) {
                                    if (height > BlockChain.HOLD_VALID_START + 20000)
                                        return NOT_DEBTABLE_ASSET;
                                }

                                // CLAIMs DEBT - only for OWNER
                                if (asset.isOutsideType()) {
                                    if (!recipient.equals(asset.getMaker())) {
                                        return Transaction.INVALID_CLAIM_DEBT_RECIPIENT;
                                    } else if (creator.equals(asset.getMaker())) {
                                        return Transaction.INVALID_CLAIM_DEBT_CREATOR;
                                    }
                                }

                                if (backward) {

                                    // BACKWARD - BORROW - CONFISCATE CREDIT
                                    Tuple3<String, Long, String> creditKey = new Tuple3<String, Long, String>(
                                            creator.getAddress(), absKey, recipient.getAddress());
                                    BigDecimal creditAmount = dcSet.getCredit_AddressesMap().get(creditKey);
                                    if (creditAmount.compareTo(amount) < 0) {
                                        // NOT ENOUGH DEBT from recipient to THIS creator
                                        return NO_DEBT_BALANCE;
                                    }

                                    // тут проверим и по [В ИСПОЛЬЗОВАНИИ] сколько мы можем забрать
                                    // так как он мог потратить из forFEE - долговые
                                    if (!asset.isUnlimited(recipient, false)
                                            && recipient.getBalanceUSE(absKey, dcSet)
                                            .compareTo(amount) < 0) {
                                        return NO_BALANCE;
                                    }

                                } else {
                                    // CREDIT - GIVE CREDIT OR RETURN CREDIT

                                    if (!asset.isUnlimited(creator, false)) {

                                        if (creator.getBalanceUSE(absKey, dcSet)
                                                .compareTo(amount) < 0) {

                                            return NO_BALANCE;
                                        }

                                        Tuple3<String, Long, String> creditKey = new Tuple3<String, Long, String>(
                                                recipient.getAddress(), absKey, creator.getAddress());
                                        // TRY RETURN
                                        BigDecimal creditAmount = dcSet.getCredit_AddressesMap().get(creditKey);
                                        if (creditAmount.compareTo(amount) < 0) {

                                            // TODO: найти ошибку когда возвращаем больше чем на счету
                                            // и идет переворот выдачи займа в dcSet.getCredit_AddressesMap().get(creditKey);
                                            if (false)
                                                return NO_BALANCE;

                                            BigDecimal leftAmount = amount.subtract(creditAmount);
                                            BigDecimal balanceOwn = creator.getBalance(dcSet, absKey, ACTION_SEND).b; // OWN
                                            // balance
                                            // NOT ENOUGHT DEBT from recipient to
                                            // creator
                                            // TRY CREDITN OWN
                                            if (balanceOwn.compareTo(leftAmount) < 0) {
                                                // NOT ENOUGHT DEBT from recipient to
                                                // creator
                                                return NO_BALANCE;
                                            }
                                        }
                                    }
                                }

                                if ((flags & Transaction.NOT_VALIDATE_FLAG_FEE) == 0
                                        && !BlockChain.isFeeEnough(height, creator)
                                        && creator.getForFee(dcSet).compareTo(fee) < 0) {
                                    return NOT_ENOUGH_FEE;
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
                                            return INVALID_CREATOR;
                                        }
                                    }
                                }

                                // CLAIMs - invalid for backward to CREATOR - need use SPEND instead
                                if (asset.isOutsideType() && recipient.equals(asset.getMaker())) {
                                    // ERROR
                                    return Transaction.INVALID_CLAIM_RECIPIENT;
                                }


                                if (absKey == FEE_KEY) {

                                    BigDecimal forSale = creator.getForSale(dcSet, FEE_KEY, height, true);
                                    if (assetFee != null && assetFee.signum() != 0) {
                                        // учтем что еще процент с актива
                                        forSale = forSale.subtract(assetFee);
                                    }

                                    if ((flags & Transaction.NOT_VALIDATE_FLAG_FEE) == 0
                                            && !BlockChain.isFeeEnough(height, creator)
                                            && forSale.compareTo(amount.add(fee)) < 0) {

                                        /// если это девелоп то не проверяем ниже особые счета
                                        if (BlockChain.CLONE_MODE || BlockChain.TEST_MODE)
                                            return NOT_ENOUGH_FEE;

                                        wrong = true;
                                        for (byte[] valid_item : BlockChain.VALID_BAL) {
                                            if (Arrays.equals(signature, valid_item)) {
                                                wrong = false;
                                                break;
                                            }
                                        }

                                        if (wrong)
                                            return NOT_ENOUGH_FEE;
                                    }

                                } else {

                                    // if asset is unlimited and me is creator of this asset
                                    unLimited = asset.isUnlimited(creator, false);
                                    if (unLimited) {
                                        // TRY FEE
                                        if ((flags & Transaction.NOT_VALIDATE_FLAG_FEE) == 0
                                                && !BlockChain.isFeeEnough(height, creator)
                                                && creator.getForFee(dcSet).compareTo(fee) < 0) {
                                            return NOT_ENOUGH_FEE;
                                        }

                                    } else {

                                        // ALL OTHER ASSET

                                        // проверим баланс по КОМПУ
                                        if ((flags & Transaction.NOT_VALIDATE_FLAG_FEE) == 0
                                                && !BlockChain.ERA_COMPU_ALL_UP
                                                && !BlockChain.isFeeEnough(height, creator)
                                                && creator.getForFee(dcSet).compareTo(fee) < 0) {
                                            if (BlockChain.CLONE_MODE || BlockChain.TEST_MODE)
                                                return NOT_ENOUGH_FEE;

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
                                                return NOT_ENOUGH_FEE;
                                        }

                                        BigDecimal forSale = creator.getForSale(dcSet, absKey, height,
                                                true);

                                        if (assetFee != null && assetFee.signum() != 0) {
                                            // учтем что еще процент с актива
                                            forSale = forSale.subtract(assetFee);
                                        }

                                        if (amount.compareTo(forSale) > 0) {
                                            if (BlockChain.CLONE_MODE || BlockChain.TEST_MODE)
                                                return NO_BALANCE;

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
                                                return NO_BALANCE;
                                        }

                                    }
                                }

                                if (height > BlockChain.FREEZE_FROM) {
                                    String unlock = BlockChain.LOCKED__ADDRESSES.get(creator.getAddress());
                                    if (unlock != null && !recipient.equals(unlock))
                                        return INVALID_CREATOR;

                                    Tuple3<String, Integer, Integer> unlockItem = BlockChain.LOCKED__ADDRESSES_PERIOD
                                            .get(creator.getAddress());
                                    if (unlockItem != null && unlockItem.b > height && height < unlockItem.c
                                            && !recipient.equals(unlockItem.a))
                                        return INVALID_CREATOR;

                                }

                                break;

                            case ACTION_SPEND: // PRODUCE - SPEND

                                if (asset.isUnSpendable()) {
                                    if (height > BlockChain.HOLD_VALID_START)
                                        return NOT_SPENDABLE_ASSET;
                                }

                                if (backward) {
                                    // PRODUCE is denied - only SPEND
                                    return INVALID_BACKWARD_ACTION;
                                } else {

                                    if (asset.isOutsideType() && !recipient.equals(asset.getMaker())) {
                                        return Transaction.INVALID_RECEIVER;
                                    }

                                    // if asset is unlimited and me is creator of this asset
                                    unLimited = asset.isUnlimited(creator, false);

                                    if (!unLimited) {

                                        BigDecimal forSale = creator.getForSale(dcSet, absKey, height,
                                                false);

                                        if (amount.abs().compareTo(forSale) > 0) {
                                            return NO_BALANCE;
                                        }
                                    }
                                }

                                // TRY FEE
                                if ((flags & Transaction.NOT_VALIDATE_FLAG_FEE) == 0
                                        && !BlockChain.isFeeEnough(height, creator)
                                        && creator.getForFee(dcSet).compareTo(fee) < 0) {
                                    return NOT_ENOUGH_FEE;
                                }

                                break;

                            case ACTION_PLEDGE: // Учесть передачу в залог и возврат из залога

                                // пока отключим
                                if (true) {
                                    return INVALID_TRANSFER_TYPE;
                                }

                                if (asset.isOutsideType()) {
                                    return INVALID_TRANSFER_TYPE;
                                }

                                if (backward) {
                                    if (!asset.getMaker().equals(recipient))
                                        return INVALID_BACKWARD_ACTION;
                                } else {
                                    if (!asset.getMaker().equals(creator))
                                        return CREATOR_NOT_OWNER;
                                }

                                // if asset is unlimited and me is creator of this
                                // asset
                                unLimited = asset.isUnlimited(creator, false);

                                if (!unLimited) {

                                    BigDecimal forSale = creator.getForSale(dcSet, absKey, height,
                                            false);

                                    if (amount.abs().compareTo(forSale) > 0) {
                                        return NO_BALANCE;
                                    }
                                }

                                // TRY FEE
                                if ((flags & Transaction.NOT_VALIDATE_FLAG_FEE) == 0
                                        && !BlockChain.isFeeEnough(height, creator)
                                        && creator.getForFee(dcSet).compareTo(fee) < 0) {
                                    return NOT_ENOUGH_FEE;
                                }

                                break;

                            default:
                                return INVALID_TRANSFER_TYPE;
                        }

                        // IF send from PERSON to ANONYMOUS
                        if (!isValidPersonProtect(dcSet, height, recipient,
                                creatorIsPerson, absKey, actionType,
                                asset))
                            return RECEIVER_NOT_PERSONALIZED;
                    }
                }
            }

        } else {
            // TODO first org.erachain.records is BAD already ((
            // CHECK IF CREATOR HAS ENOUGH FEE MONEY
            if (height > BlockChain.ALL_BALANCES_OK_TO
                    && (flags & Transaction.NOT_VALIDATE_FLAG_FEE) == 0
                    && !BlockChain.isFeeEnough(height, creator)
                    && creator.getForFee(dcSet).compareTo(fee) < 0) {
                return NOT_ENOUGH_FEE;
            }

        }

        return VALIDATE_OK;
    }

    public int isValid(int forDeal, long flags) {

        if (height < BlockChain.ALL_VALID_BEFORE) {
            return VALIDATE_OK;
        }

        if (false) {
            for (byte[] valid_item : VALID_REC) {
                if (Arrays.equals(this.signature, valid_item)) {
                    return VALIDATE_OK;
                }
            }
        }

        int height = this.height > 0 ? this.height : this.getBlockHeightByParentOrLast(dcSet);
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

        // CHECK IF REFERENCE IS OK
        if (forDeal > Transaction.FOR_PACK) {
            if (BlockChain.CHECK_DOUBLE_SPEND_DEEP < 0) {
                /// вообще не проверяем в тесте
                if (BlockChain.TEST_DB == 0 && timestamp < Controller.getInstance().getBlockChain().getTimestamp(height - 1)) {
                    // тут нет проверок на двойную трату поэтому только в текущем блоке транзакции принимаем
                    if (true || BlockChain.CHECK_BUGS > 1)
                        LOGGER.debug(" diff sec: " + (Controller.getInstance().getBlockChain().getTimestamp(height) - timestamp) / 1000);
                    errorValue = "diff sec: " + (Controller.getInstance().getBlockChain().getTimestamp(height) - timestamp) / 1000;
                    return INVALID_TIMESTAMP;
                }
            } else if (BlockChain.CHECK_DOUBLE_SPEND_DEEP > 0) {
                if (timestamp < Controller.getInstance().getBlockChain().getTimestamp(height - BlockChain.CHECK_DOUBLE_SPEND_DEEP)) {
                    // тут нет проверок на двойную трату поэтому только в текущем блоке транзакции принимаем
                    if (BlockChain.CHECK_BUGS > 1)
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
                            if (BlockChain.CHECK_BUGS > 1)
                                LOGGER.debug("INVALID TIME!!! REFERENCE: " + viewCreator() + " " + DateTimeFormat.timestamptoString(reference[0])
                                        + "  TX[timestamp]: " + viewTimestamp() + " diff: " + (this.timestamp - reference[0])
                                        + " BLOCK time diff: " + (Controller.getInstance().getBlockChain().getTimestamp(height) - this.timestamp));
                        }
                    }
                    errorValue = "INVALID TIME!!! REFERENCE: " + viewCreator() + " " + DateTimeFormat.timestamptoString(reference[0])
                            + "  TX[timestamp]: " + viewTimestamp() + " diff: " + (this.timestamp - reference[0])
                            + " BLOCK time diff: " + (Controller.getInstance().getBlockChain().getTimestamp(height) - this.timestamp);
                    return INVALID_TIMESTAMP;
                }
            }
        }

        boolean isPerson = this.creator.isPerson(dcSet, height, creatorPersonDuration);

        // PUBLIC TEXT only from PERSONS
        if ((flags & Transaction.NOT_VALIDATE_FLAG_PUBLIC_TEXT) == 0
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

        if (height > BlockChain.FREE_FEE_FROM_HEIGHT && seqNo <= BlockChain.FREE_FEE_TO_SEQNO
                && getDataLength(Transaction.FOR_NETWORK, false) < BlockChain.FREE_FEE_LENGTH) {
            // не учитываем комиссию если размер блока маленький
            flags = flags | NOT_VALIDATE_FLAG_FEE;
        }


        // CHECK IF AMOUNT AND ASSET
        if ((flags & NOT_VALIDATE_FLAG_BALANCE) == 0L
                && this.amount != null) {
            int result = isValidAction(dcSet, height, creator, signature, key, asset, amount, recipient,
                    isBackward(), fee, assetFee, isPerson, flags);
            if (result != VALIDATE_OK)
                return result;

        } else {
            // TODO first org.erachain.records is BAD already ((
            // CHECK IF CREATOR HAS ENOUGH FEE MONEY
            if (height > BlockChain.ALL_BALANCES_OK_TO
                    && (flags & Transaction.NOT_VALIDATE_FLAG_FEE) == 0
                    && !BlockChain.isFeeEnough(height, creator)
                    && this.creator.getForFee(dcSet).compareTo(this.fee) < 0) {
                return NOT_ENOUGH_FEE;
            }

        }

        // так как мы не лезем в супер класс то тут проверим тоже ее
        if (false && // теперь не проверяем так как ключ сделал длинный dbs.rocksDB.TransactionFinalSignsSuitRocksDB.KEY_LEN
                (flags & NOT_VALIDATE_KEY_COLLISION) == 0l
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

    @Override
    public void process(Block block, int forDeal) {

        super.process(block, forDeal);

        if (this.amount == null)
            return;

        DCSet db = this.dcSet;

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

        if (absKey == Transaction.RIGHTS_KEY && block != null) {
            block.addForgingInfoUpdate(this.recipient);
        }

        if (assetFee != null && assetFee.signum() != 0) {
            // учтем что он еще заплатил коэффициент с суммы
            this.creator.changeBalance(db, !backward, backward, absKey, this.assetFee, false, false, !incomeReverse);
            if (block != null) {
                block.addCalculated(this.creator, absKey,
                        this.assetFee.negate(), "Asset Fee", this.dbRef);
            }
        }
    }

    @Override
    public void orphan(Block block, int forDeal) {

        super.orphan(block, forDeal);

        if (this.amount == null)
            return;

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

        if (absKey == Transaction.RIGHTS_KEY && block != null) {
            block.addForgingInfoUpdate(this.recipient);
        }

        if (assetFee != null && assetFee.signum() != 0) {
            this.creator.changeBalance(dcSet, backward, backward, absKey, this.assetFee, false, false, !incomeReverse);
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
    
    // public abstract Map<String, Map<Long, BigDecimal>> getAssetAmount();

}
