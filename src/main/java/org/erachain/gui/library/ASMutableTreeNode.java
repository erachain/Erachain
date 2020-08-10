package org.erachain.gui.library;

import org.erachain.gui.IconPanel;
import org.erachain.lang.Lang;

import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;


// Onject from view in tt GUI2
public class ASMutableTreeNode extends DefaultMutableTreeNode {
    String title;
    Image icon;

    public ASMutableTreeNode(String o, String s, Image i) {
        super(o);
        title = Lang.getInstance().translate(s);
        icon = i;

    }

    public ASMutableTreeNode(String o, String s) {
        super(o);
        title = Lang.getInstance().translate(s);
        icon = IconPanel.getIcon(o);

    }

    public String getTitle() {
        return title;
    }

    public Image getImage() {
        return icon;
    }
}
