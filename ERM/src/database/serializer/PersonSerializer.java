package database.serializer;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;

import org.apache.log4j.Logger;
import org.mapdb.Serializer;

import qora.item.persons.PersonCls;
import qora.item.persons.PersonFactory;

public class PersonSerializer implements Serializer<PersonCls>, Serializable
{
	private static final long serialVersionUID = -6538913048331349777L;
	static Logger LOGGER = Logger.getLogger(PersonSerializer.class.getName());

	@Override
	public void serialize(DataOutput out, PersonCls value) throws IOException 
	{
		out.writeInt(value.getDataLength(true));
        out.write(value.toBytes(true));
    }

    @Override
    public PersonCls deserialize(DataInput in, int available) throws IOException 
    {
    	int length = in.readInt();
        byte[] bytes = new byte[length];
        in.readFully(bytes);
        try 
        {
        	return PersonFactory.getInstance().parse(bytes, true);
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
