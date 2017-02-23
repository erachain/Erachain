package gui.items.mails;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import controller.Controller;
import core.account.Account;
import core.item.assets.AssetCls;
import gui.MainFrame;
import gui.Main_Internal_Frame;
import gui.PasswordPane;
import lang.Lang;
// фрейм в котором работаем с адресами
public class Mails_Main_Frame extends Main_Internal_Frame {
	
	Incoming_Mails_Panel incoming_Mails_Panel;
	Outcoming_Mails_Panel sent_Mails_Panel;
	Mail_Send_Panel send_panel;
	private Incoming_Mails_SplitPanel incoming_Mails_Panel1;
	private Outcoming_Mails_SplitPanel outcoming_mails_Panel1;
	
	public Mails_Main_Frame(){
		
	
		this.setTitle(Lang.getInstance().translate("Mails"));
		this.jLabel_status_jPanel.setText(Lang.getInstance().translate("Work with Mails"));
		this.jButton2_jToolBar.setVisible(false);
		this.jButton3_jToolBar.setVisible(false);
		this.jButton1_jToolBar.setVisible(false);
		
		
		
	/*		
		//this.jToolBar.setFloatable(true);
		this.jLabel_status_jPanel.setText(Lang.getInstance().translate("Work with Mails"));
	// MY Accounts
		 incoming_Mails_Panel = new Incoming_Mails_Panel();
		this.jTabbedPane.add(incoming_Mails_Panel);
		
	// Search Accounts
		sent_Mails_Panel = new Outcoming_Mails_Panel();
		this.jTabbedPane.add(sent_Mails_Panel);
	*/
		// Incoming mails split panel
		incoming_Mails_Panel1 = new Incoming_Mails_SplitPanel();
		this.jTabbedPane.add(incoming_Mails_Panel1);

// Outcoming mails split panel
		outcoming_mails_Panel1 = new Outcoming_Mails_SplitPanel();
		this.jTabbedPane.add(outcoming_mails_Panel1);
		
		
		
		send_panel = new Mail_Send_Panel(null,null,null,null);
	
		this.jTabbedPane.add(send_panel);	
		
		
		
		
	
	this.pack();
	//	this.setSize(800,600);
	this.setMaximizable(true);
	
	this.setClosable(true);
	this.setResizable(true);
	this.setLocation(20, 20);
	//CLOSE
	setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);
    this.setResizable(true);
    this.setVisible(true);
    Dimension size = MainFrame.desktopPane.getSize();
    this.setSize(new Dimension((int)size.getWidth()-100,(int)size.getHeight()-100));
 
	
	
	}


	
	
	
}
