package gui.transaction;

import java.awt.GridBagConstraints;
import java.awt.LayoutManager;

import javax.swing.JLabel;
import javax.swing.JPanel;

import core.transaction.ArbitraryTransaction;
import core.transaction.BuyNameTransaction;
import core.transaction.CancelOrderTransaction;
import core.transaction.CancelSellNameTransaction;
import core.transaction.CreateOrderTransaction;
import core.transaction.CreatePollTransaction;

import core.transaction.GenesisCertifyPersonRecord;
import core.transaction.GenesisIssueAssetTransaction;
import core.transaction.GenesisIssueNoteRecord;
import core.transaction.GenesisTransferAssetTransaction;

import core.transaction.IssueAssetTransaction;
import core.transaction.IssueImprintRecord;
import core.transaction.IssueNoteRecord;
import core.transaction.IssuePersonRecord;
import core.transaction.IssueStatusRecord;
import core.transaction.IssueUnionRecord;
import core.transaction.R_Send;
import core.transaction.MultiPaymentTransaction;
import core.transaction.R_SertifyPubKeys;
import core.transaction.R_SetStatusToItem;
import core.transaction.R_SignNote;
import core.transaction.R_Vouch;
import core.transaction.RegisterNameTransaction;
import core.transaction.SellNameTransaction;
import core.transaction.Transaction;
import core.transaction.UpdateNameTransaction;
import core.transaction.VoteOnPollTransaction;

public class TransactionDetailsFactory 
{
	private static TransactionDetailsFactory instance;

	private void TransactionDetailsFactory(){
		
		
	
	
	}
	
	
	public static TransactionDetailsFactory getInstance()
	{
		if(instance == null)
		{
			instance = new TransactionDetailsFactory();
		}
		
		return instance;
	}
	
