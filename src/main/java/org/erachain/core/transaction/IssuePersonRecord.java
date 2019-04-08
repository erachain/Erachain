package org.erachain.core.transaction;

import com.google.common.primitives.Longs;
import org.erachain.core.BlockChain;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.block.Block;
import org.erachain.core.item.persons.PersonCls;
import org.erachain.core.item.persons.PersonFactory;
import org.erachain.core.item.persons.PersonHuman;
import org.erachain.datachain.AddressTimeSignatureMap;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

//import java.util.Map;
// import org.slf4j.LoggerFactory;

public class IssuePersonRecord extends IssueItemRecord {
    public static final int MAX_IMAGE_LENGTH = 20480;
    private static final byte TYPE_ID = (byte) ISSUE_PERSON_TRANSACTION;
    private static final String NAME_ID = "Issue Person";

    public IssuePersonRecord(byte[] typeBytes, PublicKeyAccount creator, PersonCls person, byte feePow, long timestamp, Long reference) {
        super(typeBytes, NAME_ID, creator, person, feePow, timestamp, reference);
    }

    public IssuePersonRecord(byte[] typeBytes, PublicKeyAccount creator, PersonCls person, byte feePow, long timestamp, Long reference, byte[] signature) {
        super(typeBytes, NAME_ID, creator, person, feePow, timestamp, reference, signature);
    }

    public IssuePersonRecord(byte[] typeBytes, PublicKeyAccount creator, PersonCls person, byte feePow, long timestamp,
                             Long reference, byte[] signature, long feeLong) {
        super(typeBytes, NAME_ID, creator, person, feePow, timestamp, reference, signature);
        this.fee = BigDecimal.valueOf(feeLong, BlockChain.AMOUNT_DEDAULT_SCALE);
    }

    public IssuePersonRecord(byte[] typeBytes, PublicKeyAccount creator, PersonCls person, byte[] signature) {
        super(typeBytes, NAME_ID, creator, person, (byte) 0, 0l, null, signature);
    }

    public IssuePersonRecord(PublicKeyAccount creator, PersonCls person, byte feePow, long timestamp, Long reference, byte[] signature) {
        this(new byte[]{TYPE_ID, 0, 0, 0}, creator, person, feePow, timestamp, reference, signature);
    }

    public IssuePersonRecord(PublicKeyAccount creator, PersonCls person, byte[] signature) {
        this(new byte[]{TYPE_ID, 0, 0, 0}, creator, person, (byte) 0, 0l, null, signature);
    }

    public IssuePersonRecord(PublicKeyAccount creator, PersonCls person, byte feePow, long timestamp, Long reference) {
        this(new byte[]{TYPE_ID, 0, 0, 0}, creator, person, feePow, timestamp, reference);
    }

    public IssuePersonRecord(PublicKeyAccount creator, PersonCls person) {
        this(new byte[]{TYPE_ID, 0, 0, 0}, creator, person, (byte) 0, 0l, null);
    }

    //GETTERS/SETTERS
    //public String getName() { return "Issue Person"; }

    @Override
    public long getInvitedFee() {
        return 0l;
    }

    // RETURN START KEY in tot GEMESIS
    public long getStartKey(int height) {

        if (height < BlockChain.VERS_4_11) {
            return 0l;
        }

        return START_KEY;

    }

    //PARSE CONVERT

    public static Transaction Parse(byte[] data, int asDeal) throws Exception {

        //boolean asPack = releaserReference != null;

        //CHECK IF WE MATCH BLOCK LENGTH
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

        //READ PERSON
        // person parse without reference - if is = signature
        PersonCls person = PersonFactory.getInstance().parse(Arrays.copyOfRange(data, position, data.length), false);
        position += person.getDataLength(false);

        if (asDeal > Transaction.FOR_MYPACK) {
            return new IssuePersonRecord(typeBytes, creator, person, feePow, timestamp, reference, signatureBytes, feeLong);
        } else {
            return new IssuePersonRecord(typeBytes, creator, person, signatureBytes);
        }
    }

    @Override
    public List<byte[]> getSignatures() {
        PersonHuman person = (PersonHuman) this.item;
        if (person.isMustBeSigned()) {
            List<byte[]> items = new ArrayList<byte[]>();
            items.add(person.getOwnerSignature());
            return items;
        }
        return null;
    }

	/*
	@Override
	public boolean hasPublicText() {
		for ( String admin: BlockChain.GENESIS_ADMINS) {
			if (this.creator.equals(admin)) {
				return false;
			}
		}
		return true;
	}
	 */

