package org.erachain.gui.transaction;

import org.erachain.core.item.persons.PersonCls;
import org.erachain.core.transaction.IssuePersonRecord;
import org.erachain.gui.library.MTextPane;
import org.erachain.lang.Lang;
import org.erachain.utils.MenuPopupUtil;

import javax.swing.*;

@SuppressWarnings("serial")
public class IssuePersonDetailsFrame extends RecDetailsFrame {
    public IssuePersonDetailsFrame(IssuePersonRecord personIssue) {
        super(personIssue, false);

        PersonCls person = (PersonCls) personIssue.getItem();

        //LABEL NAME
        ++labelGBC.gridy;
        JLabel nameLabel = new JLabel(Lang.T("Name") + ":");
        this.add(nameLabel, labelGBC);

        //NAME
        ++fieldGBC.gridy;
        JTextField name = new JTextField(personIssue.getItem().viewName());
        name.setEditable(false);
        MenuPopupUtil.installContextMenu(name);
        this.add(name, fieldGBC);

        //LABEL DESCRIPTION
        ++labelGBC.gridy;
        JLabel descriptionLabel = new JLabel(Lang.T("Description") + ":");
        this.add(descriptionLabel, labelGBC);

        //DESCRIPTION
        ++fieldGBC.gridy;
        MTextPane txtAreaDescription = new MTextPane(personIssue.getItem().getDescription());
        //txtAreaDescription.setRows(4);
        txtAreaDescription.setBorder(name.getBorder());
        //txtAreaDescription.setEditable(false);
        MenuPopupUtil.installContextMenu(txtAreaDescription);
        this.add(txtAreaDescription, fieldGBC);

        //LABEL Birthday
        ++labelGBC.gridy;
        JLabel birthdayLabel = new JLabel(Lang.T("Birthday") + ":");
        this.add(birthdayLabel, labelGBC);

        //Birthday
        ++fieldGBC.gridy;
        JTextField birtday = new JTextField(person.getBirthdayStr());
        birtday.setEditable(false);
        this.add(birtday, fieldGBC);

        //LABEL Death
        if (!person.isAlive(0l)) {
            ++labelGBC.gridy;
            JLabel deadLabel = new JLabel(Lang.T("Deathday") + ":");
            this.add(deadLabel, labelGBC);

            //Deathday
            ++fieldGBC.gridy;
            JTextField dead = new JTextField(person.getDeathdayStr());
            dead.setEditable(false);
            birtday.setEditable(false);
            this.add(dead, fieldGBC);
        }


        //LABEL GENDER
        ++labelGBC.gridy;
        JLabel genderLabel = new JLabel(Lang.T("Gender") + ":");
        this.add(genderLabel, labelGBC);

        //GENDER
        ++fieldGBC.gridy;
        String txt = "";
        if (person.getGender() == 0) txt = Lang.T("Male");
        else if (person.getGender() == 1) txt = Lang.T("Female");
        JTextField gender = new JTextField(txt);

        gender.setEditable(false);
        this.add(gender, fieldGBC);

        //LABEL owner
        ++labelGBC.gridy;
        JLabel ownerLabel = new JLabel(Lang.T("Owner") + ":");
        this.add(ownerLabel, labelGBC);

        //owner
        ++fieldGBC.gridy;
        JTextField owner = new JTextField(person.getOwner().getAddress());
        owner.setEditable(false);
        this.add(owner, fieldGBC);

        //LABEL owner Public key
        ++labelGBC.gridy;
        JLabel owner_Public_keyLabel = new JLabel(Lang.T("Public key") + ":");
        this.add(owner_Public_keyLabel, labelGBC);

        //owner public key
        ++fieldGBC.gridy;
        JTextField owner_Public_Key = new JTextField(person.getOwner().getBase58());
        owner_Public_Key.setEditable(false);
        this.add(owner_Public_Key, fieldGBC);

        //PACK
        //	this.pack();
        //    this.setResizable(false);
        //    this.setLocationRelativeTo(null);
        this.setVisible(true);
    }
}
