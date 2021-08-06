package org.erachain.gui.models;

import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.item.templates.TemplateCls;
import org.erachain.lang.Lang;
import org.erachain.utils.ObserverMessage;

import javax.swing.table.AbstractTableModel;
import javax.validation.constraints.Null;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

@SuppressWarnings("serial")
public class AccountStatementsTableModel extends AbstractTableModel implements Observer {
    public static final int COLUMN_TEMPLATE_KEY = 1;
    public static final int COLUMN_TEMPLATE_NAME = 2;
    public static final int COLUMN_TEXT = 3;
    private static final int COLUMN_ADDRESS = 0;
    private String[] columnNames = Lang.T(new String[]{"Account", "Template Key", "Template Name", "Own Text"});
    private List<PublicKeyAccount> publicKeyAccounts;
    private TemplateCls template;

    public AccountStatementsTableModel() {
        this.publicKeyAccounts = Controller.getInstance().getWalletPublicKeyAccounts();
        Controller.getInstance().addWalletObserver(this);
        Controller.getInstance().addObserver(this);
    }


    @Override
    public Class<? extends Object> getColumnClass(int c) {     // set column type
        Object o = getValueAt(0, c);
        return o == null ? Null.class : o.getClass();
    }


    public Account getAccount(int row) {
        return publicKeyAccounts.get(row);
    }

    public PublicKeyAccount getPublicKeyAccount(int row) {
        return publicKeyAccounts.get(row);
    }

    public void setAsset(TemplateCls template) {
        this.template = template;
        this.fireTableDataChanged();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int index) {
        return columnNames[index];
    }

    @Override
    public int getRowCount() {
        return this.publicKeyAccounts.size();
    }

    @Override
    public Object getValueAt(int row, int column) {
        if (this.publicKeyAccounts == null || row > this.publicKeyAccounts.size() - 1) {
            return null;
        }

        Account account = this.publicKeyAccounts.get(row);

        switch (column) {
            case COLUMN_ADDRESS:
                return account.getPersonAsString();
            case COLUMN_TEMPLATE_KEY:
                if (this.template == null) return "-";
                return this.template.getKey();
            case COLUMN_TEMPLATE_NAME:
                if (this.template == null) return "-";
                return this.template.viewName();
            case COLUMN_TEXT:
                if (this.template == null) return "-";
                return "+";


			/*

		case COLUMN_GENERATING_BALANCE:

			if(this.asset == null || this.asset.getKey() == AssetCls.FEE_KEY)
			{
				return  NumberAsString.getInstance().numberAsString(account.getGeneratingBalance());
			}
			else
			{
				return NumberAsString.getInstance().numberAsString(BigDecimal.ZERO);
			}
			 */

        }

        return null;
    }

    @Override
    public void update(Observable o, Object arg) {
        try {
            this.syncUpdate(o, arg);
        } catch (Exception e) {
            //GUI ERROR
        }
    }

    public synchronized void syncUpdate(Observable o, Object arg) {
        ObserverMessage message = (ObserverMessage) arg;

        if (message.getType() == ObserverMessage.NETWORK_STATUS && (int) message.getValue() == Controller.STATUS_OK) {

            this.fireTableRowsUpdated(0, this.getRowCount() - 1);

        } else if (Controller.getInstance().getStatus() == Controller.STATUS_OK) {

            if (message.getType() == ObserverMessage.WALLET_ADD_BLOCK_TYPE || message.getType() == ObserverMessage.WALLET_REMOVE_BLOCK_TYPE
                    || message.getType() == ObserverMessage.WALLET_ADD_TRANSACTION_TYPE || message.getType() == ObserverMessage.WALLET_REMOVE_TRANSACTION_TYPE) {
                this.publicKeyAccounts = Controller.getInstance().getWalletPublicKeyAccounts();

                this.fireTableRowsUpdated(0, this.getRowCount() - 1);  // WHEN UPDATE DATA - SELECTION DOES NOT DISAPPEAR
            }

            if (message.getType() == ObserverMessage.ADD_ACCOUNT_TYPE
                    || message.getType() == ObserverMessage.REMOVE_ACCOUNT_TYPE
                //|| message.getType() == ObserverMessage.ADD_STATEMENT_TYPE
                //|| message.getType() == ObserverMessage.REMOVE_STATEMENT_TYPE
                    ) {
                this.fireTableDataChanged();
            }
        }
    }

}
