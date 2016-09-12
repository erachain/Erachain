package gui.items.imprints;

import java.awt.Button;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import gui.Split_Panel;
import lang.Lang;

public class Issue_Split_Panel extends Split_Panel{
	Table_Model_Issue_Hashes table_Model;
	private JTable Table_Hash;
	private JButton jButton3_jToolBar_RightPanel;
	
public	Issue_Split_Panel(){

	// left panel

this.toolBar_LeftPanel.setVisible(false);
this.searchToolBar_LeftPanel.setVisible(false);
this.jTable_jScrollPanel_LeftPanel.setVisible(false);	
this.jScrollPanel_LeftPanel.setViewportView(new Issue_Hash_Imprint());

// Right panel
//this.jToolBar_RightPanel.setSize(WIDTH, 200);
jButton2_jToolBar_RightPanel.setText(Lang.getInstance().translate("Get Hashs from File"));
//jButton2_jToolBar_RightPanel.setSize(70, 30);
//jButton2_jToolBar_RightPanel.setPreferredSize(new Dimension(100,200));


jButton1_jToolBar_RightPanel.setText(Lang.getInstance().translate("Delete Hash"));
jButton1_jToolBar_RightPanel.addActionListener(new ActionListener() {
// delete row
    @Override
    public void actionPerformed(ActionEvent e) {
    	
    	if(table_Model.getRowCount() > 1)
    	{        	
            int selRow = Table_Hash.getSelectedRow();
            if(selRow != -1) {
                ((DefaultTableModel) table_Model).removeRow(selRow);
                table_Model.fireTableDataChanged(); 
            }
    	}
    }
});


jButton3_jToolBar_RightPanel = new JButton();
jButton3_jToolBar_RightPanel.setText(Lang.getInstance().translate("Create Hash from File"));
//jButton3_jToolBar_RightPanel.setFocusable(false);
jButton3_jToolBar_RightPanel.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
jButton3_jToolBar_RightPanel.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
jToolBar_RightPanel.add(jButton3_jToolBar_RightPanel);


jButton1_jToolBar_RightPanel.setFont(new Font("Tahoma", 0, 14));
jButton2_jToolBar_RightPanel.setFont(new Font("Tahoma", 0, 14));
jButton3_jToolBar_RightPanel.setFont(new Font("Tahoma", 0, 14));


table_Model = new Table_Model_Issue_Hashes(new Object[] { Lang.getInstance().translate("Hash") + "   32 bits  (Base58 Format)" }, 0);
Table_Hash =new JTable(table_Model);
this.jScrollPane_jPanel_RightPanel.setViewportView(Table_Hash);

}
	
	
	

}
