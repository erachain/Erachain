package org.erachain.gui.items.polls;

import org.erachain.controller.Controller;
import org.erachain.core.item.ItemCls;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.item.polls.PollCls;
import org.erachain.core.item.templates.TemplateCls;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;
import org.erachain.gui.SplitPanel;
import org.erachain.gui.items.ItemSplitPanel;
import org.erachain.gui.library.MTable;
import org.erachain.gui.models.RendererIcon;
import org.erachain.gui.models.WalletItemPersonsTableModel;
import org.erachain.gui.models.WalletItemPollsTableModel;
import org.erachain.gui.records.VouchRecordDialog;
import org.erachain.lang.Lang;
import org.erachain.settings.Settings;
import org.erachain.utils.TableMenuPopupUtil;
import org.erachain.utils.URLViewer;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.net.URL;

public class Polls_My_SplitPanel extends ItemSplitPanel {

    protected ItemCls itemTableSelected = null;


    public Polls_My_SplitPanel() {
        super(new WalletItemPollsTableModel(), "Polls_My_SplitPanel");
        this.setName(Lang.getInstance().translate("My Polls"));

        jTableJScrollPanelLeftPanel.getColumnModel().getColumn(3).setMaxWidth(200);
        jTableJScrollPanelLeftPanel.getColumnModel().getColumn(3).setPreferredWidth(100);

        JMenuItem vouch_menu = new JMenuItem(Lang.getInstance().translate("Vouch"));
        vouch_menu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                DCSet db = DCSet.getInstance();
                Transaction transaction = db.getTransactionFinalMap().get(itemTableSelected.getReference());
                new VouchRecordDialog(transaction.getBlockHeight(), transaction.getSeqNo());

            }
        });
        menuTable.add(vouch_menu);
        JMenuItem setVote_Menu = new JMenuItem(Lang.getInstance().translate("Voting"));
        setVote_Menu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                PollCls poll = (PollCls) (itemTableSelected);
                AssetCls AssetCls = DCSet.getInstance().getItemAssetMap().get((long) (1));
                new PollsDialog(poll, 0, AssetCls);
            }
        });
        menuTable.add(setVote_Menu);

        menuTable.addSeparator();

        JMenuItem setSeeInBlockexplorer = new JMenuItem(Lang.getInstance().translate("Check in Blockexplorer"));

        setSeeInBlockexplorer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                try {
                    URLViewer.openWebpage(new URL("http://" + Settings.getInstance().getBlockexplorerURL()
                            + ":" + Settings.getInstance().getWebPort() + "/index/blockexplorer.html"
                            + "?poll=" + itemTableSelected.getKey()));
                } catch (MalformedURLException e1) {
                    logger.error(e1.getMessage(), e1);
                }
            }
        });
        menuTable.add(setSeeInBlockexplorer);

    }

    // show details
    @Override
    public Component getShow(ItemCls item) {
        AssetCls AssetCls = DCSet.getInstance().getItemAssetMap().get((long) (1));
        PollsDetailPanel pollInfo = new PollsDetailPanel((PollCls) item, AssetCls);

        return pollInfo;
    }


}
