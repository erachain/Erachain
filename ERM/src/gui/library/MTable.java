package gui.library;

import java.awt.FontMetrics;

import javax.swing.JTable;
import javax.swing.table.TableModel;

public class MTable extends JTable {

	public MTable(TableModel model) {
		super();
		setModel(model);
		// set auto row Heighy
		set_Row_Height();
	}

	public void set_Row_Height() {
		FontMetrics fontMetrics = getFontMetrics(getFont());
		double textHeight = fontMetrics.getHeight();
		for (int ii = 0; ii < getRowCount(); ii++) {
			
			setRowHeight(ii, (int) (textHeight));
		}

	}

}
