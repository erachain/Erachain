package gui.items.persons;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Dialog.ModalityType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.mapdb.Fun.Tuple3;
import org.mapdb.Fun.Tuple4;

import api.ApiErrorFactory;
import controller.Controller;
import core.account.Account;
import core.account.PrivateKeyAccount;
import core.account.PublicKeyAccount;
import core.crypto.Base58;
import core.crypto.Crypto;
import core.item.assets.AssetCls;
import core.item.persons.PersonCls;
import core.item.statuses.StatusCls;
import core.transaction.Transaction;
import database.DBSet;
import gui.MainFrame;
import gui.models.AccountsComboBoxModel;
import gui.items.statuses.ComboBoxModelItemsStatuses;
import gui.transaction.OnDealClick;
import lang.Lang;
import ntp.NTP;
import utils.NameUtils;
import utils.Pair;
import utils.NameUtils.NameResult;

//public class PersonConfirm extends JDialog { // InternalFrame  {
public class PersonSetStatusDialog extends JDialog {

	private JComboBox<Account> accountLBox;
	private JComboBox<StatusCls> statusLBox;

	public PersonSetStatusDialog(JComponent  apers, PersonCls person) {
		super();
	
this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
/*
final JTextField pubKey1Txt = new JTextField();
		final JTextField pubKey2Txt = new JTextField();
		final JTextField pubKey3Txt = new JTextField();
		final JLabel pubKey1Details = new JLabel();
		final JLabel pubKey2Details = new JLabel();
		final JLabel pubKey3Details = new JLabel();

		final JTextField toDate = new JTextField();
		final JTextField feePow = new JTextField();
	*/	
		
	//	this.setBorder(new EmptyBorder(10, 10, 10, 10));
		//	MainFrame mainFram = new MainFrame();
	
		
		initComponents(person);
		this.setTitle(Lang.getInstance().translate("Set Status"));
		//	this.setClosable(true);
			this.setResizable(true);
			this.setModal(true);
		//CLOSE
		// setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 /*   
		//LAYOUT
		this.setLayout(new GridBagLayout());
		// Create and specify a layout manager
	    this.setLayout(new GridBagLayout());

	    int gridy = 0;
	    // Create a constraints object, and specify some default values
	    GridBagConstraints input = new GridBagConstraints();
	    input.insets = new Insets(0, 5, 5, 0); // 5-pixel margins on all sides
		input.fill = GridBagConstraints.HORIZONTAL;   
		input.anchor = GridBagConstraints.NORTHWEST;
	    input.gridwidth = 5;
	    input.gridheight = 1;

	    GridBagConstraints label = new GridBagConstraints();	    
	    label.insets = new Insets(0, 5, 5, 0);
		label.fill = GridBagConstraints.HORIZONTAL;   
		label.anchor = GridBagConstraints.NORTHWEST;
		label.gridx = 0;
		label.gridheight = 1;

	    GridBagConstraints detail = new GridBagConstraints();
	    detail.insets = new Insets(0, 5, 5, 0);
	    detail.fill = GridBagConstraints.BOTH; // components grow in both dimensions
	    detail.anchor = GridBagConstraints.NORTHWEST;
	    detail.gridx = 0;
	    detail.gridwidth = 5;
	    detail.gridheight = 2;
	    detail.weightx = -1;
	    detail.weighty = -1;

		//LABEL FROM
		JLabel accountLabel = new JLabel(Lang.getInstance().translate("Account") + ":");
		this.add(accountLabel, label);
		
		//COMBOBOX FROM
		this.accountLBox = new JComboBox<Account>(new AccountsComboBoxModel());
        this.add(this.accountLBox, input);

		//LABEL STATUS
        label.gridy = ++gridy;
		JLabel statusLabel = new JLabel(Lang.getInstance().translate("Status") + ":");
		this.add(statusLabel, label);

		//COMBOBOX STATUS
		input.gridy = gridy;
		this.statusLBox =  new JComboBox<StatusCls>(new ComboBoxModelItemsStatuses());
        this.add((JComponent)this.statusLBox, input);
	          		
        input.gridx = 0;
	    input.gridy = ++gridy;
	    input.gridwidth = 5;
	    input.gridheight = 1;
	    this.add(new JLabel(Lang.getInstance().translate("To date (0 - is permanent)") +":"), input);

	    input.gridx = 2;
	    input.gridy = gridy;
	    input.gridwidth = 3;
	    input.gridheight = 1;
	    this.add(toDate, input);

	    // FEE POWER
        input.gridx = 0;
	    input.gridy = ++gridy;
	    input.gridwidth = 5;
	    input.gridheight = 1;
	    this.add(new JLabel(Lang.getInstance().translate("Fee Power") +":"), input);

	    input.gridx = 2;
	    input.gridy = gridy;
	    input.gridwidth = 3;
	    input.gridheight = 1;
      	feePow.setText("0");
	    this.add(feePow, input);

	    // BUTTONS
	    input.gridx = 2;
	    input.gridy = ++gridy;
	    input.gridwidth = 1;
	    input.gridheight = 1;
	    JButton Button_Cancel = new JButton(Lang.getInstance().translate("Cancel"));
	    Button_Cancel.addActionListener(new ActionListener()
		{
		    public void actionPerformed(ActionEvent e)
		    {
		// программа обработки при нажатии cancel
		    	dispose();	
		    }
		});
	    this.add( Button_Cancel, input);

	    input.gridx = 4;
	    input.gridy = gridy;
	    input.gridwidth = 1;
	    input.gridheight = 1;
	    JButton Button_Confirm = new JButton(Lang.getInstance().translate("Confirm"));
	    this.add(Button_Confirm, input);
	    Button_Confirm.addActionListener(new ActionListener()
		{
		    public void actionPerformed(ActionEvent e)
		    {
		    	onGoClick(person, Button_Confirm, pubKey1Txt, pubKey1Txt, pubKey1Txt, toDate, feePow);
		    }
		});
	*/   
	    setPreferredSize(new Dimension(400, 600));
	 //   setSize(400,600);
		//PACK
		this.pack();
        this.setResizable(false);
        this.setLocationRelativeTo(null);
        this.setVisible(true);
	    //MainFrame.this.add(comp, constraints).setFocusable(false);
	}
	

