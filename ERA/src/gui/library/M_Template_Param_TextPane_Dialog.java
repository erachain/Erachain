package gui.library;

import java.awt.Dimension;
import java.awt.Point;

import javax.swing.JDialog;
import javax.swing.JTextPane;

public class M_Template_Param_TextPane_Dialog extends JDialog {

	public JTextPane tp;
	public M_Template_Param_TextPane_Dialog(String string, Point point){
		
	setModal(true);	
	tp = new JTextPane();
	 this.setLocationRelativeTo(null);
	 tp.setText(string);
	add(tp);
//	setLocation(point);
	setSize(new Dimension(600,400));
	setVisible(true);
	
		
	}
}
