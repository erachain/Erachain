package gui.items.persons;

import java.awt.Dimension;
import java.nio.charset.Charset;
import java.sql.Timestamp;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Longs;

import controller.Controller;
import core.account.Account;
import core.account.PrivateKeyAccount;
import core.item.persons.PersonCls;
import core.item.statuses.StatusCls;
import core.transaction.R_Vouch;
import core.transaction.Transaction;
import database.DBSet;
import gui.models.AccountsComboBoxModel;
import gui.records.Record_Info;
import gui.records.VouchRecordDialog;
import gui.items.statuses.ComboBoxModelItemsStatuses;
import gui.transaction.OnDealClick;
import jersey.repackaged.com.google.common.primitives.Ints;
import lang.Lang;
import utils.Pair;

//public class PersonConfirm extends JDialog { // InternalFrame  {
public class PersonSetStatusDialog extends JDialog {

	/**
	 * 
	 */
	//private static final long serialVersionUID = 1L;
	private static final long serialVersionUID = 2717571093561259483L;

	private static Transaction parentRecord;
	private static Record_Info infoPanel;
	
	
	 // Variables declaration - do not modify                     
    private javax.swing.JTextField jADataTxt;
    private javax.swing.JTextField jAData2Txt;
//    private javax.swing.JButton jButton_Cansel;
//    private javax.swing.JButton jButton_SetStatus;
//    private javax.swing.JComboBox<String> jComboBox_Status;
 //   private javax.swing.JComboBox<String> jComboBox_YourAddress;
//    private javax.swing.JTextField jFeeTxt;
//    private javax.swing.JFormattedTextField jFormattedTextField_fromDate;
 //   private javax.swing.JFormattedTextField jFormattedTextField_toDate;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel_Fee;
    private javax.swing.JLabel jLabel_Addition1;
    private javax.swing.JLabel jLabel_Addition2;
    private javax.swing.JLabel jLabel_Address;
    private javax.swing.JLabel jLabel_Data_From;
    private javax.swing.JLabel jLabel_Data_To;
    private javax.swing.JLabel jLabel_Param1;
    private javax.swing.JLabel jLabel_Param2;
    private javax.swing.JLabel jLabel_Parent_record;
    private javax.swing.JLabel jLabel_Status;
    private javax.swing.JLabel jLabel_Title;
    private javax.swing.JLabel jLabel__Description;
 //   private javax.swing.JTextField jPar1Txt;
//    private javax.swing.JTextField jPar2Txt;
 //   private javax.swing.JTextField jParentRecTxt;
    private javax.swing.JScrollPane jLabel_PersonInfo;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextArea jTextArea_Description;
  
	
	
	
	
	

	public PersonSetStatusDialog(JComponent  apers, PersonCls person) {
		super();
	
this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		initComponents();
		this.setTitle(Lang.getInstance().translate("Set Status"));
		//this.setResizable(true);
	   // setPreferredSize(new Dimension(400, 600));
	    
		
		    jLabel_Addition1.setText(Lang.getInstance().translate("Additional")+"1:");
		    jLabel_Addition2.setText(Lang.getInstance().translate("Additional")+"2:");
		    jLabel_Address.setText(Lang.getInstance().translate("Your Address")+":");
		    jLabel_Data_From.setText(Lang.getInstance().translate("From Date")+":");
		    jLabel_Data_To.setText(Lang.getInstance().translate("To Date")+":");
		    jLabel_Param1.setText(Lang.getInstance().translate("Parameter")+" 1:");
		    jLabel_Param2.setText(Lang.getInstance().translate("Parameter")+" 2:");
		    jLabel_Parent_record.setText(Lang.getInstance().translate("Parent Record")+":");
		    jLabel_Status.setText(Lang.getInstance().translate("Status")+":");
		    jLabel_Title.setText(Lang.getInstance().translate("Information about the person")+":");
		    jLabel__Description.setText(Lang.getInstance().translate("Description")+":");;
		    jLabel_Fee.setText(Lang.getInstance().translate("Fee Power (0..6)")+":");
		    jButton_Cansel.setText(Lang.getInstance().translate("Cancel"));    
		    jButton_Cansel.addActionListener(new java.awt.event.ActionListener() {
	            public void actionPerformed(java.awt.event.ActionEvent evt) {
	            	dispose();	
	            }
	        });
		
		    jButton_SetStatus.setText(Lang.getInstance().translate("Set status"));
		    jButton_SetStatus.addActionListener(new java.awt.event.ActionListener() {
	            public void actionPerformed(java.awt.event.ActionEvent evt) {
	            	onGoClick(person, jButton_SetStatus, (Account)jComboBox_YourAddress.getSelectedItem(),
	            			(StatusCls)jComboBox_Status.getSelectedItem(),
	            			jFormattedTextField_fromDate, jFormattedTextField_toDate, jFeeTxt);
	            }
	        });
		
		
		    jComboBox_Status.setModel(new ComboBoxModelItemsStatuses());
		    jComboBox_YourAddress.setModel(new AccountsComboBoxModel());
		    
		    jLabel_PersonInfo.setBorder(javax.swing.BorderFactory.createEtchedBorder());
		    
		
		    
	        Person_Info info = new Person_Info(); 
	        info.show_001(person);
	        info.setFocusable(false);
	        jLabel_PersonInfo.setViewportView( info);
	        jFormattedTextField_fromDate.setText("0000-00-00");
		    jFormattedTextField_toDate.setText("0000-00-00");
		    jPar1Txt.setText("");
		    jPar2Txt.setText("");
		    jADataTxt.setText("");
		    jAData2Txt.setText("");
		    jFeeTxt.setText("0");
		    jParentRecTxt.setText("0");
		    
		    
		
		
		setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setMinimumSize(new java.awt.Dimension(650, 23));
        setModal(true);
        setPreferredSize(new java.awt.Dimension(800, 650));
	    
        
        
        
        
	    
		//PACK
		this.pack();
        this.setResizable(false);
        this.setLocationRelativeTo(null);
        this.setVisible(true);
	   
	}
	
