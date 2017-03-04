package gui.items.persons;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.TreeMap;

import javax.swing.ImageIcon;
import javax.swing.JDialog;

import org.mapdb.Fun.Tuple3;

import controller.Controller;
import core.item.persons.PersonCls;
import core.transaction.Transaction;
import database.DBSet;
import gui.RunMenu;
import gui.items.accounts.Account_Send_Dialog;
import gui.items.mails.Mail_Send_Dialog;
import gui.library.MButton;
import gui.records.VouchRecordDialog;
import lang.Lang;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Саша
 */
public class Person_Work_Dialog extends JDialog {

    /**
     * Creates new form Person_Work_Dialog
     */
    public Person_Work_Dialog(PersonCls person) {
        super();
        this.setModal(true);
        getContentPane().setLayout(new java.awt.GridLayout(0, 1));
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
      //  setAlwaysOnTop(true);
        List<Image> icons = new ArrayList<Image>();
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon16.png"));
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon32.png"));
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon64.png"));
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon128.png"));
		this.setIconImages(icons);
		
		
	       
		
		
      /*  initComponents();
       
     
        ImageIcon image = new ImageIcon(person.getImage());
      		int x = image.getIconWidth();
      		int y = image.getIconHeight();

      		int x1 = 250;
      		double k = ((double) x / (double) x1);
      		y = (int) ((double) y / k);
      		

      		if (y != 0) {
      			Image Im = image.getImage().getScaledInstance(x1, y, 1);

      			jLabel_Img.setIcon(new ImageIcon(Im));
              
              
             
              
      		}
        
      		*/
      	   
      	  jButton1 = new MButton(Lang.getInstance().translate("Set Status"),3);
      	   // 	aaa.jButton1.setBorderPainted(false);
      	  //  	Search_run_menu.jButton1.setFocusPainted(true);
      	 //  	Search_run_menu.jButton1.setFocusCycleRoot(true);
//      		Search_run_menu.jButton1.setContentAreaFilled(false);
//      		Search_run_menu.jButton1.setOpaque(false);
//      			Search_run_menu.jButton1.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
      	getContentPane().add(jButton1);
      	    	jButton1.addActionListener(new ActionListener(){
      	  		@Override
      	    	public void actionPerformed(ActionEvent e) {
      	   
      	  		  	@SuppressWarnings("unused")
      				PersonSetStatusDialog fm = new PersonSetStatusDialog( person);	
      	    	}});
      	    	   	
      	    	jButton2= new MButton(Lang.getInstance().translate("Attest Public Key"),3);
      	  //  	jButton2.setText(Lang.getInstance().translate("Attest Public Key"));
      	  //  	Search_run_menu.jButton2.setContentAreaFilled(false);
      	  //  	Search_run_menu.jButton2.setOpaque(false);
      	    	getContentPane().add(jButton2);
      	    	jButton2.addActionListener(new ActionListener(){
      	  		@Override
      	    	public void actionPerformed(ActionEvent e) {
      	   
      	  
      	    		@SuppressWarnings("unused")
      				PersonConfirmDialog fm = new PersonConfirmDialog(person);		
      	    		}});
      	    	
      	    	javax.swing.JButton jButton_Vouh = new javax.swing.JButton();
      	 
      	    	jButton3 = new MButton(Lang.getInstance().translate("Vouch"),3);
      	    //	jButton_Vouh.setContentAreaFilled(false);
      	   // 	jButton_Vouh.setOpaque(false);
      	    	getContentPane().add(jButton3);
      	    	jButton3.addActionListener(new ActionListener(){
      	  		@Override
      	    	public void actionPerformed(ActionEvent e) {
      	   
      	  
      	    		
      				PersonCls per = person;
      				byte[] ref = per.getReference();
      				Transaction transaction = Transaction.findByDBRef(DBSet.getInstance(), ref);
      				int blockNo = transaction.getBlockHeight(DBSet.getInstance());
      				int recNo = transaction.getSeqNo(DBSet.getInstance());
      	    		new VouchRecordDialog(blockNo, recNo);	
      	  		
      	  		}});
      	    	
      		

      	    	jButton4 =new MButton(Lang.getInstance().translate("Send Coins"),3);
      	    //	Search_run_menu.jButton6.setContentAreaFilled(false);
      	    //	Search_run_menu.jButton6.setOpaque(false);
      	    	getContentPane().add(jButton4);
      	    	jButton4.addActionListener(new ActionListener(){
      	  		@Override
      	    	public void actionPerformed(ActionEvent e) {
      	  			
      				TreeMap<String, Stack<Tuple3<Integer, Integer, Integer>>> addresses = DBSet.getInstance().getPersonAddressMap().getItems(person.getKey());
      				if (addresses.isEmpty()) {
      					
      				} else {
      					Account_Send_Dialog fm = new Account_Send_Dialog(null,null,null, person);				
      				}
      	  			
      	  
      	    //		@SuppressWarnings("unused")
      		//		PersonConfirmDialog fm = new PersonConfirmDialog(search_Table_Model.getPerson(search_Table.convertRowIndexToModel(search_Table.getSelectedRow())));		
      	    		}});
      	    	
      	    	
      	      	jButton5 = new MButton(Lang.getInstance().translate("Send Mail"),3);
      	   // 	Search_run_menu.jButton5.setContentAreaFilled(false);
      	   // 	Search_run_menu.jButton5.setOpaque(false);
      	    	getContentPane().add(jButton5);
      	    	jButton5.addActionListener(new ActionListener(){
      	  		@Override
      	    	public void actionPerformed(ActionEvent e) {
      	   
      	  
      	  		
      				TreeMap<String, Stack<Tuple3<Integer, Integer, Integer>>> addresses = DBSet.getInstance().getPersonAddressMap().getItems(person.getKey());
      				if (addresses.isEmpty()) {
      					
      				} else {
      					Mail_Send_Dialog fm = new Mail_Send_Dialog(null,null,null, person);
      				}
      	  			
      	  			
      	  			
      	  			
      	  			
      	    //		@SuppressWarnings("unused")
      		//		PersonConfirmDialog fm = new PersonConfirmDialog(search_Table_Model.getPerson(search_Table.convertRowIndexToModel(search_Table.getSelectedRow())));		
      	    		}});
      	    	
      	    	
      	    	
      		
      		
      		
      		
      		
      		
      		
      		
      		
      		
      		
      		
      		
      		
      		
      		
      		
      		
      		
      		
      		
        
        
        
        pack();
        this.setLocationRelativeTo(null);
        this.setVisible(false);
    }

   
               
    private MButton jButton1;
    private MButton jButton2;
    private MButton jButton3;
    private MButton jButton4;
    private MButton jButton5;
  
    // End of variables declaration                   
}
