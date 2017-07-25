package gui.library;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.MaskFormatter;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.time.DateUtils;

import lang.Lang;
import utils.MenuPopupUtil;

public class My_Date_JFormatedTextField extends JFormattedTextField {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private My_Date_JFormatedTextField th;
	private Color text_Color;

	public My_Date_JFormatedTextField(MaskFormatter mf){
		super(mf);
		th = this;
		
		
		
		text_Color = this.getForeground();
		th.setForeground(Color.RED);
		th.setToolTipText(Lang.getInstance().translate("Must be Date (dd-mm-yyyy)"));
		MenuPopupUtil.installContextMenu(this);
		addCaretListener(new CaretListener(){

			@SuppressWarnings("deprecation")
			@Override
			public void caretUpdate(CaretEvent arg0) {
				
				String d = th.getText();
					
					SimpleDateFormat formatter = new SimpleDateFormat("yyyy-mm-dd");
			  //      String dateInString = d;
			        Date t; 
			        try {
			        	 t = formatter.parse(d); 
			     //       Date date = formatter.parse(dateInString);
			        	 System.out.println(t);
			      //      System.out.println(formatter.format(date));

			      
				} catch (Exception e) {
					// TODO Auto-generated catch block
					th.setForeground(Color.RED);
					return ;
				}
			        

			       

			       

			       

			      
			           
				
				if (d.replace("_", "").length() != 10) {
					
					th.setForeground(Color.RED);
					return ;
				}
				th.setForeground(text_Color);
			
			}
			
		});
	
	}

}
