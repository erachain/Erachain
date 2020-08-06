package org.erachain.gui.Wallets;

import org.erachain.gui.SplitPanel;
import org.erachain.gui.items.accounts.AccountsPanel;
import org.erachain.gui.items.accounts.AccountsRightPanel;
import org.erachain.gui.library.FileChooser;
import org.erachain.lang.Lang;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class WalletsManagerSplitPanel extends SplitPanel {

    public static String NAME = "WalletsManagerSplitPanel";
    public static String TITLE = "Wallets Manager";

    public AccountsPanel accountPanel;
    public AccountsRightPanel rightPanel;
    private int spt = 1;

    public WalletsManagerSplitPanel() {
        super(NAME, TITLE);

//		LayoutManager favoritesGBC = this.getLayout();
        this.jScrollPanelLeftPanel.setVisible(false);
        this.searchToolBar_LeftPanel.setVisible(false);
        this.toolBarLeftPanel.setVisible(true);
        this.button1ToolBarLeftPanel.setVisible(true);
        this.button1ToolBarLeftPanel.setBorder(new LineBorder(Color.BLACK));
        this.button1ToolBarLeftPanel.setText(Lang.getInstance().translate("Add wallet"));
        this.button2ToolBarLeftPanel.setVisible(false);

        this.setName(Lang.getInstance().translate("Wallets Manager"));
        this.jToolBarRightPanel.setVisible(false);

        button1ToolBarLeftPanel.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                // TODO Auto-generated method stub
                FileChooser chooser = new FileChooser();
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
        //	jScrollPaneJPanelRightPanel.setViewportView(rightPanel);
        //	 this.jSplitPanel.setDividerLocation(0.3);


        this.repaint();

    }
}