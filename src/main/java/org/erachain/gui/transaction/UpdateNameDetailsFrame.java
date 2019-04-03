package org.erachain.gui.transaction;

import org.erachain.core.transaction.UpdateNameTransaction;
import org.erachain.lang.Lang;
import org.erachain.utils.GZIP;
import org.erachain.utils.MenuPopupUtil;

import javax.swing.*;

@SuppressWarnings("serial")
public class UpdateNameDetailsFrame extends RecDetailsFrame {
    public UpdateNameDetailsFrame(UpdateNameTransaction nameUpdate) {
        super(nameUpdate);

        //LABEL OWNER
        ++labelGBC.gridy;
        JLabel ownerLabel = new JLabel(Lang.getInstance().translate("New Owner") + ":");
        this.add(ownerLabel, labelGBC);

        //OWNER
        ++detailGBC.gridy;
        JTextField owner = new JTextField(nameUpdate.getName().getOwner().getAddress());
        owner.setEditable(false);
        MenuPopupUtil.installContextMenu(owner);
        this.add(owner, detailGBC);

        String personStr = nameUpdate.getName().getOwner().viewPerson();
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
        JTextField name = new JTextField(nameUpdate.getName().getName());
        name.setEditable(false);
        MenuPopupUtil.installContextMenu(name);
        this.add(name, detailGBC);

        //LABEL VALUE
        ++labelGBC.gridy;
        JLabel valueLabel = new JLabel(Lang.getInstance().translate("New Value") + ":");
        this.add(valueLabel, labelGBC);

        //VALUE
        ++detailGBC.gridy;

        JTextArea txtareaValue = new JTextArea(GZIP.webDecompress(nameUpdate.getName().getValue()));
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
        compressed.setSelected(nameUpdate.getName().getValue().startsWith("?gz!"));
        compressed.setEnabled(false);

        this.add(compressed, detailGBC);

        //PACK
        //	this.pack();
        //     this.setResizable(false);
        //     this.setLocationRelativeTo(null);
        this.setVisible(true);
    }
}
