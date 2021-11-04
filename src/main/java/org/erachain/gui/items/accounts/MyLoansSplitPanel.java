package org.erachain.gui.items.accounts;

import org.erachain.core.account.Account;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.gui.SplitPanel;
import org.erachain.lang.Lang;
import org.mapdb.Fun;
import org.mapdb.Fun.Tuple2;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.math.BigDecimal;


public class MyLoansSplitPanel extends SplitPanel {

    public LoansPanel loansPanel;
    public AssetCls assetSelect;
    private Tuple2<PublicKeyAccount, Account> selectArg;
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
            Fun.Tuple3<PublicKeyAccount, Account, BigDecimal> item = null;
            if (loansPanel.table.getSelectedRow() >= 0) {
                item = loansPanel.tableModel.getItem(loansPanel.table.convertRowIndexToModel(loansPanel.table.getSelectedRow()));
            }

            if (item == null || asset == null || item.equals(selectArg) && asset.equals(assetSelect)) return;
            selectArg = new Tuple2<>(item.a, item.b);
            assetSelect = asset;
            rightPanel.tableModel.setAccount(item.a);
            //rightPanel.tableModel.fireTableDataChanged();
            rightPanel.setAsset(asset);
            jScrollPaneJPanelRightPanel.setViewportView(rightPanel);

        }

    }

}
