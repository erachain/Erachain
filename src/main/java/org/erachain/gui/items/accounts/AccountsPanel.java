package org.erachain.gui.items.accounts;

import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.item.ItemCls;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.gui.Gui;
import org.erachain.gui.items.assets.ComboBoxAssetsModel;
import org.erachain.gui.library.DealsPopupMenu;
import org.erachain.gui.library.MTable;
import org.erachain.gui.library.WalletCreateAccountButton;
import org.erachain.gui.library.WalletSyncButton;
import org.erachain.gui.models.AccountsTableModel;
import org.erachain.gui.models.FavoriteComboBoxModel;
import org.erachain.lang.Lang;
import org.erachain.utils.TableMenuPopupUtil;
import org.mapdb.Fun;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
public class AccountsPanel extends JPanel // implements ItemListener


//JInternalFrame
{
    //private JFrame parent;

    public JComboBox<ItemCls> cbxFavorites;
    public AccountsTableModel tableModel;
    public WalletCreateAccountButton newAccount_Button;
    public WalletSyncButton reload_Button;
    protected AssetCls asset;
    //protected Account account;
    protected PublicKeyAccount pub_Key;
    MTable table;
    private AccountsPanel th;

    @SuppressWarnings("unchecked")
    public AccountsPanel() {
        th = this;
        //this.parent = parent;
        this.setLayout(new GridBagLayout());

        //PADDING
        this.setBorder(new EmptyBorder(10, 10, 10, 10));


        //TABLE GBC
        GridBagConstraints tableGBC = new GridBagConstraints();
        tableGBC.fill = GridBagConstraints.BOTH;
        tableGBC.anchor = GridBagConstraints.NORTHWEST;
        tableGBC.gridwidth = 3;
        tableGBC.weightx = 1.0;
        tableGBC.weighty = 0.1;
        tableGBC.gridx = 1;
        tableGBC.gridy = 1;

        //BUTTON GBC
        GridBagConstraints buttonGBC = new GridBagConstraints();
        buttonGBC.insets = new Insets(10, 0, 0, 0);
        buttonGBC.fill = GridBagConstraints.NONE;
        buttonGBC.anchor = GridBagConstraints.NORTHWEST;
        buttonGBC.gridx = 1;
        buttonGBC.gridy = 2;

        //FAVORITES GBC
        GridBagConstraints favoritesGBC = new GridBagConstraints();
        favoritesGBC.insets = new Insets(10, 0, 10, 0);
        favoritesGBC.fill = GridBagConstraints.BOTH;
        favoritesGBC.anchor = GridBagConstraints.NORTHWEST;
        favoritesGBC.weightx = 0.8;
        favoritesGBC.gridx = 1;
        favoritesGBC.gridy = 0;

        //ASSET FAVORITES
        cbxFavorites = new JComboBox<ItemCls>(new ComboBoxAssetsModel());
        cbxFavorites.setRenderer(new FavoriteComboBoxModel.IconListRenderer());
        this.add(cbxFavorites, favoritesGBC);


        favoritesGBC.insets = new Insets(10, 10, 10, 0);
        favoritesGBC.fill = GridBagConstraints.BOTH;
        favoritesGBC.anchor = GridBagConstraints.NORTHWEST;
        favoritesGBC.weightx = 0.1;
        favoritesGBC.gridx = 2;
        favoritesGBC.gridy = 0;


        newAccount_Button = new WalletCreateAccountButton();
        this.add(newAccount_Button, favoritesGBC);


        reload_Button = new WalletSyncButton();
        favoritesGBC.insets = new Insets(10, 10, 10, 0);
        favoritesGBC.fill = GridBagConstraints.BOTH;
        favoritesGBC.anchor = GridBagConstraints.NORTHWEST;
        favoritesGBC.weightx = 0.1;
        favoritesGBC.gridx = 3;
        favoritesGBC.gridy = 0;

        this.add(reload_Button, favoritesGBC);


        //TABLE
        tableModel = new AccountsTableModel();
        // start data in model
        tableModel.setAsset((AssetCls) cbxFavorites.getSelectedItem());
        table = Gui.createSortableTable(tableModel, 1);

        if (false) {
            //TableRowSorter<AccountsTableModel> sorter =  (TableRowSorter<AccountsTableModel>) table.getRowSorter();
            //sorter.setComparator(0, new IntegerComparator());
            RowSorter sorter = new TableRowSorter(tableModel);
            table.setRowSorter(sorter);

            List<RowSorter.SortKey> sortKeys = new ArrayList<>(4);
            sortKeys.add(new RowSorter.SortKey(0, SortOrder.ASCENDING));
            // IF NEED add SORTED ROW
            //sortKeys.add(new RowSorter.SortKey(1, SortOrder.ASCENDING));
            sorter.setSortKeys(sortKeys);
            //sorter.setComparator(AccountsTableModel.COLUMN_FEE_BALANCE, new BigDecimalStringComparator());
        }
        // render


        // column size

        TableColumn column_No = table.getColumnModel().getColumn(tableModel.COLUMN_NO);
        column_No.setMinWidth(50);
        column_No.setMaxWidth(150);
        column_No.setPreferredWidth(50);
        column_No.setWidth(50);
        column_No.sizeWidthToFit();


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

        //MENU
        DealsPopupMenu menu = new DealsPopupMenu(tableModel, table, cbxFavorites);

        ////////////////////
        TableMenuPopupUtil.installContextMenu(table, menu);  // SELECT ROW ON WHICH CLICKED RIGHT BUTTON

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Point p = e.getPoint();
                int row = table.rowAtPoint(p);
                table.setRowSelectionInterval(row, row);
            }
        });


        //ADD TOTAL BALANCE
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
        this.add(new JScrollPane(table), tableGBC);

    }
	

    private String getTotals() {
        Fun.Tuple4<BigDecimal, BigDecimal, BigDecimal, BigDecimal> total = tableModel.getTotalBalance();
        return Lang.T("Confirmed Balance") + ": "
                + total.a.toPlainString() + " / "
                + total.b.toPlainString() + " / "
                + total.c.toPlainString() + " / "
                + total.d.toPlainString();

    }

}
