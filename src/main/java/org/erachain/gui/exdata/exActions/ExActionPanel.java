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

    private ExDataPanel parent;
    private Boolean lock = new Boolean(false);

    public ExActionPanel(ExDataPanel parent) {
        super(NAME, TITLE);
        this.parent = parent;

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

        GridBagConstraints headBGC = new GridBagConstraints();
        headBGC.gridwidth = 15;
        headBGC.fill = GridBagConstraints.HORIZONTAL;
        headBGC.weightx = 0.1;
        //headBGC.anchor = GridBagConstraints.LINE_START;
        headBGC.insets = new Insets(5, 0, 0, 0);

        int gridy = 0;

        add(new JLabel(Lang.T("Select Accruals Type") + ":"), labelGBC);

        selectBox = new JComboBox<>();
        selectBox.setModel(new javax.swing.DefaultComboBoxModel(new String[]{
                Lang.T("Without Action"),
                Lang.T("Simple List Accruals (Air Drop)"),
                Lang.T("Calculated Filtered Accruals")
        }));

        selectBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                updateAction();
            }
        });

        add(selectBox, fieldGBC);

        actionPanels[ExAction.FILTERED_ACCRUALS_TYPE] = new ExFilteredPaysPanel(parent);
        actionPanels[ExAction.LIST_PAYOUTS_TYPE] = new ExAirDropPanel(parent);

        updateAction();

        for (JPanel actionPanel : actionPanels) {
            actionPanel.setVisible(false);
            panel.add(actionPanel);
        }

        headBGC.gridx = ++gridy;
        add(panel, headBGC);


    }

    public void updateAction() {
        int selected = selectBox.getSelectedIndex();
        for (int i = 0; i < actionPanels.length; i++) {
            actionPanels[i].setVisible(selected == i);
        }
    }

    public Fun.Tuple2<ExAction, String> getAction() {
        int selected = selectBox.getSelectedIndex();
        return actionPanels[selected];
    }

    private javax.swing.JComboBox<Fun.Tuple2<Fun.Tuple2, String>> selectBox;
    private JPanel panel = new JPanel();
    private IconPanel[] actionPanels;
}