    @Override
    public List<PublicKeyAccount> getPublicKeys() {
        PersonHuman person = (PersonHuman) this.item;
        if (person.isMustBeSigned()) {
            List<PublicKeyAccount> items = new ArrayList<PublicKeyAccount>();
            items.add(person.getOwner());
            return items;
        }
        return null;
    }

    //VALIDATE

    @Override
    public int isValid(int asDeal, long flags) {
        PersonCls person = (PersonCls) getItem();
        // FOR PERSONS need LIMIT DESCRIPTION because it may be make with 0 COMPU balance
        int descriptionLength = person.getDescription().getBytes(StandardCharsets.UTF_8).length;
        if (descriptionLength > 8000) {
            return INVALID_DESCRIPTION_LENGTH;
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
        if (person.getGender() > 10) {
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
        if (person.getHeight() > 255) {
            return Transaction.ITEM_PERSON_HEIGHT_ERROR;
        }
        if (person.isAlive(timestamp)) {
            // IF PERSON is LIVE
            if (person.getImage().length > MAX_IMAGE_LENGTH) {
                if (!(!BlockChain.DEVELOP_USE && height == 2998) && height > 157640) {
                    // early blocks has wrong ISSUE_PERSON with 0 image length - in block 2998
                    return Transaction.INVALID_IMAGE_LENGTH;
                }
            }
        }
        if (person instanceof PersonHuman) {
            PersonHuman human = (PersonHuman) person;
            if (human.isMustBeSigned() && !Arrays.equals(person.getOwner().getPublicKey(), creator.getPublicKey())) {
                // OWNER of personal INFO not is CREATOR
                if (human.getOwnerSignature() == null) {
                    return Transaction.ITEM_PERSON_OWNER_SIGNATURE_INVALID;
                }
                if (!human.isSignatureValid(dcSet)) {
                    return Transaction.ITEM_PERSON_OWNER_SIGNATURE_INVALID;
                }
            }
        }

        // IF BALANCE 0 or more - not check FEE
        boolean checkFeeBalance = creator.getBalance(dcSet, FEE_KEY).a.b.compareTo(BigDecimal.ZERO) < 0;
        int res = super.isValid(asDeal, flags |
                (checkFeeBalance ? 0L : NOT_VALIDATE_FLAG_FEE) | NOT_VALIDATE_FLAG_PUBLIC_TEXT);
        // FIRST PERSONS INSERT as ADMIN
        boolean creatorAdmin = false;
        if (!creator.isPerson(dcSet, height)) {
            long count = dcSet.getItemPersonMap().getLastKey();
            if (count < 20) {
                // FIRST Persons only by ME
                // FIRST Persons only by ADMINS
                for (String admin : BlockChain.GENESIS_ADMINS) {
                    if (creator.equals(admin)) {
                        creatorAdmin = true;
                        break;
                    }
                }
            }
            if (!creatorAdmin)  {
                return CREATOR_NOT_PERSONALIZED;
            }
        }
        return res;
    }

    //PROCESS/ORPHAN

    //@Override
    public void process(Block block, int asDeal) {
        //UPDATE CREATOR
        super.process(block, asDeal);

        PersonHuman person = (PersonHuman) this.item;
        PublicKeyAccount maker = person.getOwner();
        byte[] makerBytes = maker.getPublicKey();
        // Это нужно для быстрого поиска по публичному ключу создателя персоны,
        // которая еще не удостоверена вообще
        // но надо понимать что тут будет только последняя запись создания персоны и номер на нее
        // used in org.erachain.webserver.API.getPersonKeyByOwnerPublicKey
        this.dcSet.getIssuePersonMap().set(makerBytes, person.getKey());

        if (person.isMustBeSigned()) {
            // for quick search public keys by address - use PUB_KEY from Person DATA owner
            // used in - controller.Controller.getPublicKeyByAddress
            AddressTimeSignatureMap dbASmap = this.dcSet.getAddressTime_SignatureMap();
            String creatorAddress = maker.getAddress();
            if (!dbASmap.contains(creatorAddress)) {
                dbASmap.set(creatorAddress, this.signature);
            }
        }

    }

    //@Override
    public void orphan(Block block, int asDeal) {
        //UPDATE CREATOR
        super.orphan(block, asDeal);

        PersonHuman person = (PersonHuman) this.item;
        PublicKeyAccount maker = person.getOwner();
        byte[] makerBytes = maker.getPublicKey();
        this.dcSet.getIssuePersonMap().delete(makerBytes);

    }

    @Override
    public long calcBaseFee() {

        PersonCls person = (PersonCls) this.item;

        if (person.isAlive(this.timestamp)) {
            // IF PERSON is LIVE
            return calcCommonFee() >> 1;
        }

        // is DEAD
        return calcCommonFee();
    }
}
