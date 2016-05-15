package gui.items.unions;

import java.awt.Color;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JEditorPane;

import org.mapdb.Fun.Tuple3;

import core.item.unions.UnionCls;
import core.item.statuses.StatusCls;
import core.item.unions.UnionCls;
import database.DBSet;
import lang.Lang;

// Info for union
public class Union_Info extends JEditorPane {
	private String message = "<HTML>" + Lang.getInstance().translate("Select union");
	
public  Union_Info() {

	this.setContentType("text/html");
	this.setText(message); //"<HTML>" + Lang.getInstance().translate("Select union")); // Document text is provided below.
	this.setBackground(new Color(255, 255, 255, 0));
	
	
	
}
	
 public String Get_HTML_Union_Info_001(UnionCls union){
	
	String dateAlive;
	String date_birthday;
	// TODO Auto-generated method stub
	// устанавливаем формат даты
	SimpleDateFormat formatDate = new SimpleDateFormat("dd.MM.yyyy"); // HH:mm");
	//создаем объект персоны
//	UnionCls union;
			

	if (union != null){// unionsTable.getSelectedRow() >= 0 ){
	//	union = tableModelUnions.getUnion(unionsTable.convertRowIndexToModel(unionsTable.getSelectedRow()));
	

	if (union.isConfirmed()){
		date_birthday=  formatDate.format(new Date(Long.valueOf(union.getBirthday())));
		message ="<html><div>#" + "<b>" + union.getKey() + " : " + date_birthday + "</b>"
		+ "<br>" + union.getName().toString() + "</div>";

		message += "<h2>"+ "Statuses" +"</h2>";
		// GETT PERSON STATUS for ALIVE
		Tuple3<Long, Integer, byte[]> t3Alive = DBSet.getInstance().getUnionStatusMap().getItem(union.getKey(), StatusCls.ALIVE_KEY);

		if (t3Alive != null){
			if (t3Alive.a == null) dateAlive = "active";
			else dateAlive = formatDate.format( new Date(t3Alive.a));
		} else
		{
			dateAlive = Lang.getInstance().translate("unknown");
		}
		message += "<div>" + Lang.getInstance().translate("ALIVE")+": <b>" + dateAlive +"</b></div>";

		// GETT PERSON STATUS for DEAD
		Tuple3<Long, Integer, byte[]> t3Dead = DBSet.getInstance().getUnionStatusMap().getItem(union.getKey(), StatusCls.DEAD_KEY);

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
		/*
		TreeMap<String, java.util.Stack<Tuple3<Integer, Integer, byte[]>>> addresses= DBSet.getInstance().getUnionAddressMap().getItems(union.getKey());
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
		} */					
	} else {
		message = "<html><p>"+ Lang.getInstance().translate("Not found!") +"</p>";	
	}
	message = message + "</html>";
	
	
	}	
	return message;
}
 
  public String Get_HTML_Union_Info_002(UnionCls union)	{
		
	
	  String Date_Acti; 
		
	  SimpleDateFormat formatDate = new SimpleDateFormat("dd.MM.yyyy"); // HH:mm");
	  if (union != null){ //if (table.getSelectedRow() >= 0 ){
		//union = unionsModel.getItem(table.convertRowIndexToModel(table.getSelectedRow()));
	
	
//читаем таблицу персон.
	Tuple3<Long, Integer, byte[]> t3 = DBSet.getInstance().getUnionStatusMap().getItem(union.getKey(), StatusCls.ALIVE_KEY); //(Long) unionsTable.getValueAt(unionsTable.getSelectedRow(),0));
// преобразование в дату


	
	if (t3 != null){
		if (t3.a == null) Date_Acti = "+";
		else Date_Acti = formatDate.format( new Date(Long.valueOf(t3.a.toString())));
	} else
	{
		Date_Acti =Lang.getInstance().translate("Not found!");
	};
	
	if (union.isConfirmed()){
		String Date_birs = formatDate.format(new Date(Long.valueOf(union.getBirthday())));
		 message ="<html><div></div><div> <p><b>" + Lang.getInstance().translate("Key")+":"   + union.getKey()        			+ "</p>"
		+ "<p> <b> "  + Lang.getInstance().translate("Name")+":"       			  + union.getName().toString()		+ "</p>" 
        + "<p> "  + Lang.getInstance().translate("Birthday")  +":"        	      + Date_birs			+"</p>"
        + "<p>  "  + Lang.getInstance().translate("To Date")  +":"        		  + Date_Acti			+"</p>"
        ;
		 /*
		 // Читаем адреса клиента
		 TreeMap<String, java.util.Stack<Tuple3<Integer, Integer, byte[]>>> Addresses= DBSet.getInstance().getUnionAddressMap().getItems(union.getKey());
		 if ( !Addresses.isEmpty()){
			 message =message + "<p>"  + Lang.getInstance().translate("Account")  +":  <input type='text' size='' value='"+ Addresses.lastKey() +"' id='iiii' name='nnnn' class= 'cccc' onchange =''><p></div>";
		 }
		 else{
			 message = message + "<p> " +  Lang.getInstance().translate("Account not found!")+ "</p";
										 }
		 */
	}else{
		
		message = "<html><p>"+ Lang.getInstance().translate("Not found!") +"</></>";	
	}
	message = message + "</html>";
  }
	 
	return  message;
  }

public void show_Union_001(UnionCls union){
	
	setText(new Union_Info().Get_HTML_Union_Info_001(union));
	return;
}
public void show_Union_002(UnionCls union){
	
	setText(new Union_Info().Get_HTML_Union_Info_002(union));
	return;
}



}
