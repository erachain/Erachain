package org.erachain.gui.exdata.exActions;

import org.erachain.core.exdata.exActions.ExAction;
import org.mapdb.Fun;

public interface ExActionPanelInt {
    Fun.Tuple2<ExAction, String> getResult();
}
