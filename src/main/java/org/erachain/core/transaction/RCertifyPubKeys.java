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
import org.erachain.core.exdata.exLink.ExLink;
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
public class RCertifyPubKeys extends Transaction implements Itemable {

    public static final byte TYPE_ID = (byte) Transaction.CERTIFY_PUB_KEYS_TRANSACTION;
    public static final String TYPE_NAME = "Certify Public Key";

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
    protected List<PublicKeyAccount> certifiedPublicKeys;
    protected List<byte[]> certifiedSignatures;

    public RCertifyPubKeys(byte[] typeBytes, PublicKeyAccount creator, ExLink exLink, byte feePow, long key,
                           List<PublicKeyAccount> certifiedPublicKeys,
                           int add_day, long timestamp, Long reference) {
        super(typeBytes, TYPE_NAME, creator, exLink, feePow, timestamp, reference);

        this.key = key;
        this.certifiedPublicKeys = certifiedPublicKeys;
        this.add_day = add_day;
    }

    public RCertifyPubKeys(int version, PublicKeyAccount creator, byte feePow, long key,
                           List<PublicKeyAccount> certifiedPublicKeys,
                           int add_day, long timestamp, Long reference) {
        this(new byte[]{TYPE_ID, (byte) version, (byte) certifiedPublicKeys.size(), 0}, creator, null, feePow, key,
                certifiedPublicKeys,
                add_day, timestamp, reference);
    }

    public RCertifyPubKeys(int version, PublicKeyAccount creator, ExLink exLink, byte feePow, long key,
                           List<PublicKeyAccount> certifiedPublicKeys,
                           int add_day, long timestamp, Long reference) {
        this(new byte[]{TYPE_ID, (byte) version, (byte) certifiedPublicKeys.size(), 0}, creator, null, feePow, key,
                certifiedPublicKeys,
                add_day, timestamp, reference);
    }

    // set default date
    public RCertifyPubKeys(int version, PublicKeyAccount creator, byte feePow, long key,
                           List<PublicKeyAccount> certifiedPublicKeys,
                           long timestamp, Long reference) {
        this(new byte[]{TYPE_ID, (byte) version, (byte) certifiedPublicKeys.size(), 0}, creator, null, feePow, key,
                certifiedPublicKeys,
                0, timestamp, reference);
    }

    public RCertifyPubKeys(byte[] typeBytes, PublicKeyAccount creator, byte feePow, long key,
                           List<PublicKeyAccount> certifiedPublicKeys,
                           int add_day, long timestamp, Long reference, byte[] signature,
                           List<byte[]> certifiedSignatures) {
        this(typeBytes, creator, null, feePow, key,
                certifiedPublicKeys,
                add_day, timestamp, reference);
        this.signature = signature;
        this.certifiedSignatures = certifiedSignatures;
    }

    public RCertifyPubKeys(byte[] typeBytes, PublicKeyAccount creator, ExLink exLink, byte feePow, long key,
                           List<PublicKeyAccount> certifiedPublicKeys,
                           int add_day, long timestamp, Long reference, byte[] signature, long feeLong,
                           long seqNo, List<byte[]> certifiedSignatures) {
        this(typeBytes, creator, exLink, feePow, key,
                certifiedPublicKeys,
                add_day, timestamp, reference);
        this.signature = signature;
        this.certifiedSignatures = certifiedSignatures;
        if (seqNo > 0)
            this.setHeightSeq(seqNo);
        this.fee = BigDecimal.valueOf(feeLong, BlockChain.FEE_SCALE);
    }


    // as pack
    public RCertifyPubKeys(byte[] typeBytes, PublicKeyAccount creator, ExLink exLink, long key,
                           List<PublicKeyAccount> certifiedPublicKeys,
                           int add_day, byte[] signature,
                           List<byte[]> certifiedSignatures) {
        this(typeBytes, creator, exLink, (byte) 0, key,
                certifiedPublicKeys,
                add_day, 0l, null);
        this.signature = signature;
        this.certifiedSignatures = certifiedSignatures;
    }

