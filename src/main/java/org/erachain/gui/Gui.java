package org.erachain.gui;


import org.erachain.controller.Controller;
import org.erachain.gui.create.NoWalletFrame;
import org.erachain.gui.create.SettingLangFrame;
import org.erachain.gui.library.MTable;
import org.erachain.lang.Lang;
import org.erachain.settings.Settings;
import org.erachain.utils.SysTray;

import javax.swing.*;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.awt.TrayIcon.MessageType;
import java.io.File;

import static org.erachain.gui.library.Library.setGuiLookAndFeel;

public class Gui extends JFrame {

    //private static final long serialVersionUID = 1L;
    private static final long serialVersionUID = 2717571093561259483L;

    public static final long PERIOD_UPDATE = 30000; // in MS

    private volatile static Gui maingui;
    private MainFrame mainframe;
    public final WalletNotifyTimer walletNotifyTimer;

    public static boolean SHOW_FEE_POWER = false;

    private Gui() throws Exception {

        setGuiLookAndFeel();

        if (Settings.getInstance().Dump().containsKey("lang")) {
            File langFile = new File(Settings.getInstance().getLangDir(), Settings.getInstance().getLangFileName());
            if (!langFile.isFile()) {
                new SettingLangFrame();
            }
        } else {
            new SettingLangFrame();
        }

        setGuiLookAndFeel();

        //CHECK IF WALLET EXISTS
        if (!Controller.getInstance().noUseWallet && !Controller.getInstance().doesWalletExists()) {
            //OPEN WALLET CREATION SCREEN
            new NoWalletFrame(this);
        } else if (Settings.getInstance().isGuiEnabled()) {
            mainframe = MainFrame.getInstance();
            mainframe.setVisible(true);
        }

        walletNotifyTimer = new WalletNotifyTimer();

    }

    public static Gui getInstance() throws Exception {
        if (maingui == null) {
            maingui = new Gui();
        }

        return maingui;
    }

    public static boolean isGuiStarted() {
        return maingui != null;
    }

    public static <T extends TableModel> MTable createSortableTable(T tableModel, int defaultSort) {
        //CREATE TABLE
        MTable table = new MTable(tableModel);

        //CREATE SORTER
        TableRowSorter<T> rowSorter = new TableRowSorter<T>(tableModel);
        //drowSorter.setSortsOnUpdates(true);

        //DEFAULT SORT DESCENDING
        rowSorter.toggleSortOrder(defaultSort);

        //ADD TO TABLE
        table.setRowSorter(rowSorter);

        //RETURN
        return table;
    }

    public static <T extends TableModel> MTable createSortableTable(T tableModel, int defaultSort, RowFilter<T, Object> rowFilter) {
        //CREATE TABLE
        MTable table = new MTable(tableModel);

        //CREATE SORTER
        TableRowSorter<T> rowSorter = new TableRowSorter<T>(tableModel);
        //rowSorter.setSortsOnUpdates(true);
        rowSorter.setRowFilter(rowFilter);

        //DEFAULT SORT DESCENDING
        rowSorter.toggleSortOrder(defaultSort);
        rowSorter.toggleSortOrder(defaultSort);

        //ADD TO TABLE
        table.setRowSorter(rowSorter);

        //RETURN
        return table;
    }

    public void onWalletCreated() {

        SysTray.getInstance().sendMessage(Lang.T("Wallet Initialized"),
                Lang.T("Your wallet is initialized"), MessageType.INFO);
        if (Settings.getInstance().isGuiEnabled())
            mainframe = MainFrame.getInstance();
    }

    public void bringtoFront() {
        if (mainframe != null) {
            mainframe.toFront();
        }
    }

    public void hideMainFrame() {
        if (mainframe != null) {
            mainframe.setVisible(false);
        }
    }

    public void onCancelCreateWallet() {
        Controller.getInstance().stopAndExit(0);
//		System.exit(0);
    }

}
