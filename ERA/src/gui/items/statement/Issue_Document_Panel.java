package gui.items.statement;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.swing.AbstractButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JSplitPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.event.HyperlinkEvent.EventType;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.validation.constraints.Null;

import org.apache.commons.net.util.Base64;

import com.github.rjeschke.txtmark.Processor;

import controller.Controller;
import core.BlockChain;
import core.account.Account;
import core.crypto.Base58;
import core.crypto.Crypto;
import core.item.assets.AssetCls;
import core.item.notes.NoteCls;
import core.transaction.R_SignNote;
import core.transaction.Transaction;
import gui.PasswordPane;
import gui.items.assets.ExchangeFrame;
import gui.items.assets.TableModelItemAssets;
import gui.items.imprints.Table_Model_Issue_Hashes;
import gui.items.notes.ComboBoxModelItemsNotes;
import gui.library.MButton;
import gui.library.MImprintEDIT_Pane;
import gui.library.MTable;
import gui.library.M_Fill_Template_Panel;
import gui.library.My_JFileChooser;
import gui.models.AccountsComboBoxModel;
import gui.transaction.OnDealClick;
import lang.Lang;
import utils.Compressor_ZIP;
import utils.Converter;
import utils.GZIP;
import utils.Pair;
import utils.StrJSonFine;
import utils.Zip_Bytes;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
*/
/*
  взять имя файлов 					attached_Files_Model.getValueAt(row,0);
  взять признак орхивирования		attached_Files_Model.getValueAt(row,2);
  взять содержимое файлов. Если ZIP то зашифрованный, если нет то не зашифрованный 		attached_Files_Model.getValueAt(row,5);
  
  взять хэш 						hashes_Table_Model.getValueAt(row,0);
  взять описапние хэш				hashes_Table_Model.getValueAt(row,1);
 
 */




/**
 *
 * @author Саша
 */
public class Issue_Document_Panel extends javax.swing.JPanel {

    private Table_Model_Issue_Hashes hashes_Table_Model;
	private DefaultTableModel attached_Files_Model;
	private DefaultTableModel params_Template_Model;
	protected NoteCls sel_note;
	public JSplitPane sp_pan;
	 M_Fill_Template_Panel fill_Template_Panel;

