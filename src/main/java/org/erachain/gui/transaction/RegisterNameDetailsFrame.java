package org.erachain.gui.transaction;

import org.erachain.core.transaction.RegisterNameTransaction;
import org.erachain.lang.Lang;
import org.erachain.utils.GZIP;
import org.erachain.utils.MenuPopupUtil;

import javax.swing.*;

@SuppressWarnings("serial")
public class RegisterNameDetailsFrame extends RecDetailsFrame {
    public RegisterNameDetailsFrame(RegisterNameTransaction nameRegistration) {
        super(nameRegistration);

        //LABEL OWNER
        ++labelGBC.gridy;
        JLabel ownerLabel = new JLabel(Lang.getInstance().translate("Owner") + ":");
        this.add(ownerLabel, labelGBC);

        //OWNER
        ++detailGBC.gridy;
        JTextField owner = new JTextField(nameRegistration.getName().getOwner().getPersonAsString());
        owner.setEditable(false);
        MenuPopupUtil.installContextMenu(owner);
        this.add(owner, detailGBC);

        String personStr = nameRegistration.getName().getOwner().viewPerson();
        if (personStr.length() > 0) {
            ++labelGBC.gridy;
            ++detailGBC.gridy;
            this.add(new JLabel(personStr), detailGBC);
        }

        //LABEL NAME
        ++labelGBC.gridy;
        JLabel nameLabel = new JLabel(Lang.getInstance().translate("Name") + ":");
        this.add(nameLabel, labelGBC);

        //NAME
        ++detailGBC.gridy;
        JTextField name = new JTextField(nameRegistration.getName().getName());
        name.setEditable(false);
        MenuPopupUtil.installContextMenu(name);
        this.add(name, detailGBC);

        //LABEL VALUE
        ++labelGBC.gridy;
        JLabel valueLabel = new JLabel(Lang.getInstance().translate("Value") + ":");
        this.add(valueLabel, labelGBC);

        //VALUE
        ++detailGBC.gridy;

        JTextArea txtareaValue = new JTextArea(GZIP.webDecompress(nameRegistration.getName().getValue()));
        txtareaValue.setRows(10);
        txtareaValue.setColumns(43);
        txtareaValue.setEditable(false);
        MenuPopupUtil.installContextMenu(txtareaValue);

        JScrollPane valueScroll = new JScrollPane(txtareaValue);
        valueScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        valueScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        this.add(valueScroll, detailGBC);

        //LABEL COMPRESSED
        ++labelGBC.gridy;
        JLabel compressedLabel = new JLabel(Lang.getInstance().translate("Compressed") + ":");
        this.add(compressedLabel, labelGBC);

        //COMPRESSED
        ++detailGBC.gridy;
        final JCheckBox compressed = new JCheckBox();
        compressed.setSelected(nameRegistration.getName().getValue().startsWith("?gz!"));
        compressed.setEnabled(false);

        this.add(compressed, detailGBC);

        //PACK
//		this.pack();
//        this.setResizable(false);
//        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }
}
