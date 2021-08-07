package org.erachain.gui.items.assets;

import org.erachain.core.account.Account;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.lang.Lang;

import javax.swing.*;
import java.awt.*;


public class IssueAssetSeriesDialog extends JDialog {

    private static final long serialVersionUID = 2717571093561259483L;

    private Account account;
    private AssetCls origAsset;

    // Variables declaration - do not modify
    private JTextField jFormattedTextField_Fee;
    private JScrollPane jLabel_RecordInfo;

    public IssueAssetSeriesDialog(AssetCls origAsset, Account account) {
        toSign(origAsset, account);
    }

    public IssueAssetSeriesDialog(AssetCls origAsset) {
        toSign(origAsset, null);
    }

    private void toSign(AssetCls origAsset, Account account) {
        //ICON

        this.origAsset = origAsset;
        this.account = account;

        initComponents();

        setPreferredSize(new Dimension(800, 600));
        //setMinimumSize(new Dimension(1000, 600));
        //setMaximumSize(new Dimension(1000, 600));

        //refreshRecordDetails();

        this.setTitle(Lang.T("Issue Series of the Asset"));
        this.setResizable(true);
        this.setModal(true);

        //PACK
        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    private void refreshRecordDetails() {

        jLabel_RecordInfo.setViewportView(new AssetInfo(origAsset, false));

    }

    private void initComponents() {
        GridBagConstraints gridBagConstraints;

        jLabel_RecordInfo = new JScrollPane();

        jFormattedTextField_Fee = new JTextField();

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setModal(true);
        GridBagLayout layout = new GridBagLayout();
        getContentPane().setLayout(layout);

        jLabel_RecordInfo.setBorder(BorderFactory.createEtchedBorder());

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 19;
        gridBagConstraints.anchor = GridBagConstraints.PAGE_START;
        gridBagConstraints.insets = new Insets(1, 0, 29, 0);


        AssetInfo makePanel = new AssetInfo(origAsset, false);
        //jLabel_RecordInfo.setViewportView(makePanel);

        makePanel.setVisible(true);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 22;
        gridBagConstraints.gridwidth = 10;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.anchor = GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new Insets(0, 0, 0, 0);
        getContentPane().add(makePanel);


        pack();
    }

    /*
    @Override
    protected void afterSelectItem() {
        issueSeriesMenuItem.setEnabled(((AssetCls)itemTableSelected).isUnique());
    }

     */

}
