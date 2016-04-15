package database.serializer;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;

import org.apache.log4j.Logger;
import org.mapdb.Serializer;

import qora.item.unions.UnionCls;
import qora.item.unions.UnionFactory;

public class UnionSerializer implements Serializer<UnionCls>, Serializable
{
	private static final long serialVersionUID = -6538913048331349777L;
	static Logger LOGGER = Logger.getLogger(UnionSerializer.class.getName());

	@Override
	public void serialize(DataOutput out, UnionCls value) throws IOException 
	{
		out.writeInt(value.getDataLength(true));
        out.write(value.toBytes(true));
    }

    @Override
    public UnionCls deserialize(DataInput in, int available) throws IOException 
    {
    	int length = in.readInt();
        byte[] bytes = new byte[length];
        in.readFully(bytes);
        try 
        {
        	return UnionFactory.getInstance().parse(bytes, true);
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
