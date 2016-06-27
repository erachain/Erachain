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

	public PersonSetStatusDialog(JComponent  apers, PersonCls person) {
		super();
	
this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		initComponents(person);
		this.setTitle(Lang.getInstance().translate("Set Status"));
		this.setResizable(true);
	    setPreferredSize(new Dimension(400, 600));
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
	        gridBagConstraints.gridx = 4;
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
	    private javax.swing.JTextField jADataTxt;
	    private javax.swing.JTextField jParentRecTxt;
	    private javax.swing.JTextField jFeeTxt;
	    private javax.swing.JFormattedTextField jFormattedTextField_fromDate;
	    private javax.swing.JFormattedTextField jFormattedTextField_toDate;
	    private javax.swing.JScrollPane jLabel_PersonInfo;
	    private javax.swing.JScrollPane jLabel_RecordInfo;
	    // End of variables declaration                   
	
}
