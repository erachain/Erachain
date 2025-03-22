package org.erachain.core.transaction;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Longs;
import org.erachain.core.BlockChain;
import org.erachain.core.account.Account;
import org.erachain.core.account.PrivateKeyAccount;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.block.Block;
import org.erachain.core.exdata.exLink.ExLink;
import org.erachain.core.item.ItemCls;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;

public abstract class IssueItemRecord extends Transaction implements Itemable {

    static Logger LOGGER = LoggerFactory.getLogger(IssueItemRecord.class.getName());

    protected ItemCls item;
    protected Long key = 0L;

    public IssueItemRecord(byte[] typeBytes, String TYPE_NAME, PublicKeyAccount creator, ExLink linkTo, ItemCls item, byte feePow, long timestamp, long flags) {
        super(typeBytes, TYPE_NAME, creator, linkTo, null, feePow, timestamp, flags);
        this.item = item;
        if (item.getKey() != 0)
            key = item.getKey();
    }

    public IssueItemRecord(byte[] typeBytes, String TYPE_NAME, PublicKeyAccount creator, ExLink linkTo, ItemCls item, byte feePow, long timestamp, long flags, byte[] signature) {
        this(typeBytes, TYPE_NAME, creator, linkTo, item, feePow, timestamp, flags);
        this.signature = signature;
        this.item.setReference(signature, dbRef);
    }

    public IssueItemRecord(byte[] typeBytes, String TYPE_NAME, PublicKeyAccount creator, ExLink linkTo, ItemCls item, byte[] signature) {
        this(typeBytes, TYPE_NAME, creator, linkTo, item, (byte) 0, 0L, 0L);
        this.signature = signature;
        this.item.setReference(signature, dbRef);
    }

    //GETTERS/SETTERS

    @Override
    public ItemCls getItem() {
        return this.item;
    }

    /** нужно для отображение в блокэксплорере
     *  - не участвует в Протоколе, так как перед выпуском неизвестно его значение
     * @return
     */
    @Override
    public long getKey() {
        return key == null ? (isWiped() ? 0 : null) // выдаст ошибку специально если боевая и NULL, see issues/1347
                : key;
    }

    @Override
    public String getTitle() {
        return this.item.getName();
    }

    @Override
    public void makeItemsKeys() {
        if (isWiped()) {
            itemsKeys = new Object[][]{};
        }

        if (key == null || key == 0)
            return;

        if (creatorPersonDuration != null) {
            // запомним что тут две сущности
            itemsKeys = new Object[][]{
                    new Object[]{ItemCls.PERSON_TYPE, creatorPersonDuration.a, creatorPerson.getTags()},
                    new Object[]{item.getItemType(), key, item.getTags()}
            };
        } else {
            itemsKeys = new Object[][]{
                    new Object[]{item.getItemType(), key, item.getTags()}
            };
        }
    }

    @Override
    public boolean isFreeFee() {
        return false;
    }

    @Override
    public long calcBaseFee(boolean withFreeProtocol) {

        int len = getFeeLength();

        if (this.height > BlockChain.USE_NEW_ISSUE_FEE) {
            if (len < BlockChain.MINIMAL_ISSUE_FEE)
                len = BlockChain.MINIMAL_ISSUE_FEE;
        }

        return len * BlockChain.FEE_PER_BYTE;
    }

    /**
     * нельзя вызывать для Форка и для isWIPED
     */
    @Override
    public void updateFromStateDB() {

        super.updateFromStateDB();

        if (this.dbRef == 0) {
            // неподтвержденная транзакция не может быть обновлена
            return;
        }

        if (key == null || key == 0) {
            // эта транзакция взята как скелет из набора блока
            // найдем сохраненную транзакцию - в ней есь Номер Сути
            IssueItemRecord issueItemRecord = (IssueItemRecord) dcSet.getTransactionFinalMap().get(this.dbRef);
            if (issueItemRecord == null) {
                // это может быть при откате - транзакция уже удалена и пытается в кошельке ей мясо нарастить
                return;
            }
            key = issueItemRecord.getKey();
            item.setKey(key);
        } else if (item.getKey() == 0) {
            item.setKey(key);
        }
    }

    @Override
    public String viewItemName() {
        return item.toString();
    }

