package org.erachain.gui.transaction;

import org.erachain.core.transaction.R_Hashes;
import org.erachain.lang.Lang;
import org.erachain.utils.MenuPopupUtil;

import javax.swing.*;
import java.nio.charset.Charset;

@SuppressWarnings("serial")
public class HashesDetailsFrame extends Rec_DetailsFrame {
    public HashesDetailsFrame(R_Hashes r_Hashes) {
        super(r_Hashes);

        //LABEL NAME
        ++labelGBC.gridy;
        JLabel nameLabel = new JLabel(Lang.getInstance().translate("URL") + ":");
        this.add(nameLabel, labelGBC);

        //NAME
        ++detailGBC.gridy;
        JTextField name = new JTextField(new String(r_Hashes.getURL(), Charset.forName("UTF-8")));
        name.setEditable(false);
        MenuPopupUtil.installContextMenu(name);
        this.add(name, detailGBC);

        //LABEL DESCRIPTION
        ++labelGBC.gridy;
        JLabel descriptionLabel = new JLabel(Lang.getInstance().translate("Description") + ":");
        this.add(descriptionLabel, labelGBC);

        //DESCRIPTION
        ++detailGBC.gridy;
        JTextPane txtAreaDescription = new JTextPane();
        txtAreaDescription.setContentType("text/html");
        //	txtAreaDescription.setBackground(MainFrame.getFrames()[0].getBackground());

        txtAreaDescription.setText(new String(r_Hashes.getData(), Charset.forName("UTF-8")));
		/*
		txtAreaDescription.setRows(4);
		txtAreaDescription.setColumns(4);
		*/
        txtAreaDescription.setBorder(name.getBorder());
        txtAreaDescription.setEditable(false);
        //MenuPopupUtil.installContextMenu(txtAreaDescription);
        this.add(txtAreaDescription, detailGBC);

        //LABEL HASHES
        ++labelGBC.gridy;
        JLabel hashesLabel = new JLabel(Lang.getInstance().translate("HASHES") + ":");
        this.add(hashesLabel, labelGBC);

        //HASHES
        ++detailGBC.gridy;
        JTextPane txtAreaHashes = new JTextPane();
        txtAreaHashes.setContentType("text/html");
        //	txtAreaHashes.setBackground(MainFrame.getFrames()[0].getBackground());

        txtAreaHashes.setText("<html>" + String.join("<br />", r_Hashes.getHashesB58()) + "</html>");
        txtAreaHashes.setBorder(name.getBorder());
        txtAreaHashes.setEditable(false);
        //MenuPopupUtil.installContextMenu(txtAreaHashes);
        this.add(txtAreaHashes, detailGBC);

        //txtAreaHashes.setText(String.join(" ", r_Hashes.getHashesB58()));

        //PACK
//		this.pack();
//        this.setResizable(false);
//        this.setLocationRelativeTo(null);
        this.setVisible(true);

    }
}
