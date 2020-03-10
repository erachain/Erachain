package org.erachain.core.item.persons;

//import java.math.BigDecimal;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import org.erachain.core.BlockChain;
import org.erachain.core.account.PrivateKeyAccount;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.crypto.Base58;
import org.erachain.core.crypto.Crypto;
import org.erachain.core.item.ItemCls;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;
import org.erachain.utils.ByteArrayUtils;
import org.json.simple.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

//  typeBytes[1] ::: ownerSignature==null?(byte)0:(byte)1
public class PersonHuman extends PersonCls {

    private static final int TYPE_ID = HUMAN;

    // for personal data owner - his signature
    protected byte[] ownerSignature;

    public PersonHuman(PublicKeyAccount owner, String fullName, long birthday, long deathday,
                       byte gender, String race, float birthLatitude, float birthLongitude,
                       String skinColor, String eyeColor, String hairСolor, int height, byte[] icon, byte[] image, String description,
                       byte[] ownerSignature) {
        super(new byte[]{(byte) TYPE_ID, ownerSignature == null ? (byte) 0 : (byte) 1}, owner, fullName, birthday, deathday,
                gender, race, birthLatitude, birthLongitude,
                skinColor, eyeColor, hairСolor, (byte) height, icon, image, description);
        this.ownerSignature = ownerSignature;
    }

    public PersonHuman(PublicKeyAccount owner, String fullName, String birthday, String deathday,
                       byte gender, String race, float birthLatitude, float birthLongitude,
                       String skinColor, String eyeColor, String hairСolor, int height, byte[] icon, byte[] image, String description,
                       byte[] ownerSignature) {
        super(new byte[]{(byte) TYPE_ID, ownerSignature == null ? (byte) 0 : (byte) 1}, owner, fullName, birthday, deathday,
                gender, race, birthLatitude, birthLongitude,
                skinColor, eyeColor, hairСolor, (byte) height, icon, image, description);
        this.ownerSignature = ownerSignature;
    }

    public PersonHuman(byte[] typeBytes, PublicKeyAccount owner, String fullName, long birthday, long deathday,
                       byte gender, String race, float birthLatitude, float birthLongitude,
                       String skinColor, String eyeColor, String hairСolor, int height, byte[] icon, byte[] image,
                       String description, byte[] ownerSignature) {
        super(typeBytes, owner, fullName, birthday, deathday,
                gender, race, birthLatitude, birthLongitude,
                skinColor, eyeColor, hairСolor, (byte) height, icon, image, description);
        this.ownerSignature = ownerSignature;
    }

    //GETTERS/SETTERS

