package gui.items.statement;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.zip.DataFormatException;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.mapdb.Fun.Tuple2;

import com.github.rjeschke.txtmark.Processor;

import core.blockexplorer.WEB_Transactions_HTML;
import core.item.ItemCls;
import core.item.notes.NoteCls;
import core.transaction.R_SignNote;
import core.transaction.Transaction;
import database.DBSet;
import gui.library.MTextPane;
import gui.library.M_Attached_Files_Panel;
import gui.library.Voush_Library_Panel;
import gui.library.library;
import gui.transaction.Rec_DetailsFrame;
import gui.transaction.TransactionDetailsFactory;
import lang.Lang;
import utils.MenuPopupUtil;
import utils.Zip_Bytes;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Саша
 */
public class Statement_Info extends javax.swing.JPanel {

	/**
	 * Creates new form Statement_Info
	 * 
	 * @param statement
	 */
	R_SignNote statement;
	Transaction transaction;
	private M_Attached_Files_Panel file_Panel;

	public Statement_Info(Transaction transaction) {
		if (transaction == null)
			return;
		
		this.transaction = transaction;
		

		Tuple2<BigDecimal, List<Tuple2<Integer, Integer>>> signs = DBSet.getInstance().getVouchRecordMap()
				.get(transaction.getBlockHeight(DBSet.getInstance()), transaction.getSeqNo(DBSet.getInstance()));

		if (signs != null) {

		}

		statement = (R_SignNote) transaction;
		
		initComponents();
		
		NoteCls note = (NoteCls) ItemCls.getItem(DBSet.getInstance(), ItemCls.NOTE_TYPE, statement.getKey());
		//jTextArea_Body.setContentType("text/html");
		
		
		
		String description = note.getDescription(); 
		
		file_Panel.setVisible(false);
		
//		if (statement.isText() && !statement.isEncrypted()) {
			if (!statement.isEncrypted()) {
				Set<String> kS;
				JSONObject params;
				 String files;
				 String str;
				 
			 try {
				 JSONObject data = (JSONObject) JSONValue.parseWithException(new String(statement.getData(), Charset.forName("UTF-8")));
				 // params
				
				if (data.containsKey("Statement_Params")){
				str = data.get("Statement_Params").toString();
				  params = (JSONObject) JSONValue.parseWithException(str);
				  kS = params.keySet();
				 for (String s:kS){
						description = description.replace("{{" + s + "}}", (CharSequence) params.get(s));
				 }
				}
				 // hashes
				String hasHes = "";
				 
				 if (data.containsKey("Hashes")){
				str = data.get("Hashes").toString();
				 params = (JSONObject) JSONValue.parseWithException(str);
				 kS = params.keySet();
				 
				 int i = 1;
				 for (String s:kS){
					 hasHes += i + " " + s + " " + params.get(s) + "\n";
				 }
				 }
				 // files
				 files ="";
				
				 if (data.containsKey("Files")){
					 file_Panel.setVisible(true);
				str = data.get("Files").toString();
				 JSONObject files_json = (JSONObject) JSONValue.parseWithException(str);
				 kS = files_json.keySet();
				 for (String ff:kS){
					 String fff = files_json.get(ff).toString();
					JSONObject file_json = (JSONObject) JSONValue.parseWithException(fff);
					 files += file_json.get("Name") +"\n";
					 file_Panel.insert_Row(file_json.get("Name").toString(), ((boolean) file_json.get("zip")), file_json.get("Data").toString()); 
					 
				 }
				 }
				
							 
				 jTextArea_Body.setText(
						  data.get("Title") + "\n\n"
							+ "desc: " +description + "\n\n"
							+    data.get("Message") + "\n\n"
							+ hasHes + "\n\n"
							+ files +"\n"
							
										 
						 );
				 
				
						
						
				 
				 
			if (files != null){
				
				
				
				
			}
			 
			 
			 
			 
			 
			 
			 } catch (ParseException e) {
				// TODO Auto-generated catch block
			//	e.printStackTrace();
				List<String> vars = note.getVarNames();
				if (vars != null && !vars.isEmpty()) {
					// try replace variables
					String dataVars = new String(statement.getData(), Charset.forName("UTF-8"));
					String[] rows = dataVars.split("\n");
					Map<String, String> varsArray = new HashMap<String, String>();
					for (String row: rows) {
						String[] var_Name_Value = row.split("=");
						if (var_Name_Value.length == 2) {
							varsArray.put(var_Name_Value[0].trim(), var_Name_Value[1].trim());
						}
						
					}
					
					for (Map.Entry<String, String> item : varsArray.entrySet()) {
						//description.replaceAll("{{" + item.getKey() + "}}", (String)item.getValue());
						description = description.replace("{{" + item.getKey() + "}}", (String)item.getValue());
					}
				}
				
				   
				jTextArea_Body.setText(note.getName() + "\n\n"
						+ description + "\n\n"
						+ new String(statement.getData(), Charset.forName("UTF-8")));
				
			}
			
			
			
			
		} else {
			jTextArea_Body.setText(note.getName() + "\n"
					+ Lang.getInstance().translate("Encrypted"));			
		}

		jSplitPane1.setDividerLocation(350);// .setDividerLocation((int)(jSplitPane1.getSize().getHeight()/0.5));//.setLastDividerLocation(0);

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

		jLabel_Title = new javax.swing.JLabel();
		jSplitPane1 = new javax.swing.JSplitPane();
		jPanel1 = new javax.swing.JPanel();
		jScrollPane3 = new javax.swing.JScrollPane();
		jTextArea_Body = new JTextArea();
		jPanel2 = new javax.swing.JPanel();
		file_Panel = new M_Attached_Files_Panel();
		new javax.swing.JLabel();

		// jTable_Sign = new javax.swing.JTable();

		setLayout(new java.awt.GridBagLayout());

		
		JPanel pp = new Rec_DetailsFrame(transaction);
		
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
		gridBagConstraints.weightx = 0.2;
		gridBagConstraints.insets = new java.awt.Insets(11, 11, 0, 11);
		add(pp, gridBagConstraints);
		

		jSplitPane1.setBorder(null);
		jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

		jPanel1.setLayout(new java.awt.GridBagLayout());
		int y = 0;
		
		
		
		
		// jTextArea_Body.setColumns(20);
		// jTextArea_Body.setRows(5);
		// jScrollPane3.setViewportView(jTextArea_Body);
	//	jScrollPane3.getViewport().add(jTextArea_Body);
		jLabel_Title.setText(Lang.getInstance().translate("Statement"));
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = ++y;
		gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
		gridBagConstraints.weightx = 0.2;
		gridBagConstraints.insets = new java.awt.Insets(11, 11, 0, 11);
		jPanel1.add(jLabel_Title, gridBagConstraints);
		
		
		
		jTextArea_Body.setWrapStyleWord(true);
		jTextArea_Body.setLineWrap(true);
		
		 MenuPopupUtil.installContextMenu(jTextArea_Body);
		 jTextArea_Body.setEditable(false);
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
		gridBagConstraints.gridy = ++y;
		gridBagConstraints.weightx = 0.1;
		gridBagConstraints.weighty = 0.1;
		gridBagConstraints.insets = new java.awt.Insets(11, 11, 11, 11);
		
		JScrollPane scrol1 = new JScrollPane();
		scrol1.setViewportView(jTextArea_Body);
		jPanel1.add(scrol1, gridBagConstraints);

		if (statement.isEncrypted()){
		JCheckBox encrip = new JCheckBox(Lang.getInstance().translate("Encrypted"));
		encrip.setSelected(true);
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
		gridBagConstraints.gridy = ++y;
		gridBagConstraints.weightx = 0.1;
		gridBagConstraints.insets = new java.awt.Insets(11, 11, 11, 11);
		jPanel1.add(encrip, gridBagConstraints);
		}
		
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
		gridBagConstraints.gridy = ++y;
		gridBagConstraints.weightx = 0.1;
		gridBagConstraints.weighty = 0.1;
		gridBagConstraints.insets = new java.awt.Insets(11, 11, 11, 11);
		jPanel1.add(file_Panel, gridBagConstraints);
		
		
		
		jSplitPane1.setLeftComponent(jPanel1);
		

		jPanel2.setLayout(new java.awt.GridBagLayout());

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
		gridBagConstraints.weightx = 0.1;
		gridBagConstraints.weighty = 0.1;
		gridBagConstraints.insets = new java.awt.Insets(0, 11, 11, 11);

		jPanel2.add(new Voush_Library_Panel(transaction), gridBagConstraints);
		//

		jSplitPane1.setRightComponent(jPanel2);

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
		gridBagConstraints.weightx = 0.1;
		gridBagConstraints.weighty = 0.1;
		add(jSplitPane1, gridBagConstraints);
	}// </editor-fold>

	private javax.swing.JLabel jLabel_Title;
	private javax.swing.JPanel jPanel1;
	private javax.swing.JPanel jPanel2;
	private javax.swing.JScrollPane jScrollPane3;

	private javax.swing.JSplitPane jSplitPane1;

	private JTextArea jTextArea_Body;
	// End of variables declaration
}
