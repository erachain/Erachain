package gui.library;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.text.ParseException;
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

	public My_Date_JFormatedTextField(){
		super();
		th = this;
		MaskFormatter mf = null;
		try {
			mf = new MaskFormatter("##.##.####");
			mf.setPlaceholderCharacter('_');
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		this.setFormatter(mf);
		
		text_Color = this.getForeground();
		th.setForeground(Color.RED);
		th.setToolTipText(Lang.getInstance().translate("Must be Date (dd.mm.yyyy)"));
		MenuPopupUtil.installContextMenu(this);
		addCaretListener(new CaretListener(){

			@Override
			public void caretUpdate(CaretEvent arg0) {
				if (new Date(th.getText()) != null)	{
					th.setForeground(Color.RED);
				return ;
				}
				if (th.getText().length() != 9) {
					
					th.setForeground(Color.RED);
					return ;
				}
				th.setForeground(text_Color);
			}
			
			
		});
	
	}

}
