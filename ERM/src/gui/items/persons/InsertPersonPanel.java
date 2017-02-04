package gui.items.persons;

import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import com.toedter.calendar.JDateChooser;

import core.account.Account;
import lang.Lang;

public class InsertPersonPanel extends IssuePersonPanel{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	
	
	protected JTextField txt_Sign;
	protected JTextField txt_public_key;
	protected JTextField txt_info;
    protected javax.swing.JButton trans_Button;
    protected javax.swing.JLabel label_Sign;
    protected javax.swing.JLabel label_public_key;
    protected javax.swing.JLabel label_info;
    protected javax.swing.JPanel jPanel_Paste;
    protected javax.swing.JButton pasteButton;
    
	
	InsertPersonPanel(){
		
	install();	
		
		
		
	}
	
@SuppressWarnings("deprecation")
private void install(){
	
	
	txt_Sign  = new javax.swing.JTextField();
	txt_public_key = new javax.swing.JTextField();
	txt_info = new javax.swing.JTextField();
	
	label_Sign= new javax.swing.JLabel();
	label_public_key= new javax.swing.JLabel();
	label_info= new javax.swing.JLabel();
	
	
	super.cbxFrom.setEnabled(false);
	super.txtFeePow.setEditable(false);
	super.txtName.setEditable(false);
	super.txtareaDescription.setEditable(false);
	super.txtBirthday.enable(false);
	super.txtDeathday.enable(false);
	super.iconButton.setEnabled(false);
	super.txtGender.setEnabled(false);
	super.txtRace.setEditable(false);
	super.txtBirthLatitude.setEditable(false);
	super.txtBirthLongitude.setEditable(false);
	super.txtSkinColor.setEditable(false);
	super.txtEyeColor.setEditable(false);
	super.txtHairСolor.setEditable(false);
	super.txtHeight.setEditable(false);
	super.issueButton.setVisible(false);
	
	
	
	label_Sign.setText( Lang.getInstance().translate("Signature")+ ":");
     GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
     gridBagConstraints.gridx = 0;
     gridBagConstraints.gridy = 17;
     gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
     gridBagConstraints.insets = new java.awt.Insets(0, 18, 0, 0);
     add(label_Sign, gridBagConstraints);
     gridBagConstraints = new java.awt.GridBagConstraints();
     gridBagConstraints.gridx = 2;
     gridBagConstraints.gridy = 17;
     gridBagConstraints.gridwidth = 3;
     gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
     gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
     gridBagConstraints.weightx = 0.2;
     gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 1);
     txt_Sign.setEditable(false);
     add(txt_Sign, gridBagConstraints);
	
     label_public_key.setText( Lang.getInstance().translate("Public Key")+ ":");
     gridBagConstraints.gridx = 0;
     gridBagConstraints.gridy = 18;
     gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
     gridBagConstraints.insets = new java.awt.Insets(0, 18, 0, 0);
     add(label_public_key, gridBagConstraints);
     gridBagConstraints = new java.awt.GridBagConstraints();
     gridBagConstraints.gridx = 2;
     gridBagConstraints.gridy = 18;
     gridBagConstraints.gridwidth = 3;
     gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
     gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
     gridBagConstraints.weightx = 0.2;
     gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 1);
     add(txt_public_key, gridBagConstraints);
     
     
     
     
	
	
	
	 pasteButton = new JButton();
     pasteButton.setText(Lang.getInstance().translate("Paste") +"...");
     pasteButton.addActionListener(new ActionListener(){

 		@Override
 		public void actionPerformed(ActionEvent arg0) {
 			// TODO Auto-generated method stub
 			
 		}
     	 
     	 
      });

     GridBagConstraints gridBagConstraints1 = new java.awt.GridBagConstraints();
     gridBagConstraints1.gridx = 4;
     gridBagConstraints1.gridy = 19;
  //   gridBagConstraints1.gridwidth = ;
     gridBagConstraints1.anchor = java.awt.GridBagConstraints.FIRST_LINE_END;
     gridBagConstraints1.insets = new java.awt.Insets(20, 0, 0, 0);
     add(pasteButton, gridBagConstraints1);
	
     
     
    
     trans_Button = new JButton();
     trans_Button.setText(Lang.getInstance().translate("Check")+ " & " + Lang.getInstance().translate("Issue") + "...");
     trans_Button.addActionListener(new ActionListener(){

		@Override
		public void actionPerformed(ActionEvent arg0) {
			// TODO Auto-generated method stub
			
		}
    	 
    	 
     });

  
     gridBagConstraints1.gridx = 6;
     gridBagConstraints1.gridy = 19;
 //    gridBagConstraints1.gridwidth = 15;
     gridBagConstraints1.anchor = java.awt.GridBagConstraints.FIRST_LINE_END;
     gridBagConstraints1.insets = new java.awt.Insets(20, 0, 0, 16);
     add(trans_Button, gridBagConstraints1);
	
	
	
}

}
