package gui.library;
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.math.BigDecimal;

import javax.swing.RowFilter;
import javax.swing.RowFilter.ComparisonType;
import javax.swing.table.TableColumn;
import javax.validation.constraints.Null;

import org.mapdb.Fun.Tuple4;

import lang.Lang;

/**
 *
 * @author Саша
 */
public class MTable_search_Num_Dialog extends javax.swing.JDialog {

    /**
     * Creates new form MTable_search_Num_Dialog
     * @param class1 
     * @param column 
     * @param Second_IF_Txt 
     * @param Second_IF_Com 
     * @param first_IF_Txt 
     * @param firsf_IF_Com 
     */
	
	private Tuple4<ComparisonType,String,ComparisonType,String> ansver;
	Class class_1;
	private ComparisonType old_First_If_Comp;
	private ComparisonType old_Secind_If_Comp;
	private String old_First_If_Txt;
	private String old_Second_If_Txt;
	
	
	
    public MTable_search_Num_Dialog(TableColumn column, Class class1, ComparisonType first_IF_Com, String first_IF_Txt, ComparisonType second_IF_Com, String second_IF_Txt) {
        super();
        
        old_First_If_Comp = first_IF_Com;
        old_Secind_If_Comp = second_IF_Com;
        old_First_If_Txt = first_IF_Txt;
        old_Second_If_Txt = second_IF_Txt;
        
        setTitle(Lang.getInstance().translate("Filter"));
       setModal(true);
        class_1 = class1;
        String ss="";
        initComponents();
        jComboBox_Second_IF.setVisible(false);
        jFormattedTextField_Second_IF.setVisible(false);
        jFormattedTextField_Second_IF.setText("");
        jFormattedTextField_First_IF.setText("");
        
        
        // сделать начальные установки для полей что бы бралось из предыдущей
        if (first_IF_Txt!="") jFormattedTextField_First_IF.setText(first_IF_Txt);
        if (first_IF_Com == null) first_IF_Com = RowFilter.ComparisonType.EQUAL;
        if (second_IF_Com == null) second_IF_Com = RowFilter.ComparisonType.EQUAL;
        if (second_IF_Txt!= ""){
        	jComboBox_Second_IF.setVisible(true);
            jFormattedTextField_Second_IF.setVisible(true);	
            jFormattedTextField_Second_IF.setText(second_IF_Txt);
        }
        
        if(class_1 ==Integer.class || class_1 == Long.class){
        	 jFormattedTextField_First_IF.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));
        	 jFormattedTextField_Second_IF.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));
        	
        }
        
        if( class_1 == BigDecimal.class){
        	  jFormattedTextField_First_IF.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("###0.00000000"))));
        	  jFormattedTextField_Second_IF.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("###0.00000000"))));
       	
       }
        
        ComparisonType s = first_IF_Com;
        String s1 = s.getDeclaringClass().toString();
        String s2 = first_IF_Com.name();
        int ind=0; 
        for (int k = 0;k<jComboBox_First_IF.getItemCount(); k++){
        	Item iI = jComboBox_First_IF.getItemAt(k);
        	
			if(iI.id == first_IF_Com )ind = k;
        }
        jComboBox_First_IF.setSelectedIndex(ind);
        jFormattedTextField_First_IF.setText(first_IF_Txt);
        
        ind =0;
        for (int k = 0;k<jComboBox_Second_IF.getItemCount(); k++){
        	Item iI = jComboBox_Second_IF.getItemAt(k);
        	
			if(iI.id == first_IF_Com )ind = k;
        }
        jComboBox_Second_IF.setSelectedIndex(ind);
        jFormattedTextField_Second_IF.setText(second_IF_Txt);
        
        jLabel_Title.setText(Lang.getInstance().translate("Filter column")+": "+ column.getHeaderValue().toString());
        jButton_Close.setText(Lang.getInstance().translate("Cancel"));
        jButton_Close.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				cansel_1();
				dispose();
			}
        	});
        
        jButton_Erase.setText(Lang.getInstance().translate("Erase"));
        jButton_Erase.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				 jComboBox_Second_IF.setVisible(false);
				 jFormattedTextField_Second_IF.setVisible(false);
				 jFormattedTextField_Second_IF.setText("");
			     jFormattedTextField_First_IF.setText("");
			     jComboBox_First_IF.setSelectedIndex(0);
			     jComboBox_Second_IF.setSelectedIndex(0);
			    
			     
			}
        	});
        jButton_OK.setText(Lang.getInstance().translate("OK"));
        jButton_OK.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				String first = jFormattedTextField_First_IF.getText();
				String second = jFormattedTextField_Second_IF.getText();
				 
				String fI= (first.equals(""))? null:new String(jFormattedTextField_First_IF.getText().replace(",", "."));
				String sI = (second.equals(""))?null:new String(jFormattedTextField_Second_IF.getText().replace(",", "."));
				ComparisonType fF = ((Item)jComboBox_First_IF.getSelectedItem()).getId();
				ComparisonType sF = ((Item)jComboBox_Second_IF.getSelectedItem()).getId();
				 ansver = new Tuple4(fF,fI,sF,sI);
								
				dispose();
			}
        	});
        
        this.addWindowListener(new WindowListener(){

			@Override
			public void windowActivated(WindowEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void windowClosed(WindowEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void windowClosing(WindowEvent arg0) {
				// TODO Auto-generated method stub
				cansel_1();
			}

			@Override
			public void windowDeactivated(WindowEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void windowDeiconified(WindowEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void windowIconified(WindowEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void windowOpened(WindowEvent arg0) {
				// TODO Auto-generated method stub
				
			}
        	
        	
        	
        	
        });
        
        this.setLocationRelativeTo(null);
      //  setVisible(true);
     //   setModal(true);
        
        
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

        jComboBox_First_IF = new javax.swing.JComboBox<>();
        jComboBox_Second_IF = new javax.swing.JComboBox<>();
        jFormattedTextField_First_IF = new javax.swing.JFormattedTextField();
        jFormattedTextField_Second_IF = new javax.swing.JFormattedTextField();
        jButton_OK = new javax.swing.JButton();
        jButton_Close = new javax.swing.JButton();
        jButton_Erase = new javax.swing.JButton();
        jLabel_Title = new javax.swing.JLabel();
        javax.swing.JLabel jLabel_Bootom = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        getContentPane().setLayout(new java.awt.GridBagLayout());
        
             
     //   jComboBox_First_IF.setModel(new javax.swing.DefaultComboBoxModel<ComparisonType,String>(Lang.getInstance().translate(new String[] { "EQUAL", "BEFORE", "AFTER", "NOT_EQUAL" })));
        jComboBox_First_IF.addItem(new Item(RowFilter.ComparisonType.EQUAL, Lang.getInstance().translate("EQUAL") ) );
        jComboBox_First_IF.addItem(new Item(RowFilter.ComparisonType.AFTER, Lang.getInstance().translate("AFTER") ) );
        jComboBox_First_IF.addItem(new Item(RowFilter.ComparisonType.BEFORE, Lang.getInstance().translate("BEFORE") ) );
        jComboBox_First_IF.addItem(new Item(RowFilter.ComparisonType.NOT_EQUAL, Lang.getInstance().translate("NOT EQUAL" )) );
        jComboBox_First_IF.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
								
				if (((Item)jComboBox_First_IF.getSelectedItem()).description==new Item(RowFilter.ComparisonType.AFTER, Lang.getInstance().translate("AFTER") ).description){
							
						jComboBox_Second_IF.removeAllItems();
						jComboBox_Second_IF.addItem(new Item(RowFilter.ComparisonType.BEFORE,Lang.getInstance().translate("BEFORE") ) );
						jComboBox_Second_IF.setSelectedItem(RowFilter.ComparisonType.BEFORE);
						jComboBox_Second_IF.setVisible(true);
						jFormattedTextField_Second_IF.setVisible(true);			
				}else{
					jComboBox_Second_IF.setVisible(false);
					jFormattedTextField_Second_IF.setVisible(false);	
					
				}
        	
			}
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(0, 8, 0, 8);
        getContentPane().add(jComboBox_First_IF, gridBagConstraints);

  //      jComboBox_Second_IF.setModel(new javax.swing.DefaultComboBoxModel<>(Lang.getInstance().translate(new String[] { "BEFORE", "AFTER"})));
        
        jComboBox_Second_IF.addItem(new Item(RowFilter.ComparisonType.BEFORE, "BEFORE" ) );
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 8);
        getContentPane().add(jComboBox_Second_IF, gridBagConstraints);

      
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 8);
        getContentPane().add(jFormattedTextField_First_IF, gridBagConstraints);

      //  jFormattedTextField_Second_IF.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(8, 0, 8, 8);
        getContentPane().add(jFormattedTextField_Second_IF, gridBagConstraints);

        jButton_OK.setText("jButton1");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(9, 0, 0, 0);
        getContentPane().add(jButton_OK, gridBagConstraints);

        jButton_Close.setText("jButton2");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(9, 8, 0, 8);
        getContentPane().add(jButton_Close, gridBagConstraints);

        jButton_Erase.setText("jButton3");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(9, 8, 0, 10);
        getContentPane().add(jButton_Erase, gridBagConstraints);

        jLabel_Title.setText("jLabel1");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(9, 8, 9, 8);
        getContentPane().add(jLabel_Title, gridBagConstraints);

        jLabel_Bootom.setText("");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 0.3;
        gridBagConstraints.insets = new java.awt.Insets(7, 8, 7, 8);
        getContentPane().add(jLabel_Bootom, gridBagConstraints);

        pack();
    }// </editor-fold>       
    
    public Tuple4<ComparisonType, String, ComparisonType, String> get_Ansver(){
    	return ansver;
    }
    
    private void cansel_1(){
    	// TODO Auto-generated method stub
		String fI= (old_First_If_Txt.equals(""))? null:new String(old_First_If_Txt.replace(",", "."));
		String sI = (old_Second_If_Txt.equals(""))?null:new String(old_Second_If_Txt.replace(",", "."));
		 ansver = new Tuple4(old_First_If_Comp,fI,old_Secind_If_Comp,sI);
    	
    	
    }
   

    // Variables declaration - do not modify                     
    private javax.swing.JButton jButton_Close;
    private javax.swing.JButton jButton_Erase;
    private javax.swing.JButton jButton_OK;
    private javax.swing.JComboBox<Item> jComboBox_First_IF;
    private javax.swing.JComboBox<Item> jComboBox_Second_IF;
    private javax.swing.JFormattedTextField jFormattedTextField_First_IF;
    private javax.swing.JFormattedTextField jFormattedTextField_Second_IF;
    private javax.swing.JLabel jLabel_Title;
    // End of variables declaration                   

    class Item
    {
        private ComparisonType id;
        private String description;

        public Item(ComparisonType id, String description)
        {
            this.id = id;
            this.description = description;
        }

        public ComparisonType getId()
        {
            return id;
        }

        public String getDescription()
        {
            return description;
        }

        public String toString()
        {
            return description;
        }
    }





}

