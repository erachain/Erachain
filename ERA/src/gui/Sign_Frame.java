package gui;

import java.awt.BorderLayout;

import javax.swing.JInternalFrame;

import core.account.Account;
import core.item.notes.NoteCls;
import lang.Lang;




public class Sign_Frame extends JInternalFrame{


	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public Sign_Frame (NoteCls note, Account account)
	{
	
		Sign_Panel panel = new Sign_Panel(note, account);
        getContentPane().add(panel, BorderLayout.CENTER);
	         
       //SHOW FRAME
        this.pack();
        this.setMaximizable(true);
		this.setTitle(Lang.getInstance().translate("Sign Statement"));
		this.setClosable(true);
		this.setResizable(true);
		//this.setSize(new Dimension( (int)parent.getSize().getWidth()-80,(int)parent.getSize().getHeight()-150));
		this.setLocation(20, 20);
	//	this.setIconImages(icons);
		//CLOSE
		setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);
        this.setResizable(true);
//        splitPane_1.setDividerLocation((int)((double)(this.getHeight())*0.7));//.setDividerLocation(.8);
        this.setVisible(true);
	
	}

}