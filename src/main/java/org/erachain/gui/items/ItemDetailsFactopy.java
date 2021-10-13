package org.erachain.gui.items;

import org.erachain.core.item.ItemCls;
import org.erachain.core.item.unions.UnionCls;
import org.erachain.gui.items.unions.UnionInfo;

import javax.swing.*;

public class ItemDetailsFactopy extends JPanel {

    private static final long serialVersionUID = 4763074704570450206L;
    private static ItemDetailsFactopy instance;
    private ItemCls item;
    private JButton favoritesButton;

    private ItemDetailsFactopy() {

    }

    public static ItemDetailsFactopy getInstance() {
        if (instance == null) {
            instance = new ItemDetailsFactopy();
        }
        return instance;
    }

    public Object show(ItemCls item) {
        int in = item.getItemType();

        switch (in) {
            case ItemCls.ASSET_TYPE:
            case ItemCls.IMPRINT_TYPE:
            case ItemCls.TEMPLATE_TYPE:
            case ItemCls.PERSON_TYPE:
            case ItemCls.AUTHOR_TYPE:
            case ItemCls.POLL_TYPE:
                return null;
            case ItemCls.UNION_TYPE:
                UnionInfo cc;
                cc = new UnionInfo();
                cc.show_Union_001((UnionCls) item);
                return cc;
        }
        return null;

    }

}
