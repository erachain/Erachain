package org.erachain.core.transaction;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.core.account.Account;
import org.erachain.core.account.PrivateKeyAccount;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.block.Block;
import org.erachain.core.block.GenesisBlock;
import org.erachain.core.crypto.Base58;
import org.erachain.core.crypto.Crypto;
import org.erachain.core.item.ItemCls;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.item.persons.PersonCls;
import org.erachain.datachain.DCSet;
import org.json.simple.JSONObject;
import org.mapdb.Fun;
import org.mapdb.Fun.Tuple3;
import org.mapdb.Fun.Tuple4;

import java.math.BigDecimal;
import java.util.*;


// if person has not ALIVE status - add it
// end_day = this.add_day + this.timestanp(days)
// typeBytes[1] - version =0 - not need sign by person;
// 		 =1 - need sign by person
// typeBytes[2] - size of personalized accounts
public class RSertifyPubKeys extends Transaction implements Itemable {

    protected static final BigDecimal BONUS_FOR_PERSON_4_11 = BigDecimal.valueOf(1000 * BlockChain.FEE_PER_BYTE, BlockChain.FEE_SCALE); // need SCALE for .unscaled()

    public static final int DEFAULT_DURATION = 365 * 3; // 3 years
    private static final byte TYPE_ID = (byte) Transaction.CERTIFY_PUB_KEYS_TRANSACTION;
    private static final String NAME_ID = "Certify Person";
    private static final int USER_ADDRESS_LENGTH = Transaction.CREATOR_LENGTH;
    private static final int DATE_DAY_LENGTH = 4; // one year + 256 days max

    private static final int LOAD_LENGTH = DATE_DAY_LENGTH + KEY_LENGTH;
    protected static final int BASE_LENGTH_AS_MYPACK = Transaction.BASE_LENGTH_AS_MYPACK + LOAD_LENGTH;
    protected static final int BASE_LENGTH_AS_PACK = Transaction.BASE_LENGTH_AS_PACK + LOAD_LENGTH;
    protected static final int BASE_LENGTH = Transaction.BASE_LENGTH + LOAD_LENGTH;
    protected static final int BASE_LENGTH_AS_DBRECORD = Transaction.BASE_LENGTH_AS_DBRECORD + LOAD_LENGTH;

    protected Long key; // PERSON KEY
    protected PersonCls person;
    protected Integer add_day; // in days
    protected List<PublicKeyAccount> sertifiedPublicKeys;
    protected List<byte[]> sertifiedSignatures;

    public RSertifyPubKeys(byte[] typeBytes, PublicKeyAccount creator, byte feePow, long key,
                           List<PublicKeyAccount> sertifiedPublicKeys,
                           int add_day, long timestamp, Long reference) {
        super(typeBytes, NAME_ID, creator, feePow, timestamp, reference);

        this.key = key;
        this.sertifiedPublicKeys = sertifiedPublicKeys;
        this.add_day = add_day;
    }

    public RSertifyPubKeys(int version, PublicKeyAccount creator, byte feePow, long key,
                           List<PublicKeyAccount> sertifiedPublicKeys,
                           int add_day, long timestamp, Long reference) {
        this(new byte[]{TYPE_ID, (byte) version, (byte) sertifiedPublicKeys.size(), 0}, creator, feePow, key,
                sertifiedPublicKeys,
                add_day, timestamp, reference);
    }

    // set default date
    public RSertifyPubKeys(int version, PublicKeyAccount creator, byte feePow, long key,
                           List<PublicKeyAccount> sertifiedPublicKeys,
                           long timestamp, Long reference) {
        this(new byte[]{TYPE_ID, (byte) version, (byte) sertifiedPublicKeys.size(), 0}, creator, feePow, key,
                sertifiedPublicKeys,
                0, timestamp, reference);
    }

