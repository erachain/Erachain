package org.erachain.gui.transaction;

import org.erachain.core.transaction.RHashes;
import org.erachain.lang.Lang;
import org.erachain.utils.MenuPopupUtil;

import javax.swing.*;
import java.nio.charset.StandardCharsets;

@SuppressWarnings("serial")
public class HashesDetailsFrame extends RecDetailsFrame {
    public HashesDetailsFrame(RHashes r_Hashes) {
        super(r_Hashes, true);

        //LABEL NAME
        ++labelGBC.gridy;
        JLabel nameLabel = new JLabel(Lang.T("URL") + ":");
        this.add(nameLabel, labelGBC);

        //NAME
        ++fieldGBC.gridy;
        JTextField name = new JTextField(new String(r_Hashes.getURL(), StandardCharsets.UTF_8));
        name.setEditable(false);
        MenuPopupUtil.installContextMenu(name);
        this.add(name, fieldGBC);

        //LABEL DESCRIPTION
        ++labelGBC.gridy;
        JLabel descriptionLabel = new JLabel(Lang.T("Description") + ":");
        this.add(descriptionLabel, labelGBC);

        //DESCRIPTION
        ++fieldGBC.gridy;
        JTextPane txtAreaDescription = new JTextPane();
        txtAreaDescription.setContentType("text/html");
        //	txtAreaDescription.setBackground(MainFrame.getFrames()[0].getBackground());

        txtAreaDescription.setText(new String(r_Hashes.getData(), StandardCharsets.UTF_8));
		/*
		txtAreaDescription.setRows(4);
		txtAreaDescription.setColumns(4);
		*/
        txtAreaDescription.setBorder(name.getBorder());
        txtAreaDescription.setEditable(false);
        //MenuPopupUtil.installContextMenu(txtAreaDescription);
        this.add(txtAreaDescription, fieldGBC);

        //LABEL HASHES
        ++labelGBC.gridy;
        JLabel hashesLabel = new JLabel(Lang.T("HASHES") + ":");
        this.add(hashesLabel, labelGBC);

        //HASHES
        ++fieldGBC.gridy;
        JTextPane txtAreaHashes = new JTextPane();
        txtAreaHashes.setContentType("text/html");
        //	txtAreaHashes.setBackground(MainFrame.getFrames()[0].getBackground());

        txtAreaHashes.setText("<html>" + String.join("<br />", r_Hashes.getHashesB58()) + "</html>");
        txtAreaHashes.setBorder(name.getBorder());
        txtAreaHashes.setEditable(false);
        //MenuPopupUtil.installContextMenu(txtAreaHashes);
        this.add(txtAreaHashes, fieldGBC);

        //txtAreaHashes.setText(String.join(" ", r_Hashes.getHashesB58()));

        //PACK
//		this.pack();
//        this.setResizable(false);
//        this.setLocationRelativeTo(null);
        this.setVisible(true);

    }
}
