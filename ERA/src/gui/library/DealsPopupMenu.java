package gui.library;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import core.BlockChain;
import core.account.PublicKeyAccount;
import core.crypto.Base32;
import core.item.assets.AssetCls;
import gui.items.accounts.Account_Confiscate_Debt_Dialog;
import gui.items.accounts.Account_Lend_Dialog;
import gui.items.accounts.Account_Repay_Debt_Dialog;
import gui.items.accounts.Account_Send_Dialog;
import gui.items.accounts.Account_Set_Name_Dialog;
import gui.items.accounts.Account_Take_Hold_Dialog;
import gui.models.AccountsTableModel;
import lang.Lang;

public class DealsPopupMenu extends JPopupMenu {
    
    public AccountsTableModel tableModel;
    protected JComboBox<AssetCls> assetSelector;
    protected AssetCls asset;
    protected PublicKeyAccount pubKey;
    protected MTable table;
    
    private JMenuItem sendAsset;
    private JMenuItem debtAsset;
    private JMenuItem debtAssetReturn;
    private JMenuItem debtAssetBackward;
    private JMenuItem holdAsset;
        
    public DealsPopupMenu(AccountsTableModel tableModel, MTable table, JComboBox<AssetCls> assetSelector) {
        super();
        
        this.tableModel = tableModel;
        this.table = table;
        this.assetSelector = assetSelector;
        
        sendAsset = new JMenuItem();
        sendAsset.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new Account_Send_Dialog(asset, pubKey, null, null);

            }
        });
        this.add(sendAsset);

        this.addSeparator();

        debtAsset = new JMenuItem(Lang.getInstance().translate("Lend"));
        debtAsset.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new Account_Lend_Dialog(asset, pubKey);

            }
        });
        this.add(debtAsset);

        debtAssetReturn = new JMenuItem(Lang.getInstance().translate("Repay Debt"));
        debtAssetReturn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new Account_Repay_Debt_Dialog(asset, pubKey);
            }
        });
        this.add(debtAssetReturn);

        // asset != null && asset.isOutsideType()? "Подтвердить погашение требования" : "Confiscate Debt")
        debtAssetBackward = new JMenuItem(Lang.getInstance().translate("Confiscate Debt"));
        debtAssetBackward.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new Account_Confiscate_Debt_Dialog(asset, pubKey);
            }
        });
        this.add(debtAssetBackward);

        this.addSeparator();

        holdAsset = new JMenuItem(Lang.getInstance().translate("Hold"));
        holdAsset.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new Account_Take_Hold_Dialog(asset, pubKey);

            }
        });
        this.add(holdAsset);

        this.addSeparator();

        JMenuItem copyAddress = new JMenuItem(Lang.getInstance().translate("Copy Account"));
        copyAddress.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //      int row = table.getSelectedRow();
                //      if (row < 1 ) return;

                //      row = table.convertRowIndexToModel(row);
                //      Account account = tableModel.getAccount(row);

                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                StringSelection value = new StringSelection(pubKey.getAddress());
                clipboard.setContents(value, null);
            }
        });
        this.add(copyAddress);

        JMenuItem copyBalance = new JMenuItem(Lang.getInstance().translate("Copy Balance"));
        copyBalance.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                //long key = cbxFavorites.getItemAt(cbxFavorites.getSelectedIndex()).getKey();
                long key = asset.getKey();
                StringSelection value = new StringSelection(pubKey.getBalance(key).toString());
                clipboard.setContents(value, null);
            }
        });

        this.add(copyBalance);
        
        this.addSeparator();
        
        JMenuItem copyPublicKey = new JMenuItem(Lang.getInstance().translate("Copy Public Key"));
        copyPublicKey.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                StringSelection value = new StringSelection(pubKey.getBase58());
                clipboard.setContents(value, null);
            }
        });
        this.add(copyPublicKey);

        JMenuItem copyBankKey = new JMenuItem(Lang.getInstance().translate("Copy Public Key for BANK"));
        copyBankKey.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String bankKeyAccount = "+" + Base32.encode(pubKey.getPublicKey());
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                StringSelection value = new StringSelection(bankKeyAccount);
                clipboard.setContents(value, null);
            }
        });
        this.add(copyBankKey);

        JMenuItem set_name = new JMenuItem(Lang.getInstance().translate("Edit Name"));
        set_name.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new Account_Set_Name_Dialog(pubKey.getAddress());
                table.repaint();
            }
        });
        this.add(set_name);
        
        this.addPopupMenuListener(new PopupMenuListener() {

            @Override
            public void popupMenuCanceled(PopupMenuEvent arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent arg0) {
                // TODO Auto-generated method stub

                // TODO Auto-generated method stub

                asset = (AssetCls) assetSelector.getSelectedItem();

                int row = table.getSelectedRow();
                if (row < 0)
                    return;
                row = table.convertRowIndexToModel(row);
                
                pubKey = tableModel.getPublicKeyAccount(row);
                init();

            }


        });

    }
    
    public void init() {
        
        switch (this.asset.getAssetType()) {
            case AssetCls.AS_OUTSIDE_GOODS:
                this.sendAsset.setText(Lang.getInstance().translate("Передать в собственность"));
                this.holdAsset.setText(Lang.getInstance().translate("Подтвердить прием \"на руки\""));

                // поидее тут как ЛИЗИНГ
                this.debtAsset.setVisible(false);
                this.debtAssetReturn.setVisible(false);
                this.debtAssetBackward.setVisible(false);
                
                break;
                
            case AssetCls.AS_INSIDE_ASSETS:
                this.sendAsset.setText(Lang.getInstance().translate("Передать в собственность"));
                this.holdAsset.setText(Lang.getInstance().translate("Учесть прием на баланс"));

                this.debtAsset.setText(Lang.getInstance().translate("Передать в долг"));
                this.debtAsset.setVisible(true);
                this.debtAssetReturn.setText(Lang.getInstance().translate("Вернуть долг"));
                this.debtAssetReturn.setVisible(true);
                this.debtAssetBackward.setText(Lang.getInstance().translate("Конфисковать долг"));
                this.debtAssetBackward.setVisible(true);

                break;
                
            case AssetCls.AS_OUTSIDE_IMMOVABLE:
                this.sendAsset.setText(Lang.getInstance().translate("Передать в собственность"));
                if(BlockChain.DEVELOP_USE)
                    this.holdAsset.setText(Lang.getInstance().translate("Прием на охрану/получено из охраны"));

                this.debtAsset.setText(Lang.getInstance().translate("Пепедать в аренду"));
                this.debtAsset.setVisible(true);
                this.debtAssetReturn.setText(Lang.getInstance().translate("Изъять из аренды"));
                this.debtAssetReturn.setVisible(true);
                this.debtAssetBackward.setText(Lang.getInstance().translate("Вернуть из аренды"));
                this.debtAssetBackward.setVisible(true);

                break;
                
            case AssetCls.AS_OUTSIDE_CURRENCY:
                this.sendAsset.setText(Lang.getInstance().translate("Передать требование платежа"));
                this.holdAsset.setText(Lang.getInstance().translate("Учесть прием на баланс"));

                this.debtAsset.setText(Lang.getInstance().translate("Потребовать погашение"));
                this.debtAsset.setVisible(true);
                this.debtAssetReturn.setVisible(false);
                this.debtAssetBackward.setText(Lang.getInstance().translate("Подтвердить погашение"));
                this.debtAssetBackward.setVisible(true);

                break;
                
            case AssetCls.AS_OUTSIDE_SERVICE:
                this.sendAsset.setText(Lang.getInstance().translate("Передать требование услуги"));
                this.holdAsset.setText(Lang.getInstance().translate("Учесть прием на баланс"));

                this.debtAsset.setText(Lang.getInstance().translate("Потребовать предоставление услуги"));
                this.debtAsset.setVisible(true);
                this.debtAssetReturn.setVisible(false);
                this.debtAssetBackward.setText(Lang.getInstance().translate("Подтвердить предоставление услуги"));
                this.debtAssetBackward.setVisible(true);

                break;
                
            case AssetCls.AS_OUTSIDE_SHARE:
                this.sendAsset.setText(Lang.getInstance().translate("Передать акции в собственность"));
                this.holdAsset.setText(Lang.getInstance().translate("Учесть прием на баланс"));

                this.debtAsset.setText(Lang.getInstance().translate("Потребовать передачу акций"));
                this.debtAsset.setVisible(true);
                this.debtAssetReturn.setVisible(false);
                this.debtAssetBackward.setText(Lang.getInstance().translate("Подтвердить получение акций"));
                this.debtAssetBackward.setVisible(true);

                break;
                
            case AssetCls.AS_OUTSIDE_BILL:
                this.sendAsset.setText(Lang.getInstance().translate("Передать вексель в собственность"));
                this.holdAsset.setText(Lang.getInstance().translate("Подтвердить получение выплаты?"));  //"Учесть прием на баланс"));

                this.debtAsset.setText(Lang.getInstance().translate("Потребовать погашение векселя!"));
                this.debtAsset.setVisible(true);
                this.debtAssetReturn.setText(Lang.getInstance().translate("Отозвать погашение векселя?"));
                this.debtAssetReturn.setVisible(true);
                //this.debtAssetBackward.setText(Lang.getInstance().translate("Подтвердить получение выплаты"));
                this.debtAssetBackward.setText(Lang.getInstance().translate("Отменить погашение векселя?"));
                this.debtAssetBackward.setVisible(true);

                break;
                
            case AssetCls.AS_OUTSIDE_BILL_EX:
                this.sendAsset.setText(Lang.getInstance().translate("Передать вексель в собственность"));
                this.holdAsset.setText(Lang.getInstance().translate("Подтвердить получение выплаты?"));  //"Учесть прием на баланс"));

                this.debtAsset.setText(Lang.getInstance().translate("Потребовать погашение векселя!"));
                this.debtAsset.setVisible(true);
                this.debtAssetReturn.setText(Lang.getInstance().translate("Отозвать погашение векселя?"));
                this.debtAssetReturn.setVisible(true);
                //this.debtAssetBackward.setText(Lang.getInstance().translate("Подтвердить получение выплаты"));
                this.debtAssetBackward.setText(Lang.getInstance().translate("Отменить погашение векселя?"));
                this.debtAssetBackward.setVisible(true);

                break;
                
            case AssetCls.AS_OUTSIDE_OTHER_CLAIM:
                this.sendAsset.setText(Lang.getInstance().translate("Передать в собственность требование"));
                this.holdAsset.setText(Lang.getInstance().translate("Учесть прием требования на баланс"));

                this.debtAsset.setText(Lang.getInstance().translate("Потребовать исполнения своего права"));
                this.debtAsset.setVisible(true);
                this.debtAssetReturn.setVisible(false);
                this.debtAssetBackward.setText(Lang.getInstance().translate("Подтвердить исполнение своего права"));
                this.debtAssetBackward.setVisible(true);

                break;

            case AssetCls.AS_INSIDE_CURRENCY:
                this.sendAsset.setText(Lang.getInstance().translate("Певести в собственность деньги"));
                this.holdAsset.setText(Lang.getInstance().translate("Учесть прием денег на баланс"));

                this.debtAsset.setText(Lang.getInstance().translate("Передать в долг"));
                this.debtAsset.setVisible(true);
                this.debtAssetReturn.setText(Lang.getInstance().translate("Вернуть долг"));
                this.debtAssetReturn.setVisible(true);
                this.debtAssetBackward.setText(Lang.getInstance().translate("Конфисковать долг"));
                this.debtAssetBackward.setVisible(true);

                break;

            case AssetCls.AS_INSIDE_UTILITY:
                this.sendAsset.setText(Lang.getInstance().translate("Передать в собственность действе"));
                this.holdAsset.setText(Lang.getInstance().translate("Учесть прием на баланс"));

                this.debtAsset.setText(Lang.getInstance().translate("Передать в долг"));
                this.debtAsset.setVisible(true);
                this.debtAssetReturn.setText(Lang.getInstance().translate("Вернуть долг"));
                this.debtAssetReturn.setVisible(true);
                this.debtAssetBackward.setText(Lang.getInstance().translate("Конфисковать долг"));
                this.debtAssetBackward.setVisible(true);

                break;

            case AssetCls.AS_INSIDE_SHARE:
                this.sendAsset.setText(Lang.getInstance().translate("Передать в собственность акции"));
                this.holdAsset.setText(Lang.getInstance().translate("Учесть прием на баланс"));

                this.debtAsset.setText(Lang.getInstance().translate("Передать в долг"));
                this.debtAsset.setVisible(true);
                this.debtAssetReturn.setText(Lang.getInstance().translate("Вернуть долг"));
                this.debtAssetReturn.setVisible(true);
                this.debtAssetBackward.setText(Lang.getInstance().translate("Конфисковать долг"));
                this.debtAssetBackward.setVisible(true);

                break;

            case AssetCls.AS_INSIDE_BONUS:
                this.sendAsset.setText(Lang.getInstance().translate("Начислить бонусы"));
                this.holdAsset.setText(Lang.getInstance().translate("Учесть прием на баланс"));

                this.debtAsset.setText(Lang.getInstance().translate("Передать в долг"));
                this.debtAsset.setVisible(true);
                this.debtAssetReturn.setText(Lang.getInstance().translate("Вернуть долг"));
                this.debtAssetReturn.setVisible(true);
                this.debtAssetBackward.setText(Lang.getInstance().translate("Конфисковать долг"));
                this.debtAssetBackward.setVisible(true);

                break;

            case AssetCls.AS_INSIDE_ACCESS:
                this.sendAsset.setText(Lang.getInstance().translate("Наделить правами"));
                this.holdAsset.setText(Lang.getInstance().translate("Учесть прием на баланс"));

                this.debtAsset.setText(Lang.getInstance().translate("Передать в долг"));
                this.debtAsset.setVisible(true);
                this.debtAssetReturn.setText(Lang.getInstance().translate("Вернуть долг"));
                this.debtAssetReturn.setVisible(true);
                this.debtAssetBackward.setText(Lang.getInstance().translate("Конфисковать долг"));
                this.debtAssetBackward.setVisible(true);

                break;

            case AssetCls.AS_INSIDE_VOTE:
                this.sendAsset.setText(Lang.getInstance().translate("Наделить голосом"));
                //this.holdAsset.setText(Lang.getInstance().translate("Учесть прием на баланс"));

                this.debtAsset.setText(Lang.getInstance().translate("Передать делегату"));
                this.debtAsset.setVisible(true);
                this.debtAssetReturn.setText(Lang.getInstance().translate("Вернуть делегированный голос"));
                this.debtAssetReturn.setVisible(true);
                this.debtAssetBackward.setText(Lang.getInstance().translate("Забрать у делегата"));
                this.debtAssetBackward.setVisible(true);

                break;

            case AssetCls.AS_INDEX:
                this.sendAsset.setText(Lang.getInstance().translate("Певести в собственность индекс"));
                //this.holdAsset.setText(Lang.getInstance().translate("Учесть прием индекса на баланс"));
                this.holdAsset.setVisible(false);

                //this.debtAsset.setText(Lang.getInstance().translate("Передать в долг"));
                this.debtAsset.setVisible(false);
                //this.debtAssetReturn.setText(Lang.getInstance().translate("Вернуть долг"));
                this.debtAssetReturn.setVisible(false);
                //this.debtAssetBackward.setText(Lang.getInstance().translate("Конфисковать долг"));
                this.debtAssetBackward.setVisible(false);

                break;

            case AssetCls.AS_INSIDE_OTHER_CLAIM:
                this.sendAsset.setText(Lang.getInstance().translate("Передать в собственность требование"));
                this.holdAsset.setText(Lang.getInstance().translate("Учесть прием требования на баланс"));

                this.debtAsset.setText(Lang.getInstance().translate("Передать в долг"));
                this.debtAsset.setVisible(true);
                this.debtAssetReturn.setText(Lang.getInstance().translate("Вернуть долг"));
                this.debtAssetReturn.setVisible(true);
                this.debtAssetBackward.setText(Lang.getInstance().translate("Конфисковать долг"));
                this.debtAssetBackward.setVisible(true);

                break;

        }
        
        //Component comp1 = this.getComponent(3);
        //comp1.setEnabled(false);
    }


}
