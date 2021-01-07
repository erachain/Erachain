package org.erachain.gui.items.assets;

import org.erachain.controller.Controller;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.gui.models.BalancesTableModel;
import org.erachain.lang.Lang;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
public class AssetFrame extends JFrame {
    private AssetCls asset;

    public AssetFrame(AssetCls asset) {
        super(Controller.getInstance().getApplicationName(false) + " - " + Lang.T("Check Details"));

        this.asset = asset;

        //ICON
        List<Image> icons = new ArrayList<Image>();
        icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon16.png"));
        icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon32.png"));
        icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon64.png"));
        icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon128.png"));
        this.setIconImages(icons);

        //CLOSE
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        //LAYOUT
        //this.setLayout(new GridBagLayout());

        //TAB PANE
        JTabbedPane tabPane = new JTabbedPane();

        //DETAILS
        tabPane.add(Lang.T("Details"), new AssetDetailsPanel(this.asset));

        //BALANCES
        BalancesTableModel balancesTableModel = new BalancesTableModel(asset, -1);
        final JTable balancesTable = new JTable(balancesTableModel);
        tabPane.add(Lang.T("Holders"), new JScrollPane(balancesTable));

        //ADD TAB PANE
        this.add(tabPane);

        //PACK
        this.pack();
        //this.setSize(500, this.getHeight());
        //this.setResizable(false);
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }
}
