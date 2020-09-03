package org.erachain.gui.items.templates;

import org.erachain.core.item.ItemCls;
import org.erachain.core.item.templates.TemplateCls;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;
import org.erachain.gui.items.SearchItemSplitPanel;
import org.erachain.gui.records.VouchRecordDialog;
import org.erachain.lang.Lang;
import org.erachain.settings.Settings;
import org.erachain.utils.URLViewer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.net.URL;

@SuppressWarnings("serial")
public class SearchTemplatesSplitPanel extends SearchItemSplitPanel {

    public static String NAME = "SearchTemplatesSplitPanel";
    public static String TITLE = "Search Templates";

    private SearchTemplatesSplitPanel th;

    public SearchTemplatesSplitPanel() {
        super(new TemplatesItemsTableModel(), NAME, TITLE);

        this.th = this;

        JMenuItem vouch_Item = new JMenuItem(Lang.getInstance().translate("Vouch"));

        vouch_Item.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                TemplateCls template = (TemplateCls) itemTableSelected;
                if (template == null) return;
                Transaction trans = DCSet.getInstance().getTransactionFinalMap().get(template.getReference());
                new VouchRecordDialog(trans.getBlockHeight(), trans.getSeqNo());
            }
        });
        this.menuTable.add(vouch_Item);

        menuTable.addSeparator();

        JMenuItem setSeeInBlockexplorer = new JMenuItem(Lang.getInstance().translate("Check in Blockexplorer"));

        setSeeInBlockexplorer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                try {
                    URLViewer.openWebpage(new URL(Settings.getInstance().getBlockexplorerURL()
                            + "/index/blockexplorer.html"
                            + "?template=" + itemTableSelected.getKey()));
                } catch (MalformedURLException e1) {
                    logger.error(e1.getMessage(), e1);
                }
            }
        });
        menuTable.add(setSeeInBlockexplorer);

    }


    //show details
    @Override
    protected Component getShow(ItemCls item) {
        return new InfoTemplates((TemplateCls) item);

    }

}