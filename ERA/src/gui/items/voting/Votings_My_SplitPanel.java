	package gui.items.voting;

	import java.awt.Dimension;
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
import core.voting.Poll;
import gui.Split_Panel;
import gui.library.MTable;
	import gui.models.WalletItemPersonsTableModel;
import gui.models.WalletPollsTableModel;
import lang.Lang;


	public class Votings_My_SplitPanel extends Split_Panel{
		private static final long serialVersionUID = 2717571093561259483L;

		
		private WalletPollsTableModel my_Voting_Model;
		private MTable my_Voting_table;
		private TableRowSorter my_Sorter;
		// для прозрачности
	     int alpha =255;
	     int alpha_int;
		
		
	public Votings_My_SplitPanel(){
	super("Votings_My_SplitPanel");
		this.setName(Lang.getInstance().translate("My Votings"));
			this.searthLabel_SearchToolBar_LeftPanel.setText(Lang.getInstance().translate("Search") +":  ");
			// not show buttons
			this.button1_ToolBar_LeftPanel.setVisible(false);
			this.button2_ToolBar_LeftPanel.setVisible(false);
			this.jButton1_jToolBar_RightPanel.setVisible(false);
			this.jButton2_jToolBar_RightPanel.setVisible(false);
			// not show My filter
			this.searth_My_JCheckBox_LeftPanel.setVisible(false);
			//TABLE
			my_Voting_Model = new WalletPollsTableModel();
			my_Voting_table = new MTable(my_Voting_Model);
			my_Sorter = new TableRowSorter(my_Voting_Model);
			my_Voting_table.setRowSorter(my_Sorter);
			my_Voting_table.getRowSorter();
			if (my_Voting_Model.getRowCount() > 0) my_Voting_Model.fireTableDataChanged();
			
			//CHECKBOX FOR CONFIRMED
			TableColumn confirmedColumn = my_Voting_table.getColumnModel().getColumn(WalletItemPersonsTableModel.COLUMN_CONFIRMED);
			confirmedColumn.setMinWidth(50);
			confirmedColumn.setMaxWidth(50);
			confirmedColumn.setPreferredWidth(50);//.setWidth(30);
			
			// UPDATE FILTER ON TEXT CHANGE
			this.searchTextField_SearchToolBar_LeftPanel.getDocument().addDocumentListener(new My_Search());
			// SET VIDEO			
			this.jTable_jScrollPanel_LeftPanel.setModel(my_Voting_Model);
			this.jTable_jScrollPanel_LeftPanel = my_Voting_table;
			this.jScrollPanel_LeftPanel.setViewportView(this.jTable_jScrollPanel_LeftPanel);		
			// EVENTS on CURSOR
			my_Voting_table.getSelectionModel().addListSelectionListener(new My_Tab_Listener());
	//		 Dimension size = MainFrame.getInstance().desktopPane.getSize();
	//		 this.setSize(new Dimension((int)size.getWidth()-100,(int)size.getHeight()-100));
	//		 jSplitPanel.setDividerLocation((int)(size.getWidth()/1.618));
		}

		class My_Tab_Listener implements ListSelectionListener {
			@Override
			public void valueChanged(ListSelectionEvent arg0) {
				Poll voting =null;
				if (my_Voting_table.getSelectedRow() >= 0 )voting  = my_Voting_Model.getPoll(my_Voting_table.convertRowIndexToModel(my_Voting_table.getSelectedRow()));
				if (voting == null)return;
				VotingDetailPanel votingDetailsPanel = new VotingDetailPanel(voting, Controller.getInstance().getAsset(AssetCls.FEE_KEY));
					jScrollPane_jPanel_RightPanel.setViewportView(votingDetailsPanel);
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
				my_Voting_Model.fireTableDataChanged();
			
				RowFilter filter = RowFilter.regexFilter(".*" + search + ".*", 1);
				((DefaultRowSorter)  my_Sorter).setRowFilter(filter);
					
				my_Voting_Model.fireTableDataChanged();

			}
		}
		
		
	
	}




