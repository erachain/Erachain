package gui.models;

import javax.swing.table.AbstractTableModel;

import database.SortableList;

@SuppressWarnings("serial")
public abstract class TableModelCls<T, U> extends AbstractTableModel  {

	public abstract SortableList<T, U> getSortableList();
	
}
