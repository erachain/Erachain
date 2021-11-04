package org.erachain.gui.items.accounts;

import org.erachain.core.account.Account;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.gui.SplitPanel;
import org.erachain.lang.Lang;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;


public class MyLoansSplitPanel extends SplitPanel {

    private static final long serialVersionUID = 1L;
    public LoansPanel loansPanel;
    public AssetCls assetSelect;
    private Account selectArg;
    private AccountsRightPanel rightPanel;

    public static String NAME = "MyLoansSplitPanel";
    public static String TITLE = "My Loans";

    public MyLoansSplitPanel() {
        super(NAME, TITLE);

        this.jScrollPanelLeftPanel.setVisible(false);
        this.searchToolBar_LeftPanel.setVisible(false);
        this.toolBarLeftPanel.setVisible(false);
        this.setName(Lang.T("My Loans"));
        this.jToolBarRightPanel.setVisible(false);

        GridBagConstraints PanelGBC = new GridBagConstraints();
        PanelGBC.fill = GridBagConstraints.BOTH;
        PanelGBC.anchor = GridBagConstraints.NORTHWEST;
        PanelGBC.weightx = 1;
        PanelGBC.weighty = 1;
        PanelGBC.gridx = 0;
        PanelGBC.gridy = 0;
        loansPanel = new LoansPanel();
        this.leftPanel.add(loansPanel, PanelGBC);
        // EVENTS on CURSOR
        loansPanel.table.getSelectionModel().addListSelectionListener(new Account_Tab_Listener());
        rightPanel = new AccountsRightPanel();

        //   this.repaint();

    }

    @Override
    public void onClose() {
        rightPanel.tableModel.deleteObservers();
        loansPanel.tableModel.deleteObservers();
    }

    class Account_Tab_Listener implements ListSelectionListener {

        @Override
        public void valueChanged(ListSelectionEvent arg0) {

            AssetCls asset = (AssetCls) loansPanel.cbxFavorites.getSelectedItem();
            Account account = null;
            if (loansPanel.table.getSelectedRow() >= 0)
                account = loansPanel.tableModel.getItem(loansPanel.table.convertRowIndexToModel(loansPanel.table.getSelectedRow()));

            if (account == null || asset == null || account.equals(selectArg) && asset.equals(assetSelect)) return;
            selectArg = account;
            assetSelect = asset;
            rightPanel.tableModel.setAccount(account);
            rightPanel.tableModel.fireTableDataChanged();
            rightPanel.setAsset(asset);
            jScrollPaneJPanelRightPanel.setViewportView(rightPanel);

        }

    }

}
