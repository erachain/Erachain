package org.erachain.gui.Wallets;

import org.erachain.gui.SplitPanel;
import org.erachain.gui.items.accounts.AccountsPanel;
import org.erachain.gui.items.accounts.AccountsRightPanel;
import org.erachain.gui.library.fileChooser;
import org.erachain.lang.Lang;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class WalletsManagerSplitPanel extends SplitPanel {

    public AccountsPanel accountPanel;
    public AccountsRightPanel rightPanel;
    private int spt = 1;

    public WalletsManagerSplitPanel() {
        super("WalletsManagerSplitPanel");
//		LayoutManager favoritesGBC = this.getLayout();
        this.jScrollPanel_LeftPanel.setVisible(false);
        this.searchToolBar_LeftPanel.setVisible(false);
        this.toolBar_LeftPanel.setVisible(true);
        this.button1_ToolBar_LeftPanel.setVisible(true);
        this.button1_ToolBar_LeftPanel.setBorder(new LineBorder(Color.BLACK));
        this.button1_ToolBar_LeftPanel.setText(Lang.getInstance().translate("Add wallet"));
        this.button2_ToolBar_LeftPanel.setVisible(false);

        this.setName(Lang.getInstance().translate("Wallets Manager"));
        this.jToolBar_RightPanel.setVisible(false);

        button1_ToolBar_LeftPanel.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                // TODO Auto-generated method stub
                fileChooser chooser = new fileChooser();
                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                chooser.setMultiSelectionEnabled(false);
                chooser.setDialogTitle(Lang.getInstance().translate("Open Wallet") + "...");
                int returnVal = chooser.showOpenDialog(getParent());
                if (returnVal == JFileChooser.APPROVE_OPTION) {

                    File file = new File(chooser.getSelectedFile().getPath());
                    // если размер больше 30к то не вставляем

                }

            }

        });


        GridBagConstraints PanelGBC = new GridBagConstraints();
        PanelGBC.fill = GridBagConstraints.BOTH;
        PanelGBC.anchor = GridBagConstraints.NORTHWEST;
        PanelGBC.weightx = 1;
        PanelGBC.weighty = 1;
        PanelGBC.gridx = 0;
        PanelGBC.gridy = 0;

        //	accountPanel = new AccountsPanel();
        //		rightPanel = new AccountsRightPanel();

        //	this.leftPanel.add( accountPanel, PanelGBC);
        //this.rightPanel1.add(rightPanel,PanelGBC);
        //	jScrollPane_jPanel_RightPanel.setViewportView(rightPanel);
        //	 this.jSplitPanel.setDividerLocation(0.3);


        this.repaint();

    }


}
