package gui.items.persons;

import java.awt.Color;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Stack;
import java.util.TreeMap;

import javax.swing.JEditorPane;
import javax.swing.JTextPane;

import org.mapdb.Fun.Tuple3;
import org.mapdb.Fun.Tuple4;

import core.item.persons.PersonCls;
import core.item.statuses.StatusCls;
import core.item.unions.UnionCls;
import database.ItemStatusMap;
import database.DBSet;
import gui.MainFrame;
import lang.Lang;

// Info for person
public class Person_Info extends JTextPane {
	private static final long serialVersionUID = 4763074704570450206L;	
	
	public  Person_Info() {
	
		this.setContentType("text/html");
		this.setBackground(MainFrame.getFrames()[0].getBackground());
		
	}
	
	
	static String Get_HTML_Person_Info_001(PersonCls person)
	{
		 
		String message = "";
		String dateAlive;
		String date_birthday;
		SimpleDateFormat formatDate = new SimpleDateFormat("dd.MM.yyyy"); // HH:mm");
		
		if (person == null) return message += "Empty Person";
		
		if (!person.isConfirmed()) {
			message = Lang.getInstance().translate("Not confirmed");
		} else {
			message = "" + person.getKey();
		}
		message = "<div><b>" + message + "</b> : " + person.getName().toString() + "</div>";
	
		date_birthday =  formatDate.format(new Date(Long.valueOf(person.getBirthday())));
		message += "<div>" + date_birthday;
		if ( person.getBirthday() < person.getDeathday())
			message += " - " + formatDate.format(new Date(Long.valueOf(person.getDeathday())));
		message += "</div>";
						
		// GET CERTIFIED ACCOUNTS
		TreeMap<String, java.util.Stack<Tuple3<Integer, Integer, Integer>>> addresses= DBSet.getInstance().getPersonAddressMap().getItems(person.getKey());
		if ( addresses.isEmpty()){
			message += "<p>" +  Lang.getInstance().translate("Not personalized")+ "</p";
		} else {
			message += "<h3>"+ Lang.getInstance().translate("Personalized Accounts") +"</h3>";
			// for each account seek active date
			String active_date_str;
			for( Map.Entry<String, java.util.Stack<Tuple3<Integer, Integer, Integer>>> e : addresses.entrySet())
			{
				Tuple3<Integer, Integer, Integer> active_date = e.getValue().peek();
				if (active_date.a == 0) active_date_str = "active";
				else active_date_str = formatDate.format( new Date(active_date.a * (long)86400000));
				
				message += "<div><input  style='background: #00ffff'; type='text' size='33' value='"+ e.getKey() +"' disabled='disabled' class='disabled' onchange =''>"
						+ " -> <b>" + active_date_str +"</b></div>";
			}
		}	
		
		TreeMap<Long, Stack<Tuple4<Long, Long, Integer, Integer>>> statuses = DBSet.getInstance().getPersonStatusMap().get(person.getKey());
		if ( statuses.isEmpty()){
			message += "<p>" +  Lang.getInstance().translate("Not statuses")+ "</p";
		} else {
			message += "<h3>"+ "Statuses" +"</h3>";
			String from_date_str;
			String to_date_str;
			Long dte;
			ItemStatusMap statusesMap = DBSet.getInstance().getItemStatusMap();
			for( Map.Entry<Long, java.util.Stack<Tuple4<Long, Long, Integer, Integer>>> status: statuses.entrySet())
			{
				message += "<div>" + statusesMap.get(status.getKey()).toString() + " : ";
				
				Tuple4<Long, Long, Integer, Integer> dates = status.getValue().peek();
				
				dte = dates.a;
				if (dte == null || dte == Long.MIN_VALUE) from_date_str = " ? ";
				else from_date_str = formatDate.format( new Date(dte));
				
				dte = dates.b;
				if (dte == null || dte == Long.MAX_VALUE) to_date_str = " ? ";
				else to_date_str = formatDate.format( new Date(dte));
				
				message += from_date_str + " - " + to_date_str + "</div";

			}
			
		}

		return message;
	}
	 	
	public void show_001(PersonCls person){
		
		setText("<html>" + Get_HTML_Person_Info_001(person) + "</html>");
		return;
	}	

}
