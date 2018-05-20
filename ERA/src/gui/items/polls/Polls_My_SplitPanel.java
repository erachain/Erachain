package gui.items.polls;

import javax.swing.DefaultRowSorter;
import javax.swing.RowFilter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;

import controller.Controller;
import core.item.assets.AssetCls;
import core.item.polls.PollCls;
import gui.Split_Panel;
import gui.library.MTable;
import gui.models.WalletItemPersonsTableModel;
import gui.models.WalletItemPollsTableModel;
import lang.Lang;

public class Polls_My_SplitPanel extends Split_Panel {
	private static final long serialVersionUID = 2717571093561259483L;

	private WalletItemPollsTableModel my_Poll_Model;
	private MTable my_Poll_table;
	private TableRowSorter my_Sorter;
	// для прозрачности
	int alpha = 255;
	int alpha_int;

	public Polls_My_SplitPanel() {
		super("Polls_My_SplitPanel");
		this.setName(Lang.getInstance().translate("My Polls"));
		this.searthLabel_SearchToolBar_LeftPanel.setText(Lang.getInstance().translate("Search") + ":  ");
		// not show buttons
		this.button1_ToolBar_LeftPanel.setVisible(false);
		this.button2_ToolBar_LeftPanel.setVisible(false);
		this.jButton1_jToolBar_RightPanel.setVisible(false);
		this.jButton2_jToolBar_RightPanel.setVisible(false);
		// not show My filter
		this.searth_My_JCheckBox_LeftPanel.setVisible(false);
		// TABLE
		my_Poll_Model = new WalletItemPollsTableModel();
		my_Poll_table = new MTable(my_Poll_Model);
		my_Sorter = new TableRowSorter(my_Poll_Model);
		my_Poll_table.setRowSorter(my_Sorter);
		my_Poll_table.getRowSorter();
		if (my_Poll_Model.getRowCount() > 0)
			my_Poll_Model.fireTableDataChanged();

		// CHECKBOX FOR CONFIRMED
		TableColumn confirmedColumn = my_Poll_table.getColumnModel()
				.getColumn(WalletItemPersonsTableModel.COLUMN_CONFIRMED);
		confirmedColumn.setMinWidth(50);
		confirmedColumn.setMaxWidth(50);
		confirmedColumn.setPreferredWidth(50);// .setWidth(30);

		// UPDATE FILTER ON TEXT CHANGE
		this.searchTextField_SearchToolBar_LeftPanel.getDocument().addDocumentListener(new My_Search());
		// SET VIDEO
		this.jTable_jScrollPanel_LeftPanel.setModel(my_Poll_Model);
		this.jTable_jScrollPanel_LeftPanel = my_Poll_table;
		this.jScrollPanel_LeftPanel.setViewportView(this.jTable_jScrollPanel_LeftPanel);
		// EVENTS on CURSOR
		my_Poll_table.getSelectionModel().addListSelectionListener(new My_Tab_Listener());
		// Dimension size = MainFrame.getInstance().desktopPane.getSize();
		// this.setSize(new
		// Dimension((int)size.getWidth()-100,(int)size.getHeight()-100));
		// jSplitPanel.setDividerLocation((int)(size.getWidth()/1.618));
	}

	class My_Tab_Listener implements ListSelectionListener {
		@Override
		public void valueChanged(ListSelectionEvent arg0) {
			PollCls poll = null;
			if (my_Poll_table.getSelectedRow() >= 0)
				poll = my_Poll_Model.getPoll(my_Poll_table.convertRowIndexToModel(my_Poll_table.getSelectedRow()));
			if (poll == null)
				return;
			PollsDetailPanel pollDetailsPanel = new PollsDetailPanel(poll,
					Controller.getInstance().getAsset(AssetCls.FEE_KEY));
			jScrollPane_jPanel_RightPanel.setViewportView(pollDetailsPanel);
		}

	}

	class My_Search implements DocumentListener {
		public void changedUpdate(DocumentEvent e) {
			onChange();
		}

		public void removeUpdate(DocumentEvent e) {
			onChange();
		}

		public void insertUpdate(DocumentEvent e) {
			onChange();
		}

		public void onChange() {
			// GET VALUE
			String search = searchTextField_SearchToolBar_LeftPanel.getText();
			// SET FILTER
			my_Poll_Model.fireTableDataChanged();

			RowFilter filter = RowFilter.regexFilter(".*" + search + ".*", 1);
			((DefaultRowSorter) my_Sorter).setRowFilter(filter);

			my_Poll_Model.fireTableDataChanged();

		}
	}

	@Override
	public void delay_on_close() {
		// delete observer left panel
		my_Poll_Model.removeObservers();
		// get component from right panel
		// Component c1 = jScrollPane_jPanel_RightPanel.getViewport().getView();
		// if Person_Info 002 delay on close
		// if (c1 instanceof Statement_Info) (
		// (Statement_Info)c1).delay_on_Close();

	}

}