    public RSertifyPubKeys(byte[] typeBytes, PublicKeyAccount creator, byte feePow, long key,
                           List<PublicKeyAccount> sertifiedPublicKeys,
                           int add_day, long timestamp, Long reference, byte[] signature,
                           List<byte[]> sertifiedSignatures) {
        this(typeBytes, creator, feePow, key,
                sertifiedPublicKeys,
                add_day, timestamp, reference);
        this.signature = signature;
        this.sertifiedSignatures = sertifiedSignatures;
    }
    public RSertifyPubKeys(byte[] typeBytes, PublicKeyAccount creator, byte feePow, long key,
                           List<PublicKeyAccount> sertifiedPublicKeys,
                           int add_day, long timestamp, Long reference, byte[] signature, long feeLong,
                           List<byte[]> sertifiedSignatures) {
        this(typeBytes, creator, feePow, key,
                sertifiedPublicKeys,
                add_day, timestamp, reference);
        this.signature = signature;
        this.sertifiedSignatures = sertifiedSignatures;
        this.fee = BigDecimal.valueOf(feeLong, BlockChain.AMOUNT_DEDAULT_SCALE);
    }


    // as pack
    public RSertifyPubKeys(byte[] typeBytes, PublicKeyAccount creator, long key,
                           List<PublicKeyAccount> sertifiedPublicKeys,
                           int add_day, byte[] signature,
                           List<byte[]> sertifiedSignatures) {
        this(typeBytes, creator, (byte) 0, key,
                sertifiedPublicKeys,
                add_day, 0l, null);
        this.signature = signature;
        this.sertifiedSignatures = sertifiedSignatures;
    }

    public RSertifyPubKeys(int version, PublicKeyAccount creator, byte feePow, long key,
                           List<PublicKeyAccount> sertifiedPublicKeys,
                           int add_day, long timestamp, Long reference, byte[] signature,
                           byte[] userSignature1, byte[] userSignature2, byte[] userSignature3) {
        this(new byte[]{TYPE_ID, (byte) version, (byte) sertifiedPublicKeys.size(), 0}, creator, feePow, key,
                sertifiedPublicKeys,
                add_day, timestamp, reference);
    }

    // as pack
    public RSertifyPubKeys(int version, PublicKeyAccount creator, long key,
                           List<PublicKeyAccount> sertifiedPublicKeys,
                           int add_day, byte[] signature,
                           byte[] userSignature1, byte[] userSignature2, byte[] userSignature3) {
        this(new byte[]{TYPE_ID, (byte) version, (byte) sertifiedPublicKeys.size(), 0}, creator, (byte) 0, key,
                sertifiedPublicKeys,
                add_day, 0l, null);
    }

    //GETTERS/SETTERS

    @Override
    public ItemCls getItem()
    {
        if (person == null) {
            person = (PersonCls) dcSet.getItemPersonMap().get(key);
        }
        return this.person;
    }

    public static int getPublicKeysSize(byte[] typeBytes) {
        return typeBytes[2];
    }

    // releaserReference = null - not a pack
    // releaserReference = reference for releaser account - it is as pack
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
        byte[] signature = Arrays.copyOfRange(data, position, position + SIGNATURE_LENGTH);
        position += SIGNATURE_LENGTH;

        long feeLong = 0;
        if (asDeal == FOR_DB_RECORD) {
            // READ FEE
            byte[] feeBytes = Arrays.copyOfRange(data, position, position + FEE_LENGTH);
            feeLong = Longs.fromByteArray(feeBytes);
            position += FEE_LENGTH;
        }

        //READ PERSON KEY
        byte[] keyBytes = Arrays.copyOfRange(data, position, position + KEY_LENGTH);
        long key = Longs.fromByteArray(keyBytes);
        position += KEY_LENGTH;

        //byte[] item;
        List<PublicKeyAccount> sertifiedPublicKeys = new ArrayList<PublicKeyAccount>();
        List<byte[]> sertifiedSignatures = new ArrayList<byte[]>();
        for (int i = 0; i < getPublicKeysSize(typeBytes); i++) {
            //READ USER ACCOUNT
            sertifiedPublicKeys.add(new PublicKeyAccount(Arrays.copyOfRange(data, position, position + USER_ADDRESS_LENGTH)));
            position += USER_ADDRESS_LENGTH;

            if (getVersion(typeBytes) == 1) {
                //READ USER SIGNATURE
                sertifiedSignatures.add(Arrays.copyOfRange(data, position, position + SIGNATURE_LENGTH));
                position += SIGNATURE_LENGTH;
            }
        }

