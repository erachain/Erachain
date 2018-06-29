package gui.items.persons;

import core.item.ItemCls;
import core.item.persons.PersonCls;
import core.transaction.Transaction;
import datachain.DCSet;
import gui.items.Item_Search_SplitPanel;
import gui.items.accounts.Account_Send_Dialog;
import gui.items.mails.Mail_Send_Dialog;
import gui.library.MButton;
import gui.models.PersonAccountsModel;
import gui.records.VouchRecordDialog;
import lang.Lang;

import javax.swing.*;

import org.mapdb.Fun.Tuple3;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Stack;
import java.util.TreeMap;

public class Persons_Search_SplitPanel extends Item_Search_SplitPanel {

    private static final long serialVersionUID = 2717571093561259483L;

    private static TableModelPersons search_Table_Model = new TableModelPersons();

    private Persons_Search_SplitPanel th;

    public Persons_Search_SplitPanel() {
        super(search_Table_Model, "Persons_Search_SplitPanel", "Persons_Search_SplitPanel");

        this.th = this;
        JMenuItem vsend_Coins_Item = new JMenuItem(Lang.getInstance().translate("Send Asset"));

        vsend_Coins_Item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                new Account_Send_Dialog(null, null, null, (PersonCls) th.item_Menu);
            }
        });

      //  this.menu_Table.add(vsend_Coins_Item);
        
        
        JMenuItem send_Mail_Item = new JMenuItem(Lang.getInstance().translate("Send Mail"));
        send_Mail_Item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                new Mail_Send_Dialog(null, null, null, (PersonCls) th.item_Menu);
            }
        });

     //   this.menu_Table.add(send_Mail_Item);
        JMenuItem set_Status_Item = new JMenuItem(Lang.getInstance().translate("Set Status to Person"));
       
        set_Status_Item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                @SuppressWarnings("unused")
                PersonSetStatusDialog fm = new PersonSetStatusDialog((PersonCls) th.item_Menu);
                
            }
        });
        this.menu_Table.add(set_Status_Item);

        JMenuItem attestPubKey_Item = new JMenuItem(Lang.getInstance().translate("Attest Public Key for Person"));
       
        attestPubKey_Item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {


                @SuppressWarnings("unused")
                PersonConfirmDialog fm = new PersonConfirmDialog((PersonCls) th.item_Menu, th.item_Menu.getOwner());
               
            }
        });
        this.menu_Table.add(attestPubKey_Item);

        JMenuItem vouchPerson_Item = new JMenuItem(Lang.getInstance().translate("Vouch the Person Info"));
        vouchPerson_Item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                PersonCls per = (PersonCls) th.item_Menu;
                byte[] ref = per.getReference();
                Transaction transaction = Transaction.findByDBRef(DCSet.getInstance(), ref);
                int blockNo = transaction.getBlockHeight(DCSet.getInstance());
                int recNo = transaction.getSeqNo(DCSet.getInstance());
                new VouchRecordDialog(blockNo, recNo);
               
            }
        });
        this.menu_Table.add(vouchPerson_Item);


      

      
        
        
    }

    // show details
    @Override
    public Component get_show(ItemCls item) {

        return new Person_Info_002((PersonCls) item, true);

    }
}
