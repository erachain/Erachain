package gui.library;

import java.awt.GridBagConstraints;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.table.TableRowSorter;

import core.item.assets.AssetCls;
import gui.models.BalancesTableModel;
import gui.models.Renderer_BigDecimals;
import lang.Lang;

public class Holders_Library_Panel extends JPanel {


    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private BalancesTableModel balancesTableModel;

    public Holders_Library_Panel(AssetCls asset, int balanceIndex) {
        super();
        
        switch (balanceIndex) {
            case 1:
                this.setName(Lang.getInstance().translate("Owners"));
                break;
            case 2:
                this.setName(Lang.getInstance().translate("Debtors"));
                break;
            case 3:
                this.setName(Lang.getInstance().translate("Holders"));
                break;
            case 4:
                this.setName(Lang.getInstance().translate("Spenders"));
                break;
            default:
                this.setName(Lang.getInstance().translate("Balances"));
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
        
        jTable1.setDefaultRenderer(BigDecimal.class, new Renderer_BigDecimals(asset.getScale()));
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
