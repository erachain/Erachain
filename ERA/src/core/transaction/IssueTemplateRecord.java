package core.transaction;

import com.google.common.primitives.Longs;
import core.account.PublicKeyAccount;
import core.item.templates.TemplateCls;
import core.item.templates.TemplateFactory;

import java.util.Arrays;

//import java.util.Map;
// import org.apache.log4j.Logger;

public class IssueTemplateRecord extends Issue_ItemRecord {
    private static final byte TYPE_ID = (byte) ISSUE_TEMPLATE_TRANSACTION;
    private static final String NAME_ID = "Issue Template";

    public IssueTemplateRecord(byte[] typeBytes, PublicKeyAccount creator, TemplateCls template, byte feePow, long timestamp, Long reference) {
        super(typeBytes, NAME_ID, creator, template, feePow, timestamp, reference);
    }

    public IssueTemplateRecord(byte[] typeBytes, PublicKeyAccount creator, TemplateCls template, byte feePow, long timestamp, Long reference, byte[] signature) {
        super(typeBytes, NAME_ID, creator, template, feePow, timestamp, reference, signature);
    }

    public IssueTemplateRecord(byte[] typeBytes, PublicKeyAccount creator, TemplateCls template, byte[] signature) {
        super(typeBytes, NAME_ID, creator, template, (byte) 0, 0l, null, signature);
    }

    public IssueTemplateRecord(PublicKeyAccount creator, TemplateCls template, byte feePow, long timestamp, Long reference, byte[] signature) {
        this(new byte[]{TYPE_ID, 0, 0, 0}, creator, template, feePow, timestamp, reference, signature);
    }

    public IssueTemplateRecord(PublicKeyAccount creator, TemplateCls template, byte[] signature) {
        this(new byte[]{TYPE_ID, 0, 0, 0}, creator, template, (byte) 0, 0l, null, signature);
    }

    public IssueTemplateRecord(PublicKeyAccount creator, TemplateCls template, byte feePow, long timestamp, Long reference) {
        this(new byte[]{TYPE_ID, 0, 0, 0}, creator, template, feePow, timestamp, reference);
    }

    public IssueTemplateRecord(PublicKeyAccount creator, TemplateCls template) {
        this(new byte[]{TYPE_ID, 0, 0, 0}, creator, template, (byte) 0, 0l, null);
    }

    //GETTERS/SETTERS
    //public static String getName() { return "Issue Template"; }

    public static Transaction Parse(byte[] data, Long releaserReference) throws Exception {

        boolean asPack = releaserReference != null;

        //CHECK IF WE MATCH BLOCK LENGTH
        if (data.length < BASE_LENGTH_AS_PACK
                | !asPack & data.length < BASE_LENGTH) {
            throw new Exception("Data does not match block length " + data.length);
        }

        // READ TYPE
        byte[] typeBytes = Arrays.copyOfRange(data, 0, TYPE_LENGTH);
        int position = TYPE_LENGTH;

        long timestamp = 0;
        if (!asPack) {
            //READ TIMESTAMP
            byte[] timestampBytes = Arrays.copyOfRange(data, position, position + TIMESTAMP_LENGTH);
            timestamp = Longs.fromByteArray(timestampBytes);
            position += TIMESTAMP_LENGTH;
        }

        Long reference = null;
        if (!asPack) {
            //READ REFERENCE
            byte[] referenceBytes = Arrays.copyOfRange(data, position, position + REFERENCE_LENGTH);
            reference = Longs.fromByteArray(referenceBytes);
            position += REFERENCE_LENGTH;
        }

        //READ CREATOR
        byte[] creatorBytes = Arrays.copyOfRange(data, position, position + CREATOR_LENGTH);
        PublicKeyAccount creator = new PublicKeyAccount(creatorBytes);
        position += CREATOR_LENGTH;

        byte feePow = 0;
        if (!asPack) {
            //READ FEE POWER
            byte[] feePowBytes = Arrays.copyOfRange(data, position, position + 1);
            feePow = feePowBytes[0];
            position += 1;
        }

        //READ SIGNATURE
        byte[] signatureBytes = Arrays.copyOfRange(data, position, position + SIGNATURE_LENGTH);
        position += SIGNATURE_LENGTH;

        //READ PLATE
        // template parse without reference - if is = signature
        TemplateCls template = TemplateFactory.getInstance().parse(Arrays.copyOfRange(data, position, data.length), false);
        position += template.getDataLength(false);

        if (!asPack) {
            return new IssueTemplateRecord(typeBytes, creator, template, feePow, timestamp, reference, signatureBytes);
        } else {
            return new IssueTemplateRecord(typeBytes, creator, template, signatureBytes);
        }
    }

    // NOT GENESIS ISSUE STRT FRON NUM
    protected long getStartKey() {
        return 1000l;
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

    //VALIDATE


    //PROCESS/ORPHAN

	/*
	@Override
	public int calcBaseFee() {
		return 10 * (calcCommonFee() + BlockChain.FEE_PER_BYTE * 1000);
	}
	*/

}
