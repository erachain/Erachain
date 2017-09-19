package gui.library;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

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
import utils.Compressor_ZIP;
import utils.GZIP;
import utils.TableMenuPopupUtil;
import utils.ZIP_File;
import utils.Zip_Bytes;

import org.apache.commons.net.util.Base64;
public class M_Attached_Files_Panel extends JPanel{

	private Attache_Files_Model model;
	private MTable table;
	private JScrollPane scrollPane;

	public M_Attached_Files_Panel() {
		
		setLayout(new java.awt.GridBagLayout());
		model = new Attache_Files_Model();
		table = new MTable(model);
		JPopupMenu menu = new JPopupMenu();
		java.awt.GridBagConstraints gridBagConstraints;

		
    	
    	JMenuItem vsend_Coins_Item= new JMenuItem(Lang.getInstance().translate("Save File"));
    
    	vsend_Coins_Item.addActionListener(new ActionListener(){
  		@Override
    	public void actionPerformed(ActionEvent e) {
  			
  			if (table.getSelectedRow() < 0 ) return;
  				int row = table.convertRowIndexToModel(table.getSelectedRow());
  			My_JFileChooser chooser = new My_JFileChooser();
  			String str = (String) model.getValueAt(row, 0);
  			chooser.setDialogTitle(Lang.getInstance().translate("Save File")+": " + str );
  			//chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
  			chooser.setDialogType(javax.swing.JFileChooser.SAVE_DIALOG);
  			chooser.setMultiSelectionEnabled(false);
  			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
  			//chooser.setAcceptAllFileFilterUsed(false); 
  			
  			 if ( chooser.showSaveDialog(getParent()) == JFileChooser.APPROVE_OPTION ) {
  	          
  				String pp = chooser.getSelectedFile().getPath();
  				 try(FileOutputStream fos=new FileOutputStream(pp + File.separatorChar +  str))
  		        {
  		            // перевод строки в байты
  					String ssst = model.getValueAt(row, 2).toString();
  		            byte[] buffer =(byte[]) model.getValueAt(row, 2);
  		            // if ZIP
  		           if ((boolean)model.getValueAt(row, 1)){
  		         		byte[] buffer1 = null;
							try {
								buffer1 = Zip_Bytes.decompress(buffer);
							} catch (DataFormatException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
							fos.write(buffer1, 0, buffer1.length);
  		           	}
  		          else {
  		            fos.write(buffer, 0, buffer.length);
  		           }
  		        
  		        }
  		        catch(IOException ex){
  		             
  		            System.out.println(ex.getMessage());
  		        } 
  	           
  	        }
  			
  			
  			
  			
			}});
    	
    	menu.add(vsend_Coins_Item);
    	
    	TableMenuPopupUtil.installContextMenu(table, menu);
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		scrollPane = new JScrollPane();
		scrollPane.setViewportView(table);
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.weightx = 0.1;
		gridBagConstraints.weighty = 0.1;
		
		add(scrollPane,gridBagConstraints);
		
		// TODO Auto-generated constructor stub
	}
	public void insert_Row (String name,boolean zip, byte[] data){
		model.addRow(new Object[] {name, zip,  data});
		model.fireTableDataChanged();
	
	}
	
	private byte[] zip_un(byte[] compressedData) throws  Exception{
		//    byte[] compressedData = null;
		    Inflater decompressor = new Inflater();
		    decompressor.setInput(compressedData);
		    ByteArrayOutputStream bos = new ByteArrayOutputStream(compressedData.length);
		    byte[] buf = null;
		    while (!decompressor.finished()) {
		      int count = decompressor.inflate(buf);
		      bos.write(buf, 0, count);
		    }
		 //   bos.close();
		   return bos.toByteArray();
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
class MyByteArrayDecompress {
	 
public byte[] decompressByteArray(byte[] bytes){
         
        ByteArrayOutputStream baos = null;
        Inflater iflr = new Inflater();
        iflr.setInput(bytes);
        baos = new ByteArrayOutputStream();
        byte[] tmp = new byte[4*1024];
        try{
            while(!iflr.finished()){
                int size = iflr.inflate(tmp);
                baos.write(tmp, 0, size);
            }
        } catch (Exception ex){
             
        } finally {
            try{
                if(baos != null) baos.close();
            } catch(Exception ex){}
        }
         
        return baos.toByteArray();
    }
}
