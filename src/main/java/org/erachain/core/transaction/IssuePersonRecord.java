package org.erachain.core.transaction;

import com.google.common.primitives.Longs;
import org.erachain.core.BlockChain;
import org.erachain.core.account.Account;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.block.Block;
import org.erachain.core.exdata.exLink.ExLink;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.item.persons.PersonCls;
import org.erachain.core.item.persons.PersonFactory;
import org.erachain.core.item.persons.PersonHuman;
import org.mapdb.Fun;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;


public class IssuePersonRecord extends IssueItemRecord {
    public static final byte TYPE_ID = (byte) ISSUE_PERSON_TRANSACTION;
    public static final String TYPE_NAME = "Issue Person";
    /**
     * Нельзя делать большой, так как вся комиссия будет эммитироваться - а значит слишком большой размер будет эммитрировать больше
     */
    public static final int MAX_DESCRIPTION_LENGTH = 1 << 15;


    public IssuePersonRecord(byte[] typeBytes, PublicKeyAccount creator, ExLink linkTo, PersonCls person, byte feePow, long timestamp, Long reference) {
        super(typeBytes, TYPE_NAME, creator, linkTo, person, feePow, timestamp, reference);
    }

    public IssuePersonRecord(byte[] typeBytes, PublicKeyAccount creator, ExLink linkTo, PersonCls person, byte feePow, long timestamp, Long reference, byte[] signature) {
        super(typeBytes, TYPE_NAME, creator, linkTo, person, feePow, timestamp, reference, signature);
    }

    public IssuePersonRecord(byte[] typeBytes, PublicKeyAccount creator, ExLink linkTo, PersonCls person, byte feePow, long timestamp,
                             Long reference, byte[] signature, long seqNo, long feeLong) {
        super(typeBytes, TYPE_NAME, creator, linkTo, person, feePow, timestamp, reference, signature);
        this.fee = BigDecimal.valueOf(feeLong, BlockChain.FEE_SCALE);
        if (seqNo > 0)
            this.setHeightSeq(seqNo);
    }

    public IssuePersonRecord(byte[] typeBytes, PublicKeyAccount creator, ExLink linkTo, PersonCls person, byte[] signature) {
        super(typeBytes, TYPE_NAME, creator, linkTo, person, (byte) 0, 0L, null, signature);
    }

    public IssuePersonRecord(PublicKeyAccount creator, PersonCls person, byte feePow, long timestamp, Long reference, byte[] signature) {
        this(new byte[]{TYPE_ID, 0, 0, 0}, creator, null, person, feePow, timestamp, reference, signature);
    }

    public IssuePersonRecord(PublicKeyAccount creator, ExLink linkTo, PersonCls person, byte feePow, long timestamp, Long reference) {
        this(new byte[]{TYPE_ID, 0, 0, 0}, creator, linkTo, person, feePow, timestamp, reference);
    }

    //GETTERS/SETTERS

    @Override
    public long getInvitedFee() {
        return 0L;
    }

    //PARSE CONVERT