	/**
     * Creates new form Issue_Document_Panel
     */
    public Issue_Document_Panel() {
    	jTextPane_Message_Public = new MImprintEDIT_Pane();
    	jTextPane_Message_Public.addHyperlinkListener(new HyperlinkListener(){

			@Override
			public void hyperlinkUpdate(HyperlinkEvent arg0) {
				// TODO Auto-generated method stub
				EventType i = arg0.getEventType();
				 if (arg0.getEventType() != HyperlinkEvent.EventType.ACTIVATED) return;
				 String str = JOptionPane.showInputDialog(jTextPane_Message_Public.th, Lang.getInstance().translate("Insert") + " "+arg0.getDescription(), jTextPane_Message_Public.pars.get("{{"+ arg0.getDescription()+"}}"));
				 if (str==null || str.equals("")) return;
				 jTextPane_Message_Public.pars.replace("{{"+ arg0.getDescription()+"}}", str);
				 jTextPane_Message_Public.setText(jTextPane_Message_Public.init_String(jTextPane_Message_Public.text, false));
				 for ( int i1=0; i1 < params_Template_Model.getRowCount(); i1++){
					 if (arg0.getDescription().equals(params_Template_Model.getValueAt(i1, 0))) params_Template_Model.setValueAt(str, i1, 1);
					 
    		
    		
    	}
			
			}
			
			
			
			
		});
    	
        initComponents();
       
  //  jLabel_Template.setText(Lang.getInstance().translate("Select Template") + ":");
    jLabel_Title_Message.setText(Lang.getInstance().translate("Title") + ":");
    jTextField_Title_Message.setText("");
    jTextField_Fee_Work.setText("0");
    jCheckBox_Message_Private.setText(Lang.getInstance().translate("Text Message"));
    jCheckBox_Message_Private.setSelected(true);
    jCheckBox_Message_Public.setText(Lang.getInstance().translate("Text Message"));
    jCheckBox_Message_Public.setSelected(true);
//    jButton_View.setText(Lang.getInstance().translate("View"));
    jLabel_Account_Work.setText(Lang.getInstance().translate("Select Account") + ":");
    jButton_Work_OK.setText(Lang.getInstance().translate("Sign and Send"));
    jButton_Work_OK.addActionListener(new ActionListener()
	{
	    public void actionPerformed(ActionEvent e)
	    {
	        onSendClick();
	    }
	});	
    jButton_Work_OK1.setText(Lang.getInstance().translate("Sign and Pack"));
    jButton_Work_OK1.addActionListener(new ActionListener()
	{
	    public void actionPerformed(ActionEvent e)
	    {
	        onPackClick();
	    }
	});	
    jLabel_Fee_Work.setText(Lang.getInstance().translate("Fee") + ":");
    this.jButton_Work_Cancel.setVisible(false);
    this.jButton_Clear.setText(Lang.getInstance().translate("Clear"));
    
    this.jButton_Remove_Other_Hashes.setText(Lang.getInstance().translate("Delete"));
    this.jButton_Remove_Other_Hashes.addActionListener(new ActionListener() {
		// delete row
		@Override
		public void actionPerformed(ActionEvent e) {
			if (hashes_Table_Model.getRowCount() > 0) {
				int selRow = jTable_Other_Hashes.getSelectedRow();
				if (selRow != -1 && hashes_Table_Model.getRowCount()>=selRow) {
					((DefaultTableModel) hashes_Table_Model).removeRow(selRow);
					hashes_Table_Model.fireTableDataChanged();
				}
			}
		}
	});
    
    this.jButton_Add_From_File_Other_Hashes.setText(Lang.getInstance().translate("Create Hash"));
	// jButton3_jToolBar_RightPanel.setFocusable(false);
    jButton_Add_From_File_Other_Hashes.addActionListener(new ActionListener() {
		// create Hashs
		@Override
		public void actionPerformed(ActionEvent e) {
			Hashs_from_Files(false);

		}
	});
    
    this.jButton_Add_Other_Hashes.setText(Lang.getInstance().translate("Add"));
    jButton_Add_Other_Hashes.addActionListener(new ActionListener() {
		// create Hashs
		@Override
		public void actionPerformed(ActionEvent e) {
			String str = JOptionPane.showInputDialog(null, Lang.getInstance().translate("Insert Hash"), Lang.getInstance().translate("Add"), JOptionPane.INFORMATION_MESSAGE);
			if (str == null || str =="" || str.equals("")) return;
			hashes_Table_Model.addRow(new Object[]{str, "Add"});
			hashes_Table_Model.fireTableDataChanged();
		}
	});
    this.jButton_Input_Hashes_From_File_Other_Hashes.setText(Lang.getInstance().translate("Import Hashs"));
    jButton_Input_Hashes_From_File_Other_Hashes.addActionListener(new ActionListener() {
		// create Hashs
		@Override
		public void actionPerformed(ActionEvent e) {
			Hashs_from_Files(true);
		}
	});
    
    this.jButton_Add_Attached_Files.setText(Lang.getInstance().translate("Add"));
    jButton_Add_Attached_Files.addActionListener(new ActionListener() {
		// create Hashs
		@Override
		public void actionPerformed(ActionEvent e) {
			 attache_Files();
		}
	});
    
    this.jButton_Remove_Attached_Files.setText(Lang.getInstance().translate("Delete"));
    this.jButton_Remove_Attached_Files.addActionListener(new ActionListener() {
		// delete row
		@Override
		public void actionPerformed(ActionEvent e) {
			
			
			
			if (attached_Files_Model.getRowCount() > 0) {
				int selRow = jTable_Attached_Files.getSelectedRow();
				if (selRow != -1 && attached_Files_Model.getRowCount()>=selRow) {
					
					
//					Object a = attached_Files_Model.getValueAt(selRow, 1);
					
					((DefaultTableModel) attached_Files_Model).removeRow(selRow);
					attached_Files_Model.fireTableDataChanged();
				}
			}
		}
	});
 // combo Template
 /*    
   jComboBox_Template.addItemListener(new ItemListener(){

		@Override
		public void itemStateChanged(ItemEvent e) {
			// TODO Auto-generated method stub
		
			
			if(e.getStateChange() == ItemEvent.SELECTED) 
			{		
				sel_note = (NoteCls) jComboBox_Template.getSelectedItem();
				String ww = Processor.process(sel_note.getDescription().replace("\n\n", "\n").replace("\n", "  \n"));
				
				int ee = params_Template_Model.getRowCount()-1;
				int ccc;
				for (ccc = params_Template_Model.getRowCount()-1; ccc>=0; ccc--){
					params_Template_Model.removeRow(ccc);
					
				}
				jTextPane_Message_Public.pars.clear();
				jTextPane_Message_Public.set_Text(ww);
				HashMap<String, String> ss = jTextPane_Message_Public.get_Params();
				Set<String> sk = ss.keySet();
				
				for (String s:sk){
					ss.get(s);
					params_Template_Model.addRow(new Object[] { s, ss.get(s)});
							
				}
				
				
			} 	
			
			
		}
	});
	*/
    
    TableColumnModel at_F_Col_M = jTable_Attached_Files.getColumnModel();
    
    TableColumn col = at_F_Col_M.getColumn(2);
    col.setMinWidth(50);
    col.setPreferredWidth(60);
    col.setMaxWidth(100);
    col = at_F_Col_M.getColumn(3);
    col.setMinWidth(150);
    col.setPreferredWidth(160);
    col.setMaxWidth(200);
    
    
    jTable_Attached_Files.addMouseListener(new MouseAdapter() {
		@Override
		public void mousePressed(MouseEvent e) {
			Point p = e.getPoint();
			int row = jTable_Attached_Files.rowAtPoint(p);
			jTable_Attached_Files.setRowSelectionInterval(row, row);
			
			if(e.getClickCount() == 2)
			{
			//	row = assetsTable.convertRowIndexToModel(row);
			//	AssetCls asset = tableModelItemAssets.getAsset(row);
			//	new AssetPairSelect(asset.getKey(), "","");
			//	new ExchangeFrame(asset,null, "", "");
		//		new AssetFrame(asset);
			}
			if(e.getClickCount() == 1 & e.getButton() == e.BUTTON1)
			{
				
				if (jTable_Attached_Files.getSelectedColumn() == 2){
					row = jTable_Attached_Files.convertRowIndexToModel(row);
					attached_Files_Model.setValueAt(new Boolean(!(boolean) attached_Files_Model.getValueAt(row, 2)), row, 2);
					if (new Boolean((boolean)attached_Files_Model.getValueAt(row,2))) {
				//	 Compressor_ZIP zip = new Compressor_ZIP();
				//	attached_Files_Model.setValueAt(zip.compress((byte[]) attached_Files_Model.getValueAt(row, 4))	, row, 5); 	
					
					byte[] z1 = null ;
					try {
						attached_Files_Model.setValueAt(Zip_Bytes.compress( (byte[]) attached_Files_Model.getValueAt(row, 4)), row, 5);
						z1 = (byte[]) attached_Files_Model.getValueAt(row, 4);
						
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				// read & un Zip info
					byte[] z2 = null ;
					try {
		//				z2 = GZIP.GZIPdecompress_b((byte[]) attached_Files_Model.getValueAt(row, 5));
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					z2 = z2;
					
					}else{
						attached_Files_Model.setValueAt(attached_Files_Model.getValueAt(row, 4), row, 5); 
						
					}
			//		AssetCls asset = tableModelItemAssets.getAsset(row);
			//		favorite_set( assetsTable);	
					attached_Files_Model.setValueAt(((byte[])attached_Files_Model.getValueAt(row, 5)).length,row,3);
					
					
				}
				
				
			}
		}
	});
    
 // hand cursor  for Favorite column
    jTable_Attached_Files.addMouseMotionListener(new MouseMotionListener() {
 	    public void mouseMoved(MouseEvent e) {
 	       
 	        if(jTable_Attached_Files.columnAtPoint(e.getPoint())==2)
 	        {
 	     
 	        	jTable_Attached_Files.setCursor(new Cursor(Cursor.HAND_CURSOR));
 	        } else {
 	        	jTable_Attached_Files.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
 	        }
 	    }

 	    public void mouseDragged(MouseEvent e) {
 	    }
 	});
 	
    
    
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">                          
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jTabbedPane_Message = new javax.swing.JTabbedPane();
        jPanel_Message_Public = new javax.swing.JPanel();
        jScrollPane_Message_Public_TextPane = new javax.swing.JScrollPane();
        jScrollPane_Params_Template_Public_TextPane = new javax.swing.JScrollPane();
        jCheckBox_Message_Public = new javax.swing.JCheckBox();
        jPanel_Message = new javax.swing.JPanel();
        jScrollPane_Message_TextPane = new javax.swing.JScrollPane();
        jTextPane_Message_Private = new javax.swing.JTextPane();
        jCheckBox_Message_Private = new javax.swing.JCheckBox();
        jTabbedPane_Other = new javax.swing.JTabbedPane();
        jPanel_Attached_Files = new javax.swing.JPanel();
        jScrollPane_Attached_Files_Table = new javax.swing.JScrollPane();
   //     jTable_Attached_Files = new javax.swing.JTable();
        jPanel_Other_Attached_Files_Work = new javax.swing.JPanel();
        jButton_Remove_Attached_Files = new MButton();
        jButton_Add_Attached_Files = new MButton();
        jPanel_Other_Hashes = new javax.swing.JPanel();
        jScrollPane_Hashes_Files_Tale = new javax.swing.JScrollPane();
  //      jTable_Other_Hashes = new javax.swing.JTable();
        jButton_Add_From_File_Other_Hashes = new MButton();
        jButton_Add_Other_Hashes = new MButton();
        jButton_Remove_Other_Hashes = new MButton();
        jPanel_Title = new javax.swing.JPanel();
   //     jLabel_Template = new javax.swing.JLabel();
  //      jComboBox_Template = new JComboBox<NoteCls>(new ComboBoxModelItemsNotes());
        jLabel_Title_Message = new javax.swing.JLabel();
        jTextField_Title_Message = new javax.swing.JTextField();
  //      jButton_View = new MButton();
        jPanel_Work = new javax.swing.JPanel();
        jLabel_Account_Work = new javax.swing.JLabel();
        jComboBox_Account_Work = new JComboBox<Account>( new AccountsComboBoxModel());
        jLabel_Fee_Work = new javax.swing.JLabel();
        jTextField_Fee_Work = new javax.swing.JTextField();
        jButton_Work_Cancel = new MButton();
        jButton_Work_OK = new MButton();
        jButton_Clear = new MButton();
        jButton_Work_OK1 = new MButton();
        jButton_Input_Hashes_From_File_Other_Hashes= new MButton();
        
        params_Template_Model = new Params_Template_Model();
        jTable_Params_Message_Public = new MTable(params_Template_Model);
        params_Template_Model.addTableModelListener(new TableModelListener(){

			@Override
			public void tableChanged(TableModelEvent arg0) {
				// TODO Auto-generated method stub
	
			if (arg0.getType() != 0 && arg0.getColumn()<0 ) return;
			System.out.print("\n row = " + arg0.getFirstRow() + "  Col="+ arg0.getColumn() + "   type =" + arg0.getType());
			String dd = params_Template_Model.getValueAt(arg0.getFirstRow(),  arg0.getColumn()).toString();
			System.out.print("\n key:"+ params_Template_Model.getValueAt(arg0.getFirstRow(),  0) +" value:" + params_Template_Model.getValueAt(arg0.getFirstRow(),  arg0.getColumn()));
			
			 jTextPane_Message_Public.pars.replace("{{"+ params_Template_Model.getValueAt(arg0.getFirstRow(),  0) +"}}",(String) params_Template_Model.getValueAt(arg0.getFirstRow(),  arg0.getColumn()));
			 System.out.print("\n");
				System.out.print(jTextPane_Message_Public.pars);
			 jTextPane_Message_Public.setText(jTextPane_Message_Public.init_String(jTextPane_Message_Public.text, false));
			arg0=arg0;
			}});

        java.awt.GridBagLayout layout = new java.awt.GridBagLayout();
        layout.columnWidths = new int[] {0, 0, 0};
        layout.rowHeights = new int[] {0, 0, 0, 0, 0, 0, 0};
        setLayout(layout);

        jPanel_Message_Public.setLayout(new java.awt.GridBagLayout());

        jScrollPane_Message_Public_TextPane.setViewportView(jTextPane_Message_Public);
        sp_pan = new JSplitPane();
        sp_pan.setLeftComponent(jScrollPane_Message_Public_TextPane);
        
        jScrollPane_Params_Template_Public_TextPane.setViewportView(jTable_Params_Message_Public);
        sp_pan.setRightComponent(jScrollPane_Params_Template_Public_TextPane);
        
        
        
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        jPanel_Message_Public.add(sp_pan, gridBagConstraints);

        jCheckBox_Message_Public.setText("Text");
        jCheckBox_Message_Public.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox_Message_PublicActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        jPanel_Message_Public.add(jCheckBox_Message_Public, gridBagConstraints);

     //   jTabbedPane_Message.addTab(Lang.getInstance().translate("Public Part"), jPanel_Message_Public);
        fill_Template_Panel = new M_Fill_Template_Panel();
        jTabbedPane_Message.addTab(Lang.getInstance().translate("Template"), fill_Template_Panel );
        

        jPanel_Message.setLayout(new java.awt.GridBagLayout());

        jScrollPane_Message_TextPane.setViewportView(jTextPane_Message_Private);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        jPanel_Message.add(jScrollPane_Message_TextPane, gridBagConstraints);

        jCheckBox_Message_Private.setText("Text");
        jCheckBox_Message_Private.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox_Message_PrivateActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
  //      jPanel_Message.add(jCheckBox_Message_Private, gridBagConstraints);

        jTabbedPane_Message.addTab(Lang.getInstance().translate("Message"), jPanel_Message);
     
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.4;
        gridBagConstraints.insets = new java.awt.Insets(16, 8, 0, 8);
        add(jTabbedPane_Message, gridBagConstraints);

        jPanel_Attached_Files.setMinimumSize(new java.awt.Dimension(0, 64));
        jPanel_Attached_Files.setPreferredSize(new java.awt.Dimension(0, 64));
        jPanel_Attached_Files.setLayout(new java.awt.GridBagLayout());

        
        
        attached_Files_Model =new Attache_Files_Model(); // new javax.swing.table.DefaultTableModel(new Object [][][][][] { {null,null, null, null,null}}, new String [] {Lang.getInstance().translate("Path"), "Data","ZIP?", "Size/Zip Size", "www"});
    //    attached_Files_Model.removeRow(0);
        jTable_Attached_Files = new MTable(attached_Files_Model);
        jTable_Attached_Files.removeColumn(jTable_Attached_Files.getColumnModel().getColumn(5));
        jTable_Attached_Files.removeColumn(jTable_Attached_Files.getColumnModel().getColumn(4));
              
        jTable_Attached_Files.setAlignmentX(0.0F);
        jTable_Attached_Files.setAlignmentY(0.0F);
        jScrollPane_Attached_Files_Table.setViewportView(jTable_Attached_Files);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanel_Attached_Files.add(jScrollPane_Attached_Files_Table, gridBagConstraints);

        jPanel_Other_Attached_Files_Work.setLayout(new java.awt.GridBagLayout());

        jButton_Remove_Attached_Files.setText(Lang.getInstance().translate("Remove File"));
        jButton_Remove_Attached_Files.setToolTipText("");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(8, 41, 8, 8);
        jPanel_Other_Attached_Files_Work.add(jButton_Remove_Attached_Files, gridBagConstraints);

        jButton_Add_Attached_Files.setText("Add File");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 8);
        jPanel_Other_Attached_Files_Work.add(jButton_Add_Attached_Files, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        jPanel_Attached_Files.add(jPanel_Other_Attached_Files_Work, gridBagConstraints);

        jTabbedPane_Message.addTab(Lang.getInstance().translate("Attached Files"), jPanel_Attached_Files);

        jPanel_Other_Hashes.setLayout(new java.awt.GridBagLayout());

        jScrollPane_Hashes_Files_Tale.setOpaque(false);
        jScrollPane_Hashes_Files_Tale.setPreferredSize(new java.awt.Dimension(0, 0));
        
        hashes_Table_Model = new  Table_Model_Issue_Hashes(0);
        jTable_Other_Hashes = new MTable(hashes_Table_Model);
        jScrollPane_Hashes_Files_Tale.setViewportView(jTable_Other_Hashes);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 9;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        jPanel_Other_Hashes.add(jScrollPane_Hashes_Files_Tale, gridBagConstraints);

  
        jButton_Input_Hashes_From_File_Other_Hashes.setText("Import Hashs");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 8);
        jPanel_Other_Hashes.add(jButton_Input_Hashes_From_File_Other_Hashes, gridBagConstraints);
        
        jButton_Add_From_File_Other_Hashes.setText("Add From File");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 8);
        jPanel_Other_Hashes.add(jButton_Add_From_File_Other_Hashes, gridBagConstraints);

        jButton_Add_Other_Hashes.setText("Add");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 8);
        jPanel_Other_Hashes.add(jButton_Add_Other_Hashes, gridBagConstraints);

