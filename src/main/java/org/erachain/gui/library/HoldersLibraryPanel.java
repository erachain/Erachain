package org.erachain.gui.library;

import org.erachain.core.item.assets.AssetCls;
import org.erachain.gui.models.BalancesTableModel;
import org.erachain.gui.models.RendererBigDecimals;
import org.erachain.lang.Lang;

import javax.swing.*;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class HoldersLibraryPanel extends JPanel {


    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private BalancesTableModel balancesTableModel;

    public HoldersLibraryPanel(AssetCls asset, int balanceIndex) {
        super();
        
        switch (balanceIndex) {
            case 1:
                this.setName(Lang.T("Owners"));
                break;
            case 2:
                this.setName(Lang.T("Debtors"));
                break;
            case 3:
                this.setName(Lang.T("Holders"));
                break;
            case 4:
                this.setName(Lang.T("Spenders"));
                break;
            default:
                this.setName(Lang.T("Balances"));
                break;
        }

        JScrollPane jScrollPane_Tab_Holders = new javax.swing.JScrollPane();

        this.setLayout(new java.awt.GridBagLayout());


        balancesTableModel = new BalancesTableModel(asset, balanceIndex);
        @SuppressWarnings("rawtypes")
        MTable jTable1 = new MTable(balancesTableModel);
        
        TableRowSorter t = new TableRowSorter(balancesTableModel);
        List<RowSorter.SortKey> sortKeys = new ArrayList<RowSorter.SortKey>();
        sortKeys.add(new RowSorter.SortKey(balancesTableModel.COLUMN_OWN, SortOrder.DESCENDING));
        t.setSortKeys(sortKeys);
        jTable1.setRowSorter(t);
        
        jTable1.setDefaultRenderer(BigDecimal.class, new RendererBigDecimals(asset.getScale()));
        //  jTable1.setMinimumSize(new Dimension(0,0));

        //    Dimension d = jTable1.getPreferredSize();
        //    d.height = 300;
        //     jTable1.setPreferredScrollableViewportSize(d);


        jScrollPane_Tab_Holders.setViewportView(jTable1);

        GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        this.add(jScrollPane_Tab_Holders, gridBagConstraints);


    }

}
