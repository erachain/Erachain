package org.erachain.utils;

import org.erachain.gui.library.MTextPane;
import org.erachain.lang.Lang;

import javax.swing.*;
import javax.swing.text.DefaultEditorKit;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class MenuPopupUtil {

    // https://github.com/jrwalsh/CycTools/blob/master/src/edu/iastate/cyctools/externalSourceCode/MenuPopupUtil.java
    // http://www.coderanch.com/t/346220/GUI/java/Copy-paste-popup-menu
    public static void installContextMenu(final JTextField component) {
        component.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showMenu(e);
                }
            }

            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showMenu(e);
                }
            }

            public void showMenu(final MouseEvent e) {
                if (e.isPopupTrigger()) {
                    component.requestFocus();
                    if (!component.isEditable() && component.getSelectedText() == null) {
                        component.selectAll();
                    }
                    final JPopupMenu menu = new JPopupMenu();
                    JMenuItem item;
                    item = new JMenuItem(new DefaultEditorKit.CopyAction());
                    item.setText(Lang.T("Copy"));
                    item.setEnabled(component.getSelectionStart() != component
                            .getSelectionEnd());
                    menu.add(item);
                    item = new JMenuItem(new DefaultEditorKit.CutAction());
                    item.setText(Lang.T("Cut"));
                    item.setEnabled(component.isEditable()
                            && component.getSelectionStart() != component
                            .getSelectionEnd());
                    menu.add(item);
                    item = new JMenuItem(new DefaultEditorKit.PasteAction());
                    item.setText(Lang.T("Paste"));
                    item.setEnabled(component.isEditable());
                    menu.add(item);
                    menu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });
    }

    public static void installContextMenu(final JTextArea component) {
        component.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showMenu(e);
                }
            }

            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showMenu(e);
                }
            }

            public void showMenu(final MouseEvent e) {
                if (e.isPopupTrigger()) {
                    component.requestFocus();
                    if (!component.isEditable() && component.getSelectedText() == null) {
                        component.selectAll();
                    }
                    final JPopupMenu menu = new JPopupMenu();
                    JMenuItem item;
                    item = new JMenuItem(new DefaultEditorKit.CopyAction());
                    item.setText(Lang.T("Copy"));
                    item.setEnabled(component.getSelectionStart() != component
                            .getSelectionEnd());
                    menu.add(item);
                    item = new JMenuItem(new DefaultEditorKit.CutAction());
                    item.setText(Lang.T("Cut"));
                    item.setEnabled(component.isEditable()
                            && component.getSelectionStart() != component
                            .getSelectionEnd());
                    menu.add(item);
                    item = new JMenuItem(new DefaultEditorKit.PasteAction());
                    item.setText(Lang.T("Paste"));
                    item.setEnabled(component.isEditable());
                    menu.add(item);
                    menu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });
    }

    public static void installContextMenu(final JTextPane component) {
        component.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showMenu(e);
                }
            }

            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showMenu(e);
                }
            }

            public void showMenu(final MouseEvent e) {
                if (e.isPopupTrigger()) {
                    component.requestFocus();
                    if (!component.isEditable() && component.getSelectedText() == null) {
                        //	component.selectAll();
                        //component.moveCaretPosition(0);
                    }
                    final JPopupMenu menu = new JPopupMenu();
                    JMenuItem item;
                    item = new JMenuItem(new DefaultEditorKit.CopyAction());
                    item.setText(Lang.T("Copy"));
                    item.setEnabled(component.getSelectionStart() != component
                            .getSelectionEnd());
                    menu.add(item);
                    item = new JMenuItem(new DefaultEditorKit.CutAction());
                    item.setText(Lang.T("Cut"));
                    item.setEnabled(component.isEditable()
                            && component.getSelectionStart() != component
                            .getSelectionEnd());
                    menu.add(item);
                    item = new JMenuItem(new DefaultEditorKit.PasteAction());
                    item.setText(Lang.T("Paste"));
                    item.setEnabled(component.isEditable());
                    menu.add(item);
                    menu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });
    }

    public static void installContextMenu(final MTextPane component) {
        component.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showMenu(e);
                }
            }

            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showMenu(e);
                }
            }

            public void showMenu(final MouseEvent e) {
                if (e.isPopupTrigger()) {
                    component.requestFocus();
                    final JPopupMenu menu = new JPopupMenu();
                    JMenuItem item;
                    item = new JMenuItem(new DefaultEditorKit.CopyAction());
                    item.setText(Lang.T("Copy"));
                    item.setEnabled(false);
                    menu.add(item);
                    item = new JMenuItem(new DefaultEditorKit.CutAction());
                    item.setText(Lang.T("Cut"));
                    item.setEnabled(false);
                    menu.add(item);
                    item = new JMenuItem(new DefaultEditorKit.PasteAction());
                    item.setText(Lang.T("Paste"));
                    item.setEnabled(false);
                    menu.add(item);
                    menu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });
    }


    public static void addPopup(Component component, final JPopupMenu popup) {
        component.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showMenu(e);
                }
            }

            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showMenu(e);
                }
            }

            private void showMenu(MouseEvent e) {
                popup.show(e.getComponent(), e.getX(), e.getY());
            }
        });
    }
}