        jButton_Remove_Other_Hashes.setText("Remove");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 8;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 8);
        jPanel_Other_Hashes.add(jButton_Remove_Other_Hashes, gridBagConstraints);

        jTabbedPane_Message.addTab(Lang.getInstance().translate("Hashes"), jPanel_Other_Hashes);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(16, 8, 0, 8);
 //       add(jTabbedPane_Other, gridBagConstraints);

        jPanel_Title.setLayout(new java.awt.GridBagLayout());

  //     jLabel_Template.setText("Template: ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 0);
  //      jPanel_Title.add(jLabel_Template, gridBagConstraints);

      
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 8);
 //       jPanel_Title.add(jComboBox_Template, gridBagConstraints);

        jLabel_Title_Message.setText("Title:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 0);
        jPanel_Title.add(jLabel_Title_Message, gridBagConstraints);

        jTextField_Title_Message.setText("jTextField1");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 8);
        jPanel_Title.add(jTextField_Title_Message, gridBagConstraints);

  //      jButton_View.setText("View ");
  //      jButton_View.setToolTipText("");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 8);
   //     jPanel_Title.add(jButton_View, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 0, 8);
        add(jPanel_Title, gridBagConstraints);

        jPanel_Work.setLayout(new java.awt.GridBagLayout());

        jLabel_Account_Work.setText("Account: ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 0);
        jPanel_Work.add(jLabel_Account_Work, gridBagConstraints);

       
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 8);
        jPanel_Work.add(jComboBox_Account_Work, gridBagConstraints);

        jLabel_Fee_Work.setText("Fee: ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 0);
        jPanel_Work.add(jLabel_Fee_Work, gridBagConstraints);

        jTextField_Fee_Work.setText("jTextField2");
        jTextField_Fee_Work.setMinimumSize(new java.awt.Dimension(120, 20));
        jTextField_Fee_Work.setPreferredSize(new java.awt.Dimension(120, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 8);
        jPanel_Work.add(jTextField_Fee_Work, gridBagConstraints);

        jButton_Work_Cancel.setText("Cancel");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(8, 16, 8, 16);
        jPanel_Work.add(jButton_Work_Cancel, gridBagConstraints);

        jButton_Work_OK.setText("OK");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(8, 16, 8, 16);
        jPanel_Work.add(jButton_Work_OK, gridBagConstraints);

        jButton_Clear.setText("Clear");
        jButton_Clear.setToolTipText("");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(8, 16, 8, 16);
        jPanel_Work.add(jButton_Clear, gridBagConstraints);

        
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(8, 16, 8, 16);
        jPanel_Work.add(jButton_Work_OK1, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 8);
        add(jPanel_Work, gridBagConstraints);
        jPanel_Work.getAccessibleContext().setAccessibleName("Work");
        jPanel_Work.getAccessibleContext().setAccessibleDescription("");
    }// </editor-fold>
    
	@SuppressWarnings("null")
	public Pair<Transaction, Integer> makeDeal(boolean asPack)
	{
		
		//CHECK IF WALLET UNLOCKED
		if(!Controller.getInstance().isWalletUnlocked())
		{
			//ASK FOR PASSWORD
			String password = PasswordPane.showUnlockWalletDialog(); 
			if(password.equals(""))
			{
				return null;
			}
			if(!Controller.getInstance().unlockWallet(password))
			{
				//WRONG PASSWORD
				JOptionPane.showMessageDialog(null, Lang.getInstance().translate("Invalid password"), Lang.getInstance().translate("Unlock Wallet"), JOptionPane.ERROR_MESSAGE);
								
				return null;
			}
		}
		
		Pair<Transaction, Integer> result;

		//READ SENDER
		Account sender = (Account) this.jComboBox_Account_Work.getSelectedItem();
		int feePow = 0;
		String message = "";
		boolean isTextB = true;
		byte[] messageBytes;
		long key = 0;
		byte[] isTextByte;
		byte[] encrypted;
		HashMap  params_Map = new HashMap();
		HashMap  out_Map = new HashMap();
		HashMap hashes_Map = new HashMap();
		int parsing = 0;
		try
		{
			
			
			//READ AMOUNT
			parsing = 1;
			
			//READ FEE
			parsing = 2;
			feePow = Integer.parseInt(this.jTextField_Fee_Work.getText());		
			Set<Entry<String, String>> param_keys = this.fill_Template_Panel.get_Params().entrySet();
// template params			 
			for (Entry<String, String> key1:param_keys){
			message += key1.getKey() + " = " + key1.getValue() + "  \n"; // for MarkDown need "  "
			params_Map.put( key1.getKey(), key1.getValue());
			}
			
// hashes			
			
			int hR = hashes_Table_Model.getRowCount();
			for (int i = 0;i<hR;i++){
				hashes_Map.put(hashes_Table_Model.getValueAt(i, 0),hashes_Table_Model.getValueAt(i, 1));
				
			}
			
			out_Map.put("Hashes", hashes_Map);
			out_Map.put("Statement_Params", params_Map);
			out_Map.put("Version", "2.01");
			out_Map.put("Message", this.jTextPane_Message_Private.getDocument().getText(0, this.jTextPane_Message_Private.getDocument().getLength()));
			out_Map.put("Title", jTextField_Title_Message.getText());
// files	
			HashMap out_Files = new HashMap();
			
			int oF = attached_Files_Model.getRowCount();
			for (int i=0; i<oF; i++){
				/*
				  взять имя файлов 					attached_Files_Model.getValueAt(row,0);
				  взять признак орхивирования		attached_Files_Model.getValueAt(row,2);
				  взять содержимое файлов. Если ZIP то зашифрованный, если нет то не зашифрованный 		attached_Files_Model.getValueAt(row,5);
				*/
				HashMap file_Attr = new HashMap();
				file_Attr.put("Name", attached_Files_Model.getValueAt(i,0));
				file_Attr.put("zip", attached_Files_Model.getValueAt(i,2));
				byte[] ss = (byte[]) attached_Files_Model.getValueAt(i,5);
				file_Attr.put("Data",Base64.encodeBase64String(ss));
				
				
				out_Files.put(i, file_Attr);
				
			}
			out_Map.put("Files", out_Files);
			message += this.jTextPane_Message_Public.getText();
			
			isTextB = this.jCheckBox_Message_Public.isSelected();
						
			
			messageBytes = message.getBytes( Charset.forName("UTF-8") );
			
			 String sS = StrJSonFine.convert(out_Map);
			 messageBytes =	 StrJSonFine.convert(out_Map).getBytes( Charset.forName("UTF-8") );
			
			
			
			
			
			
			
			//String jSons = StrJSonFine.convert(output);
			//byte[] mess = jSons.getBytes( Charset.forName("UTF-8") );
			
			
			
			
			
			if ( messageBytes.length < 10 || messageBytes.length > BlockChain.MAX_REC_DATA_BYTES )
			{
				JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Message size exceeded! 10...MAX"), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
								
				return null;
			}
			
			//Pair<Transaction, Integer> result;

			isTextByte = (isTextB)? new byte[] {1}:new byte[]{0};

			boolean encryptMessage = this.fill_Template_Panel.jCheckBox_Is_Encripted.isSelected();			
			encrypted = (encryptMessage)?new byte[]{1}:new byte[]{0};
			
			//READ NOTE
			parsing = 5;
			//CHECK IF PAYMENT OR ASSET TRANSFER
			NoteCls note = (NoteCls) this.fill_Template_Panel.jComboBox_Template.getSelectedItem();
			key = note.getKey(); 

		}
		catch(Exception e)
		{
			//CHECK WHERE PARSING ERROR HAPPENED
			switch(parsing)
			{
			case 1:
				
				JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Invalid amount!"), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
				break;
				
			case 2:
				
				JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Invalid fee!"), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
				break;

			case 5:
				
				JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Note not exist!"), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
				break;
			}
			return null;
		}

		//CREATE TX MESSAGE
		
		result = Controller.getInstance().signNote(asPack,
				Controller.getInstance().getPrivateKeyAccountByAddress(sender.getAddress()),
				feePow, key, messageBytes, isTextByte, encrypted);
		
		//CHECK VALIDATE MESSAGE
		if (result.getB() == Transaction.VALIDATE_OK) {
			return result;
		} else {		
			JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate(OnDealClick.resultMess(result.getB())), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
			return null;
		}
		
		
	}

	public void onSendClick()
	{
		this.jButton_Work_OK.setEnabled(false);
		this.jButton_Work_OK1.setEnabled(false);
		Pair<Transaction, Integer> result = makeDeal(false);
		if (result != null) {
			JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Statement has been sent!"), Lang.getInstance().translate("Success"), JOptionPane.INFORMATION_MESSAGE);
		}
		this.jButton_Work_OK.setEnabled(true);
		this.jButton_Work_OK1.setEnabled(true);
	}
	public void onPackClick()
	{
		this.jButton_Work_OK.setEnabled(false);
		this.jButton_Work_OK1.setEnabled(false);
		Pair<Transaction, Integer> result = makeDeal(true);
		if (result != null) {
			this.jCheckBox_Message_Public.setText( Base58.encode(result.getA().toBytes(true, null)));
		}
		
		this.jButton_Work_OK.setEnabled(false);
		this.jButton_Work_OK1.setEnabled(false);
	}

    
    

    private void jCheckBox_Message_PublicActionPerformed(java.awt.event.ActionEvent evt) {                                                         
        // TODO add your handling code here:
    }                                                        

    private void jCheckBox_Message_PrivateActionPerformed(java.awt.event.ActionEvent evt) {                                                          
        // TODO add your handling code here:
    }                                                         

    private void jMButton_Work_OK1ActionPerformed(java.awt.event.ActionEvent evt) {                                                 
        // TODO add your handling code here:
    } 
    
	protected void Hashs_from_Files(boolean importing) {
		// TODO Auto-generated method stub
		// true - если импорт из файла
		// false - если создаем хэш для файлов

		// открыть диалог для файла
		//JFileChooser chooser = new JFileChooser();
		// руссификация диалога выбора файла
		//new All_Options().setUpdateUI(chooser);
		My_JFileChooser chooser = new My_JFileChooser();
		chooser.setDialogTitle(Lang.getInstance().translate("Select File"));
		
		

		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setMultiSelectionEnabled(true);
		if (importing)
			chooser.setMultiSelectionEnabled(false);

		// FileNameExtensionFilter filter = new FileNameExtensionFilter(
		// "Image", "png", "jpg");
		// chooser.setFileFilter(filter);

		int returnVal = chooser.showOpenDialog(getParent());
		if (returnVal == JFileChooser.APPROVE_OPTION) {
// если есть пустые строки удаляем их
			int i;
			for( i=0; i <= hashes_Table_Model.getRowCount()-1; i++){
				if (hashes_Table_Model.getValueAt(i, 0).toString().equals("")){
					
					hashes_Table_Model.removeRow(i);
					
				}
			}
			
			
			
			if (importing) {
				// IMPORT FROM FILE
				File patch = chooser.getSelectedFile();
				String file_name = patch.getPath();
				String hashesStr = "";
				try {
					hashesStr = new String(Files.readAllBytes(Paths.get(file_name)));
				} catch (IOException e) {
					e.printStackTrace();
					hashes_Table_Model.addRow(new Object[] { "",
							Lang.getInstance().translate("error reading") + " - " + file_name });
				}
				
				if (hashesStr.length() > 0) {
				String[] hashes = hashesStr.split("\\s*(\\s|,|!|;|:|\n|\\.)\\s*");
					for (String hashB58: hashes) {
						if (hashB58!= null && !hashB58.equals(new String("")))	hashes_Table_Model.addRow(new Object[] { hashB58, Lang.getInstance().translate("imported from") + " " +  file_name});					
					}
					
				}

			} else {

				// make HASHES from files
				File[] patchs = chooser.getSelectedFiles();

				for (File patch : patchs) {

					String file_name = patch.getPath();
					File file = new File(patch.getPath());

					// преобразуем в байты
					long file_len = file.length();
					if (file_len > Integer.MAX_VALUE) {
						hashes_Table_Model.addRow(new Object[] { "",
								Lang.getInstance().translate("length very long") + " - " + file_name });
						continue;
					}
					byte[] fileInArray = new byte[(int) file.length()];
					FileInputStream f = null;
					try {
						f = new FileInputStream(patch.getPath());
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						hashes_Table_Model.addRow(new Object[] { "",
								Lang.getInstance().translate("error streaming") + " - " + file_name });
						continue;
					}
					try {
						f.read(fileInArray);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						hashes_Table_Model.addRow(new Object[] { "",
								Lang.getInstance().translate("error reading") + " - " + file_name });
						continue;
					}
					try {
						f.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						continue;
					}

					/// HASHING
					String hashes = Base58.encode(Crypto.getInstance().digest(fileInArray));
					hashes_Table_Model.addRow(new Object[] { hashes,
							Lang.getInstance().translate("from file ") + file_name });

				}
				
				
			}
	//		hashes_Table_Model.addRow(new Object[] { "",""});
			hashes_Table_Model.fireTableDataChanged();
			jTable_Other_Hashes.setRowSelectionInterval(hashes_Table_Model.getRowCount()-1,hashes_Table_Model.getRowCount()-1);
			
			
			
		}

	}

	protected void attache_Files() {
		// TODO Auto-generated method stub
		// true - если импорт из файла
		// false - если создаем хэш для файлов

		// открыть диалог для файла
		//JFileChooser chooser = new JFileChooser();
		// руссификация диалога выбора файла
		//new All_Options().setUpdateUI(chooser);
		My_JFileChooser chooser = new My_JFileChooser();
		chooser.setDialogTitle(Lang.getInstance().translate("Select File"));
		
		

		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setMultiSelectionEnabled(true);
		
		int returnVal = chooser.showOpenDialog(getParent());
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			
		

				// make HASHES from files
				File[] patchs = chooser.getSelectedFiles();

				for (File patch : patchs) {

					String file_name = patch.getPath();
					File file = new File(patch.getPath());
					
					

					// преобразуем в байты
					long file_len = file.length();
					byte[] fileInArray = new byte[(int) file.length()];
					FileInputStream f = null;
					try {
						f = new FileInputStream(patch.getPath());
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					return;
					}
					try {
						f.read(fileInArray);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						return;
					}
					try {
						f.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						continue;
					}

					
					attached_Files_Model.addRow(new Object[] {patch.getName().toString(), patch.getPath().substring(0, patch.getPath().length()-patch.getName().length()), new Boolean(false), new Integer(fileInArray.length), fileInArray, fileInArray});
			

				}
				
				
			
	//		hashes_Table_Model.addRow(new Object[] { "",""});
				attached_Files_Model.fireTableDataChanged();
				jTable_Attached_Files.setRowSelectionInterval(attached_Files_Model.getRowCount()-1,attached_Files_Model.getRowCount()-1);
			
			
			
		}

	}

    // Variables declaration - do not modify                     
    private MButton jButton_Add_Attached_Files;
    private MButton jButton_Add_From_File_Other_Hashes;
    private MButton jButton_Add_Other_Hashes;
    private MButton jButton_Clear;
    private MButton jButton_Remove_Attached_Files;
    private MButton jButton_Remove_Other_Hashes;
 //   private MButton jButton_View;
    private MButton jButton_Work_Cancel;
    private MButton jButton_Work_OK;
    private MButton jButton_Work_OK1;
    private MButton jButton_Input_Hashes_From_File_Other_Hashes;
    private javax.swing.JCheckBox jCheckBox_Message_Private;
    private javax.swing.JCheckBox jCheckBox_Message_Public;
    private javax.swing.JComboBox jComboBox_Account_Work;
 //   private javax.swing.JComboBox jComboBox_Template;
    private javax.swing.JLabel jLabel_Account_Work;
    private javax.swing.JLabel jLabel_Fee_Work;
 //   private javax.swing.JLabel jLabel_Template;
    private javax.swing.JLabel jLabel_Title_Message;
    private javax.swing.JPanel jPanel_Attached_Files;
    private javax.swing.JPanel jPanel_Message;
    private javax.swing.JPanel jPanel_Message_Public;
    private javax.swing.JPanel jPanel_Other_Attached_Files_Work;
    private javax.swing.JPanel jPanel_Other_Hashes;
    private javax.swing.JPanel jPanel_Title;
    private javax.swing.JPanel jPanel_Work;
    private javax.swing.JScrollPane jScrollPane_Attached_Files_Table;
    private javax.swing.JScrollPane jScrollPane_Hashes_Files_Tale;
    private javax.swing.JScrollPane jScrollPane_Message_TextPane;
    private javax.swing.JScrollPane jScrollPane_Message_Public_TextPane;
    private javax.swing.JScrollPane jScrollPane_Params_Template_Public_TextPane;
    private javax.swing.JTabbedPane jTabbedPane_Message;
    private javax.swing.JTabbedPane jTabbedPane_Other;
    private MTable jTable_Attached_Files;
    private MTable jTable_Other_Hashes;
    private javax.swing.JTextField jTextField_Fee_Work;
    private javax.swing.JTextField jTextField_Title_Message;
    private javax.swing.JTextPane jTextPane_Message_Private;
    private MImprintEDIT_Pane jTextPane_Message_Public;
    public MTable jTable_Params_Message_Public;
    // End of variables declaration                   
}
class Attache_Files_Model extends DefaultTableModel{
    
