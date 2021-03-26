package org.erachain.core.item.polls;

//import java.math.BigDecimal;

import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import org.erachain.core.BlockChain;
import org.erachain.core.account.PublicKeyAccount;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Poll extends PollCls {

    private static final int TYPE_ID = POLL;

    public Poll(long[] flags, PublicKeyAccount maker, String name, byte[] icon, byte[] image, String description, List<String> options) {
        super(TYPE_ID, flags, maker, name, icon, image, description, options);
    }

    public Poll(byte[] typeBytes, long[] flags, PublicKeyAccount maker, String name, byte[] icon, byte[] image, String description, List<String> options) {
        super(typeBytes, flags, maker, name, icon, image, description, options);
    }

    //PARSE
    // includeReference - TRUE only for store in local DB
    public static Poll parse(byte[] data, boolean includeReference) throws Exception {

        // READ TYPE
        byte[] typeBytes = Arrays.copyOfRange(data, 0, TYPE_LENGTH);
        int position = TYPE_LENGTH;

        //READ CREATOR
        byte[] makerBytes = Arrays.copyOfRange(data, position, position + MAKER_LENGTH);
        PublicKeyAccount maker = new PublicKeyAccount(makerBytes);
        position += MAKER_LENGTH;

        //READ NAME
        //byte[] nameLengthBytes = Arrays.copyOfRange(data, position, position + NAME_SIZE_LENGTH);
        //int nameLength = Ints.fromByteArray(nameLengthBytes);
        //position += NAME_SIZE_LENGTH;
        int nameLength = Byte.toUnsignedInt(data[position]);
        position++;

        if (nameLength < 1 || nameLength > MAX_NAME_LENGTH) {
            throw new Exception("Invalid name length");
        }

        byte[] nameBytes = Arrays.copyOfRange(data, position, position + nameLength);
        String name = new String(nameBytes, StandardCharsets.UTF_8);
        position += nameLength;

        //READ ICON
        byte[] iconLengthBytes = Arrays.copyOfRange(data, position, position + ICON_SIZE_LENGTH);
        int iconLength = Ints.fromBytes((byte) 0, (byte) 0, iconLengthBytes[0], iconLengthBytes[1]);
        position += ICON_SIZE_LENGTH;

        if (iconLength < 0 || iconLength > MAX_ICON_LENGTH) {
            throw new Exception("Invalid icon length");
        }

        byte[] icon = Arrays.copyOfRange(data, position, position + iconLength);
        position += iconLength;

        //READ IMAGE
        byte[] imageLengthBytes = Arrays.copyOfRange(data, position, position + IMAGE_SIZE_LENGTH);
        int imageLength = Ints.fromByteArray(imageLengthBytes);
        position += IMAGE_SIZE_LENGTH;

        // TEST FLAGS
        boolean hasFlags = (imageLength & FLAGS_MASK) != 0;
        if (hasFlags)
            // RESET LEN
            imageLength *= -1;

        if (imageLength < 0 || imageLength > MAX_IMAGE_LENGTH) {
            throw new Exception("Invalid image length" + name + ": " + imageLength);
        }

        byte[] image = Arrays.copyOfRange(data, position, position + imageLength);
        position += imageLength;

        long[] flags;
        if (hasFlags) {
            byte flagsBytesLen = Arrays.copyOfRange(data, position, ++position)[0];
            flags = new long[flagsBytesLen];

            for (int i = 0; i < flagsBytesLen; i++) {
                byte[] flagsBytes = Arrays.copyOfRange(data, position, position + FLAGS_LENGTH);
                flags[i] = Longs.fromByteArray(flagsBytes);
                position += FLAGS_LENGTH;
            }
        } else {
            flags = null;
        }

        //READ DESCRIPTION
        byte[] descriptionLengthBytes = Arrays.copyOfRange(data, position, position + DESCRIPTION_SIZE_LENGTH);
        int descriptionLength = Ints.fromByteArray(descriptionLengthBytes);
        position += DESCRIPTION_SIZE_LENGTH;

        if (descriptionLength > BlockChain.MAX_REC_DATA_BYTES) {
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

        //READ OPTIONS SIZE
        byte[] optionsLengthBytes = Arrays.copyOfRange(data, position, position + OPTIONS_SIZE_LENGTH);
        int optionsLength = Ints.fromByteArray(optionsLengthBytes);
        position += OPTIONS_SIZE_LENGTH;

        //READ OPTIONS
        List<String> options = new ArrayList<String>();
        for (int i = 0; i < optionsLength; i++) {

            nameLength = Byte.toUnsignedInt(data[position]);
            position++;

            byte[] optionBytes = Arrays.copyOfRange(data, position, position + nameLength);
            String option = new String(optionBytes, StandardCharsets.UTF_8);
            position += nameLength;

            options.add(option);
        }

        //RETURN
        Poll poll = new Poll(typeBytes, flags, maker, name, icon, image, description, options);
        if (includeReference) {
            poll.setReference(reference, dbRef);
        }

        return poll;
    }

    //GETTERS/SETTERS
    public String getItemSubType() {
        return "poll";
    }

    @Override
    public int getMinNameLen() {
        return 12;
    }

}
