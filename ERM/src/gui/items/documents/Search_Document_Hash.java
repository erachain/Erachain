package gui.items.documents;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Set;
import java.util.Stack;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JTable;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple3;

import core.crypto.Base58;
import core.crypto.Crypto;
import core.transaction.Transaction;
import database.DBSet;
import database.HashesSignsMap;
import database.SortableList;
import gui.Split_Panel;
import gui.items.imprints.TableModelImprints;
import gui.library.My_JFileChooser;
import lang.Lang;

public class Search_Document_Hash extends Split_Panel {
	
		
	Model_Hashes_info model_Hashs;
	JTable Table_Hash;
	
	public Search_Document_Hash(){
		
		
		 model_Hashs = new Model_Hashes_info();
			Table_Hash = new JTable(model_Hashs);	
	
	this.jButton2_jToolBar_RightPanel.setVisible(false);
	this.jButton1_jToolBar_RightPanel.setVisible(false);
	this.searth_Favorite_JCheckBox_LeftPanel.setVisible(false);
	this.searth_My_JCheckBox_LeftPanel.setVisible(false);
	this.searthLabel_SearchToolBar_LeftPanel.setText(Lang.getInstance().translate("Hash"));
	this.searchTextField_SearchToolBar_LeftPanel.setMinimumSize(new Dimension(500,20));
	this.searchTextField_SearchToolBar_LeftPanel.setPreferredSize(new Dimension(500,20));
	this.button2_ToolBar_LeftPanel.setVisible(false);
	this.button1_ToolBar_LeftPanel.setVisible(false);
	JButton search_Button = new JButton();
	this.searchToolBar_LeftPanel.add(search_Button);
	
	search_Button.setText(Lang.getInstance().translate("Search Hash"));
		
	search_Button.addActionListener(new ActionListener(){

		@Override
		public void actionPerformed(ActionEvent arg0) {
			// TODO Auto-generated method stub
			
				//Hashs_from_Files();
			
			model_Hashs.Set_Data(searchTextField_SearchToolBar_LeftPanel.getText());

			}
			
	});
	
	searchTextField_SearchToolBar_LeftPanel.addActionListener(new ActionListener(){

		@Override
		public void actionPerformed(ActionEvent arg0) {
			// TODO Auto-generated method stub
			model_Hashs.Set_Data(searchTextField_SearchToolBar_LeftPanel.getText().toString());
		}
		
		
		
		
		
	});
	
	JButton from_File_Button = new JButton(Lang.getInstance().translate("Get Hash"));
	this.searchToolBar_LeftPanel.add(from_File_Button);
	from_File_Button.addActionListener(new ActionListener(){

		@Override
		public void actionPerformed(ActionEvent arg0) {
			// TODO Auto-generated method stub
			
				Hashs_from_Files();

			}
			
	});
	
	
	
	
	this.jScrollPanel_LeftPanel.setViewportView(Table_Hash);
	
	
	// while hashs.
	// SortableList<byte[], Stack<Tuple3<Long, Integer, Integer>>> bbb = map.getList();
	// Integer seq = null;
	// Integer hh = null;
	//int seq = 1;
	// Transaction tt = db.getTransactionFinalMap().getTransaction(Integer.valueOf(1000),Integer.valueOf(1));
	 // код персоны, номер блока, номер транзакции
//	 value = stack.clone()
//	 db.getTransactionFinalMap().getTransaction(hashs.pop(), seq);Transaction
// while (value.size >0)
	 
	
//	byte[] a1 = 	{116, -87, -21, -1, 47, -76, -109, 86, 81, -39, -13, 86, 49, -66, 66, -71, -124, 106, 115, -31, -40, -11, -30, -128, -75, -120, 113, 74, 111, -120, -125, 105};
//	String as = Base58.encode(a1)	;
	}
	
	
	protected void Hashs_from_Files() {
		
		// открыть диалог для файла
		//JFileChooser chooser = new JFileChooser();
		// руссификация диалога выбора файла
		//new All_Options().setUpdateUI(chooser);
		My_JFileChooser chooser = new My_JFileChooser();
		chooser.setDialogTitle(Lang.getInstance().translate("Select File"));
		

		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setMultiSelectionEnabled(false);
		

		// FileNameExtensionFilter filter = new FileNameExtensionFilter(
		// "Image", "png", "jpg");
		// chooser.setFileFilter(filter);

		int returnVal = chooser.showOpenDialog(getParent());
		if (returnVal == JFileChooser.APPROVE_OPTION) {

				// make HASHES from files
				File patch = chooser.getSelectedFile();

				

					
					File file = new File(patch.getPath());

					// преобразуем в байты
					long file_len = file.length();
					if (file_len > Integer.MAX_VALUE) {
				//		table_Model.addRow(new Object[] { "",
				//				Lang.getInstance().translate("length very long") + " - " + file_name });
				//		continue;
					}
					byte[] fileInArray = new byte[(int) file.length()];
					FileInputStream f = null;
					try {
						f = new FileInputStream(patch.getPath());
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
				//		table_Model.addRow(new Object[] { "",
				//				Lang.getInstance().translate("error streaming") + " - " + file_name });
				//		continue;
					}
					try {
						f.read(fileInArray);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
				//		table_Model.addRow(new Object[] { "",
				//				Lang.getInstance().translate("error reading") + " - " + file_name });
				//		continue;
					}
					try {
						f.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
				//		continue;
					}

					/// HASHING
					String hashe = Base58.encode(Crypto.getInstance().digest(fileInArray));
				//	table_Model.addRow(new Object[] { hashes,
				//			Lang.getInstance().translate("from file ") + file_name });
					this.searchTextField_SearchToolBar_LeftPanel.setText(hashe);

					model_Hashs.Set_Data(hashe);
				//	model_Hashs = new Model_Hashes_info(hashe);
				//		Table_Hash = new JTable(model_Hashs);
				//	this.jScrollPanel_LeftPanel.setViewportView(Table_Hash);
		
		
		
		}
				
				
			}
		
			
			
		

	}
	
	