    public RCertifyPubKeys(int version, PublicKeyAccount creator, byte feePow, long key,
                           List<PublicKeyAccount> certifiedPublicKeys,
                           int add_day, long timestamp, Long reference, byte[] signature,
                           byte[] userSignature1, byte[] userSignature2, byte[] userSignature3) {
        this(new byte[]{TYPE_ID, (byte) version, (byte) certifiedPublicKeys.size(), 0}, creator, null, feePow, key,
                certifiedPublicKeys,
                add_day, timestamp, reference);
    }

    // as pack
    public RCertifyPubKeys(int version, PublicKeyAccount creator, long key,
                           List<PublicKeyAccount> certifiedPublicKeys,
                           int add_day, byte[] signature,
                           byte[] userSignature1, byte[] userSignature2, byte[] userSignature3) {
        this(new byte[]{TYPE_ID, (byte) version, (byte) certifiedPublicKeys.size(), 0}, creator, null, (byte) 0, key,
                certifiedPublicKeys,
                add_day, 0l, null);
    }

    //GETTERS/SETTERS

    public void setDC(DCSet dcSet, boolean andUpdateFromState) {
        super.setDC(dcSet, false);

        if (dcSet != null) {
            this.person = (PersonCls) this.dcSet.getItemPersonMap().get(this.key);
        }

        if (false && andUpdateFromState && !isWiped())
            updateFromStateDB();

    }

