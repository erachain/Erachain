package org.erachain.gui.items.other;

import org.erachain.gui.ConsolePanel;
import org.erachain.gui.IconPanel;
import org.erachain.lang.Lang;
import org.erachain.settings.Settings;

import javax.swing.*;

@SuppressWarnings("serial")
public class OtherConsolePanel extends IconPanel {

    public static String NAME = "OtherConsolePanel";
    public static String TITLE = "Console";

    private ConsolePanel debugTabPane;

    public OtherConsolePanel() {
        super(NAME, TITLE);

        //ADD TABS
        if (Settings.getInstance().isGuiConsoleEnabled()) {
            this.debugTabPane = new ConsolePanel();


            java.awt.GridBagConstraints gridBagConstraints;
            setLayout(new java.awt.GridBagLayout());

            JLabel jLabel1 = new javax.swing.JLabel();
            jLabel1.setText(Lang.T("Console"));
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
            add(jLabel1, gridBagConstraints);


            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 1;
            gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
            gridBagConstraints.weightx = 0.1;
            gridBagConstraints.weighty = 0.1;
            gridBagConstraints.insets = new java.awt.Insets(0, 10, 10, 10);
            add(debugTabPane, gridBagConstraints);

        }

        this.setVisible(true);

    }
}
