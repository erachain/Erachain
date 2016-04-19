package utils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import controller.Controller;
import core.account.Account;
import core.account.PrivateKeyAccount;
import core.crypto.Base58;
import core.crypto.Crypto;
import core.payment.Payment;
import core.transaction.ArbitraryTransactionV3;
import core.transaction.MessageTransaction;
import core.transaction.Transaction;
import gui.transaction.ArbitraryTransactionDetailsFrame;
import gui.transaction.MessageTransactionDetailsFrame;
import ntp.NTP;

public class txWindowTest {

	byte FEE_POWER = 0;
	
	@Test
	public void windowTest() {
		
		new MessageTransactionDetailsFrame(
				(MessageTransaction) Controller.getInstance().getTransaction(Base58.decode("2kGG3Nmu2VNatZ8MAL1PF5r3VUZyY5FsbPve9G2zJ1UL1x3NHDU96VFWn2cXvqHnvdjvY2jt3kuGTkgabr2JQXAx"))
			);
		
		
		Account recipient1 = new Account("QfreeNWCeaU3BiXUxktaJRJrBB1SDg2k7o");		
		Account recipient2 = new Account("QbVq5kgfYY1kRh9EdLSQfR9XHxVy1fLstQ");		
		Account recipient3 = new Account("QcJCST3wT8t22jKM2FFDhL8zKiH8cuBjEB");		

		List<Payment> payments = new ArrayList<Payment>();
		payments.add(new Payment(recipient1, 61l, BigDecimal.valueOf(110).setScale(8)));
		payments.add(new Payment(recipient2, 61l, BigDecimal.valueOf(120).setScale(8)));
		payments.add(new Payment(recipient3, 61l, BigDecimal.valueOf(201).setScale(8)));
		
		byte[] seed = Crypto.getInstance().digest("test".getBytes());

		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		
		byte[] data = "test123!".getBytes();
		
		PrivateKeyAccount creator = new PrivateKeyAccount(privateKey);
		
		ArbitraryTransactionV3 arbitraryTransactionV3 = new ArbitraryTransactionV3(
				creator, payments, 111,
				data,
				FEE_POWER, 
				//Transaction.getPOWFIX_RELEASE(),
				NTP.getTime(),
				new byte[]{0}
				);
		
		new ArbitraryTransactionDetailsFrame(
				arbitraryTransactionV3
			);
	}
	
}
