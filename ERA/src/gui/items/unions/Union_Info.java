package gui.items.unions;

import java.awt.Color;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JEditorPane;

import org.mapdb.Fun.Tuple3;

import core.item.unions.UnionCls;
import datachain.DCSet;
import core.item.statuses.StatusCls;
import lang.Lang;

// Info for union
public class Union_Info extends JEditorPane {
	
	private static final long serialVersionUID = 1L;
	private String message = "<HTML>" + Lang.getInstance().translate("Select union");
	
public  Union_Info() {

	this.setContentType("text/html");
	this.setText(message); 
	this.setBackground(new Color(255, 255, 255, 0));
	
	
	
}
	
 public String Get_HTML_Union_Info_001(UnionCls union){
	
	String dateAlive;
	String date_birthday;

	// устанавливаем формат даты
	SimpleDateFormat formatDate = new SimpleDateFormat("dd.MM.yyyy"); // HH:mm");

			



	if (union != null){
		//date_birthday =  formatDate.format(new Date(Long.valueOf(union.getBirthday())));
		date_birthday = union.getBirthdayStr();
		message ="<html><div>#" + "<b>" + union.getKey() + " : " + date_birthday + "</b>"
		+ "<br>" + union.getName().toString() +  
		"<br>" +
		"</div>";

	} else {
		message = "<html><p>"+ Lang.getInstance().translate("Not found!") +"</p>";	
	}
	message = message + "</html>";
	
	
		
	return message;
}
 
  public String Get_HTML_Union_Info_002(UnionCls union)	{
		
	
	  String Date_Acti; 
		
	  SimpleDateFormat formatDate = new SimpleDateFormat("dd.MM.yyyy"); // HH:mm");
	  if (union != null){ //if (table.getSelectedRow() >= 0 ){
	
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
