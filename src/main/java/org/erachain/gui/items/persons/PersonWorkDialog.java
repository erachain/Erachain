package org.erachain.gui.items.persons;

import org.erachain.core.item.persons.PersonCls;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;
import org.erachain.gui.items.accounts.AccountAssetSendPanel;
import org.erachain.gui.items.mails.MailSendPanel;
import org.erachain.gui.library.MButton;
import org.erachain.gui.models.PersonAccountsModel;
import org.erachain.gui.records.toSignRecordDialog;
import org.erachain.gui2.MainPanel;
import org.erachain.lang.Lang;
import org.mapdb.Fun.Tuple3;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.TreeMap;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * @author Саша
 */
public class PersonWorkDialog extends JDialog {

    private MButton jButton1;
    private MButton jButton2;
    private MButton jButton3;
    private MButton jButton4;
    private MButton jButton5;
    /**
     * Creates new form PersonWorkDialog
     */
    public PersonWorkDialog(PersonCls person) {
        super();
        this.setModal(true);

        getContentPane().setLayout(new java.awt.GridLayout(0, 1));
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        //  setAlwaysOnTop(true);
        List<Image> icons = new ArrayList<Image>();
        icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon16.png"));
        icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon32.png"));
        icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon64.png"));
        icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon128.png"));
        this.setIconImages(icons);


        jButton1 = new MButton(Lang.T("Set Status to Person"), 3);
        getContentPane().add(jButton1);
        jButton1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                @SuppressWarnings("unused")
                PersonSetStatusDialog fm = new PersonSetStatusDialog(person);
                dispose();
            }
        });

        jButton2 = new MButton(Lang.T("Certify Public Key for Person"), 3);
        getContentPane().add(jButton2);
        jButton2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {


                @SuppressWarnings("unused")
                PersonCertifyPubKeysDialog fm = new PersonCertifyPubKeysDialog(person, person.getMaker());
                dispose();
            }
        });


        jButton3 = new MButton(Lang.T("Vouch the Person Info"), 3);
        getContentPane().add(jButton3);
        jButton3.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                PersonCls per = person;
                byte[] ref = per.getReference();
                Transaction transaction = Transaction.findByDBRef(DCSet.getInstance(), ref);
                int blockNo = transaction.getBlockHeight();
                int recNo = transaction.getSeqNo();
                new toSignRecordDialog(blockNo, recNo);
                dispose();
            }
        });


        PersonAccountsModel person_Accounts_Model = new PersonAccountsModel(person.getKey());
        if (person_Accounts_Model.getRowCount() > 0) {
            jButton4 = new MButton(Lang.T("Send Asset to Person"), 3);
            getContentPane().add(jButton4);
            jButton4.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {

                    TreeMap<String, Stack<Tuple3<Integer, Integer, Integer>>> addresses = DCSet.getInstance().getPersonAddressMap().getItems(person.getKey());
                    if (addresses.isEmpty()) {

                    } else {
                        MainPanel.getInstance().insertNewTab(Lang.T("Send Asset to Person"), new AccountAssetSendPanel(null,
                                null, null, person, null, false));

                    }
                    dispose();
                }
            });


            jButton5 = new MButton(Lang.T("Send Mail to Person"), 3);
            getContentPane().add(jButton5);
            jButton5.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {

                    TreeMap<String, Stack<Tuple3<Integer, Integer, Integer>>> addresses = DCSet.getInstance().getPersonAddressMap().getItems(person.getKey());
                    if (addresses.isEmpty()) {

                    } else {
                        MainPanel.getInstance().insertNewTab(Lang.T("Send Mail to Person"), new MailSendPanel(null, null, person));
                    }
                    dispose();
                }
            });

        }


        pack();
        this.setLocationRelativeTo(null);
        this.setVisible(false);
    }

    // End of variables declaration                   
}
