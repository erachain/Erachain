package org.erachain.gui.items.mails;

import org.erachain.core.account.Account;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.item.persons.PersonCls;
import org.erachain.lang.Lang;

import javax.swing.*;
import java.awt.*;

//import org.erachain.gui.*;
//import org.erachain.gui.*;
//import org.erachain.gui.*;

public class MailSendDialog extends JDialog {


    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public MailSendDialog(AssetCls asset, Account account, Account account_To, PersonCls person) {

        MailSendPanel panel = new MailSendPanel(asset, account, account_To, person);
        getContentPane().add(panel, BorderLayout.CENTER);

        //SHOW FRAME
        //       this.pack();
        //     this.setMaximizable(true);
        this.setTitle(Lang.getInstance().translate("Send"));
        //	this.setClosable(true);
        this.setResizable(true);
        //this.setSize(new Dimension( (int)parent.getSize().getWidth()-80,(int)parent.getSize().getHeight()-150));
        //	this.setLocation(20, 20);
        //	this.setIconImages(icons);
        //CLOSE
        setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);

        //	 setMinimumSize(new java.awt.Dimension(650, 23));
        setModal(true);
        //     setPreferredSize(new java.awt.Dimension(650,600));


        //PACK
        this.pack();
        this.setResizable(true);
        this.setLocationRelativeTo(null);

        //      this.setResizable(true);
//        splitPane_1.setDividerLocation((int)((double)(this.getHeight())*0.7));//.setDividerLocation(.8);
        this.setVisible(true);

    }

}