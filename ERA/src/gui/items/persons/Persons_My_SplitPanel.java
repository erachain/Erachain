package gui.items.persons;

import core.item.ItemCls;
import core.item.persons.PersonCls;
import core.transaction.Transaction;
import datachain.DCSet;
import gui.items.Item_SplitPanel;
import gui.models.WalletItemPersonsTableModel;
import gui.records.VouchRecordDialog;
import lang.Lang;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class Persons_My_SplitPanel extends Item_SplitPanel {
    private static final long serialVersionUID = 2717571093561259483L;
      
    private Persons_My_SplitPanel th;
    private static WalletItemPersonsTableModel table_Model = new WalletItemPersonsTableModel();

    public Persons_My_SplitPanel() {
        super(table_Model, "Persons_My_SplitPanel");

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

    @Override
    protected void splitClose() {
        table_Model.removeObservers();

    }

   

}




