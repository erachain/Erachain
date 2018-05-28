package gui.items;

import controller.Controller;
import core.item.ItemCls;

import javax.swing.*;
import java.util.Collection;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@SuppressWarnings("serial")
public class ComboBoxModelItemsAll extends DefaultComboBoxModel<ItemCls> {
    Lock lock = new ReentrantLock();

    public ComboBoxModelItemsAll(int itemType) {
        Collection<ItemCls> allItems = Controller.getInstance().getAllItems(itemType);

        for (ItemCls item : allItems) {
            this.addElement(item);
        }

        this.setSelectedItem(this.getElementAt(0));
    }
}
