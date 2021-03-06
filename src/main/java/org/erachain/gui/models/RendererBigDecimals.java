package org.erachain.gui.models;

import org.erachain.gui.WalletTableRenderer;

import javax.swing.*;
import java.math.BigDecimal;

/**
 * @author Ermolaev Alexander
 * @see JTable
 */
public class RendererBigDecimals extends WalletTableRenderer {

    private int scale;

    /**
     * Creates a BigDecimal table cell renderer.
     ** @param scale the scale digits view
     */
    public RendererBigDecimals(Integer scale) {
        super();
        this.scale = scale;
        setHorizontalAlignment(JLabel.RIGHT);
    }


    /**
     * Sets the <code>String</code> object for the cell being rendered to
     * <code>value</code>.
     *
     * @param value  the string value for this cell; if value is
     *          <code>null</code> it sets the text value to an empty string
     * @see JLabel#setText
     *
     */
    @Override
    protected void setValue(Object value) {

        try {
            setText((value == null) ? "" :
                    value instanceof BigDecimal ? ((BigDecimal) value).setScale(scale).toString() : value.toString());
        } catch (Exception e) {
            e.printStackTrace();
            setText("");
        }
    }
}
