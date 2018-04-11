package gui.items.templates;

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
import core.item.templates.TemplateCls;
import gui.items.ComboBoxModelItems;

@SuppressWarnings("serial")
public class ComboBoxModelItemsTemplates_2 extends ComboBoxModelItems
{
	
	public ComboBoxModelItemsTemplates_2()
	{
		super(ObserverMessage.LIST_TEMPLATE_FAVORITES_TYPE, ItemCls.TEMPLATE_TYPE);
	}
}
