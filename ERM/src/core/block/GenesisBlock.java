package core.block;

// import org.apache.log4j.Logger;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
//import java.sql.Timestamp;
//import org.mapdb.Fun.Tuple3;
//import org.mapdb.Fun.Tuple11;
import java.util.TreeSet;

import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple3;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;

import core.account.Account;
import core.account.PublicKeyAccount;
import core.BlockChain;
import core.crypto.Base58;
import core.crypto.Crypto;
import core.item.assets.AssetCls;
//import core.item.assets.AssetCls;
import core.item.assets.AssetVenture;
import core.item.notes.NoteCls;
import core.item.notes.Note;
import core.item.persons.PersonCls;
import core.item.persons.PersonHuman;
import core.item.statuses.Status;
import core.item.statuses.StatusCls;
import core.transaction.Transaction;
import core.transaction.GenesisCertifyPersonRecord;
import core.transaction.GenesisIssueAssetTransaction;
import core.transaction.GenesisIssueNoteRecord;
import core.transaction.GenesisIssuePersonRecord;
import core.transaction.GenesisIssueStatusRecord;
import core.transaction.GenesisTransferAssetTransaction;
import database.DBSet;
import gui.Gui;
import lang.Lang;
import settings.Settings;
import utils.Pair;
import utils.SysTray;

public class GenesisBlock extends Block{
	
	private static int genesisVersion = 0;
	private static byte[] genesisReference = Bytes.ensureCapacity(new byte[]{19,66,8,21,0,0,0,0}, Crypto.SIGNATURE_LENGTH, 0);
	public final static int GENESIS_GENERATING_BALANCE = BlockChain.GENESIS_ERMO_TOTAL; // starting max volume for generating	
	//public static final long MAX_GENERATING_BALANCE = GENESIS_GENERATING_BALANCE / 2;
	public static final int MIN_GENERATING_BALANCE = 100;
	public static final BigDecimal MIN_GENERATING_BALANCE_BD = new BigDecimal(MIN_GENERATING_BALANCE);
	//public static final int GENERATING_RETARGET = 10;
	public static final int GENERATING_MIN_BLOCK_TIME = 288;
	public static final int GENERATING_MAX_BLOCK_TIME = 1000;
	public static final int MAX_BLOCK_BYTES = 4 * 1048576;

	private static byte[] icon = new byte[0];
	private static byte[] image = new byte[0];

	private String testnetInfo; 
	private long genesisTimestamp;
	
	AssetVenture asset0;
	AssetVenture asset1;
	List<Transaction> transactions = new ArrayList<Transaction>();
	public static final PublicKeyAccount CREATOR = new PublicKeyAccount(new byte[PublicKeyAccount.PUBLIC_KEY_LENGTH]);


