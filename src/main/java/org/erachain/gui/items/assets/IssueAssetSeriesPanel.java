package org.erachain.gui.items.assets;

import org.erachain.core.item.statuses.StatusCls;
import org.erachain.gui.items.IssueItemPanel;
import org.erachain.gui.items.utils.GUIConstants;
import org.erachain.gui.library.MDecimalFormatedTextField;
import org.erachain.lang.Lang;

import javax.swing.*;

public class IssueAssetSeriesPanel extends IssueItemPanel {

    public static String NAME = "IssueAssetSeriesPanel";
    public static String TITLE = "Issue Asset Series";

    //private JCheckBox jcheckUnique;
    public JTextField assetRefField = new JTextField("");
    private MDecimalFormatedTextField totalField = new MDecimalFormatedTextField(Short.class);

    public IssueAssetSeriesPanel() {
        super(NAME, TITLE, "Series issue has been sent", true,
                GUIConstants.WIDTH_IMAGE, GUIConstants.WIDTH_IMAGE, true, true);

        initComponents();

        // вывод верхней панели
        int gridy = initTopArea(true);

        JLabel signLabel = new JLabel(Lang.T("Asset Issue TX Signature") + ":");
        labelGBC.gridy = gridy;
        jPanelAdd.add(signLabel, labelGBC);

        fieldGBC.gridy = gridy++;
        jPanelAdd.add(assetRefField, fieldGBC);

        JLabel totalLabel = new JLabel(Lang.T("Series Total") + ":");
        labelGBC.gridy = gridy;
        jPanelAdd.add(totalLabel, labelGBC);

        fieldGBC.gridy = gridy++;
        jPanelAdd.add(totalField, fieldGBC);

        // вывод подвала
        initBottom(gridy);

        setVisible(true);
    }

    @Override
    protected boolean checkValues() {
        return true;
    }

    @Override
    protected void makeTransaction() {

        //transaction = Controller.getInstance().issueStatus(itemAppData, creator,
        //        exLink, nameField.getText(), textAreaDescription.getText(), unique,
        //        addIconLabel.getMediaBytes(), addImageLabel.getMediaBytes(),
        //        feePow);

    }

    @Override
    protected String makeBodyView() {

        String out = super.makeBodyView();

        StatusCls status = (StatusCls) item;
        out += Lang.T("Unique") + ": " + status.isUnique()
                + "<br>";

        return out;

    }

}
