package gui.items.assets;

import java.awt.Color;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JEditorPane;

import org.mapdb.Fun.Tuple3;
import org.mapdb.Fun.Tuple4;

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
		  Tuple4<Long, Long, Integer, Integer> t3 = DBSet.getInstance().getAssetStatusMap().getItem(asset.getKey(), StatusCls.ALIVE_KEY); //(Long) assetsTable.getValueAt(assetsTable.getSelectedRow(),0));
// преобразование в дату

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
