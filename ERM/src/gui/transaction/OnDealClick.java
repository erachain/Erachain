package gui.transaction;

import java.sql.Timestamp;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JButton;

import controller.Controller;
import core.account.Account;
import core.account.PrivateKeyAccount;

//import javax.swing.JFrame;
//import javax.swing.JOptionPane;

import core.transaction.Transaction;
//import lang.Lang;
import gui.PasswordPane;
import lang.Lang;
import utils.Pair;

public class OnDealClick 
{

	public static boolean proccess1(JButton button)
	{
		//DISABLE
		button.setEnabled(false);
	
		//CHECK IF NETWORK OK
		if(Controller.getInstance().getStatus() != Controller.STATUS_OK)
		{
			//NETWORK NOT OK
			JOptionPane.showMessageDialog(null, Lang.getInstance().translate("You are unable to send a transaction while synchronizing or while having no connections!"), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
			
			//ENABLE
			button.setEnabled(true);
			
			return false;
		}
		
		//CHECK IF WALLET UNLOCKED
		if(!Controller.getInstance().isWalletUnlocked())
		{
			//ASK FOR PASSWORD
			String password = PasswordPane.showUnlockWalletDialog(); 
			if(!Controller.getInstance().unlockWallet(password))
			{
				//WRONG PASSWORD
				JOptionPane.showMessageDialog(null, Lang.getInstance().translate("Invalid password"), Lang.getInstance().translate("Unlock Wallet"), JOptionPane.ERROR_MESSAGE);
				
				//ENABLE
				button.setEnabled(true);
				
				return false;
			}
		}
		return true;
		
	}
	
	public static String resultMess(int error)
	{
		String mess = "Unknown error: " + error;

		switch(error)
		{
		case Transaction.INVALID_ADDRESS:
			mess = "Invalid address";
			break;
		case Transaction.NEGATIVE_AMOUNT:
			mess = "Negative amount";
			break;
		case Transaction.NOT_ENOUGH_FEE:
			mess = "Not enought fee";
			break;
		case Transaction.INVALID_FEE_POWER:
			mess = "Invalid fee power";
			break;
			
		case Transaction.NO_BALANCE:
			mess = "No balance";
			break;
		case Transaction.NO_DEBT_BALANCE:
			mess = "No debt balance";
			break;
		case Transaction.NO_HOLD_BALANCE:
			mess = "No hold balance";
			break;
			
		case Transaction.INVALID_REFERENCE:
			mess = "Invalid reference";
			break;
		case Transaction.INVALID_TIMESTAMP:
			mess = "Invalid timestamp";
			break;
		case Transaction.INVALID_MAKER_ADDRESS:
			mess = "Invalid maker address";
			break;
		case Transaction.INVALID_PUBLIC_KEY:
			mess = "Invalid public key";
			break;

		case Transaction.INVALID_NAME_LENGTH:
			mess = "Invalid  name length";
			break;
		case Transaction.INVALID_VALUE_LENGTH:
			mess = "Invalid value length";
			break;
		case Transaction.NAME_ALREADY_REGISTRED:
			mess = "Name already registred";
			break;
						
		case Transaction.NAME_DOES_NOT_EXIST:
			mess = "Name does not exist";
			break;
		case Transaction.NAME_ALREADY_ON_SALE:
			mess = "Name already on sale";
			break;
		case Transaction.NAME_NOT_FOR_SALE:
			mess = "Name not for sale";
			break;

		case Transaction.INVALID_UPDATE_VALUE:
			mess = "Invalid update value";
			break;
		case Transaction.NAME_KEY_ALREADY_EXISTS:
			mess = "Name key already exists";
			break;
		case Transaction.NAME_KEY_NOT_EXISTS:
			mess = "Name key not exists";
			break;
		case Transaction.LAST_KEY_IS_DEFAULT_KEY:
			mess = "Name name key id default key";
			break;


			
		case Transaction.BUYER_ALREADY_OWNER:
			mess = "Buyer already owner";
			break;
		case Transaction.INVALID_AMOUNT:
			mess = "Invalid amount";
			break;
			
		case Transaction.NAME_NOT_LOWER_CASE:
			mess = "Name not lower case";
			break;
		case Transaction.INVALID_ICON_LENGTH:
			mess = "Invalid icon length";
			break;
		case Transaction.INVALID_IMAGE_LENGTH:
			mess = "Invalid image length";
			break;
			
		case Transaction.INVALID_DESCRIPTION_LENGTH:
			mess = "Invalid description length";
			break;
		case Transaction.INVALID_OPTIONS_LENGTH:
			mess = "Invalid oprion length";
			break;
		case Transaction.INVALID_OPTION_LENGTH:
			mess = "Invalid option length";
			break;
		case Transaction.DUPLICATE_OPTION:
			mess = "Invalid duplicte option";
			break;
		case Transaction.POLL_ALREADY_CREATED:
			mess = "Pool already created";
			break;
		case Transaction.POLL_ALREADY_HAS_VOTES:
			mess = "Poll already has votes";
			break;
		case Transaction.POLL_NOT_EXISTS:
			mess = "Poll not exists";
			break;
		case Transaction.POLL_OPTION_NOT_EXISTS:
			mess = "Option not exists";
			break;
		case Transaction.ALREADY_VOTED_FOR_THAT_OPTION:
			mess = "Already voted for that option";
			break;
		case Transaction.INVALID_DATA_LENGTH:
			mess = "Invalid data length";
			break;
		case Transaction.INVALID_DATA:
			mess = "Invalid data";
			break;
		case Transaction.INVALID_SIGNATURE:
			mess = "Invalid signature";
			break;
		case Transaction.TRANSACTION_DOES_NOT_EXIST:
			mess = "Transaction does not exist";
			break;
			
			
		case Transaction.INVALID_QUANTITY:
			mess = "Invalid quantity";
			break;
		case Transaction.ASSET_DOES_NOT_EXIST:
			mess = "asset does not exist";
			break;
		case Transaction.INVALID_RETURN:
			mess = "Invalid return";
			break;
		case Transaction.HAVE_EQUALS_WANT:
			mess = "Have equals want";
			break;
		case Transaction.ORDER_DOES_NOT_EXIST:
			mess = "Order does not exists";
			break;
		case Transaction.INVALID_ORDER_CREATOR:
			mess = "Invalid order creator";
			break;
		case Transaction.INVALID_PAYMENTS_LENGTH:
			mess = "Invalid payment length";
			break;
		case Transaction.NEGATIVE_PRICE:
			mess = "Negative price";
			break;
		case Transaction.INVALID_PRICE:
			mess = "Invalid price";
			break;
		case Transaction.INVALID_CREATION_BYTES:
			mess = "Invalid creation bytes";
			break;
		case Transaction.AT_ERROR:
			mess = "AT error";
			break;
		case Transaction.INVALID_TAGS_LENGTH:
			mess = "Invalid tags length";
			break;
		case Transaction.INVALID_TYPE_LENGTH:
			mess = "Invalid type lenght";
			break;
		case Transaction.NOT_MOVABLE_ASSET:
			mess = "Not movable asset";
			break;
						
		case Transaction.INVALID_RAW_DATA:
			mess = "Invalid raw data";
			break;
			
		case Transaction.INVALID_DATE:
			mess = "Invalid date";
			break;
	
		case Transaction.NOT_ENOUGH_RIGHTS:
			mess = "Not enough rights";
			break;
			
		case Transaction.INVALID_ITEM_VALUE:
			mess = "Invalid item value";
			break;
		case Transaction.CREATOR_NOT_OWNER:
			mess = "Creator not owner";
			break;
			
		case Transaction.ITEM_DOES_NOT_EXIST:
			mess = "Item does not exist";
			break;
		case Transaction.ACCOUNT_NOT_PERSONALIZED:
			mess = "This Account is not personalized";
			break;
		case Transaction.ITEM_DUPLICATE_KEY:
			mess = "Duplicate key";
			break;
		case Transaction.INVALID_CREATOR:
			mess = "Invalis creator";
			break;
			
		case Transaction.ITEM_ASSET_DOES_NOT_EXIST:
			mess = "Item asset does not exist";
			break;
		case Transaction.ITEM_IMPRINT_DOES_NOT_EXIST:
			mess = "Item imprint does not exist";
			break;
		case Transaction.ITEM_NOTE_NOT_EXIST:
			mess = "Item note does not exist";
			break;
		case Transaction.ITEM_PERSON_NOT_EXIST:
			mess = "Item person does not exist";
			break;
		case Transaction.ITEM_UNION_NOT_EXIST:
			mess = "Item union does not exist";
			break;
	
		case Transaction.ITEM_PERSON_LATITUDE_ERROR:
			mess = "Invalid birth latitude";
			break;
		case Transaction.ITEM_PERSON_LONGITUDE_ERROR:
			mess = "Invalid birth longitude";
			break;
		case Transaction.ITEM_PERSON_RACE_ERROR:
			mess = "Invalid person race";
			break;
		case Transaction.ITEM_PERSON_GENDER_ERROR:
			mess = "Invalid person gender";
			break;
		case Transaction.ITEM_PERSON_SKIN_COLOR_ERROR:
			mess = "Invalid skin color";
			break;
		case Transaction.ITEM_PERSON_EYE_COLOR_ERROR:
			mess = "Invalid eye color";
			break;
		case Transaction.ITEM_PERSON_HAIR_COLOR_ERROR:
			mess = "Invalid hair color";
			break;
		case Transaction.ITEM_PERSON_HEIGHT_ERROR:
			mess = "Invalid height";
			break;
		case Transaction.ACCOUNT_ALREADY_PERSONALIZED:
			mess = "Account already personalizes";
			break;
			
		case Transaction.INVALID_BLOCK_HEIGHT:
			mess = "Invalid block height";
			break;
		case Transaction.INVALID_BLOCK_TRANS_SEQ_ERROR:
			mess = "Invalid block record sequence";
			break;
			
		}
		return mess;
	}
		
}