	public JPanel createTransactionDetail(Transaction transaction)
	{
		
		 
		  GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
		 JLabel jLabel9 = new JLabel("");
		 
		 gridBagConstraints.gridx = 0;
	       
	        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
	        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
	        gridBagConstraints.weightx = 1.0;
	        gridBagConstraints.weighty = 1.0;
	       
		 
		 
		switch(transaction.getType())
		{
		
		case Transaction.SIGN_NOTE_TRANSACTION:
			
			R_SignNote statement = (R_SignNote) transaction;
			
			RecStatementDetailsFrame recStatementDetailsFrame = new RecStatementDetailsFrame(statement);
			 gridBagConstraints.gridy =recStatementDetailsFrame.labelGBC.gridy+1; 
			 recStatementDetailsFrame. add(jLabel9, gridBagConstraints);
			
			return recStatementDetailsFrame;
			
		case Transaction.REGISTER_NAME_TRANSACTION:
			
			
			RegisterNameTransaction nameRegistration = (RegisterNameTransaction) transaction;
			RegisterNameDetailsFrame registerNameDetailsFrame = new RegisterNameDetailsFrame(nameRegistration);
			gridBagConstraints.gridy =registerNameDetailsFrame.labelGBC.gridy+1; 
			registerNameDetailsFrame. add(jLabel9, gridBagConstraints);
			return registerNameDetailsFrame;
			
		case Transaction.UPDATE_NAME_TRANSACTION:
			
			UpdateNameTransaction nameUpdate = (UpdateNameTransaction) transaction;
			
			
			UpdateNameDetailsFrame updateNameDetailsFrame = new UpdateNameDetailsFrame(nameUpdate);
			
			gridBagConstraints.gridy = updateNameDetailsFrame.labelGBC.gridy+1; 
			updateNameDetailsFrame. add(jLabel9, gridBagConstraints);
						
			return updateNameDetailsFrame;	
			
		case Transaction.SELL_NAME_TRANSACTION:
			
			SellNameTransaction nameSale = (SellNameTransaction) transaction;
			
			SellNameDetailsFrame sellNameDetailsFrame = new SellNameDetailsFrame(nameSale);
			
			gridBagConstraints.gridy = sellNameDetailsFrame.labelGBC.gridy+1; 
			sellNameDetailsFrame. add(jLabel9, gridBagConstraints);
			
			return sellNameDetailsFrame;		
			
		case Transaction.CANCEL_SELL_NAME_TRANSACTION:
			
			CancelSellNameTransaction cancelNameSale = (CancelSellNameTransaction) transaction;
			CancelSellNameDetailsFrame cancelSellNameDetailsFrame = new CancelSellNameDetailsFrame(cancelNameSale);
			
			gridBagConstraints.gridy = cancelSellNameDetailsFrame.labelGBC.gridy+1; 
			cancelSellNameDetailsFrame. add(jLabel9, gridBagConstraints);
			
			
			return cancelSellNameDetailsFrame;			
			
		case Transaction.BUY_NAME_TRANSACTION:
			
			BuyNameTransaction namePurchase = (BuyNameTransaction) transaction;
			
			BuyNameDetailsFrame buyNameDetailsFrame = new BuyNameDetailsFrame(namePurchase);
			
			gridBagConstraints.gridy = buyNameDetailsFrame.labelGBC.gridy+1; 
			buyNameDetailsFrame. add(jLabel9, gridBagConstraints);
			
			
			
			return buyNameDetailsFrame;	
		
		case Transaction.CREATE_POLL_TRANSACTION:
			
			CreatePollTransaction pollCreation = (CreatePollTransaction) transaction;
			
			CreatePollDetailsFrame createPollDetailsFrame = new CreatePollDetailsFrame(pollCreation);
			
			gridBagConstraints.gridy = createPollDetailsFrame.labelGBC.gridy+1; 
			createPollDetailsFrame. add(jLabel9, gridBagConstraints);
			
			
			return createPollDetailsFrame;			

		case Transaction.VOTE_ON_POLL_TRANSACTION:
			
			VoteOnPollTransaction pollVote = (VoteOnPollTransaction) transaction;
			VoteOnPollDetailsFrame voteOnPollDetailsFrame =new VoteOnPollDetailsFrame(pollVote);
			
			gridBagConstraints.gridy = voteOnPollDetailsFrame.labelGBC.gridy+1; 
			voteOnPollDetailsFrame. add(jLabel9, gridBagConstraints);
			
			return voteOnPollDetailsFrame;
			
		case Transaction.ARBITRARY_TRANSACTION:
			
			ArbitraryTransaction arbitraryTransaction = (ArbitraryTransaction) transaction;
			
			ArbitraryTransactionDetailsFrame arbitraryTransactionDetailsFrame =new ArbitraryTransactionDetailsFrame(arbitraryTransaction);
			
			gridBagConstraints.gridy = arbitraryTransactionDetailsFrame.labelGBC.gridy+1; 
			arbitraryTransactionDetailsFrame. add(jLabel9, gridBagConstraints);
			
			return arbitraryTransactionDetailsFrame;	
			
		case Transaction.ISSUE_ASSET_TRANSACTION:
			
			IssueAssetTransaction issueAssetTransaction = (IssueAssetTransaction) transaction;
			IssueAssetDetailsFrame issueAssetDetailsFrame = new IssueAssetDetailsFrame(issueAssetTransaction);
			
			gridBagConstraints.gridy = issueAssetDetailsFrame.labelGBC.gridy+1; 
			issueAssetDetailsFrame. add(jLabel9, gridBagConstraints);
				
				
		      
		     			
			return issueAssetDetailsFrame;	
						
		case Transaction.ISSUE_PERSON_TRANSACTION:
			
			IssuePersonRecord issuePerson = (IssuePersonRecord) transaction;
			
			IssuePersonDetailsFrame issuePersonDetailsFrame = new IssuePersonDetailsFrame(issuePerson);
			gridBagConstraints.gridy = issuePersonDetailsFrame.labelGBC.gridy+1; 
			issuePersonDetailsFrame. add(jLabel9, gridBagConstraints);
			
			return issuePersonDetailsFrame;	

		case Transaction.SET_STATUS_TO_ITEM_TRANSACTION:
			
			R_SetStatusToItem setStatusToItem = (R_SetStatusToItem) transaction;
			
			SetStatusToItemDetailsFrame setStatusToItemDetailsFrame = new SetStatusToItemDetailsFrame(setStatusToItem);
			gridBagConstraints.gridy = setStatusToItemDetailsFrame.labelGBC.gridy+1; 
			setStatusToItemDetailsFrame. add(jLabel9, gridBagConstraints);
			
			return setStatusToItemDetailsFrame;	

		case Transaction.CREATE_ORDER_TRANSACTION:
			
			CreateOrderTransaction createOrderTransaction = (CreateOrderTransaction) transaction;
			
			CreateOrderDetailsFrame createOrderDetailsFrame = new CreateOrderDetailsFrame(createOrderTransaction);
			gridBagConstraints.gridy = createOrderDetailsFrame.labelGBC.gridy+1; 
			createOrderDetailsFrame. add(jLabel9, gridBagConstraints);
			
			return createOrderDetailsFrame;	
		
		case Transaction.CANCEL_ORDER_TRANSACTION:
			
			CancelOrderTransaction cancelOrderTransaction = (CancelOrderTransaction) transaction;
			
			CancelOrderDetailsFrame cancelOrderDetailsFrame = new CancelOrderDetailsFrame(cancelOrderTransaction);
			gridBagConstraints.gridy = cancelOrderDetailsFrame.labelGBC.gridy+1; 
			cancelOrderDetailsFrame. add(jLabel9, gridBagConstraints);
			
			
			return cancelOrderDetailsFrame;		
			
		case Transaction.MULTI_PAYMENT_TRANSACTION:
			
			MultiPaymentTransaction MultiPaymentTransaction = (MultiPaymentTransaction) transaction;
			
			MultiPaymentDetailsFrame multiPaymentDetailsFrame =  new MultiPaymentDetailsFrame(MultiPaymentTransaction);
			gridBagConstraints.gridy = multiPaymentDetailsFrame.labelGBC.gridy+1; 
			multiPaymentDetailsFrame. add(jLabel9, gridBagConstraints);
			
			
			
			return multiPaymentDetailsFrame;

		case Transaction.SEND_ASSET_TRANSACTION:
			R_Send r_Send = (R_Send)transaction;
			
			Send_RecordDetailsFrame send_RecordDetailsFrame =   new Send_RecordDetailsFrame(r_Send);
			gridBagConstraints.gridy = send_RecordDetailsFrame.labelGBC.gridy+1; 
			send_RecordDetailsFrame. add(jLabel9, gridBagConstraints);
			
			
			return send_RecordDetailsFrame;
			
		case Transaction.VOUCH_TRANSACTION:
			R_Vouch r_Vouch = (R_Vouch)transaction;
			VouchingDetailsFrame vouchingDetailsFrame =   new VouchingDetailsFrame(r_Vouch);
			gridBagConstraints.gridy = vouchingDetailsFrame.labelGBC.gridy+1; 
			vouchingDetailsFrame. add(jLabel9, gridBagConstraints);
			
			return vouchingDetailsFrame;

		case Transaction.CERTIFY_PUB_KEYS_TRANSACTION:
			R_SertifyPubKeys sertifyPubKeysRecord = (R_SertifyPubKeys)transaction;
			SertifyPubKeysDetailsFrame sertifyPubKeysDetailsFrame =  new SertifyPubKeysDetailsFrame(sertifyPubKeysRecord);
			gridBagConstraints.gridy = sertifyPubKeysDetailsFrame.labelGBC.gridy+1; 
			sertifyPubKeysDetailsFrame. add(jLabel9, gridBagConstraints);
			
			return sertifyPubKeysDetailsFrame;

		case Transaction.ISSUE_IMPRINT_TRANSACTION:
			
			IssueImprintRecord issueImprint = (IssueImprintRecord) transaction;
			IssueImprintDetailsFrame issueImprintDetailsFrame =  new IssueImprintDetailsFrame(issueImprint);
			gridBagConstraints.gridy = issueImprintDetailsFrame.labelGBC.gridy+1; 
			issueImprintDetailsFrame. add(jLabel9, gridBagConstraints);
			
			return issueImprintDetailsFrame ;	

		case Transaction.ISSUE_NOTE_TRANSACTION:
			
			IssueNoteRecord issueNote = (IssueNoteRecord) transaction;
			IssueNoteDetailsFrame issueNoteDetailsFrame =   new IssueNoteDetailsFrame(issueNote);
			gridBagConstraints.gridy = issueNoteDetailsFrame.labelGBC.gridy+1; 
			issueNoteDetailsFrame. add(jLabel9, gridBagConstraints);
			return issueNoteDetailsFrame;	

		case Transaction.ISSUE_UNION_TRANSACTION:
			
			IssueUnionRecord issueUnion = (IssueUnionRecord) transaction;
			IssueUnionDetailsFrame issueUnionDetailsFrame =   new IssueUnionDetailsFrame(issueUnion);
			gridBagConstraints.gridy = issueUnionDetailsFrame.labelGBC.gridy+1; 
			issueUnionDetailsFrame. add(jLabel9, gridBagConstraints);
			return issueUnionDetailsFrame;	

		case Transaction.ISSUE_STATUS_TRANSACTION:
			
			IssueStatusRecord issueStatus = (IssueStatusRecord) transaction;
			IssueStatusDetailsFrame issueStatusDetailsFrame =   new IssueStatusDetailsFrame(issueStatus);
			gridBagConstraints.gridy = issueStatusDetailsFrame.labelGBC.gridy+1; 
			issueStatusDetailsFrame. add(jLabel9, gridBagConstraints);
			return issueStatusDetailsFrame ;	

		case Transaction.GENESIS_SEND_ASSET_TRANSACTION:
			
			GenesisTransferAssetTransaction genesisTransferAssetTransaction = (GenesisTransferAssetTransaction) transaction;
			
			GenesisTransferAssetDetailsFrame genesisTransferAssetDetailsFrame =    new GenesisTransferAssetDetailsFrame(genesisTransferAssetTransaction);
			gridBagConstraints.gridy = genesisTransferAssetDetailsFrame.labelGBC.gridy+1; 
			genesisTransferAssetDetailsFrame. add(jLabel9, gridBagConstraints);
			
			return genesisTransferAssetDetailsFrame;		
			
		case Transaction.GENESIS_ISSUE_NOTE_TRANSACTION:
			
			GenesisIssueNoteRecord genesisIssueNoteRecord = (GenesisIssueNoteRecord) transaction;
			GenesisIssueNoteDetailsFrame genesisIssueNoteDetailsFrame =    new GenesisIssueNoteDetailsFrame(genesisIssueNoteRecord);
			gridBagConstraints.gridy = genesisIssueNoteDetailsFrame.labelGBC.gridy+1; 
			genesisIssueNoteDetailsFrame. add(jLabel9, gridBagConstraints);
			
			return genesisIssueNoteDetailsFrame;	

		case Transaction.GENESIS_ISSUE_ASSET_TRANSACTION:
			
			GenesisIssueAssetTransaction genesisIssueAssetTransaction = (GenesisIssueAssetTransaction) transaction;
			
			GenesisIssueAssetDetailsFrame genesisIssueAssetDetailsFrame =   new GenesisIssueAssetDetailsFrame(genesisIssueAssetTransaction);
			gridBagConstraints.gridy = genesisIssueAssetDetailsFrame.labelGBC.gridy+1; 
			genesisIssueAssetDetailsFrame. add(jLabel9, gridBagConstraints);
			
			
			return genesisIssueAssetDetailsFrame;	

		case Transaction.GENESIS_ISSUE_PERSON_TRANSACTION:
			
			GenesisCertifyPersonRecord record = (GenesisCertifyPersonRecord) transaction;
			
			GenesisCertifyPersonRecordFrame genesisCertifyPersonRecordFrame =   new GenesisCertifyPersonRecordFrame(record);
			gridBagConstraints.gridy = genesisCertifyPersonRecordFrame.labelGBC.gridy+1; 
			genesisCertifyPersonRecordFrame. add(jLabel9, gridBagConstraints);
			
			return genesisCertifyPersonRecordFrame ;
		}
		
		return null;
	}
}
