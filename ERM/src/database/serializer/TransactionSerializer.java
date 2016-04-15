package database.serializer;
// upd 09/03
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;
 import org.apache.log4j.Logger;


import org.mapdb.Serializer;

import qora.transaction.Transaction;
import qora.transaction.TransactionFactory;

public class TransactionSerializer implements Serializer<Transaction>, Serializable
{
	private static final long serialVersionUID = -6538913048331349777L;
	static Logger LOGGER = Logger.getLogger(TransactionSerializer.class.getName());

	@Override
	public void serialize(DataOutput out, Transaction value) throws IOException 
	{
		out.writeInt(value.getDataLength(false));
        out.write(value.toBytes(true, null));
    }

    @Override
    public Transaction deserialize(DataInput in, int available) throws IOException 
    {
    	int length = in.readInt();
        byte[] bytes = new byte[length];
        in.readFully(bytes);
        try 
        {
        	return TransactionFactory.getInstance().parse(bytes, null);
		} 
        catch (Exception e) 
        {
        	LOGGER.error(e.getMessage(),e);
		}
		return null;
    }

    @Override
    public int fixedSize() 
    {
    	return -1;
    }
}
