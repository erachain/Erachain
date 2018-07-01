package core.transCalculated;

import org.json.simple.JSONObject;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Longs;

/*

счетчик вычисленных трнзакций для источника

 */

public abstract class CalculatedCounter extends Calculated {
            
    protected static final int COUNTER_LENGTH = 8;

    protected long counter;

    protected CalculatedCounter(byte[] typeBytes, String type_name, Integer blockNo, Integer transNo, long seq,
            long counter) {
        super(typeBytes, type_name, blockNo, transNo, seq);
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
    
    @SuppressWarnings("unchecked")
    protected JSONObject getJsonBase() {
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
