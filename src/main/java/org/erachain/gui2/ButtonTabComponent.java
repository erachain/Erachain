package org.erachain.gui2;

/*
 * Copyright (c) 1995, 2008, Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle or the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */


import org.erachain.gui.*;
import org.erachain.gui.items.other.OtherSplitPanel;
import org.erachain.lang.Lang;
import org.json.simple.JSONObject;
import org.erachain.settings.Settings;
import org.erachain.utils.SaveStrToFile;

import javax.swing.*;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.HashMap;

/**
 * Component to be used as tabComponent;
 * Contains a JLabel to show the text and
 * a JButton to close the tab it belongs to
 */
public class ButtonTabComponent extends JPanel {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private final static MouseListener buttonMouseListener = new MouseAdapter() {
        public void mouseEntered(MouseEvent e) {
            Component component = e.getComponent();
            if (component instanceof AbstractButton) {
                AbstractButton button = (AbstractButton) component;
                button.setBorderPainted(true);
            }
        }

        public void mouseExited(MouseEvent e) {
            Component component = e.getComponent();
            if (component instanceof AbstractButton) {
                AbstractButton button = (AbstractButton) component;
                button.setBorderPainted(false);
            }
        }
    };
    private final JTabbedPane pane;

    public ButtonTabComponent(final JTabbedPane pane) {
        //unset default FlowLayout' gaps
        super(new FlowLayout(FlowLayout.LEFT, 0, 0));
        if (pane == null) {
            throw new NullPointerException("TabbedPane is null");
        }
        this.pane = pane;
        setOpaque(false);

        //make JLabel read titles from JTabbedPane
        JLabel label = new JLabel() {
            public String getText() {
                int i = pane.indexOfTabComponent(ButtonTabComponent.this);
                if (i != -1) {
                    return pane.getTitleAt(i);
                }
                return null;
            }
        };

        add(label);
        //add more space between the label and the button
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
        //tab button
        JButton button = new TabButton();
        add(button);
        //add more space to the top of the component
        setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
    }

    private class TabButton extends JButton implements ActionListener {
        public TabButton() {
            int size = UIManager.getFont("TextField.font").getSize() + 4;
            if (size < 10) size = 10;
            setPreferredSize(new Dimension(size, size));
            setToolTipText(Lang.getInstance().translate("close this tab"));
            //Make the button looks the same for all Laf's
            setUI(new BasicButtonUI());
            //Make it transparent
            setContentAreaFilled(false);
            //No need to be focusable
            setFocusable(false);
            setBorder(BorderFactory.createEtchedBorder());
            setBorderPainted(false);
            //Making nice rollover effect
            //we use the same listener for all buttons
            addMouseListener(buttonMouseListener);
            setRolloverEnabled(true);
            //Close the proper tab by clicking the button
            addActionListener(this);
        }

        public void actionPerformed(ActionEvent e) {

            int i = pane.indexOfTabComponent(ButtonTabComponent.this);
            JFrame frame1 = new JFrame();
            if (i != -1) {
                Component p_Comp = pane.getComponentAt(i);

                // seve Split Panel params
                JSONObject settingsJSONbuf = new JSONObject();
                settingsJSONbuf = Settings.getInstance().Dump();
                JSONObject settingsJSON = new JSONObject();
                if (settingsJSONbuf.containsKey("Main_Frame_Setting"))
                    settingsJSON = (JSONObject) settingsJSONbuf.get("Main_Frame_Setting");
                HashMap outTabbedDiv = new HashMap();
                if (p_Comp instanceof OtherSplitPanel) {
                    OtherSplitPanel sP = ((OtherSplitPanel) p_Comp);
                    sP.onClose();
                } else if (p_Comp instanceof SplitPanel) {
                    SplitPanel sP = ((SplitPanel) p_Comp);
                    outTabbedDiv.put("Div_Orientation", sP.jSplitPanel.getOrientation() + "");

                    // write

                    int lDiv = sP.jSplitPanel.getLastDividerLocation();
                    int div = sP.jSplitPanel.getDividerLocation();


                    outTabbedDiv.put("Div_Last_Loc", lDiv + "");
                    outTabbedDiv.put("Div_Loc", div + "");

                    settingsJSON.put(p_Comp.getClass().getSimpleName(),
                            outTabbedDiv);

                    settingsJSONbuf.put("Main_Frame_Setting", settingsJSON);
                    // save setting to setting file
                    try {
                        SaveStrToFile.saveJsonFine(Settings.getInstance().getSettingsPath(), settingsJSONbuf);
                    } catch (IOException e1) {

                        JOptionPane.showMessageDialog(frame1, "Error writing to the file: "
                                        + Settings.getInstance().getSettingsPath() + "\nProbably there is no access.", "Error!",
                                JOptionPane.ERROR_MESSAGE);
                    }
                    sP.onClose();
                    sP = null;

                }
                pane.remove(i);
                p_Comp = null;
                outTabbedDiv = null;
                settingsJSON = null;
                settingsJSONbuf = null;
                frame1 = null;

            }
        }

        //we don't want to update UI for this button
        public void updateUI() {
        }

        //paint the cross
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            //shift the image for pressed buttons
            if (getModel().isPressed()) {
                g2.translate(1, 1);
            }
            g2.setStroke(new BasicStroke(2));
            g2.setColor(Color.PINK);
            if (getModel().isRollover()) {
                g2.setColor(Color.MAGENTA);
            }
            int delta = 5;
            g2.drawLine(delta, delta, getWidth() - delta - 1, getHeight() - delta - 1);
            g2.drawLine(getWidth() - delta - 1, delta, delta, getHeight() - delta - 1);
            g2.dispose();
            g2 = null;
        }
    }
}