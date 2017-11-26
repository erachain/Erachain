package gui.models;

import javax.swing.table.AbstractTableModel;

import core.item.ItemCls;
import datachain.SortableList;

@SuppressWarnings("serial")
public abstract class TableModelCls<T, U> extends AbstractTableModel  {

	public int COLUMN_FAVORITE=1000;
	
	public abstract SortableList<T, U> getSortableList();

	public abstract Object getItem(int k);
}
