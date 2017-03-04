package gui.library;

import java.awt.GridBagConstraints;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableColumn;

import core.item.persons.PersonCls;
import gui.models.PersonStatusesModel;
import gui.models.Renderer_Left;
import lang.Lang;

public class Statuses_Library_Panel extends JPanel {



/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
private MTable jTable_Statuses;
private JScrollPane jScrollPane_Tab_Status;
private GridBagConstraints gridBagConstraints;

public Statuses_Library_Panel(PersonCls person){

	
    this.setName(Lang.getInstance().translate("Statuses"));
    this.setLayout(new java.awt.GridBagLayout());


	   PersonStatusesModel statusModel = new PersonStatusesModel (person.getKey());
       jTable_Statuses = new MTable(statusModel);
       
       jTable_Statuses.setDefaultRenderer(String.class, new Renderer_Left(jTable_Statuses.getFontMetrics(jTable_Statuses.getFont()),statusModel.get_Column_AutoHeight())); // set renderer
       //CHECKBOX FOR FAVORITE
       		TableColumn to_Date_Column1 = jTable_Statuses.getColumnModel().getColumn( PersonStatusesModel.COLUMN_PERIOD);	
       		//favoriteColumn.setCellRenderer(new Renderer_Boolean()); //personsTable.getDefaultRenderer(Boolean.class));
       		to_Date_Column1.setMinWidth(80);
       		to_Date_Column1.setMaxWidth(200);
       		to_Date_Column1.setPreferredWidth(120);//.setWidth(30);
      
       		TableColumn Date_Column = jTable_Statuses.getColumnModel().getColumn( PersonStatusesModel.COLUMN_MAKE_DATA);	
       		//favoriteColumn.setCellRenderer(new Renderer_Boolean()); //personsTable.getDefaultRenderer(Boolean.class));
       		int rr = (int) (getFontMetrics(getFont()).stringWidth("22-22-2222"));	
       		Date_Column.setMinWidth(rr+1);
       		Date_Column.setMaxWidth(rr+20);
       		Date_Column.setPreferredWidth(rr+5);//.setWidth(30);
      
       		
       		
    
       jScrollPane_Tab_Status = new javax.swing.JScrollPane();
      
      
    

      
       jScrollPane_Tab_Status.setViewportView(jTable_Statuses);

       gridBagConstraints = new java.awt.GridBagConstraints();
       gridBagConstraints.gridx = 0;
       gridBagConstraints.gridy = 0;
       gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
       gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
       gridBagConstraints.weightx = 0.1;
       gridBagConstraints.weighty = 0.1;
       gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
       this.add(jScrollPane_Tab_Status, gridBagConstraints);

    
   












}



}
