package gui.items.polls;

import core.item.ItemCls;
import core.item.assets.AssetCls;
import core.item.polls.PollCls;
import gui.items.ComboBoxModelItemsAll;
import lang.Lang;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

@SuppressWarnings("serial")
public class PollsDetailPanel extends JPanel {

    PollTabPane pollTabPane;
    private JComboBox<ItemCls> cbxAssets;

    public PollsDetailPanel(PollCls poll, AssetCls asset) {
        // CREATE FRAME
        // super(Lang.getInstance().translate("Erachain.org") + " - " +
        // Lang.getInstance().translate("Poll Details"));

        // ICON
        // List<Image> icons = new ArrayList<Image>();
        // icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon16.png"));
        // icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon32.png"));
        // icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon64.png"));
        // icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon128.png"));
        // this.setIconImages(icons);

        // LAYOUT
        this.setLayout(new GridBagLayout());

        // ASSET LABEL GBC
        GridBagConstraints assetLabelGBC = new GridBagConstraints();
        assetLabelGBC.insets = new Insets(5, 5, 5, 5);
        assetLabelGBC.fill = GridBagConstraints.HORIZONTAL;
        assetLabelGBC.anchor = GridBagConstraints.CENTER;
        assetLabelGBC.weightx = 0;
        assetLabelGBC.gridwidth = 1;
        assetLabelGBC.gridx = 0;
        assetLabelGBC.gridy = 1;
        // assetLabelGBC.weightx=0.1;
        // assetLabelGBC.weighty=0.1;

        // ASSETS GBC
        GridBagConstraints assetsGBC = new GridBagConstraints();
        assetsGBC.insets = new Insets(5, 5, 5, 5);
        assetsGBC.fill = GridBagConstraints.HORIZONTAL;
        assetsGBC.anchor = GridBagConstraints.NORTHWEST;
        assetsGBC.weightx = 0;
        assetsGBC.gridwidth = 1;
        assetsGBC.gridx = 1;
        assetsGBC.gridy = 1;
        assetsGBC.weightx = 0.1;

        // POLLTABPANE GBC
        GridBagConstraints pollTabPaneGBC = new GridBagConstraints();
        pollTabPaneGBC.insets = new Insets(0, 5, 5, 0);
        pollTabPaneGBC.fill = GridBagConstraints.HORIZONTAL;
        pollTabPaneGBC.anchor = GridBagConstraints.NORTHWEST;
        pollTabPaneGBC.weightx = 0;
        pollTabPaneGBC.gridwidth = 2;
        pollTabPaneGBC.gridx = 0;
        pollTabPaneGBC.gridy = 2;
        pollTabPaneGBC.weightx = 0.1;

        this.add(new JLabel(Lang.getInstance().translate("Check") + ":"), assetLabelGBC);

        cbxAssets = new JComboBox<ItemCls>(new ComboBoxModelItemsAll(ItemCls.ASSET_TYPE));
        cbxAssets.setSelectedItem(asset);
        this.add(cbxAssets, assetsGBC);

        cbxAssets.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                AssetCls asset = ((AssetCls) cbxAssets.getSelectedItem());

                if (asset != null) {
                    pollTabPane.setAsset(asset);
                }
            }
        });

        // POLL TABPANE
        this.pollTabPane = new PollTabPane(poll, asset);
        /*
         * //ON CLOSE this.addWindowListener(new WindowAdapter() { public void
         * windowClosing(WindowEvent e) { //CLOSE POLL FRME
         * votingTabPane.close();
         *
         * //DISPOSE setVisible(false); dispose(); } });
         */
        // ADD POLL TABPANE TO FRAME
        this.add(this.pollTabPane, pollTabPaneGBC);

        // SHOW FRAME
        // this.pack();
        // this.setLocationRelativeTo(null);
        this.setVisible(true);
    }
}
