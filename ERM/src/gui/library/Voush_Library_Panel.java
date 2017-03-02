package gui.library;

import java.awt.GridBagConstraints;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import core.transaction.Transaction;
import gui.items.statement.Statements_Vouch_Table_Model;
import gui.models.Renderer_Left;
import lang.Lang;

public class Voush_Library_Panel extends JPanel {

	/**
	 * view VOUSH PANEL
	 */
	private static final long serialVersionUID = 1L;
	private JScrollPane jScrollPane_Tab_Vouches;
	private GridBagConstraints gridBagConstraints;

	public Voush_Library_Panel(Transaction transaction) {

		this.setName(Lang.getInstance().translate("Certified"));
		Statements_Vouch_Table_Model model = new Statements_Vouch_Table_Model(transaction);
		JTable jTable_Vouches = new JTable(model);
		TableColumnModel column_mod = jTable_Vouches.getColumnModel();
		TableColumn col_data = column_mod.getColumn(model.COLUMN_TIMESTAMP);
		col_data.setMinWidth(50);
		col_data.setMaxWidth(200);
		col_data.setPreferredWidth(120);// .setWidth(30);

		jTable_Vouches.setDefaultRenderer(String.class, new Renderer_Left(
				jTable_Vouches.getFontMetrics(jTable_Vouches.getFont()), model.get_Column_AutoHeight())); // set renderer

		// jPanel_Tab_Vouch = new javax.swing.JPanel();
		jScrollPane_Tab_Vouches = new javax.swing.JScrollPane();

		this.setLayout(new java.awt.GridBagLayout());

		jScrollPane_Tab_Vouches.setViewportView(jTable_Vouches);

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
		gridBagConstraints.weightx = 0.1;
		gridBagConstraints.weighty = 0.1;
		gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
		this.add(jScrollPane_Tab_Vouches, gridBagConstraints);

	}

}
