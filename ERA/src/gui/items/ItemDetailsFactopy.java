package gui.items;

import core.item.ItemCls;
import core.item.unions.UnionCls;
import gui.items.unions.Union_Info;

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
        int in = item.getItemTypeInt();

        switch (in) {
            case ItemCls.ASSET_TYPE:
                return null;
            case ItemCls.IMPRINT_TYPE:
                return null;
            case ItemCls.TEMPLATE_TYPE:
                return null;
            case ItemCls.PERSON_TYPE:
                return null;
            case ItemCls.UNION_TYPE:
                Union_Info cc;
                cc = new Union_Info();
                cc.show_Union_001((UnionCls) item);
                return cc;
        }
        return null;

    }

}
