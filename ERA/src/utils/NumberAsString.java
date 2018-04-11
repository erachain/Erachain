package utils;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import org.json.simple.parser.ParseException;

public class NumberAsString {
	private static NumberAsString instance;
	private DecimalFormat decimalFormat;
	private DecimalFormat decimalFormat12;
	
	public static NumberAsString getInstance()
	{
		if(instance == null)
		{
			instance = new NumberAsString();
		}
		
		return instance;
	}
	
	public NumberAsString()
	{
		Locale locale = new Locale("en", "US");
		DecimalFormatSymbols symbols = new DecimalFormatSymbols(locale);
		symbols.setDecimalSeparator('.');
		symbols.setGroupingSeparator(',');
		
		decimalFormat = new DecimalFormat("###,##0.00000000", symbols);

		String ss = "";
		for (int i=0; i< 12; i++) {
			ss += "0";
		}
		decimalFormat12 = new DecimalFormat("###,##0." + ss, symbols);
	}
	
	public String numberAsString(Object amount) {
		return decimalFormat.format(amount);
	}
	public String numberAsString12(Object amount) {
		return decimalFormat12.format(amount);
	}
}
