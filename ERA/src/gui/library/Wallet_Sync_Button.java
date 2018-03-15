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
import core.wallet.Wallet;
import datachain.DCSet;
import gui.PasswordPane;
import lang.Lang;
import utils.ObserverMessage;

public class Wallet_Sync_Button extends JButton implements Observer {

	private Wallet_Sync_Button th;

	public Wallet_Sync_Button() {

		super(Lang.getInstance().translate("Sync Wallet"));
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

						Controller.getInstance().wallet.synchronize(true);
					}
				}.start();
			}
		});
		Controller.getInstance().addObserver(this);
	}

	@Override
	public void update(Observable arg0, Object arg1) {
		// TODO Auto-generated method stub
		ObserverMessage message = (ObserverMessage) arg1;
		int type = message.getType();
		if (type == ObserverMessage.WALLET_SYNC_STATUS) {
			int currentHeight = (int) message.getValue();
			if (currentHeight == 0 || currentHeight == DCSet.getInstance().getBlockMap().size())
			{
				th.setEnabled(true);
				return;
			}
			th.setEnabled(false);
		}
	}
}
