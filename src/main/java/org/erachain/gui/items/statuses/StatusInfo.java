package org.erachain.gui.items.statuses;

import org.erachain.core.item.statuses.StatusCls;
import org.erachain.gui.items.ItemInfo;
import org.erachain.lang.Lang;

import javax.swing.*;

// Info for status
public class StatusInfo extends ItemInfo {
    private static final long serialVersionUID = 476307470457045006L;

    public StatusInfo(StatusCls status) {
        super(status);

        JLabel singleLabel = new JLabel(Lang.T("Single") + ":");
        ++labelGBC.gridy;
        add(singleLabel, labelGBC);

        initFoot();

    }
}
