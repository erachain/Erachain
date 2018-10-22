package org.erachain.gui.items.assets;

import org.erachain.lang.Lang;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

//import java.math.BigDecimal;
//import org.erachain.settings.Settings;

@SuppressWarnings("serial")
public class IssueAssetDialog extends JDialog //JFrame
{


    public IssueAssetDialog() {
        this.setTitle(Lang.getInstance().translate("Erachain.org") + " - " + Lang.getInstance().translate("Issue Asset"));
        //CLOSE
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setModal(true);
        //ICON
        List<Image> icons = new ArrayList<Image>();
        icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon16.png"));
        icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon32.png"));
        icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon64.png"));
        icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon128.png"));
        this.setIconImages(icons);

        //LAYOUT
        this.setLayout(new GridBagLayout());

        //PADDING
        ((JComponent) this.getContentPane()).setBorder(new EmptyBorder(5, 5, 5, 5));

        IssueAssetPanel Issue_Assets_SplitPanel = new IssueAssetPanel();
        Issue_Assets_SplitPanel.setName(Lang.getInstance().translate("Issue Asset"));
    }
}
