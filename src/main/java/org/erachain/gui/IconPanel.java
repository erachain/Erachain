/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.erachain.gui;

import javax.swing.*;
import java.awt.*;


/**
 * @author Саша
 */
public class IconPanel extends JPanel {

    protected static String panelName;
    protected static String iconName;

    public IconPanel(String panelName) {
        super();
        this.panelName = panelName;
        iconName += ".png";
    }

    public Image getIcon() {
        {
            try {
                return Toolkit.getDefaultToolkit().getImage(iconName);
            } catch (Exception e) {
                return null;
            }
        }
    }
}