package org.erachain.gui.models;

import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.core.block.Block;
import org.erachain.database.DBMap;
import org.erachain.database.SortableList;
import org.erachain.gui.ObserverWaiter;
import org.erachain.utils.DateTimeFormat;
import org.erachain.utils.ObserverMessage;
import org.erachain.utils.Pair;
import org.mapdb.Fun.Tuple2;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Observable;

@SuppressWarnings("serial")
public abstract class WalletSortedTableModel<T, U> extends SortedListTableModelCls<T, U> implements ObserverWaiter {

    public WalletSortedTableModel(DBMap map, String[] columnNames, Boolean[] column_AutoHeight, boolean descending) {
        super(map, columnNames, column_AutoHeight, true);

        // сначала ЛОГЕР задаем
        logger = LoggerFactory.getLogger(this.getClass().getName());

        addObservers();
    }


    public void addObservers() {

        if (Controller.getInstance().doesWalletDatabaseExists()) {
            super.addObservers();
            map.addObserver(this);
        } else {
            // ожидаем открытия кошелька
            Controller.getInstance().wallet.addWaitingObserver(this);
        }
    }

    public void deleteObservers() {
        if (Controller.getInstance().doesWalletDatabaseExists()) {
            super.deleteObservers();
            map.deleteObserver(this);
        }
    }
}