    @Override
    public ItemCls getItem() {
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
    public static Transaction Parse(byte[] data, int forDeal) throws Exception {

        int test_len;
        if (forDeal == Transaction.FOR_MYPACK) {
            test_len = BASE_LENGTH_AS_MYPACK;
        } else if (forDeal == Transaction.FOR_PACK) {
            test_len = BASE_LENGTH_AS_PACK;
        } else if (forDeal == Transaction.FOR_DB_RECORD) {
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
        if (forDeal > Transaction.FOR_MYPACK) {
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

        ExLink exLink;
        if ((typeBytes[2] & HAS_EXLINK_MASK) > 0) {
            exLink = ExLink.parse(data, position);
            position += exLink.length();
        } else {
            exLink = null;
        }

        byte feePow = 0;
        if (forDeal > Transaction.FOR_PACK) {
            //READ FEE POWER
            byte[] feePowBytes = Arrays.copyOfRange(data, position, position + 1);
            feePow = feePowBytes[0];
            position += 1;
        }

        //READ SIGNATURE
        byte[] signature = Arrays.copyOfRange(data, position, position + SIGNATURE_LENGTH);
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

        //READ PERSON KEY
        byte[] keyBytes = Arrays.copyOfRange(data, position, position + KEY_LENGTH);
        long key = Longs.fromByteArray(keyBytes);
        position += KEY_LENGTH;

        //byte[] item;
        List<PublicKeyAccount> certifiedPublicKeys = new ArrayList<PublicKeyAccount>();
        List<byte[]> certifiedSignatures = new ArrayList<byte[]>();
        for (int i = 0; i < getPublicKeysSize(typeBytes); i++) {
            //READ USER ACCOUNT
            certifiedPublicKeys.add(new PublicKeyAccount(Arrays.copyOfRange(data, position, position + USER_ADDRESS_LENGTH)));
            position += USER_ADDRESS_LENGTH;

            if (getVersion(typeBytes) == 1) {
                //READ USER SIGNATURE
                certifiedSignatures.add(Arrays.copyOfRange(data, position, position + SIGNATURE_LENGTH));
                position += SIGNATURE_LENGTH;
            }
        }

        // READ DURATION
        int add_day = Ints.fromByteArray(Arrays.copyOfRange(data, position, position + DATE_DAY_LENGTH));
        position += DATE_DAY_LENGTH;

        if (forDeal > Transaction.FOR_MYPACK) {
            return new RCertifyPubKeys(typeBytes, creator, exLink, feePow, key,
                    certifiedPublicKeys,
                    add_day, timestamp, reference, signature, feeLong,
                    seqNo, certifiedSignatures);
        } else {
            return new RCertifyPubKeys(typeBytes, creator, exLink, key,
                    certifiedPublicKeys,
                    add_day, signature,
                    certifiedSignatures);
        }

    }

    // PERSON KEY
    @Override
    public long getKey() {
        return this.key;
    }

    @Override
    public List<PublicKeyAccount> getPublicKeys() {
        return this.certifiedPublicKeys;
    }

    public List<PublicKeyAccount> getCertifiedPublicKeys() {
        return this.certifiedPublicKeys;
    }

    public List<String> getCertifiedPublicKeysB58() {
        List<String> pbKeys = new ArrayList<String>();
        for (PublicKeyAccount key : this.certifiedPublicKeys) {
            pbKeys.add(Base58.encode(key.getPublicKey()));
        }
        ;
        return pbKeys;
    }

    public List<byte[]> getCertifiedSignatures() {
        return this.certifiedSignatures;
    }

    public List<String> getCertifiedSignaturesB58() {
        List<String> items = new ArrayList<String>();
        if (this.certifiedSignatures == null)
            return items;

        for (byte[] item : this.certifiedSignatures) {
            items.add(Base58.encode(item));
        }

        return items;
    }

    @Override
    public List<byte[]> getOtherSignatures() {
        return certifiedSignatures;
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
        return !BlockChain.ANONIM_SERT_USE;
    }

    //////// VIEWS
    @Override
    public String viewAmount(String address) {
        return add_day > 0 ? "+" + add_day : "" + add_day;
    }

    @Override
    public String viewRecipient() {
        return Base58.encode(this.certifiedPublicKeys.get(0).getPublicKey());
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
        transaction.put("certified_public_keys", this.getCertifiedPublicKeysB58());
        transaction.put("certified_signatures", this.getCertifiedSignaturesB58());
        transaction.put("add_day", this.add_day);

        return transaction;
    }

    public void signUserAccounts(List<PrivateKeyAccount> userPrivateAccounts) {
        byte[] data;
        // use this.reference in any case
        data = this.toBytes(FOR_NETWORK, false);
        if (data == null) return;

        if (BlockChain.CLONE_MODE) {
            // чтобы из других цепочек не срабатывало
            data = Bytes.concat(data, Controller.getInstance().blockChain.getGenesisBlock().getSignature());
        } else {
            // чтобы из TestNEt не сработало
            int port = BlockChain.NETWORK_PORT;
            data = Bytes.concat(data, Ints.toByteArray(port));
        }

        if (this.certifiedSignatures == null) this.certifiedSignatures = new ArrayList<byte[]>();

        byte[] publicKey;
        for (PublicKeyAccount publicAccount : this.certifiedPublicKeys) {
            for (PrivateKeyAccount privateAccount : userPrivateAccounts) {
                publicKey = privateAccount.getPublicKey();
                if (Arrays.equals((publicKey), publicAccount.getPublicKey())) {
                    this.certifiedSignatures.add(Crypto.getInstance().sign(privateAccount, data));
                    break;
                }
            }
        }
    }

    @Override
    public long getInvitedFee() {
        return 0L;
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
        for (PublicKeyAccount publicAccount : this.certifiedPublicKeys) {
            data = Bytes.concat(data, publicAccount.getPublicKey());

            if (withSignature & this.getVersion() == 1) {
                data = Bytes.concat(data, this.certifiedSignatures.get(i++));
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

        if (exLink != null)
            base_len += exLink.length();

        if (!withSignature)
            base_len -= SIGNATURE_LENGTH;

        int accountsSize = this.certifiedPublicKeys.size();
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
            pAccountsSize = this.certifiedPublicKeys.size();
            if (pAccountsSize > this.certifiedSignatures.size())
                return false;

            byte[] singItem;
            for (int i = 0; i < pAccountsSize; i++) {
                //if (this.certifiedSignatures.e(i);
                singItem = this.certifiedSignatures.get(i);
                if (singItem == null || singItem.length != Crypto.SIGNATURE_LENGTH
                        || singItem == new byte[Crypto.SIGNATURE_LENGTH]) {
                    return false;
                }
            }
        }

        byte[] data = this.toBytes(Transaction.FOR_NETWORK, false);
        if (data == null) return false;

        if (BlockChain.CLONE_MODE) {
            // чтобы из других цепочек не срабатывало
            data = Bytes.concat(data, Controller.getInstance().blockChain.getGenesisBlock().getSignature());
        } else {
            // чтобы из TestNEt не сработало
            int port = BlockChain.NETWORK_PORT;
            data = Bytes.concat(data, Ints.toByteArray(port));
        }

        Crypto crypto = Crypto.getInstance();
        if (!crypto.verify(creator.getPublicKey(), signature, data))
            return false;

        // if use signs from person
        if (this.getVersion() == 1) {
            for (int i = 0; i < pAccountsSize; i++) {
                if (!crypto.verify(this.certifiedPublicKeys.get(i).getPublicKey(), this.certifiedSignatures.get(i), data))
                    return false;
            }
        }

        return true;
    }

    //
    @Override
    public int isValid(int forDeal, long flags) {

        if (height < BlockChain.ALL_VALID_BEFORE) {
            return VALIDATE_OK;
        }

        for (String admin : BlockChain.GENESIS_ADMINS) {
            if (creator.equals(admin)) {
                flags = flags | NOT_VALIDATE_FLAG_FEE;
                break;
            }
        }
        int result = super.isValid(forDeal, flags | NOT_VALIDATE_FLAG_PUBLIC_TEXT);

        // сюда без проверки Персоны приходит
        if (result != VALIDATE_OK)
            return result;

        ///// CREATOR
        if ((flags & NOT_VALIDATE_FLAG_PERSONAL) == 0L && !BlockChain.ANONIM_SERT_USE
                && !this.creator.isPerson(dcSet, height, creatorPersonDuration)) {
            boolean creator_admin = false;
            // ALL Persons by ADMINS
            for (String admin : BlockChain.GENESIS_ADMINS) {
                if (this.creator.equals(admin)) {
                    creator_admin = true;
                    break;
                }
            }
            if (!creator_admin
                    && !BlockChain.MAIN_MODE && this.height != 176085) // TODO: wrong transaction
                return CREATOR_NOT_PERSONALIZED;
        }

        //////// PERSON
        if (!dcSet.getItemPersonMap().contains(this.key)) {
            return Transaction.ITEM_PERSON_NOT_EXIST;
        }

        if (!person.isAlive(this.timestamp))
            return Transaction.ITEM_PERSON_IS_DEAD;

        ///////// PUBLIC KEYS
        for (PublicKeyAccount publicAccount : this.certifiedPublicKeys) {
            //CHECK IF PERSON PUBLIC KEY IS VALID
            if (!publicAccount.isValid()) {
                return INVALID_PUBLIC_KEY;
            }

            Tuple4<Long, Integer, Integer, Integer> personDuration = publicAccount.getPersonDuration(dcSet);

            if (personDuration != null) {
                // если этот ключ уже удостоверен, то его изменять может только сам владелец
                // снять удостоверение ключа может только сам владелец
                // или продлить только сам владелец может
                if (!personDuration.a.equals(this.key)) {
                    return INVALID_PERSONALIZY_ANOTHER_PERSON;
                }
            } else {
                if (this.add_day < 0) {
                    // нельзя снять удостоверение со счета который еще не удостоверен
                    return PUB_KEY_NOT_PERSONALIZED;
                }
            }
        }

        if (height > BlockChain.START_ISSUE_RIGHTS) {
            Fun.Tuple4<Long, Integer, Integer, Integer> creatorPerson = creator.getPersonDuration(dcSet);
            if (creatorPerson != null) {
                Set<String> thisPersonAddresses = dcSet.getPersonAddressMap().getItems(creatorPerson.a).keySet();

                BigDecimal totalERAOwned = Account.totalForAddresses(dcSet, thisPersonAddresses, AssetCls.ERA_KEY, TransactionAmount.ACTION_SEND);
                BigDecimal totalLIAOwned = Account.totalForAddresses(dcSet, thisPersonAddresses, AssetCls.LIA_KEY, TransactionAmount.ACTION_DEBT);

                int resultERA = BlockChain.VALID_PERSON_CERT_ERA(this, height, totalERAOwned, totalLIAOwned);
                if (resultERA > 0) {
                    return resultERA;
                }
            }
        } else {
            if (creator.getBalanceUSE(RIGHTS_KEY, dcSet).compareTo(BlockChain.MIN_GENERATING_BALANCE_BD) < 0) {
                return Transaction.NOT_ENOUGH_ERA_USE_100;
            }
        }

        return Transaction.VALIDATE_OK;
    }

    @Override
    public void makeItemsKeys() {
        if (isWiped()) {
            itemsKeys = new Object[][]{};
        }

        if (creatorPersonDuration == null) {
            // Creator is ADMIN
            itemsKeys = new Object[][]{
                    new Object[]{ItemCls.PERSON_TYPE, key, person.getTags()}
            };
        } else {
            itemsKeys = new Object[][]{
                    new Object[]{ItemCls.PERSON_TYPE, key, person.getTags()},
                    new Object[]{ItemCls.PERSON_TYPE, creatorPersonDuration.a, creatorPerson.getTags()}
            };
        }
    }

    //PROCESS/ORPHAN

    @Override
    public void process(Block block, int forDeal) {

        //UPDATE SENDER
        super.process(block, forDeal);

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

            PublicKeyAccount pkAccount = this.certifiedPublicKeys.get(0);

            //////////// FIND Issuer (registrar) this PERSON
            // FIND person
            ItemCls person = dcSet.getItemPersonMap().get(this.key);
            // FIND issue record
            Transaction transPersonIssue = dcSet.getTransactionFinalMap().get(person.getReference());
            // GIVE GIFT for ISSUER
            Account issuer = transPersonIssue.getCreator();

            // EMITTE LIA
            issuer.changeBalance(this.dcSet, false, false, AssetCls.LIA_KEY, BigDecimal.ONE,
                    false, false, false);
            // SUBSTRACT from EMISSION (with minus)
            GenesisBlock.CREATOR.changeBalance(dcSet, true, false, AssetCls.LIA_KEY, BigDecimal.ONE,
                    false, false, true);

            // EMITTE LIA
            this.creator.changeBalance(this.dcSet, false, false, -AssetCls.LIA_KEY, BigDecimal.ONE,
                    false, false, false);
            // SUBSTRACT from EMISSION (with minus)
            GenesisBlock.CREATOR.changeBalance(dcSet, true, false, -AssetCls.LIA_KEY, BigDecimal.ONE,
                    false, false, true);


            // GIVE GIFT for this PUB_KEY - to PERSON
            BigDecimal issued_FEE_BD_total;
            BigDecimal personBonus = BlockChain.BONUS_FOR_PERSON(height);
            if (personBonus.signum() != 0) {
                pkAccount.changeBalance(dcSet, false, false, FEE_KEY, personBonus,
                        false, false, false);
                pkAccount.changeCOMPUStatsBalances(dcSet, false, personBonus, Account.FEE_BALANCE_SIDE_TOTAL_EARNED);
                if (block != null) {
                    block.addCalculated(pkAccount, FEE_KEY, personBonus,
                            "enter bonus", this.dbRef);
                }
                issued_FEE_BD_total = personBonus;
            } else {
                issued_FEE_BD_total = BigDecimal.ZERO;
            }

            BigDecimal issued_FEE_BD = transPersonIssue.getFee();
            issuer.changeBalance(dcSet, false, false, FEE_KEY, issued_FEE_BD, // BONUS_FOR_PERSON_REGISTRAR_4_11,
                    false, false, false);
            issuer.changeCOMPUStatsBalances(dcSet, false, issued_FEE_BD, Account.FEE_BALANCE_SIDE_TOTAL_EARNED);
            if (block != null)
                block.addCalculated(issuer, FEE_KEY, issued_FEE_BD, // BONUS_FOR_PERSON_REGISTRAR_4_11,
                        "register reward @P:" + this.key, this.dbRef);

            issued_FEE_BD_total = issued_FEE_BD_total.add(issued_FEE_BD); //BONUS_FOR_PERSON_REGISTRAR_4_11);

            // TO EMITTE FEE (with minus)
            BlockChain.FEE_ASSET_EMITTER.changeBalance(dcSet, true, false, FEE_KEY, issued_FEE_BD_total,
                    false, false, true);

        }

        int add_day = this.add_day < 0? this.add_day : BlockChain.DEFAULT_DURATION;
        // set to time stamp of record
        int end_day = (int) (this.timestamp / 86400000L) + add_day;

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
        for (PublicKeyAccount publicAccount : this.certifiedPublicKeys) {
            address = publicAccount.getAddress();
            dcSet.getAddressPersonMap().addItem(publicAccount.getShortAddressBytes(), itemA);
            dcSet.getPersonAddressMap().addItem(this.key, address, itemP);


            // TODO удалить это если публичный ключ будет созраняться в таблице Счетов
            if (publicAccount.getLastTimestamp(dcSet) == null) {
                // for quick search public keys by address - use PUB_KEY from Person DATA owner
                // used in - controller.Controller.getPublicKeyByAddress
                publicAccount.setLastTimestamp(new long[]{timestamp, dbRef}, dcSet);
            }

        }

    }

    @Override
    public void orphan(Block block, int forDeal) {

        //UPDATE SENDER
        super.orphan(block, forDeal);

        //UPDATE RECIPIENT
        String address;
        for (PublicKeyAccount publicAccount : this.certifiedPublicKeys) {
            address = publicAccount.getAddress();
            dcSet.getAddressPersonMap().removeItem(publicAccount.getShortAddressBytes());
            dcSet.getPersonAddressMap().removeItem(this.key, address);

            // TODO удалить это если публичный ключ будет созраняться в таблице Счетов
            // при откате нужно след в истории удалить а сам публичный ключ отсавить на всякий случай?
            long[] lastPoint = publicAccount.getLastTimestamp(dcSet);
            if (lastPoint != null && lastPoint[0] == timestamp) {
                publicAccount.removeLastTimestamp(dcSet, timestamp);
            }

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

            PublicKeyAccount pkAccount = this.certifiedPublicKeys.get(0);

            //////////// FIND Issuer (registrar) this PERSON
            // FIND person
            ItemCls person = dcSet.getItemPersonMap().get(this.key);
            // FIND issue record
            Transaction transPersonIssue = dcSet.getTransactionFinalMap().get(person.getReference());
            Account issuer = transPersonIssue.getCreator();

            // EMITTE LIA
            issuer.changeBalance(this.dcSet, true, false, AssetCls.LIA_KEY, BigDecimal.ONE,
                    false, false, false);
            // SUBSTRACT from EMISSION (with minus)
            GenesisBlock.CREATOR.changeBalance(dcSet, false, false, AssetCls.LIA_KEY, BigDecimal.ONE, false, false, true);

            // EMITTE LIA
            this.creator.changeBalance(this.dcSet, true, false, -AssetCls.LIA_KEY, BigDecimal.ONE, false, false, false);
            // SUBSTRACT from EMISSION (with minus)
            GenesisBlock.CREATOR.changeBalance(dcSet, false, false, -AssetCls.LIA_KEY, BigDecimal.ONE, false, false, true);

            // BONUSES

            // GIVE GIFT for this PUB_KEY - to PERSON
            BigDecimal issued_FEE_BD_total;
            BigDecimal personBonus = BlockChain.BONUS_FOR_PERSON(height);
            if (personBonus.signum() != 0) {

                pkAccount.changeBalance(dcSet, true, false, FEE_KEY, personBonus, false, false, false);
                pkAccount.changeCOMPUStatsBalances(dcSet, true, personBonus, Account.FEE_BALANCE_SIDE_TOTAL_EARNED);
                issued_FEE_BD_total = personBonus;
            } else {
                issued_FEE_BD_total = BigDecimal.ZERO;
            }

            BigDecimal issued_FEE_BD = transPersonIssue.getFee();
            issuer.changeBalance(dcSet, true, false, FEE_KEY, issued_FEE_BD, //BONUS_FOR_PERSON_REGISTRAR_4_11,
                    false, false, false);
            issuer.changeCOMPUStatsBalances(dcSet, true, issued_FEE_BD, Account.FEE_BALANCE_SIDE_TOTAL_EARNED);
            issued_FEE_BD_total = issued_FEE_BD_total.add(issued_FEE_BD); //BONUS_FOR_PERSON_REGISTRAR_4_11);

            // ADD to EMISSION (with minus)
            BlockChain.FEE_ASSET_EMITTER.changeBalance(dcSet, false, false, FEE_KEY, issued_FEE_BD_total, false, false, true);

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
        accounts.addAll(this.certifiedPublicKeys);

        return accounts;
    }

    @Override
    public boolean isInvolved(Account account) {
        if (account.equals(creator)) return true;

        for (PublicKeyAccount publicAccount : this.certifiedPublicKeys) {
            if (publicAccount.equals(account))
                return true;
        }

        return false;
    }


}