	private Transaction refreshRecordDetails()
	{
		
		/*
		if(Controller.getInstance().getStatus() != Controller.STATUS_OK)
		{
			infoPanel.show_mess(Lang.getInstance().translate("Status must be OK to show public key details."));
	        jLabel_RecordInfo.setViewportView(infoPanel);
			return null;
		}
		*/

		Transaction record = null;
		if (jParentRecTxt.getText().length() == 0) {
			infoPanel.show_mess(Lang.getInstance().translate(""));
	        jLabel_RecordInfo.setViewportView(infoPanel);
			return record;
		}
		
		record = R_Vouch.getVouchingRecord(DBSet.getInstance(), jParentRecTxt.getText());
		if (record == null) {
			infoPanel.show_mess(Lang.getInstance().translate("Error - use 1233-321."));
	        jLabel_RecordInfo.setViewportView(infoPanel);
			return record;
		}
		
		////ENABLE
		//jButton_Confirm.setEnabled(true);

		infoPanel.show_001(record);
		//infoPanel.setFocusable(false);
        jLabel_RecordInfo.setViewportView(infoPanel);

        return record;
	}

	public void onGoClick(
			PersonCls person, JButton Button_Confirm,
			Account creator, StatusCls status,
			JTextField fromDateTxt, JTextField toDateTxt, JTextField feePowTxt)
	{

    	if (!OnDealClick.proccess1(Button_Confirm)) return;

    	
    	long fromDate = 0;
    	long toDate = 0;
    	int feePow = 0;
    	int parse = 0;

    	int value_1 = 0;
		int value_2 = 0;
		byte[] data = jADataTxt.getText().length()==0? null:
			jADataTxt.getText().getBytes( Charset.forName("UTF-8") );
		long refParent = 0l;

    	try {

			//READ FEE POW
			feePow = Integer.parseInt(feePowTxt.getText());
			
			//READ FROM DATE
			parse++;
			String str = fromDateTxt.getText();
			if (str.equals("0000-00-00")) 
				fromDate = Long.MIN_VALUE;
			else {
				if (str.length() < 11) str = str + " 00:00:00";
				fromDate = Timestamp.valueOf(str).getTime();
			}

			//READ TO DATE
			parse++;
			str = toDateTxt.getText();
			if (str.equals("0000-00-00")) 
				toDate = Long.MAX_VALUE;
			else {
				if (str.length() < 11) str = str + " 00:00:00";
				toDate = Timestamp.valueOf(str).getTime();
			}

			//READ VALUE 1
			parse++;
			if (jPar1Txt.getText().length() > 0) {
				value_1 = Integer.parseInt(jPar1Txt.getText());
				assert(value_1 >=0);
			}

			//READ VALUE 2
			parse++;
			if (jPar2Txt.getText().length() > 0) {
				value_2 = Integer.parseInt(jPar2Txt.getText());
				assert(value_2 >=0);
			}

		}
		catch(Exception e)
		{
			if(parse == 0)
			{
				JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Invalid fee"), Lang.getInstance().translate("Error") + e, JOptionPane.ERROR_MESSAGE);
			}
			else if (parse == 1)
			{
				JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Invalid From Date") + e, Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
			}
			else if (parse == 2)
			{
				JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Invalid To Date") + e, Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
			}
			else if (parse == 3)
			{
				JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Invalid Value 1") + e, Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
			}
			else if (parse == 4)
			{
				JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Invalid Value 2") + e, Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
			}
			
			//ENABLE
			Button_Confirm.setEnabled(true);

			return;
			
		}
    	    	
		//Account authenticator =  new Account(address);
		PrivateKeyAccount authenticator = Controller.getInstance().getPrivateKeyAccountByAddress(creator.getAddress());

		int version = 0;
		if (PersonSetStatusDialog.parentRecord != null) {
			int blockID = PersonSetStatusDialog.parentRecord.getBlockHeight(DBSet.getInstance());
			int seqNo = PersonSetStatusDialog.parentRecord.getSeqNo(DBSet.getInstance());
			byte[] bytesParent = Ints.toByteArray(blockID);
			bytesParent = Bytes.concat(bytesParent, Ints.toByteArray(seqNo));
			refParent = Longs.fromByteArray(bytesParent);
		}
		
		Pair<Transaction, Integer> result = Controller.getInstance().r_SetStatusToItem(version, false, authenticator,
				feePow, status.getKey(), 
				person, fromDate, toDate,
				value_1, value_2, data, refParent
				);
		
		//CHECK VALIDATE MESSAGE
		if (result.getB() == Transaction.VALIDATE_OK) {
			JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Status assigned!"), Lang.getInstance().translate("Success"), JOptionPane.INFORMATION_MESSAGE);
			this.dispose();
		} else {
		
			JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate(OnDealClick.resultMess(result.getB())), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
		}
		
		//ENABLE
		Button_Confirm.setEnabled(true);
		
	}

