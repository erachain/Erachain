package org.erachain.gui2.lib;

import org.erachain.gui.library.ASMutableTreeNode;
import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;

public class AS_tt_Render extends DefaultTreeCellRenderer {

    public AS_tt_Render (){
        super();
    };

    //@Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        super.getTreeCellRendererComponent( tree,  value,  selected,  expanded,  leaf,  row,  hasFocus);
        try {
            if ( selected || expanded || hasFocus || tree.isCollapsed(row) || tree.isExpanded(row) || tree.isRowSelected(row) || tree.isSelectionEmpty())
            {
                ASMutableTreeNode vv = (ASMutableTreeNode) value;
                setText(" " + vv.getViewName());
                Image im = vv.getImage();
                int size1 = UIManager.getFont("TextField.font").getSize();
                setIcon(new ImageIcon(im.getScaledInstance(size1, size1, 0)));
            }
        } catch (Exception e) {
           // e.printStackTrace();
        }
        return this;
    }
}