	public void onGoClick(
			PersonCls person, JButton Button_Confirm,
			JTextField pubKey1Txt, JTextField pubKey2Txt, JTextField pubKey3Txt, JTextField toDateTxt, JTextField feePowTxt)
	{

    	if (!OnDealClick.proccess1(Button_Confirm)) return;

		Account creator = (Account) this.accountLBox.getSelectedItem();
    	//String address = pubKey1Txt.getText();
    	int toDate = 0;
    	int feePow = 0;
    	int parse = 0;
    	String toDateStr = toDateTxt.getText();
		try {

			//READ FEE POW
			feePow = Integer.parseInt(feePowTxt.getText());
			
			//READ to DAY
			parse++;
	    	if (toDateStr.length() > 0)
    			toDate = Integer.parseInt(toDateStr);
    		}
		catch(Exception e)
		{
			if(parse == 0)
			{
				JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Invalid fee"), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
			}
			else
			{
				JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Invalid to Date"), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
			}
		}
    	
	    List<PublicKeyAccount> sertifiedPublicKeys = new ArrayList<PublicKeyAccount>();
	    if (pubKey1Txt.getText().length() == 30) {
	    	PublicKeyAccount userAccount1 = new PublicKeyAccount(Base58.decode(pubKey1Txt.getText()));
	    	if (userAccount1.isValid()) sertifiedPublicKeys.add(userAccount1);
	    }
	    if (pubKey2Txt.getText().length() > 30) {
	    	PublicKeyAccount userAccount2 = new PublicKeyAccount(Base58.decode(pubKey2Txt.getText()));
	    	if (userAccount2.isValid()) sertifiedPublicKeys.add(userAccount2);
	    }
	    if (pubKey3Txt.getText().length() > 30) {
	    	PublicKeyAccount userAccount3 = new PublicKeyAccount(Base58.decode(pubKey3Txt.getText()));
	    	if (userAccount3.isValid()) sertifiedPublicKeys.add(userAccount3);
	    }
    	
		//Account authenticator =  new Account(address);
		PrivateKeyAccount authenticator = Controller.getInstance().getPrivateKeyAccountByAddress(creator.getAddress());

		int version = 0; // without user signs
		
		Pair<Transaction, Integer> result = Controller.getInstance().r_SertifyPerson(version, false, authenticator,
				feePow, person.getKey(), 
				sertifiedPublicKeys, toDate);
		
		//CHECK VALIDATE MESSAGE
		if (result.getB() == Transaction.VALIDATE_OK) {
			JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Person has been authenticated!"), Lang.getInstance().translate("Success"), JOptionPane.INFORMATION_MESSAGE);
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

	    @SuppressWarnings("unchecked")
	    // <editor-fold defaultstate="collapsed" desc="Generated Code">                          
	    private void initComponents(PersonCls person) {
	        java.awt.GridBagConstraints gridBagConstraints;

	        jLabel_PersonInfo = new javax.swing.JScrollPane();
	        jLabel_YourAddress = new javax.swing.JLabel();
	        jComboBox_YourAddress = new javax.swing.JComboBox<>();
	        jLabel_Status = new javax.swing.JLabel();
	        jLabel_ToDo = new javax.swing.JLabel();
	        jFormattedTextField_ToDo = new javax.swing.JFormattedTextField();
	        jLabel_Fee = new javax.swing.JLabel();
	        jFormattedTextField_Fee = new javax.swing.JFormattedTextField();
	        jButton_Cansel = new javax.swing.JButton();
	        jButton_SetStatus = new javax.swing.JButton();
	        jLabel_ToDo_Check = new javax.swing.JLabel();
	        jLabel_Fee_Check = new javax.swing.JLabel();
	        jLabel_Title = new javax.swing.JLabel();
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

	        jLabel_YourAddress.setText(Lang.getInstance().translate("Your Address:")+":");
	        gridBagConstraints = new java.awt.GridBagConstraints();
	        gridBagConstraints.gridx = 0;
	        gridBagConstraints.gridy = 0;
	        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
	        gridBagConstraints.insets = new java.awt.Insets(21, 27, 0, 0);
	        getContentPane().add(jLabel_YourAddress, gridBagConstraints);

	       // jComboBox_YourAddress.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
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

	        jLabel_Status.setText(Lang.getInstance().translate("Status") + ":");
	        gridBagConstraints = new java.awt.GridBagConstraints();
	        gridBagConstraints.gridx = 0;
	        gridBagConstraints.gridy = 10;
	        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
	        gridBagConstraints.insets = new java.awt.Insets(0, 27, 0, 0);
	        getContentPane().add(jLabel_Status, gridBagConstraints);

	        jLabel_ToDo.setText(Lang.getInstance().translate("To Do:"));
	        gridBagConstraints = new java.awt.GridBagConstraints();
	        gridBagConstraints.gridx = 0;
	        gridBagConstraints.gridy = 14;
	        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
	        gridBagConstraints.insets = new java.awt.Insets(0, 27, 0, 0);
	        getContentPane().add(jLabel_ToDo, gridBagConstraints);

	        try {
	            jFormattedTextField_ToDo.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.MaskFormatter("##.##.####")));
	        } catch (java.text.ParseException ex) {
	            ex.printStackTrace();
	        }
	        jFormattedTextField_ToDo.setHorizontalAlignment(javax.swing.JTextField.LEFT);
	        jFormattedTextField_ToDo.setToolTipText("");
	        jFormattedTextField_ToDo.setMinimumSize(new java.awt.Dimension(100, 20));
	        jFormattedTextField_ToDo.setName(""); // NOI18N
	        jFormattedTextField_ToDo.setPreferredSize(new java.awt.Dimension(100, 20));
	        jFormattedTextField_ToDo.addActionListener(new java.awt.event.ActionListener() {
	            public void actionPerformed(java.awt.event.ActionEvent evt) {
	                jFormattedTextField_ToDoActionPerformed(evt);
	            }
	        });
	        gridBagConstraints = new java.awt.GridBagConstraints();
	        gridBagConstraints.gridx = 2;
	        gridBagConstraints.gridy = 14;
	        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
	        getContentPane().add(jFormattedTextField_ToDo, gridBagConstraints);

	        jLabel_Fee.setText(Lang.getInstance().translate("fee :"));
	        gridBagConstraints = new java.awt.GridBagConstraints();
	        gridBagConstraints.gridx = 0;
	        gridBagConstraints.gridy = 16;
	        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
	        gridBagConstraints.insets = new java.awt.Insets(0, 27, 0, 0);
	        getContentPane().add(jLabel_Fee, gridBagConstraints);

	        jFormattedTextField_Fee.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("####,###.00"))));
	        jFormattedTextField_Fee.setHorizontalAlignment(javax.swing.JTextField.LEFT);
	        jFormattedTextField_Fee.setMinimumSize(new java.awt.Dimension(100, 20));
	        jFormattedTextField_Fee.setPreferredSize(new java.awt.Dimension(100, 20));
	        jFormattedTextField_Fee.addActionListener(new java.awt.event.ActionListener() {
	            public void actionPerformed(java.awt.event.ActionEvent evt) {
	                jFormattedTextField_FeeActionPerformed(evt);
	            }
	        });
	        gridBagConstraints = new java.awt.GridBagConstraints();
	        gridBagConstraints.gridx = 2;
	        gridBagConstraints.gridy = 16;
	        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
	        gridBagConstraints.insets = new java.awt.Insets(0, 0, 48, 0);
	        getContentPane().add(jFormattedTextField_Fee, gridBagConstraints);

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
	         //   	onGoClick(person,  jButton_SetStatus, pubKey1Txt, pubKey1Txt, pubKey1Txt, toDate, feePow);
	            }
	        });
	        gridBagConstraints = new java.awt.GridBagConstraints();
	        gridBagConstraints.gridx = 6;
	        gridBagConstraints.gridy = 18;
	        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_END;
	        getContentPane().add(jButton_SetStatus, gridBagConstraints);

	        jLabel_ToDo_Check.setText(Lang.getInstance().translate("insert date"));
	        gridBagConstraints = new java.awt.GridBagConstraints();
	        gridBagConstraints.gridx = 4;
	        gridBagConstraints.gridy = 14;
	        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
	        getContentPane().add(jLabel_ToDo_Check, gridBagConstraints);

	        jLabel_Fee_Check.setText(Lang.getInstance().translate("insert fee"));
	        gridBagConstraints = new java.awt.GridBagConstraints();
	        gridBagConstraints.gridx = 4;
	        gridBagConstraints.gridy = 16;
	        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
	        getContentPane().add(jLabel_Fee_Check, gridBagConstraints);

	        jLabel_Title.setText(Lang.getInstance().translate("Information about the person"));
	        gridBagConstraints = new java.awt.GridBagConstraints();
	        gridBagConstraints.gridx = 0;
	        gridBagConstraints.gridy = 2;
	        gridBagConstraints.gridwidth = 9;
	        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
	        gridBagConstraints.weightx = 1.0;
	        gridBagConstraints.insets = new java.awt.Insets(12, 23, 0, 9);
	        getContentPane().add(jLabel_Title, gridBagConstraints);

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

	    private void jFormattedTextField_ToDoActionPerformed(java.awt.event.ActionEvent evt) {                                                         
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
	    private javax.swing.JFormattedTextField jFormattedTextField_Fee;
	    private javax.swing.JFormattedTextField jFormattedTextField_ToDo;
	    private javax.swing.JLabel jLabel_Fee;
	    private javax.swing.JLabel jLabel_Fee_Check;
	    private javax.swing.JScrollPane jLabel_PersonInfo;
	    private javax.swing.JLabel jLabel_Status;
	    private javax.swing.JLabel jLabel_Title;
	    private javax.swing.JLabel jLabel_ToDo;
	    private javax.swing.JLabel jLabel_ToDo_Check;
	    private javax.swing.JLabel jLabel_YourAddress;
	    // End of variables declaration                   
	

	
	
	
}
