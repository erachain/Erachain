package gui.items.imprints;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.DefaultTableModel;
import javax.validation.constraints.Null;

import lang.Lang;

@SuppressWarnings("serial")
public class Table_Model_Issue_Hashes extends DefaultTableModel {
	
	public Table_Model_Issue_Hashes(int rows) {
		super(new Object[] { Lang.getInstance().translate("Hash 32 bytes"),
				Lang.getInstance().translate("Description") }, rows);
		
	//	this.addRow(new Object[]{"", ""});
	}

	@Override
	public boolean isCellEditable(int row, int column)
	{
		if (column == 0) {
			return true;			
		}
		return true;
	}
	
	public Class<? extends Object> getColumnClass(int c) {     // set column type
		Object o = getValueAt(0, c);
		return o==null?Null.class:o.getClass();
	   }
	
	
	
	public Object getValueAt(int row, int col){
		
		if (this.getRowCount()<row || this.getRowCount() ==0 )return null;
		
		
		return super.getValueAt(row, col);
		
		
		
	}
	
	@Override 
	public void setValueAt(Object aValue, int row, int column)
	{
		//IF STRING
		if(aValue instanceof String)
		{
			//CHECK IF NOT EMPTY
			if(((String) aValue).length() > 0)
			{
				//CHECK IF LAST ROW
				if(row == this.getRowCount()-1)
				{
			//		this.addRow(new Object[]{"", ""});
				}
				
				super.setValueAt(aValue, row, column);
			}
		}
		else
		{
			super.setValueAt(aValue, row, column);
			
			//CHECK IF LAST ROW
			if(row == this.getRowCount()-1)
			{
			//	this.addRow(new Object[]{"", ""});
			}
		}
	}
	
	public List<String> getValues(int column)
	{
		List<String> values = new ArrayList<String>();
		
		for(int i=0; i<this.getRowCount(); i++)
		{
			String value = String.valueOf(this.getValueAt(i, column));
			
			if(value.length() > column)
			{
		//		values.add(value);
			}
		}
		
		return values;
	}
	
}