	public GenesisBlock()
	{
		//SET HEADER
		super(genesisVersion, genesisReference, CREATOR, new byte[0], new byte[0]);
		
		this.genesisTimestamp = Settings.getInstance().getGenesisStamp();
		this.generatingBalance = BlockChain.GENERAL_ERMO_BALANCE;
		
		Account recipient;
		BigDecimal bdAmount0;
		BigDecimal bdAmount1;
		//PublicKeyAccount issuer = new PublicKeyAccount(new byte[Crypto.HASH_LENGTH]);
		PersonCls user;
		

		// ISSUE ITEMS
		this.initItems();
		
		if(genesisTimestamp != BlockChain.DEFAULT_MAINNET_STAMP) {
			this.testnetInfo = ""; 
			
			//ADD TESTNET GENESIS TRANSACTIONS
			this.testnetInfo += "\ngenesisStamp: " + String.valueOf(genesisTimestamp);	

			byte[] seed = Crypto.getInstance().digest(Longs.toByteArray(genesisTimestamp));

			this.testnetInfo += "\ngenesisSeed: " + Base58.encode(seed);
			
			bdAmount0 = new BigDecimal(GENESIS_GENERATING_BALANCE * 0.1).setScale(8);
			bdAmount1 = new BigDecimal(asset1.getQuantity() * 0.1).setScale(8);
			for(int nonce=0; nonce<3; nonce++)
		    {
				byte[] accountSeed = generateAccountSeed(seed, nonce);
				
				Pair<byte[], byte[]> keyPair = Crypto.getInstance().createKeyPair(accountSeed);
				byte[] publicKey = keyPair.getB();
				String address = Crypto.getInstance().getAddress(publicKey);
				recipient = new Account(address);

				user = new PersonHuman(recipient,
						"UNKNOWN", "1966-08-21 0:10:10.0", null, (byte)1, "-", (float)0.0, (float)0.0,
						"-", "-", "-", (int) 188, icon, image, "-");
				

				//CREATE ISSUE PERSON TRANSACTION
				//this.addTransaction(new GenesisIssuePersonRecord(user));

				// CERTIFY PERSON
				transactions.add(new GenesisCertifyPersonRecord(recipient, nonce++));

				this.testnetInfo += "\ngenesisAccount(" + String.valueOf(nonce) + "): " + address +  " / POST addresses " + Base58.encode(accountSeed);

				// SEND GENESIS ASSETS
				transactions.add(new GenesisTransferAssetTransaction(recipient, AssetCls.ERMO_KEY, bdAmount0));
				transactions.add(new GenesisTransferAssetTransaction(recipient, AssetCls.FEE_KEY, bdAmount1));
		    }
			this.testnetInfo += "\nStart the other nodes with command" + ":";
			this.testnetInfo += "\njava -Xms512m -Xmx1024m -jar ERM.jar -testnet=" + genesisTimestamp;
			
		} else {

			List<Tuple2<Account, BigDecimal>> sends_toUsers = new ArrayList<Tuple2<Account, BigDecimal>>();
			
			/*
			 */
			///////// GENEGAL
			List<List<Object>> generalGenesisUsers = Arrays.asList(
					Arrays.asList("7R2WUFaS7DF2As6NKz13Pgn9ij4sFw6ymZ", "1000000"),
					Arrays.asList("7EpDngzSLXrqnRBJ5x9YKTU395VEpsz5Mz", "990000"),
					Arrays.asList("7Psb8dEDd4drdHxJvd4bFihembSWBJQDvC", "980000"),
					Arrays.asList("7B3gTXXKB226bxTxEHi8cJNfnjSbuuDoMC", "970000"),
					Arrays.asList("77QnJnSbS9EeGBa2LPZFZKVwjPwzeAxjmy", "960000"),
					Arrays.asList("78JFPWVVAVP3WW7S8HPgSkt24QF2vsGiS5", "950000"),
					Arrays.asList("7R4jwh5C83HLj7C1FiSbsGptMHqfAirr8R", "940000"),
					Arrays.asList("75hXUtuRoKGCyhzps7LenhWnNtj9BeAF12", "930000"),
					Arrays.asList("7Dwjk4TUB74CqW6PqfDQF1siXquK48HSPB", "782000")
					);
			/////////// MAJOR
			List<List<Object>> majorGenesisUsers = Arrays.asList(
					/*
					Arrays.asList(1000, new PersonHuman(new Account("7FoC1wAtbR9Z5iwtcw4Ju1u2DnLBQ1TNS7"),
							"Симанков, Дмитрий", "1966-08-21", null,
							(byte)1, "европеец-славянин", (float)43.1330, (float)131.9224,
							"белый", "серо-зеленый", "серо-коричневый", (int) 188, icon, image, "-")),
							*/
				);
			////////// MINOR
			List<List<Object>> minorGenesisUsers = Arrays.asList(
					/*
					Arrays.asList(100, new PersonHuman(new Account("73CcZe3PhwvqMvWxDznLAzZBrkeTZHvNzo"),
							"неизвестный участник", "1966-08-21",  null,
							(byte)1, "европеец-славянин", (float)0.0, (float)0.0,
							"белый", "серо-зеленый", "серо-коричневый", (int) 188, icon, image, "-")),
							*/
					);
			List<PersonCls> personGenesisUsers = Arrays.asList(
					/*
					new PersonHuman(CREATOR,
							"Менделеев, Дмитрий Иванович", "1834-02-08", "1907-02-02",
							(byte)1, "европеец-славянин", (float)58.195278, (float)68.258056,
							"белый", "серо-зеленый", "серо-коричневый", (int) 180, icon, image, "русский учёный-энциклопедист: химик, физикохимик, физик, метролог, экономист, технолог, геолог, метеоролог, нефтяник, педагог, воздухоплаватель, приборостроитель. Профессор Санкт-Петербургского университета; член-корреспондент по разряду «физический» Императорской Санкт-Петербургской Академии наук. Среди наиболее известных открытий — периодический закон химических элементов, один из фундаментальных законов мироздания, неотъемлемый для всего естествознания. Автор классического труда «Основы химии».")
							*/
					);

			////////// INVESTORS ICO 10%
			List<List<Object>> genesisInvestors = Arrays.asList(
					Arrays.asList("7J1S62H1YrVhPcLibcUtA2vFACMtiLakMA", "1289.69596627"),
					Arrays.asList("73igNXcJbLZxoM989B2yj4214oztMHoLGc", "43.84966285"),
					Arrays.asList("7A9FFw3mQfDrP9y8WCifrZ3pvsKwerkMLr", "1289.69596627"),
					Arrays.asList("7QqeSR442vstwcf5Hzm3t2pWgqupQNxRTv", "257.93919325"),
					Arrays.asList("7CbRHH27V9xsaqKfTzSqNwNFhxKLhbf4g5", "41.27027092"),
					Arrays.asList("77fdZVgXhnebykEmhuEkkxYxs7nFoTEWdP", "2579.39193253"),
					Arrays.asList("74MxuwvW8EhtJKZqF7McbcAMzu5V5bnQap", "257.93919325"),
					Arrays.asList("7Cp622VhpUwpzWnzDV3XyPepVM5AF682UF", "1289.69596627"),
					Arrays.asList("76UjGyQ4TG9buoK8yQ1PmW2GE6PoPAEDZw", "1289.69596627"),
					Arrays.asList("7PnyFvPSVxczqueXfmjtwZNXN54vU9Zxsw", "92858.1095712"),
					Arrays.asList("7ANHQck4rANJ5K2RsF1aAGYYTyshpFP4cM", "1131.83718"),
					Arrays.asList("78HfjphyuwWkLw7jMymcTM3UsRdXCE5auq", "257.93919325"),
					Arrays.asList("CmmGpEbumf3FspKEC9zTzpFTk86ibLRwEbqxZ3GuAykL", "154.76351595"),
					Arrays.asList("7JU8UTuREAJG2yht5ASn7o1Ur34P1nvTk5", "121231.420829"),
					Arrays.asList("7DedW8f87pSDiRnDArq381DNn1FsTBa68Y", "141866.556289"),
					Arrays.asList("7EMFYDJW2mxBAPDgWsVbAULMSx5BzhC9tq", "1870.05915109"),
					Arrays.asList("7GWr8njMyjkDs1gdRAgQ6MaEp2DMkK26h7", "25793.9193253"),
					Arrays.asList("7JMtB4zjEyig1sfBqFTHdvuPqvbtmaQcvL", "859.71133111"),
					Arrays.asList("7LPhKZXmd6miLE9XxWZciabydoC8vf4f64", "3353.20951229"),
					Arrays.asList("7Gdt8ZdiFuHrwe9zdLcsE1cKtoFVLDqyho", "2298.23821189"),
					Arrays.asList("788AwMejUTX3tEcD5Leym8TTzKgbxVSgzr", "210.47838169"),
					Arrays.asList("77QMFKSdY4ZsG8bFHynYdFNCmis9fNw5yP", "232145.273928"),
					Arrays.asList("79MXwfzHPDGWoQUgyPXRf2fxKuzY1osNsg", "15476.3515952"),
					Arrays.asList("7AfGz1FJ6tUnxxKSAHfcjroFEm8jSyVm7r", "1031.75677301"),
					Arrays.asList("7PrZEW6ZdkZDj5GMCCp918n7EbyHVf3mRa", "128.96959663"),
					Arrays.asList("7K4XaDVf98J1fKDdCS8oYofYgFgoezFEAA", "1289.69596627"),
					Arrays.asList("7Pw2u4k2QBxrrUYsoaaBTCkdsYDK7wvS1X", "7093.32781447"),
					Arrays.asList("77HyuCsr8u7f6znj2Lq8gXjK6DCG7osehs", "1786.22891328"),
					Arrays.asList("79VxiuxRgFTp8cUTRDBAoZPGXEdqY7hD8h", "257.93919325"),
					Arrays.asList("7NeUmKbZadHLwS9FfLdhFL4ymVYSieF9Uc", "128.96959663"),
					Arrays.asList("7LETj4cW4rLWBCN52CaXmzQDnhwkEcrv9G", "335320.951229"),
					Arrays.asList("7ARdsYAd4c92mHUofN7fLS8C3VeMwbTJAr", "141.86655629"),
					Arrays.asList("7EM7P1neMZkw2EXr2kn15XMixfYVVTwvWF", "1418.66556289"),
					Arrays.asList("7LcwdEBZWVyFyaFhFoGC3SUxyqH5Uo9Zrq", "1432.85221852"),
					Arrays.asList("79qUjyTW4VoSgMKpF2dLW9eCwGVTSSnP2H", "502.98142684")
					);

			////////// ACTIVISTS
			List<List<Object>> genesisActivists = Arrays.asList(
					Arrays.asList("7KcBS1bmK1NiYwJD1mgwhz1ZFWESviQthG", "1231.5270936"),
					Arrays.asList("78Eo2dL898wzqXBn6zbGanEnwXtdDF2BWV", "1231.5270936"),
					Arrays.asList("73igNXcJbLZxoM989B2yj4214oztMHoLGc", "1231.5270936"),
					Arrays.asList("7PnyFvPSVxczqueXfmjtwZNXN54vU9Zxsw", "1231.5270936"),
					Arrays.asList("7D7S5veDCiAwvBCkoK4G2YqdXC4dZ3SH1Q", "1231.5270936"),
					Arrays.asList("74MxuwvW8EhtJKZqF7McbcAMzu5V5bnQap", "129310.344828"),
					Arrays.asList("7FPm2tet9HTVmBMe5xvRzp4sWoS6d8PgWZ", "1231.5270936"),
					Arrays.asList("78cK2QS34j8cPLWwHDqCBy36ZmikiCzLcg", "1231.5270936"),
					Arrays.asList("79gQ4iB4Cs8EkhrUanEiDQtKArt6k6NAdu", "1231.5270936"),
					Arrays.asList("7Kh5KvHCuWAq8XHioKyUBZxRmbwCJZV5b2", "123152.70936"),
					Arrays.asList("74Rcp979npxf6Q5zV6ZnpEnsxrsCHdXeNU", "1231.5270936"),
					Arrays.asList("78HfjphyuwWkLw7jMymcTM3UsRdXCE5auq", "2463.05418719"),
					Arrays.asList("78JFPWVVAVP3WW7S8HPgSkt24QF2vsGiS5", "123152.70936"),
					Arrays.asList("7DedW8f87pSDiRnDArq381DNn1FsTBa68Y", "1231.5270936"),
					Arrays.asList("7K4XaDVf98J1fKDdCS8oYofYgFgoezFEAA", "1231.5270936"),
					Arrays.asList("7Cy2J5ST6ukHSJVWtQd7eH4wbhbSBbMbZD", "1231.5270936"),
					Arrays.asList("7DRH1MjEo3GgtySGsXjzfdqeQYagutXqeP", "1231.5270936"),
					Arrays.asList("74rRXsxoKtVKJqN8z6t1zHfufBXsELF94y", "2463.05418719"),
					Arrays.asList("7A9FFw3mQfDrP9y8WCifrZ3pvsKwerkMLr", "1231.5270936"),
					Arrays.asList("7MPmVWSobucE6TdJvnEeohFAZnCej7fr2F", "1231.5270936"),
					Arrays.asList("75qZ6ncf5T4Gkz1vrwkqjCPJ1A5gr2Cyah", "1231.5270936"),
					Arrays.asList("7JNUfHeuCRLApKX9MievkAoGdFgVfBf7DE", "1231.5270936"),
					Arrays.asList("7Fgkw8cuPiTc4LVRvkYBuXEEfGYxrg6XiX", "1231.5270936"),
					Arrays.asList("75rVEuvpzhLJznkXZaYyxJq8L9pVCeqFbk", "1231.5270936"),
					Arrays.asList("7J3M8xwJeG5gyBC5kLPb5c2kVHoTsMT5MK", "1231.5270936"),
					Arrays.asList("75rEoNUknMU3qYGjS3wriY53n1aRUznFus", "1231.5270936"),
					Arrays.asList("73dXJb1orwqk1ADW364KEAzPVQNGa1vX9S", "61576.3546798"),
					Arrays.asList("7CPGk25mTFGhANaBCiV4LqrowcUfrfLcRe", "1231.5270936"),
					Arrays.asList("78KCkgNeSvxwtnVJTyzLFGGzmP8SUUuN1J", "1231.5270936"),
					Arrays.asList("7AJNCwQvbEbGn7Mt3mzPHbK1Zxvy9t6xtA", "1231.5270936"),
					Arrays.asList("77GYw61CPhDhdHsHg8oYCaKhenq2izAps8", "1231.5270936"),
					Arrays.asList("7NeUmKbZadHLwS9FfLdhFL4ymVYSieF9Uc", "1231.5270936"),
					Arrays.asList("73yfeCDiSciBF1vc3PG8uyJMty4jRDxxL9", "1231.5270936"),
					Arrays.asList("7AXey16ivPRCQoFWzkMU4Q7V8FZugqjYUX", "18472.9064039"),
					Arrays.asList("7GWr8njMyjkDs1gdRAgQ6MaEp2DMkK26h7", "3694.58128079"),
					Arrays.asList("7HWxbcgVRxzdxDiVj9oc5ZG39a93imLUWz", "1231.5270936"),
					Arrays.asList("79qUjyTW4VoSgMKpF2dLW9eCwGVTSSnP2H", "1231.5270936")
					);

			// genesis users
			List<List<Object>> genesisDebtors = Arrays.asList(
					Arrays.asList("7MdXzNcKgWXvy7unJ7WPLmp3LQvUdiNEAz", 2),
					Arrays.asList("73igNXcJbLZxoM989B2yj4214oztMHoLGc", 2),
					Arrays.asList("8Q7zyxx1rYKBbiKVfs66H5G16Vtsag54wCHnV2tHY5nA", 2),
					Arrays.asList("74fCzX79v5etyt1pjtfAQhyrLCRSVfm6AM", 2),
					Arrays.asList("7BbrDtJWt9WYfoFQg9VV4aW2yVdaQpsjH9", 2),
					Arrays.asList("7NZAQieFR3Qyzzj8iZhWHHPZJ9D2TPW7uR", 2),
					Arrays.asList("73shRmoD4YNAtMKzF8ZnFtsYVx4hx9cShi", 2),
					Arrays.asList("7EwDnU3F8znwp3bsFq1W5NA1b3YiEwYt7N", 3),
					Arrays.asList("3r1fXZPBcVf2acj5ELhEZ4uYGPdZoYFwY4bYS4qeG22F", 1),
					Arrays.asList("7QF8kYdmv1dqT548S6HjSvgdtF7txncvbr", 2),
					Arrays.asList("3dLWtiRPb9PYDXbb6t1P9jmVqDkmZnBiMtYsCSJWWw3S", 1),
					Arrays.asList("7FXqM9Lq9wGJkjpduXoYuBqLzrkHQqhH78", 2),
					Arrays.asList("74Rcp979npxf6Q5zV6ZnpEnsxrsCHdXeNU", 2),
					//Arrays.asList("78JFPWVVAVP3WW7S8HPgSkt24QF2vsGiS5", 2),
					Arrays.asList("7APgaQe1uiG8Vgzz5bZCTPw39mTwjPfgLF", 2),
					Arrays.asList("79VxiuxRgFTp8cUTRDBAoZPGXEdqY7hD8h", 3),
					Arrays.asList("2B8NRmXsVJ3zSxS5Px78h7qieb5Yvp3XRnyzzK2qk8kq", 2),
					Arrays.asList("7JJjBJqpySJmoJws6xSDsvwRo5yKS7wneg", 2),
					Arrays.asList("78KCkgNeSvxwtnVJTyzLFGGzmP8SUUuN1J", 3),
					Arrays.asList("76JECepZ1DYWqbF4Vb2VZMBs4A6WsvrN53", 1),
					Arrays.asList("7J3M8xwJeG5gyBC5kLPb5c2kVHoTsMT5MK", 3),
					Arrays.asList("7KstpqkbQrCiAuVD3WBQSitbDvoCUTh2D6", 2),
					Arrays.asList("7CPGk25mTFGhANaBCiV4LqrowcUfrfLcRe", 3),
					Arrays.asList("7HLmWov2KYx4MBBceN1KqYom6m97ppp2wF", 2),
					Arrays.asList("7JRYHaNtKshTbAMdbGALjmScB2c1NksKD4", 2),
					Arrays.asList("7AXey16ivPRCQoFWzkMU4Q7V8FZugqjYUX", 5),
					Arrays.asList("77GYw61CPhDhdHsHg8oYCaKhenq2izAps8", 5),
					Arrays.asList("7JAmAzeehdP5JWspXodhQR31dVqhKDR8sj", 2),
					Arrays.asList("77Atk56iAvdFgayLyC6EbfkBLmcJNBQzrh", 1),
					Arrays.asList("73yfeCDiSciBF1vc3PG8uyJMty4jRDxxL9", 4),
					Arrays.asList("78F5m5oUf1N4iZB7XPdWmQJdwMT3tWMQ2j", 2),
					Arrays.asList("7MoR3qqs959XAnDQ8mDr11sBnKs4woogkK", 2),
					Arrays.asList("7C17PgbPTJeju3yJPbw4Wmus9gj8Jeo3TF", 2),
					Arrays.asList("79qUjyTW4VoSgMKpF2dLW9eCwGVTSSnP2H", 2),
					//Arrays.asList("https://vk.com/away.php?to=http%3A%2F%2Fdatachains.world%2Fs", 2),
					Arrays.asList("7CqCmqYwCqCDe599vHeWLdL4YSi1ShYg2r", 2),
					Arrays.asList("75Uej5KmQVmmqHusKfj3zP3AoZ5wkyyu7E", 2),
					Arrays.asList("7McczL4B1xfNnDRhjqrgQKdtXVp7YdKYVM", 2),
					Arrays.asList("75R3LayKe3orQrtZnMWR1VdadBdypj2NWW", 3),
					Arrays.asList("741kxf9sRgRk2JZfEpxt2D9NcooUCRAj2m", 2),
					Arrays.asList("7Luf2TRvoQuxaQriWmB1G9DgsZ6b1Pfith", 1),
					Arrays.asList("7FPm2tet9HTVmBMe5xvRzp4sWoS6d8PgWZ", 1),
					Arrays.asList("7G9QBw7TBgB9DLVcmDARmCBLX2yaLNnzXS", 2),
					Arrays.asList("7SErqYci2YesFsg4zcxowJ62G9LDk6mic3", 2),
					Arrays.asList("78HfjphyuwWkLw7jMymcTM3UsRdXCE5auq", 2),
					Arrays.asList("7DwK6UC648aGPKyGboQeU6WMCNhUSYAxHb", 2),
					Arrays.asList("7MKdGrRFzrmy3KLNmcrS7PmfJn6KYerR3G", 2),
					Arrays.asList("7LDPFrsPUFedgGsomXACwh7qM2qdcRGso7", 1),
					Arrays.asList("7KVfBqULZJx2DgbBHrkf1pZzkJMVDru8pB", 2),
					Arrays.asList("ByVNbfq6xp7AFqEH1bbacFr9eTVPGnxLQboyv46WqZra", 3),
					Arrays.asList("7PPpw4H1UQm865jxe9FpTKBzC3fBULXN1w", 4),
					Arrays.asList("EFLHKpYpQXZYUaJJ1mgMZ8H7i1jwoGXRojVV84rTA49h", 2),
					Arrays.asList("7S1LnztovJEgYWS4MKLNyccZVriaFiJjUL", 2),
					Arrays.asList("75rEoNUknMU3qYGjS3wriY53n1aRUznFus", 2),
					Arrays.asList("75v2xRHKypQqqSM4pwQs9pq49ZL9rxYg8B", 2),
					Arrays.asList("7PrskypEaZWX4nqN19BJQkssKEdaJcvaVu", 1),
					Arrays.asList("7HgZk85BA5VxATncBRqgYRZ84mWDFpD8jS", 1),
					Arrays.asList("7AAyNfFFGipUXVdRLwWJnhUwfj9FDqnJ2z", 2),
					Arrays.asList("77HyuCsr8u7f6znj2Lq8gXjK6DCG7osehs", 3),
					Arrays.asList("7RSLd62fpgBW5PyaGHLNh8rHZQbmRqcret", 2),
					Arrays.asList("7NLEQV71W4X9YqopA15k5VNk2WFiKc3ePE", 2),
					Arrays.asList("7MJyC8L6AQGtckhJaF4BS1MiMQHBeuk5ss", 1),
					Arrays.asList("1A3P7u56G4NgYfsWMms1BuctZfnCeqrYk3", 1),
					Arrays.asList("7D9mKfdvXwgTpogHN1KTGmF78PjteidPA6", 2)
					);

			// TRANSFERS
			// 

			
			for(List<Object> item: generalGenesisUsers)
			{
				
				recipient = new Account((String)item.get(0));
				
				bdAmount0 = new BigDecimal((String)item.get(1)).setScale(8);
				transactions.add(new GenesisTransferAssetTransaction(recipient, AssetCls.ERMO_KEY, bdAmount0));
				
				// buffer for CREDIT sends
				sends_toUsers.add(new Tuple2<Account, BigDecimal>(recipient, bdAmount0));

				bdAmount1 = BigDecimal.ONE.setScale(8);
				transactions.add(new GenesisTransferAssetTransaction(recipient, AssetCls.FEE_KEY, bdAmount1));

			}


			// NOT PERSONALIZE INVESTORS - ICO 10%
			for(List<Object> item: genesisInvestors)
			{
				
				//recipient = new Account((String)item.get(0));
				if (((String)item.get(0)).length() > 36 ) {
					recipient = new PublicKeyAccount((String)item.get(0));					
				} else {
					recipient = new Account((String)item.get(0));
				}
				
				bdAmount0 = new BigDecimal((String)item.get(1)).setScale(8);
				transactions.add(new GenesisTransferAssetTransaction(recipient, AssetCls.ERMO_KEY, bdAmount0));

			}			

			// ACTIVITES
			for(List<Object> item: genesisActivists)
			{
				
				recipient = new Account((String)item.get(0));
				
				bdAmount0 = new BigDecimal((String)item.get(1)).setScale(8);
				transactions.add(new GenesisTransferAssetTransaction(recipient, AssetCls.ERMO_KEY, bdAmount0));

			}			

			// FOR DEBROTS
			int nonce = 0;
			int i = 0;
			int pickDebt = 19000;
			BigDecimal limitOwned = new BigDecimal( 0.001 * GENESIS_GENERATING_BALANCE).setScale(8);

			Account bufferCreditor = sends_toUsers.get(i).a;
			BigDecimal bufferAmount = sends_toUsers.get(i).b.subtract(limitOwned);
			for(List<Object> item: genesisDebtors)
			{
				
				if (((String)item.get(0)).length() > 36 ) {
					recipient = new PublicKeyAccount((String)item.get(0));					
				} else {
					recipient = new Account((String)item.get(0));
				}
				
				bdAmount0 = new BigDecimal((int)item.get(1) * pickDebt + nonce++).setScale(8);

				do {
					if (bufferAmount.compareTo(bdAmount0) < 0) {
						// REST for investor!
						////transactions.add(new GenesisTransferAssetTransaction(recipient, -AssetCls.ERMO_KEY,
						////		bufferAmount, bufferCreditor));
						bdAmount0 = bdAmount0.subtract(bufferAmount);
						i++;
						bufferCreditor = sends_toUsers.get(i).a;
						bufferAmount = sends_toUsers.get(i).b;
						// TRY rest limit for self
						if (bufferAmount.compareTo(limitOwned) > 0) {
							bufferAmount = bufferAmount.subtract(limitOwned);
						}
						continue;
					} else {
						transactions.add(new GenesisTransferAssetTransaction(recipient, -AssetCls.ERMO_KEY,
								bdAmount0, bufferCreditor));
						bufferAmount = bufferAmount.subtract(bdAmount0);
						break;
					}
				} while (true);

			}			
		}
		
		//GENERATE AND VALIDATE TRANSACTIONS
		this.setTransactions(transactions);
		// SIGN simple as HASH
		this.signature = generateHeadHash();
	}
	
