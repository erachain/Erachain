package gui.library;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import controller.Controller;
import core.BlockGenerator;
import core.wallet.Wallet;
import datachain.DCSet;
import gui.PasswordPane;
import lang.Lang;
import utils.ObserverMessage;

public class Wallet_Orphan_Button extends JButton implements Observer {

	private Wallet_Orphan_Button th;

	public Wallet_Orphan_Button() {

		super(Lang.getInstance().translate("Orphan Bloks"));
		th = this;
		this.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				// TODO Auto-generated method stub
				// check synchronize Walet
				if (Controller.getInstance().isProcessingWalletSynchronize()) {
					return;
				}
				// CHECK IF WALLET UNLOCKED
				if (!Controller.getInstance().isWalletUnlocked()) {
					// ASK FOR PASSWORD
					String password = PasswordPane.showUnlockWalletDialog(th);
					if (!Controller.getInstance().unlockWallet(password)) {
						// WRONG PASSWORD
						JOptionPane.showMessageDialog(null, Lang.getInstance().translate("Invalid password"),
								Lang.getInstance().translate("Unlock Wallet"), JOptionPane.ERROR_MESSAGE);
						return;
					}
				}

				// GENERATE NEW ACCOUNT

				// newAccount_Button.setEnabled(false);
				// creane new thread
				new Thread() {
					@Override
					public void run() {
						
						String message = "Insert orphan Blocks ";
						String retVal = JOptionPane.showInputDialog(null, message, " ");
						if (retVal != null){
							
							Integer retValint = Integer.valueOf(retVal);
							int hh = DCSet.getInstance().getBlockMap().size() - retValint;
							if (hh >1 )
							Controller.getInstance().setOrphanTo(hh);
						}
						
						
						
						
					}
				}.start();
			}
		});
		Controller.getInstance().addObserver(this);
	}

	@Override
	public void update(Observable arg0, Object arg1) 
	{
		ObserverMessage message = (ObserverMessage) arg1;
		
		if(message.getType() == ObserverMessage.FORGING_STATUS)
		{
			BlockGenerator.ForgingStatus status = (BlockGenerator.ForgingStatus) message.getValue();
			
						
			if (status == BlockGenerator.ForgingStatus.FORGING_WAIT || status == BlockGenerator.ForgingStatus.FORGING_ENABLED)
				th.setEnabled(false);
				else{
				th.setEnabled(true);
				}
		}		
	}

	
}
