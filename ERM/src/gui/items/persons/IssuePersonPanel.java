package gui.items.persons;

import gui.PasswordPane;
import gui.models.AccountsComboBoxModel;
import lang.Lang;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.MaskFormatter;

import com.toedter.calendar.JDateChooser;

import utils.Pair;
import controller.Controller;
import core.account.Account;
import core.account.PrivateKeyAccount;
import core.transaction.Transaction;

import gui.transaction.OnDealClick;

@SuppressWarnings("serial")
public class IssuePersonPanel extends JPanel 
{
	
	private  MaskFormatter AccFormat;
	private JComboBox<Account> cbxFrom;
	private JTextField txtFeePow;
	private JTextField txtName;
	private JTextArea txtareaDescription;
	private JDateChooser txtBirthday;
	private JDateChooser txtDeathday;
	private JButton iconButton;
	@SuppressWarnings("rawtypes")
	private JComboBox txtGender;
	private JTextField txtRace;
	private JTextField txtBirthLatitude;
	private JTextField txtBirthLongitude;
	private JTextField txtSkinColor;
	private JTextField txtEyeColor;
	private JTextField txtHairСolor;
	private JTextField txtHeight;
    private javax.swing.JButton issueButton;
    private javax.swing.JLabel jLabel_Fee;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLabel jLabel_Account;
    private javax.swing.JLabel jLabel_BirthLatitude;
    private javax.swing.JLabel jLabel_BirthLongitude;
    private javax.swing.JLabel jLabel_Born;
    private javax.swing.JLabel jLabel_Dead;
    private javax.swing.JLabel jLabel_Description;
    private javax.swing.JLabel jLabel_EyeColor;
    private javax.swing.JLabel jLabel_Gender;
    private javax.swing.JLabel jLabel_HairСolor;
    private javax.swing.JLabel jLabel_Height;
    private javax.swing.JLabel jLabel_Name;
    private javax.swing.JLabel jLabel_Race;
    private javax.swing.JLabel jLabel_SkinColor;
    private javax.swing.JLabel jLabel_Title;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
  
    // End of variables declaration
	
	private byte[] imgButes;

