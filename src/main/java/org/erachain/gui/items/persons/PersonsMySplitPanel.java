package org.erachain.gui.items.persons;

import org.erachain.core.item.ItemCls;
import org.erachain.core.item.persons.PersonCls;
import org.erachain.gui.items.ItemSplitPanel;
import org.erachain.gui.models.WalletItemPersonsTableModel;
import org.erachain.lang.Lang;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class PersonsMySplitPanel extends ItemSplitPanel {

    public static String NAME = "PersonsMySplitPanel";
    public static String TITLE = "My Persons";

    private static final long serialVersionUID = 2717571093561259483L;

    public PersonsMySplitPanel() {
        super(new WalletItemPersonsTableModel(), NAME, TITLE);

        // add items in menu
        JMenuItem set_Status_Item = new JMenuItem(Lang.T("Set Status to Person"));

        set_Status_Item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                @SuppressWarnings("unused")
                PersonSetStatusDialog fm = new PersonSetStatusDialog((PersonCls) itemTableSelected);

            }
        });
        this.menuTable.add(set_Status_Item);

        JMenuItem attestPubKey_Item = new JMenuItem(Lang.T("Certify Public Key for Person"));

        attestPubKey_Item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {


                @SuppressWarnings("unused")
                PersonCertifyPubKeysDialog fm = new PersonCertifyPubKeysDialog((PersonCls) itemTableSelected, itemTableSelected.getMaker());

            }
        });
        this.menuTable.add(attestPubKey_Item);

    }

    // show details
    @Override
    public Component getShow(ItemCls item) {
        return new PersonInfo002((PersonCls) item, true);
    }

}
