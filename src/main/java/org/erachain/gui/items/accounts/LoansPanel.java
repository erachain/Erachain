package org.erachain.gui.items.accounts;

import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.item.ItemCls;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.gui.Gui;
import org.erachain.gui.items.assets.ComboBoxAssetsModel;
import org.erachain.gui.library.MTable;
import org.erachain.gui.models.AccountLoansTableModel;
import org.erachain.gui.models.FavoriteComboBoxModel;
import org.erachain.lang.Lang;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;

@SuppressWarnings("serial")
public class LoansPanel extends JPanel {

    public JComboBox<ItemCls> cbxFavorites;
    public AccountLoansTableModel tableModel;
    protected AssetCls asset;
    protected PublicKeyAccount pub_Key;
    MTable table;

    @SuppressWarnings("unchecked")
    public LoansPanel() {

        this.setLayout(new GridBagLayout());

        //PADDING
        this.setBorder(new EmptyBorder(10, 10, 10, 10));

        //BUTTON GBC
        GridBagConstraints buttonGBC = new GridBagConstraints();
        buttonGBC.insets = new Insets(10, 0, 10, 10);
        buttonGBC.fill = GridBagConstraints.BOTH;
        buttonGBC.anchor = GridBagConstraints.NORTHWEST;
        buttonGBC.gridwidth = 3;
        buttonGBC.weightx = 0.8;
        buttonGBC.gridx = 1;
        buttonGBC.gridy = 0;

        //ASSET FAVORITES
        cbxFavorites = new JComboBox<ItemCls>(new ComboBoxAssetsModel(AssetCls.ERA_KEY));
        cbxFavorites.setRenderer(new FavoriteComboBoxModel.IconListRenderer());
        this.add(cbxFavorites, buttonGBC);

        //TABLE
        tableModel = new AccountLoansTableModel((AssetCls) cbxFavorites.getSelectedItem());
        table = Gui.createSortableTable(tableModel, 0);

        TableColumn column_No = table.getColumnModel().getColumn(tableModel.COLUMN_NO);
        column_No.setMinWidth(50);
        column_No.setMaxWidth(150);
        column_No.setPreferredWidth(50);
        column_No.setWidth(50);
        column_No.sizeWidthToFit();

        //MENU
        //DealsPopupMenu menu = new DealsPopupMenu(tableModel, table, cbxFavorites);

        ////////////////////
        //TableMenuPopupUtil.installContextMenu(table, menu);  // SELECT ROW ON WHICH CLICKED RIGHT BUTTON

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Point p = e.getPoint();
                int row = table.rowAtPoint(p);
                table.setRowSelectionInterval(row, row);
            }
        });


        //ADD TOTAL BALANCE
        buttonGBC.insets = new Insets(10, 0, 0, 0);
        buttonGBC.fill = GridBagConstraints.NONE;
        buttonGBC.anchor = GridBagConstraints.NORTHWEST;
        buttonGBC.gridx = 1;
        buttonGBC.gridy = 2;
        final JLabel totalBalance = new JLabel(getTotals());
        this.add(totalBalance, buttonGBC);

        //ON TABLE CHANGE
        table.getModel().addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent arg0) {
                totalBalance.setText(getTotals());
            }
        });

        //ADD ACCOUNTS TABLE
        //TABLE GBC
        GridBagConstraints tableGBC = new GridBagConstraints();
        tableGBC.fill = GridBagConstraints.BOTH;
        tableGBC.anchor = GridBagConstraints.NORTHWEST;
        tableGBC.gridwidth = 10;
        tableGBC.weightx = 1.0;
        tableGBC.weighty = 0.1;
        tableGBC.gridx = 1;
        tableGBC.gridy = 1;
        this.add(new JScrollPane(table), tableGBC);

        //ON FAVORITES CHANGE
        cbxFavorites.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                // TODO Auto-generated method stub

                if (e.getStateChange() == ItemEvent.SELECTED) {
                    AssetCls asset = (AssetCls) cbxFavorites.getSelectedItem();
                    tableModel.setAsset(asset);
                }
            }
        });

    }


    private String getTotals() {
        BigDecimal total = tableModel.getTotalBalance();
        return Lang.T("Total Loan") + ": " + total.toPlainString();

    }

}
