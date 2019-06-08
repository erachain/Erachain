package org.erachain.gui.items.accounts;

import org.erachain.core.account.Account;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.item.persons.PersonCls;
import org.erachain.core.transaction.TransactionAmount;
import org.erachain.lang.Lang;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class AccountSendDialog extends JDialog {

    private static final long serialVersionUID = 1L;
    public AccountActionSendPanel panel;


    public AccountSendDialog(AssetCls asset, Account accountFrom, Account accountTo, PersonCls person, boolean receive) {
        init(asset, accountFrom, accountTo, person, receive, null);
    }

    public AccountSendDialog(AssetCls asset, Account accountFrom, Account accountTo, PersonCls person) {
        init(asset, accountFrom, accountTo, person, true, null);
    }
    public AccountSendDialog(AssetCls asset, Account accountFrom, Account accountTo, PersonCls person, String message) {
        init(asset, accountFrom, accountTo, person, true, message);
    }

    
    public void sertParams(String ammount, String title, String message) {

        panel.jTextField_Ammount.setText(ammount);
        panel.jTextArea_Description.setText(title);
        panel.jTextField_Mess_Title.setText(message);

    }

    public void setNoRecive(boolean noR) {

        panel.noRecive = noR;

    }
    private void  init(AssetCls asset, Account accountFrom, Account accountTo, PersonCls person, boolean receive, String message){
     // ICON
        List<Image> icons = new ArrayList<Image>();
        icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon16.png"));
        icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon32.png"));
        icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon64.png"));
        icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon128.png"));
        this.setIconImages(icons);
        panel = new AccountActionSendPanel(asset, TransactionAmount.ACTION_SEND, accountFrom, accountTo,  person, message);
        // no recieve
        this.setNoRecive(!receive);
        
        getContentPane().add(panel, BorderLayout.CENTER);
        this.setTitle(Lang.getInstance().translate("Send"));
        Toolkit kit = Toolkit.getDefaultToolkit();
        Dimension screens = kit.getScreenSize();
        int h = screens.height - 50;
        int w = screens.width - 50;
        this.setPreferredSize(new Dimension(w,h));
        this.pack();
        this.setResizable(true);
        setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);
        setModal(true);

        
        this.setLocationRelativeTo(null);
        this.setVisible(true);
        
    }

}