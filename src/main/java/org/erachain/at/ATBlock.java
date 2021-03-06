/*
 * Copyright (c) 2014 CIYAM Developers

 Distributed under the MIT/X11 software license, please refer to the file license.txt
 in the root project directory or http://www.opensource.org/licenses/mit-license.php.

 */

package org.erachain.at;

public class ATBlock {

    long totalFees;
    long totalAmount;
    byte[] bytesForBlock;
    boolean validated;

    ATBlock(long totalFees, long totalAmount, byte[] bytesForBlock) {
        this.totalFees = totalFees;
        this.totalAmount = totalAmount;
        this.bytesForBlock = bytesForBlock;
        this.validated = true;
    }

    ATBlock(long totalFees, long totalAmount, byte[] bytesForBlock, boolean validated) {
        this.totalFees = totalFees;
        this.totalAmount = totalAmount;
        this.bytesForBlock = bytesForBlock;
        this.validated = validated;
    }

    public long getTotalFees() {
        return totalFees;
    }

    public long getTotalAmount() {
        return totalAmount;
    }

    public byte[] getBytesForBlock() {
        return bytesForBlock;
    }

}
