package org.erachain.gui.items.assets;

import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.core.block.GenesisBlock;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;
import org.erachain.gui2.MainPanel;
import org.erachain.lang.Lang;
import org.erachain.settings.Settings;
import org.erachain.utils.NumberAsString;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class AssetDetailsPanel extends JPanel {

    private static final long serialVersionUID = 4763074704570450206L;

    private AssetCls asset;

    private JButton favoritesButton;

    public AssetDetailsPanel(AssetCls asset) {
        this.asset = asset;
        this.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        //LAYOUT

        this.setLayout(new GridBagLayout());

        //PADDING
        //	this.setBorder(new EmptyBorder(5, 5, 5, 5));
        //	this.setSize(50,100);
        int gridy = 0;
        //LABEL GBC
        GridBagConstraints labelGBC = new GridBagConstraints();
        labelGBC.insets = new Insets(0, 5, 5, 0);
        labelGBC.fill = GridBagConstraints.BOTH;
        labelGBC.anchor = GridBagConstraints.NORTHWEST;
        labelGBC.weightx = 1;
        labelGBC.weighty = 1;
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
        labelGBC.gridy = ++gridy;
        JLabel keyLabel = new JLabel(Lang.T("Key") + ":");
        this.add(keyLabel, labelGBC);

        //KEY
        detailGBC.gridy = gridy;
        JTextField txtKey = new JTextField(Long.toString(asset.getKey()));
        txtKey.setEditable(false);
        this.add(txtKey, detailGBC);

        //LABEL SEQ-NO
        labelGBC.gridy = ++gridy;
        this.add(new JLabel(Lang.T("Block-SeqNo") + ":"), labelGBC);

        // SEQ-NO
        Transaction record = Transaction.findByDBRef(DCSet.getInstance(), asset.getReference());
        detailGBC.gridy = gridy;
        JTextField txtSeqNo = new JTextField(record.viewHeightSeq());
        txtSeqNo.setEditable(false);
        this.add(txtSeqNo, detailGBC);

        //LABEL NAME
        labelGBC.gridy = ++gridy;
        JLabel nameLabel = new JLabel(Lang.T("Name") + ":");
        this.add(nameLabel, labelGBC);

        //NAME
        detailGBC.gridy = gridy;
        JTextField txtName = new JTextField(asset.viewName());
        txtName.setEditable(false);
        this.add(txtName, detailGBC);

        //LABEL DESCRIPTION
        labelGBC.gridy = ++gridy;
        JLabel descriptionLabel = new JLabel(Lang.T("Description") + ":");
        this.add(descriptionLabel, labelGBC);

        //DESCRIPTION
        detailGBC.gridy = gridy;
        JTextArea txtAreaDescription;
        if (asset.getKey() > 0 && asset.getKey() < 1000) {
            txtAreaDescription = new JTextArea(Lang.T(asset.viewDescription()));
        } else {
            txtAreaDescription = new JTextArea(asset.viewDescription());
        }
        txtAreaDescription.setRows(4);
        txtAreaDescription.setBorder(txtName.getBorder());
        txtAreaDescription.setEditable(false);
        this.add(txtAreaDescription, detailGBC);

        //LABEL OWNER
        labelGBC.gridy = ++gridy;
        JLabel makerLabel = new JLabel(Lang.T("Maker") + ":");
        this.add(makerLabel, labelGBC);

        //OWNER
        Account maker = asset.getMaker();
        detailGBC.gridy = gridy;
        JTextField makerTxt = new JTextField(GenesisBlock.CREATOR.equals(maker) ? "GENESIS" : maker.getAddress());
        makerTxt.setEditable(false);
        this.add(makerTxt, detailGBC);

        String personStr = maker.viewPerson();
        if (personStr.length() > 0) {
            //LABEL PERSON
            detailGBC.gridy = ++gridy;
            this.add(new JLabel(personStr), detailGBC);

        }

        if (false) {
            //LABEL DIVISIBLE
            labelGBC.gridy = ++gridy;
            this.add(new JLabel(Lang.T("Movable") + ":"), labelGBC);
    
            //DIVISIBLE
            detailGBC.gridy = gridy;
            JCheckBox chkMovable = new JCheckBox();
            chkMovable.setSelected(asset.isMovable());
            chkMovable.setEnabled(false);
            this.add(chkMovable, detailGBC);
        }


        //LABEL QUANTITY
        labelGBC.gridy = ++gridy;
        JLabel quantityLabel = new JLabel(Lang.T("Quantity") + ":");
        this.add(quantityLabel, labelGBC);

        //QUANTITY
        detailGBC.gridy = gridy;
        JTextField txtQuantity = new JTextField(NumberAsString.formatAsString(asset.getQuantity()));
        txtQuantity.setEditable(false);
        this.add(txtQuantity, detailGBC);

        //LABEL RELEASED
        labelGBC.gridy = ++gridy;
        this.add(new JLabel(Lang.T("Released") + ":"), labelGBC);
        //RELEASED
        detailGBC.gridy = gridy;
        JTextField txtReleased = new JTextField(NumberAsString.formatAsString(asset.getReleased()));
        txtReleased.setEditable(false);
        this.add(txtReleased, detailGBC);

        //LABEL TYPE
        labelGBC.gridy = ++gridy;
        JLabel divisibleLabel = new JLabel(Lang.T("Type") + ":");
        this.add(divisibleLabel, labelGBC);

        //TYPE
        detailGBC.gridy = gridy;
        JTextField textType = new JTextField(Lang.T(asset.viewAssetTypeFull()));

        //	textType.setSelected(asset.isDivisible()); // SELECT - OPION = asset.getAssetType();
        //	asset.viewAssetType()
        //	int option = asset.getAssetType();

        textType.setEnabled(false);
        this.add(textType, detailGBC);

        //IF ASSET CONFIRMED
        if (this.asset.getKey() >= 0) {
            //ADD ERM PAIR BUTTON
            labelGBC.gridy++;
            labelGBC.gridwidth = 2;
            JButton openPairButton = new JButton(Lang.T("Open pair"));
            openPairButton.setPreferredSize(new Dimension(200, 25));
            openPairButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    onOpenPairClick();
                }
            });
            //		this.add(openPairButton, labelGBC);
        }

        //IF ASSET CONFIRMED AND NOT ERM
        if (this.asset.getKey() >= AssetCls.INITIAL_FAVORITES) {
            //FAVORITES
            labelGBC.gridy++;
            labelGBC.gridwidth = 2;
            this.favoritesButton = new JButton();

            //CHECK IF FAVORITES
            if (Controller.getInstance().isItemFavorite(asset)) {
                this.favoritesButton.setText(Lang.T("Remove Favorite"));
            } else {
                this.favoritesButton.setText(Lang.T("Add Favorite"));
            }

            this.favoritesButton.setPreferredSize(new Dimension(200, 25));
            this.favoritesButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    onFavoriteClick();
                }
            });
            //		this.add(this.favoritesButton, labelGBC);

        }

        //PACK
        this.setVisible(true);
    }

    public void onOpenPairClick() {

        String action = null;
        AssetCls assetSell = Settings.getInstance().getDefaultPairAsset();
        ExchangePanel panel = new ExchangePanel(asset, assetSell, action, "");
        panel.setName(asset.getTickerName() + "/" + assetSell.getTickerName());
        MainPanel.getInstance().insertNewTab(Lang.T("Exchange") + ":" + asset.getKey(),
                panel);

    }

    public void onFavoriteClick() {
        //CHECK IF FAVORITES
        if (Controller.getInstance().isItemFavorite(asset)) {
            this.favoritesButton.setText(Lang.T("Add Favorite"));
            Controller.getInstance().removeItemFavorite(this.asset);
        } else {
            this.favoritesButton.setText(Lang.T("Remove Favorite"));
            Controller.getInstance().addItemFavorite(this.asset);
        }

    }

}
