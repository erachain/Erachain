package org.erachain.gui.transaction;

import org.erachain.controller.Controller;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.transaction.Transaction;
import org.erachain.gui.MainFrame;
import org.erachain.gui.PasswordPane;
import org.erachain.lang.Lang;

import javax.swing.*;

//import javax.swing.JFrame;
//import javax.swing.JOptionPane;
//import org.erachain.lang.Lang;

public class OnDealClick {

    public static boolean proccess1(JButton button) {
        //DISABLE
        button.setEnabled(false);

        //CHECK IF NETWORK OK
        if (false && Controller.getInstance().getStatus() != Controller.STATUS_OK) {
            //NETWORK NOT OK
            JOptionPane.showMessageDialog(null, Lang.T("You are unable to send a transaction while synchronizing or while having no connections!"), Lang.T("Error"), JOptionPane.ERROR_MESSAGE);

            //ENABLE
            button.setEnabled(true);

            return false;
        }

        //CHECK IF WALLET UNLOCKED
        if (!Controller.getInstance().isWalletUnlocked()) {
            //ASK FOR PASSWORD
            String password = PasswordPane.showUnlockWalletDialog(MainFrame.getInstance());
            if (!Controller.getInstance().unlockWallet(password)) {
                //WRONG PASSWORD
                JOptionPane.showMessageDialog(null, Lang.T("Invalid password"), Lang.T("Unlock Wallet"), JOptionPane.ERROR_MESSAGE);

                //ENABLE
                button.setEnabled(true);

                return false;
            }
        }
        return true;

    }

