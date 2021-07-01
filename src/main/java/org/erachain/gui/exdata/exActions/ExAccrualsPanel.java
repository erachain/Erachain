package org.erachain.gui.exdata.exActions;


import org.erachain.core.exdata.exActions.ExPays;
import org.erachain.core.item.ItemCls;
import org.erachain.gui.IconPanel;
import org.erachain.gui.exdata.ExDataPanel;
import org.erachain.lang.Lang;
import org.mapdb.Fun;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;


public class ExAccrualsPanel extends IconPanel {

    public static String NAME = "ExAccrualsPanel";
    public static String TITLE = "Accruals";

    private ExDataPanel parent;
    private Boolean lock = new Boolean(false);

    public ExAccrualsPanel(ExDataPanel parent) {
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
                Lang.T("Without Accruals"),
                Lang.T("Simple"),
                Lang.T("Calculated")
        }));

        selectBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                updateAction();
            }
        });

        add(selectBox, fieldGBC);

        filteredPaysPanel = new ExFilteredPaysPanel(parent);
        airDropPanel = new ExAirDropPanel(parent);

        updateAction();

        panel.add(filteredPaysPanel);
        panel.add(airDropPanel);

        headBGC.gridx = ++gridy;
        add(panel, headBGC);


    }

    public void updateAction() {
        int selected = selectBox.getSelectedIndex();
        filteredPaysPanel.setVisible(selected == 1);
        airDropPanel.setVisible(selected == 2);

    }

    public Fun.Tuple2<ExPays, String> getAccruals() {
        return null;
    }

    private javax.swing.JComboBox<Fun.Tuple2<Fun.Tuple2, String>> selectBox;
    private JScrollPane jScrollPaneAccruals = new JScrollPane();
    private JPanel panel = new JPanel();
    public JComboBox<ItemCls> jComboBoxAccrualAsset;
    public JCheckBox jCheckBoxAccrualsUse;
    public JPanel jPanelMain;
    public JComboBox<ItemCls> jComboBoxFilterAsset;

    public ExAirDropPanel airDropPanel;
    public ExFilteredPaysPanel filteredPaysPanel;
}
