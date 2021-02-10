/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.erachain.gui;

import org.erachain.lang.Lang;

import javax.swing.*;
import java.awt.*;


/**
 * @author Саша
 */
public class IconPanel extends JPanel {

    protected static String panelName;
    protected String title;
    protected static String iconName;

    public IconPanel(String panelName) {
        super();
        this.panelName = title = iconName = panelName;
        setName(title);
        iconName += ".png";
    }

    public IconPanel(String panelName, String panelTitle) {
        super();
        this.panelName = iconName = panelName;
        if (panelTitle != null) {
            title = Lang.T(panelTitle);
            setName(title);
        }
        if (iconName != null) {
            iconName += ".png";
        }

    }

    public static Image getIcon(String iconName) {
        {
            try {
                return Toolkit.getDefaultToolkit().getImage(iconName);
            } catch (Exception e) {
                return null;
            }
        }
    }

    public Image getIcon() {
        return getIcon(iconName);
    }

    public String getTitle() {
        return title;
    }

}