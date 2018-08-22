package gui.library;

import controller.Controller;
import core.account.Account;
import core.account.PublicKeyAccount;
import core.block.GenesisBlock;
import gui.items.accounts.Account_Send_Dialog;
import gui.items.mails.Mail_Send_Dialog;
import lang.Lang;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class M_Accoutn_Text_Field extends JTextField {
    public Account account;

    public M_Accoutn_Text_Field() {
        super();
    }

    public M_Accoutn_Text_Field(Account account) {
        super();
        this.account = account;
        set_account(account);
    }

    public void set_account(Account account) {
        if (account.isPerson()) {
            this.setText(account.getPersonAsString());
        } else if (GenesisBlock.CREATOR.equals(account)) this.setText("GENESIS");
        else {
            this.setText(account.getAddress());
        }

        // menu
        JPopupMenu creator_Meny = new JPopupMenu();
        JMenuItem copy_Creator_Address1 = new JMenuItem(Lang.getInstance().translate("Copy Account"));
        copy_Creator_Address1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                StringSelection value = new StringSelection(account.getAddress());
                clipboard.setContents(value, null);
            }
        });
        creator_Meny.add(copy_Creator_Address1);

        PublicKeyAccount public_Account;
        if (account instanceof PublicKeyAccount) {
            public_Account = (PublicKeyAccount) account;
        } else {
            byte[] publick_Key = Controller.getInstance().getPublicKeyByAddress(account.getAddress());
            public_Account = publick_Key == null ? null : new PublicKeyAccount(publick_Key);
        }

        JMenuItem copyPublicKey;
        if (public_Account == null) {
            copyPublicKey = new JMenuItem(Lang.getInstance().translate("Public Key not Found"));
            copyPublicKey.setEnabled(false);
        } else {
            copyPublicKey = new JMenuItem(Lang.getInstance().translate("Copy Public Key"));
            copyPublicKey.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                    // StringSelection value = new
                    // StringSelection(person.getCreator().getAddress().toString());
                    StringSelection value = new StringSelection(public_Account.getBase58());
                    clipboard.setContents(value, null);
                }
            });
        }
        creator_Meny.add(copyPublicKey);

        JMenuItem Send_Coins_Crator = new JMenuItem(Lang.getInstance().translate("Send asset"));
        Send_Coins_Crator.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new Account_Send_Dialog(null, null, account, null);
            }
        });
        creator_Meny.add(Send_Coins_Crator);

        JMenuItem Send_Mail_Creator = new JMenuItem(Lang.getInstance().translate("Send Mail"));
        Send_Mail_Creator.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                new Mail_Send_Dialog(null, null, account, null);
            }
        });
        creator_Meny.add(Send_Mail_Creator);
        this.setComponentPopupMenu(creator_Meny);

    }

}
