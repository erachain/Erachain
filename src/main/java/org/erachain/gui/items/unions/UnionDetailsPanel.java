package org.erachain.gui.items.unions;

import org.erachain.controller.Controller;
import org.erachain.core.item.unions.UnionCls;
import org.erachain.gui.library.Library;
import org.erachain.gui.library.MTextPane;
import org.erachain.lang.Lang;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class UnionDetailsPanel extends JPanel {

    private static final long serialVersionUID = 4763074704570450206L;

    private UnionCls union;

    private JButton favoritesButton;

    public UnionDetailsPanel(UnionCls union) {
        this.union = union;

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
        ++labelGBC.gridy;
        JLabel keyLabel = new JLabel(Lang.getInstance().translate("Key") + ":");
        this.add(keyLabel, labelGBC);

        //KEY
        ++detailGBC.gridy;
        JTextField txtKey = new JTextField(Long.toString(union.getKey()));
        txtKey.setEditable(false);
        this.add(txtKey, detailGBC);

        //LABEL NAME
        ++labelGBC.gridy;
        JLabel nameLabel = new JLabel(Lang.getInstance().translate("Name") + ":");
        this.add(nameLabel, labelGBC);

        //NAME
        ++detailGBC.gridy;
        JTextField txtName = new JTextField(union.viewName());
        txtName.setEditable(false);
        this.add(txtName, detailGBC);

        //LABEL DESCRIPTION
        ++labelGBC.gridy;
        JLabel descriptionLabel = new JLabel(Lang.getInstance().translate("Description") + ":");
        this.add(descriptionLabel, labelGBC);

        //DESCRIPTION
        ++detailGBC.gridy;
        MTextPane txtAreaDescription = new MTextPane(union.getDescription());
        //txtAreaDescription.setRows(4);
        txtAreaDescription.setBorder(txtName.getBorder());
        //txtAreaDescription.setEditable(false);
        this.add(txtAreaDescription, detailGBC);

        //LABEL CREAtoR
        ++labelGBC.gridy;
        JLabel ownerLabel = new JLabel(Lang.getInstance().translate("Creator") + ":");
        this.add(ownerLabel, labelGBC);

        //OWNER
        ++detailGBC.gridy;
        JTextField owner = new JTextField(union.getOwner().getAddress());
        owner.setEditable(false);
        this.add(owner, detailGBC);

        String unionStr = union.getOwner().viewPerson();
        if (unionStr.length() > 0) {
            //LABEL UNION
            ++labelGBC.gridy;
            ++detailGBC.gridy;
            this.add(new JLabel(unionStr), detailGBC);
        }

        //IF UNION CONFIRMED
        if (this.union.getKey() >= 0) {
            //ADD ERM PAIR BUTTON
            ++labelGBC.gridy;
            labelGBC.gridwidth = 2;
            JButton openPairButton = new JButton(Lang.getInstance().translate("Open pair"));
            openPairButton.setPreferredSize(new Dimension(200, 25));
            openPairButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    onOpenPairClick();
                }
            });
            this.add(openPairButton, labelGBC);
        }

        //IF UNION CONFIRMED AND NOT ERM
        if (this.union.getKey() > 2l) {
            //FAVORITES
            ++labelGBC.gridy;
            labelGBC.gridwidth = 2;
            this.favoritesButton = new JButton();

            //CHECK IF FAVORITES
            if (Controller.getInstance().isItemFavorite(union)) {
                this.favoritesButton.setText(Lang.getInstance().translate("Remove Favorite"));
            } else {
                this.favoritesButton.setText(Lang.getInstance().translate("Add Favorite"));
            }

            this.favoritesButton.setPreferredSize(new Dimension(200, 25));
            this.favoritesButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    onFavoriteClick();
                }
            });
            this.add(this.favoritesButton, labelGBC);

        }

        //PACK
        this.setVisible(true);
    }

    public void onOpenPairClick() {

        //new UnionPairSelect(this.union.getKey());

    }

    public void onFavoriteClick() {
        //CHECK IF FAVORITES
        if (Controller.getInstance().isItemFavorite(union)) {
            this.favoritesButton.setText(Lang.getInstance().translate("Add Favorite"));
            Controller.getInstance().removeItemFavorite(this.union);
        } else {
            this.favoritesButton.setText(Lang.getInstance().translate("Remove Favorite"));
            Controller.getInstance().addItemFavorite(this.union);
        }

    }

}
