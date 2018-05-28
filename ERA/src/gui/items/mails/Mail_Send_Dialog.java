package gui.items.mails;

import core.account.Account;
import core.item.assets.AssetCls;
import core.item.persons.PersonCls;
import lang.Lang;

import javax.swing.*;
import java.awt.*;

//import gui.items.persons.IssuePersonFrame;
//import gui.items.persons.MyPersonsPanel;
//import gui.items.persons.PersonFrame;

public class Mail_Send_Dialog extends JDialog {


    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public Mail_Send_Dialog(AssetCls asset, Account account, Account account_To, PersonCls person) {

        Mail_Send_Panel panel = new Mail_Send_Panel(asset, account, account_To, person);
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