	/*
	 * To change this license header, choose License Headers in Project Properties.
	 * To change this template file, choose Tools | Templates
	 * and open the template in the editor.
	 */

	
   // <editor-fold defaultstate="collapsed" desc="Generated Code">                          
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jComboBox_YourAddress = new javax.swing.JComboBox<>();
        jLabel_Address = new javax.swing.JLabel();
        jLabel_Status = new javax.swing.JLabel();
        jComboBox_Status = new javax.swing.JComboBox<>();
        jLabel_Data_From = new javax.swing.JLabel();
        jFormattedTextField_fromDate = new javax.swing.JFormattedTextField();
        jLabel_Data_To = new javax.swing.JLabel();
        jFormattedTextField_toDate = new javax.swing.JFormattedTextField();
        jLabel_Param1 = new javax.swing.JLabel();
        jPar1Txt = new javax.swing.JTextField();
        jLabel_Param2 = new javax.swing.JLabel();
        jPar2Txt = new javax.swing.JTextField();
        jLabel_Addition1 = new javax.swing.JLabel();
        jADataTxt = new javax.swing.JTextField();
        jLabel_Addition2 = new javax.swing.JLabel();
        jAData2Txt = new javax.swing.JTextField();
        jLabel_Parent_record = new javax.swing.JLabel();
        jParentRecTxt = new javax.swing.JTextField();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTextArea_Description = new javax.swing.JTextArea();
        jLabel_Fee = new javax.swing.JLabel();
        jFeeTxt = new javax.swing.JTextField();
        jButton_Cansel = new javax.swing.JButton();
        jButton_SetStatus = new javax.swing.JButton();
        jLabel_Title = new javax.swing.JLabel();
        jLabel__Description = new javax.swing.JLabel();
        jLabel_PersonInfo = new javax.swing.JScrollPane();
        jLabel1 = new javax.swing.JLabel();

  //      setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        java.awt.GridBagLayout layout1 = new java.awt.GridBagLayout();
  //      layout.columnWidths = new int[] {0, 8, 0, 8, 0, 8, 0, 8, 0, 8, 0, 8, 0};
   //     layout.rowHeights = new int[] {0, 4, 0, 4, 0, 4, 0, 4, 0, 4, 0, 4, 0, 4, 0, 4, 0, 4, 0, 4, 0, 4, 0, 4, 0, 4, 0, 4, 0};
        getContentPane().setLayout(layout1);

 //       jComboBox_YourAddress.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 9;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(15, 0, 0, 19);
        getContentPane().add(jComboBox_YourAddress, gridBagConstraints);

        jLabel_Address.setText("jLabel2");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(17, 16, 0, 0);
        getContentPane().add(jLabel_Address, gridBagConstraints);

        jLabel_Status.setText("jLabel3");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(15, 16, 0, 0);
        getContentPane().add(jLabel_Status, gridBagConstraints);

   //     jComboBox_Status.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 9;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(15, 0, 0, 19);
        getContentPane().add(jComboBox_Status, gridBagConstraints);

        jLabel_Data_From.setText("jLabel4");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(22, 16, 0, 0);
        getContentPane().add(jLabel_Data_From, gridBagConstraints);