    @Override
    public boolean hasPublicText() {
        return true;
    }

    //@Override
    @Override
    public void sign(PrivateKeyAccount creator, int forDeal) {
        super.sign(creator, forDeal);
        item.setReference(signature, dbRef);
    }

    //PARSE CONVERT


    @SuppressWarnings("unchecked")
    @Override
    public JSONObject toJson() {

        //GET BASE
        JSONObject transaction = this.getJsonBase();

        transaction.put("item", this.item.toJson());

        return transaction;
    }

    @Override
    public byte[] toBytes(int forDeal, boolean withSignature) {
        byte[] data = super.toBytes(forDeal, withSignature);

        // without reference
        data = Bytes.concat(data, this.item.toBytes(forDeal, false, false));

        if (forDeal == FOR_DB_RECORD) {
            if (key == null) {
                // для неподтвержденных когда еще номера нету
                data = Bytes.concat(data, new byte[KEY_LENGTH]);
            } else {
                byte[] keyBytes = Longs.toByteArray(key);
                keyBytes = Bytes.ensureCapacity(keyBytes, KEY_LENGTH, 0);
                data = Bytes.concat(data, keyBytes);
            }

        }

        return data;
    }

    @Override
    public int getDataLength(int forDeal, boolean withSignature) {

        int base_len;

        base_len = super.getDataLength(forDeal, withSignature);
        if (forDeal == FOR_DB_RECORD)
            base_len += KEY_LENGTH;

        // not include item reference
        return base_len + this.item.getDataLength(false);

    }

    //VALIDATE

    //@Override
    @Override
    public int isValid(int forDeal, long checkFlags) {

        if (height < BlockChain.ALL_VALID_BEFORE) {
            return VALIDATE_OK;
        }

        if (BlockChain.startKeys[this.item.getItemType()] < 0) {
            if (this.item.isNovaItem(this.dcSet) <= 0) {
                return INVALID_ISSUE_PROHIBITED;
            }
        }

        int result = item.isValid();
        if (result != Transaction.VALIDATE_OK) {
            errorValue = item.errorValue;
            return result;
        }

        //CHECK NAME LENGTH
        String name = this.item.getName();
        // TEST ONLY CHARS
        int nameLen = name.length();

        if (nameLen < item.getMinNameLen()) {
            // IF is NEW NOVA
            if (this.item.isNovaItem(this.dcSet) <= 0) {
                errorValue = "So short: " + nameLen;
                return INVALID_NAME_LENGTH_MIN;
            }
        }

        return super.isValid(forDeal, checkFlags);

    }

    //PROCESS/ORPHAN

    protected void processItem() {

        this.item.setReference(this.signature, dbRef);

        //INSERT INTO DATABASE
        key = this.item.insertToMap(this.dcSet, this.item.getStartKey());

        if (creatorPerson != null) {
            dcSet.getItemsValuesMap().putIssuedItem(creatorPerson, item, dbRef);
        }

    }

    @Override
    public void processBody(Block block, int forDeal) {

        //UPDATE CREATOR
        super.processBody(block, forDeal);

        processItem();

    }

    protected void orphanItem() {
        //DELETE FROM DATABASE
        key = this.item.deleteFromMap(this.dcSet, item.getStartKey());

        if (creatorPerson != null) {
            dcSet.getItemsValuesMap().deleteIssuedItem(creatorPerson, item);
        }
    }

    @Override
    public void orphanBody(Block block, int forDeal) {
        //UPDATE CREATOR
        super.orphanBody(block, forDeal);

        orphanItem();

    }

    @Override
    public HashSet<Account> getInvolvedAccounts() {
        HashSet<Account> accounts = this.getRecipientAccounts();
        accounts.add(this.creator);
        return accounts;
    }

    @Override
    public HashSet<Account> getRecipientAccounts() {
        HashSet<Account> accounts = new HashSet<>(3, 1);
        if (!this.item.getMaker().equals(this.creator)) {
            accounts.add(this.item.getMaker());
        }
        return accounts;
    }

    @Override
    public boolean isInvolved(Account account) {

        if (account.equals(this.creator) || account.equals(this.item.getMaker())) {
            return true;
        }

        return false;
    }

}
