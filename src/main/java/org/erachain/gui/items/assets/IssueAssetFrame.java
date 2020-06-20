package org.erachain.gui.items.assets;

import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.gui.models.AccountsComboBoxModel;
import org.erachain.lang.Lang;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

//import java.math.BigDecimal;
//import org.erachain.settings.Settings;

@SuppressWarnings("serial")
public class IssueAssetFrame extends JFrame {
    private JComboBox<Account> cbxFrom;
    private JTextField txtScale;
    private JTextField txtFeePow;
    private JTextField txtName;
    private JTextArea txtareaDescription;
    private JTextField txtQuantity;
    private JCheckBox chkDivisible;
    private JButton issueButton;

    public IssueAssetFrame() {
        super(Controller.getInstance().getApplicationName(false) + " - " + Lang.getInstance().translate("Issue Asset"));

        //CLOSE
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        //ICON
        List<Image> icons = new ArrayList<Image>();
        icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon16.png"));
        icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon32.png"));
        icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon64.png"));
        icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon128.png"));
        this.setIconImages(icons);

        //LAYOUT
        this.setLayout(new GridBagLayout());

        //PADDING
        ((JComponent) this.getContentPane()).setBorder(new EmptyBorder(5, 5, 5, 5));

        //LABEL GBC
        GridBagConstraints labelGBC = new GridBagConstraints();
        labelGBC.insets = new Insets(5, 5, 5, 5);
        labelGBC.fill = GridBagConstraints.HORIZONTAL;
        labelGBC.anchor = GridBagConstraints.NORTHWEST;
        labelGBC.weightx = 0;
        labelGBC.gridx = 0;

        //COMBOBOX GBC
        GridBagConstraints cbxGBC = new GridBagConstraints();
        cbxGBC.insets = new Insets(5, 5, 5, 5);
        cbxGBC.fill = GridBagConstraints.NONE;
        cbxGBC.anchor = GridBagConstraints.NORTHWEST;
        cbxGBC.weightx = 0;
        cbxGBC.gridx = 1;

        //TEXTFIELD GBC
        GridBagConstraints txtGBC = new GridBagConstraints();
        txtGBC.insets = new Insets(5, 5, 5, 5);
        txtGBC.fill = GridBagConstraints.HORIZONTAL;
        txtGBC.anchor = GridBagConstraints.NORTHWEST;
        txtGBC.weightx = 1;
        txtGBC.gridwidth = 2;
        txtGBC.gridx = 1;

        //BUTTON GBC
        GridBagConstraints buttonGBC = new GridBagConstraints();
        buttonGBC.insets = new Insets(5, 5, 5, 5);
        buttonGBC.fill = GridBagConstraints.NONE;
        buttonGBC.anchor = GridBagConstraints.NORTHWEST;
        buttonGBC.gridwidth = 2;
        buttonGBC.gridx = 0;

        //LABEL FROM
        labelGBC.gridy = 0;
        JLabel fromLabel = new JLabel(Lang.getInstance().translate("Account") + ":");
        this.add(fromLabel, labelGBC);

        //COMBOBOX FROM
        txtGBC.gridy = 0;
        this.cbxFrom = new JComboBox<Account>(new AccountsComboBoxModel());
        this.add(this.cbxFrom, txtGBC);

        IssueAssetPanel Issue_Assets_SplitPanel = new IssueAssetPanel();
        Issue_Assets_SplitPanel.setName(Lang.getInstance().translate("Issue Asset"));


    }
}
