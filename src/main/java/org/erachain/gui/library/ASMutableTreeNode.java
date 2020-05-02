package org.erachain.gui.library;

import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;


// Onject from view in tt GUI2
public class ASMutableTreeNode extends DefaultMutableTreeNode {
    String viewName;
    Image im;

    public ASMutableTreeNode(Object o, String s, Image i){
        super(o);
       viewName =s;
       im =i;

    }

    public String getViewName() {
        return viewName;
    }

    public Image getImage() {
        return im;
    }
}
