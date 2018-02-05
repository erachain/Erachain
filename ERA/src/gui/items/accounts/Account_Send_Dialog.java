package gui.items.accounts;

import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JInternalFrame;
import core.account.Account;
import core.item.assets.AssetCls;
import core.item.persons.PersonCls;
import lang.Lang;
import java.awt.Image;

public class Account_Send_Dialog extends JDialog {

	private static final long serialVersionUID = 1L;
	public Account_Send_Panel panel;
	

	public Account_Send_Dialog(AssetCls asset, Account account, Account account_To, PersonCls person) {

		// ICON
		List<Image> icons = new ArrayList<Image>();
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon16.png"));
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon32.png"));
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon64.png"));
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon128.png"));
		this.setIconImages(icons);
		panel = new Account_Send_Panel(asset, account, account_To, person);
		getContentPane().add(panel, BorderLayout.CENTER);
		this.setTitle(Lang.getInstance().translate("Send"));
		this.setResizable(true);
		setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);
		setModal(true);
		this.pack();
		this.setLocationRelativeTo(null);
		this.setVisible(false);

	}
	public void  sertParams(String ammount, String title, String message){
		
		panel.txtAmount.setText(ammount);
		panel.txtMessage.setText(title);
		panel.txt_Title.setText(message);
		
	}
	
	public void setNoRecive(boolean noR){
		
		panel.noRecive = noR;
		
	}

}