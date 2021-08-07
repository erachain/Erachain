package org.erachain.core.transaction;

//import com.google.common.primitives.Longs;

public class TransactionFactory {

    private static TransactionFactory instance;

    private TransactionFactory() {

    }

    public static TransactionFactory getInstance() {
        if (instance == null) {
            instance = new TransactionFactory();
        }

        return instance;
    }

    public Transaction parse(byte[] data, int forDeal) throws Exception {
        //READ TYPE
        int type = Byte.toUnsignedInt(data[0]);
        //logger.info(" 1: " + parsedAssetTransfer.getKey() );


        switch (type) {
            case Transaction.SIGN_NOTE_TRANSACTION:

                //PARSE PAYMENT TRANSACTION
                return RSignNote.Parse(data, forDeal);

            case Transaction.CREATE_POLL_TRANSACTION:

                //PARSE CREATE POLL TRANSACTION
                return CreatePollTransaction.Parse(data, forDeal);

            case Transaction.VOTE_ON_POLL_TRANSACTION:

                //PARSE CREATE POLL VOTE
                return VoteOnPollTransaction.Parse(data, forDeal);

            case Transaction.VOTE_ON_ITEM_POLL_TRANSACTION:

                //PARSE CREATE ITEM POLL VOTE
                return VoteOnItemPollTransaction.Parse(data, forDeal);

            case Transaction.ARBITRARY_TRANSACTION:

                //PARSE ARBITRARY TRANSACTION
                return ArbitraryTransaction.Parse(data);

            case Transaction.CREATE_ORDER_TRANSACTION:

                //PARSE ORDER CREATION TRANSACTION
                return CreateOrderTransaction.Parse(data, forDeal);

            case Transaction.CANCEL_ORDER_TRANSACTION:

                //PARSE ORDER CANCEL
                return CancelOrderTransaction.Parse(data, forDeal);

            case Transaction.CHANGE_ORDER_TRANSACTION:

                //PARSE UPDATE CREATION TRANSACTION
                return ChangeOrderTransaction.Parse(data, forDeal);

            case Transaction.ISSUE_ASSET_SERIES_TRANSACTION:

                //PARSE ISSUE ASSET SERIES TRANSACTION
                return IssueAssetSeriesTransaction.Parse(data, forDeal);

            case Transaction.MULTI_PAYMENT_TRANSACTION:

                //PARSE MULTI PAYMENT
                return MultiPaymentTransaction.Parse(data, forDeal);

            case Transaction.DEPLOY_AT_TRANSACTION:
                return DeployATTransaction.Parse(data);

            case Transaction.SEND_ASSET_TRANSACTION:

                // PARSE MESSAGE TRANSACTION
                return RSend.Parse(data, forDeal);

            case Transaction.HASHES_RECORD:


                // PARSE ACCOUNTING TRANSACTION V3
                return RHashes.Parse(data, forDeal);
				
                    /*
            case Transaction.JSON_TRANSACTION:


                // PARSE JSON1 TRANSACTION
                return JsonTransaction.Parse(Arrays.copyOfRange(data, 4, data.length));
                */

            case Transaction.SIGN_TRANSACTION:

                //PARSE CERTIFY PERSON TRANSACTION
                return RVouch.Parse(data, forDeal);

            case Transaction.SET_STATUS_TO_ITEM_TRANSACTION:

                //PARSE CERTIFY PERSON TRANSACTION
                return RSetStatusToItem.Parse(data, forDeal);

            case Transaction.SET_UNION_TO_ITEM_TRANSACTION:

                //PARSE CERTIFY PERSON TRANSACTION
                return RSetUnionToItem.Parse(data, forDeal);

            case Transaction.CERTIFY_PUB_KEYS_TRANSACTION:

                //PARSE CERTIFY PERSON TRANSACTION
                return RCertifyPubKeys.Parse(data, forDeal);

            case Transaction.ISSUE_ASSET_TRANSACTION:

                //PARSE ISSUE ASSET TRANSACTION
                return IssueAssetTransaction.Parse(data, forDeal);

            case Transaction.ISSUE_IMPRINT_TRANSACTION:

                //PARSE ISSUE IMPRINT TRANSACTION
                return IssueImprintRecord.Parse(data, forDeal);

            case Transaction.ISSUE_TEMPLATE_TRANSACTION:

                //PARSE ISSUE PLATE TRANSACTION
                return IssueTemplateRecord.Parse(data, forDeal);

            case Transaction.ISSUE_PERSON_TRANSACTION:

                //PARSE ISSUE PERSON TRANSACTION
                return IssuePersonRecord.Parse(data, forDeal);

            case Transaction.ISSUE_POLL_TRANSACTION:

                //PARSE ISSUE POLL TRANSACTION
                return IssuePollRecord.Parse(data, forDeal);

            case Transaction.ISSUE_STATUS_TRANSACTION:

                //PARSE ISSUE PLATE TRANSACTION
                return IssueStatusRecord.Parse(data, forDeal);

            case Transaction.ISSUE_UNION_TRANSACTION:

                //PARSE ISSUE PLATE TRANSACTION
                return IssueUnionRecord.Parse(data, forDeal);

            case Transaction.CALCULATED_TRANSACTION:

                //PARSE ISSUE PLATE TRANSACTION
                return RCalculated.Parse(data);

            /*
            case Transaction.GENESIS_CERTIFY_PERSON_TRANSACTION:

                //PARSE TRANSFER ASSET TRANSACTION
                return GenesisCertifyPersonRecord.Parse(data);
                */

            /*
            case Transaction.GENESIS_ASSIGN_STATUS_TRANSACTION:

                //PARSE TRANSFER ASSET TRANSACTION
                return GenesisTransferStatusTransaction.Parse(data);
                */

            case Transaction.GENESIS_SEND_ASSET_TRANSACTION:

                //PARSE TRANSFER ASSET TRANSACTION
                return GenesisTransferAssetTransaction.Parse(data);

            case Transaction.GENESIS_ISSUE_PERSON_TRANSACTION:

                //PARSE ISSUE PERSON TRANSACTION
                return GenesisIssuePersonRecord.Parse(data);

            case Transaction.GENESIS_ISSUE_TEMPLATE_TRANSACTION:

                //PARSE ISSUE PLATE TRANSACTION
                return GenesisIssueTemplateRecord.Parse(data);

            case Transaction.GENESIS_ISSUE_STATUS_TRANSACTION:

                //PARSE ISSUE STATUS TRANSACTION
                return GenesisIssueStatusRecord.Parse(data);

            case Transaction.GENESIS_ISSUE_ASSET_TRANSACTION:

                //PARSE GENESIS TRANSACTION
                return GenesisIssueAssetTransaction.Parse(data);

        }

        throw new Exception("Invalid transaction type: " + type);
    }

}
