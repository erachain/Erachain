package gui.items.documents;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JInternalFrame;

import gui.MainFrame;
import gui.Main_Internal_Frame;
import lang.Lang;

public class Main_Hash_Document_Frame extends Main_Internal_Frame{

	private static final long serialVersionUID = 1L;

public Main_Hash_Document_Frame(){
// not show buttons main Toolbar
	this.setTitle(Lang.getInstance().translate("Documents Hashes"));
	this.jButton2_jToolBar.setVisible(false);
	this.jButton3_jToolBar.setVisible(false);
// buttun1
//	this.jButton1_jToolBar.setText(Lang.getInstance().translate("Issue Imprint"));
// status panel
	this.jLabel_status_jPanel.setText(Lang.getInstance().translate("Work with Documents"));
	this.jToolBar.setVisible(false);
	
	Write_Documents_Hashes_Panel write_Documents_Hashes_Panel = new Write_Documents_Hashes_Panel();
	this.jTabbedPane.add(write_Documents_Hashes_Panel, Lang.getInstance().translate("Write Documents Hashes"));
	
	Search_Document_Hash search_Document_Hash = new Search_Document_Hash();
	this.jTabbedPane.add(search_Document_Hash, Lang.getInstance().translate("Sign Document Hash"));
	
	this.pack();
	this.setSize(800,600);	
	this.setMaximizable(true);	
	this.setClosable(true);
	this.setResizable(true);
	this.setLocation(20, 20);
	setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);
	this.setResizable(true);
    this.setVisible(true);
	Dimension size = MainFrame.desktopPane.getSize();
	this.setSize(new Dimension((int)size.getWidth()-100,(int)size.getHeight()-100));
	write_Documents_Hashes_Panel.jSplitPanel.setDividerLocation((int)(size.getWidth()/2));
	
	}

}