    public static String resultMess(int error) {
        String mess = "Unknown error: " + error;

        switch (error) {
            case Transaction.FUTURE_ABILITY:
                mess = "ERROR: Future Ability";
                break;
            case Transaction.INVALID_WALLET_ADDRESS:
                mess = "Invalid Wallet Address";
                break;
            case Transaction.INVALID_ADDRESS:
                mess = "Invalid Address";
                break;
            case Transaction.INVALID_RECEIVER:
                mess = "Invalid Receiver";
                break;

            case Transaction.NEGATIVE_AMOUNT:
                mess = "Negative amount";
                break;
            case Transaction.NOT_ENOUGH_FEE:
                mess = "Not enough fee";
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
            case Transaction.INVALID_BACKWARD_ACTION:
                mess = "Invalid backward action";
                break;
                
            case Transaction.NO_HOLD_BALANCE:
                mess = "No hold balance";
                break;

            case Transaction.INVALID_HOLD_DIRECTION:
                mess = "Invalid hold direction";
                break;
                
            case Transaction.INVALID_REFERENCE:
                mess = "Invalid reference";
                break;
            case Transaction.INVALID_TIMESTAMP:
                mess = "Invalid timestamp";
                break;
            case Transaction.INVALID_MAKER_ADDRESS:
                mess = "Invalid maker Account";
                break;
            case Transaction.INVALID_PUBLIC_KEY:
                mess = "Invalid public key";
                break;

            case Transaction.INVALID_ISSUE_PROHIBITED:
                mess = "Item Issue Prohibited";
                break;

            case Transaction.INVALID_NAME_LENGTH_MIN:
                mess = "Invalid name MIN length";
                break;
            case Transaction.INVALID_NAME_LENGTH_MAX:
                mess = "Invalid name MAX length";
                break;
            case Transaction.INVALID_TAGS_LENGTH_MAX:
                mess = "Invalid tags MAX length";
                break;

            case Transaction.INVALID_VALUE_LENGTH_MAX:
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

            case Transaction.PRIVATE_KEY_NOT_FOUND:
                mess = "Private key not found in Wallet";
                break;

            case Transaction.BUYER_ALREADY_OWNER:
                mess = "Buyer already maker";
                break;
            case Transaction.INVALID_AMOUNT:
                mess = "Invalid amount";
                break;
            case Transaction.INVALID_AMOUNT_IS_NULL:
                mess = "Invalid amount is ZERO";
                break;
            case Transaction.INVALID_TITLE_LENGTH_MIN:
                mess = "Invalid title MIN length";
                break;
            case Transaction.INVALID_TITLE_LENGTH_MAX:
                mess = "Invalid title MAX length";
                break;
            case Transaction.INVALID_MESSAGE_FORMAT:
                mess = "Invalid message format as data";
                break;
            case Transaction.INVALID_MESSAGE_LENGTH:
                mess = "Invalid message length";
                break;
            case Transaction.UNKNOWN_PUBLIC_KEY_FOR_ENCRYPT:
                mess = "Unknown public key (need for encrypt)";
                break;
            case Transaction.ENCRYPT_DENIED_FOR_DAPP:
                mess = "Encryption is denied for dApps";
                break;


            case Transaction.HASH_ALREADY_EXIST:
                mess = "HASH_ALREADY_EXIST";
                break;
            case Transaction.NOT_TRANSFERABLE_ASSET:
                mess = "Not transferable asset";
                break;

            case Transaction.WRONG_SIGNER:
                mess = "Wrong Signer";
                break;
            case Transaction.INVALID_BALANCE_POS:
                mess = "Invalid balance Position";
                break;
            case Transaction.INVALID_BALANCE_SIDE:
                mess = "Invalid balance Side";
                break;

            case Transaction.NAME_NOT_LOWER_CASE:
                mess = "Name not lower case";
                break;
            case Transaction.INVALID_ICON_LENGTH_MIN:
                mess = "Invalid icon MIN length";
                break;
            case Transaction.INVALID_ICON_LENGTH_MAX:
                mess = "Invalid icon MAX length";
                break;
            case Transaction.INVALID_ICON_TYPE:
                mess = "Invalid icon type";
                break;

            case Transaction.INVALID_IMAGE_TYPE:
                mess = "Invalid image type";
                break;
            case Transaction.INVALID_IMAGE_LENGTH_MIN:
                mess = "Invalid image MIN length";
                break;
            case Transaction.INVALID_IMAGE_LENGTH_MAX:
                mess = "Invalid image MAX length";
                break;

            case Transaction.INVALID_DESCRIPTION_LENGTH_MIN:
                mess = "Invalid description MIN length";
                break;
            case Transaction.INVALID_DESCRIPTION_LENGTH_MAX:
                mess = "Invalid description MAX length";
                break;
            case Transaction.INVALID_OPTIONS_LENGTH:
                mess = "Invalid options length";
                break;
            case Transaction.INVALID_OPTION_LENGTH:
                mess = "Invalid option length";
                break;
            case Transaction.DUPLICATE_OPTION:
                mess = "Invalid duplicte option";
                break;
            case Transaction.POLL_ALREADY_CREATED:
                mess = "Poll already created";
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
            case Transaction.INVALID_OUTSIDE_VALIDATY_PERIOD:
                mess = "TX.INVALID_OUTSIDE_VALIDATY_PERIOD";
                break;

            //case Transaction.ALREADY_VOTED_FOR_THAT_OPTION:
            //	mess = "Already voted for that option";
            //	break;
            case Transaction.INVALID_DATA_LENGTH:
                mess = "Invalid data length";
                break;
            case Transaction.INVALID_DATA:
                mess = "Invalid data";
                break;
            case Transaction.INVALID_DATA_FORMAT:
                mess = "Invalid data format";
                break;

            case Transaction.TX_NOT_FOUND:
                mess = "Transaction not found";
                break;

            case Transaction.INVALID_EX_LINK_TYPE:
                mess = "Invalid Link Type";
                break;
            case Transaction.INVALID_EX_LINK_REF:
                mess = "Empty or invalid 'linkTo' parameter";
                break;

            case Transaction.INVALID_URL_LENGTH:
                mess = "Invalid URL length";
                break;
            case Transaction.INVALID_PARAMS_LENGTH:
                mess = "Invalid parameters length";
                break;
            case Transaction.INVALID_SIGNATURE:
                mess = "Invalid signature";
                break;
            case Transaction.ITEM_PERSON_OWNER_SIGNATURE_INVALID:
                mess = "Invalid Person Maker signature";
                break;
            case Transaction.ITEM_PERSON_MUST_BE_SIGNED:
                mess = "Persom must be signed";
                break;
            case Transaction.TRANSACTION_DOES_NOT_EXIST:
                mess = "Transaction does not exist";
                break;

            case Transaction.NO_INCLAIM_BALANCE:
                mess = "No in claims balance";
                break;
            case Transaction.INVALID_CLAIM_RECIPIENT:
                mess = "Invalid claim Recipient: can not be the CLAIM Issuer";
                break;
            case Transaction.INVALID_CLAIM_DEBT_RECIPIENT:
                mess = "Invalid claim Recipient: claim may be only the to it Issuer";
                break;
            case Transaction.INVALID_CLAIM_DEBT_CREATOR:
                mess = "Invalid claim Creator: claim may be only to it Issuer";
                break;

            case Transaction.INVALID_AWARD:
                mess = "Invalid Award Item";
                break;
            case Transaction.INVALID_MAX_AWARD_COUNT:
                mess = "MAX Award Count";
                break;
            case Transaction.INVALID_MAX_ITEMS_COUNT:
                mess = "MAX Items Count";
                break;

            case Transaction.INVALID_QUANTITY:
                mess = "Invalid quantity";
                break;
            case Transaction.INVALID_ACCOUNTING_PAIR:
                mess = "Invalid Accounting Pair";
                break;
            case Transaction.INVALID_ECXHANGE_PAIR:
                mess = "Invalid Ecxhange Pair";
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
            case Transaction.ORDER_ALREADY_COMPLETED:
                mess = "Order already completed";
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

            case Transaction.NOT_DEBTABLE_ASSET:
                mess = "Not debtable asset";
                break;
            case Transaction.NOT_HOLDABLE_ASSET:
                mess = "Not movable asset";
                break;
            case Transaction.NOT_SPENDABLE_ASSET:
                mess = "Not spendable asset";
                break;

            case Transaction.NOT_DEBT_ASSET:
                mess = "Not debt asset";
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

            case Transaction.NOT_ENOUGH_ERA_OWN:
                mess = "Not enough " + AssetCls.ERA_ABBREV + " balance in OWN";
                break;
            case Transaction.NOT_ENOUGH_ERA_USE:
                mess = "Not enough " + AssetCls.ERA_ABBREV + " balance in USE";
                break;

            case Transaction.NOT_ENOUGH_ERA_OWN_10:
                mess = "Need 10 ERA in OWN or more";
                break;
            case Transaction.NOT_ENOUGH_ERA_USE_10:
                mess = "Need 10 ERA in USE or more";
                break;

            case Transaction.NOT_ENOUGH_ERA_OWN_100:
                mess = "Need 100 ERA in OWN or more";
                break;
            case Transaction.NOT_ENOUGH_ERA_USE_100:
                mess = "Need 100 ERA in USE or more";
                break;

            case Transaction.NOT_ENOUGH_ERA_OWN_1000:
                mess = "Need 1000 ERA in OWN or more";
                break;
            case Transaction.NOT_ENOUGH_ERA_USE_1000:
                mess = "Need 1000 ERA in USE or more";
                break;

            case Transaction.NOT_ENOUGH_ERA_OWN_10000:
                mess = "Need 10000 ERA in OWN or more";
                break;
            case Transaction.NOT_ENOUGH_ERA_USE_10000:
                mess = "Need 10000 ERA in USE or more";
                break;

            case Transaction.INVALID_ITEM_KEY:
                mess = "Invalid item key";
                break;

            case Transaction.INVALID_FLAGS:
                mess = "Invalid flags = -1";
                break;

            case Transaction.INVALID_ITEM_VALUE:
                mess = "Invalid item value";
                break;
            case Transaction.CREATOR_NOT_MAKER:
                mess = "Creator not maker";
                break;
            case Transaction.CREATOR_NOT_OWNER:
                mess = "Creator not owner";
                break;

            case Transaction.ITEM_DOES_NOT_EXIST:
                mess = "Item does not exist";
                break;

            case Transaction.OWNER_NOT_PERSONALIZED:
                mess = "Owner Account is not personalized";
                break;
            case Transaction.CREATOR_NOT_PERSONALIZED:
                mess = "Creator Account is not personalized";
                break;
            case Transaction.INVALID_PERSONALIZY_ANOTHER_PERSON:
                mess = "Public Key already certified to another Person";
                break;
            case Transaction.PUB_KEY_NOT_PERSONALIZED:
                mess = "Public Key is not personalized";
                break;

            case Transaction.RECEIVER_NOT_PERSONALIZED:
                mess = "Receiver Account is not personalized";
                break;
            case Transaction.ITEM_DUPLICATE_KEY:
                mess = "Duplicate key";
                break;
            case Transaction.ITEM_DUPLICATE:
                mess = "ITEM_DUPLICATE";
                break;
            case Transaction.INVALID_TIMESTAMP_START:
                mess = "Invalid start timestamp item";
                break;
            case Transaction.INVALID_TIMESTAMP_END:
                mess = "Invalid end timestamp";
                break;
            case Transaction.INVALID_ASSET_TYPE:
                mess = "Invalid asset type (not unique?)";
                break;
            case Transaction.ANONIM_OWN_DENIED:
                mess = "ANONIM_OWN_DENIED";
                break;

            case Transaction.INVALID_CREATOR:
                mess = "Invalid creator";
                break;
            case Transaction.ITEM_PERSON_IS_DEAD:
                mess = "Person is Dead";
                break;
            case Transaction.INVALID_TRANSFER_TYPE:
                mess = "Invalid transfer type";
                break;

            case Transaction.ITEM_ASSET_NOT_EXIST:
                mess = "Item asset does not exist";
                break;
            case Transaction.ITEM_IMPRINT_DOES_NOT_EXIST:
                mess = "Item imprint does not exist";
                break;
            case Transaction.ITEM_TEMPLATE_NOT_EXIST:
                mess = "Item template does not exist";
                break;
            case Transaction.ITEM_PERSON_NOT_EXIST:
                mess = "Item person does not exist";
                break;
            case Transaction.ITEM_POLL_NOT_EXIST:
                mess = "Item poll does not exist";
                break;
            case Transaction.ITEM_STATUS_NOT_EXIST:
                mess = "Item status does not exist";
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

            case Transaction.AMOUNT_LENGHT_SO_LONG:
                mess = "Amount accuracy so big";
                break;
            case Transaction.AMOUNT_SCALE_SO_BIG:
                mess = "Amount scale so big";
                break;
            case Transaction.AMOUNT_SCALE_WRONG:
                mess = "Amount point scale wrong";
                break;

            case Transaction.INVALID_PACKET_SIZE:
                mess = "Invalid packet size";
                break;

            case Transaction.BLOCK_NOT_EXIST:
                mess = "Block not exist";
                break;
            case Transaction.INVALID_BLOCK_HEIGHT:
                mess = "Invalid block height";
                break;
            case Transaction.INVALID_BLOCK_TRANS_SEQ_ERROR:
                mess = "Invalid block record sequence";
                break;
            case Transaction.ACCOUNT_ACCSES_DENIED:
                mess = "Access denied for account";
                break;
            case Transaction.ACTION_DENIED:
                mess = "Access denied";
                break;
            case Transaction.KEY_COLLISION:
                mess = "Key collision, try again";
                break;
            case Transaction.TELEGRAM_DOES_NOT_EXIST:
                mess = "Telegram does not exist";
                break;

            case Transaction.INVALID_TRANSACTION_TYPE:
                mess = "Invalid transaction type";
                break;

            case Transaction.JSON_ERROR:
                mess = "Invalid JSON";
                break;

        }
        return mess;
    }

}
