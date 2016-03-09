package database.serializer;
// upd 09/03
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;
import java.util.logging.Logger;


import org.mapdb.Serializer;

import qora.transaction.Transaction;
import qora.transaction.TransactionFactory;

public class TransactionSerializer implements Serializer<Transaction>, Serializable
{
	private static final long serialVersionUID = -6538913048331349777L;

	@Override
	public void serialize(DataOutput out, Transaction value) throws IOException 
	{
		////Logger.getGlobal().info("serialize tx type: " + value.getType() + "  data len:" +  value.getDataLength());
		out.writeInt(value.getDataLength());
        out.write(value.toBytes(true));
    }

    @Override
    public Transaction deserialize(DataInput in, int available) throws IOException 
    {
    	int length = in.readInt();
        byte[] bytes = new byte[length];
        in.readFully(bytes);
        try 
        {
    		////Logger.getGlobal().info("deserialize  data len:" +  bytes.length);
        	return TransactionFactory.getInstance().parse(bytes);
		} 
        catch (Exception e) 
        {
        	e.printStackTrace();
		}
		return null;
    }

    @Override
    public int fixedSize() 
    {
    	return -1;
    }
}
