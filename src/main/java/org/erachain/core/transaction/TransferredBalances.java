package org.erachain.core.transaction;

import org.erachain.core.transaction.dto.TransferBalanceDto;

/**
 * Указывает на то что есть переведенные балансы между разными счетами
 */
public interface TransferredBalances {
    TransferBalanceDto[] getTransfers();
}
