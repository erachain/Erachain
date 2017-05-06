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
private int set_CloseOnOneTouch; // perasmert close one touch panel
public static final int ONE_TOUCH_CLOSE_LEFT_TOP = 1; // left-top
public static final int ONE_TOUCH_CLOSE_RIGHT_BOTTOM = 2; // right-bottom
public static final int ONE_TOUCH_CLOSE_LEFT_RIGHT = 0; // left-right
   
   public MSplitPane() {
	   super(); //JSplitPane.VERTICAL_SPLIT, true);
	   splitPane = this;
	   set_CloseOnOneTouch= ONE_TOUCH_CLOSE_LEFT_RIGHT;
	   init();
   }
   public MSplitPane(int pos, boolean bol){
	   super(pos, bol);
	   splitPane = this;
	   init();
	}
   public void set_CloseOnOneTouch(int cl){
	// TODO    params: ONE_TOUCH_CLOSE_LEFT_TOP, ONE_TOUCH_CLOSE_RIGHT_BOTTOM, ONE_TOUCH_CLOSE_LEFT_RIGHT
	   set_CloseOnOneTouch = cl;   
	   
   }
   
   private void init(){
     //splitPane.setDividerSize(20);
     // splitPane.setOneTouchExpandable(true);
   button1 = new JButton("<");
      button = new JButton(">");
  // set divider position
      button.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent ae) {
       // 	System.out.print("lastposition = " + splitPane.getLastDividerLocation() + "\n");
        	if(splitPane.getDividerLocation() == splitPane.getMinimumDividerLocation()) {splitPane.setDividerLocation(splitPane.getLastDividerLocation());
        //	button1.setVisible(true);
        //	button.setVisible(true);
        	}
        	else if(splitPane.getDividerLocation() == splitPane.getMaximumDividerLocation()){return;}else{splitPane.setDividerLocation(splitPane.getMaximumDividerLocation());
        //	button.setVisible(false);
       // 	button1.setVisible(true);
        	}
        	
         }
      });
  
     
     
  
       
     
      button1.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent ae) {
        	System.out.print("lastposition = " + splitPane.getLastDividerLocation() + "\n");
        	if(splitPane.getDividerLocation() == splitPane.getMaximumDividerLocation()) {
        		splitPane.setDividerLocation(splitPane.getLastDividerLocation());
        	//	button1.setVisible(true);
           // 	button.setVisible(true);	
        	}
        	else if(splitPane.getDividerLocation() == splitPane.getMinimumDividerLocation()){return;}
        		else{splitPane.setDividerLocation(splitPane.getMinimumDividerLocation());
        	//	button1.setVisible(false);
        	//	button.setVisible(true);
        		}
        	
         }
      });
     
      
     
  
     setUI(new ButtonDividerUI(button, button1, wight_Div));
      
     splitPane.addComponentListener(new ComponentListener(){

		@Override
		public void componentHidden(ComponentEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void componentMoved(ComponentEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void componentResized(ComponentEvent arg0) {
			// TODO Auto-generated method stub
		int a = 1;
		a=a;
		}

		@Override
		public void componentShown(ComponentEvent arg0) {
			// TODO Auto-generated method stub
			
		}
    	  
    	  
    	  
    	  
      });
 // view bottons divider    
     splitPane.addPropertyChangeListener(new PropertyChangeListener(){

		@Override
		public void propertyChange(PropertyChangeEvent arg0) {
			// TODO Auto-generated method stub
			if(arg0.getPropertyName().equals("dividerLocation")){
				switch(set_CloseOnOneTouch){
				
				case (ONE_TOUCH_CLOSE_LEFT_RIGHT):
					if(((int) arg0.getNewValue())==splitPane.getMinimumDividerLocation()) {
					button1.setVisible(false);
	        		button.setVisible(true);
	        		return;
				}
				if(((int) arg0.getNewValue())==splitPane.getMaximumDividerLocation()) {
					button.setVisible(false);
	        		button1.setVisible(true);
	        		return;
				}
				button.setVisible(true);
        		button1.setVisible(true);
				return;
					
				case (ONE_TOUCH_CLOSE_LEFT_TOP):
					if(((int) arg0.getNewValue())==splitPane.getMinimumDividerLocation()) {
						button1.setVisible(false);
		        		button.setVisible(true);
		        		return;
					}
				button.setVisible(false);
        		button1.setVisible(true);
								
				return;
				
				
				case (ONE_TOUCH_CLOSE_RIGHT_BOTTOM):
					if(((int) arg0.getNewValue())==splitPane.getMaximumDividerLocation()) {
						button.setVisible(false);
		        		button1.setVisible(true);
		        		return;
					}
					button.setVisible(true);
	        		button1.setVisible(false);
					
					
					
					
				return;
					
					
				}	
				
			}
		
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
              	 button1.setPreferredSize(new Dimension(wight_Div, wight_Div*2));
                 button.setPreferredSize(new Dimension(wight_Div, wight_Div*2));
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