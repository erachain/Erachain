package gui.items.accounts;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.zip.DataFormatException;

import javax.swing.DefaultRowSorter;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.RowFilter;
import javax.swing.RowSorter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.mapdb.Fun.Tuple2;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

import controller.Controller;
import database.wallet.AccountsPropertisMap;
import datachain.SortableList;
import gui.Split_Panel;
import gui.library.MTable;
import gui.library.My_JFileChooser;
import gui.models.WalletItemImprintsTableModel;
import gui.settings.SettingsFrame;
import lang.Lang;
import settings.Settings;
import utils.Pair;
import utils.SaveStrToFile;
import utils.Zip_Bytes;

public class Accounts_Name_Search_SplitPanel extends Split_Panel{

	static Logger LOGGER = Logger.getLogger(Accounts_Name_Search_SplitPanel.class.getName());
/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
private Accounts_Name_TableModel tableModelImprints;
private JButton button3_ToolBar_LeftPanel;
protected My_JFileChooser chooser;

public Accounts_Name_Search_SplitPanel(){
	super("Accounts_Name_Search_SplitPanel");

	setName(Lang.getInstance().translate("Name Accounts"));
	searthLabel_SearchToolBar_LeftPanel.setText(Lang.getInstance().translate("Search") +":  ");
	searthLabel_SearchToolBar_LeftPanel.setVisible(true);
//	this.searchTextField_SearchToolBar_LeftPanel.setVisible(true);
//	this.searchToolBar_LeftPanel.setVisible(true);
// not show buttons
//	button1_ToolBar_LeftPanel.setVisible(false);
//	button2_ToolBar_LeftPanel.setVisible(false);
	button1_ToolBar_LeftPanel.setText(Lang.getInstance().translate("Load"));
	button2_ToolBar_LeftPanel.setText(Lang.getInstance().translate("Save"));
	button3_ToolBar_LeftPanel = new JButton();
	button3_ToolBar_LeftPanel.setText(Lang.getInstance().translate("Add"));
	this.toolBar_LeftPanel.add(button3_ToolBar_LeftPanel);
	jButton1_jToolBar_RightPanel.setVisible(false);
	jButton2_jToolBar_RightPanel.setVisible(false);
	
//CREATE TABLE
	this.tableModelImprints = new Accounts_Name_TableModel();
	final MTable imprintsTable = new MTable(this.tableModelImprints);

//CHECKBOX FOR FAVORITE
//	TableColumn favoriteColumn = imprintsTable.getColumnModel().getColumn(TableModelUnions.COLUMN_FAVORITE);
//	favoriteColumn.setCellRenderer(new Renderer_Boolean()); //unionsTable.getDefaultRenderer(Boolean.class));
//	favoriteColumn.setMinWidth(50);
//	favoriteColumn.setMaxWidth(50);
//	favoriteColumn.setPreferredWidth(50);//.setWidth(30);
// column #1
	TableColumn column1 = imprintsTable.getColumnModel().getColumn(WalletItemImprintsTableModel.COLUMN_KEY);//.COLUMN_CONFIRMED);
	column1.setMinWidth(1);
	column1.setMaxWidth(1000);
	column1.setPreferredWidth(50);

// set showvideo			
	jTable_jScrollPanel_LeftPanel.setModel(this.tableModelImprints);
	jTable_jScrollPanel_LeftPanel = imprintsTable;
	jScrollPanel_LeftPanel.setViewportView(jTable_jScrollPanel_LeftPanel);
	
	// Event LISTENER		
	jTable_jScrollPanel_LeftPanel.getSelectionModel().addListSelectionListener(new ListSelectionListener()  {
				@Override
				public void valueChanged(ListSelectionEvent arg0) {
	/*				ImprintCls imprint = null;
					if (jTable_jScrollPanel_LeftPanel.getSelectedRow() >= 0 ) imprint = tableModelImprints.getImprint(jTable_jScrollPanel_LeftPanel.convertRowIndexToModel(jTable_jScrollPanel_LeftPanel.getSelectedRow()));
					
					
					
				//	info.show_001(person);
					
				//	search_Person_SplitPanel.jSplitPanel.setDividerLocation(search_Person_SplitPanel.jSplitPanel.getDividerLocation());	
				//	search_Person_SplitPanel.searchTextField_SearchToolBar_LeftPanel.setEnabled(true);
					 Imprints_Info_Panel info_panel = new Imprints_Info_Panel(imprint);
					info_panel.setPreferredSize(new Dimension(jScrollPane_jPanel_RightPanel.getSize().width-50,jScrollPane_jPanel_RightPanel.getSize().height-50));
					jScrollPane_jPanel_RightPanel.setViewportView(info_panel);
		*/		}
				
			});
	
	
	
	
	
	/*
// MENU
	JPopupMenu nameSalesMenu = new JPopupMenu();
	JMenuItem details = new JMenuItem(Lang.getInstance().translate("Details"));
	details.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			int row = imprintsTable.getSelectedRow();
			row = imprintsTable.convertRowIndexToModel(row);
			ImprintCls imprint = tableModelImprints.getImprint(row);
			new ImprintFrame(imprint);
		}
	});
	nameSalesMenu.add(details);
	imprintsTable.setComponentPopupMenu(nameSalesMenu);
	imprintsTable.addMouseListener(new MouseAdapter() {
	@Override
		public void mousePressed(MouseEvent e) {
			Point p = e.getPoint();
			int row = imprintsTable.rowAtPoint(p);
			imprintsTable.setRowSelectionInterval(row, row);
			if(e.getClickCount() == 2)
			{
				row = imprintsTable.convertRowIndexToModel(row);
				ImprintCls imprint = tableModelImprints.getImprint(row);
				new ImprintFrame(imprint);
			}
		}
	});
	*/
	button2_ToolBar_LeftPanel.addActionListener(new ActionListener(){

		@SuppressWarnings("unchecked")
		@Override
		public void actionPerformed(ActionEvent arg0) {
			// TODO Auto-generated method stub
			chooser = new My_JFileChooser();
  			
  			chooser.setDialogTitle(Lang.getInstance().translate("Save File") );
  			//chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
  			chooser.setDialogType(javax.swing.JFileChooser.SAVE_DIALOG);
  			chooser.setMultiSelectionEnabled(false);
  		//	chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
  			//chooser.setAcceptAllFileFilterUsed(false); 
  			 // add filters
  			 FileNameExtensionFilter xmlFilter = new FileNameExtensionFilter("Era Name Accounts files (*.enaf)", "enaf");
  			chooser.setAcceptAllFileFilterUsed(false);//only filter
  	        chooser.addChoosableFileFilter(xmlFilter);
  	        chooser.setFileFilter(xmlFilter);
  			
  			 if ( chooser.showSaveDialog(getParent()) == JFileChooser.APPROVE_OPTION ) {
  	          
  				String pp = chooser.getSelectedFile().getPath();
  				if (!pp.contains(".enaf")) pp += ".enaf";
  				File ff = new File (pp);
  				// if file  
  				if (ff.exists() && ff.isFile()) {
  					int aaa = JOptionPane.showConfirmDialog(chooser, Lang.getInstance().translate("File") +  Lang.getInstance().translate("Exists") + "! " + Lang.getInstance().translate("Overwrite") + "?",  Lang.getInstance().translate("Message"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE );
  				System.out.print("\n gggg " + aaa);	
  				if (aaa != 0){
  					return;
  				}
  				ff.delete();
  				
  				}
  				
  				
  				 try(FileOutputStream fos=new FileOutputStream(pp))
  		        {
  		           
  					
  					// buffer
  					JSONObject output = new JSONObject();
  					
  					SortableList<String, Tuple2<String, String>> lists = Controller.getInstance().wallet.database.getAccountsPropertisMap().getList();
  					
  					for(Pair<String, Tuple2<String, String>> list:lists){
  						JSONObject account = new JSONObject();
  						account.put("name", list.getB().a);
  						account.put("json", list.getB().b);
  						output.put(list.getA(), account);
  					
  					}
  		         //   byte[] buffer =(byte[]) ;
  		            // copy buffer in file
  		         ///   fos.write(buffer, 0, buffer.length);
  					try {
  						SaveStrToFile.saveJsonFine(pp, output);			
  					} catch (IOException e) {
  						LOGGER.error(e.getMessage(),e);
  						JOptionPane.showMessageDialog(
  								new JFrame(), "Error writing to the file: " + Settings.getInstance().getSettingsPath()
  										+ "\nProbably there is no access.",
  				                "Error!",
  				                JOptionPane.ERROR_MESSAGE);
  					}

  		        
  		        }
  		        catch(IOException ex){
  
  		            System.out.println(ex.getMessage());
  		        } 
  			 } 
		}
	});
	
	button1_ToolBar_LeftPanel.addActionListener(new ActionListener(){

		@Override
		public void actionPerformed(ActionEvent arg0) {
			// TODO Auto-generated method stub
			
			chooser = new My_JFileChooser();
  			
  			chooser.setDialogTitle(Lang.getInstance().translate("Open File") );
  			//chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
  			chooser.setDialogType(javax.swing.JFileChooser.OPEN_DIALOG);
  			chooser.setMultiSelectionEnabled(false);
  		//	chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
  			//chooser.setAcceptAllFileFilterUsed(false); 
  			 // add filters
  			 FileNameExtensionFilter xmlFilter = new FileNameExtensionFilter("Era Name Accounts files (*.enaf)", "enaf");
  			chooser.setAcceptAllFileFilterUsed(false);//only filter
  	        chooser.addChoosableFileFilter(xmlFilter);
  	        chooser.setFileFilter(xmlFilter);
  	      if ( chooser.showSaveDialog(getParent()) == JFileChooser.APPROVE_OPTION ) {
			
			File file = new File(chooser.getSelectedFile().getPath());
			
			JSONObject inJSON;
			try
			{
			//OPEN FILE
			//READ SETTINS JSON FILE
			List<String> lines = Files.readLines(file, Charsets.UTF_8);
			
			String jsonString = "";
			for(String line : lines){
				
				//correcting single backslash bug
				if(line.contains("userpath"))
				{
					line = line.replace("\\", File.separator);
				}
				
				jsonString += line;
			}
			
			//CREATE JSON OBJECT
			inJSON = (JSONObject) JSONValue.parse(jsonString);
			inJSON =	inJSON == null ? new JSONObject() : inJSON;
		
		Set<String> keys = inJSON.keySet();
		Iterator<String> itKeys = keys.iterator();
		while (itKeys.hasNext()){
			String a = itKeys.next(); 
			JSONObject ss = (JSONObject) inJSON.get(a);
			Object a1 = ss.get("name");
			Object a2 = ss.get("json");
			Controller.getInstance().wallet.database.getAccountsPropertisMap().set(a, new Tuple2(ss.get("name"), ss.get("json")));
		}
		
		
		//while (it.hasNext()){
		//	Object ss = it..next();
		//	ss=ss;
			
		//}
			
			
		
		
				
		}
			catch(Exception e)
			{
				LOGGER.info("Error while reading/creating settings.json " + file.getAbsolutePath() + " using default!");
				LOGGER.error(e.getMessage(),e);
				inJSON =	new JSONObject();
			}
  	     }
		}
		
	});


}

@Override
public void delay_on_close(){
	// delete observer left panel
	tableModelImprints.deleteObserver();
	// get component from right panel
	Component c1 = jScrollPane_jPanel_RightPanel.getViewport().getView();
	// if Person_Info 002 delay on close
//	  if (c1 instanceof Imprints_Info_Panel) ( (Imprints_Info_Panel)c1).delay_on_Close();
	
}

}
