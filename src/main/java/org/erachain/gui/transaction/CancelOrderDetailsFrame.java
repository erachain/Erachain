package org.erachain.gui.transaction;

import org.erachain.core.transaction.CancelOrderTransaction;

@SuppressWarnings("serial")
public class CancelOrderDetailsFrame extends RecDetailsFrame {
    public CancelOrderDetailsFrame(CancelOrderTransaction orderCreation) {
        super(orderCreation);

        //PACK
//		this.pack();
        //       this.setResizable(false);
//        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }
}
