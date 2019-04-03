package org.erachain.gui.library;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JInternalFrame;

public class FindHashFrmDirDialog  extends JDialog {

        private static final long serialVersionUID = 1L;
        public FindHashFromDirPanel panel;


        public FindHashFrmDirDialog() {
            init();
            addWindowListener(new WindowListener(){

                @Override
                public void windowActivated(WindowEvent arg0) {
                    // TODO Auto-generated method stub
                    
                }

                @Override
                public void windowClosed(WindowEvent arg0) {
                    // TODO Auto-generated method stub
                    panel.stop();
                    panel.dialog.setVisible(false);
                }

                @Override
                public void windowClosing(WindowEvent arg0) {
                    // TODO Auto-generated method stub
                    panel.stop();
                }

                @Override
                public void windowDeactivated(WindowEvent arg0) {
                    // TODO Auto-generated method stub
                    
                }

                @Override
                public void windowDeiconified(WindowEvent arg0) {
                    // TODO Auto-generated method stub
                    
                }

                @Override
                public void windowIconified(WindowEvent arg0) {
                    // TODO Auto-generated method stub
                    
                }

                @Override
                public void windowOpened(WindowEvent arg0) {
                    // TODO Auto-generated method stub
                    
                }
                
            });
           
        }

       
        
       

        private void  init(){
         // ICON
            List<Image> icons = new ArrayList<Image>();
            icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon16.png"));
            icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon32.png"));
            icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon64.png"));
            icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon128.png"));
            this.setIconImages(icons);
            panel = new FindHashFromDirPanel();
                       
            getContentPane().add(panel, BorderLayout.CENTER);
            Toolkit kit = Toolkit.getDefaultToolkit();
            Dimension screens = kit.getScreenSize();
            int h = screens.height - 50;
            int w = screens.width - 50;
            this.setPreferredSize(new Dimension(w,h));
            this.pack();
            this.setResizable(true);
            setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);
            setModal(true);

            
            this.setLocationRelativeTo(null);
            this.setVisible(true);
            
        

    }
}
