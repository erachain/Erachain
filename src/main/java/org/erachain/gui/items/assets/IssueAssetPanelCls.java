package org.erachain.gui.items.assets;

import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.item.assets.AssetType;
import org.erachain.gui.items.IssueItemPanel;
import org.erachain.gui.library.Library;
import org.erachain.gui.library.MDecimalFormatedTextField;
import org.erachain.gui.library.MultipleRoyaltyPanel;
import org.erachain.lang.Lang;

import javax.swing.*;
import java.awt.*;

/**
 * @author Саша
 */
public abstract class IssueAssetPanelCls extends IssueItemPanel {


    protected final JLabel quantityJLabel = new JLabel(Lang.T("Quantity") + ":");

    protected final JComboBox<AssetType> assetTypeJComboBox = new JComboBox();
    protected final JCheckBox isUnTransferable = new JCheckBox(Lang.T("NOT_TRANSFERABLE"));
    protected final JCheckBox isAnonimDenied = new JCheckBox(Lang.T("ANONIM_OWN_DENIED"));
    protected MultipleRoyaltyPanel multipleRoyaltyPanel = new MultipleRoyaltyPanel(fromJComboBox, assetTypeJComboBox);

    protected JTextPane textAreasAssetTypeDescription;
    protected MDecimalFormatedTextField textQuantity = new MDecimalFormatedTextField();

    protected AssetTypesComboBoxModel assetTypesComboBoxModel;

    protected final JLabel typeJLabel = new JLabel(Lang.T("Type") + ":");


    public IssueAssetPanelCls(String name, String title, String titleDescription, String issueMess, boolean useIcon,
                              int cropWidth, int cropHeight, boolean originalSize, boolean useExtURL) {
        super(name, title, titleDescription, issueMess, useIcon, cropWidth, cropHeight, originalSize, useExtURL);

        assetTypesComboBoxModel = new AssetTypesComboBoxModel();
        assetTypeJComboBox.setModel(assetTypesComboBoxModel);

        textQuantity.setText("0");


    }

    protected void initBottom(int gridy) {

        isUnTransferable.setToolTipText(Lang.T("IssueAssetPanel.isUnTransferable.tip"));
        fieldGBC.gridy = gridy++;
        jPanelAdd.add(isUnTransferable, fieldGBC);

        isAnonimDenied.setToolTipText(Lang.T("IssueAssetPanel.isAnonimDenied.tip"));
        fieldGBC.gridy = gridy++;
        jPanelAdd.add(isAnonimDenied, fieldGBC);

        fieldGBC.gridy = gridy++;
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = fieldGBC.gridy;
        gridBagConstraints.gridwidth = 9;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        jPanelAdd.add(multipleRoyaltyPanel, gridBagConstraints);

        super.initBottom(gridy);
    }

    long quantity;
    int assetType;

    @Override
    protected void makeAppData() {
        itemAppData = AssetCls.makeAppData(!addIconLabel.isInternalMedia(), addIconLabel.getMediaType(),
                !addImageLabel.isInternalMedia(), addImageLabel.getMediaType(),
                !startCheckBox.isSelected() ? null : startField.getCalendar().getTimeInMillis(),
                !stopCheckBox.isSelected() ? null : stopField.getCalendar().getTimeInMillis(),
                tagsField.getText(), multipleRoyaltyPanel.recipientsTableModel.getRecipients(),
                isUnTransferable.isSelected(), isAnonimDenied.isSelected());

    }

    @Override
    protected String makeTailView() {
        String out = "";
        out += Lang.T("Description") + ":<br>";
        if (item.getKey() > 0 && item.getKey() < 1000) {
            out += Library.to_HTML(Lang.T(item.viewDescription()));
        } else {
            out += Library.to_HTML(item.viewDescription());
        }

        return out;
    }

}
