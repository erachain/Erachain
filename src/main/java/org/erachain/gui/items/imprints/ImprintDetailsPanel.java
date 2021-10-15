package org.erachain.gui.items.imprints;

import org.erachain.core.item.imprints.ImprintCls;
import org.erachain.gui.library.MTextPane;
import org.erachain.lang.Lang;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class ImprintDetailsPanel extends JPanel {

    private static final long serialVersionUID = 4763074704570450206L;

    private ImprintCls imprint;

    public ImprintDetailsPanel(ImprintCls imprint) {
        this.imprint = imprint;

        //LAYOUT
        this.setLayout(new GridBagLayout());

        //PADDING
        this.setBorder(new EmptyBorder(5, 5, 5, 5));

        //LABEL GBC
        GridBagConstraints labelGBC = new GridBagConstraints();
        labelGBC.insets = new Insets(0, 5, 5, 0);
        labelGBC.fill = GridBagConstraints.HORIZONTAL;
        labelGBC.anchor = GridBagConstraints.NORTHWEST;
        labelGBC.weightx = 0;
        labelGBC.gridx = 0;

        //DETAIL GBC
        GridBagConstraints detailGBC = new GridBagConstraints();
        detailGBC.insets = new Insets(0, 5, 5, 0);
        detailGBC.fill = GridBagConstraints.HORIZONTAL;
        detailGBC.anchor = GridBagConstraints.NORTHWEST;
        detailGBC.weightx = 1;
        detailGBC.gridwidth = 2;
        detailGBC.gridx = 1;

        //LABEL KEY
        labelGBC.gridy = 1;
        JLabel keyLabel = new JLabel(Lang.T("Key") + ":");
        this.add(keyLabel, labelGBC);

        //KEY
        detailGBC.gridy = 1;
        JTextField txtKey = new JTextField(Long.toString(imprint.getKey()));
        txtKey.setEditable(false);
        this.add(txtKey, detailGBC);

        //LABEL NAME
        labelGBC.gridy = 2;
        JLabel nameLabel = new JLabel(Lang.T("Name") + ":");
        this.add(nameLabel, labelGBC);

        //NAME
        detailGBC.gridy = 2;
        JTextField txtName = new JTextField(imprint.viewName());
        txtName.setEditable(false);
        this.add(txtName, detailGBC);

        //LABEL DESCRIPTION
        labelGBC.gridy = 3;
        JLabel descriptionLabel = new JLabel(Lang.T("Description") + ":");
        this.add(descriptionLabel, labelGBC);

        //DESCRIPTION
        detailGBC.gridy = 3;
        MTextPane txtAreaDescription = new MTextPane(imprint.getDescription());
        txtAreaDescription.setBorder(txtName.getBorder());
        this.add(txtAreaDescription, detailGBC);

        //LABEL OWNER
        labelGBC.gridy = 4;
        JLabel makerLabel = new JLabel(Lang.T("Maker") + ":");
        this.add(makerLabel, labelGBC);

        //OWNER
        detailGBC.gridy = 4;
        JTextField maker = new JTextField(imprint.getMaker().getPersonAsString());
        maker.setEditable(false);
        this.add(maker, detailGBC);

        //PACK
        this.setVisible(true);
    }

}