	private void initItems()
	{
		
		///// ASSETS
		//CREATE ERM ASSET
		asset0 = makeAsset(AssetCls.ERMO_KEY);
		transactions.add(new GenesisIssueAssetTransaction(asset0));
		//CREATE JOB ASSET
		asset1 = makeAsset(AssetCls.FEE_KEY);
		transactions.add(new GenesisIssueAssetTransaction(asset1));
		// ASSET OTHER
		for (int i = (int)AssetCls.FEE_KEY + 1; i <= AssetCls.REAL_KEY; i++) 
			transactions.add(new GenesisIssueAssetTransaction(makeAsset(i)));

		///// NOTES
		for (int i = 1; i <= NoteCls.UNHIRING_KEY; i++) 
			transactions.add(new GenesisIssueNoteRecord(makeNote(i)));

		///// STATUSES
		for (int i = 1; i <= StatusCls.MEMBER_KEY; i++)
			transactions.add(new GenesisIssueStatusRecord(makeStatus(i)));		
	}
	
	// make assets
	public static AssetVenture makeAsset(long key) 
	{
		switch((int)key)
		{
		case (int)AssetCls.FEE_KEY:
			return new AssetVenture(CREATOR, AssetCls.FEE_NAME, icon, image, AssetCls.FEE_DESCR, false, 0l, (byte)8, true);
		case (int)AssetCls.TRUST_KEY:
			return new AssetVenture(CREATOR, AssetCls.TRUST_NAME, icon, image, AssetCls.TRUST_DESCR, false, 0l, (byte)8, true);
		case (int)AssetCls.REAL_KEY:
			return new AssetVenture(CREATOR, AssetCls.REAL_NAME, icon, image, AssetCls.REAL_DESCR, false, 0l, (byte)8, true);
		//case (int)AssetCls.DEAL_KEY:
		//	return new AssetVenture(CREATOR, AssetCls.DEAL_NAME, icon, image, AssetCls.DEAL_DESCR, false, 0l, (byte)8, true);
		}
		//return new AssetVenture(genesisGenerator, AssetCls.ERMO_NAME, icon, image, AssetCls.ERMO_DESCR, false, GENESIS_GENERATING_BALANCE, (byte)0, true);
		return new AssetVenture(CREATOR, AssetCls.ERMO_NAME, icon, image, AssetCls.ERMO_DESCR, false, 0l, (byte)0, true);
	}
	// make notes
	public static Note makeNote(int key) 
	{
		switch(key)
		{
		case (int)NoteCls.MARRIAGE_KEY:
			return new Note(CREATOR, "Заявление о бракосочетании", icon, image, "Мы, %person1% и %person2%, женимся!");
		case (int)NoteCls.UNMARRIAGE_KEY:
			return new Note(CREATOR, "Заявление о разводе", icon, image, "Я, %person1%, развожусь с %person2%");
		case (int)NoteCls.HIRING_KEY:
			return new Note(CREATOR, "Заявление о приёме на работу", icon, image, "Прошу принять меня в объединение %union%, на должность %job%");
		case (int)NoteCls.UNHIRING_KEY:
			return new Note(CREATOR, "Заявление об уволнении", icon, image, "Прошу уволить меня из объединения %union% по собственному желанию");
		}
		return new Note(CREATOR, "empty", icon, image, "empty");
	}
	// make notes
	public static Status makeStatus(int key)
	{
		if (key == StatusCls.MEMBER_KEY) return new Status(CREATOR,
				"Членство", icon, image, "Уровень %1 членства в объединении %2");
		//else if (key == StatusCls.ALIVE_KEY) return new Status(CREATOR, "Alive", icon, image, "Alive or Dead");
		//else if (key == StatusCls.RANK_KEY) return new Status(CREATOR, "Rank", icon, image, "General, Major or Minor");
		//else if (key == StatusCls.USER_KEY) return new Status(CREATOR, "User", icon, image, "Admin, User, Observer");
		//else if (key == StatusCls.MAKER_KEY) return new Status(CREATOR, "Maker", icon, image, "Creator, Designer, Maker");
		//else if (key == StatusCls.DELEGATE_KEY) return new Status(CREATOR, "Delegate", icon, image, "President, Senator, Deputy");
		//else if (key == StatusCls.CERTIFIED_KEY) return new Status(CREATOR, "Certified", icon, image, "Certified, Notarized, Confirmed");
		//else if (key == StatusCls.MARRIED_KEY) return new Status(CREATOR, "Married", icon, image, "Husband, Wife, Spouse");

		return new Status(CREATOR, "Права", icon, image, "Уровень %1 прав (власти) в объединении %2");
	}
	
	
	//GETTERS

