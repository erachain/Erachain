package gui.items.statement;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.zip.DataFormatException;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple4;

import com.github.rjeschke.txtmark.Processor;

import core.blockexplorer.WEB_Transactions_HTML;
import core.item.ItemCls;
import core.item.notes.NoteCls;
import core.transaction.R_Send;
import core.transaction.R_SignNote;
import core.transaction.Transaction;
import datachain.DCSet;
import gui.library.MSplitPane;
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
	private Voush_Library_Panel voush_Library_Panel;

	public Statement_Info(Transaction transaction) {
		if (transaction == null)
			return;
		this.transaction = transaction;
		statement = (R_SignNote) transaction;
		if (statement.getVersion() ==2) {
			
			view_V2();
			return;
		}
		//view version 1
		view_V1();
	}
		
	private void view_V1(){
		

		Tuple2<BigDecimal, List<Tuple2<Integer, Integer>>> signs = DCSet.getInstance().getVouchRecordMap()
				.get(transaction.getBlockHeight(DCSet.getInstance()), transaction.getSeqNo(DCSet.getInstance()));

		if (signs != null) {

		}

		
		
		initComponents();
		
		NoteCls note = (NoteCls) ItemCls.getItem(DCSet.getInstance(), ItemCls.NOTE_TYPE, statement.getKey());
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
				
				 if(data.containsKey("Title"))jLabel_Title.setText(Lang.getInstance().translate("Title") + ": "+data.get("Title").toString());
				 
				 
							 
				 if (data.containsKey("Message"))jTextArea_Body.setText(
						  	 description + "\n\n"
							+    data.get("Message") + "\n\n"
							+ hasHes + "\n\n"
					//		+ files +"\n"
							
										 
						 );
				 
				
						
			} catch (ParseException e) {
				
			
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
		jSplitPane1 = new MSplitPane();
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
		jSplitPane1.setOrientation(MSplitPane.VERTICAL_SPLIT);

		jPanel1.setLayout(new java.awt.GridBagLayout());
		int y = 0;
		
		
		
		
		// jTextArea_Body.setColumns(20);
		// jTextArea_Body.setRows(5);
		// jScrollPane3.setViewportView(jTextArea_Body);
	//	jScrollPane3.getViewport().add(jTextArea_Body);
		jLabel_Title.setText(Lang.getInstance().translate("Title"));
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
		voush_Library_Panel =new Voush_Library_Panel(transaction);
		jPanel2.add(voush_Library_Panel, gridBagConstraints);
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

	public void  delay_on_Close(){
		voush_Library_Panel.delay_on_close();
		
		
		
		
	}
	
	private void view_V2(){
		String description ="";
		 String str ="";
			JSONObject params;
			Set<String> kS;
		Tuple4<String, String, JSONObject, HashMap<String, Tuple2<Boolean, byte[]>>> map = null;
		byte[] data = statement.getData();
		
		
		initComponents();
		
			 try {
				map = R_SignNote.parse_Data_V2(data);
			
			 JSONObject jSON = map.c;
		

		HashMap<String, Tuple2<Boolean, byte[]>> files = map.d;
		if (files != null){
		Iterator<Entry<String, Tuple2<Boolean, byte[]>>> it_Files = files.entrySet().iterator();
		while (it_Files.hasNext()){
			Entry<String, Tuple2<Boolean, byte[]>> file = it_Files.next();
			boolean zip = new Boolean(file.getValue().a);
			String name_File = (String) file.getKey();
			byte[] file_byte = (byte[]) file.getValue().b;
			file_Panel.insert_Row(name_File, zip, file_byte); 
		}
		}
		 if(jSON.containsKey("Title"))jLabel_Title.setText(Lang.getInstance().translate("Title") + ": "+jSON.get("Title").toString());
		
		//v2.0 
		if(jSON.containsKey("Template")){
			
		 NoteCls note = (NoteCls) ItemCls.getItem(DCSet.getInstance(), ItemCls.NOTE_TYPE,  new Long((String) jSON.get("Template")) );
		 if (note != null){
			 description = note.getDescription();
		
		if (jSON.containsKey("Statement_Params")){
				str = jSON.get("Statement_Params").toString();
				
					params = (JSONObject) JSONValue.parseWithException(str);
				
				 kS =  params.keySet();
				 for (String s:kS){
						description = description.replace("{{" + s + "}}", (CharSequence) params.get(s));
				 }
				
		}
		 }
		}
		// v 2.1
		if(jSON.containsKey("TM")){
			
			 NoteCls note = (NoteCls) ItemCls.getItem(DCSet.getInstance(), ItemCls.NOTE_TYPE,  new Long((String) jSON.get("TM")) );
			 if (note != null){
				 description = note.getDescription();
				 jLabel_Title.setText(Lang.getInstance().translate("Title") + ": "+ map.b);
			
			if (jSON.containsKey("PR")){
					str = jSON.get("PR").toString();
					
						params = (JSONObject) JSONValue.parseWithException(str);
					
					 kS =  params.keySet();
					 for (String s:kS){
							description = description.replace("{{" + s + "}}", (CharSequence) params.get(s));
					 }
					
			}
			 }
			}
		 
				 // hashes
				String hasHes = "";
	// v2.0			 
		if (jSON.containsKey("Hashes")){
				 str = jSON.get("Hashes").toString();
				
				
					params = (JSONObject) JSONValue.parseWithException(str);
				
				 kS = params.keySet();
				 
				 int i = 1;
				 for (String s:kS){
					 hasHes += i + " " + s + " " + params.get(s) + "\n";
				 }
		}
	// 2.1
		if (jSON.containsKey("HS")){
			 str = jSON.get("HS").toString();
			
			
				params = (JSONObject) JSONValue.parseWithException(str);
			
			 kS = params.keySet();
			 
			 int i = 1;
			 for (String s:kS){
				 hasHes += i + " " + s + " " + params.get(s) + "\n";
			 }
	}
		
			
							 
				String message="";
		// v 2.0
				if (jSON.containsKey("Message")) message = (String) jSON.get("Message");
		// v 2.1
				if (jSON.containsKey("MS")) message = (String) jSON.get("MS");
				
				 jTextArea_Body.setText(
					  	 description + "\n\n"
						+    message + "\n\n"
						+ hasHes + "\n\n"
				//		+ files +"\n"
						
									 
					 );
		
		 	
		
			 } catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
			 }
				
				
				
		 
		 
		 
		
	
		 
		
	}
	
	private javax.swing.JLabel jLabel_Title;
	private javax.swing.JPanel jPanel1;
	public javax.swing.JPanel jPanel2;
	private javax.swing.JScrollPane jScrollPane3;

	private MSplitPane jSplitPane1;
	private JLabel title_Txt;
	private JTextArea jTextArea_Body;
	// End of variables declaration
}
