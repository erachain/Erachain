package gui.items.statuses;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Stack;
import java.util.TreeMap;

import javax.swing.JTextPane;

import org.mapdb.Fun.Tuple3;
import org.mapdb.Fun.Tuple4;

import core.account.Account;
import core.block.GenesisBlock;
import core.item.statuses.StatusCls;
import datachain.DCSet;
import datachain.ItemStatusMap;
import gui.library.MTextPane;
import lang.Lang;

// Info for status
public class Status_Info extends MTextPane {
	private static final long serialVersionUID = 476307470457045006L;	
	
	public  Status_Info() {
	
	//	this.setContentType("text/html");
	//	this.setBackground(MainFrame.getFrames()[0].getBackground());
		
	}
	
	
	static String Get_HTML_Status_Info_001(StatusCls status)
	{
		 
		String message;
		
		if (status == null) return message = "Empty Status";
		
		if (!status.isConfirmed()) {
			message = Lang.getInstance().translate("Not confirmed");
		} else {
			message = "" + status.getKey();
		}
		message = "<div><b>" + message + "</b> : " + status.getName().toString() + "</div>";
		
		message += "<div>" + status.getDescription() + "</div>";
		message += "<div>" + (status.isUnique()?"UNIQUE":"multi") + "</div>";

		String creator = GenesisBlock.CREATOR.equals(status.getOwner())?"GENESIS":status.getOwner().getPersonAsString_01(false);

		message += "<div> Creator: " + (creator.length()==0?status.getOwner().getAddress():creator) + "</div>";

		return message;
	}
	 	
	public void show_001(StatusCls status){
		
		set_text("<html>" + Get_HTML_Status_Info_001(status) + "</html>");
		
		return;
	}	
	public void delay_on_Close(){
		
		
	}

}
