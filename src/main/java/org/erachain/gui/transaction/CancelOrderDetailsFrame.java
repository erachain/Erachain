package org.erachain.gui.transaction;

import org.erachain.core.transaction.CancelOrderTransaction;

@SuppressWarnings("serial")
public class CancelOrderDetailsFrame extends Rec_DetailsFrame {
    public CancelOrderDetailsFrame(CancelOrderTransaction orderCreation) {
        super(orderCreation);

        //PACK
//		this.pack();
        //       this.setResizable(false);
//        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }
}
