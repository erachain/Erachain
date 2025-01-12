package org.erachain.core.transaction.dto;

import lombok.Data;
import org.erachain.core.account.Account;

import java.math.BigDecimal;

@Data
public class TransferRecipientDto {
    private final Account account;
    private final BigDecimal value;
    private final int balancePos;
}
