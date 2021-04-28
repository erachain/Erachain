package org.erachain.gui;

import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.lang.Lang;
import org.erachain.settings.Settings;
import org.erachain.utils.ObserverMessage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

@SuppressWarnings("serial")
public class AboutFrame extends JDialog implements Observer {

    private static AboutFrame instance;
    protected boolean user_close = true;
    private AboutPanel aboutPanel;
    public JTextField console_Text;
    public JLabel lblAuthorsLabel;

    public AboutFrame() {
        //CREATE FRAME
        setTitle(Controller.getInstance().getApplicationName(false) + " - " + Lang.T("Debug"));
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
        this.getContentPane().setPreferredSize(new Dimension(802, 370));
        this.setUndecorated(true);

        this.aboutPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (user_close) {
                    setVisible(false);
                    //              dispose();
                }
            }
        });

        this.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (user_close) {
                    setVisible(false);
                    //              dispose();
                }
            }
        });

        //ADD GENERAL TABPANE TO FRAME
        getContentPane().add(this.aboutPanel);
        GridBagLayout gbl_aboutPanel = new GridBagLayout();
        //gbl_aboutPanel.columnWidths = new int[]{310, 181, 70, 200};
        gbl_aboutPanel.rowHeights = new int[]{252, 0, 0, 0, 0, 0};
        gbl_aboutPanel.columnWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
        gbl_aboutPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
        aboutPanel.setLayout(gbl_aboutPanel);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 10, 5, 10);
        //gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridwidth = 4;

        lblAuthorsLabel = new JLabel(//Lang.T("Author") + ": " +
                //+ "Ермолаев Дмитрий Сергеевич");
                Lang.T("ERACHAIN WORLD PTE LTD.")); //"Dmitrii Ermolaev"));
        lblAuthorsLabel.setFont(new Font("Tahoma", Font.PLAIN, 17));
        lblAuthorsLabel.setForeground(Color.RED);
        lblAuthorsLabel.setHorizontalAlignment(SwingConstants.CENTER);
        lblAuthorsLabel.setVisible(true);
        aboutPanel.add(lblAuthorsLabel, gbc);

        int gridy = 2;
        if (BlockChain.CLONE_MODE) {
            JLabel appNameLabel = new JLabel(Lang.T(Settings.CLONE_OR_SIDE.toLowerCase() + "chain") + ": "
                    + Controller.getInstance().APP_NAME);
            appNameLabel.setFont(new Font("Tahoma", Font.PLAIN, 17));
            appNameLabel.setForeground(Color.RED);
            appNameLabel.setHorizontalAlignment(SwingConstants.CENTER);
            ++gbc.gridy;
            aboutPanel.add(appNameLabel, gbc);
        }

        JLabel lblversionLabel = new JLabel(
                Lang.T("Version") + ": " + Controller.getVersion(true));
        lblversionLabel.setFont(new Font("Tahoma", Font.PLAIN, 17));
        lblversionLabel.setForeground(Color.RED);
        lblversionLabel.setHorizontalAlignment(SwingConstants.CENTER);
        ++gbc.gridy;
        aboutPanel.add(lblversionLabel, gbc);

        JLabel label = new JLabel(
                Controller.version + " " + Lang.T("build") + " "
                        + Controller.buildTime);

        label.setForeground(Color.RED);
        label.setFont(new Font("Tahoma", Font.PLAIN, 13));
        label.setHorizontalAlignment(SwingConstants.CENTER);
        ++gbc.gridy;
        aboutPanel.add(label, gbc);

        console_Text = new JTextField();
        console_Text.setEditable(false);
        console_Text.setText("");
        //  console_Text.setSize(100,26);
        console_Text.setForeground(Color.BLUE);
        console_Text.setFont(new Font("Tahoma", Font.PLAIN, 13));
        ++gbc.gridy;
        aboutPanel.add(console_Text, gbc);

        //SHOW FRAME
        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    public static AboutFrame getInstance() {

        if (instance == null) {
            instance = new AboutFrame();
        }

        return instance;


    }

    public void setUserClose(boolean uc) {
        user_close = uc;

    }

    public void set_console_Text(String str) {
        console_Text.setText(str);

    }

    @Override
    public void update(Observable o, Object arg) {
        ObserverMessage mes = (ObserverMessage) arg;
        if (mes.getType() == ObserverMessage.GUI_ABOUT_TYPE){
            String str ="";
            if(mes.getValue() != null) str = mes.getValue().toString();
            console_Text.setText(str);
        }
    }
}