	@Override
	public long getTimestamp(DBSet db)
	{
		return this.genesisTimestamp;
	}
	
	public String getTestNetInfo() 
	{
		return this.testnetInfo;
	}
	
	@Override
	public Block getParent(DBSet db)
	{
		//PARENT DOES NOT EXIST
		return null;
	}
	/*
	@Override
	public int getGeneratingBalance()
	{
		return 0;
	}
	*/

	
	public byte[] generateHeadHash()
	{
		byte[] data = new byte[0];
		
		//WRITE VERSION
		byte[] versionBytes = Longs.toByteArray(genesisVersion);
		versionBytes = Bytes.ensureCapacity(versionBytes, 4, 0);
		data = Bytes.concat(data, versionBytes);
		
		//WRITE REFERENCE
		byte[] referenceBytes = Bytes.ensureCapacity(genesisReference, Crypto.SIGNATURE_LENGTH, 0);
		data = Bytes.concat(data, referenceBytes);
		
		
		//WRITE TIMESTAMP
		byte[] genesisTimestampBytes = Longs.toByteArray(this.genesisTimestamp);
		genesisTimestampBytes = Bytes.ensureCapacity(genesisTimestampBytes, 8, 0);
		data = Bytes.concat(data, genesisTimestampBytes);
		
		/*
		//WRITE GENERATING BALANCE
		byte[] generatingBalanceBytes = Longs.toByteArray(GENESIS_GENERATING_BALANCE);
		generatingBalanceBytes = Bytes.ensureCapacity(generatingBalanceBytes, 8, 0);
		data = Bytes.concat(data, generatingBalanceBytes);
		*/
	
		/*
		//WRITE GENERATOR
		byte[] generatorBytes = Bytes.ensureCapacity(genesisGenerator.getPublicKey(), Crypto.HASH_LENGTH, 0);
		data = Bytes.concat(data, generatorBytes);
		*/
		
		//DIGEST [32]
		byte[] digest = Crypto.getInstance().digest(data);
		
		//DIGEST + transactionsHash
		// = byte[64]
		digest = Bytes.concat(digest, transactionsHash);
		
		return digest;
	}
	
