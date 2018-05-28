package gui.library;

import core.item.assets.AssetCls;
import gui.models.BalancesTableModel;
import lang.Lang;

import javax.swing.*;
import java.awt.*;

public class Holders_Library_Panel extends JPanel {


    private BalancesTableModel balancesTableModel;

    public Holders_Library_Panel(AssetCls asset) {
        super();
        this.setName(Lang.getInstance().translate("Holders"));

        JScrollPane jScrollPane_Tab_Holders = new javax.swing.JScrollPane();

        this.setLayout(new java.awt.GridBagLayout());


        balancesTableModel = new BalancesTableModel(asset.getKey());
        MTable jTable1 = new MTable(balancesTableModel);

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
