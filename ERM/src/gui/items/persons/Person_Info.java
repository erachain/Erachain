package gui.items.persons;

import java.awt.Color;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JEditorPane;

import org.mapdb.Fun.Tuple3;

import core.item.persons.PersonCls;
import core.item.statuses.StatusCls;
import database.DBSet;
import lang.Lang;

// Info for person
public class Person_Info extends JEditorPane {
	private String message = "<HTML>" + Lang.getInstance().translate("Select person");
	
public  Person_Info() {

	this.setContentType("text/html");
	this.setText(message); //"<HTML>" + Lang.getInstance().translate("Select person")); // Document text is provided below.
	this.setBackground(new Color(255, 255, 255, 0));
	
	
	
}
	
 public String Get_HTML_Person_Info_001(PersonCls person){
	
	String dateAlive;
	String date_birthday;
	// TODO Auto-generated method stub
	// устанавливаем формат даты
	SimpleDateFormat formatDate = new SimpleDateFormat("dd.MM.yyyy"); // HH:mm");
	//создаем объект персоны
//	PersonCls person;
			

	if (person != null){// personsTable.getSelectedRow() >= 0 ){
	//	person = tableModelPersons.getPerson(personsTable.convertRowIndexToModel(personsTable.getSelectedRow()));
	

	if (person.isConfirmed()){
		date_birthday=  formatDate.format(new Date(Long.valueOf(person.getBirthday())));
		message ="<html><div>#" + "<b>" + person.getKey() + " : " + date_birthday + "</b>"
		+ "<br>" + person.getName().toString() + "</div>";

		message += "<h2>"+ "Statuses" +"</h2>";
		// GETT PERSON STATUS for ALIVE
		Tuple3<Long, Integer, byte[]> t3Alive = DBSet.getInstance().getPersonStatusMap().getItem(person.getKey(), StatusCls.ALIVE_KEY);

		if (t3Alive != null){
			if (t3Alive.a == null) dateAlive = "active";
			else dateAlive = formatDate.format( new Date(t3Alive.a));
		} else
		{
			dateAlive = Lang.getInstance().translate("unknown");
		}
		message += "<div>" + Lang.getInstance().translate("ALIVE")+": <b>" + dateAlive +"</b></div>";

		// GETT PERSON STATUS for DEAD
		Tuple3<Long, Integer, byte[]> t3Dead = DBSet.getInstance().getPersonStatusMap().getItem(person.getKey(), StatusCls.DEAD_KEY);

		if (t3Dead != null){
			if (t3Dead.a == null) dateAlive = "yes";
			else dateAlive = formatDate.format( new Date(t3Dead.a));
		} else
		{
			dateAlive = Lang.getInstance().translate("unknown");
		}
		message += "<div style='color:#ffff00'>" + Lang.getInstance().translate("DEAD")+": <b>" + dateAlive +"</b></div>";

		// GET CERTIFIED ACCOUNTS
		message += "<h2>"+ "Accounts" +"</h2>";
		TreeMap<String, java.util.Stack<Tuple3<Integer, Integer, byte[]>>> addresses= DBSet.getInstance().getPersonAddressMap().getItems(person.getKey());
		if ( !addresses.isEmpty()){
			// for each account seek active date
			String active_date_str;
			for( Map.Entry<String, java.util.Stack<Tuple3<Integer, Integer, byte[]>>> e : addresses.entrySet())
			{
				Tuple3<Integer, Integer, byte[]> active_date = e.getValue().peek();
				if (active_date.a == 0) active_date_str = "active";
				else active_date_str = formatDate.format( new Date(active_date.a * (long)86400000));
				
				message += "<div><input  style='background: #00ffff'; type='text' size='33' value='"+ e.getKey() +"' disabled='disabled' class='disabled' onchange =''>"
						+ " -> <b>" + active_date_str +"</b></div>";
			}
		}
		else{
			message += "<p> " +  Lang.getInstance().translate("Account not found!")+ "</p";
		}					
	} else {
		message = "<html><p>"+ Lang.getInstance().translate("Not found!") +"</p>";	
	}
	message = message + "</html>";
	
	
	}	
	return message;
}
 
  public String Get_HTML_Person_Info_002(PersonCls person)	{
		
	
	  String Date_Acti; 
		
	  SimpleDateFormat formatDate = new SimpleDateFormat("dd.MM.yyyy"); // HH:mm");
	  if (person != null){ //if (table.getSelectedRow() >= 0 ){
		//person = personsModel.getItem(table.convertRowIndexToModel(table.getSelectedRow()));
	
	
//читаем таблицу персон.
	Tuple3<Long, Integer, byte[]> t3 = DBSet.getInstance().getPersonStatusMap().getItem(person.getKey(), StatusCls.ALIVE_KEY); //(Long) personsTable.getValueAt(personsTable.getSelectedRow(),0));
// преобразование в дату


	
	if (t3 != null){
		if (t3.a == null) Date_Acti = "+";
		else Date_Acti = formatDate.format( new Date(Long.valueOf(t3.a.toString())));
	} else
	{
		Date_Acti =Lang.getInstance().translate("Not found!");
	};
	
	if (person.isConfirmed()){
		String Date_birs = formatDate.format(new Date(Long.valueOf(person.getBirthday())));
		 message ="<html><div></div><div> <p><b>" + Lang.getInstance().translate("Key")+":"   + person.getKey()        			+ "</p>"
		+ "<p> <b> "  + Lang.getInstance().translate("Name")+":"       			  + person.getName().toString()		+ "</p>" 
        + "<p> "  + Lang.getInstance().translate("Birthday")  +":"        	      + Date_birs			+"</p>"
        + "<p>  "  + Lang.getInstance().translate("To Date")  +":"        		  + Date_Acti			+"</p>"
        ;
		 // Читаем адреса клиента
		 TreeMap<String, java.util.Stack<Tuple3<Integer, Integer, byte[]>>> Addresses= DBSet.getInstance().getPersonAddressMap().getItems(person.getKey());
		 if ( !Addresses.isEmpty()){
			 message =message + "<p>"  + Lang.getInstance().translate("Account")  +":  <input type='text' size='' value='"+ Addresses.lastKey() +"' id='iiii' name='nnnn' class= 'cccc' onchange =''><p></div>";
		 }
		 else{
			 message = message + "<p> " +  Lang.getInstance().translate("Account not found!")+ "</p";
										 }
	}else{
		
		message = "<html><p>"+ Lang.getInstance().translate("Not found!") +"</></>";	
	}
	message = message + "</html>";
  }
	 
	return  message;
  }

public void show_001(PersonCls person){
	
	setText(new Person_Info().Get_HTML_Person_Info_001(person));
	return;
}
public void show_002(PersonCls person){
	
	setText(new Person_Info().Get_HTML_Person_Info_002(person));
	return;
}

}
