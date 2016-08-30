package ntp;

import java.net.InetAddress;
import org.apache.log4j.Logger;
import java.sql.Timestamp;

import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;

import lang.Lang;
import network.message.MessageFactory;

public final class NTP
{
	private static final long TIME_TILL_UPDATE = 1000*60*10;
	private static final String NTP_SERVER = "pool.ntp.org";
	
	private static long lastUpdate = 0;
	private static long offset = 0;
   
	static Logger LOGGER = Logger.getLogger(NTP.class.getName());

	/*
	public static long getTime()
	{
		getTime_old();
		java.util.Date date= new java.util.Date();
		return new Timestamp(date.getTime()).getTime() + offset;
		
	}
	*/

	// TODO - if offset is GREAT!
	public static long getTime()
	{
		//CHECK IF OFFSET NEEDS TO BE UPDATED
		// NOT NEED NOW - random offset is GOOD now 
		if(System.currentTimeMillis() > lastUpdate + TIME_TILL_UPDATE)
		{
			updateOffSet();
			lastUpdate = System.currentTimeMillis();
			
			if (offset != 0l) {
				//LOG OFFSET
				LOGGER.info(Lang.getInstance().translate("Adjusting time with %offset% milliseconds.").replace("%offset%", String.valueOf(offset)));
			}
		}
	   
		//CALCULATE CORRECTED TIME
		return System.currentTimeMillis() + offset;
	}
   
	private static void updateOffSet()
	{
		//CREATE CLIENT
		NTPUDPClient client = new NTPUDPClient();
	   
		//SET TIMEOUT
		client.setDefaultTimeout(10000);
		try 
		{
			//OPEN CLIENT
			client.open();
          
			//GET INFO FROM NTP SERVER
			InetAddress hostAddr = InetAddress.getByName(NTP_SERVER);
			TimeInfo info = client.getTime(hostAddr);
			info.computeDetails();
           
			//UPDATE OFFSET
			Long offsetResult = info.getOffset();
			if(offsetResult != null 
					&& (offsetResult < -30000 || offsetResult > 30000))
			{
				// and add random Nonce for generate BLOCK
				offset = offsetResult + (int)(Math.random() * 20000) * (int)Math.signum((float)offsetResult);
			} 
		} 
		catch (Exception e) 
		{
    	   	//ERROR GETTING OFFSET
		}

		client.close(); 
   }
}
