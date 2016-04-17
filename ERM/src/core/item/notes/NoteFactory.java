package qora.item.notes;

// import org.apache.log4j.Logger;

//import com.google.common.primitives.Ints;
//import com.google.common.primitives.Longs;

import qora.item.notes.NoteCls;

public class NoteFactory {

	private static NoteFactory instance;
	
	public static NoteFactory getInstance()
	{
		if(instance == null)
		{
			instance = new NoteFactory();
		}
		
		return instance;
	}
	
	private NoteFactory()
	{
		
	}
	
	public NoteCls parse(byte[] data, boolean includeReference) throws Exception
	{
		//READ TYPE
		int type = data[0];
				
		switch(type)
		{
		case NoteCls.NOTE:
			
			//PARSE SIMPLE NOTE
			return Note.parse(data, includeReference);
						
		case NoteCls.SAMPLE:

			//
			//return Note.parse(data, includeReference);

		case NoteCls.PAPER:
				
			//
			//return Note.parse(data, includeReference);
		}

		throw new Exception("Invalid Note type: " + type);
	}
	
}
