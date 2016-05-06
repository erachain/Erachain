package core.transaction;

import java.util.Arrays;
 import org.apache.log4j.Logger;

import com.google.common.primitives.Ints;
//import com.google.common.primitives.Longs;

public class TransactionFactory {

	private static TransactionFactory instance;
	
	public static TransactionFactory getInstance()
	{
		if(instance == null)
		{
			instance = new TransactionFactory();
		}
		
		return instance;
	}
	
	private TransactionFactory()
	{
		
	}
	
	public Transaction parse(byte[] data, byte[] releaserReference) throws Exception
	{
		//READ TYPE
		int type = Byte.toUnsignedInt(data[0]);
		//LOGGER.info(" 1: " + parsedAssetTransfer.getKey() );

		
		switch(type)
		{
		case Transaction.SIGN_NOTE_TRANSACTION:
			
			//PARSE PAYMENT TRANSACTION
			return R_SignNote.Parse(data, releaserReference);

		case Transaction.PAYMENT_TRANSACTION:
			
			//PARSE PAYMENT TRANSACTION
			return PaymentTransaction.Parse(data, releaserReference);
		
		case Transaction.REGISTER_NAME_TRANSACTION:
			
			//PARSE REGISTER NAME TRANSACTION
			return RegisterNameTransaction.Parse(data);
			
		case Transaction.UPDATE_NAME_TRANSACTION:
			
			//PARSE UPDATE NAME TRANSACTION
			return UpdateNameTransaction.Parse(data);
			
		case Transaction.SELL_NAME_TRANSACTION:
			
			//PARSE SELL NAME TRANSACTION
			return SellNameTransaction.Parse(data);
			
		case Transaction.CANCEL_SELL_NAME_TRANSACTION:
			
			//PARSE CANCEL SELL NAME TRANSACTION
			return CancelSellNameTransaction.Parse(data);
			
		case Transaction.BUY_NAME_TRANSACTION:
			
			//PARSE CANCEL SELL NAME TRANSACTION
			return BuyNameTransaction.Parse(data);	
			
		case Transaction.CREATE_POLL_TRANSACTION:
			
			//PARSE CREATE POLL TRANSACTION
			return CreatePollTransaction.Parse(data);	
			
		case Transaction.VOTE_ON_POLL_TRANSACTION:
			
			//PARSE CREATE POLL VOTE
			return VoteOnPollTransaction.Parse(data, releaserReference);		
			
		case Transaction.ARBITRARY_TRANSACTION:
			
			//PARSE ARBITRARY TRANSACTION
			return ArbitraryTransaction.Parse(data);			
			
		case Transaction.TRANSFER_ASSET_TRANSACTION_OLD:
			
			//PARSE TRANSFER ASSET TRANSACTION
			return TransferAssetTransaction.Parse(data, releaserReference);	
		
		case Transaction.CREATE_ORDER_TRANSACTION:
			
			//PARSE ORDER CREATION TRANSACTION
			return CreateOrderTransaction.Parse(data);	
			
		case Transaction.CANCEL_ORDER_TRANSACTION:
			
			//PARSE ORDER CANCEL
			return CancelOrderTransaction.Parse(data);	
			
		case Transaction.MULTI_PAYMENT_TRANSACTION:
			
			//PARSE MULTI PAYMENT
			return MultiPaymentTransaction.Parse(data, releaserReference);		
		
		case Transaction.DEPLOY_AT_TRANSACTION:
			return DeployATTransaction.Parse(data);

		case Transaction.SEND_ASSET_TRANSACTION:

			// PARSE MESSAGE TRANSACTION
			return MessageTransaction.Parse(data, releaserReference);
			
			/*
		case Transaction.ACCOUNTING_TRANSACTION:

			
				// PARSE ACCOUNTING TRANSACTION V3
				return AccountingTransaction.Parse(Arrays.copyOfRange(data, 4, data.length));
				
		case Transaction.JSON_TRANSACTION:

			
			// PARSE JSON1 TRANSACTION
			return JsonTransaction.Parse(Arrays.copyOfRange(data, 4, data.length));
			*/
			
		case Transaction.CERTIFY_PERSON_TRANSACTION:
			
			//PARSE CERTIFY PERSON TRANSACTION
			return R_SertifyPerson.Parse(data, releaserReference);
			
			
		case Transaction.ISSUE_ASSET_TRANSACTION:
			
			//PARSE ISSUE ASSET TRANSACTION
			return IssueAssetTransaction.Parse(data, releaserReference);
			
		case Transaction.ISSUE_IMPRINT_TRANSACTION:
			
			//PARSE ISSUE IMPRINT TRANSACTION
			return IssueImprintRecord.Parse(data, releaserReference);

		case Transaction.ISSUE_NOTE_TRANSACTION:
			
			//PARSE ISSUE NOTE TRANSACTION
			return IssueNoteRecord.Parse(data, releaserReference);

		case Transaction.ISSUE_PERSON_TRANSACTION:
			
			//PARSE ISSUE PERSON TRANSACTION
			return IssuePersonRecord.Parse(data, releaserReference);

		case Transaction.ISSUE_STATUS_TRANSACTION:
			
			//PARSE ISSUE NOTE TRANSACTION
			return IssueStatusRecord.Parse(data, releaserReference);

		case Transaction.ISSUE_UNION_TRANSACTION:
			
			//PARSE ISSUE NOTE TRANSACTION
			return IssueUnionRecord.Parse(data, releaserReference);

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
			
			//PARSE ISSUE ASSET TRANSACTION
			return GenesisIssuePersonRecord.Parse(data);

		case Transaction.GENESIS_ISSUE_NOTE_TRANSACTION:
			
			//PARSE ISSUE ASSET TRANSACTION
			return GenesisIssueNoteTransaction.Parse(data);

		case Transaction.GENESIS_ISSUE_STATUS_TRANSACTION:
			
			//PARSE ISSUE STATUS TRANSACTION
			return GenesisIssueStatusTransaction.Parse(data);

		case Transaction.GENESIS_ISSUE_ASSET_TRANSACTION:
			
			//PARSE GENESIS TRANSACTION
			return GenesisIssueAssetTransaction.Parse(data);
			
		}

		throw new Exception("Invalid transaction type: " + type);
	}
	
}
