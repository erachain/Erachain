package org.erachain.gui.library;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class MSplitPane extends JSplitPane {
    public static final int ONE_TOUCH_CLOSE_LEFT_TOP = 1; // left-top
    public static final int ONE_TOUCH_CLOSE_RIGHT_BOTTOM = 2; // right-bottom
    public static final int ONE_TOUCH_CLOSE_LEFT_RIGHT = 0; // left-right
    public int set_CloseOnOneTouch; // perasmert close one touch panel
    private JSplitPane splitPane;
    private int wight_Div = 10;
    private JButton button;
    private JButton button1;
    public JButton buttonOrientation;

    public MSplitPane() {
        super(); //JSplitPane.VERTICAL_SPLIT, true);
        splitPane = this;
        init();
    }

    public MSplitPane(int pos, boolean bol) {
        super(pos, bol);
        splitPane = this;
        init();
    }

    // TODO    params: ONE_TOUCH_CLOSE_LEFT_TOP, ONE_TOUCH_CLOSE_RIGHT_BOTTOM, ONE_TOUCH_CLOSE_LEFT_RIGHT
    public void set_CloseOnOneTouch(int cl) {

        set_CloseOnOneTouch = cl;

    }

    private void init() {
        button1 = new JButton();
        button = new JButton();
        button1.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        // set sive devider
        Font ff = (Font) UIManager.get("Label.font");
        wight_Div = ff.getSize() + 4;
        // set minimum size panels
        Dimension minimumSize = new Dimension(0, 0);
        //   leftComponent.setMinimumSize(minimumSize);
        //   rightComponent.setMinimumSize(minimumSize);
        splitPane.setResizeWeight(0.5);
        // set left-right
        set_CloseOnOneTouch = ONE_TOUCH_CLOSE_LEFT_RIGHT;

        buttonOrientation = new JButton("!");
        buttonOrientation.setCursor(new Cursor(Cursor.HAND_CURSOR));


// set icon fron div location
        set_button_title();
        buttonOrientation.setMargin(new Insets(0, 0, 0, 0));
        button1.setMargin(new Insets(0, 0, 0, 0));
        button.setMargin(new Insets(0, 0, 0, 0));
        // set divider position
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                // 	System.out.print("lastposition = " + splitPane.getLastDividerLocation() + "\n");
                if (splitPane.getDividerLocation() == splitPane.getMinimumDividerLocation()) {
                    splitPane.setDividerLocation(splitPane.getLastDividerLocation());
                    //	button1.setVisible(true);
                    //	button.setVisible(true);
                } else if (splitPane.getDividerLocation() == splitPane.getMaximumDividerLocation()) {
                    return;
                } else {
                    splitPane.setDividerLocation(splitPane.getMaximumDividerLocation());
                    //	button.setVisible(false);
                    // 	button1.setVisible(true);
                }

            }
        });


        button1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                System.out.print("lastposition = " + splitPane.getLastDividerLocation() + "\n");
                if (splitPane.getDividerLocation() == splitPane.getMaximumDividerLocation()) {
                    splitPane.setDividerLocation(splitPane.getLastDividerLocation());
                    //	button1.setVisible(true);
                    // 	button.setVisible(true);
                } else if (splitPane.getDividerLocation() == splitPane.getMinimumDividerLocation()) {
                    return;
                } else {
                    splitPane.setDividerLocation(splitPane.getMinimumDividerLocation());
                    //	button1.setVisible(false);
                    //	button.setVisible(true);
                }

            }
        });

        buttonOrientation.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                // 	System.out.print("lastposition = " + splitPane.getLastDividerLocation() + "\n");
                if (getOrientation() == VERTICAL_SPLIT) {
                    setOrientation(HORIZONTAL_SPLIT);

                } else if (getOrientation() == HORIZONTAL_SPLIT) {
                    setOrientation(VERTICAL_SPLIT);

                }
                // set title Deveder Buttons
                set_button_title();
                // repaint Devider Panel
                setUI(new ButtonDividerUI(button, button1, wight_Div, buttonOrientation));
            }
        });


        setUI(new ButtonDividerUI(button, button1, wight_Div, buttonOrientation));


        // view buttons divider
        splitPane.addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent arg0) {
                // TODO Auto-generated method stub
                if (arg0.getPropertyName().equals("dividerLocation")) {
                    switch (set_CloseOnOneTouch) {

                        case (ONE_TOUCH_CLOSE_LEFT_RIGHT):
                            if (((int) arg0.getNewValue()) == splitPane.getMinimumDividerLocation()) {
                                button.setVisible(true);
                                button1.setVisible(false);
                                return;
                            }
                            if (((int) arg0.getNewValue()) == splitPane.getMaximumDividerLocation()) {
                                button.setVisible(false);
                                button1.setVisible(true);
                                return;
                            }
                            button.setVisible(true);
                            button1.setVisible(true);
                            return;

                        case (ONE_TOUCH_CLOSE_LEFT_TOP):
                            if (((int) arg0.getNewValue()) == splitPane.getMinimumDividerLocation()) {
                                button.setVisible(true);
                                button1.setVisible(false);
                                return;
                            }
                            button.setVisible(false);
                            button1.setVisible(true);

                            return;


                        case (ONE_TOUCH_CLOSE_RIGHT_BOTTOM):
                            if (((int) arg0.getNewValue()) == splitPane.getMaximumDividerLocation()) {
                                button.setVisible(false);
                                button1.setVisible(true);
                                return;
                            }
                            button.setVisible(true);
                            button1.setVisible(false);


                            return;


                    }

                }

            }

        });
         
        M_setDividerSize((int)(button1.getFont().getSize()*1.5));

    }


    public void M_setDividerSize(int div) {
        wight_Div = div;
        // splitPane.setDividerSize(div);
        setUI(new ButtonDividerUI(button, button1, wight_Div, buttonOrientation));

    }

    public void set_button_title() {
        if (getOrientation() == VERTICAL_SPLIT) {
            //char aa = (char)176;
            button1.setText((char) 0x02C4 + "");
            button.setText((char) 0x02C5 + "");
            buttonOrientation.setText((char) 0x02C2 + "");
        } else {
            button1.setText((char) 0x02C2 + "");
            button.setText((char) 0x02C3 + "");
            buttonOrientation.setText((char) 0x02C4 + "");

        }

    }

}

