package org.erachain.core.item.imprints;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import org.erachain.core.BlockChain;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.crypto.Base58;
import org.erachain.core.crypto.Crypto;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

//import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

public class Imprint extends ImprintCls {

    private static final int TYPE_ID = IMPRINT;
    private static final int CUTTED_REFERENCE_LENGTH = 20;

    public Imprint(PublicKeyAccount owner, String name, byte[] icon, byte[] image, String description) {
        super(TYPE_ID, owner, name, icon, image, description);
        this.reference = Bytes.ensureCapacity(Base58.decode(name), REFERENCE_LENGTH, 0);

    }

    public Imprint(byte[] typeBytes, PublicKeyAccount owner, String name, byte[] icon, byte[] image, String description) {
        super(typeBytes, owner, name, icon, image, description);
        this.reference = Bytes.ensureCapacity(Base58.decode(name), REFERENCE_LENGTH, 0);
    }

    public static String hashNameToBase58(String name_total) {
        byte[] digest = Crypto.getInstance().digest(name_total.getBytes());
        digest = Arrays.copyOfRange(digest, 0, Imprint.CUTTED_REFERENCE_LENGTH);
        return Base58.encode(digest);
    }

    //PARSE
    // includeReference - TRUE only for store in local DB
    public static Imprint parse(byte[] data, boolean includeReference) throws Exception {

        // READ TYPE
        byte[] typeBytes = Arrays.copyOfRange(data, 0, TYPE_LENGTH);
        int position = TYPE_LENGTH;

        //READ CREATOR
        byte[] ownerBytes = Arrays.copyOfRange(data, position, position + OWNER_LENGTH);
        PublicKeyAccount owner = new PublicKeyAccount(ownerBytes);
        position += OWNER_LENGTH;

        //READ NAME
        //byte[] nameLengthBytes = Arrays.copyOfRange(data, position, position + NAME_SIZE_LENGTH);
        //int nameLength = Ints.fromByteArray(nameLengthBytes);
        //position += NAME_SIZE_LENGTH;
        int nameLength = Byte.toUnsignedInt(data[position]);
        position++;

        if (nameLength < CUTTED_REFERENCE_LENGTH || nameLength > CUTTED_REFERENCE_LENGTH * 2) {
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

        if (imageLength < 0 || imageLength > MAX_IMAGE_LENGTH) {
            throw new Exception("Invalid image length");
        }

        byte[] image = Arrays.copyOfRange(data, position, position + imageLength);
        position += imageLength;

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
        if (includeReference) {
            //READ REFERENCE
            reference = Arrays.copyOfRange(data, position, position + REFERENCE_LENGTH);
            position += REFERENCE_LENGTH;
        }

        //RETURN
        Imprint imprint = new Imprint(typeBytes, owner, name, icon, image, description);
        if (includeReference) {
            imprint.setReference(reference);
        }

        return imprint;
    }

    //GETTERS/SETTERS
    public String getItemSubType() {
        return "cutted58";
    }

    public int getMinNameLen() {
        return 20;
    }

    public byte[] getCuttedReference() {
        return Arrays.copyOfRange(this.reference, 0, CUTTED_REFERENCE_LENGTH);
    }

    //@Override
    public int getDataLength(boolean includeReference) {
        return super.getDataLength(includeReference)
                - this.name.getBytes(StandardCharsets.UTF_8).length // remove UTF8
                + this.name.getBytes().length; // it is Base58 - not UTF
    }


}
