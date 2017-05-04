package gui.library;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.validation.constraints.Null;


import core.item.persons.PersonCls;
import gui.items.accounts.Account_Send_Dialog;
import gui.items.mails.Mail_Send_Dialog;
import lang.Lang;
import utils.TableMenuPopupUtil;
import org.apache.commons.net.util.Base64;
public class M_Attached_Files_Panel extends JPanel{

	private Attache_Files_Model model;
	private MTable table;
	private JScrollPane scrollPane;

	public M_Attached_Files_Panel() {
		model = new Attache_Files_Model();
		table = new MTable(model);
		JPopupMenu menu = new JPopupMenu();

		
    	
    	JMenuItem vsend_Coins_Item= new JMenuItem(Lang.getInstance().translate("Save File"));
    
    	vsend_Coins_Item.addActionListener(new ActionListener(){
  		@Override
    	public void actionPerformed(ActionEvent e) {
  			
  			if (table.getSelectedRow() < 0 ) return;
  				int row = table.convertRowIndexToModel(table.getSelectedRow());
  			My_JFileChooser chooser = new My_JFileChooser();
  			chooser.setDialogTitle(Lang.getInstance().translate("Save File"));
  			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
  			chooser.setMultiSelectionEnabled(false);
  	//		 if ( chooser.showSaveDialog(getParent()) == JFileChooser.APPROVE_OPTION ) {
  	          
  				 try(FileOutputStream fos=new FileOutputStream("d://1" + model.getValueAt(row, 0)))
  		        {
  		            // перевод строки в байты
  					String ssst = model.getValueAt(row, 2).toString();
  		            byte[] buffer = Base64.decodeBase64( model.getValueAt(row, 2).toString());
  		             
  		            fos.write(buffer, 0, buffer.length);
  		        }
  		        catch(IOException ex){
  		             
  		            System.out.println(ex.getMessage());
  		        } 
  	           
  	   //     }
  			
  			
  			
  			
			}});
    	
    	menu.add(vsend_Coins_Item);
    	
    	TableMenuPopupUtil.installContextMenu(table, menu);
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		scrollPane = new JScrollPane();
		scrollPane.setViewportView(table);
		add(scrollPane);
		
		// TODO Auto-generated constructor stub
	}
	public void insert_Row (String name,boolean zip, String data){
		model.addRow(new Object[] {name, zip, data});
		model.fireTableDataChanged();
	
	}


}

class Attache_Files_Model extends DefaultTableModel{
    
	public Attache_Files_Model()
    {
      super(new Object[] {Lang.getInstance().translate("Name"),"ZIP?","data"}, 0);
   
    }
    
   public int getColumnCount(){
	return 3;
   }
	
	@Override
    public boolean isCellEditable(int row, int column)
    {
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
