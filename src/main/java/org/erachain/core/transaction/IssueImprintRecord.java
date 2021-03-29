package org.erachain.core.transaction;

import com.google.common.primitives.Longs;
import org.erachain.core.BlockChain;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.block.Block;
import org.erachain.core.exdata.exLink.ExLink;
import org.erachain.core.item.ItemCls;
import org.erachain.core.item.imprints.Imprint;
import org.erachain.core.item.imprints.ImprintCls;

import java.math.BigDecimal;
import java.util.Arrays;

// reference - as item.name
// TODO - reference NOT NEED - because it is unique record! - make it as new version protocol
public class IssueImprintRecord extends IssueItemRecord {
    //protected static final int BASE_LENGTH_AS_PACK = Transaction.BASE_LENGTH_AS_PACK;

    protected static final int BASE_LENGTH_AS_MYPACK = Transaction.BASE_LENGTH_AS_MYPACK - REFERENCE_LENGTH;
    protected static final int BASE_LENGTH_AS_PACK = Transaction.BASE_LENGTH_AS_PACK - REFERENCE_LENGTH;
    protected static final int BASE_LENGTH = Transaction.BASE_LENGTH - REFERENCE_LENGTH;
    protected static final int BASE_LENGTH_AS_DBRECORD = Transaction.BASE_LENGTH_AS_DBRECORD - REFERENCE_LENGTH;

    public static final byte TYPE_ID = (byte) ISSUE_IMPRINT_TRANSACTION;
    public static final String TYPE_NAME = "Issue Imprint";


    public IssueImprintRecord(byte[] typeBytes, PublicKeyAccount creator, ImprintCls imprint, byte feePow, long timestamp) {
        super(typeBytes, TYPE_NAME, creator, null, imprint, feePow, timestamp, null);
    }

    public IssueImprintRecord(byte[] typeBytes, PublicKeyAccount creator, ExLink linkTo, ImprintCls imprint, byte feePow, long timestamp, byte[] signature) {
        super(typeBytes, TYPE_NAME, creator, linkTo, imprint, feePow, timestamp, null, signature);
    }

    public IssueImprintRecord(byte[] typeBytes, PublicKeyAccount creator, ExLink linkTo, ImprintCls imprint, byte feePow,
                              long timestamp, byte[] signature, long seqNo, long feeLong) {
        super(typeBytes, TYPE_NAME, creator, linkTo, imprint, feePow, timestamp, null, signature);
        this.fee = BigDecimal.valueOf(feeLong, BlockChain.FEE_SCALE);
        if (seqNo > 0)
            this.setHeightSeq(seqNo);
    }

    // asPack
    public IssueImprintRecord(byte[] typeBytes, PublicKeyAccount creator, ExLink linkTo, ImprintCls imprint, byte[] signature) {
        super(typeBytes, TYPE_NAME, creator, linkTo, imprint, (byte) 0, 0L, null, signature);
    }

    public IssueImprintRecord(PublicKeyAccount creator, ImprintCls imprint, byte feePow, long timestamp, byte[] signature) {
        this(new byte[]{TYPE_ID, 0, 0, 0}, creator, null, imprint, feePow, timestamp, signature);
    }

    public IssueImprintRecord(PublicKeyAccount creator, ExLink linkTo, ImprintCls imprint, byte feePow, long timestamp) {
        this(new byte[]{TYPE_ID, 0, 0, 0}, creator, linkTo, imprint, feePow, timestamp, null);
    }

    //GETTERS/SETTERS
    //public static String getName() { return "Issue Imprint"; }

    int minLen = 200 * 25;

    @Override
    public long calcBaseFee(boolean withFreeProtocol) {

        int len = this.getDataLength(Transaction.FOR_NETWORK, true);

        if (this.height > BlockChain.USE_NEW_ISSUE_FEE) {
            if (len < minLen)
                len = minLen;
        }

        return len * BlockChain.FEE_PER_BYTE;
    }

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
        //byte[] referenceBytes = Arrays.copyOfRange(data, position, position + REFERENCE_LENGTH);
        //Long reference = Longs.fromByteArray(referenceBytes);
        //position += REFERENCE_LENGTH;

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

        //READ IMPRINT
        // imprint parse without reference - if is = signature
        ImprintCls imprint = Imprint.parse(forDeal, Arrays.copyOfRange(data, position, data.length), false);
        position += imprint.getDataLength(false);

        if (forDeal == FOR_DB_RECORD) {
            //READ KEY
            byte[] timestampBytes = Arrays.copyOfRange(data, position, position + KEY_LENGTH);
            long key = Longs.fromByteArray(timestampBytes);
            position += KEY_LENGTH;

            imprint.setKey(key);

        }

        if (forDeal > Transaction.FOR_MYPACK) {
            return new IssueImprintRecord(typeBytes, creator, linkTo, imprint, feePow, timestamp, signatureBytes, seqNo, feeLong);
        } else {
            return new IssueImprintRecord(typeBytes, creator, linkTo, imprint, signatureBytes);
        }
    }

    @Override
    public boolean hasPublicText() {
        String description = item.getDescription();
        return description != null && description.length() < 300;
    }


    //PARSE CONVERT

    //VALIDATE
    //
    @Override
    public int isValid(int forDeal, long flags) {

        if (height < BlockChain.ALL_VALID_BEFORE) {
            return VALIDATE_OK;
        }

        //CHECK NAME LENGTH
        ItemCls item = this.getItem();
        int nameLength = item.getName().getBytes().length;
        if (nameLength < item.getMinNameLen()) {
            return INVALID_NAME_LENGTH_MIN;
        }
        if (nameLength > 40) {
            return INVALID_NAME_LENGTH_MAX;
        }

        int result = super.isValid(forDeal, flags);
        if (result != Transaction.VALIDATE_OK) return result;

        // CHECK reference in DB
        if (dcSet.getTransactionFinalMapSigns().contains(((ImprintCls) item).hashName())) {
            return Transaction.ITEM_DUPLICATE_KEY;
        }

        return Transaction.VALIDATE_OK;

    }

    @Override
    public int getDataLength(int forDeal, boolean withSignature) {
        // not include item reference

        int base_len;
        if (forDeal == FOR_MYPACK)
            base_len = BASE_LENGTH_AS_MYPACK;
        else if (forDeal == FOR_PACK)
            base_len = BASE_LENGTH_AS_PACK;
        else if (forDeal == FOR_DB_RECORD)
            base_len = BASE_LENGTH_AS_DBRECORD + KEY_LENGTH;
        else
            base_len = BASE_LENGTH;

        if (exLink != null)
            base_len += exLink.length();

        if (!withSignature)
            base_len -= SIGNATURE_LENGTH;

        return base_len + this.getItem().getDataLength(false);

    }

    //PROCESS/ORPHAN

    @Override
    public void process(Block block, int forDeal) {
        //UPDATE CREATOR
        super.process(block, forDeal);

        dcSet.getTransactionFinalMapSigns().put(((ImprintCls) item).hashName(), dbRef);

    }

    //@Override
    @Override
    public void orphan(Block block, int forDeal) {
        //UPDATE CREATOR
        super.orphan(block, forDeal);

        dcSet.getTransactionFinalMapSigns().delete(((ImprintCls) item).hashName());

    }

}