        // READ DURATION
        int add_day = Ints.fromByteArray(Arrays.copyOfRange(data, position, position + DATE_DAY_LENGTH));
        position += DATE_DAY_LENGTH;

        if (asDeal > Transaction.FOR_MYPACK) {
            return new RSertifyPubKeys(typeBytes, creator, feePow, key,
                    sertifiedPublicKeys,
                    add_day, timestamp, reference, signature, feeLong,
                    sertifiedSignatures);
        } else {
            return new RSertifyPubKeys(typeBytes, creator, key,
                    sertifiedPublicKeys,
                    add_day, signature,
                    sertifiedSignatures);
        }

    }

    // PERSON KEY
    @Override
    public long getKey() {
        return this.key;
    }

    @Override
    public List<PublicKeyAccount> getPublicKeys() {
        return this.sertifiedPublicKeys;
    }

    public List<PublicKeyAccount> getSertifiedPublicKeys() {
        return this.sertifiedPublicKeys;
    }

    public List<String> getSertifiedPublicKeysB58() {
        List<String> pbKeys = new ArrayList<String>();
        for (PublicKeyAccount key : this.sertifiedPublicKeys) {
            pbKeys.add(Base58.encode(key.getPublicKey()));
        }
        ;
        return pbKeys;
    }

    public List<byte[]> getSertifiedSignatures() {
        return this.sertifiedSignatures;
    }

    public List<String> getSertifiedSignaturesB58() {
        List<String> items = new ArrayList<String>();
        for (byte[] item : this.sertifiedSignatures) {
            items.add(Base58.encode(item));
        }
        ;
        return items;
    }

    @Override
    public List<byte[]> getOtherSignatures() {
        return sertifiedSignatures;
    }

    public int getAddDay() {
        return this.add_day;
    }

    public int getPublicKeysSize() {
        return this.typeBytes[2];
    }

    // IT is only PERSONALITY record
    @Override
    public boolean hasPublicText() {
        return !BlockChain.ANONIM_SERT_USE && !BlockChain.DEVELOP_USE;
    }

    //////// VIEWS
    @Override
    public String viewAmount(String address) {
        return add_day > 0 ? "+" + add_day : "" + add_day;
    }

    @Override
    public String viewRecipient() {
        return Base58.encode(this.sertifiedPublicKeys.get(0).getPublicKey());
    }

    //////////////
    @SuppressWarnings("unchecked")
    @Override
    public JSONObject toJson() {
        //GET BASE
        JSONObject transaction = this.getJsonBase();

        //ADD CREATOR/SERVICE/DATA
        transaction.put("key", this.key);
        //List<String> pbKeys = new ArrayList<String>();
        transaction.put("sertified_public_keys", this.getSertifiedPublicKeysB58());
        transaction.put("sertified_signatures", this.getSertifiedSignaturesB58());
        transaction.put("add_day", this.add_day);

        return transaction;
    }

    public void signUserAccounts(List<PrivateKeyAccount> userPrivateAccounts) {
        byte[] data;
        // use this.reference in any case
        data = this.toBytes(FOR_NETWORK, false);
        if (data == null) return;

        // all test a not valid for main test
        // all other network must be invalid here!
        int port = Controller.getInstance().getNetworkPort();
        data = Bytes.concat(data, Ints.toByteArray(port));

        if (this.sertifiedSignatures == null) this.sertifiedSignatures = new ArrayList<byte[]>();

        byte[] publicKey;
        for (PublicKeyAccount publicAccount : this.sertifiedPublicKeys) {
            for (PrivateKeyAccount privateAccount : userPrivateAccounts) {
                publicKey = privateAccount.getPublicKey();
                if (Arrays.equals((publicKey), publicAccount.getPublicKey())) {
                    this.sertifiedSignatures.add(Crypto.getInstance().sign(privateAccount, data));
                    break;
                }
            }
        }
    }

    @Override
    public long getInvitedFee() {
        return 0l;
    }

    //@Override
    @Override
    public byte[] toBytes(int forDeal, boolean withSignature) {

        byte[] data = super.toBytes(forDeal, withSignature);

        //WRITE PERSON KEY
        byte[] keyBytes = Longs.toByteArray(this.key);
        keyBytes = Bytes.ensureCapacity(keyBytes, KEY_LENGTH, 0);
        data = Bytes.concat(data, keyBytes);

        //WRITE USER PUBLIC KEYS
        int i = 0;
        for (PublicKeyAccount publicAccount : this.sertifiedPublicKeys) {
            data = Bytes.concat(data, publicAccount.getPublicKey());

            if (withSignature & this.getVersion() == 1) {
                data = Bytes.concat(data, this.sertifiedSignatures.get(i++));
            }
        }

        //WRITE DURATION
        data = Bytes.concat(data, Ints.toByteArray(this.add_day));

        return data;
    }

    @Override
    public int getDataLength(int forDeal, boolean withSignature) {
        // not include reference

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

        int accountsSize = this.sertifiedPublicKeys.size();
        base_len += accountsSize * PublicKeyAccount.PUBLIC_KEY_LENGTH;
        return this.typeBytes[1] == 1 ? base_len + Transaction.SIGNATURE_LENGTH * accountsSize : base_len;
    }

    //VALIDATE

    @Override
    public boolean isSignatureValid(DCSet dcSet) {

        if (this.signature == null || this.signature.length != Crypto.SIGNATURE_LENGTH
                || this.signature == new byte[Crypto.SIGNATURE_LENGTH])
            return false;

        int pAccountsSize = 0;
        if (this.getVersion() == 1) {
            pAccountsSize = this.sertifiedPublicKeys.size();
            if (pAccountsSize > this.sertifiedSignatures.size())
                return false;

            byte[] singItem;
            for (int i = 0; i < pAccountsSize; i++) {
                //if (this.sertifiedSignatures.e(i);
                singItem = this.sertifiedSignatures.get(i);
                if (singItem == null || singItem.length != Crypto.SIGNATURE_LENGTH
                        || singItem == new byte[Crypto.SIGNATURE_LENGTH]) {
                    return false;
                }
            }
        }

        byte[] data = this.toBytes(Transaction.FOR_NETWORK, false);
        if (data == null) return false;

        // all test a not valid for main test
        // all other network must be invalid here!
        int port = Controller.getInstance().getNetworkPort();
        data = Bytes.concat(data, Ints.toByteArray(port));

        Crypto crypto = Crypto.getInstance();
        if (!crypto.verify(creator.getPublicKey(), signature, data))
            return false;

        // if use signs from person
        if (this.getVersion() == 1) {
            for (int i = 0; i < pAccountsSize; i++) {
                if (!crypto.verify(this.sertifiedPublicKeys.get(i).getPublicKey(), this.sertifiedSignatures.get(i), data))
                    return false;
            }
        }

        return true;
    }

    //
    @Override
    public int isValid(int asDeal, long flags) {

        int result = super.isValid(asDeal, flags | NOT_VALIDATE_FLAG_PUBLIC_TEXT);

        // сюда без проверки Персоны приходит
        if (result != VALIDATE_OK)
            return result;

        if (!BlockChain.ANONIM_SERT_USE
                && !BlockChain.DEVELOP_USE && !this.creator.isPerson(dcSet, height)) {
            boolean creator_admin = false;
            long personsCount = dcSet.getItemPersonMap().getLastKey();
            if (personsCount < 20) {
                // FIRST Persons only by ME
                // FIRST Persons only by ADMINS
                for (String admin : BlockChain.GENESIS_ADMINS) {
                    if (this.creator.equals(admin)) {
                        creator_admin = true;
                        break;
                    }
                }
            }
            if (!creator_admin
                    && this.height != 176085) // TODO: wrong transaction
                return CREATOR_NOT_PERSONALIZED;
        }

        // MY SELF PERSON INFO
        Fun.Tuple2<Integer, PersonCls> creatorPersonInfo = this.creator.getPerson(dcSet, height);

        for (PublicKeyAccount publicAccount : this.sertifiedPublicKeys) {
            //CHECK IF PERSON PUBLIC KEY IS VALID
            if (!publicAccount.isValid()) {
                return INVALID_PUBLIC_KEY;
            }
            Fun.Tuple2<Integer, PersonCls> sertifyInfo = publicAccount.getPerson(dcSet, height);
            if (sertifyInfo != null) {
                // если этот ключ уже удостоврен то его изменять может только сам владелец
                // снять удостоврение ключа может только сам владелец
                // или продлить только сам владелец может
                if (creatorPersonInfo.b.getKey() != this.key) {
                    return NOT_SELF_PERSONALIZY;
                }
            } else {
                if (this.add_day < 0) {
                    // нельзя снять удостоврение со счета который еще не удостоверен
                    return PUB_KEY_NOT_PERSONALIZED;
                }
            }
        }

        if (!dcSet.getItemPersonMap().contains(this.key)) {
            return Transaction.ITEM_PERSON_NOT_EXIST;
        }

        BigDecimal balERA = this.creator.getBalanceUSE(RIGHTS_KEY, dcSet);
        if (balERA.compareTo(
                //BlockChain.MINOR_ERA_BALANCE_BD
                BlockChain.MIN_GENERATING_BALANCE_BD
                ) < 0
            )
            return Transaction.NOT_ENOUGH_RIGHTS;

        if (creatorPersonInfo != null && !creatorPersonInfo.b.isAlive(this.timestamp))
            return Transaction.ITEM_PERSON_IS_DEAD;

        return Transaction.VALIDATE_OK;
    }

    public BigDecimal getBonuses() {

        // TODO: в кошельке сделать правильный пересчет
        long personsTotal = this.dcSet.getItemPersonMap().getLastKey();
        if (personsTotal < 3000)
            return BlockChain.BONUS_FEE_LVL1;
        else if (personsTotal < 10000)
            return BlockChain.BONUS_FEE_LVL2;
        else if (personsTotal < 100000)
            return BlockChain.BONUS_FEE_LVL3;
        else if (personsTotal < 1000000)
            return BlockChain.BONUS_FEE_LVL4;
        else
            return BlockChain.BONUS_FEE_LVL5;

    }

    //PROCESS/ORPHAN

    @Override
    public void process(Block block, int asDeal) {

        //UPDATE SENDER
        super.process(block, asDeal);

        int transactionIndex = -1;
        int blockIndex = -1;
        //Block block = this.getBlock(db);// == null (((
        if (block == null) {
            blockIndex = dcSet.getBlockMap().last().getHeight();
        } else {
            blockIndex = block.getHeight();
            if (blockIndex < 1) {
                // if block not is confirmed - get last block + 1
                blockIndex = dcSet.getBlockMap().last().getHeight() + 1;
            }
            //transactionIndex = this.getSeqNo(db);
            transactionIndex = block.getTransactionSeq(signature);
        }

        boolean personalized = false;
        TreeMap<String, Stack<Tuple3<Integer, Integer, Integer>>> personalisedData = this.dcSet.getPersonAddressMap().getItems(this.key);
        if (personalisedData != null && !personalisedData.isEmpty()) {
            for (Stack<Tuple3<Integer, Integer, Integer>> personalisedDataStack: personalisedData.values()) {
                if (!personalisedDataStack.isEmpty()) {
                    personalized = true;
                    break;
                }
            }
        }

        if (!personalized) {
            // IT IS NOT VOUCHED PERSON

            PublicKeyAccount pkAccount = this.sertifiedPublicKeys.get(0);

            //////////// FIND Issuer (registrator) this PERSON
            // FIND person
            ItemCls person = dcSet.getItemPersonMap().get(this.key);
            // FIND issue record
            Transaction transPersonIssue = dcSet.getTransactionFinalMap().get(person.getReference());
            // GIVE GIFT for ISSUER
            Account issuer = transPersonIssue.getCreator();

            // EMITTE LIA
            issuer.changeBalance(this.dcSet, false, AssetCls.LIA_KEY, BigDecimal.ONE, false);
            // SUBSTRACT from EMISSION (with minus)
            GenesisBlock.CREATOR.changeBalance(dcSet, true, AssetCls.LIA_KEY, BigDecimal.ONE, true);

            // EMITTE LIA
            this.creator.changeBalance(this.dcSet, false, -AssetCls.LIA_KEY, BigDecimal.ONE, false);
            // SUBSTRACT from EMISSION (with minus)
            GenesisBlock.CREATOR.changeBalance(dcSet, true, -AssetCls.LIA_KEY, BigDecimal.ONE, true);


            boolean makeCalculates = false;
            if (block != null && block.txCalculated != null) {
                makeCalculates = true;
            }

            // GIVE GIFT for this PUB_KEY - to PERSON
            BigDecimal personBonus = height < BlockChain.VERS_4_11 ? BONUS_FOR_PERSON_4_11.add(BONUS_FOR_PERSON_4_11)
                    : BONUS_FOR_PERSON_4_11;
            pkAccount.changeBalance(dcSet, false, FEE_KEY, personBonus, false);
            if (makeCalculates) {
                block.txCalculated.add(new RCalculated(pkAccount, FEE_KEY, personBonus,
                        "enter bonus", this.dbRef));
            }
            BigDecimal issued_FEE_BD_total = personBonus;

            BigDecimal issued_FEE_BD = transPersonIssue.getFee();
            issuer.changeBalance(dcSet, false, FEE_KEY, issued_FEE_BD, // BONUS_FOR_PERSON_REGISTRATOR_4_11,
                    false);
            if (makeCalculates) {
                block.txCalculated.add(new RCalculated(issuer, FEE_KEY, issued_FEE_BD, // BONUS_FOR_PERSON_REGISTRATOR_4_11,
                        "register reward @P:" + this.key, this.dbRef));
            }
            issued_FEE_BD_total = issued_FEE_BD_total.add(issued_FEE_BD); //BONUS_FOR_PERSON_REGISTRATOR_4_11);

            // TO EMITTE FEE (with minus)
            GenesisBlock.CREATOR.changeBalance(dcSet, true, FEE_KEY, issued_FEE_BD_total, true);

        }

        int add_day = this.add_day < 0? this.add_day : DEFAULT_DURATION;
        // set to time stamp of record
        int end_day = (int) (this.timestamp / 86400000l) + add_day;

        Tuple3<Integer, Integer, Integer> itemP = new Tuple3<Integer, Integer, Integer>(end_day,
                //Controller.getInstance().getHeight(), this.signature);
                blockIndex, transactionIndex);
        Tuple4<Long, Integer, Integer, Integer> itemA = new Tuple4<Long, Integer, Integer, Integer>(this.key, end_day,
                blockIndex, transactionIndex);

		/*
		Tuple5<Long, Long, byte[], Integer, Integer> psItem = dcSet.getPersonStatusMap().getItem(this.key, StatusCls.ALIVE_KEY);
		if (psItem == null) {
			// ADD ALIVE STATUS to PERSON for permanent TO_DATE
			PersonCls person = (PersonCls)dcSet.getItemPersonMap().get(this.key);
			dcSet.getPersonStatusMap().addItem(this.key, StatusCls.ALIVE_KEY,
					new Tuple5<Long, Long, byte[], Integer, Integer>(
							person.getBirthday(), Long.MAX_VALUE,
							new byte[0],
							blockIndex, transactionIndex));
		}
		 */

        // SET PERSON ADDRESS
        String address;
        for (PublicKeyAccount publicAccount : this.sertifiedPublicKeys) {
            address = publicAccount.getAddress();
            dcSet.getAddressPersonMap().addItem(address, itemA);
            dcSet.getPersonAddressMap().addItem(this.key, address, itemP);


            if (!dcSet.getReferenceMap().contains(publicAccount.getShortAddressBytes())) {
                // for quick search public keys by address - use PUB_KEY from Person DATA owner
                // used in - controller.Controller.getPublicKeyByAddress
                dcSet.getReferenceMap().set(
                        publicAccount.getShortAddressBytes(), new long[]{timestamp, dbRef});
            }

        }

    }

    @Override
    public void orphan(Block block, int asDeal) {

        //UPDATE SENDER
        super.orphan(block, asDeal);

        //UPDATE RECIPIENT
        String address;
        for (PublicKeyAccount publicAccount : this.sertifiedPublicKeys) {
            address = publicAccount.getAddress();
            dcSet.getAddressPersonMap().removeItem(address);
            dcSet.getPersonAddressMap().removeItem(this.key, address);
        }

        boolean personalized = false;
        TreeMap<String, Stack<Tuple3<Integer, Integer, Integer>>> personalisedData = this.dcSet.getPersonAddressMap().getItems(this.key);
        if (personalisedData != null && !personalisedData.isEmpty()) {
            for (Stack<Tuple3<Integer, Integer, Integer>> personalisedDataStack: personalisedData.values()) {
                if (!personalisedDataStack.isEmpty()) {
                    personalized = true;
                    break;
                }
            }
        }

        if (!personalized) {
            // IT WAS NOT VOUCHED PERSON BEFORE

            PublicKeyAccount pkAccount = this.sertifiedPublicKeys.get(0);

            //////////// FIND Issuer (registrator) this PERSON
            // FIND person
            ItemCls person = dcSet.getItemPersonMap().get(this.key);
            // FIND issue record
            Transaction transPersonIssue = dcSet.getTransactionFinalMap().get(person.getReference());
            Account issuer = transPersonIssue.getCreator();

            // EMITTE LIA
            issuer.changeBalance(this.dcSet, true, AssetCls.LIA_KEY, BigDecimal.ONE, false);
            // SUBSTRACT from EMISSION (with minus)
            GenesisBlock.CREATOR.changeBalance(dcSet, false, AssetCls.LIA_KEY, BigDecimal.ONE, true);

            // EMITTE LIA
            this.creator.changeBalance(this.dcSet, true, -AssetCls.LIA_KEY, BigDecimal.ONE, false);
            // SUBSTRACT from EMISSION (with minus)
            GenesisBlock.CREATOR.changeBalance(dcSet, false, -AssetCls.LIA_KEY, BigDecimal.ONE, true);

            // BONUSES

            // GIVE GIFT for this PUB_KEY - to PERSON
            // GIVE GIFT for this PUB_KEY - to PERSON
            BigDecimal personBonus = height < BlockChain.VERS_4_11 ? BONUS_FOR_PERSON_4_11.add(BONUS_FOR_PERSON_4_11)
                    : BONUS_FOR_PERSON_4_11;
            pkAccount.changeBalance(dcSet, true, FEE_KEY, personBonus, false);
            BigDecimal issued_FEE_BD_total = personBonus;

            BigDecimal issued_FEE_BD = transPersonIssue.getFee();
            issuer.changeBalance(dcSet, true, FEE_KEY, issued_FEE_BD, //BONUS_FOR_PERSON_REGISTRATOR_4_11,
                    false);
            issued_FEE_BD_total = issued_FEE_BD_total.add(issued_FEE_BD); //BONUS_FOR_PERSON_REGISTRATOR_4_11);

            // ADD to EMISSION (with minus)
            GenesisBlock.CREATOR.changeBalance(dcSet, false, FEE_KEY, issued_FEE_BD_total, true);

        }
    }

    @Override
    public HashSet<Account> getInvolvedAccounts() {
        HashSet<Account> accounts = this.getRecipientAccounts();
        accounts.add(this.creator);
        return accounts;
    }

    @Override
    public HashSet<Account> getRecipientAccounts() {
        HashSet<Account> accounts = new HashSet<Account>(3, 1);
        accounts.addAll(this.sertifiedPublicKeys);

        return accounts;
    }

    @Override
    public boolean isInvolved(Account account) {
        if (false) {
            return getInvolvedAccounts().contains(account);

        } else {
            String address = account.getAddress();
            if (address.equals(creator.getAddress())) return true;

            for (PublicKeyAccount publicAccount : this.sertifiedPublicKeys) {
                if (address.equals(publicAccount.getAddress()))
                    return true;
            }
        }

        return false;
    }


}