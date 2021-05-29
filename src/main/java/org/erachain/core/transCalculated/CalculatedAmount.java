package org.erachain.core.transCalculated;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Longs;
import org.erachain.core.BlockChain;
import org.erachain.core.account.Account;
import org.erachain.core.crypto.Base58;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.transaction.Transaction;
import org.erachain.core.transaction.TransactionAmount;
import org.erachain.utils.NumberAsString;
import org.json.simple.JSONObject;
import org.mapdb.Fun.Tuple3;

import java.math.BigDecimal;
import java.util.HashSet;

/*

вычисляемая трнзакция по изменению объеиов

 */

public abstract class CalculatedAmount extends Calculated {

    protected Account sender;
    protected Account recipient;
    protected BigDecimal amount;
    protected long assetKey;
    protected AssetCls asset;

    static boolean isDirect = false;

    protected CalculatedAmount(byte[] typeBytes, String type_name, Integer blockNo, Integer transNo, long seq,
                               Account sender, Account recipient, long assetKey, BigDecimal amount) {
        super(typeBytes, type_name, blockNo, transNo, seq);
        this.sender = sender;
        this.recipient = recipient;

        this.amount = amount;
        this.assetKey = assetKey;
    }
        
    // GETTERS/SETTERS
    
    public Account getRecipient() {
        return this.recipient;
    }
        
    @Override
    public long getAssetKey() {
        return this.assetKey;
    }
    
