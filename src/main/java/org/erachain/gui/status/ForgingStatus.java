package org.erachain.gui.status;
// 16/03

import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.core.BlockGenerator;
import org.erachain.core.account.Account;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;
import org.erachain.lang.Lang;
import org.erachain.utils.GUIUtils;
import org.erachain.utils.ObserverMessage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.TimeUnit;

;

@SuppressWarnings("serial")
public class ForgingStatus extends JLabel implements Observer {

    private ImageIcon forgingDisabledIcon;
    private ImageIcon forgingEnabledIcon;
    private ImageIcon forgingIcon;
    private ImageIcon forgingWaitIcon;

    public ForgingStatus() {
        super();

        //CREATE ICONS
        this.forgingDisabledIcon = this.createIcon(Color.RED);
        this.forgingEnabledIcon = this.createIcon(Color.ORANGE);
        this.forgingWaitIcon = this.createIcon(Color.MAGENTA);
        this.forgingIcon = this.createIcon(Color.GREEN);

        //TOOLTIP
        ToolTipManager.sharedInstance().setDismissDelay((int) TimeUnit.SECONDS.toMillis(3));
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent mEvt) {

                long winBalance = 0;
                long winBalance2 = 0;
                Account winAccount = null;
                BlockChain bchain = Controller.getInstance().getBlockChain();
                // List<Block> lastBlocksForTarget = bchain.getLastBlocksForTarget(DCSet.getInstance());
                int newHeight = bchain.getHeight(DCSet.getInstance()) + 1;
                long target = bchain.getTarget(DCSet.getInstance());
                if (target == 0l)
                    target = 1000l;

                DCSet dcSet = DCSet.getInstance();
                for (Account account : Controller.getInstance().getWalletAccounts()) {
                    long win_value = BlockChain.calcWinValue(dcSet, account, newHeight, account.getBalanceUSE(Transaction.RIGHTS_KEY, dcSet).intValue(), null);
                    if (Math.abs(win_value) > winBalance) {
                        winBalance = Math.abs(win_value);
                        winAccount = account;
                        winBalance2 = win_value;
                    }
                }
                ///

                String timeForge = "";
                if (winAccount != null) {
                    //timeForge = getTimeToGoodView((60*5+19)*Controller.getInstance().getLastBlock().getGeneratingBalance()/totalBalanceInt);
                    timeForge = new BigDecimal(BlockChain.calcWinValueTargetedBase(dcSet, newHeight, winBalance, target)).movePointLeft(3).toPlainString();
                    //timeForge = "" + (BlockChain.BASE_TARGET * winBalance / target);
                    timeForge = winBalance2 > 0 ? timeForge + "%" : ("(" + winBalance2 + ")");
                    timeForge = timeForge + " " + winAccount.getAddress();
                    timeForge = Lang.T("Won data for forging: %timeForge%.").replace("%timeForge%", timeForge);
                } else {
                    timeForge = Lang.T("infinity");
                }


                if (Controller.getInstance().getForgingStatus() == BlockGenerator.ForgingStatus.FORGING) {
                    setToolTipText(timeForge);
                } else if (Controller.getInstance().getForgingStatus() == BlockGenerator.ForgingStatus.FORGING_DISABLED && Controller.getInstance().getStatus() == Controller.STATUS_OK) {
                    setToolTipText(Lang.T("To start forging you need to unlock the wallet."
                            + " " + timeForge));
                } else if (Controller.getInstance().getForgingStatus() == BlockGenerator.ForgingStatus.FORGING_WAIT && Controller.getInstance().getStatus() == Controller.STATUS_OK) {
                    setToolTipText(Lang.T("To start forging need await SYNC peer.")
                            + " " + timeForge);
                } else {
                    setToolTipText(Lang.T("For forging wallet must be online and fully synchronized.")
                            + " " + timeForge);
                }

            }
        });

        //LISTEN ON STATUS
        Controller.getInstance().addObserver(this);
        setIconAndText(Controller.getInstance().getForgingStatus());
    }

    public static String getTimeToGoodView(long intdif) {
        String result = "+ ";
        long diff = intdif * 1000;
        final int ONE_DAY = 1000 * 60 * 60 * 24;
        final int ONE_HOUR = ONE_DAY / 24;
        final int ONE_MINUTE = ONE_HOUR / 60;
        final int ONE_SECOND = ONE_MINUTE / 60;

        long d = diff / ONE_DAY;
        diff %= ONE_DAY;

        long h = diff / ONE_HOUR;
        diff %= ONE_HOUR;

        long m = diff / ONE_MINUTE;
        diff %= ONE_MINUTE;

        long s = diff / ONE_SECOND;
        //long ms = diff % ONE_SECOND;

        if (d > 0) {
            result += d > 1 ? d + " " + Lang.T("days") + " " : d + " " + Lang.T("day") + " ";
        }

        if (h > 0 && d < 5) {
            result += h > 1 ? h + " " + Lang.T("hours") + " " : h + " " + Lang.T("hour") + " ";
        }

        if (m > 0 && d == 0 && h < 10) {
            result += m > 1 ? m + " " + Lang.T("mins") + " " : m + " " + Lang.T("min") + " ";
        }

        if (s > 0 && d == 0 && h == 0 && m < 15) {
            result += s > 1 ? s + " " + Lang.T("secs") + " " : s + " " + Lang.T("sec") + " ";
        }

        return result.substring(0, result.length() - 1);
    }

    private ImageIcon createIcon(Color color) {
        return GUIUtils.createIcon(getFont().getSize(), color, this.getBackground());
    }

    @Override
    public void update(Observable arg0, Object arg1) {
        ObserverMessage message = (ObserverMessage) arg1;

        if (message.getType() == ObserverMessage.FORGING_STATUS) {
            BlockGenerator.ForgingStatus status = (BlockGenerator.ForgingStatus) message.getValue();

            setIconAndText(status);
        }
    }

    private void setIconAndText(BlockGenerator.ForgingStatus status) {
        if (status == BlockGenerator.ForgingStatus.FORGING_DISABLED) {
            forgingDisabled();
        } else if (status == BlockGenerator.ForgingStatus.FORGING_ENABLED) {
            this.setIcon(forgingEnabledIcon);
            this.setText(status.getName());
        } else if (status == BlockGenerator.ForgingStatus.FORGING_WAIT) {
            this.setIcon(forgingWaitIcon);
            this.setText(status.getName());
        } else if (status == BlockGenerator.ForgingStatus.FORGING) {
            this.setIcon(forgingIcon);
            this.setText(status.getName());
        }
    }

    public void forgingDisabled() {
        this.setIcon(forgingDisabledIcon);
        this.setText(BlockGenerator.ForgingStatus.FORGING_DISABLED.getName());
    }


}
