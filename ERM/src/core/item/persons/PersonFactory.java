package qora.item.persons;

import java.util.Arrays;
 import org.apache.log4j.Logger;

import com.google.common.primitives.Ints;
//import com.google.common.primitives.Longs;

import qora.item.persons.PersonCls;

public class PersonFactory {

	private static PersonFactory instance;
	
	public static PersonFactory getInstance()
	{
		if(instance == null)
		{
			instance = new PersonFactory();
		}
		
		return instance;
	}
	
	private PersonFactory()
	{
		
	}
	
	public PersonCls parse(byte[] data, boolean includeReference) throws Exception
	{
		//READ TYPE
		int type = data[0];
				
		switch(type)
		{
		case PersonCls.HUMAN:
			
			//PARSE SIMPLE NOTE
			return PersonHuman.parse(data, includeReference);
						
		case PersonCls.DOG:
				
			//
			//return Person.parse(data, includeReference);
		case PersonCls.CAT:
			//
		}

		throw new Exception("Invalid Person type: " + type);
	}
	
}
