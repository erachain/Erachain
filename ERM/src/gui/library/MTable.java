package gui.library;

import java.awt.FontMetrics;

import javax.swing.JTable;
import javax.swing.table.TableModel;

public class MTable extends JTable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public MTable(TableModel model) {
		super();
	// set model
		setModel(model);
	// height row in table	
		setRowHeight( (int) (getFontMetrics(getFont()).getHeight()));
	// set renders
	
	}

}
