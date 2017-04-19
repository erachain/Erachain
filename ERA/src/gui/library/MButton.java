package gui.library;

import java.awt.Dimension;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.UIManager;

import lang.Lang;

public class MButton extends JButton{


	public MButton(){
		super();
		
	}
	
public MButton(String str, double row){
	super();
	set_Text_and_Size_From_UIManaget(str, row)	;
}
/** Set text with set saze button at UIManager
 * 
 * @param str - text
 * @param row - col row
 */
public void set_Text_and_Size_From_UIManaget(String str, double scale_Height){
	   int wt = (int) (getFontMetrics( UIManager.getFont("Button.font")).stringWidth(str +"wwWW"));	
    	int ht = (int) ((int) (getFontMetrics( UIManager.getFont("Button.font")).getFont().getSize()) *scale_Height);
    
	setText(str);
    setPreferredSize(new Dimension(wt,ht));
    
}


}
