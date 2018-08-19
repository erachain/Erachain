package core.transaction;

import com.google.common.primitives.Longs;
import core.account.PublicKeyAccount;
import core.item.unions.UnionCls;
import core.item.unions.UnionFactory;

import java.util.Arrays;

//import java.util.Map;
// import org.apache.log4j.Logger;

public class IssueUnionRecord extends Issue_ItemRecord {
    private static final byte TYPE_ID = (byte) ISSUE_UNION_TRANSACTION;
    private static final String NAME_ID = "Issue Union";

    public IssueUnionRecord(byte[] typeBytes, PublicKeyAccount creator, UnionCls union, byte feePow, long timestamp, Long reference) {
        super(typeBytes, NAME_ID, creator, union, feePow, timestamp, reference);
    }

    public IssueUnionRecord(byte[] typeBytes, PublicKeyAccount creator, UnionCls union, byte feePow, long timestamp, Long reference, byte[] signature) {
        super(typeBytes, NAME_ID, creator, union, feePow, timestamp, reference, signature);
    }

    public IssueUnionRecord(byte[] typeBytes, PublicKeyAccount creator, UnionCls union, byte[] signature) {
        super(typeBytes, NAME_ID, creator, union, (byte) 0, 0l, null, signature);
    }

    public IssueUnionRecord(PublicKeyAccount creator, UnionCls union, byte feePow, long timestamp, Long reference, byte[] signature) {
        this(new byte[]{TYPE_ID, 0, 0, 0}, creator, union, feePow, timestamp, reference, signature);
    }

    public IssueUnionRecord(PublicKeyAccount creator, UnionCls union, byte[] signature) {
        this(new byte[]{TYPE_ID, 0, 0, 0}, creator, union, (byte) 0, 0l, null, signature);
    }

    public IssueUnionRecord(PublicKeyAccount creator, UnionCls union, byte feePow, long timestamp, Long reference) {
        this(new byte[]{TYPE_ID, 0, 0, 0}, creator, union, feePow, timestamp, reference);
    }

    public IssueUnionRecord(PublicKeyAccount creator, UnionCls union) {
        this(new byte[]{TYPE_ID, 0, 0, 0}, creator, union, (byte) 0, 0l, null);
    }

    //GETTERS/SETTERS
    //public static String getName() { return "Issue Union"; }

    public static Transaction Parse(byte[] data, int asDeal) throws Exception {

        int test_len = BASE_LENGTH;
        if (asDeal == Transaction.FOR_MYPACK) {
            test_len -= Transaction.TIMESTAMP_LENGTH + Transaction.FEE_POWER_LENGTH;
        } else if (asDeal == Transaction.FOR_PACK) {
            test_len -= Transaction.TIMESTAMP_LENGTH;
        } else if (asDeal == Transaction.FOR_DB_RECORD) {
            test_len += Transaction.FEE_POWER_LENGTH;
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

        //READ UNION
        // union parse without reference - if is = signature
        UnionCls union = UnionFactory.getInstance().parse(Arrays.copyOfRange(data, position, data.length), false);
        position += union.getDataLength(false);

        if (!asPack) {
            return new IssueUnionRecord(typeBytes, creator, union, feePow, timestamp, reference, signatureBytes);
        } else {
            return new IssueUnionRecord(typeBytes, creator, union, signatureBytes);
        }
    }

    // NOT GENESIS ISSUE STRT FRON NUM
    protected long getStartKey() {
        return 10000l;
    }

    //PARSE CONVERT

    //@Override
    public int isValid(Long releaserReference, long flags) {

        int result = super.isValid(releaserReference, flags);
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

	/*
	@Override
	public int calcBaseFee() {
		return 10 * (calcCommonFee() + BlockChain.FEE_PER_BYTE * 1000);
	}
	*/

}
