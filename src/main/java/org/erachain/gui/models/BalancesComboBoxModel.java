package org.erachain.gui.models;

import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.core.account.Account;
import org.erachain.datachain.ItemAssetBalanceMap;
import org.erachain.utils.ObserverMessage;
import org.erachain.utils.Pair;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple5;

import javax.swing.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

@SuppressWarnings("serial")
public class BalancesComboBoxModel extends DefaultComboBoxModel<Pair<Tuple2<String, Long>,
        Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>>> implements Observer {

    private List<Tuple2<byte[], Tuple5<
            Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>,
            Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>>>
            balances;

    Account account;

    public BalancesComboBoxModel(Account account) {
        this.account = account;

        Controller.getInstance().addObserver(this);
        this.balances = Controller.getInstance().getBalances(account);

        this.update();
    }

    @Override
    public void update(Observable o, Object arg) {
        try {
            this.syncUpdate(o, arg);
        } catch (Exception e) {
            //GUI ERROR
        }
    }

    private synchronized void syncUpdate(Observable o, Object arg) {
        ObserverMessage message = (ObserverMessage) arg;

        if ((message.getType() == ObserverMessage.NETWORK_STATUS && (int) message.getValue() == Controller.STATUS_OK)
                || (Controller.getInstance().getStatus() == Controller.STATUS_OK &&
                (message.getType() == ObserverMessage.ADD_BALANCE_TYPE || message.getType() == ObserverMessage.REMOVE_BALANCE_TYPE))
        ) {
            this.update();
        }
    }

    private void update() {

        //EMPTY LIST
        this.removeAllElements();

        //INSERT ALL ACCOUNTS
        for (int i = 0; i < this.balances.size(); i++) {
            Tuple2<byte[], Tuple5<
                    Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>,
                    Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>>
                    item = this.balances.get(i);

            long assetKey = ItemAssetBalanceMap.getAssetKeyFromKey(item.a);
            Tuple5<
                    Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>,
                    Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>> balance = item.b;
            if (BlockChain.ERA_COMPU_ALL_UP) {
                balance = account.balanceAddDEVAmount(assetKey, balance);
            }

            this.addElement(new Pair(new Tuple2(account, assetKey), balance));
        }

    }

    public void removeObservers() {
        Controller.getInstance().deleteObserver(this);
    }
}