	@SuppressWarnings({ "unchecked" })
	public IssuePersonPanel()
	{

		
		initComponents();
		initLabelsText();
		
		cbxFrom.setModel(new AccountsComboBoxModel());
      
		
		
		txtName.setText("");
	// проверка на код
		txtName.addFocusListener(new FocusListener(){

			@Override
			public void focusGained(FocusEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void focusLost(FocusEvent e) {
				// TODO Auto-generated method stub
				if (txtName.getText().getBytes().length<2) {
					
					JOptionPane.showMessageDialog(null, Lang.getInstance().translate("the name must be longer than 2 characters"), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
					txtName.requestFocus();
				}
				
			}
			
			
			
		});
      	

		
        
    	
    	String[] items = {
      			Lang.getInstance().translate("Male"),
      			Lang.getInstance().translate("Female"),
      			Lang.getInstance().translate("-")
        	};	
       	txtGender.setModel(new javax.swing.DefaultComboBoxModel<>(items));
       	
       	
       	
       	
      // 	txtBirthday.setText("1970-12-08");
       	
       
       	
       	
       	

       	
       	
  //     	txtDeathday.setText("0000-00-00");
       	
       	
       	
       	
       	
       	txtRace.setText("-");
       	this.txtBirthLatitude.setText("45.123");
       	this.txtBirthLongitude.setText("12.123");
       	this.txtHeight.setText("170");
       	this.txtFeePow.setText("0");
 // issue buton
       	this.issueButton.setText(Lang.getInstance().translate("Issue"));
        this.issueButton.setPreferredSize(new Dimension(120, 30));
        this.issueButton.addActionListener(new ActionListener()
		{
		    public void actionPerformed(ActionEvent e)
		    {
		        onIssueClick();
		    }
		});
 // add icin
        iconButton.setText(Lang.getInstance().translate("Add Image")+ "...");
        iconButton.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);//.LEADING);
        iconButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        iconButton.setVerticalAlignment(javax.swing.SwingConstants.CENTER);//.TOP);
        iconButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        iconButton.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
			addimage();
			}
        });
        
        
        
        
        this.setVisible(true);
        
        
    
        
        
	}
	
	
	
    @SuppressWarnings("resource")
	private static byte[] getBytesFromFile(File file) throws IOException {
        InputStream is = new FileInputStream(file);
        long length = file.length();
        if (length > Integer.MAX_VALUE) {
            // File is too large
        }
        byte[] bytes = new byte[(int)length];
        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length
               && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
            offset += numRead;
        }
        if (offset < bytes.length) {
            throw new IOException("Could not completely read file "+file.getName());
        }
        is.close();
        return bytes;
    }
    
    private void initLabelsText(){
    	
    	jLabel_Title.setText("");
		jLabel_Account.setText(Lang.getInstance().translate("Account") + ":")	;
		jLabel_Name.setText(Lang.getInstance().translate("Name") + ":");
		jLabel_Description.setText(Lang.getInstance().translate("Description") + ":");
    	jLabel_Gender.setText(Lang.getInstance().translate("Gender") + ":");
     	jLabel_Born.setText(Lang.getInstance().translate("Birthday") + ":");
    	jLabel_SkinColor.setText(Lang.getInstance().translate("Skin Color") + ":");
    	jLabel_EyeColor.setText(Lang.getInstance().translate("Eye Color") + ":");
    	jLabel_HairСolor.setText(Lang.getInstance().translate("Hair Сolor") + ":");
        jLabel_Height.setText(Lang.getInstance().translate("Height") + ":");
        jLabel_Fee.setText(Lang.getInstance().translate("Fee Power") + ":");
    	jLabel_BirthLongitude.setText(Lang.getInstance().translate("Birth Longitude") + ":");
    	jLabel_BirthLatitude.setText(Lang.getInstance().translate("Birth Latitude") + ":");
    	jLabel_Race.setText(Lang.getInstance().translate("Race") + ":");
    	jLabel_Dead.setText(Lang.getInstance().translate("Deathday") + ":");
    	
    }
	
	protected void addimage() {
		// TODO Auto-generated method stub
		
		
		// открыть диалог для файла
		JFileChooser chooser = new JFileChooser();
		
		
		 FileNameExtensionFilter filter = new FileNameExtensionFilter(
                 "Image", "png", "jpg");
		 chooser.setFileFilter(filter);
		 
	    int returnVal = chooser.showOpenDialog(getParent());
	    if(returnVal == JFileChooser.APPROVE_OPTION) {
	       System.out.println("You chose to open this file: " +
	            chooser.getSelectedFile().getName());
	    
	
	       
	       File file = new File(chooser.getSelectedFile().getPath());
// если размер больше 30к то не вставляем	       
	       if (file.length()>30000) {
	    	   
	    	   JOptionPane.showMessageDialog(null, Lang.getInstance().translate("File Large"), Lang.getInstance().translate("File Large"), JOptionPane.ERROR_MESSAGE);
	    	   
	    	   return;
	       }
	       
// его надо в базу вставлять
	        imgButes = null; 
			try {
				imgButes = getBytesFromFile(file);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        InputStream inputStream = new ByteArrayInputStream(imgButes);
	        try {
				BufferedImage image = ImageIO.read(inputStream);
				iconButton.setIcon(new ImageIcon(imgButes));
				
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        
	       
	       
	       
	    }
		
	
		
		
		
	}

	@SuppressWarnings("deprecation")
	public void onIssueClick()
	{
		//DISABLE
		this.issueButton.setEnabled(false);
	
		//CHECK IF NETWORK OK
		if(Controller.getInstance().getStatus() != Controller.STATUS_OK)
		{
			//NETWORK NOT OK
			JOptionPane.showMessageDialog(null, Lang.getInstance().translate("You are unable to send a transaction while synchronizing or while having no connections!"), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
			
			//ENABLE
			this.issueButton.setEnabled(true);
			
			return;
		}
		
		//CHECK IF WALLET UNLOCKED
		if(!Controller.getInstance().isWalletUnlocked())
		{
			//ASK FOR PASSWORD
			String password = PasswordPane.showUnlockWalletDialog(); 
			if(!Controller.getInstance().unlockWallet(password))
			{
				//WRONG PASSWORD
				JOptionPane.showMessageDialog(null, Lang.getInstance().translate("Invalid password"), Lang.getInstance().translate("Unlock Wallet"), JOptionPane.ERROR_MESSAGE);
				
				//ENABLE
				this.issueButton.setEnabled(true);
				
				return;
			}
		}
		
		//READ CREATOR
		Account sender = (Account) this.cbxFrom.getSelectedItem();

		int parse = 0;
		int feePow = 0;
		byte gender = 0;
		long birthday = 0;
		long deathday = 0;
		float birthLatitude = 0;
		float birthLongitude = 0;
		int height = 0;
		try
		{
			
			//READ FEE POW
			feePow = Integer.parseInt(this.txtFeePow.getText());
			
			String b = this.txtFeePow.getText();

			//READ GENDER
			parse++;
			gender = (byte) (this.txtGender.getSelectedIndex());
			
			parse++;
			  Date date = this.txtBirthday.getCalendar().getTime();
			String str = (date.getYear()+1900)+"-"+(date.getMonth()+1)+"-"+(date.getDate());
			if (str.length() < 11) str = str + " 00:00:00";
			birthday = Timestamp.valueOf(str).getTime();
try{
			parse++;
			date = this.txtDeathday.getCalendar().getTime();
			//str = this.txtDeathday.getDate().toString();
			str = (date.getYear()+1900)+"-"+(date.getMonth()+1)+"-"+(date.getDate());
			
				if (str.length() < 11) str = str + " 00:00:00";
				deathday = Timestamp.valueOf(str).getTime();
			
}
catch(Exception e3){
	deathday = birthday -1;
	
}
			parse++;
			birthLatitude = Float.parseFloat(this.txtBirthLatitude.getText());
			
			parse++;
			birthLongitude = Float.parseFloat(this.txtBirthLongitude.getText());

			parse++;
			height = Integer.parseInt(this.txtHeight.getText());
			
		}
		catch(Exception e)
		{
			String mess = "Invalid pars... " + parse;
			switch(parse)
			{
			case 0:
				mess = "Invalid fee power 0..6";
				break;
			case 1:
				mess = "Invalid gender";
				break;
			case 2:
				mess = "Invalid birthday [YYYY-MM-DD] or [YYYY-MM-DD hh:mm:ss]";
				break;
			case 3:
				mess = "Invalid deathday [YYYY-MM-DD] or [YYYY-MM-DD hh:mm:ss]";
				break;
			case 4:
				mess = "Invalid birth Latitude -180..180";
				break;
			case 5:
				mess = "Invalid birth Longitude -90..90";
				break;
			case 6:
				mess = "Invalid height 10..255 ";
				break;
			}
			JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate(e + mess), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
			
			this.issueButton.setEnabled(true);
			return;
		}
						
		//CREATE ASSET
		//PrivateKeyAccount creator, String fullName, int feePow, long birthday,
		//byte gender, String race, float birthLatitude, float birthLongitude,
		//String skinColor, String eyeColor, String hairСolor, int height, String description
		PrivateKeyAccount creator = Controller.getInstance().getPrivateKeyAccountByAddress(sender.getAddress());
		Pair<Transaction, Integer> result = Controller.getInstance().issuePerson(
				creator, this.txtName.getText(), feePow, birthday, deathday,
				gender, this.txtRace.getText(), birthLatitude, birthLongitude,
				this.txtSkinColor.getText(), this.txtEyeColor.getText(),
				this.txtHairСolor.getText(), height,
				null, this.imgButes, this.txtareaDescription.getText()
				);
		
		//CHECK VALIDATE MESSAGE
		if (result.getB() == Transaction.VALIDATE_OK) {
			JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Person issue has been sent!"), Lang.getInstance().translate("Success"), JOptionPane.INFORMATION_MESSAGE);
		//	this.dispose();
		
			
			
			txtFeePow.setText("0");
			txtName.setText("");
			txtareaDescription.setText("");
			//txtBirthday.setText("0000-00-00");
			//txtDeathday.setText("0000-00-00");
			
			txtGender.setSelectedIndex(2);
			txtRace.setText("");
			 txtBirthLatitude.setText("");
			 txtBirthLongitude.setText("");
			 txtSkinColor.setText("");
			 txtEyeColor.setText("");
			 txtHairСolor.setText("");
			 txtHeight.setText("");
			 iconButton.setText(Lang.getInstance().translate("Add Image..."));
			 imgButes = null;
			 iconButton.setIcon(null);
			
			
			
		
			
			
		} else {		
			JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate(OnDealClick.resultMess(result.getB())), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
		}
		
		//ENABLE
		this.issueButton.setEnabled(true);
	}

                             
    @SuppressWarnings({ "unchecked", "null" })
    // <editor-fold defaultstate="collapsed" desc="Generated Code">                          
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jLabel_Gender = new javax.swing.JLabel();
        jLabel_Born = new javax.swing.JLabel();
        jLabel_Dead = new javax.swing.JLabel();
        jLabel_Race = new javax.swing.JLabel();
        txtRace = new javax.swing.JTextField();
        jLabel_SkinColor = new javax.swing.JLabel();
        txtSkinColor = new javax.swing.JTextField();
        jLabel_EyeColor = new javax.swing.JLabel();
        txtEyeColor = new javax.swing.JTextField();
        jLabel_HairСolor = new javax.swing.JLabel();
        txtHairСolor = new javax.swing.JTextField();
        jLabel_Height = new javax.swing.JLabel();
        txtHeight = new javax.swing.JTextField();
        jLabel_BirthLatitude = new javax.swing.JLabel();
        txtBirthLatitude = new javax.swing.JTextField();
        jLabel_BirthLongitude = new javax.swing.JLabel();
        txtBirthLongitude = new javax.swing.JTextField();
        jLabel_Fee = new javax.swing.JLabel();
        txtFeePow = new javax.swing.JTextField();
        jPanel1 = new javax.swing.JPanel();
        iconButton = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        txtareaDescription = new javax.swing.JTextArea();
        txtName = new javax.swing.JTextField();
        jLabel_Name = new javax.swing.JLabel();
        jLabel_Account = new javax.swing.JLabel();
        jLabel_Description = new javax.swing.JLabel();
        cbxFrom = new javax.swing.JComboBox<>();
        jLabel9 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        issueButton = new javax.swing.JButton();
        jLabel_Title = new javax.swing.JLabel();
        txtGender = new javax.swing.JComboBox<>();
        txtBirthday =  new JDateChooser();
        txtDeathday = new JDateChooser();

        java.awt.GridBagLayout layout = new java.awt.GridBagLayout();
        layout.columnWidths = new int[] {0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0};
        layout.rowHeights = new int[] {0, 4, 0, 4, 0, 4, 0, 4, 0, 4, 0, 4, 0, 4, 0, 4, 0, 4, 0, 4, 0};
        setLayout(layout);

        jLabel_Gender.setText("Pol");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 18, 0, 0);
        add(jLabel_Gender, gridBagConstraints);

        jLabel_Born.setText("born");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 18, 0, 0);
        add(jLabel_Born, gridBagConstraints);

        jLabel_Dead.setText("Dead");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        add(jLabel_Dead, gridBagConstraints);

        jLabel_Race.setText("Race");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        add(jLabel_Race, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 8;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 0.2;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 16);
        add(txtRace, gridBagConstraints);

        jLabel_SkinColor.setText("SkinColor");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 18, 0, 0);
        add(jLabel_SkinColor, gridBagConstraints);

        txtSkinColor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
 //               txtSkinColorActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 0.2;
        add(txtSkinColor, gridBagConstraints);

        jLabel_EyeColor.setText("EyeColor");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        add(jLabel_EyeColor, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 8;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.gridwidth = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 0.2;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 16);
        add(txtEyeColor, gridBagConstraints);

        jLabel_HairСolor.setText("HairСolor");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 18, 0, 0);
        add(jLabel_HairСolor, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 0.2;
        add(txtHairСolor, gridBagConstraints);

        jLabel_Height.setText("Height");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        add(jLabel_Height, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 8;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.gridwidth = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 0.2;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 16);
        add(txtHeight, gridBagConstraints);

        jLabel_BirthLatitude.setText("BirthLatitude");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 14;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 18, 0, 0);
        add(jLabel_BirthLatitude, gridBagConstraints);

        txtBirthLatitude.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
   //             txtBirthLatitudeActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 14;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 0.2;
        add(txtBirthLatitude, gridBagConstraints);

        jLabel_BirthLongitude.setText("BirthLongitude");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 14;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        add(jLabel_BirthLongitude, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 8;
        gridBagConstraints.gridy = 14;
        gridBagConstraints.gridwidth = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 0.2;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 16);
        add(txtBirthLongitude, gridBagConstraints);

        jLabel_Fee.setText("Fee");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 16;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 18, 0, 0);
        add(jLabel_Fee, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 16;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 0.2;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 1);
        add(txtFeePow, gridBagConstraints);

        java.awt.GridBagLayout jPanel1Layout = new java.awt.GridBagLayout();
        jPanel1Layout.columnWidths = new int[] {0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0};
        jPanel1Layout.rowHeights = new int[] {0, 4, 0, 4, 0};
        jPanel1.setLayout(jPanel1Layout);

        iconButton.setText("Add image");
        iconButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
     //           iconButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 7;
        gridBagConstraints.gridheight = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 0.05;
        jPanel1.add(iconButton, gridBagConstraints);

        txtareaDescription.setColumns(20);
        txtareaDescription.setRows(5);
        jScrollPane1.setViewportView(txtareaDescription);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 12;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.2;
        jPanel1.add(jScrollPane1, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 12;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 1.1;
        jPanel1.add(txtName, gridBagConstraints);

        jLabel_Name.setText("Name");
        jLabel_Name.setToolTipText("");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 8;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        jPanel1.add(jLabel_Name, gridBagConstraints);

        jLabel_Account.setText("Account");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 8;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 0);
        jPanel1.add(jLabel_Account, gridBagConstraints);

        jLabel_Description.setText("Description");
        jLabel_Description.setToolTipText("");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 8;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        jPanel1.add(jLabel_Description, gridBagConstraints);

   //     cbxFrom.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 12;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 0.1;
        jPanel1.add(cbxFrom, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 15;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(10, 18, 0, 16);
        add(jPanel1, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 20;
        gridBagConstraints.gridwidth = 15;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.2;
        gridBagConstraints.weighty = 0.2;
        add(jLabel9, gridBagConstraints);

        jPanel2.setLayout(new java.awt.GridBagLayout());

        issueButton.setText("jButton1");
        jPanel2.add(issueButton, new java.awt.GridBagConstraints());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 18;
        gridBagConstraints.gridwidth = 15;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 16);
        add(jPanel2, gridBagConstraints);

        jLabel_Title.setText("jLabel8");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 13;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 0);
        add(jLabel_Title, gridBagConstraints);

        txtGender.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 0.1;
        add(txtGender, gridBagConstraints);

     /*     
        try {
			AccFormat = new MaskFormatter("****-**-**");
			AccFormat.setValidCharacters("1,2,3,4,5,6,7,8,9,0");
	     //   AccFormat.setPlaceholder("yyyy-mm-dd");
	        AccFormat.setPlaceholderCharacter('_');
	      //  AccFormat.setOverwriteMode(true);
	        
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
         
        JFormattedTextField txtBirthday = new JFormattedTextField(AccFormat);    
    */    
         
        txtBirthday.setDateFormatString("yyyy-MM-dd");
        
   //     txtBirthday.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.DateFormatter(new java.text.SimpleDateFormat("yyyy-MM-dd"))));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 0.2;
        add( txtBirthday, gridBagConstraints);

  //      txtDeathday.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.DateFormatter(new java.text.SimpleDateFormat("yyyy-MM-dd"))));
  //      txtDeathday.addActionListener(new java.awt.event.ActionListener() {
 //           public void actionPerformed(java.awt.event.ActionEvent evt) {
 //               txtDeathdayActionPerformed(evt);
 //           }
 //       });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 8;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.2;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 16);
        add(txtDeathday, gridBagConstraints);
    }// </editor-fold>                        

}
// Фильтр выбора файлов определенного типа
class FileFilterExt extends javax.swing.filechooser.FileFilter 
{
	String extension  ;  // расширение файла
	String description;  // описание типа файлов

	FileFilterExt(String extension, String descr)
	{
		this.extension = extension;
		this.description = descr;
	}
	@Override
	public boolean accept(java.io.File file)
	{
		if(file != null) {
			if (file.isDirectory())
				return true;
			if( extension == null )
				return (extension.length() == 0);
			return file.getName().endsWith(extension);
		}
		return false;
	}
	// Функция описания типов файлов
	@Override
	public String getDescription() {
		return description;
	}
	
	
	
	
	
}

