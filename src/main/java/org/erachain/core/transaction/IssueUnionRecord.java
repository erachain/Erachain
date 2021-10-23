package org.erachain.core.transaction;

import com.google.common.primitives.Longs;
import org.erachain.core.BlockChain;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.exdata.exLink.ExLink;
import org.erachain.core.item.unions.UnionCls;
import org.erachain.core.item.unions.UnionFactory;
import org.erachain.smartcontracts.SmartContract;

import java.math.BigDecimal;
import java.util.Arrays;

public class IssueUnionRecord extends IssueItemRecord {
    public static final byte TYPE_ID = (byte) ISSUE_UNION_TRANSACTION;
    public static final String TYPE_NAME = "Issue Union";

    public IssueUnionRecord(byte[] typeBytes, PublicKeyAccount creator, ExLink linkTo, UnionCls union, byte feePow, long timestamp, Long reference) {
        super(typeBytes, TYPE_NAME, creator, null, union, feePow, timestamp, reference);
    }

    public IssueUnionRecord(byte[] typeBytes, PublicKeyAccount creator, ExLink linkTo, UnionCls union, byte feePow, long timestamp, Long reference, byte[] signature) {
        super(typeBytes, TYPE_NAME, creator, linkTo, union, feePow, timestamp, reference, signature);
    }

    public IssueUnionRecord(byte[] typeBytes, PublicKeyAccount creator, ExLink linkTo, UnionCls union, byte feePow, long timestamp,
                            Long reference, byte[] signature, long seqNo, long feeLong) {
        super(typeBytes, TYPE_NAME, creator, linkTo, union, feePow, timestamp, reference, signature);
        if (seqNo > 0)
            this.setHeightSeq(seqNo);
        this.fee = BigDecimal.valueOf(feeLong, BlockChain.FEE_SCALE);
    }

    public IssueUnionRecord(byte[] typeBytes, PublicKeyAccount creator, ExLink linkTo, UnionCls union, byte[] signature) {
        super(typeBytes, TYPE_NAME, creator, linkTo, union, (byte) 0, 0l, null, signature);
    }

    public IssueUnionRecord(PublicKeyAccount creator, UnionCls union, byte feePow, long timestamp, Long reference, byte[] signature) {
        this(new byte[]{TYPE_ID, 0, 0, 0}, creator, null, union, feePow, timestamp, reference, signature);
    }

    public IssueUnionRecord(PublicKeyAccount creator, ExLink linkTo, UnionCls union, byte feePow, long timestamp, Long reference) {
        this(new byte[]{TYPE_ID, 0, 0, 0}, creator, linkTo, union, feePow, timestamp, reference);
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

        //READ REFERENCE
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

        SmartContract smartContract;
        if ((typeBytes[2] & HAS_SMART_CONTRACT_MASK) > 0) {
            smartContract = SmartContract.Parses(data, position, forDeal);
            position += smartContract.length(forDeal);
        } else {
            smartContract = null;
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

        //READ UNION
        // union parse without reference - if is = signature
        UnionCls union = UnionFactory.getInstance().parse(forDeal, Arrays.copyOfRange(data, position, data.length), false);
        position += union.getDataLength(false);

        if (forDeal == FOR_DB_RECORD) {
            //READ KEY
            byte[] timestampBytes = Arrays.copyOfRange(data, position, position + KEY_LENGTH);
            long key = Longs.fromByteArray(timestampBytes);
            position += KEY_LENGTH;

            union.setKey(key);

        }

        if (forDeal > Transaction.FOR_MYPACK) {
            return new IssueUnionRecord(typeBytes, creator, linkTo, union, feePow, timestamp, flagsTX, signatureBytes, seqNo, feeLong);
        } else {
            return new IssueUnionRecord(typeBytes, creator, linkTo, union, signatureBytes);
        }
    }

    //PARSE CONVERT

    //@Override
    public int isValid(int forDeal, long flags) {

        if (height < BlockChain.ALL_VALID_BEFORE) {
            return VALIDATE_OK;
        }

        int result = super.isValid(forDeal, flags);
        if (result != Transaction.VALIDATE_OK) return result;

		/*
		BigDecimal balERA = this.creator.getBalanceUSE(RIGHTS_KEY, db);
		if ( balERA.compareTo(BlockChain.MAJOR_ERA_BALANCE_BD)<0 )
		{
			return Transaction.NOT_ENOUGH_RIGHTS;
		}
		*/

        return Transaction.VALIDATE_OK;
    }

    //PROCESS/ORPHAN

}
