package gui.library;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.HashMap;
import java.util.Set;

import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.event.HyperlinkEvent.EventType;
import javax.swing.table.DefaultTableModel;
import javax.validation.constraints.Null;

import com.github.rjeschke.txtmark.Processor;

import core.item.notes.NoteCls;
import gui.items.notes.ComboBoxModelItemsNotes;
import lang.Lang;

public class M_Fill_Template_Panel extends javax.swing.JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Params_Template_Model params_Template_Model;
	public JComboBox<NoteCls> jComboBox_Template;
	protected NoteCls sel_note;
	
	
	
	public M_Fill_Template_Panel() {
		jTextPane_Message_Public = new MImprintEDIT_Pane();
    	jTextPane_Message_Public.addHyperlinkListener(new HyperlinkListener(){

			@Override
			public void hyperlinkUpdate(HyperlinkEvent arg0) {
				// TODO Auto-generated method stub
				EventType i = arg0.getEventType();
				 if (arg0.getEventType() != HyperlinkEvent.EventType.ACTIVATED) return;
				 String str = JOptionPane.showInputDialog(jTextPane_Message_Public.th, Lang.getInstance().translate("Insert") + " "+arg0.getDescription(), jTextPane_Message_Public.pars.get("{{"+ arg0.getDescription()+"}}"));
				 if (str==null || str.equals("")) return;
				 jTextPane_Message_Public.pars.replace("{{"+ arg0.getDescription()+"}}", str);
				 jTextPane_Message_Public.setText(jTextPane_Message_Public.init_String(jTextPane_Message_Public.text, false));
				 for ( int i1=0; i1 < params_Template_Model.getRowCount(); i1++){
					 if (arg0.getDescription().equals(params_Template_Model.getValueAt(i1, 0))) params_Template_Model.setValueAt(str, i1, 1);
					 
    		
    		
    	}
			
			}
			
			
			
			
		});
		
		
		initComponents();

		 jComboBox_Template.addItemListener(new ItemListener(){

				@Override
				public void itemStateChanged(ItemEvent e) {
					// TODO Auto-generated method stub
				
					
					if(e.getStateChange() == ItemEvent.SELECTED) 
					{		
						sel_note = (NoteCls) jComboBox_Template.getSelectedItem();
						String ww = Processor.process(sel_note.getDescription().replace("\n\n", "\n").replace("\n", "  \n"));
						
						int ee = params_Template_Model.getRowCount()-1;
						int ccc;
						for (ccc = params_Template_Model.getRowCount()-1; ccc>=0; ccc--){
							params_Template_Model.removeRow(ccc);
							
						}
						jTextPane_Message_Public.pars.clear();
						jTextPane_Message_Public.set_Text(ww);
						HashMap<String, String> ss = jTextPane_Message_Public.get_Params();
						Set<String> sk = ss.keySet();
						
						for (String s:sk){
							ss.get(s);
							params_Template_Model.addRow(new Object[] { s, ss.get(s)});
									
						}
						
						
					} 	
					
					
				}
			});	
		
	
	}
	public HashMap<String, String> get_Params(){
	
	return jTextPane_Message_Public.get_Params();	
		
	}
	
	public NoteCls get_TemplateCls(){
		return (NoteCls) jComboBox_Template.getSelectedItem();
		
	}

	@SuppressWarnings("unchecked")
	// <editor-fold defaultstate="collapsed" desc="Generated Code">
	private void initComponents() {
		java.awt.GridBagConstraints gridBagConstraints;

		jLabel_Template1 = new javax.swing.JLabel();
		jComboBox_Template = new JComboBox<NoteCls>(new ComboBoxModelItemsNotes());
		jCheckBox_Is_Text = new javax.swing.JCheckBox();
		jCheckBox_Is_Encripted = new javax.swing.JCheckBox();
		sp_pan = new javax.swing.JSplitPane();
		jScrollPane_Message_Public_TextPane = new javax.swing.JScrollPane();
		jScrollPane_Params_Template_Public_TextPane = new javax.swing.JScrollPane();
		
		 params_Template_Model = new Params_Template_Model();
	        jTable_Params_Message_Public = new MTable(params_Template_Model);
	        params_Template_Model.addTableModelListener(new TableModelListener(){

				@Override
				public void tableChanged(TableModelEvent arg0) {
					// TODO Auto-generated method stub
		
				if (arg0.getType() != 0 && arg0.getColumn()<0 ) return;
				System.out.print("\n row = " + arg0.getFirstRow() + "  Col="+ arg0.getColumn() + "   type =" + arg0.getType());
				String dd = params_Template_Model.getValueAt(arg0.getFirstRow(),  arg0.getColumn()).toString();
				System.out.print("\n key:"+ params_Template_Model.getValueAt(arg0.getFirstRow(),  0) +" value:" + params_Template_Model.getValueAt(arg0.getFirstRow(),  arg0.getColumn()));
				
				 jTextPane_Message_Public.pars.replace("{{"+ params_Template_Model.getValueAt(arg0.getFirstRow(),  0) +"}}",(String) params_Template_Model.getValueAt(arg0.getFirstRow(),  arg0.getColumn()));
				 System.out.print("\n" + get_TemplateCls().getName() + "\n");
					System.out.print(get_Params());
				 jTextPane_Message_Public.setText(jTextPane_Message_Public.init_String(jTextPane_Message_Public.text, false));
				arg0=arg0;
				}});
	        
	        
	        
	        
		jLabel1 = new javax.swing.JLabel();

		setLayout(new java.awt.GridBagLayout());
		jLabel_Template1.setText(Lang.getInstance().translate("Select Template") + ":");
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
		gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 0);
		add(jLabel_Template1, gridBagConstraints);

		
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.gridwidth = 5;
		gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
		gridBagConstraints.weightx = 0.1;
		gridBagConstraints.insets = new java.awt.Insets(8, 0, 8, 0);
		add(jComboBox_Template, gridBagConstraints);

		jCheckBox_Is_Text.setText("jCheckBox1");
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 2;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
		gridBagConstraints.insets = new java.awt.Insets(0, 8, 8, 0);
	//	add(jCheckBox_Is_Text, gridBagConstraints);

		jCheckBox_Is_Encripted.setText(Lang.getInstance().translate("Encrypt Message"));
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 2;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
		gridBagConstraints.insets = new java.awt.Insets(0, 0, 8, 0);
		add(jCheckBox_Is_Encripted, gridBagConstraints);

		jScrollPane_Message_Public_TextPane.setViewportView(jTextPane_Message_Public);

		sp_pan.setLeftComponent(jScrollPane_Message_Public_TextPane);
		jScrollPane_Params_Template_Public_TextPane.setViewportView(jTable_Params_Message_Public);

		sp_pan.setRightComponent(jScrollPane_Params_Template_Public_TextPane);

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.gridwidth = 7;
		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
		gridBagConstraints.weightx = 0.2;
		gridBagConstraints.weighty = 0.2;
		gridBagConstraints.insets = new java.awt.Insets(0, 8, 8, 8);
		add(sp_pan, gridBagConstraints);

		jLabel1.setText("                   ");
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.insets = new java.awt.Insets(8, 0, 8, 8);
		add(jLabel1, gridBagConstraints);
	}// </editor-fold>

	// Variables declaration - do not modify
	private javax.swing.JCheckBox jCheckBox_Is_Encripted;
	private javax.swing.JCheckBox jCheckBox_Is_Text;
	private javax.swing.JLabel jLabel1;
	private javax.swing.JLabel jLabel_Template1;
	private javax.swing.JScrollPane jScrollPane_Message_Public_TextPane;
	private javax.swing.JScrollPane jScrollPane_Params_Template_Public_TextPane;
	private MTable jTable_Params_Message_Public;
	public javax.swing.JSplitPane sp_pan;
	private MImprintEDIT_Pane jTextPane_Message_Public;
	// End of variables declaration

	 class Params_Template_Model extends DefaultTableModel{
	        
	    	/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public Params_Template_Model()
	        {
	          super(new Object[] {Lang.getInstance().translate("Name"),Lang.getInstance().translate("=")}, 0);
	       
	        }
	        
	       public int getColumnCount(){
	    	return 2;
	       }
	    	
	    	@Override
	        public boolean isCellEditable(int row, int column)
	        {
	           if (column ==1) return true;
	    		return new Boolean(null);
	        } 
	        public Class<? extends Object> getColumnClass(int c) {     // set column type
	    		Object o = getValueAt(0, c);
	    		return o==null?Null.class:o.getClass();
	    	   }
	    	
	        public Object getValueAt(int row, int col){
	        	
	        	
	        	if (this.getRowCount()<row || this.getRowCount() ==0 || col <0 || row <0)return null;
	    	return super.getValueAt(row, col);
	        	
	        	
	        }
	   

}
}
