package gui.items.persons;

import java.awt.Color;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JEditorPane;
import javax.swing.JTextPane;

import org.mapdb.Fun.Tuple3;
import org.mapdb.Fun.Tuple4;

import core.item.persons.PersonCls;
import core.item.statuses.StatusCls;
import core.item.unions.UnionCls;
import database.DBSet;
import gui.MainFrame;
import lang.Lang;

// Info for person
public class Person_Info extends JTextPane {
	private String message = "<HTML>" + Lang.getInstance().translate("Select person");
	
public  Person_Info() {

	this.setContentType("text/html");
	this.setText(message); //"<HTML>" + Lang.getInstance().translate("Select person")); // Document text is provided below.
	//this.setBackground(this.getBackground());
	//this.setBackground(new Color(211,211,211));
	this.setBackground(MainFrame.getFrames()[0].getBackground());
	
	
	
	
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
	

	if (person.isConfirmed()) {
		message ="<html><div>#" + "<b>" + person.getKey() + "</b> : " + person.getName().toString() + "</br>";

		date_birthday =  formatDate.format(new Date(Long.valueOf(person.getBirthday())));
		message += date_birthday;
		if ( person.getBirthday() < person.getDeathday())
			message += "..." + formatDate.format(new Date(Long.valueOf(person.getDeathday())));

		message += "</br>";

		message += "<h2>"+ "Statuses" +"</h2>";

		// GET CERTIFIED ACCOUNTS
		message += "<h2>"+ "Accounts" +"</h2>";
		TreeMap<String, java.util.Stack<Tuple3<Integer, Integer, Integer>>> addresses= DBSet.getInstance().getPersonAddressMap().getItems(person.getKey());
		if ( !addresses.isEmpty()){
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
	
		
	if (person.isConfirmed()){
		String Date_birs = formatDate.format(new Date(Long.valueOf(person.getBirthday())));
		 message ="<html><div></div><div> <p><b>" + Lang.getInstance().translate("Key")+":"   + person.getKey()        			+ "</p>"
		+ "<p> <b> "  + Lang.getInstance().translate("Name")+":"       			  + person.getName().toString()		+ "</p>" 
        + "<p> "  + Lang.getInstance().translate("Birthday")  +":"        	      + Date_birs			+"</p>"
        ;
		 // Читаем адреса клиента
		 TreeMap<String, java.util.Stack<Tuple3<Integer, Integer, Integer>>> Addresses= DBSet.getInstance().getPersonAddressMap().getItems(person.getKey());
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