	public Attache_Files_Model()
    {
      super(new Object[] {Lang.getInstance().translate("Name"),Lang.getInstance().translate("Path"),"ZIP?", "Size/Zip Size"}, 0);
   
    }
    
   public int getColumnCount(){
	return 6;
   }
	
	@Override
    public boolean isCellEditable(int row, int column)
    {
        return new Boolean(null);
    } 
    public Class<? extends Object> getColumnClass(int c) {     // set column type
		Object o = getValueAt(0, c);
		return o==null?Null.class:o.getClass();
	   }
	
    public Object getValueAt(int row, int col){
    	
    	
    	if (this.getRowCount()<row || this.getRowCount() ==0 || col <0 || row <0)return null;
	return super.getValueAt(row, col);
    	
    	
    }
}
    class Params_Template_Model extends DefaultTableModel{
        
    	/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public Params_Template_Model()
        {
          super(new Object[] {Lang.getInstance().translate("Name"),Lang.getInstance().translate("=")}, 0);
       
        }
        
       public int getColumnCount(){
    	return 2;
       }
    	
    	@Override
        public boolean isCellEditable(int row, int column)
        {
           if (column ==1) return true;
    		return new Boolean(null);
        } 
        public Class<? extends Object> getColumnClass(int c) {     // set column type
    		Object o = getValueAt(0, c);
    		return o==null?Null.class:o.getClass();
    	   }
    	
        public Object getValueAt(int row, int col){
        	
        	
        	if (this.getRowCount()<row || this.getRowCount() ==0 || col <0 || row <0)return null;
    	return super.getValueAt(row, col);
        	
        	
        }
   
	
	
}