    //PARSE
    // TODO - когда нулевая длдлинна и ошибка - но в ГУИ ошибка нне высветилась и создалась плоая запись и она развалила сеть
    // includeReference - TRUE only for store in local DB
    public static PersonHuman parse(byte[] data, boolean includeReference) throws Exception {

        // READ TYPE
        byte[] typeBytes = Arrays.copyOfRange(data, 0, TYPE_LENGTH);
        int position = TYPE_LENGTH;

        //READ CREATOR
        byte[] ownerBytes = Arrays.copyOfRange(data, position, position + OWNER_LENGTH);
        PublicKeyAccount owner = new PublicKeyAccount(ownerBytes);
        position += OWNER_LENGTH;

        //READ FULL NAME
        int fullNameLength = Byte.toUnsignedInt(data[position]);
        position++;

        if (fullNameLength < 1 || fullNameLength > MAX_NAME_LENGTH) {
            throw new Exception("Invalid full name length");
        }

        byte[] fullNameBytes = Arrays.copyOfRange(data, position, position + fullNameLength);
        String fullName = new String(fullNameBytes, StandardCharsets.UTF_8);
        position += fullNameLength;

        //READ ICON
        byte[] iconLengthBytes = Arrays.copyOfRange(data, position, position + ICON_SIZE_LENGTH);
        int iconLength = Ints.fromBytes((byte) 0, (byte) 0, iconLengthBytes[0], iconLengthBytes[1]);
        position += ICON_SIZE_LENGTH;

        if (iconLength < 0 || iconLength > MAX_ICON_LENGTH) {
            throw new Exception("Invalid icon length - " + iconLength);
        }

        byte[] icon = Arrays.copyOfRange(data, position, position + iconLength);
        position += iconLength;

        //READ IMAGE
        byte[] imageLengthBytes = Arrays.copyOfRange(data, position, position + IMAGE_SIZE_LENGTH);
        int imageLength = Ints.fromByteArray(imageLengthBytes);
        position += IMAGE_SIZE_LENGTH;

        if (imageLength < 0 || imageLength > ItemCls.MAX_IMAGE_LENGTH) {
            throw new Exception("Invalid image length " + imageLength);
        }

        byte[] image = Arrays.copyOfRange(data, position, position + imageLength);
        position += imageLength;

        //READ DESCRIPTION
        byte[] descriptionLengthBytes = Arrays.copyOfRange(data, position, position + DESCRIPTION_SIZE_LENGTH);
        int descriptionLength = Ints.fromByteArray(descriptionLengthBytes);
        position += DESCRIPTION_SIZE_LENGTH;

        if (descriptionLength < 0 || descriptionLength > BlockChain.MAX_REC_DATA_BYTES) {
            throw new Exception("Invalid description length");
        }

        byte[] descriptionBytes = Arrays.copyOfRange(data, position, position + descriptionLength);
        String description = new String(descriptionBytes, StandardCharsets.UTF_8);
        position += descriptionLength;

        byte[] reference = null;
        if (includeReference) {
            //READ REFERENCE
            reference = Arrays.copyOfRange(data, position, position + REFERENCE_LENGTH);
            position += REFERENCE_LENGTH;
        }

        //READ BIRTDAY
        byte[] birthdayBytes = Arrays.copyOfRange(data, position, position + BIRTHDAY_LENGTH);
        long birthday = Longs.fromByteArray(birthdayBytes);
        position += BIRTHDAY_LENGTH;

        //READ DEATHDAY
        byte[] deathdayBytes = Arrays.copyOfRange(data, position, position + DEATHDAY_LENGTH);
        long deathday = Longs.fromByteArray(deathdayBytes);
        position += DEATHDAY_LENGTH;

        //READ GENDER
        byte gender = data[position];
        position++;

        //READ RACE
        int raceLength = Byte.toUnsignedInt(data[position]);
        position++;

        if (raceLength < 0 || raceLength > MAX_RACE_LENGTH) {
            throw new Exception("Invalid race length");
        }

        byte[] raceBytes = Arrays.copyOfRange(data, position, position + raceLength);
        String race = new String(raceBytes, StandardCharsets.UTF_8);
        position += raceLength;

        //READ BIRTH LATITUDE
        float birthLatitude = ByteArrayUtils.ByteArray2float(Arrays.copyOfRange(data, position, position + LATITUDE_LENGTH));
        position += LATITUDE_LENGTH;

        //READ BIRTH LONGITUDE
        float birthLongitude = ByteArrayUtils.ByteArray2float(Arrays.copyOfRange(data, position, position + LATITUDE_LENGTH));
        position += LATITUDE_LENGTH;

        //READ SKIN COLOR LENGTH
        int skinColorLength = Byte.toUnsignedInt(data[position]);
        position++;

        if (skinColorLength < 0 || skinColorLength > MAX_SKIN_COLOR_LENGTH) {
            throw new Exception("Invalid skin color length");
        }

        byte[] skinColorBytes = Arrays.copyOfRange(data, position, position + skinColorLength);
        String skinColor = new String(skinColorBytes, StandardCharsets.UTF_8);
        position += skinColorLength;

        //READ EYE COLOR LENGTH
        int eyeColorLength = Byte.toUnsignedInt(data[position]);
        position++;

        if (eyeColorLength < 0 || eyeColorLength > MAX_EYE_COLOR_LENGTH) {
            throw new Exception("Invalid eye color length");
        }

        byte[] eyeColorBytes = Arrays.copyOfRange(data, position, position + eyeColorLength);
        String eyeColor = new String(eyeColorBytes, StandardCharsets.UTF_8);
        position += eyeColorLength;

        //READ HAIR COLOR LENGTH
        int hairСolorLength = Byte.toUnsignedInt(data[position]);
        position++;

        if (hairСolorLength < 0 || hairСolorLength > MAX_HAIR_COLOR_LENGTH) {
            throw new Exception("Invalid hair color length");
        }

        byte[] hairСolorBytes = Arrays.copyOfRange(data, position, position + hairСolorLength);
        String hairСolor = new String(hairСolorBytes, StandardCharsets.UTF_8);
        position += hairСolorLength;

        //READ HEIGHT
        byte height = data[position];
        position++;

        byte[] ownerSignature;
        if (typeBytes[1] == 1) {
            // with signature
            //READ SIGNATURE
            ownerSignature = Arrays.copyOfRange(data, position, position + Transaction.SIGNATURE_LENGTH);
            position += Transaction.SIGNATURE_LENGTH;
        } else {
            ownerSignature = null;
        }

        //RETURN
        PersonHuman personHuman = new PersonHuman(typeBytes, owner, fullName, birthday, deathday,
                gender, race, birthLatitude, birthLongitude,
                skinColor, eyeColor, hairСolor, height, icon, image, description, ownerSignature);

        if (includeReference) {
            personHuman.setReference(reference);
        }

        return personHuman;
    }

