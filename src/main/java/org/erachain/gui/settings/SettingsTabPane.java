package org.erachain.gui.settings;
// 16 03

import org.erachain.lang.Lang;

import javax.swing.*;


public class SettingsTabPane extends JTabbedPane {

    private static final long serialVersionUID = 2198816415884720961L;

    public SettingsKnownPeersPanel settingsKnownPeersPanel;
    public SettingsBasicPanel settingsBasicPanel;
    public SettingsAllowedPanel settingsAllowedPanel;
    public UISettingPanel uI_Settings_Panel;

    public BackUPSettingPanel backUp_Setting_Panel;

    public RatesSettingPanel rates_Setting_Panel;

    public SettingsTabPane() {
        super();

        //ADD TABS

        settingsBasicPanel = new SettingsBasicPanel();
        JScrollPane scrollPane1 = new JScrollPane(settingsBasicPanel);
        this.addTab(Lang.T("Basic"), scrollPane1);

        settingsKnownPeersPanel = new SettingsKnownPeersPanel();
        JScrollPane scrollPane2 = new JScrollPane(settingsKnownPeersPanel);
        this.addTab(Lang.T("Known Peers"), scrollPane2);

        settingsAllowedPanel = new SettingsAllowedPanel();
        JScrollPane scrollPane3 = new JScrollPane(settingsAllowedPanel);
        this.addTab(Lang.T("Access permission"), scrollPane3);

        uI_Settings_Panel = new UISettingPanel();
        JScrollPane scrollPane4 = new JScrollPane(uI_Settings_Panel);
        this.addTab(Lang.T("UI Settings"), scrollPane4);

        backUp_Setting_Panel = new BackUPSettingPanel();
        JScrollPane scrollPane5 = new JScrollPane(backUp_Setting_Panel);
        this.addTab(Lang.T("BackUp Settings"), scrollPane5);

        rates_Setting_Panel = new RatesSettingPanel();
        JScrollPane scrollPane6 = new JScrollPane(rates_Setting_Panel);
        this.addTab(Lang.T("Exchange"), scrollPane6);
    }

    public void close() {
        //REMOVE OBSERVERS/HANLDERS
        this.settingsKnownPeersPanel.close();
        this.settingsAllowedPanel.close();
    }
}