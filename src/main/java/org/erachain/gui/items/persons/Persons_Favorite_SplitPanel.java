package org.erachain.gui.items.persons;

import org.erachain.core.item.ItemCls;
import org.erachain.core.item.persons.PersonCls;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;
import org.erachain.gui.items.FavoriteItemModelTable;
import org.erachain.gui.items.Item_SplitPanel;
import org.erachain.gui.items.accounts.Account_Send_Dialog;
import org.erachain.gui.items.mails.Mail_Send_Dialog;
import org.erachain.gui.records.VouchRecordDialog;
import org.erachain.lang.Lang;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Persons_Favorite_SplitPanel extends Item_SplitPanel {
    private static final long serialVersionUID = 2717571093561259483L;
    private static FavoriteItemModelTable table_Model = new FavoritePersonsTableModel();
    private Persons_Favorite_SplitPanel th;

    public Persons_Favorite_SplitPanel() {
        super(table_Model, "Persons_Favorite_SplitPanel");
        this.setName(Lang.getInstance().translate("Favorite Persons"));
        th = this;
        JMenuItem vsend_Coins_Item = new JMenuItem(Lang.getInstance().translate("Send asset"));

        vsend_Coins_Item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new Account_Send_Dialog(null, null, null, (PersonCls) th.item_Menu);
            }
        });

       // th.menu_Table.add(vsend_Coins_Item);
        JMenuItem send_Mail_Item = new JMenuItem(Lang.getInstance().translate("Send mail"));
        send_Mail_Item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new Mail_Send_Dialog(null, null, null, (PersonCls) th.item_Menu);
            }
        });

     //   th.menu_Table.add(send_Mail_Item);
//      add items in menu


        JMenuItem set_Status_Item = new JMenuItem(Lang.getInstance().translate("Set status to person"));
      
       set_Status_Item.addActionListener(new ActionListener() {
           @Override
           public void actionPerformed(ActionEvent e) {

               @SuppressWarnings("unused")
               PersonSetStatusDialog fm = new PersonSetStatusDialog((PersonCls) th.item_Menu);
               
           }
       });
       this.menu_Table.add(set_Status_Item);

        JMenuItem attestPubKey_Item = new JMenuItem(Lang.getInstance().translate("Attest public key for person"));
      
       attestPubKey_Item.addActionListener(new ActionListener() {
           @Override
           public void actionPerformed(ActionEvent e) {


               @SuppressWarnings("unused")
               PersonConfirmDialog fm = new PersonConfirmDialog((PersonCls) th.item_Menu, th.item_Menu.getOwner());
              
           }
       });
       this.menu_Table.add(attestPubKey_Item);

        JMenuItem vouchPerson_Item = new JMenuItem(Lang.getInstance().translate("Vouch the person info"));
       vouchPerson_Item.addActionListener(new ActionListener() {
           @Override
           public void actionPerformed(ActionEvent e) {

               PersonCls per = (PersonCls) th.item_Menu;
               byte[] ref = per.getReference();
               Transaction transaction = Transaction.findByDBRef(DCSet.getInstance(), ref);
               int blockNo = transaction.getBlockHeight();
               int recNo = transaction.getSeqNo();
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

    @Override
    protected void splitClose() {
        table_Model.removeObservers();

    }
}
