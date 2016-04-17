package database.serializer;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;

import org.apache.log4j.Logger;
import org.mapdb.Serializer;

import core.item.assets.AssetCls;
import core.item.assets.AssetFactory;
import database.TransactionDatabase;

public class AssetSerializer implements Serializer<AssetCls>, Serializable
{
	private static final long serialVersionUID = -6538913048331349777L;
	
	static Logger LOGGER = Logger.getLogger(AssetSerializer.class.getName());


	@Override
	public void serialize(DataOutput out, AssetCls value) throws IOException 
	{
		out.writeInt(value.getDataLength(true));
        out.write(value.toBytes(true));
    }

    @Override
    public AssetCls deserialize(DataInput in, int available) throws IOException 
    {
    	int length = in.readInt();
        byte[] bytes = new byte[length];
        in.readFully(bytes);
        try 
        {
        	return AssetFactory.getInstance().parse(bytes, true);
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