    @Override
    public long getAbsKey() {
        if (this.assetKey < 0)
            return -this.assetKey;
        return this.assetKey;
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
    
    public String getStr() {
        return "calcsAmount";
    }
        
    @Override
    public BigDecimal getAmount(String address) {
        BigDecimal amount = BigDecimal.ZERO;
        
        if (this.amount != null) {
            
            if (address.equals(this.sender.getAddress())) {
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
    
    public int getActionType() {
        int type = Account.balancePosition(this.assetKey, this.amount, isBackward(), isDirect);
        return type * (isBackward() ? -1 : 1);
    }
    
    // BACKWARD AMOUNT
    public boolean isBackward() {
        return typeBytes[1] == 1 || typeBytes[1] > 1 && (typeBytes[2] & TransactionAmount.BACKWARD_MASK) > 0;
    }
    
    /*
     * ************** VIEW
     */
    
    @Override
    public String viewTypeName() {
        if (this.amount == null || this.amount.signum() == 0)
            return "LETTER";
        
        if (this.isBackward()) {
            return "backward";
        } else {
            return "SEND";
        }
    }
    
    @Override
    public String viewSubTypeName() {
        return TransactionAmount.viewSubTypeName(this.assetKey, this.amount, this.isBackward(), isDirect);
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
    public String viewSender() {
        return sender.getPersonAsString();
    }

    @Override
    public String viewRecipient() {
        return recipient.getPersonAsString();
    }
    
    // PARSE/CONVERT
    // @Override
    @Override
    public byte[] toBytes() {
        
        byte[] data = super.toBytes();

        // WRITE SENDER
        data = Bytes.concat(data, Base58.decode(this.sender.getAddress()));

        // WRITE RECIPIENT
        data = Bytes.concat(data, Base58.decode(this.recipient.getAddress()));
        
        // WRITE ASSET KEY
        byte[] keyBytes = Longs.toByteArray(this.assetKey);
        keyBytes = Bytes.ensureCapacity(keyBytes, TransactionAmount.KEY_LENGTH, 0);
        data = Bytes.concat(data, keyBytes);
        
        // WRITE ACCURACY of AMMOUNT
        int different_scale = this.amount.scale() - BlockChain.AMOUNT_DEDAULT_SCALE;
        BigDecimal amountBase;
        if (different_scale != 0) {
            // RESCALE AMOUNT
            amountBase = this.amount.scaleByPowerOfTen(different_scale);
            if (different_scale < 0)
                different_scale += TransactionAmount.SCALE_MASK + 1;
            
            data[3] = (byte) (data[3] | different_scale);
        } else {
            amountBase = this.amount;
        }
        
        // WRITE AMOUNT
        byte[] amountBytes = Longs.toByteArray(amountBase.unscaledValue().longValue());
        amountBytes = Bytes.ensureCapacity(amountBytes, TransactionAmount.AMOUNT_LENGTH, 0);
        data = Bytes.concat(data, amountBytes);
        
        return data;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    protected JSONObject getJsonBase() {
        JSONObject transaction = super.getJsonBase();
        
        transaction.put("recipient", this.recipient.getAddress());
        if (this.amount != null) {
            transaction.put("asset", this.getAbsKey());
            transaction.put("amount", this.viewAmount());
            // transaction.put("action_type", this.viewActionType());
            transaction.put("action_key", this.getActionType());
            transaction.put("action_name", TransactionAmount.viewSubTypeName(this.assetKey, this.amount, this.isBackward(), isDirect));
            transaction.put("backward", this.isBackward());
        }
        
        return transaction;
    }
    
    @Override
    public HashSet<Account> getInvolvedAccounts() {
        HashSet<Account> accounts = new HashSet<Account>();
        accounts.add(this.sender);
        accounts.addAll(this.getRecipientAccounts());
        return accounts;
    }
    
    @Override
    public HashSet<Account> getRecipientAccounts() {
        HashSet<Account> accounts = new HashSet<Account>();
        accounts.add(this.recipient);
        return accounts;
    }
    
    @Override
    public boolean isInvolved(Account account) {
        String address = account.getAddress();
        
        if (address.equals(sender.getAddress()) || address.equals(recipient.getAddress())) {
            return true;
        }
        
        return false;
    }
    
    @Override
    public int getDataLength() {
        // IF VERSION 1 (amount = null)
        return BASE_LENGTH + TransactionAmount.KEY_LENGTH + TransactionAmount.AMOUNT_LENGTH;
    }
        
    public void process() {

        if (this.amount == null)
            return;

        int amount_sign = this.amount.compareTo(BigDecimal.ZERO);
        if (amount_sign == 0)
            return;

        // BACKWARD - CONFISCATE
        boolean backward = this.isBackward();

        long absKey = getAbsKey();
        int actionType = Account.balancePosition(this.assetKey, amount, backward, isDirect);
        boolean incomeReverse = actionType == TransactionAmount.ACTION_HOLD;

        // ASSET ACTIONS PROCESS
        if (this.asset.isOutsideType()) {
            if (actionType == TransactionAmount.ACTION_SEND && backward) {
                // UPDATE SENDER
                this.sender.changeBalance(dcSet, true, false, this.assetKey, this.amount, false, false, true, false);

                // UPDATE RECIPIENT
                this.recipient.changeBalance(dcSet, false, false, this.assetKey, this.amount, false, false, true, false);

                // CLOSE IN CLAIN - back amount to claim ISSUER
                this.sender.changeBalance(dcSet, false, false, -absKey, this.amount, false, false, true, false);
                this.recipient.changeBalance(dcSet, true, false, -absKey, this.amount, false, false, true, false);

                // CLOSE IN CLAIM table balance
                Tuple3<String, Long, String> creditKey = new Tuple3<String, Long, String>(this.sender.getAddress(),
                        absKey, this.recipient.getAddress());
                dcSet.getCredit_AddressesMap().sub(creditKey, this.amount);
            } else {
                // UPDATE SENDER
                this.sender.changeBalance(dcSet, !backward, false, this.assetKey, this.amount, false, false, false, false);
                
                // UPDATE RECIPIENT
                this.recipient.changeBalance(dcSet, backward, false, this.assetKey, this.amount, false, false, false, false);
                
            }
            
        } else {
            // STANDART ACTION PROCESS
            if (false && actionType == TransactionAmount.ACTION_DEBT) {
                if (backward) {
                    // UPDATE CREDITOR
                    this.sender.changeBalance(dcSet, !backward, false, this.assetKey, this.amount, false, false, true, false);
                    // UPDATE DEBTOR
                    this.recipient.changeBalance(dcSet, backward, false, this.assetKey, this.amount, false, false, false, false);
                } else {
                    // UPDATE CREDITOR
                    this.sender.changeBalance(dcSet, !backward, false, this.assetKey, this.amount, false, false, true, false);
                    // UPDATE DEBTOR
                    this.recipient.changeBalance(dcSet, backward, false, this.assetKey, this.amount, false, false, false, false);
                }
                
            } else {
                // UPDATE SENDER
                if (absKey == 666l) {
                    this.sender.changeBalance(dcSet, backward, false, this.assetKey, this.amount, false, false, !incomeReverse, false);
                } else {
                    this.sender.changeBalance(dcSet, !backward, false, this.assetKey, this.amount, false, false, !incomeReverse, false);
                }
                // UPDATE RECIPIENT
                this.recipient.changeBalance(dcSet, backward, false, this.assetKey, this.amount, false, false, incomeReverse, false);
            }
        }
        
        if (actionType == TransactionAmount.ACTION_DEBT) {
            if (backward) {
                // BORROW
                Tuple3<String, Long, String> creditKey = new Tuple3<String, Long, String>(this.sender.getAddress(),
                        absKey, this.recipient.getAddress());
                dcSet.getCredit_AddressesMap().sub(creditKey, this.amount);
            } else {
                // CREDIR or RETURN CREDIT
                Tuple3<String, Long, String> creditKey = new Tuple3<String, Long, String>(this.recipient.getAddress(),
                        absKey, this.sender.getAddress());
                BigDecimal creditAmount = dcSet.getCredit_AddressesMap().get(creditKey);
                if (creditAmount.compareTo(amount) >= 0) {
                    // ALL CREDIT RETURN
                    dcSet.getCredit_AddressesMap().sub(creditKey, this.amount);
                } else {
                    // update creditAmount to 0
                    BigDecimal leftAmount;
                    if (creditAmount.signum() != 0) {
                        dcSet.getCredit_AddressesMap().sub(creditKey, creditAmount);
                        // GET CREDIT for left AMOUNT
                        leftAmount = amount.subtract(creditAmount);
                    } else {
                        leftAmount = amount;
                    }
                    
                    Tuple3<String, Long, String> leftCreditKey = new Tuple3<String, Long, String>(
                            this.sender.getAddress(), absKey, this.recipient.getAddress()); // REVERSE
                    dcSet.getCredit_AddressesMap().add(leftCreditKey, leftAmount);
                }
            }
        }
                
        if (absKey == Transaction.RIGHTS_KEY) {
            // update last forging block if it not exist
            // if exist - it not need - incomes will be negate from forging
            // balance
            // get height by LAST block in CHAIN + 2 - skip incoming BLOCK
            
            Tuple3<Integer, Integer, Integer> privousForgingPoint = this.recipient.getLastForgingData(dcSet);
            int currentForgingBalance = recipient.getBalanceUSE(Transaction.RIGHTS_KEY, dcSet).intValue();
            if (privousForgingPoint == null || privousForgingPoint.a == this.blockNo) {
                if (currentForgingBalance >= BlockChain.MIN_GENERATING_BALANCE) {
                    this.recipient.setForgingData(dcSet, this.blockNo, currentForgingBalance);
                }
            } else {
                if (privousForgingPoint.b < BlockChain.MIN_GENERATING_BALANCE
                        && currentForgingBalance >= BlockChain.MIN_GENERATING_BALANCE) {
                    this.recipient.setForgingData(dcSet, this.blockNo, currentForgingBalance);
                }
            }
        }
    }
    
    @Override
    public void orphan() {

        if (this.amount == null)
            return;

        //DCSet db = this.dcSet;

        int amount_sign = this.amount.compareTo(BigDecimal.ZERO);
        if (amount_sign == 0)
            return;

        // BACKWARD - CONFISCATE
        boolean backward = this.isBackward();

        long absKey = getAbsKey();
        int actionType = Account.balancePosition(this.assetKey, amount, backward, isDirect);
        boolean incomeReverse = actionType == TransactionAmount.ACTION_HOLD;

        // ASSET TYPE ORPHAN
        if (this.asset.isOutsideType()) {
            if (actionType == TransactionAmount.ACTION_SEND && backward) {
                // UPDATE SENDER
                this.sender.changeBalance(dcSet, false, false, this.assetKey, this.amount, false, false, true, false);

                // UPDATE RECIPIENT
                this.recipient.changeBalance(dcSet, true, false, this.assetKey, this.amount, false, false, true, false);

                this.sender.changeBalance(dcSet, true, false, -absKey, this.amount, false, false, true, false);
                this.recipient.changeBalance(dcSet, false, false, -absKey, this.amount, false, false, true, false);

                Tuple3<String, Long, String> creditKey = new Tuple3<String, Long, String>(this.sender.getAddress(),
                        absKey, this.recipient.getAddress());
                dcSet.getCredit_AddressesMap().add(creditKey, this.amount);
            } else {
                // UPDATE SENDER
                this.sender.changeBalance(dcSet, backward, false, this.assetKey, this.amount, false, false, false, false);
                
                // UPDATE RECIPIENT
                this.recipient.changeBalance(dcSet, !backward, false, this.assetKey, this.amount, false, false, false, false);
                
            }
            
        } else {
            
            // STANDART ACTION ORPHAN
            if (false && actionType == TransactionAmount.ACTION_DEBT) {
                if (backward) {
                    // UPDATE CREDITOR
                    this.sender.changeBalance(dcSet, backward, false, this.assetKey, this.amount, false, false, true, false);
                    // UPDATE DEBTOR
                    this.recipient.changeBalance(dcSet, !backward, false, this.assetKey, this.amount, false, false, false, false);
                } else {
                    // UPDATE CREDITOR
                    this.sender.changeBalance(dcSet, backward, false, this.assetKey, this.amount, false, false, true, false);
                    // UPDATE DEBTOR
                    this.recipient.changeBalance(dcSet, !backward, false, this.assetKey, this.amount, false, false, false, false);
                }
                
            } else {
                
                // UPDATE SENDER
                if (absKey == 666l) {
                    this.sender.changeBalance(dcSet, !backward, false, this.assetKey, this.amount, false, false, !incomeReverse, false);
                } else {
                    this.sender.changeBalance(dcSet, backward, false, this.assetKey, this.amount, false, false, !incomeReverse, false);
                }
                // UPDATE RECIPIENT
                this.recipient.changeBalance(dcSet, !backward, false, this.assetKey, this.amount, false, false, incomeReverse, false);
                
            }
        }
        
        if (actionType == TransactionAmount.ACTION_DEBT) {
            if (backward) {
                // BORROW
                Tuple3<String, Long, String> creditKey = new Tuple3<String, Long, String>(this.sender.getAddress(),
                        absKey, this.recipient.getAddress());
                dcSet.getCredit_AddressesMap().add(creditKey, this.amount);
            } else {
                // in BACK order - RETURN CREDIT << CREDIT
                // GET CREDIT for left AMOUNT
                Tuple3<String, Long, String> leftCreditKey = new Tuple3<String, Long, String>(this.sender.getAddress(),
                        absKey, this.recipient.getAddress()); // REVERSE
                BigDecimal leftAmount = dcSet.getCredit_AddressesMap().get(leftCreditKey);
                if (leftAmount.compareTo(amount) < 0) {
                    dcSet.getCredit_AddressesMap().sub(leftCreditKey, leftAmount);
                    // CREDIR or RETURN CREDIT
                    Tuple3<String, Long, String> creditKey = new Tuple3<String, Long, String>(
                            this.recipient.getAddress(), absKey, this.sender.getAddress());
                    dcSet.getCredit_AddressesMap().add(creditKey, amount.subtract(leftAmount));
                } else {
                    // ONLY RETURN CREDIT
                    dcSet.getCredit_AddressesMap().sub(leftCreditKey, amount);
                }
                
            }
        }
        
        if (absKey == Transaction.RIGHTS_KEY) {
            Tuple3<Integer, Integer, Integer> lastForgingPoint = this.recipient.getLastForgingData(dcSet);
            if (lastForgingPoint != null && lastForgingPoint.a == this.blockNo) {
                this.recipient.delForgingData(dcSet, this.blockNo);
            }
        }
    }    
    
}
