package org.erachain.gui.items.templates;

import org.erachain.core.item.ItemCls;
import org.erachain.core.item.templates.TemplateCls;
import org.erachain.gui.items.ItemSplitPanel;
import org.erachain.gui.library.MainPanelInterface;
import org.erachain.gui.models.WalletItemTemplatesTableModel;
import org.erachain.lang.Lang;
import org.erachain.settings.Settings;
import org.erachain.utils.URLViewer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.net.URL;


public class TemplateMySplitPanel extends ItemSplitPanel implements MainPanelInterface {
    private static final long serialVersionUID = 2717571093561259483L;
    private String iconFile = "images/pageicons/TemplateMySplitPanel.png";
    //private TemplateMySplitPanel th;

    public TemplateMySplitPanel() {
        super(new WalletItemTemplatesTableModel(), "TemplateMySplitPanel");

        this.setName(Lang.getInstance().translate("My Templates"));

        //      add items in menu

        JMenuItem setSeeInBlockexplorer = new JMenuItem(Lang.getInstance().translate("Check in Blockexplorer"));

        setSeeInBlockexplorer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                try {
                    URLViewer.openWebpage(new URL("http://" + Settings.getInstance().getBlockexplorerURL()
                            + ":" + Settings.getInstance().getWebPort() + "/index/blockexplorer.html"
                            + "?template=" + itemTableSelected.getKey()));
                } catch (MalformedURLException e1) {
                    logger.error(e1.getMessage(), e1);                }
            }
        });

        menuTable.add(setSeeInBlockexplorer);

    }

    // show details
    @Override
    public Component getShow(ItemCls item) {
        return new InfoTemplates((TemplateCls) item);
    }

    @Override
    public Image getIcon() {
        {
            try {
                return Toolkit.getDefaultToolkit().getImage(iconFile);
            } catch (Exception e) {
                return null;
            }
        }
    }
}
