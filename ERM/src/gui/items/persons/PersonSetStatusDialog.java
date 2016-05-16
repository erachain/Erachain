package gui.items.persons;

import java.awt.Dimension;
import java.sql.Timestamp;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import controller.Controller;
import core.account.Account;
import core.account.PrivateKeyAccount;
import core.item.persons.PersonCls;
import core.item.statuses.StatusCls;
import core.transaction.Transaction;
import gui.models.AccountsComboBoxModel;
import gui.items.statuses.ComboBoxModelItemsStatuses;
import gui.transaction.OnDealClick;
import lang.Lang;
import utils.Pair;

//public class PersonConfirm extends JDialog { // InternalFrame  {
public class PersonSetStatusDialog extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
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
			str = toDateTxt.getText();
			if (str.equals("0000-00-00")) 
				toDate = Long.MAX_VALUE;
			else {
				if (str.length() < 11) str = str + " 00:00:00";
				toDate = Timestamp.valueOf(str).getTime();
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
			
			//ENABLE
			Button_Confirm.setEnabled(true);

			return;
			
		}
    	    	
		//Account authenticator =  new Account(address);
		PrivateKeyAccount authenticator = Controller.getInstance().getPrivateKeyAccountByAddress(creator.getAddress());

		int version = 0;
		
		Pair<Transaction, Integer> result = Controller.getInstance().r_SetStatusToItem(version, false, authenticator,
				feePow, status.getKey(), 
				person, fromDate, toDate);
		
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
	        jComboBox_YourAddress = new javax.swing.JComboBox<>();
	        jFormattedTextField_fromDate = new javax.swing.JFormattedTextField();
	        jFormattedTextField_toDate = new javax.swing.JFormattedTextField();
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

	        jLabel_PersonInfo.setBorder(javax.swing.BorderFactory.createEtchedBorder());
	        Person_Info info = new Person_Info(); 
	        info.show_001(person);
	        info.setFocusable(false);
	        jLabel_PersonInfo.setViewportView( info);
	   //     jLabel_PersonInfo.setText(new Person_Info().Get_HTML_Person_Info_001(person) );
	        gridBagConstraints = new java.awt.GridBagConstraints();
	        gridBagConstraints.gridx = 0;
	        gridBagConstraints.gridy = 4;
	        gridBagConstraints.gridwidth = 11;
	        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
	        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
	        gridBagConstraints.weightx = 1.0;
	        gridBagConstraints.weighty = 1.0;
	        gridBagConstraints.insets = new java.awt.Insets(0, 9, 0, 9);
	        getContentPane().add(jLabel_PersonInfo, gridBagConstraints);

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

	        gridBagConstraints = new java.awt.GridBagConstraints();
	        gridBagConstraints.gridx = 0;
	        gridBagConstraints.gridy = 10;
	        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
	        gridBagConstraints.insets = new java.awt.Insets(0, 27, 0, 0);
	        getContentPane().add(new javax.swing.JLabel(Lang.getInstance().translate("Status")+":"),
	        		gridBagConstraints);

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

	        //////////////// FEE
	        gridBagConstraints = new java.awt.GridBagConstraints();
	        gridBagConstraints.gridx = 6;
	        gridBagConstraints.gridy = 14;
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
	        gridBagConstraints.gridx = 7;
	        gridBagConstraints.gridy = 14;
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
	        gridBagConstraints.gridx = 4;
	        gridBagConstraints.gridy = 18;
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
	        gridBagConstraints.gridx = 6;
	        gridBagConstraints.gridy = 18;
	        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_END;
	        getContentPane().add(jButton_SetStatus, gridBagConstraints);

	        gridBagConstraints = new java.awt.GridBagConstraints();
	        gridBagConstraints.gridx = 0;
	        gridBagConstraints.gridy = 2;
	        gridBagConstraints.gridwidth = 9;
	        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
	        gridBagConstraints.weightx = 1.0;
	        gridBagConstraints.insets = new java.awt.Insets(12, 23, 0, 9);
	        getContentPane().add(new javax.swing.JLabel(Lang.getInstance().translate("Information about the person")+":"),
	        		gridBagConstraints);

	        //jComboBox_Status.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
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
	        gridBagConstraints.gridy = 10;
	        gridBagConstraints.gridwidth = 5;
	        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
	        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
	        getContentPane().add(jComboBox_Status, gridBagConstraints);

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
	    private javax.swing.JTextField jFeeTxt;
	    private javax.swing.JFormattedTextField jFormattedTextField_fromDate;
	    private javax.swing.JFormattedTextField jFormattedTextField_toDate;
	    private javax.swing.JScrollPane jLabel_PersonInfo;
	    // End of variables declaration                   
	
}