    public String getItemSubType() {
        return "human";
    }

    public int getMinNameLen() {
        return 5;
    }

    public byte[] getOwnerSignature() {
        return ownerSignature;
    }

    public boolean isMustBeSigned() {
        return typeBytes[1] == (byte) 1;
    }

    public byte[] toBytes(boolean includeReference, boolean onlyBody) {
        byte[] data = super.toBytes(includeReference, onlyBody);
        if (this.typeBytes[1] == 1) {
            data = Bytes.concat(data, this.ownerSignature);
        }

        return data;
    }

    @SuppressWarnings("unchecked")
    public JSONObject toJson() {

        JSONObject personJSON = super.toJson();

        if (this.ownerSignature != null) {
            personJSON.put("ownerSignature", Base58.encode(this.ownerSignature));
        }

        return personJSON;
    }

    public int getDataLength(boolean includeReference) {
        return super.getDataLength(includeReference)
                + (typeBytes[1] == 1 ? Transaction.SIGNATURE_LENGTH : 0);
    }

    //
    public void sign(PrivateKeyAccount owner) {

        if (!Arrays.equals(owner.getPublicKey(), this.owner.getPublicKey())) {
            //this.typeBytes[1] = (byte)0;
            return;
        }

        boolean includeReference = false;
        // not use SIGNATURE here
        boolean forOwnerSign = true;
        byte[] data = super.toBytes(includeReference, forOwnerSign);
        if (data == null) {
            //this.typeBytes[1] = (byte)0;
            return;
        }

        this.ownerSignature = Crypto.getInstance().sign(owner, data);
        this.typeBytes[1] = (byte) 1;

    }

    public boolean isSignatureValid(DCSet dcSet) {

        if (this.ownerSignature == null || this.ownerSignature.length != Crypto.SIGNATURE_LENGTH
                || Arrays.equals(this.ownerSignature, new byte[Crypto.SIGNATURE_LENGTH]))
            return false;

        if (dcSet.getBlocksHeadsMap().size() < BlockChain.SKIP_VALID_SIGN_BEFORE) {
            // for skip NOT VALID SIGNs
            for (byte[] valid_item : BlockChain.VALID_SIGN) {
                if (Arrays.equals(this.reference, valid_item)) {
                    if (dcSet.getTransactionFinalMapSigns().contains(this.reference))
                        return false;
                    else
                        return true;
                }
            }
        }

        boolean includeReference = false;
        boolean forOwnerSign = true;
        // not use SIGNATURE here
        byte[] data = super.toBytes(includeReference, forOwnerSign);
        if (data == null)
            return false;

        return Crypto.getInstance().verify(this.owner.getPublicKey(), this.ownerSignature, data);
    }

}
