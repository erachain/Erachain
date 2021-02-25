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
        title = Lang.T(s);
        icon = i;

    }

    public ASMutableTreeNode(String s, Image i) {
        super(s);
        title = Lang.T(s);
        icon = i;

    }

    public ASMutableTreeNode(String o, String s) {
        super(o);
        title = Lang.T(s);
        icon = IconPanel.getIcon(o);

    }

    public ASMutableTreeNode(String s) {
        super(s);
        title = Lang.T(s);
    }

    public String getTitle() {
        return title;
    }

    public Image getImage() {
        return icon;
    }
}
