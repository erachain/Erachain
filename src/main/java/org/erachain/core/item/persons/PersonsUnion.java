package org.erachain.core.item.persons;

//import java.math.BigDecimal;

import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import org.erachain.core.BlockChain;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.item.ItemCls;
import org.erachain.utils.ByteArrayUtils;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class PersonsUnion extends PersonCls {

    private static final int TYPE_ID = UNION;

    public PersonsUnion(byte[] appData, PublicKeyAccount maker, String fullName, long birthday, long deathday,
                        byte gender, String race, float birthLatitude, float birthLongitude,
                        String skinColor, String eyeColor, String hairСolor, int height, byte[] icon, byte[] image, String description) {
        super(new byte[]{(byte) TYPE_ID, (byte) 0}, appData, maker, fullName, birthday, deathday,
                gender, race, birthLatitude, birthLongitude,
                skinColor, eyeColor, hairСolor, (byte) height, icon, image, description);
    }

    public PersonsUnion(byte[] appData, PublicKeyAccount maker, String fullName, String birthday, String deathday,
                        byte gender, String race, float birthLatitude, float birthLongitude,
                        String skinColor, String eyeColor, String hairСolor, int height, byte[] icon, byte[] image, String description) {
        super(new byte[]{(byte) TYPE_ID, (byte) 0}, appData, maker, fullName, birthday, deathday,
                gender, race, birthLatitude, birthLongitude,
                skinColor, eyeColor, hairСolor, (byte) height, icon, image, description);
    }

    public PersonsUnion(byte[] typeBytes, byte[] appData, PublicKeyAccount maker, String fullName, long birthday, long deathday,
                        byte gender, String race, float birthLatitude, float birthLongitude,
                        String skinColor, String eyeColor, String hairСolor, int height, byte[] icon, byte[] image,
                        String description) {
        super(typeBytes, appData, maker, fullName, birthday, deathday,
                gender, race, birthLatitude, birthLongitude,
                skinColor, eyeColor, hairСolor, (byte) height, icon, image, description);
    }

    //GETTERS/SETTERS

    //PARSE
    // TODO - когда нулевая длдлинна и ошибка - но в ГУИ ошибка нне высветилась и создалась плоая запись и она развалила сеть
    // includeReference - TRUE only for store in local DB
    public static PersonsUnion parse(byte[] data, boolean includeReference, int forDeal) throws Exception {

        // READ TYPE
        byte[] typeBytes = Arrays.copyOfRange(data, 0, TYPE_LENGTH);
        int position = TYPE_LENGTH;

        //READ CREATOR
        byte[] makerBytes = Arrays.copyOfRange(data, position, position + MAKER_LENGTH);
        PublicKeyAccount maker = new PublicKeyAccount(makerBytes);
        position += MAKER_LENGTH;

        //READ FULL NAME
        int fullNameLength = Byte.toUnsignedInt(data[position]);
        position++;

        // !!! Проверяем по максимуму протокола - по супер классу ItemCls. Локальные ограничения в isValid тут
        if (fullNameLength < 1 || fullNameLength > ItemCls.MAX_NAME_LENGTH) {
            throw new Exception("Invalid full name length");
        }

        byte[] fullNameBytes = Arrays.copyOfRange(data, position, position + fullNameLength);
        String name = new String(fullNameBytes, StandardCharsets.UTF_8);
        position += fullNameLength;

        //READ ICON
        byte[] iconLengthBytes = Arrays.copyOfRange(data, position, position + ICON_SIZE_LENGTH);
        int iconLength = Ints.fromBytes((byte) 0, (byte) 0, iconLengthBytes[0], iconLengthBytes[1]);
        position += ICON_SIZE_LENGTH;

        // !!! Проверяем по максимуму протокола - по супер классу ItemCls. Локальные ограничения в isValid тут
        if (iconLength < 0 || iconLength > ItemCls.MAX_ICON_LENGTH) {
            throw new Exception("Invalid icon length - " + iconLength);
        }

        byte[] icon = Arrays.copyOfRange(data, position, position + iconLength);
        position += iconLength;

        //READ IMAGE
        byte[] imageLengthBytes = Arrays.copyOfRange(data, position, position + IMAGE_SIZE_LENGTH);
        int imageLength = Ints.fromByteArray(imageLengthBytes);
        position += IMAGE_SIZE_LENGTH;

        // TEST APP DATA
        boolean hasAppData = (imageLength & APP_DATA_MASK) != 0;
        if (hasAppData)
            // RESET LEN
            imageLength &= ~APP_DATA_MASK;

        // !!! Проверяем по максимуму протокола - по супер классу ItemCls. Локальные ограничения в isValid тут
        if (imageLength < 0 || imageLength > ItemCls.MAX_IMAGE_LENGTH) {
            throw new Exception("Invalid image length " + imageLength);
        }

        byte[] image = Arrays.copyOfRange(data, position, position + imageLength);
        position += imageLength;

        byte[] appData;
        if (hasAppData) {
            // READ APP DATA
            int appDataLen = Ints.fromByteArray(Arrays.copyOfRange(data, position, position + APP_DATA_LENGTH));
            position += APP_DATA_LENGTH;

            appData = Arrays.copyOfRange(data, position, position + appDataLen);
            position += appDataLen;

        } else {
            appData = null;
        }

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
        long dbRef = 0;
        if (includeReference) {
            //READ REFERENCE
            reference = Arrays.copyOfRange(data, position, position + REFERENCE_LENGTH);
            position += REFERENCE_LENGTH;

            //READ SEQNO
            byte[] dbRefBytes = Arrays.copyOfRange(data, position, position + DBREF_LENGTH);
            dbRef = Longs.fromByteArray(dbRefBytes);
            position += DBREF_LENGTH;
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

        //RETURN
        PersonsUnion personsUnion = new PersonsUnion(typeBytes, appData, maker, name, birthday, deathday,
                gender, race, birthLatitude, birthLongitude,
                skinColor, eyeColor, hairСolor, height, icon, image, description);

        if (includeReference) {
            personsUnion.setReference(reference, dbRef);
        }

        return personsUnion;
    }

    public String getItemSubType() {
        return "union";
    }

    public int getMinNameLen() {
        return 2;
    }

}
