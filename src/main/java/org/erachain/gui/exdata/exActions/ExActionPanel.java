package org.erachain.gui.exdata.exActions;


import org.erachain.core.exdata.exActions.ExAction;
import org.erachain.gui.IconPanel;
import org.erachain.gui.exdata.ExDataPanel;
import org.erachain.lang.Lang;
import org.mapdb.Fun;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;


public class ExActionPanel extends IconPanel {

    public static String NAME = "ExActionPanel";
    public static String TITLE = "Action";

    public ExActionPanel(ExDataPanel parent) {
        super(NAME, TITLE);

        GridBagLayout layout = new GridBagLayout();
        setLayout(layout);

        GridBagConstraints labelGBC = new GridBagConstraints();
        labelGBC.anchor = GridBagConstraints.LINE_END;
        labelGBC.insets = new Insets(10, 20, 10, 10);

        GridBagConstraints fieldGBC = new GridBagConstraints();
        fieldGBC.gridx = 1;
        fieldGBC.gridwidth = 2;
        fieldGBC.fill = GridBagConstraints.HORIZONTAL;
        fieldGBC.anchor = GridBagConstraints.LINE_START;
        fieldGBC.weightx = 0.1;
        fieldGBC.insets = new Insets(10, 0, 10, 20);

        GridBagConstraints panelBGC = new GridBagConstraints();
        panelBGC.gridx = 0;
        panelBGC.gridwidth = 15;
        panelBGC.fill = GridBagConstraints.BOTH;
        panelBGC.weightx = 0.1;
        //headBGC.anchor = GridBagConstraints.LINE_START;
        panelBGC.insets = new Insets(0, 0, 0, 0);

        int gridy = 0;

        add(new JLabel(Lang.T("Select Action Type") + ":"), labelGBC);

        selectBox = new JComboBox<>();
        selectBox.setModel(new javax.swing.DefaultComboBoxModel(new String[]{
                Lang.T("Without Action / Payments"),
                Lang.T("Mass Accruals by Filter"),
                Lang.T("Mass Payments by List (airdrop)")
        }));

        selectBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                updateAction();
            }
        });

        add(selectBox, fieldGBC);

        actionPanels[ExAction.FILTERED_ACCRUALS_TYPE] = new ExFilteredPaysPanel(parent);
        actionPanels[ExAction.SIMPLE_PAYOUTS_TYPE] = new ExAirDropPanel(parent);

        for (JPanel actionPanel : actionPanels) {
            actionPanel.setVisible(false);
            actionsPanel.add(actionPanel);
        }

        panelBGC.gridx = ++gridy;
        add(actionsPanel, panelBGC);

    }

    public void updateAction() {
        int selected = selectBox.getSelectedIndex() - 1;
        for (int i = 0; i < actionPanels.length; i++) {
            actionPanels[i].setVisible(selected == i);
        }
    }

    public Fun.Tuple2<ExAction, String> getAction() {
        int selected = selectBox.getSelectedIndex() - 1;
        if (selected < 0)
            return null;
        return ((ExActionPanelInt) actionPanels[selected]).getResult();
    }

    public javax.swing.JComboBox<Fun.Tuple2<Fun.Tuple2, String>> selectBox;
    private JPanel actionsPanel = new JPanel();
    public IconPanel[] actionPanels = new IconPanel[2];
}
