package gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import controller.Controller;
import lang.Lang;

@SuppressWarnings("serial")
public class AboutFrame extends JDialog{

	private static AboutFrame instance;
	private AboutPanel aboutPanel;
	protected boolean user_close = true;
	private JTextField console_Text;
	
	public static AboutFrame getInstance(){
		
		if(instance == null)
		{
			instance = new AboutFrame();
		}
		
		return instance;
		
		
	}
	
	private AboutFrame() 
	{
		//CREATE FRAME
		setTitle(Lang.getInstance().translate("ARONICLE.com")+ " - " + Lang.getInstance().translate("Debug"));
		//setModalityType(DEFAULT_MODALITY_TYPE);
		setModalityType(ModalityType.MODELESS);

		setAlwaysOnTop(false);
		//ICON
		List<Image> icons = new ArrayList<Image>();
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon16.png"));
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon32.png"));
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon64.png"));
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon128.png"));
		this.setIconImages(icons);
		
		//DEBUG TABPANE
        this.aboutPanel = new AboutPanel();
        this.getContentPane().setPreferredSize(new Dimension(802,370));
        this.setUndecorated(true);
     
        this.aboutPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
            	if (user_close ){
            	setVisible(false);
                dispose();
            	}
            }
        });
        
        this.addKeyListener(new KeyAdapter() {
		    public void keyPressed(KeyEvent e) {
		    	if (user_close){
		    	setVisible(false);
                dispose();
		    	}
		    }
		});
        
        //ADD GENERAL TABPANE TO FRAME
        getContentPane().add(this.aboutPanel);
        GridBagLayout gbl_aboutPanel = new GridBagLayout();
        gbl_aboutPanel.columnWidths = new int[]{483, 181, 70, 0};
        gbl_aboutPanel.rowHeights = new int[]{252, 0, 0, 0, 0, 0};
        gbl_aboutPanel.columnWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
        gbl_aboutPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
        aboutPanel.setLayout(gbl_aboutPanel);

        JLabel lblAuthorsLabel = new JLabel(Lang.getInstance().translate("Author") + ": "
        		+ "Ермолаев Дмитрий Сергеевич");
        lblAuthorsLabel.setFont(new Font("Tahoma", Font.PLAIN, 17));
        lblAuthorsLabel.setForeground(Color.WHITE);
        lblAuthorsLabel.setHorizontalAlignment(SwingConstants.CENTER);
        GridBagConstraints gbc_lblAuthorsLabel = new GridBagConstraints();
        gbc_lblAuthorsLabel.fill = GridBagConstraints.BOTH;
        gbc_lblAuthorsLabel.insets = new Insets(0, 0, 5, 5);
        gbc_lblAuthorsLabel.anchor = GridBagConstraints.NORTHWEST;
        gbc_lblAuthorsLabel.gridx = 1;
        gbc_lblAuthorsLabel.gridy = 1;
        aboutPanel.add(lblAuthorsLabel, gbc_lblAuthorsLabel);

        JLabel lblversionLabel = new JLabel(Lang.getInstance().translate("Version: ") + Controller.getInstance().getVersion());
        lblversionLabel.setFont(new Font("Tahoma", Font.PLAIN, 17));
        lblversionLabel.setForeground(Color.WHITE);
        lblversionLabel.setHorizontalAlignment(SwingConstants.CENTER);
        GridBagConstraints gbc_lbllversionLabel = new GridBagConstraints();
        gbc_lbllversionLabel.fill = GridBagConstraints.BOTH;
        gbc_lbllversionLabel.insets = new Insets(0, 0, 5, 5);
        gbc_lbllversionLabel.anchor = GridBagConstraints.NORTHWEST;
        gbc_lbllversionLabel.gridx = 1;
        gbc_lbllversionLabel.gridy = 2;
        aboutPanel.add(lblversionLabel, gbc_lbllversionLabel);
        
        JLabel label = new JLabel(Lang.getInstance().translate("Build date: ") + Controller.getInstance().getBuildDateString());
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Tahoma", Font.PLAIN, 13));
        GridBagConstraints gbc_label = new GridBagConstraints();
        gbc_label.insets = new Insets(0, 0, 5, 5);
        gbc_label.gridx = 1;
        gbc_label.gridy = 3;
        aboutPanel.add(label, gbc_label);

       
        console_Text = new JTextField();
        console_Text.setEditable(false);
        console_Text.setText("");
      //  console_Text.setSize(100,26);
        console_Text.setFont(new Font("Tahoma", Font.PLAIN, 13));
        GridBagConstraints gbc_Console = new GridBagConstraints();
        gbc_Console.insets = new Insets(5, 5, 5, 5);
        gbc_Console.gridx = 0;
        gbc_Console.gridy = 4;
        gbc_Console.fill = GridBagConstraints.HORIZONTAL;
        gbc_Console.weightx =1.0;
        gbc_Console.gridwidth = 3;
        aboutPanel.add(console_Text, gbc_Console);
        
        //SHOW FRAME
        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);
	}	
	public void setUserClose(boolean uc){
		user_close = uc;
		
	}
	public void set_console_Text(String str){
		console_Text.setText(str);
		
	}
}