	//VALIDATE
	
	@Override
	public boolean isSignatureValid()
	{
		
		//VALIDATE BLOCK SIGNATURE
		byte[] digest = generateHeadHash();				
		if(!Arrays.equals(digest, this.signature))
		{
			return false;
		}
				
		return true;
	}
	
	/*
	@Override
	public BigDecimal getTotalFee()
	{
		return BigDecimal.ZERO.setScale(8);
	}
	*/
	
	@Override
	public boolean isValid(DBSet db)
	{
		//CHECK IF NO OTHER BLOCK IN DB
		if(db.getBlockMap().getLastBlock() != null)
		{
			return false;
		}
		
		//VALIDATE TRANSACTIONS
		byte[] transactionsSignatures = new byte[0];
		for(Transaction transaction: this.getTransactions())
		{
			if(transaction.isValid(db, null) != Transaction.VALIDATE_OK)
			{
				return false;
			}
			transactionsSignatures = Bytes.concat(transactionsSignatures, transaction.getSignature());

		}
		transactionsSignatures = Crypto.getInstance().digest(transactionsSignatures);
		if (!Arrays.equals(this.transactionsHash, transactionsSignatures)) {
			LOGGER.error("*** GenesisBlock.digest(transactionsSignatures) invalid");
			return false;
		}
		
		return true;
	}
	
	private static byte[] generateAccountSeed(byte[] seed, int nonce) 
	{		
		byte[] nonceBytes = Ints.toByteArray(nonce);
		byte[] accountSeed = Bytes.concat(nonceBytes, seed, nonceBytes);
		return Crypto.getInstance().doubleDigest(accountSeed);		
	}	
}
