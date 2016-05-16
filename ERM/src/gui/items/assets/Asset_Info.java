package gui.items.assets;

import java.awt.Color;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JEditorPane;

import org.mapdb.Fun.Tuple3;

import core.item.assets.AssetCls;
import core.item.statuses.StatusCls;
import database.DBSet;
import lang.Lang;

// Info for asset
public class Asset_Info extends JEditorPane {
	
	private static final long serialVersionUID = 1L;
	private String message = "<HTML>" + Lang.getInstance().translate("Select asset");
	
public  Asset_Info() {

	this.setContentType("text/html");
	this.setText(message); 
	this.setBackground(new Color(255, 255, 255, 0));
	
	
	
}
	
 public String Get_HTML_Asset_Info_001(AssetCls asset){
	
	String dateAlive;
	String date_birthday;

	// устанавливаем формат даты
	SimpleDateFormat formatDate = new SimpleDateFormat("dd.MM.yyyy"); // HH:mm");

			



	if (asset != null){
//		date_birthday=  formatDate.format(new Date(Long.valueOf(asset.getBirthday())));
		message ="<html><div>#" + "<b>" + asset.getKey()            // + " : " + date_birthday + "</b>"
		+ "<br>" + asset.getName().toString() + "</div>";

		message += "<h2>"+ "Statuses" +"</h2>";
		// GETT PERSON STATUS for ALIVE
		Tuple3<Long, Integer, byte[]> t3Alive = DBSet.getInstance().getAssetStatusMap().getItem(asset.getKey(), StatusCls.ALIVE_KEY);

		if (t3Alive != null){
			if (t3Alive.a == null) dateAlive = "active";
			else dateAlive = formatDate.format( new Date(t3Alive.a));
		} else
		{
			dateAlive = Lang.getInstance().translate("unknown");
		}
		message += "<div>" + Lang.getInstance().translate("ALIVE")+": <b>" + dateAlive +"</b></div>";

		// GETT PERSON STATUS for DEAD
		Tuple3<Long, Integer, byte[]> t3Dead = DBSet.getInstance().getAssetStatusMap().getItem(asset.getKey(), StatusCls.DEAD_KEY);

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
		TreeMap<String, java.util.Stack<Tuple3<Integer, Integer, byte[]>>> addresses= DBSet.getInstance().getAssetAddressMap().getItems(asset.getKey());
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
	
	
		
	return message;
}
 
  public String Get_HTML_Asset_Info_002(AssetCls asset)	{
		
	
	  String Date_Acti; 
		
	  SimpleDateFormat formatDate = new SimpleDateFormat("dd.MM.yyyy"); // HH:mm");
	  if (asset != null){ //if (table.getSelectedRow() >= 0 ){
		//asset = assetsModel.getItem(table.convertRowIndexToModel(table.getSelectedRow()));
	
	
//читаем таблицу .
	Tuple3<Long, Integer, byte[]> t3 = DBSet.getInstance().getAssetStatusMap().getItem(asset.getKey(), StatusCls.ALIVE_KEY); //(Long) assetsTable.getValueAt(assetsTable.getSelectedRow(),0));
// преобразование в дату


	
	if (t3 != null){
		if (t3.a == null) Date_Acti = "+";
		else Date_Acti = formatDate.format( new Date(Long.valueOf(t3.a.toString())));
	} else
	{
		Date_Acti =Lang.getInstance().translate("Not found!");
	};
	
	if (asset != null){
	//	String Date_birs = formatDate.format(new Date(Long.valueOf(asset.getBirthday())));
		 message ="<html><div></div><div> <p><b>" + Lang.getInstance().translate("Key")+":"   + asset.getKey()        			+ "</p>"
		+ "<p> <b> "  + Lang.getInstance().translate("Name")+":"       			  + asset.getName().toString()		+ "</p>" 
    //    + "<p> "  + Lang.getInstance().translate("Birthday")  +":"        	      + Date_birs			+"</p>"
        + "<p>  "  + Lang.getInstance().translate("To Date")  +":"        		  + Date_Acti			+"</p>"
        ;
		 }else{
		message = "<html><p>"+ Lang.getInstance().translate("Not found!") +"</></>";	
		 }
	message = message + "</html>";
  }
	 
	return  message;
  }

public void show_Asset_001(AssetCls asset){
	
	setText(new Asset_Info().Get_HTML_Asset_Info_001(asset));
	return;
}
public void show_Asset_002(AssetCls asset){
	
	setText(new Asset_Info().Get_HTML_Asset_Info_002(asset));
	return;
}



}
