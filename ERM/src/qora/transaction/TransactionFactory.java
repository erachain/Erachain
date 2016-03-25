package qora.transaction;

import java.util.Arrays;
import java.util.logging.Logger;

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
	
	public Transaction parse(byte[] data) throws Exception
	{
		//READ TYPE
		int type = Byte.toUnsignedInt(data[0]);
		//Logger.getGlobal().info(" 1: " + parsedAssetTransfer.getKey() );

		
		switch(type)
		{
		case Transaction.STATEMENT_RECORD:
			
			//PARSE PAYMENT TRANSACTION
			return RecStatement.Parse(data);

		case Transaction.PAYMENT_TRANSACTION:
			
			//PARSE PAYMENT TRANSACTION
			return PaymentTransaction.Parse(data);
		
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
			return VoteOnPollTransaction.Parse(data);		
			
		case Transaction.ARBITRARY_TRANSACTION:
			
			//PARSE ARBITRARY TRANSACTION
			return ArbitraryTransaction.Parse(data);			
			
		case Transaction.ISSUE_ASSET_TRANSACTION:
			
			//PARSE ISSUE ASSET TRANSACTION
			return IssueAssetTransaction.Parse(data);
			
		case Transaction.TRANSFER_ASSET_TRANSACTION:
			
			//PARSE TRANSFER ASSET TRANSACTION
			return TransferAssetTransaction.Parse(data);	
		
		case Transaction.CREATE_ORDER_TRANSACTION:
			
			//PARSE ORDER CREATION TRANSACTION
			return CreateOrderTransaction.Parse(data);	
			
		case Transaction.CANCEL_ORDER_TRANSACTION:
			
			//PARSE ORDER CANCEL
			return CancelOrderTransaction.Parse(data);	
			
		case Transaction.MULTI_PAYMENT_TRANSACTION:
			
			//PARSE MULTI PAYMENT
			return MultiPaymentTransaction.Parse(data);		
		
		case Transaction.DEPLOY_AT_TRANSACTION:
			return DeployATTransaction.Parse(data);

		case Transaction.MESSAGE_TRANSACTION:

			// PARSE MESSAGE TRANSACTION
			return MessageTransaction.Parse(data);
			
			/*
		case Transaction.ACCOUNTING_TRANSACTION:

			
				// PARSE ACCOUNTING TRANSACTION V3
				return AccountingTransaction.Parse(Arrays.copyOfRange(data, 4, data.length));
				
		case Transaction.JSON_TRANSACTION:

			
			// PARSE JSON1 TRANSACTION
			return JsonTransaction.Parse(Arrays.copyOfRange(data, 4, data.length));
			*/
		case Transaction.GENESIS_TRANSFER_ASSET_TRANSACTION:
			
			//PARSE TRANSFER ASSET TRANSACTION
			return GenesisTransferAssetTransaction.Parse(data);	
		
		case Transaction.GENESIS_ISSUE_ASSET_TRANSACTION:
			
			//PARSE ISSUE ASSET TRANSACTION
			return GenesisIssueAssetTransaction.Parse(data);

		case Transaction.GENESIS_TRANSACTION:
			
			//PARSE GENESIS TRANSACTION
			return GenesisTransaction.Parse(data);
			
		}

		throw new Exception("Invalid transaction type: " + type);
	}
	
}
