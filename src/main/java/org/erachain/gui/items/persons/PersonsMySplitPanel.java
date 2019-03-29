package org.erachain.gui.items.persons;

import org.erachain.core.item.ItemCls;
import org.erachain.core.item.persons.PersonCls;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;
import org.erachain.gui.items.ItemSplitPanel;
import org.erachain.gui.models.WalletItemPersonsTableModel;
import org.erachain.gui.records.VouchRecordDialog;
import org.erachain.lang.Lang;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class PersonsMySplitPanel extends ItemSplitPanel {
    private static final long serialVersionUID = 2717571093561259483L;
      
    private PersonsMySplitPanel th;

    public PersonsMySplitPanel() {
        super(new WalletItemPersonsTableModel(), "PersonsMySplitPanel");

        this.setName(Lang.getInstance().translate("My Persons"));
        th = this;
//      add items in menu

        
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
        return new PersonInfo002((PersonCls) item, true);
    }

}
