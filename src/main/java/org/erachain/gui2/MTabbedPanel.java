package org.erachain.gui2;

import javax.swing.*;
import java.awt.*;

public class MTabbedPanel extends JTabbedPane {
    public MTabbedPanel() {
        super();
    }

    public MTabbedPanel(int a, int b) {
        //TODO	public JTabbedPane(int tabPlacement, int tabLayoutPolicy)
        super(a, b);
    }

    public void addTabWithCloseButton(String str, Component comp) {
        // add tab to tabbed panel
        this.addTab(str, comp);
    }

    public void addTabWithCloseButton(String str, Image icon,  JPanel comp) {

        this.addTab(str, comp);
        init(comp, icon );
    }

    public void addTabWithCloseButton(String str, Image icon, Component comp, String tip) {
        init(comp, icon);
    }

    private void init(Component comp, Image icon) {

        // set for tab view close button
        ButtonTabComponent button_Comp = new ButtonTabComponent(this, icon);
        this.setTabComponentAt(this.indexOfComponent(comp), button_Comp);
        // write info to tabbed setting object

    }


}
