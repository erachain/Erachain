package gui.items.notes;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.DefaultComboBoxModel;

import utils.ObserverMessage;
import controller.Controller;
import core.item.ItemCls;
import core.item.notes.NoteCls;
import gui.items.ComboBoxModelItems;

@SuppressWarnings("serial")
public class ComboBoxModelItemsNotes_2 extends ComboBoxModelItems
{
	
	public ComboBoxModelItemsNotes_2()
	{
		super(ObserverMessage.LIST_NOTE_FAVORITES_TYPE, ItemCls.NOTE_TYPE);
	}
}
