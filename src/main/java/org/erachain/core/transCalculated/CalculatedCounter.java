package org.erachain.core.transCalculated;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import org.json.simple.JSONObject;

import java.util.Arrays;

/*

счетчик вычисленных трнзакций для источника

 */

public class CalculatedCounter extends Calculated {
            
    protected static final int COUNTER_LENGTH = 8;
    private static final String NAME_ID = "Counter";

    protected long counter;

    protected CalculatedCounter(byte[] typeBytes, Integer blockNo, Integer transNo, long seq,
            long counter) {
        super(typeBytes, NAME_ID, blockNo, transNo, seq);
        this.counter = counter;
    }
        
    // GETTERS/SETTERS
            
    public long getCounter() {
        return this.counter;
    }
        
    public String getStr() {
        return "calcsCounter";
    }
        
    
    /*
     * ************** VIEW
     */
                
    // PARSE/CONVERT
    public byte[] toBytes() {
        
        byte[] data = super.toBytes();
        
        // WRITE COUNTER
        byte[] bytes = Longs.toByteArray(this.counter);
        bytes = Bytes.ensureCapacity(bytes, COUNTER_LENGTH, 0);
        data = Bytes.concat(data, bytes);
        
        return data;
    }

    public static Calculated Parse(byte[] data) throws Exception {

        int test_len = BASE_LENGTH;
        if (data.length < test_len) {
            throw new Exception("Data does not match RAW length " + data.length + " < " + test_len);
        }

        // READ TYPE
        byte[] typeBytes = Arrays.copyOfRange(data, 0, TYPE_LENGTH);
        int position = TYPE_LENGTH;

        // READ BLOCK NO
        byte[] bytes = Arrays.copyOfRange(data, position, position + BLOCK_NO_LENGTH);
        int blockNo = Ints.fromByteArray(bytes);
        position += BLOCK_NO_LENGTH;

        // READ TRANS NO
        bytes = Arrays.copyOfRange(data, position, position + TRANS_NO_LENGTH);
        int transNo = Ints.fromByteArray(bytes);
        position += TRANS_NO_LENGTH;

        // READ SEQ NO
        bytes = Arrays.copyOfRange(data, position, position + SEQ_NO_LENGTH);
        long seqNo = Longs.fromByteArray(bytes);
        position += SEQ_NO_LENGTH;

        // READ COUNTER
        bytes = Arrays.copyOfRange(data, position, position + COUNTER_LENGTH);
        long counter = Longs.fromByteArray(bytes);
        position += COUNTER_LENGTH;

        return new CalculatedCounter(typeBytes, blockNo, transNo, seqNo,
                counter);

    }

    @SuppressWarnings("unchecked")
    public JSONObject toJson() {
        JSONObject transaction = super.getJsonBase();
        
        transaction.put("counter", this.counter);
        
        return transaction;
    }
        
    public int getDataLength() {
        return BASE_LENGTH + COUNTER_LENGTH;
    }
        
    public void process() {                
    }
    
    public void orphan() {
    }
    
}
