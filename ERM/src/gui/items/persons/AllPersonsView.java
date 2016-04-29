package gui.items.persons;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JToolBar;

import lang.Lang;

public class AllPersonsView extends JInternalFrame{


	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public AllPersonsView () {
		
		// tool bar
				JToolBar tb1 = new JToolBar(" Панель 1"),

						tb2 = new JToolBar(" Панель 2");

						tb1.setRollover(true);

						tb1.add(new JButton(new ImageIcon("Add24.gif"))); tb1.add(new JButton(new ImageIcon("AlignTop24.gif")));

						tb1.add(new JButton(new ImageIcon("About24.gif")));

						tb2.add(new JButton("Первая")); tb2.add(new JButton("Вторая"));

						tb2.add(new JButton("Третья"));

						//add(tb1, BorderLayout.NORTH); 
						add(tb2, BorderLayout.NORTH);
	
	
	add(new AllPersonsFrame(),BorderLayout.CENTER);
	
	
		//класс взаимодействия с оконной системой ОС

        Toolkit kit = Toolkit.getDefaultToolkit();

        Dimension screens = kit.getScreenSize();

        int w,h;

        w = screens.width;

        h = screens.height;

     

     
        
        
        //SHOW FRAME
        this.pack();
     //   this.setLocationRelativeTo(null);
 
		this.setMaximizable(true);
		this.setTitle(Lang.getInstance().translate("Persons"));
		this.setClosable(true);
//		this.setResizable(true);
		
	
		   this.setSize(1000,400);
		this.setLocation(50, 20);
	//	this.setIconImages(icons);
		
		//CLOSE
		setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);
        this.setResizable(true);
        this.setVisible(true);
	
	}

}

