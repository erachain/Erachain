package org.erachain.gui.transaction;

import org.erachain.core.transaction.*;
import org.erachain.gui.items.statement.RNoteInfo;

import javax.swing.*;

public class TransactionDetailsFactory {

    public static JPanel createTransactionDetail(Transaction transaction) {

        switch (transaction.getType()) {

            case Transaction.CALCULATED_TRANSACTION:
                return new RCalculatedDetailsFrame((RCalculated) transaction);

            case Transaction.SEND_ASSET_TRANSACTION:
                return new Send_RecordDetailsFrame((RSend) transaction);

            case Transaction.SIGN_NOTE_TRANSACTION:
                return new RNoteInfo((RSignNote) transaction);

            case Transaction.CREATE_POLL_TRANSACTION:
                return new CreatePollDetailsFrame((CreatePollTransaction) transaction);

            case Transaction.VOTE_ON_POLL_TRANSACTION:
                return new VoteOnPollDetailsFrame((VoteOnPollTransaction) transaction);

            case Transaction.VOTE_ON_ITEM_POLL_TRANSACTION:
                return new VoteOnItemPollDetailsFrame((VoteOnItemPollTransaction) transaction);

            case Transaction.ARBITRARY_TRANSACTION:
                return new ArbitraryTransactionDetailsFrame((ArbitraryTransaction) transaction);

            case Transaction.ISSUE_ASSET_TRANSACTION:
                return new IssueAssetDetailsFrame((IssueAssetTransaction) transaction);

            case Transaction.ISSUE_PERSON_TRANSACTION:
                return new IssuePersonDetailsFrame((IssuePersonRecord) transaction);

            case Transaction.ISSUE_POLL_TRANSACTION:
                return new IssuePollDetailsFrame((IssuePollRecord) transaction);

            case Transaction.SET_STATUS_TO_ITEM_TRANSACTION:
                return new SetStatusToItemDetailsFrame((RSetStatusToItem) transaction);

            case Transaction.CREATE_ORDER_TRANSACTION:
                return new CreateOrderDetailsFrame((CreateOrderTransaction) transaction);

            case Transaction.CANCEL_ORDER_TRANSACTION:
                return new CancelOrderDetailsFrame((CancelOrderTransaction) transaction);

            case Transaction.CHANGE_ORDER_TRANSACTION:
                return new UpdateOrderDetailsFrame((ChangeOrderTransaction) transaction);

            case Transaction.ISSUE_ASSET_SERIES_TRANSACTION:
                return new IssueAssetSeriesDetailsFrame((IssueAssetSeriesTransaction) transaction);

            case Transaction.MULTI_PAYMENT_TRANSACTION:
                return new MultiPaymentDetailsFrame((MultiPaymentTransaction) transaction);

            case Transaction.SIGN_TRANSACTION:
                return new SigningDetailsFrame((RVouch) transaction);

            case Transaction.CERTIFY_PUB_KEYS_TRANSACTION:
                return new CertifyPubKeysDetailsFrame((RCertifyPubKeys) transaction);

            case Transaction.HASHES_RECORD:
                return new HashesDetailsFrame((RHashes) transaction);

            case Transaction.ISSUE_IMPRINT_TRANSACTION:
                return new IssueImprintDetailsFrame((IssueImprintRecord) transaction);

            case Transaction.ISSUE_TEMPLATE_TRANSACTION:
                return new IssueTemplateDetailsFrame((IssueTemplateRecord) transaction);

            case Transaction.ISSUE_UNION_TRANSACTION:
                return new IssueUnionDetailsFrame((IssueUnionRecord) transaction);

            case Transaction.ISSUE_STATUS_TRANSACTION:
                return new IssueStatusDetailsFrame((IssueStatusRecord) transaction);

            case Transaction.GENESIS_SEND_ASSET_TRANSACTION:
                return new GenesisTransferAssetDetailsFrame((GenesisTransferAssetTransaction) transaction);

            case Transaction.GENESIS_ISSUE_TEMPLATE_TRANSACTION:
                return new GenesisIssueTemplateDetailsFrame((GenesisIssueTemplateRecord) transaction);

            case Transaction.GENESIS_ISSUE_ASSET_TRANSACTION:
                return new GenesisIssueAssetDetailsFrame((GenesisIssueAssetTransaction) transaction);

            case Transaction.GENESIS_ISSUE_PERSON_TRANSACTION:
                return new GenesisCertifyPersonRecordFrame((GenesisCertifyPersonRecord) transaction);
        }

        return new JPanel();
    }
}
