package gui.library;

import javax.swing.plaf.basic.*;
import java.awt.event.*;
import javax.swing.*;
import java.beans.*;
import java.awt.*;
  
public class MSplitPane extends JSplitPane
{
 private  JSplitPane splitPane;
private int wight_Div=10;
private JButton button;
private JButton button1;
 
   
   public MSplitPane() {
	   super(); //JSplitPane.VERTICAL_SPLIT, true);
	   splitPane = this;
	   init();
   }
   public MSplitPane(int pos, boolean bol){
	   super(pos, bol);
	   splitPane = this;
	   init();
	}
   
   private void init(){
     //splitPane.setDividerSize(20);
     // splitPane.setOneTouchExpandable(true);
   
      button = new JButton(">>");
      button.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent ae) {
        	System.out.print("lastposition = " + splitPane.getLastDividerLocation() + "\n");
        	if(splitPane.getDividerLocation() == splitPane.getMinimumDividerLocation()) {splitPane.setDividerLocation(splitPane.getLastDividerLocation());}
        	else if(splitPane.getDividerLocation() == splitPane.getMaximumDividerLocation()){return;}else{splitPane.setDividerLocation(splitPane.getMaximumDividerLocation());}
        	
         }
      });
  
     
     
  
       button1 = new JButton("<<");
     
      button1.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent ae) {
        	System.out.print("lastposition = " + splitPane.getLastDividerLocation() + "\n");
        	if(splitPane.getDividerLocation() == splitPane.getMaximumDividerLocation()) {splitPane.setDividerLocation(splitPane.getLastDividerLocation());}
        	else if(splitPane.getDividerLocation() == splitPane.getMinimumDividerLocation()){return;}else{splitPane.setDividerLocation(splitPane.getMinimumDividerLocation());}
        	
         }
      });
     
        
     
  
     setUI(new ButtonDividerUI(button, button1, wight_Div));
      
      
     
  
      addComponentListener(new ComponentAdapter() {
         public void componentShown(ComponentEvent event) {
            splitPane.setDividerLocation(0.5); 
                 
            removeComponentListener(this);
         }
      });
   }
  
    
 public void  M_setDividerSize(int div){
	 wight_Div = div;
	// splitPane.setDividerSize(div);
	 setUI(new ButtonDividerUI(button, button1, wight_Div));
	 
 }

}
  
class ButtonDividerUI extends BasicSplitPaneUI
{
   protected JButton button;
   protected JButton button1;
private BasicSplitPaneDivider divider1;
protected GridBagConstraints gridBagConstraints;
protected GridBagConstraints gridBagConstraints1;
private int wight_Div;
  
   public ButtonDividerUI(JButton button,JButton button1, int wight_Div ) {
      this.button = button;
      this.button1 = button1;
      this.wight_Div = wight_Div;
   }
  
   public BasicSplitPaneDivider createDefaultDivider() {
	        divider1 = new BasicSplitPaneDivider(this) {
        

		public int getDividerSize() {
        	 
        	
        	 
            if (getOrientation() == JSplitPane.HORIZONTAL_SPLIT) {
              	divider1.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
              	 button1.setPreferredSize(new Dimension(wight_Div, wight_Div));
                 button.setPreferredSize(new Dimension(wight_Div, wight_Div));
              	return button.getPreferredSize().width;
            }
            divider1.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
            //button1.setPreferredSize(new Dimension(wight_Div, wight_Div));
            //button.setPreferredSize(new Dimension(wight_Div, wight_Div));
          return button.getPreferredSize().height;
         }
      };
  
      divider1.setLayout(new java.awt.GridBagLayout());
     
    
      divider1.add(button1);
      divider1.add(button);
      return divider1;
   }
}