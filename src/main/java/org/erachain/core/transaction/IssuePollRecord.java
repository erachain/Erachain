package org.erachain.core.transaction;

import com.google.common.primitives.Longs;
import org.erachain.core.BlockChain;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.item.polls.PollCls;
import org.erachain.core.item.polls.PollFactory;

import java.math.BigDecimal;
import java.util.Arrays;

public class IssuePollRecord extends IssueItemRecord {
    private static final byte TYPE_ID = (byte) ISSUE_POLL_TRANSACTION;
    private static final String NAME_ID = "Issue Poll";

    // TODO: в старой версии с 1 - 2 первый номер будет - надо скинуть в  0 или 1000000L
    public static final long START_KEY = 1000L;

    public IssuePollRecord(byte[] typeBytes, PublicKeyAccount creator, PollCls poll, byte feePow, long timestamp, Long reference) {
        super(typeBytes, NAME_ID, creator, poll, feePow, timestamp, reference);
    }

    public IssuePollRecord(byte[] typeBytes, PublicKeyAccount creator, PollCls poll, byte feePow, long timestamp, Long reference, byte[] signature) {
        super(typeBytes, NAME_ID, creator, poll, feePow, timestamp, reference, signature);
    }

    public IssuePollRecord(byte[] typeBytes, PublicKeyAccount creator, PollCls poll, byte feePow, long timestamp,
                           Long reference, byte[] signature, long feeLong) {
        super(typeBytes, NAME_ID, creator, poll, feePow, timestamp, reference, signature);
        this.fee = BigDecimal.valueOf(feeLong, BlockChain.AMOUNT_DEDAULT_SCALE);
    }

    public IssuePollRecord(byte[] typeBytes, PublicKeyAccount creator, PollCls poll, byte[] signature) {
        super(typeBytes, NAME_ID, creator, poll, (byte) 0, 0l, null, signature);
    }

    public IssuePollRecord(PublicKeyAccount creator, PollCls poll, byte feePow, long timestamp, Long reference, byte[] signature) {
        this(new byte[]{TYPE_ID, 0, 0, 0}, creator, poll, feePow, timestamp, reference, signature);
    }

    public IssuePollRecord(PublicKeyAccount creator, PollCls poll, byte[] signature) {
        this(new byte[]{TYPE_ID, 0, 0, 0}, creator, poll, (byte) 0, 0l, null, signature);
    }

    public IssuePollRecord(PublicKeyAccount creator, PollCls poll, byte feePow, long timestamp, Long reference) {
        this(new byte[]{TYPE_ID, 0, 0, 0}, creator, poll, feePow, timestamp, reference);
    }

    public IssuePollRecord(PublicKeyAccount creator, PollCls poll) {
        this(new byte[]{TYPE_ID, 0, 0, 0}, creator, poll, (byte) 0, 0l, null);
    }

    //GETTERS/SETTERS

    // RETURN START KEY in tot GEMESIS

    @Override
    public long getStartKey(int height) {
        return START_KEY;
    }

    public static Transaction Parse(byte[] data, int asDeal) throws Exception {

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

        //READ POLL
        // poll parse without reference - if is = signature
        PollCls poll = PollFactory.getInstance().parse(Arrays.copyOfRange(data, position, data.length), false);
        position += poll.getDataLength(false);

        if (asDeal > Transaction.FOR_MYPACK) {
            return new IssuePollRecord(typeBytes, creator, poll, feePow, timestamp, reference, signatureBytes, feeLong);
        } else {
            return new IssuePollRecord(typeBytes, creator, poll, signatureBytes);
        }
    }

    //PARSE CONVERT

    //PROCESS/ORPHAN

}
