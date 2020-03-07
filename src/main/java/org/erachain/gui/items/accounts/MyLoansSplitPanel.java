package org.erachain.gui.items.accounts;

import org.erachain.core.account.Account;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.gui.SplitPanel;
import org.erachain.gui.library.MainPanelInterface;
import org.erachain.lang.Lang;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;

// панель моих адресов
public class MyLoansSplitPanel extends SplitPanel implements MainPanelInterface {

    public AccountsPanel accountPanel;
    public AccountsRightPanel rightPanel;
    private String iconFile = "images/pageicons/MyLoansSplitPanel.png";

    public MyLoansSplitPanel() {
        super("MyLoansSplitPanel");
         //	LayoutManager favoritesGBC = this.getLayout();
        this.jScrollPanelLeftPanel.setVisible(false);
        this.searchToolBar_LeftPanel.setVisible(false);
        this.toolBarLeftPanel.setVisible(false);
        this.setName(Lang.getInstance().translate("My Loans"));
        this.jToolBarRightPanel.setVisible(false);

        GridBagConstraints PanelGBC = new GridBagConstraints();
        PanelGBC.fill = GridBagConstraints.BOTH;
        PanelGBC.anchor = GridBagConstraints.NORTHWEST;
        PanelGBC.weightx = 1;
        PanelGBC.weighty = 1;
        PanelGBC.gridx = 0;
        PanelGBC.gridy = 0;

        accountPanel = new AccountsPanel();
        rightPanel = new AccountsRightPanel();

        this.leftPanel.add(accountPanel, PanelGBC);
        //this.rightPanel1.add(rightPanel,PanelGBC);
        jScrollPaneJPanelRightPanel.setViewportView(rightPanel);

        // EVENTS on CURSOR
        accountPanel.table.getSelectionModel().addListSelectionListener(new Account_Tab_Listener());


    }


    class Account_Tab_Listener implements ListSelectionListener {

        //@SuppressWarnings("deprecation")
        @Override
        public void valueChanged(ListSelectionEvent arg0) {

            AssetCls asset = (AssetCls) accountPanel.cbxFavorites.getSelectedItem();
            Account account = null;
            if (accountPanel.table.getSelectedRow() >= 0)
                account = accountPanel.tableModel.getItem(accountPanel.table.convertRowIndexToModel(accountPanel.table.getSelectedRow()));
            //info1.show_001(person);
            //	rightPanel.jTable1.Search_Accoutnt_Transaction_From_Asset(account, asset);
//			my_Accounts_SplitPanel.rightPanel.jTable1.revalidate();
            // PersJSpline.setDividerLocation(PersJSpline.getDividerLocation());
            //my_Person_SplitPanel.jSplitPanel.setDividerLocation(my_Person_SplitPanel.jSplitPanel.getDividerLocation());
            ////my_Person_SplitPanel.searchTextFieldSearchToolBarLeftPanelDocument.setEnabled(true);


        }

    }
    @Override
    public Icon getIcon() {
        {
            try {
                return new ImageIcon(Toolkit.getDefaultToolkit().getImage(iconFile));
            } catch (Exception e) {
                return null;
            }
        }
    }


}