        jFormattedTextField_fromDate.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.DateFormatter(new java.text.SimpleDateFormat("yyyy-MM-dd"))));
        jFormattedTextField_fromDate.setMinimumSize(new java.awt.Dimension(80, 20));
        jFormattedTextField_fromDate.setName(""); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(22, 0, 0, 0);
        getContentPane().add(jFormattedTextField_fromDate, gridBagConstraints);

        jLabel_Data_To.setText("jLabel5");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(22, 0, 0, 0);
        getContentPane().add(jLabel_Data_To, gridBagConstraints);

        jFormattedTextField_toDate.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.DateFormatter(new java.text.SimpleDateFormat("yyyy-MM-dd"))));
        jFormattedTextField_toDate.setMinimumSize(new java.awt.Dimension(80, 20));
        jFormattedTextField_toDate.setName(""); // NOI18N
        jFormattedTextField_toDate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jFormattedTextField_toDateActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(22, 0, 0, 1);
        getContentPane().add(jFormattedTextField_toDate, gridBagConstraints);

        jLabel_Param1.setText("jLabel6");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(21, 16, 0, 0);
        getContentPane().add(jLabel_Param1, gridBagConstraints);

        jPar1Txt.setText("jTextField1");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.gridwidth = 9;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(21, 0, 0, 19);
        getContentPane().add(jPar1Txt, gridBagConstraints);

        jLabel_Param2.setText("jLabel7");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 16, 0, 0);
        getContentPane().add(jLabel_Param2, gridBagConstraints);

        jPar2Txt.setText("jTextField2");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.gridwidth = 9;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 19);
        getContentPane().add(jPar2Txt, gridBagConstraints);

        jLabel_Addition1.setText("jLabel8");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 14;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(17, 16, 0, 0);
        getContentPane().add(jLabel_Addition1, gridBagConstraints);

        jADataTxt.setText("jTextField3");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 14;
        gridBagConstraints.gridwidth = 9;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(17, 0, 0, 19);
        getContentPane().add(jADataTxt, gridBagConstraints);

        jLabel_Addition2.setText("jLabel9");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 16;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 16, 0, 0);
        getContentPane().add(jLabel_Addition2, gridBagConstraints);

        jAData2Txt.setText("jTextField4");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 16;
        gridBagConstraints.gridwidth = 9;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 19);
        getContentPane().add(jAData2Txt, gridBagConstraints);

        jLabel_Parent_record.setText("jLabel10");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 18;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(15, 16, 0, 0);
        getContentPane().add(jLabel_Parent_record, gridBagConstraints);

        jParentRecTxt.setText("jTextField5");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 18;
        gridBagConstraints.gridwidth = 9;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(15, 0, 0, 19);
        getContentPane().add(jParentRecTxt, gridBagConstraints);

        jTextArea_Description.setColumns(20);
        jTextArea_Description.setRows(5);
        jScrollPane2.setViewportView(jTextArea_Description);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 20;
        gridBagConstraints.gridwidth = 9;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(13, 0, 0, 19);
        getContentPane().add(jScrollPane2, gridBagConstraints);

        jLabel_Fee.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel_Fee.setText("jLabel11");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 8;
        gridBagConstraints.gridy = 24;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        getContentPane().add(jLabel_Fee, gridBagConstraints);

        jFeeTxt.setText("jTextField6");
        jFeeTxt.setMinimumSize(new java.awt.Dimension(80, 20));
        jFeeTxt.setName(""); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 10;
        gridBagConstraints.gridy = 24;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 17);
        getContentPane().add(jFeeTxt, gridBagConstraints);

        jButton_Cansel.setText("jButton1");
        jButton_Cansel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_CanselActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 28;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 23, 0);
        getContentPane().add(jButton_Cansel, gridBagConstraints);

        jButton_SetStatus.setText("jButton2");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 8;
        gridBagConstraints.gridy = 28;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.ipadx = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 23, 16);
        getContentPane().add(jButton_SetStatus, gridBagConstraints);

        jLabel_Title.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel_Title.setText("jLabel12");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 11;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(10, 17, 0, 20);
        getContentPane().add(jLabel_Title, gridBagConstraints);

        jLabel__Description.setText("jLabel15");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 20;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(13, 16, 0, 0);
        getContentPane().add(jLabel__Description, gridBagConstraints);

        jLabel_PersonInfo.setMaximumSize(new java.awt.Dimension(700, 200));
        jLabel_PersonInfo.setMinimumSize(new java.awt.Dimension(500, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 11;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(0, 16, 0, 19);
        getContentPane().add(jLabel_PersonInfo, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 10;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        getContentPane().add(jLabel1, gridBagConstraints);

        pack();
    }// </editor-fold>                        

	
	
	
	
	
	
	    
	    // <editor-fold defaultstate="collapsed" desc="Generated Code">                          
	    private void initComponents(PersonCls person) {
	        java.awt.GridBagConstraints gridBagConstraints;

	        jLabel_PersonInfo = new javax.swing.JScrollPane();
	        jLabel_RecordInfo = new javax.swing.JScrollPane();

	        jComboBox_YourAddress = new javax.swing.JComboBox<>();
	        jFormattedTextField_fromDate = new javax.swing.JFormattedTextField();
	        jFormattedTextField_toDate = new javax.swing.JFormattedTextField();
	        jPar1Txt = new javax.swing.JFormattedTextField();
	        jPar2Txt = new javax.swing.JFormattedTextField();
	        jADataTxt = new javax.swing.JFormattedTextField();
	        jParentRecTxt = new javax.swing.JFormattedTextField();
	        jFeeTxt = new javax.swing.JFormattedTextField();
	        jButton_Cansel = new javax.swing.JButton();
	        jButton_SetStatus = new javax.swing.JButton();
	        jComboBox_Status = new javax.swing.JComboBox<>();

	        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
	        setMinimumSize(new java.awt.Dimension(650, 23));
	        setModal(true);
	        setPreferredSize(new java.awt.Dimension(700, 600));
	        addHierarchyBoundsListener(new java.awt.event.HierarchyBoundsListener() {
	            public void ancestorMoved(java.awt.event.HierarchyEvent evt) {
	                formAncestorMoved(evt);
	            }
	            public void ancestorResized(java.awt.event.HierarchyEvent evt) {
	            }
	        });
	        java.awt.GridBagLayout layout = new java.awt.GridBagLayout();
	        layout.columnWidths = new int[] {0, 9, 0, 9, 0, 9, 0, 9, 0, 9, 0};
	        layout.rowHeights = new int[] {0, 9, 0, 9, 0, 9, 0, 9, 0, 9, 0, 9, 0, 9, 0, 9, 0, 9, 0};
	        getContentPane().setLayout(layout);

	        gridBagConstraints = new java.awt.GridBagConstraints();
	        gridBagConstraints.gridx = 0;
	        gridBagConstraints.gridy = 0;
	        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
	        gridBagConstraints.insets = new java.awt.Insets(21, 27, 0, 0);
	        getContentPane().add(new javax.swing.JLabel(Lang.getInstance().translate("Your Address")+":"),
	        		gridBagConstraints);

	        jComboBox_YourAddress =new JComboBox<Account>(new AccountsComboBoxModel());
	        jComboBox_YourAddress.setMinimumSize(new java.awt.Dimension(500, 22));
	        jComboBox_YourAddress.setPreferredSize(new java.awt.Dimension(500, 22));
	        gridBagConstraints = new java.awt.GridBagConstraints();
	        gridBagConstraints.gridx = 2;
	        gridBagConstraints.gridy = 0;
	        gridBagConstraints.gridwidth = 7;
	        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
	        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
	        gridBagConstraints.weightx = 1.0;
	        gridBagConstraints.insets = new java.awt.Insets(21, 0, 0, 13);
	        getContentPane().add(jComboBox_YourAddress, gridBagConstraints);

	        // PERSON INFO
	        gridBagConstraints = new java.awt.GridBagConstraints();
	        gridBagConstraints.gridx = 0;
	        gridBagConstraints.gridy = 2;
	        gridBagConstraints.gridwidth = 9;
	        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
	        gridBagConstraints.weightx = 1.0;
	        gridBagConstraints.insets = new java.awt.Insets(12, 23, 0, 9);
	        getContentPane().add(new javax.swing.JLabel(Lang.getInstance().translate("Information about the person")+":"),
	        		gridBagConstraints);

	        jLabel_PersonInfo.setBorder(javax.swing.BorderFactory.createEtchedBorder());
	        Person_Info info = new Person_Info(); 
	        info.show_001(person);
	        info.setFocusable(false);
	        jLabel_PersonInfo.setViewportView( info);
	   //     jLabel_PersonInfo.setText(new Person_Info().Get_HTML_Person_Info_001(person) );
	        gridBagConstraints = new java.awt.GridBagConstraints();
	        gridBagConstraints.gridx = 0;
	        gridBagConstraints.gridy = 3;
	        gridBagConstraints.gridwidth = 11;
	        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
	        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
	        gridBagConstraints.weightx = 1.0;
	        gridBagConstraints.weighty = 1.0;
	        gridBagConstraints.insets = new java.awt.Insets(0, 9, 0, 9);
	        getContentPane().add(jLabel_PersonInfo, gridBagConstraints);
	        
	        /// STATUS
	        gridBagConstraints = new java.awt.GridBagConstraints();
	        gridBagConstraints.gridx = 0;
	        gridBagConstraints.gridy = 13;
	        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
	        gridBagConstraints.insets = new java.awt.Insets(0, 27, 0, 0);
	        getContentPane().add(new javax.swing.JLabel(Lang.getInstance().translate("Status")+":"),
	        		gridBagConstraints);
	        
	        jComboBox_Status =new JComboBox<StatusCls>(new ComboBoxModelItemsStatuses());
	        jComboBox_Status.setMinimumSize(new java.awt.Dimension(400, 22));
	        jComboBox_Status.setPreferredSize(new java.awt.Dimension(400, 22));
	        jComboBox_Status.addActionListener(new java.awt.event.ActionListener() {
	            public void actionPerformed(java.awt.event.ActionEvent evt) {
	                jComboBox_StatusActionPerformed(evt);
	            }
	        });
	        gridBagConstraints = new java.awt.GridBagConstraints();
	        gridBagConstraints.gridx = 2;
	        gridBagConstraints.gridy = 13;
	        gridBagConstraints.gridwidth = 5;
	        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
	        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
	        getContentPane().add(jComboBox_Status, gridBagConstraints);

	        /// FROM DATE
	        gridBagConstraints = new java.awt.GridBagConstraints();
	        gridBagConstraints.gridx = 0;
	        gridBagConstraints.gridy = 14;
	        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
	        gridBagConstraints.insets = new java.awt.Insets(0, 27, 0, 0);
	        getContentPane().add(new javax.swing.JLabel(Lang.getInstance().translate("From Date")+":"),
	        		gridBagConstraints);

	        try {
	            jFormattedTextField_fromDate.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.MaskFormatter("####-##-##")));
	        } catch (java.text.ParseException ex) {
	            ex.printStackTrace();
	        }
	        jFormattedTextField_fromDate.setHorizontalAlignment(javax.swing.JTextField.LEFT);
	        jFormattedTextField_fromDate.setToolTipText("0000-00-00 - set to MIN");
	        jFormattedTextField_fromDate.setMinimumSize(new java.awt.Dimension(100, 20));
	        jFormattedTextField_fromDate.setText("0000-00-00"); // NOI18N
	        jFormattedTextField_fromDate.setPreferredSize(new java.awt.Dimension(100, 20));
	        jFormattedTextField_fromDate.addActionListener(new java.awt.event.ActionListener() {
	            public void actionPerformed(java.awt.event.ActionEvent evt) {
	                jFormattedTextField_fromDateActionPerformed(evt);
	            }
	        });
	        gridBagConstraints = new java.awt.GridBagConstraints();
	        gridBagConstraints.gridx = 2;
	        gridBagConstraints.gridy = 14;
	        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
	        getContentPane().add(jFormattedTextField_fromDate, gridBagConstraints);

	        /// TO DATE
	        gridBagConstraints = new java.awt.GridBagConstraints();
	        gridBagConstraints.gridx = 3;
	        gridBagConstraints.gridy = 14;
	        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
	        gridBagConstraints.insets = new java.awt.Insets(0, 27, 0, 0);
	        getContentPane().add(new javax.swing.JLabel(Lang.getInstance().translate("To Date")+":"),
	        		gridBagConstraints);

	        try {
	        	jFormattedTextField_toDate.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(
	        			new javax.swing.text.MaskFormatter("####-##-##")));
	        } catch (java.text.ParseException ex) {
	            ex.printStackTrace();
	        }
	        jFormattedTextField_toDate.setHorizontalAlignment(javax.swing.JTextField.LEFT);
	        jFormattedTextField_toDate.setToolTipText("0000-00-00 - set to MAX");
	        jFormattedTextField_toDate.setMinimumSize(new java.awt.Dimension(100, 20));
	        jFormattedTextField_toDate.setText("0000-00-00"); // NOI18N
	        jFormattedTextField_toDate.setPreferredSize(new java.awt.Dimension(100, 20));
	        jFormattedTextField_toDate.addActionListener(new java.awt.event.ActionListener() {
	            public void actionPerformed(java.awt.event.ActionEvent evt) {
	                jFormattedTextField_toDateActionPerformed(evt);
	            }
	        });
	        gridBagConstraints = new java.awt.GridBagConstraints();
	        gridBagConstraints.gridx = 4;
	        gridBagConstraints.gridy = 14;
	        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
	        getContentPane().add(jFormattedTextField_toDate, gridBagConstraints);

	        /////////////////// PAR 1
	        gridBagConstraints = new java.awt.GridBagConstraints();
	        gridBagConstraints.gridx = 0;
	        gridBagConstraints.gridy = 15;
	        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
	        gridBagConstraints.insets = new java.awt.Insets(0, 27, 0, 0);
	        getContentPane().add(new javax.swing.JLabel(Lang.getInstance().translate("Parameter")+" 1:"),
	        		gridBagConstraints);

	        jPar1Txt.setHorizontalAlignment(javax.swing.JTextField.LEFT);
	        jPar1Txt.setText("0");
	        jPar1Txt.setMinimumSize(new java.awt.Dimension(100, 20));
	        jPar1Txt.setPreferredSize(new java.awt.Dimension(100, 20));
	        gridBagConstraints.gridx = 2;
	        gridBagConstraints.gridy = 15;
	        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
	        gridBagConstraints.insets = new java.awt.Insets(0, 0, 48, 0);
	        getContentPane().add(jPar1Txt, gridBagConstraints);

	        /////////////////// PAR 2
	        gridBagConstraints = new java.awt.GridBagConstraints();
	        gridBagConstraints.gridx = 3;
	        gridBagConstraints.gridy = 15;
	        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
	        gridBagConstraints.insets = new java.awt.Insets(0, 27, 0, 0);
	        getContentPane().add(new javax.swing.JLabel(Lang.getInstance().translate("Parameter")+" 2:"),
	        		gridBagConstraints);

	        //jFormattedTextField_Fee.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("####,###.00"))));
	        jPar2Txt.setHorizontalAlignment(javax.swing.JTextField.LEFT);
	        jPar2Txt.setText("0");
	        jPar2Txt.setMinimumSize(new java.awt.Dimension(100, 20));
	        jPar2Txt.setPreferredSize(new java.awt.Dimension(100, 20));
	        gridBagConstraints.gridx = 4;
	        gridBagConstraints.gridy = 15;
	        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
	        gridBagConstraints.insets = new java.awt.Insets(0, 0, 48, 0);
	        getContentPane().add(jPar2Txt, gridBagConstraints);

	        /////////////////// ADDITIONAL DATA
	        gridBagConstraints = new java.awt.GridBagConstraints();
	        gridBagConstraints.gridx = 0;
	        gridBagConstraints.gridy = 16;
	        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
	        gridBagConstraints.insets = new java.awt.Insets(0, 27, 0, 0);
	        getContentPane().add(new javax.swing.JLabel(Lang.getInstance().translate("Additional")+":"),
	        		gridBagConstraints);

	        //jFormattedTextField_Fee.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("####,###.00"))));
	        jADataTxt.setHorizontalAlignment(javax.swing.JTextField.LEFT);
	        jADataTxt.setText("");
	        jADataTxt.setMinimumSize(new java.awt.Dimension(400, 22));
	        jADataTxt.setPreferredSize(new java.awt.Dimension(400, 22));

	        gridBagConstraints = new java.awt.GridBagConstraints();
	        gridBagConstraints.gridx = 2;
	        gridBagConstraints.gridy = 16;
	        gridBagConstraints.gridwidth = 5;
	        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
	        gridBagConstraints.insets = new java.awt.Insets(0, 0, 48, 0);
	        getContentPane().add(jADataTxt, gridBagConstraints);

	        /////////////////// PARENT RECORD
	        gridBagConstraints = new java.awt.GridBagConstraints();
	        gridBagConstraints.gridx = 0;
	        gridBagConstraints.gridy = 17;
	        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
	        gridBagConstraints.insets = new java.awt.Insets(0, 27, 0, 0);
	        getContentPane().add(new javax.swing.JLabel(Lang.getInstance().translate("Parent Record")+":"),
	        		gridBagConstraints);

	        //jFormattedTextField_Fee.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("####,###.00"))));
	        jParentRecTxt.setHorizontalAlignment(javax.swing.JTextField.LEFT);
	        jParentRecTxt.setText("0");
	        jParentRecTxt.setMinimumSize(new java.awt.Dimension(100, 20));
	        jParentRecTxt.setPreferredSize(new java.awt.Dimension(100, 20));
	        jParentRecTxt.getDocument().addDocumentListener(new DocumentListener() {
	            
				@Override
				public void changedUpdate(DocumentEvent arg0) {
				}
				@Override
				public void insertUpdate(DocumentEvent arg0) {
					PersonSetStatusDialog.parentRecord = refreshRecordDetails();
				}
				@Override
				public void removeUpdate(DocumentEvent arg0) {
					PersonSetStatusDialog.parentRecord = refreshRecordDetails();
				}
	        });
	        gridBagConstraints = new java.awt.GridBagConstraints();
	        gridBagConstraints.gridx = 2;
	        gridBagConstraints.gridy = 17;
	        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
	        gridBagConstraints.insets = new java.awt.Insets(0, 0, 48, 0);
	        getContentPane().add(jParentRecTxt, gridBagConstraints);

	        jLabel_RecordInfo.setBorder(javax.swing.BorderFactory.createEtchedBorder());
	        infoPanel = new Record_Info(); 
	        //info.show_001(record);
	        //infoPanel.setFocusable(false);
	        //jLabel_RecordInfo.setViewportView(infoPanel);

	        gridBagConstraints = new java.awt.GridBagConstraints();
	        gridBagConstraints.gridx = 0;
	        gridBagConstraints.gridy = 19;
	        gridBagConstraints.gridwidth = 11;
	        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
	        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
	        gridBagConstraints.weightx = 1.0;
	        gridBagConstraints.weighty = 1.0;
	    //    gridBagConstraints.insets = new java.awt.Insets(12, 9, 0, 9);
	        gridBagConstraints.insets = new java.awt.Insets(0, 9, 0, 9);
	        getContentPane().add(jLabel_RecordInfo, gridBagConstraints);

	        
	        //////////////// FEE
	        gridBagConstraints = new java.awt.GridBagConstraints();
	        gridBagConstraints.gridx = 0;
	        gridBagConstraints.gridy = 20;
	        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
	        gridBagConstraints.insets = new java.awt.Insets(0, 27, 0, 0);
	        getContentPane().add(new javax.swing.JLabel(Lang.getInstance().translate("Fee Power (0..6)")+":"),
	        		gridBagConstraints);

	        //jFormattedTextField_Fee.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("####,###.00"))));
	        jFeeTxt.setHorizontalAlignment(javax.swing.JTextField.LEFT);
	        jFeeTxt.setText("0");
	        jFeeTxt.setMinimumSize(new java.awt.Dimension(100, 20));
	        jFeeTxt.setPreferredSize(new java.awt.Dimension(100, 20));
	        jFeeTxt.addActionListener(new java.awt.event.ActionListener() {
	            public void actionPerformed(java.awt.event.ActionEvent evt) {
	                jFormattedTextField_FeeActionPerformed(evt);
	            }
	        });
	        gridBagConstraints = new java.awt.GridBagConstraints();
	        gridBagConstraints.gridx = 2;
	        gridBagConstraints.gridy = 20;
	        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
	        gridBagConstraints.insets = new java.awt.Insets(0, 0, 48, 0);
	        getContentPane().add(jFeeTxt, gridBagConstraints);

	        jButton_Cansel.setText(Lang.getInstance().translate("Cancel"));
	        jButton_Cansel.addActionListener(new java.awt.event.ActionListener() {
	            public void actionPerformed(java.awt.event.ActionEvent evt) {
	            	dispose();	
	            }
	        });
	        gridBagConstraints = new java.awt.GridBagConstraints();
	        gridBagConstraints.gridx = 3;
	        gridBagConstraints.gridy = 22;
	        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
	        gridBagConstraints.insets = new java.awt.Insets(1, 0, 29, 0);
	        getContentPane().add(jButton_Cansel, gridBagConstraints);

	        jButton_SetStatus.setText(Lang.getInstance().translate("Set status"));
	        jButton_SetStatus.setToolTipText("");
	        jButton_SetStatus.addActionListener(new java.awt.event.ActionListener() {
	            public void actionPerformed(java.awt.event.ActionEvent evt) {
	            	onGoClick(person, jButton_SetStatus, (Account)jComboBox_YourAddress.getSelectedItem(),
	            			(StatusCls)jComboBox_Status.getSelectedItem(),
	            			jFormattedTextField_fromDate, jFormattedTextField_toDate, jFeeTxt);
	            }
	        });
	        gridBagConstraints = new java.awt.GridBagConstraints();
	        gridBagConstraints.gridx = 5;
	        gridBagConstraints.gridy = 22;
	        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_END;
	        getContentPane().add(jButton_SetStatus, gridBagConstraints);

	        pack();
	    }// </editor-fold>                        

	    private void jFormattedTextField_fromDateActionPerformed(java.awt.event.ActionEvent evt) {                                                         
	        // TODO add your handling code here:
	    }                                                        

	    private void jFormattedTextField_toDateActionPerformed(java.awt.event.ActionEvent evt) {                                                         
	        // TODO add your handling code here:
	    }                                                        

	    private void jButton_CanselActionPerformed(java.awt.event.ActionEvent evt) {                                               
	        // TODO add your handling code here:
	    }                                              

	    private void jButton_ConfirmActionPerformed(java.awt.event.ActionEvent evt) {                                                
	        // TODO add your handling code here:
	    }                                               

	    private void jFormattedTextField_FeeActionPerformed(java.awt.event.ActionEvent evt) {                                                        
	        // TODO add your handling code here:
	    }                                                       

	    private void formAncestorMoved(java.awt.event.HierarchyEvent evt) {                                   
	        // TODO add your handling code here:
	    }                                  

	    private void jComboBox_StatusActionPerformed(java.awt.event.ActionEvent evt) {                                                 
	        // TODO add your handling code here:
	    }                                                

	    /**
	     * @param args the command line arguments
	     */
	  

	    // Variables declaration - do not modify                     
	    private javax.swing.JButton jButton_Cansel;
	    private javax.swing.JButton jButton_SetStatus;
	    private JComboBox<StatusCls> jComboBox_Status;
	    private JComboBox<Account> jComboBox_YourAddress;
	    private javax.swing.JTextField jPar1Txt;
	    private javax.swing.JTextField jPar2Txt;
//	    private javax.swing.JTextField jADataTxt;
	    private javax.swing.JTextField jParentRecTxt;
	    private javax.swing.JTextField jFeeTxt;
	    private javax.swing.JFormattedTextField jFormattedTextField_fromDate;
	    private javax.swing.JFormattedTextField jFormattedTextField_toDate;
	//    private javax.swing.JScrollPane jLabel_PersonInfo;
	    private javax.swing.JScrollPane jLabel_RecordInfo;
	    // End of variables declaration                   
	
}
