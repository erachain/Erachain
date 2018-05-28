package gui.items.assets;

import core.item.assets.AssetCls;
import datachain.DCSet;
import lang.Lang;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Exchange_Panel extends JPanel {
    private static final long serialVersionUID = -7052380905136603354L;
    public CreateOrderPanel buyOrderPanel;
    Echange_Sell_Buy_Panel tt;
    String action;
    String account;
    java.awt.GridBagConstraints gridBagConstraints;
    private AssetCls have;
    private AssetCls want;
    private JButton change_Button;
    private JTextField jTextField_Asset_1;
    private JButton jButton_Change_Asset_1;
    private JTextField jTextField_Asset_2;
    private JButton jButton_Change_Asset_2;
    private JPanel jSelect_Trade;
    private JScrollPane jScrollPane_jPanel_RightPanel;

    public Exchange_Panel(AssetCls have, AssetCls want, String action, String account) {

        this.account = account;
        this.action = action;
        this.have = have;
        this.want = want;

        install();
        // this.setTitle(Lang.getInstance().translate("Erachain.org") + " - " +
        // Lang.getInstance().translate("Check Exchange")+" - " +
        // this.have.toString() + " / " + this.want.toString());
        initComponents();
    }

    public Exchange_Panel(AssetCls have, String account) {

        this.account = account;
        this.action = "";
        this.have = have;
        this.want = have;
        install();
        initComponents();

    }

    private void install() {
        if (have == null) {
            have = (AssetCls) DCSet.getInstance().getItemAssetMap().get((long) 2);
        }
        if (want == null) {
            want = (AssetCls) DCSet.getInstance().getItemAssetMap().get((long) 1);
        }
    }

    private void initComponents() {
        // LAYOUT
        this.setLayout(new GridBagLayout());
        // select panel
        jSelect_Trade = new javax.swing.JPanel();
        jSelect_Trade.setLayout(new java.awt.GridBagLayout());
        change_Button = new JButton();
        jTextField_Asset_1 = new javax.swing.JTextField();
        jButton_Change_Asset_1 = new javax.swing.JButton();
        jTextField_Asset_2 = new javax.swing.JTextField();
        jButton_Change_Asset_2 = new javax.swing.JButton();

        setLayout(new java.awt.GridBagLayout());

        change_Button.setText("");
        ImageIcon ic = new ImageIcon(Toolkit.getDefaultToolkit().getImage("images/icons/exchange.png"));
        change_Button.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                // TODO Auto-generated method stub
                AssetCls a = have;
                have = want;
                want = a;

                jTextField_Asset_1.setText(have.viewName());
                jScrollPane_jPanel_RightPanel.setViewportView(new Echange_Sell_Buy_Panel(have, want, action, account));

                jTextField_Asset_2.setText(want.viewName());
                jScrollPane_jPanel_RightPanel.setViewportView(new Echange_Sell_Buy_Panel(have, want, action, account));

            }

        });

        change_Button.setIcon(new ImageIcon(ic.getImage().getScaledInstance(20, 20, 1)));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(8, 0, 8, 0);
        jSelect_Trade.add(change_Button, gridBagConstraints);

        jTextField_Asset_1.setText(have.viewName());
        jTextField_Asset_1.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 0.3;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 0);
        jSelect_Trade.add(jTextField_Asset_1, gridBagConstraints);

        jButton_Change_Asset_1.setText(Lang.getInstance().translate("Search"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 8);
        jSelect_Trade.add(jButton_Change_Asset_1, gridBagConstraints);
        jButton_Change_Asset_1.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO Auto-generated method stub
                AssetPairSelect ss = new AssetPairSelect(want.getKey(), "", "");
                ss.assetPairSelectTableModel.removeObservers();
                if (ss.pairAsset != null) {
                    have = ss.pairAsset;
                    jTextField_Asset_1.setText(have.viewName());
                    jScrollPane_jPanel_RightPanel
                            .setViewportView(new Echange_Sell_Buy_Panel(have, want, action, account));
                }
            }
        });

        jTextField_Asset_2.setText(want.viewName());
        jTextField_Asset_2.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 0.3;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 0);
        jSelect_Trade.add(jTextField_Asset_2, gridBagConstraints);

        jButton_Change_Asset_2.setText(Lang.getInstance().translate("Search"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 8);
        jSelect_Trade.add(jButton_Change_Asset_2, gridBagConstraints);

        jButton_Change_Asset_2.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                // TODO Auto-generated method stub
                AssetPairSelect ss = new AssetPairSelect(have.getKey(), "", "");
                ss.assetPairSelectTableModel.removeObservers();
                if (ss.pairAsset != null) {
                    want = ss.pairAsset;

                    jTextField_Asset_2.setText(want.viewName());
                    jScrollPane_jPanel_RightPanel
                            .setViewportView(new Echange_Sell_Buy_Panel(have, want, action, account));

                }

            }

        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        add(jSelect_Trade, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 0.2;
        gridBagConstraints.weighty = 0.2;

        jScrollPane_jPanel_RightPanel = new javax.swing.JScrollPane();
        // jScrollPane_jPanel_RightPanel.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);

        // jScrollPane_jPanel_RightPanel.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        // jScrollPane_jPanel_RightPanel.setHorizontalScrollBar(null);
        // jScrollPane_jPanel_RightPanel.setVerticalScrollBar(null);
        jScrollPane_jPanel_RightPanel.setViewportView(new Echange_Sell_Buy_Panel(have, want, action, account));
        // tt = new pane_Tab(have, want, action, account);
        add(jScrollPane_jPanel_RightPanel, gridBagConstraints);
    }

}
