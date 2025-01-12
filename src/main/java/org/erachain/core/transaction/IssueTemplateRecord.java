package org.erachain.core.transaction;

import com.google.common.primitives.Longs;
import org.erachain.core.BlockChain;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.exdata.exLink.ExLink;
import org.erachain.core.item.templates.TemplateCls;
import org.erachain.core.item.templates.TemplateFactory;
import org.erachain.dapp.DApp;

import java.math.BigDecimal;
import java.util.Arrays;

public class IssueTemplateRecord extends IssueItemRecord {
    public static final byte TYPE_ID = (byte) ISSUE_TEMPLATE_TRANSACTION;
    public static final String TYPE_NAME = "Issue Template";

    public IssueTemplateRecord(byte[] typeBytes, PublicKeyAccount creator, ExLink linkTo, TemplateCls template, byte feePow, long timestamp, long flags) {
        super(typeBytes, TYPE_NAME, creator, linkTo, template, feePow, timestamp, flags);
    }

    public IssueTemplateRecord(byte[] typeBytes, PublicKeyAccount creator, ExLink linkTo, TemplateCls template, byte feePow, long timestamp, long flags, byte[] signature) {
        super(typeBytes, TYPE_NAME, creator, linkTo, template, feePow, timestamp, flags, signature);
    }

    public IssueTemplateRecord(byte[] typeBytes, PublicKeyAccount creator, ExLink linkTo, TemplateCls template, byte feePow,
                               long timestamp, long flags, byte[] signature, long seqNo, long feeLong) {
        super(typeBytes, TYPE_NAME, creator, linkTo, template, feePow, timestamp, flags, signature);
        if (seqNo > 0)
            this.setHeightSeq(seqNo);
        this.fee = BigDecimal.valueOf(feeLong, BlockChain.FEE_SCALE);
    }

    public IssueTemplateRecord(byte[] typeBytes, PublicKeyAccount creator, ExLink linkTo, TemplateCls template, byte[] signature) {
        super(typeBytes, TYPE_NAME, creator, linkTo, template, (byte) 0, 0L, 0L, signature);
    }

    public IssueTemplateRecord(PublicKeyAccount creator, TemplateCls template, byte feePow, long timestamp, long flags, byte[] signature) {
        this(new byte[]{TYPE_ID, 0, 0, 0}, creator, null, template, feePow, timestamp, flags, signature);
    }

    public IssueTemplateRecord(PublicKeyAccount creator, TemplateCls template, byte[] signature) {
        this(new byte[]{TYPE_ID, 0, 0, 0}, creator, null, template, (byte) 0, 0L, 0L, signature);
    }

    public IssueTemplateRecord(PublicKeyAccount creator, ExLink linkTo, TemplateCls template, byte feePow, long timestamp, long flags) {
        this(new byte[]{TYPE_ID, 0, 0, 0}, creator, linkTo, template, feePow, timestamp, flags);
    }

    public IssueTemplateRecord(PublicKeyAccount creator, TemplateCls template) {
        this(new byte[]{TYPE_ID, 0, 0, 0}, creator, null, template, (byte) 0, 0L, 0L);
    }

    //GETTERS/SETTERS

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
            throw new Exception("Data does not match RAW length " + data.length + " < " + test_len);
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

        //READ FLAGS
        byte[] flagsBytes = Arrays.copyOfRange(data, position, position + FLAGS_LENGTH);
        long flagsTX = Longs.fromByteArray(flagsBytes);
        position += FLAGS_LENGTH;

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

        DApp dapp;
        if ((typeBytes[2] & HAS_SMART_CONTRACT_MASK) > 0) {
            dapp = DApp.Parses(data, position, forDeal);
            position += dapp.length(forDeal);
        } else {
            dapp = null;
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

        //READ PLATE
        // template parse without reference - if is = signature
        TemplateCls template = TemplateFactory.getInstance().parse(forDeal, Arrays.copyOfRange(data, position, data.length), false);
        position += template.getDataLength(false);

        if (forDeal == FOR_DB_RECORD) {
            //READ KEY
            byte[] timestampBytes = Arrays.copyOfRange(data, position, position + KEY_LENGTH);
            long key = Longs.fromByteArray(timestampBytes);
            position += KEY_LENGTH;

            template.setKey(key);

        }

        if (forDeal > Transaction.FOR_MYPACK) {
            return new IssueTemplateRecord(typeBytes, creator, linkTo, template, feePow, timestamp, flagsTX, signatureBytes, seqNo, feeLong);
        } else {
            return new IssueTemplateRecord(typeBytes, creator, linkTo, template, signatureBytes);
        }
    }

    //PARSE CONVERT

    //@Override
    public int isValid(int forDeal, long checkFlags) {

        if (height < BlockChain.ALL_VALID_BEFORE) {
            return VALIDATE_OK;
        }

        int result = super.isValid(forDeal, checkFlags);
        if (result != Transaction.VALIDATE_OK) return result;

        return Transaction.VALIDATE_OK;
    }

    //VALIDATE


    //PROCESS/ORPHAN


}
