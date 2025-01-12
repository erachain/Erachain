package org.erachain.core.transaction.dto;

import lombok.Data;
import org.erachain.core.account.Account;
import org.erachain.core.item.assets.AssetCls;

@Data
public class TransferBalanceDto {
    private final Account sender;
    private final AssetCls asset;
    private final int position;
    private final boolean backward;
    private final TransferRecipientDto[] recipients;
}