    public static Transaction Parse(byte[] data, int forDeal) throws Exception {

        //boolean asPack = releaserReference != null;

        //CHECK IF WE MATCH BLOCK LENGTH
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

        ExLink linkTo;
        if ((typeBytes[2] & HAS_EXLINK_MASK) > 0) {
            linkTo = ExLink.parse(data, position);
            position += linkTo.length();
        } else {
            linkTo = null;
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

        //READ PERSON
        // person parse without reference - if is = signature
        PersonCls person = PersonFactory.getInstance().parse(forDeal, Arrays.copyOfRange(data, position, data.length), false);
        position += person.getDataLength(false);

        if (forDeal == FOR_DB_RECORD) {
            //READ KEY
            byte[] timestampBytes = Arrays.copyOfRange(data, position, position + KEY_LENGTH);
            long key = Longs.fromByteArray(timestampBytes);
            position += KEY_LENGTH;

            person.setKey(key);

        }

        if (forDeal > Transaction.FOR_MYPACK) {
            return new IssuePersonRecord(typeBytes, creator, linkTo, person, feePow, timestamp, reference, signatureBytes, seqNo, feeLong);
        } else {
            return new IssuePersonRecord(typeBytes, creator, linkTo, person, signatureBytes);
        }
    }

    @Override
    public List<byte[]> getOtherSignatures() {
        PersonHuman person = (PersonHuman) this.item;
        if (person.isMustBeSigned()) {
            List<byte[]> items = new ArrayList<byte[]>();
            items.add(person.getMakerSignature());
            return items;
        }
        return null;
    }

	@Override
	public boolean hasPublicText() {
        return !BlockChain.ANONIM_SERT_USE;
	}

    @Override
    public List<PublicKeyAccount> getPublicKeys() {
        PersonHuman person = (PersonHuman) this.item;
        if (person.isMustBeSigned()) {
            List<PublicKeyAccount> items = new ArrayList<PublicKeyAccount>();
            items.add(person.getMaker());
            return items;
        }
        return null;
    }

    //VALIDATE

    @Override
    public int isValid(int forDeal, long flags) {

        if (height < BlockChain.ALL_VALID_BEFORE) {
            return VALIDATE_OK;
        }

        PersonCls person = (PersonCls) getItem();
        // FOR PERSONS need LIMIT DESCRIPTION because it may be make with 0 COMPU balance
        int descriptionLength = person.getDescription().getBytes(StandardCharsets.UTF_8).length;
        if (descriptionLength > MAX_DESCRIPTION_LENGTH) {
            return INVALID_DESCRIPTION_LENGTH_MAX;
        }
        // birthLatitude -90..90; birthLongitude -180..180
        if (person.getBirthLatitude() > 90 || person.getBirthLatitude() < -90) {
            return Transaction.ITEM_PERSON_LATITUDE_ERROR;
        }
        if (person.getBirthLongitude() > 180 || person.getBirthLongitude() < -180) {
            return Transaction.ITEM_PERSON_LONGITUDE_ERROR;
        }
        if (person.getRace().getBytes(StandardCharsets.UTF_8).length > 255) {
            return Transaction.ITEM_PERSON_RACE_ERROR;
        }
        if (person.getGender() < 0 || person.getGender() > 2) {
            return Transaction.ITEM_PERSON_GENDER_ERROR;
        }
        if (person.getSkinColor().getBytes(StandardCharsets.UTF_8).length > 255) {
            return Transaction.ITEM_PERSON_SKIN_COLOR_ERROR;
        }
        if (person.getEyeColor().getBytes(StandardCharsets.UTF_8).length > 255) {
            return Transaction.ITEM_PERSON_EYE_COLOR_ERROR;
        }
        if (person.getHairColor().getBytes(StandardCharsets.UTF_8).length > 255) {
            return Transaction.ITEM_PERSON_HAIR_COLOR_ERROR;
        }

        //int ii = Math.abs(person.getHeight());
        //if (Math.abs(person.getHeight()) < 1) return Transaction.ITEM_PERSON_HEIGHT_ERROR;
        if (person.getHeight() > 255) {
            return Transaction.ITEM_PERSON_HEIGHT_ERROR;
        }

        int result = person.isValid();
        if (result != Transaction.VALIDATE_OK) {
            if (result == Transaction.INVALID_IMAGE_LENGTH_MAX
                    && BlockChain.MAIN_MODE && height < 157640) {
                ;
            } else {
                errorValue = person.errorValue;
                return result;
            }
        }

        // TODO  удалить правки протокола для новой цепочки NEW CHAIN
        boolean isPersonAlive = person.isAlive(this.timestamp);

        if (isPersonAlive) {
            // IF PERSON is LIVE

            if (person.getImage() == null) {
                return Transaction.INVALID_IMAGE_LENGTH_MIN;
            }
            int len = person.getImage().length;
            if (len < person.getImageMINLength()) {
                // 2998-1 - транзакция забаненная
                if (!BlockChain.MAIN_MODE || height != 2998) {
                    errorValue = "" + len + " < " + person.getImageMINLength();
                    return Transaction.INVALID_IMAGE_LENGTH_MIN;
                }
            }

        } else {
            // person is DIE - any PHOTO
        }
        if (person instanceof PersonHuman) {
            PersonHuman human = (PersonHuman) person;
            if (human.isMustBeSigned()) {
                if (!Arrays.equals(person.getMaker().getPublicKey(), creator.getPublicKey())) {
                    // OWNER of personal INFO not is CREATOR
                    if (human.getMakerSignature() == null) {
                        return Transaction.ITEM_PERSON_OWNER_SIGNATURE_INVALID;
                    }
                    if (!human.isSignatureValid(dcSet)) {
                        return Transaction.ITEM_PERSON_OWNER_SIGNATURE_INVALID;
                    }

                }
                if (height > BlockChain.START_ITEM_DUPLICATE && dcSet.getTransactionFinalMapSigns().contains(human.getMakerSignature())) {
                    return Transaction.ITEM_DUPLICATE;
                }
            }
        }

        if (this.item.isNovaAsset(this.creator, this.dcSet) > 0) {
            Fun.Tuple3<Long, Long, byte[]> item = BlockChain.NOVA_PERSONS.get(this.item.getName());
            if (!item.b.equals(((PersonCls) this.item).getBirthday())) {
                return INVALID_TIMESTAMP_START;
            }
        }

        // IF BALANCE 0 or more - not check FEE
        boolean checkFeeBalance = creator.getBalance(dcSet, FEE_KEY).a.b.compareTo(BigDecimal.ZERO) < 0;
        int res = super.isValid(forDeal, flags |
                (checkFeeBalance ? 0L : NOT_VALIDATE_FLAG_FEE) | NOT_VALIDATE_FLAG_PUBLIC_TEXT);
        // FIRST PERSONS INSERT as ADMIN
        boolean creatorAdmin = false;
        boolean creatorIsPerson = creator.isPerson(dcSet, height);
        if ((flags & NOT_VALIDATE_FLAG_PERSONAL) == 0L && !BlockChain.ANONIM_SERT_USE
                && !creatorIsPerson) {
            // ALL Persons by ADMINS
            for (String admin : BlockChain.GENESIS_ADMINS) {
                if (creator.equals(admin)) {
                    creatorAdmin = true;
                    break;
                }
            }
            if (!creatorAdmin) {
                return CREATOR_NOT_PERSONALIZED;
            }
        }

        if (isPersonAlive && height > BlockChain.START_ISSUE_RIGHTS) {
            Fun.Tuple4<Long, Integer, Integer, Integer> creatorPerson = creator.getPersonDuration(dcSet);
            if (creatorPerson != null) {
                Set<String> thisPersonAddresses = dcSet.getPersonAddressMap().getItems(creatorPerson.a).keySet();

                BigDecimal totalERAOwned = Account.totalForAddresses(dcSet, thisPersonAddresses, AssetCls.ERA_KEY, TransactionAmount.ACTION_SEND);
                BigDecimal totalLIAOwned = Account.totalForAddresses(dcSet, thisPersonAddresses, AssetCls.LIA_KEY, TransactionAmount.ACTION_SEND);

                int resultERA = BlockChain.VALID_PERSON_REG_ERA(height, totalERAOwned, totalLIAOwned);
                if (resultERA > 0) {
                    return resultERA;
                }
            }
        }

        return res;
    }

    //PROCESS/ORPHAN

    //@Override
    public void process(Block block, int forDeal) {
        //UPDATE CREATOR
        super.process(block, forDeal);

        PersonHuman person = (PersonHuman) this.item;
        PublicKeyAccount maker = person.getMaker();
        byte[] makerBytes = maker.getPublicKey();
        // Это нужно для быстрого поиска по публичному ключу создателя персоны,
        // которая еще не удостоверена вообще
        // но надо понимать что тут будет только последняя запись создания персоны и номер на нее
        // used in org.erachain.webserver.API.getPersonKeyByOwnerPublicKey
        this.dcSet.getIssuePersonMap().put(makerBytes, person.getKey());

        if (person.isMustBeSigned()) {
            // for quick search public keys by address - use PUB_KEY from Person DATA owner
            // used in - controller.Controller.getPublicKeyByAddress
            long[] makerLastTimestamp = maker.getLastTimestamp(this.dcSet);
            if (makerLastTimestamp == null) {
                maker.setLastTimestamp(new long[]{timestamp, dbRef}, this.dcSet);
            }

            // запомним подпись для поиска потом
            dcSet.getTransactionFinalMapSigns().put(person.getMakerSignature(), dbRef);
        }

    }

    //@Override
    public void orphan(Block block, int forDeal) {
        //UPDATE CREATOR
        super.orphan(block, forDeal);

        PersonHuman person = (PersonHuman) this.item;
        PublicKeyAccount maker = person.getMaker();
        byte[] makerBytes = maker.getPublicKey();
        this.dcSet.getIssuePersonMap().delete(makerBytes);

        if (person.isMustBeSigned()) {
            dcSet.getTransactionFinalMapSigns().delete(person.getMakerSignature());
        }

    }

    @Override
    public long calcBaseFee(boolean withFreeProtocol) {

        PersonCls person = (PersonCls) this.item;

        if (person.isAlive(this.timestamp)) {
            // IF PERSON is LIVE
            return super.calcBaseFee(withFreeProtocol) >> 1;
        }

        // is DEAD
        return super.calcBaseFee(withFreeProtocol);
    }
}
