package org.erachain.gui.transaction;

import org.erachain.core.transaction.*;
import org.erachain.gui.items.statement.RNoteInfo;

import javax.swing.*;
import java.awt.*;

public class TransactionDetailsFactory {
    private static TransactionDetailsFactory instance;

    public static TransactionDetailsFactory getInstance() {
        if (instance == null) {
            instance = new TransactionDetailsFactory();
        }

        return instance;
    }

    private void TransactionDetailsFactory() {

    }

    public JPanel createTransactionDetail(Transaction transaction) {

        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        JLabel jLabel9 = new JLabel("");

        gridBagConstraints.gridx = 0;

        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;

        switch (transaction.getType()) {

            case Transaction.CALCULATED_TRANSACTION:
                RCalculated r_Calc = (RCalculated) transaction;

                RCalculatedDetailsFrame frame = new RCalculatedDetailsFrame(r_Calc);
                gridBagConstraints.gridy = frame.labelGBC.gridy + 1;
                frame.add(jLabel9, gridBagConstraints);

                return frame;

            case Transaction.SEND_ASSET_TRANSACTION:
                RSend r_Send = (RSend) transaction;

                Send_RecordDetailsFrame send_RecordDetailsFrame = new Send_RecordDetailsFrame(r_Send);
                gridBagConstraints.gridy = send_RecordDetailsFrame.labelGBC.gridy + 1;
                send_RecordDetailsFrame.add(jLabel9, gridBagConstraints);

                return send_RecordDetailsFrame;

            case Transaction.SIGN_NOTE_TRANSACTION:

                RSignNote statement = (RSignNote) transaction;
                return new RNoteInfo(statement);

            case Transaction.CREATE_POLL_TRANSACTION:

                CreatePollTransaction pollCreation = (CreatePollTransaction) transaction;

                CreatePollDetailsFrame createPollDetailsFrame = new CreatePollDetailsFrame(pollCreation);

                gridBagConstraints.gridy = createPollDetailsFrame.labelGBC.gridy + 1;
                createPollDetailsFrame.add(jLabel9, gridBagConstraints);

                return createPollDetailsFrame;

            case Transaction.VOTE_ON_POLL_TRANSACTION:

                VoteOnPollTransaction pollVote = (VoteOnPollTransaction) transaction;
                VoteOnPollDetailsFrame voteOnPollDetailsFrame = new VoteOnPollDetailsFrame(pollVote);

                gridBagConstraints.gridy = voteOnPollDetailsFrame.labelGBC.gridy + 1;
                voteOnPollDetailsFrame.add(jLabel9, gridBagConstraints);

                return voteOnPollDetailsFrame;

            case Transaction.VOTE_ON_ITEM_POLL_TRANSACTION:

                VoteOnItemPollTransaction itemPollVote = (VoteOnItemPollTransaction) transaction;
                VoteOnItemPollDetailsFrame voteOnItemPollDetailsFrame = new VoteOnItemPollDetailsFrame(itemPollVote);

                gridBagConstraints.gridy = voteOnItemPollDetailsFrame.labelGBC.gridy + 1;
                voteOnItemPollDetailsFrame.add(jLabel9, gridBagConstraints);

                return voteOnItemPollDetailsFrame;

            case Transaction.ARBITRARY_TRANSACTION:

                ArbitraryTransaction arbitraryTransaction = (ArbitraryTransaction) transaction;

                ArbitraryTransactionDetailsFrame arbitraryTransactionDetailsFrame = new ArbitraryTransactionDetailsFrame(
                        arbitraryTransaction);

                gridBagConstraints.gridy = arbitraryTransactionDetailsFrame.labelGBC.gridy + 1;
                arbitraryTransactionDetailsFrame.add(jLabel9, gridBagConstraints);

                return arbitraryTransactionDetailsFrame;

            case Transaction.ISSUE_ASSET_TRANSACTION:

                IssueAssetTransaction issueAssetTransaction = (IssueAssetTransaction) transaction;
                IssueAssetDetailsFrame issueAssetDetailsFrame = new IssueAssetDetailsFrame(issueAssetTransaction);

                gridBagConstraints.gridy = issueAssetDetailsFrame.labelGBC.gridy + 1;
                issueAssetDetailsFrame.add(jLabel9, gridBagConstraints);

                return issueAssetDetailsFrame;

            case Transaction.ISSUE_PERSON_TRANSACTION:

                IssuePersonRecord issuePerson = (IssuePersonRecord) transaction;

                IssuePersonDetailsFrame issuePersonDetailsFrame = new IssuePersonDetailsFrame(issuePerson);
                gridBagConstraints.gridy = issuePersonDetailsFrame.labelGBC.gridy + 1;
                issuePersonDetailsFrame.add(jLabel9, gridBagConstraints);

                return issuePersonDetailsFrame;

            case Transaction.ISSUE_POLL_TRANSACTION:

                IssuePollRecord issuePoll = (IssuePollRecord) transaction;

                IssuePollDetailsFrame issuePollDetailsFrame = new IssuePollDetailsFrame(issuePoll);
                gridBagConstraints.gridy = issuePollDetailsFrame.labelGBC.gridy + 1;
                issuePollDetailsFrame.add(jLabel9, gridBagConstraints);

                return issuePollDetailsFrame;

            case Transaction.SET_STATUS_TO_ITEM_TRANSACTION:

                RSetStatusToItem setStatusToItem = (RSetStatusToItem) transaction;

                SetStatusToItemDetailsFrame setStatusToItemDetailsFrame = new SetStatusToItemDetailsFrame(setStatusToItem);
                gridBagConstraints.gridy = setStatusToItemDetailsFrame.labelGBC.gridy + 1;
                setStatusToItemDetailsFrame.add(jLabel9, gridBagConstraints);

                return setStatusToItemDetailsFrame;

            case Transaction.CREATE_ORDER_TRANSACTION:

                CreateOrderTransaction createOrderTransaction = (CreateOrderTransaction) transaction;

                CreateOrderDetailsFrame createOrderDetailsFrame = new CreateOrderDetailsFrame(createOrderTransaction);
                gridBagConstraints.gridy = createOrderDetailsFrame.labelGBC.gridy + 1;
                createOrderDetailsFrame.add(jLabel9, gridBagConstraints);

                return createOrderDetailsFrame;

            case Transaction.CANCEL_ORDER_TRANSACTION:

                CancelOrderTransaction cancelOrderTransaction = (CancelOrderTransaction) transaction;

                CancelOrderDetailsFrame cancelOrderDetailsFrame = new CancelOrderDetailsFrame(cancelOrderTransaction);
                gridBagConstraints.gridy = cancelOrderDetailsFrame.labelGBC.gridy + 1;
                cancelOrderDetailsFrame.add(jLabel9, gridBagConstraints);

                return cancelOrderDetailsFrame;

            case Transaction.UPDATE_ORDER_TRANSACTION:

                UpdateOrderTransaction updateOrderTransaction = (UpdateOrderTransaction) transaction;

                UpdateOrderDetailsFrame updateOrderDetailsFrame = new UpdateOrderDetailsFrame(updateOrderTransaction);
                gridBagConstraints.gridy = updateOrderDetailsFrame.labelGBC.gridy + 1;
                updateOrderDetailsFrame.add(jLabel9, gridBagConstraints);

                return updateOrderDetailsFrame;

            case Transaction.MULTI_PAYMENT_TRANSACTION:

                MultiPaymentTransaction MultiPaymentTransaction = (MultiPaymentTransaction) transaction;

                MultiPaymentDetailsFrame multiPaymentDetailsFrame = new MultiPaymentDetailsFrame(MultiPaymentTransaction);
                gridBagConstraints.gridy = multiPaymentDetailsFrame.labelGBC.gridy + 1;
                multiPaymentDetailsFrame.add(jLabel9, gridBagConstraints);

                return multiPaymentDetailsFrame;

            case Transaction.SIGN_TRANSACTION:
                RVouch r_Vouch = (RVouch) transaction;
                SigningDetailsFrame signingDetailsFrame = new SigningDetailsFrame(r_Vouch);
                gridBagConstraints.gridy = signingDetailsFrame.labelGBC.gridy + 1;
                signingDetailsFrame.add(jLabel9, gridBagConstraints);

                return signingDetailsFrame;

            case Transaction.CERTIFY_PUB_KEYS_TRANSACTION:
                RCertifyPubKeys certifyPubKeysRecord = (RCertifyPubKeys) transaction;
                CertifyPubKeysDetailsFrame certifyPubKeysDetailsFrame = new CertifyPubKeysDetailsFrame(
                        certifyPubKeysRecord);
                gridBagConstraints.gridy = certifyPubKeysDetailsFrame.labelGBC.gridy + 1;
                certifyPubKeysDetailsFrame.add(jLabel9, gridBagConstraints);

                return certifyPubKeysDetailsFrame;

            case Transaction.HASHES_RECORD:
                RHashes r_Hashes = (RHashes) transaction;
                HashesDetailsFrame hashesDetailsFrame = new HashesDetailsFrame(r_Hashes);
                gridBagConstraints.gridy = hashesDetailsFrame.labelGBC.gridy + 1;
                hashesDetailsFrame.add(jLabel9, gridBagConstraints);

                return hashesDetailsFrame;

            case Transaction.ISSUE_IMPRINT_TRANSACTION:

                IssueImprintRecord issueImprint = (IssueImprintRecord) transaction;
                IssueImprintDetailsFrame issueImprintDetailsFrame = new IssueImprintDetailsFrame(issueImprint);
                gridBagConstraints.gridy = issueImprintDetailsFrame.labelGBC.gridy + 1;
                issueImprintDetailsFrame.add(jLabel9, gridBagConstraints);

                return issueImprintDetailsFrame;

            case Transaction.ISSUE_TEMPLATE_TRANSACTION:

                IssueTemplateRecord issueTemplate = (IssueTemplateRecord) transaction;
                IssueTemplateDetailsFrame issueTemplateDetailsFrame = new IssueTemplateDetailsFrame(issueTemplate);
                gridBagConstraints.gridy = issueTemplateDetailsFrame.labelGBC.gridy + 1;
                issueTemplateDetailsFrame.add(jLabel9, gridBagConstraints);
                return issueTemplateDetailsFrame;

            case Transaction.ISSUE_UNION_TRANSACTION:

                IssueUnionRecord issueUnion = (IssueUnionRecord) transaction;
                IssueUnionDetailsFrame issueUnionDetailsFrame = new IssueUnionDetailsFrame(issueUnion);
                gridBagConstraints.gridy = issueUnionDetailsFrame.labelGBC.gridy + 1;
                issueUnionDetailsFrame.add(jLabel9, gridBagConstraints);
                return issueUnionDetailsFrame;

            case Transaction.ISSUE_STATUS_TRANSACTION:

                IssueStatusRecord issueStatus = (IssueStatusRecord) transaction;
                IssueStatusDetailsFrame issueStatusDetailsFrame = new IssueStatusDetailsFrame(issueStatus);
                gridBagConstraints.gridy = issueStatusDetailsFrame.labelGBC.gridy + 1;
                issueStatusDetailsFrame.add(jLabel9, gridBagConstraints);
                return issueStatusDetailsFrame;

            case Transaction.GENESIS_SEND_ASSET_TRANSACTION:

                GenesisTransferAssetTransaction genesisTransferAssetTransaction = (GenesisTransferAssetTransaction) transaction;

                GenesisTransferAssetDetailsFrame genesisTransferAssetDetailsFrame = new GenesisTransferAssetDetailsFrame(
                        genesisTransferAssetTransaction);
                gridBagConstraints.gridy = genesisTransferAssetDetailsFrame.labelGBC.gridy + 1;
                genesisTransferAssetDetailsFrame.add(jLabel9, gridBagConstraints);

                return genesisTransferAssetDetailsFrame;

            case Transaction.GENESIS_ISSUE_TEMPLATE_TRANSACTION:

                GenesisIssueTemplateRecord genesisIssueTemplateRecord = (GenesisIssueTemplateRecord) transaction;
                GenesisIssueTemplateDetailsFrame genesisIssueTemplateDetailsFrame = new GenesisIssueTemplateDetailsFrame(
                        genesisIssueTemplateRecord);
                gridBagConstraints.gridy = genesisIssueTemplateDetailsFrame.labelGBC.gridy + 1;
                genesisIssueTemplateDetailsFrame.add(jLabel9, gridBagConstraints);

                return genesisIssueTemplateDetailsFrame;

            case Transaction.GENESIS_ISSUE_ASSET_TRANSACTION:

                GenesisIssueAssetTransaction genesisIssueAssetTransaction = (GenesisIssueAssetTransaction) transaction;

                GenesisIssueAssetDetailsFrame genesisIssueAssetDetailsFrame = new GenesisIssueAssetDetailsFrame(
                        genesisIssueAssetTransaction);
                gridBagConstraints.gridy = genesisIssueAssetDetailsFrame.labelGBC.gridy + 1;
                genesisIssueAssetDetailsFrame.add(jLabel9, gridBagConstraints);

                return genesisIssueAssetDetailsFrame;

            case Transaction.GENESIS_ISSUE_PERSON_TRANSACTION:

                GenesisCertifyPersonRecord record = (GenesisCertifyPersonRecord) transaction;

                GenesisCertifyPersonRecordFrame genesisCertifyPersonRecordFrame = new GenesisCertifyPersonRecordFrame(
                        record);
                gridBagConstraints.gridy = genesisCertifyPersonRecordFrame.labelGBC.gridy + 1;
                genesisCertifyPersonRecordFrame.add(jLabel9, gridBagConstraints);

                return genesisCertifyPersonRecordFrame;
        }

        return new JPanel();
    }
}
