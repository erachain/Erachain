package org.erachain.gui.items.templates;

import org.erachain.core.item.ItemCls;
import org.erachain.core.item.templates.TemplateCls;
import org.erachain.gui.items.SearchItemSplitPanel;

import java.awt.*;

@SuppressWarnings("serial")
public class SearchTemplatesSplitPanel extends SearchItemSplitPanel {

    public static String NAME = "SearchTemplatesSplitPanel";
    public static String TITLE = "Search Templates";

    private SearchTemplatesSplitPanel th;

    public SearchTemplatesSplitPanel() {
        super(new TemplatesItemsTableModel(), NAME, TITLE);

        this.th = this;

        /*
        JMenuItem vouch_Item = new JMenuItem(Lang.T("Sign / Vouch"));

        vouch_Item.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                TemplateCls template = (TemplateCls) itemTableSelected;
                if (template == null) return;
                Transaction trans = DCSet.getInstance().getTransactionFinalMap().get(template.getReference());
                new toSignRecordDialog(trans.getBlockHeight(), trans.getSeqNo());
            }
        });
        this.menuTable.add(vouch_Item);

         */

    }


    //show details
    @Override
    protected Component getShow(ItemCls item) {
        return new InfoTemplates((TemplateCls) item);

    }

}