package org.erachain.core.block;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import org.erachain.core.BlockChain;
import org.erachain.core.account.Account;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.crypto.Base58;
import org.erachain.core.crypto.Crypto;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.item.assets.AssetVenture;
import org.erachain.core.item.persons.PersonCls;
import org.erachain.core.item.statuses.Status;
import org.erachain.core.item.statuses.StatusCls;
import org.erachain.core.item.templates.Template;
import org.erachain.core.item.templates.TemplateCls;
import org.erachain.core.transaction.*;
import org.erachain.datachain.DCSet;
import org.erachain.settings.Settings;
import org.erachain.utils.Pair;
import org.mapdb.Fun.Tuple2;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// import org.slf4j.LoggerFactory;

//import org.erachain.core.item.assets.AssetCls;

public class GenesisBlock extends Block {

    //AssetVenture asset0;
    //AssetVenture asset1;
    public static final PublicKeyAccount CREATOR = new PublicKeyAccount(new byte[PublicKeyAccount.PUBLIC_KEY_LENGTH]);
    private static int genesisVersion = 0;
    private static byte[] genesisReference = Bytes.ensureCapacity(new byte[]{19, 66, 8, 21, 1, 2, 3, 4}, Crypto.SIGNATURE_LENGTH, 0);
    private static byte[] icon = new byte[0];
    private static byte[] image = new byte[0];
    private String testnetInfo;
    private long genesisTimestamp;

    public GenesisBlock() {

        super(genesisVersion, genesisReference, CREATOR);

        this.genesisTimestamp = Settings.getInstance().getGenesisStamp();

        Account recipient;
        BigDecimal bdAmount0;
        BigDecimal bdAmount1;

        //PublicKeyAccount issuer = new PublicKeyAccount(new byte[Crypto.HASH_LENGTH]);
        //PersonCls user;

        // ISSUE ITEMS
        this.initItems();

        if (Settings.getInstance().isTestNet()) {
            this.testnetInfo = "";

            //ADD TESTNET GENESIS TRANSACTIONS
            this.testnetInfo += "\ngenesisStamp: " + String.valueOf(genesisTimestamp);

            byte[] seed = Crypto.getInstance().digest(Longs.toByteArray(genesisTimestamp));

            this.testnetInfo += "\ngenesisSeed: " + Base58.encode(seed);

            this.testnetInfo += "\nStart the other nodes with command" + ":";
            this.testnetInfo += "\njava -Xms512m -Xmx1024m -jar erachain.jar -testnet=" + genesisTimestamp;

        } else {

            List<Tuple2<Account, BigDecimal>> sends_toUsers = new ArrayList<Tuple2<Account, BigDecimal>>();

            String[] grands = new String[]{
            "79BXYUAA29MKa9LQntiA289rCtCREi3GPu",
                    "74ULDk7gvZGMLMjhNvpNpucDUXTPVcL3ZE",
                    "7PqDCB5YTkEye3sP3WLLP3LiRXr3kxQfVR",
                    "7H1SztpZd6ApLwGvSTiW8KjsibekfZHrPN",
                    "7Pepr5XXWQcdSCLjt1NDVRRJLWqqXLsC7V",
                    "7SHsASQK1QAZfCHyFjyqTQ7Uua3HS1Tpoy",
                    "7PN4XYXRPgpfwPwuATVvBHf7CNPKofMaTy",
                    "73HyN9Tv3acqa25ShFW2DRcKQwcpebQWPS",
                    "79voG8gQWAH1AVRTNmc5kxjYKcztVjcgV5",
                    "7HmEqLLLkDTePTogxruqfcNsA1XLY5iPHS",
                    "79vTVTdFixbsmmJssfbV2csWoFfcgh5UcA",
                    "7DHnRuNLEVyhk2LXmGW4DwUQQWdhmuiyfT",
                    "7LP4wPc6pimts6NRWM52d2x2NT4xxD4CSf",
                    "7AHuzbtu8yFLwn25cj27mLchGyqbRHXVtX",
                    "7EuBJ56kDjYsdJ1AL44EfJnr9M69Eo56sv",
                    "7J45P19d2mpaj2may1hbdcdUmzPgdNFan1",
                    "7M9BmVJFCRZwQXnG1CDugnRpMzipBGVqgH",
                    "7NAJrh1MAaJw5nv3AxCEdRtF4begfQ6fHc",
                    "76QY9uLwqiejDUtDE9VFhRT8z73uvmGrmT",
                    "7S6Gz8PiyGA9pxB3QehA9HYRkPvrvP9XD1",
                    "74iPzjC8E571CYx7sXDtXg8CTLQoevZaVt",
                    "7CCE4UHCBammxH5GKS5mU4ntiQhgQ9cwP2",
                    "78CwLuNXSyswNmL4FFiAo64R7ngAkwHWEZ",
                    "7MKF2vXLVnCfu6K5gCEbXt3JPT7451r4cY",
                    "7CBJ8NxNYiZJiTeC4ZwLF67Fk5WoVXAYoW",
                    "7HthA3z22Gtac9HAV2tBRGTjjaouXZeMHD",
                    "7APA91hHTuiKbAE2Yu1LBVcH2EfuaLPJXw",
                    "7Qbz14wwCWitqbydHGrMH5NZa6CduiJcjr",
                    "7Mz88PKEaQREmVtavFjW5KxeuN2fyCWAbG",
                    "73rDQtdw1nDME6bAWeTcAFwp8Zs7ZjfKzo",
                    "7JSbAJvDPvcrrJNocRsJvQSa6zxTuFyKxg",
                    "7KBgqGpXRo8g5hJSgh16wGpy6dHS1gnRfG",
                    "7K1hh8pYpStZJs9HHu6iy5UakwampbmtE3",
                    "7McqnEqL5BvgsSqj4Z3NnE5Umk4VP6ctbJ",
                    "7GF2tVUncP2spkfPrpyRix5Y8wGwZgjoFg",
                    "78aT6QKpj4giaeLNEZXG1cE7wq2dsVXuwq",
                    "74xLeEXbFRAyZcQLjTAMTMfWMEkVwBwH6x",
                    "7Gb5rssz56uPMgMzGVA8J3Tj2E2w24whiX",
                    "76SnV5eJk39rsbdgrJiMWT2LSPDQGFxdtA",
                    "7PqKCsiQRUfgTNrBFey7ys93pFvpYCRz32"};
            /*
             */
            ///////// GENEGAL
            List<List<Object>> generalGenesisUsers = new ArrayList();

            for (String address: grands) {
                generalGenesisUsers.add(Arrays.asList(address, "100000"));
            }

            generalGenesisUsers.add(Arrays.asList("7CMGxqHPGy7MmRaJfLCAK3AApF9edcf95e", "100000"));
            generalGenesisUsers.add(Arrays.asList("7F5gYLdBYhUeseoC9ZPiA5LZkbdwkRGqWM", "100000"));

            //generalGenesisUsers.add(Arrays.asList("74ULDk7gvZGMLMjhNvpNpucDUXTPVcL3ZE", "300000"));

            // FOR ERACHAIN MASTERs
            generalGenesisUsers.add(Arrays.asList("78JFPWVVAVP3WW7S8HPgSkt24QF2vsGiS5", "500000"));

            /////////// MAJOR
            List<List<Object>> majorGenesisUsers = Arrays.asList(
            );
            ////////// MINOR
            List<List<Object>> minorGenesisUsers = Arrays.asList(
            );
            List<PersonCls> personGenesisUsers = Arrays.asList(
            );

            ////////// INVESTORS ICO 10%
            List<List<Object>> genesisInvestors = Arrays.asList(
                    //Arrays.asList("7DedW8f87pSDiRnDArq381DNn1FsTBa68Y", "333000"),
            );

            ////////// ACTIVISTS
            List<List<Object>> genesisActivists = Arrays.asList(
                    //Arrays.asList("7PChKkoASF1eLtCnAMx8ynU2sMYdSPwkGV", "1000.0"), //
            );

            // GENESIS FORGERS
            //ArrayList<String> arrayList = new ArrayList<String>(Arrays.asList(arr));
            // NEED for .add() ::
            ArrayList<List<Object>> genesisDebtors = new ArrayList<List<Object>>(Arrays.asList(
                    //Arrays.asList("7DRH1MjEo3GgtySGsXjzfdqeQYagutXqeP", 2), //
            ));

            // TRANSFERS
            //

            BigDecimal totalSended = BigDecimal.ZERO;

            for (List<Object> item : generalGenesisUsers) {

                recipient = new Account((String) item.get(0));

                bdAmount0 = new BigDecimal((String) item.get(1)).setScale(BlockChain.AMOUNT_DEDAULT_SCALE);
                transactions.add(new GenesisTransferAssetTransaction(recipient, AssetCls.ERA_KEY, bdAmount0));
                totalSended = totalSended.add(bdAmount0);

                // buffer for CREDIT sends
                sends_toUsers.add(new Tuple2<Account, BigDecimal>(recipient, bdAmount0));

                // COMPU
                if (false) {
                    bdAmount1 = BigDecimal.ONE.setScale(BlockChain.AMOUNT_DEDAULT_SCALE);
                    transactions.add(new GenesisTransferAssetTransaction(recipient, AssetCls.FEE_KEY, bdAmount1));
                }

            }

            int pickDebt = 27000;
            BigDecimal limitOwned = new BigDecimal(pickDebt * 6).setScale(BlockChain.AMOUNT_DEDAULT_SCALE);

            // NOT PERSONALIZE INVESTORS - ICO 10%
            for (List<Object> item : genesisInvestors) {

                //recipient = new Account((String)item.get(0));
                if (((String) item.get(0)).length() > 36) {
                    recipient = new PublicKeyAccount((String) item.get(0));
                } else {
                    recipient = new Account((String) item.get(0));
                }

                bdAmount0 = new BigDecimal((String) item.get(1)).setScale(BlockChain.AMOUNT_DEDAULT_SCALE);
                transactions.add(new GenesisTransferAssetTransaction(recipient, AssetCls.ERA_KEY, bdAmount0));
                totalSended = totalSended.add(bdAmount0);


                if (bdAmount0.compareTo(limitOwned) < 1) {
                    addDebt(recipient.getAddress(), 1, genesisDebtors);
                } else {
                    // buffer for CREDIT sends
                    sends_toUsers.add(new Tuple2<Account, BigDecimal>(recipient, bdAmount0));
                }
            }

            // ACTIVITES
            int nonce = genesisActivists.size() >> 1;
            for (List<Object> item : genesisActivists) {

                recipient = new Account((String) item.get(0));

                bdAmount0 = new BigDecimal((String) item.get(1)).add(new BigDecimal(nonce--)).setScale(BlockChain.AMOUNT_DEDAULT_SCALE);
                transactions.add(new GenesisTransferAssetTransaction(recipient, AssetCls.ERA_KEY, bdAmount0));
                totalSended = totalSended.add(bdAmount0);

                addDebt(recipient.getAddress(), 1, genesisDebtors);

            }

            // ADJUST end
            transactions.add(new GenesisTransferAssetTransaction(
                    new Account(grands[0]), AssetCls.ERA_KEY,
                    new BigDecimal(BlockChain.GENESIS_ERA_TOTAL).subtract(totalSended).setScale(BlockChain.AMOUNT_DEDAULT_SCALE)));


            // FOR DEBROTS
            nonce = genesisDebtors.size() >> 1;

            int i = 0;
            Account bufferCreditor = sends_toUsers.get(i).a;
            BigDecimal bufferAmount = sends_toUsers.get(i).b;

            for (List<Object> item : genesisDebtors) {

                if (((String) item.get(0)).length() > 36) {
                    recipient = new PublicKeyAccount((String) item.get(0));
                } else {
                    recipient = new Account((String) item.get(0));
                }

                bdAmount0 = new BigDecimal((int) item.get(1) * pickDebt + nonce--).setScale(BlockChain.AMOUNT_DEDAULT_SCALE);

                do {
                    if (bufferAmount.subtract(bdAmount0).compareTo(limitOwned) < 0) {
                        // use  MIN BALANCE investor!
                        BigDecimal diffLimit = bufferAmount.subtract(limitOwned);
                        bdAmount0 = bdAmount0.subtract(diffLimit);

                        transactions.add(new GenesisTransferAssetTransaction(recipient, -AssetCls.ERA_KEY,
                                diffLimit, bufferCreditor));
                        i++;
                        limitOwned = limitOwned.subtract(BigDecimal.ONE);
                        bufferCreditor = sends_toUsers.get(i).a;
                        bufferAmount = sends_toUsers.get(i).b;
                        continue;
                    } else {
                        transactions.add(new GenesisTransferAssetTransaction(recipient, -AssetCls.ERA_KEY,
                                bdAmount0, bufferCreditor));
                        bufferAmount = bufferAmount.subtract(bdAmount0);
                        break;
                    }
                } while (true);
            }
        }

        //GENERATE AND VALIDATE TRANSACTIONS
        this.transactionCount = transactions.size();

        makeTransactionsRAWandHASH();

        // SIGN simple as HASH
        this.signature = generateHeadHash();
    }

    // make assets
    public static AssetVenture makeAsset(long key) {
        switch ((int) key) {
            case (int) AssetCls.ERA_KEY:
                return new AssetVenture(CREATOR, AssetCls.ERA_NAME, icon, image, AssetCls.ERA_DESCR, 0, 8, 0l);
            case (int) AssetCls.FEE_KEY:
                return new AssetVenture(CREATOR, AssetCls.FEE_NAME, icon, image, AssetCls.FEE_DESCR, 0, 8, 0l);
        }
        return null;
    }

    // make templates
    public static Template makeTemplate(int key) {
        switch (key) {
            case (int) TemplateCls.LICENSE_KEY:
                String license = "";
                if (!(BlockChain.TESTS_VERS != 0 && BlockChain.TEST_MODE)) {
                    try {
                        //File file = new File("License Erachain.txt");
                        File file = new File("Erachain Licence Agreement (genesis).txt");
                        //READ SETTINS JSON FILE
                        List<String> lines = Files.readLines(file, Charsets.UTF_8);

                        for (String line : lines) {
                            license += line + "\n";
                        }
                        //file.close();
                    } catch (Exception e) {
                        return null;
                    }
                }
                return new Template(CREATOR, "Пользовательское соглашение на использование данного программного продукта"
                        //+ " \"" + Controller.APP_NAME + "\"", icon, image,
                        + " \"ERM4\"", icon, image,
                        license
                );
            case (int) TemplateCls.MARRIAGE_KEY:
                return new Template(CREATOR, "Заявление о бракосочетании", icon, image, "Мы, %person1% и %person2%, женимся!");
            case (int) TemplateCls.UNMARRIAGE_KEY:
                return new Template(CREATOR, "Заявление о разводе", icon, image, "Я, %person1%, развожусь с %person2%");
            case (int) TemplateCls.HIRING_KEY:
                return new Template(CREATOR, "Заявление о приёме на работу", icon, image, "Прошу принять меня в объединение %union%, на должность %job%");
            case (int) TemplateCls.UNHIRING_KEY:
                return new Template(CREATOR, "Заявление об уволнении", icon, image, "Прошу уволить меня из объединения %union% по собственному желанию");
        }
        return new Template(CREATOR, "empty", icon, image, "empty");
    }

    // make statuses
    public static Status makeStatus(int key) {
        if (key == StatusCls.MEMBER_KEY) return new Status(CREATOR,
                "Членство %1 ур. в объед. %2", icon, image, "Уровень %1 членства в объединении %2", false);
        //else if (key == StatusCls.ALIVE_KEY) return new Status(CREATOR, "Alive", icon, image, "Alive or Dead");
        //else if (key == StatusCls.RANK_KEY) return new Status(CREATOR, "Rank", icon, image, "General, Major or Minor");
        //else if (key == StatusCls.USER_KEY) return new Status(CREATOR, "User", icon, image, "Admin, User, Observer");
        //else if (key == StatusCls.MAKER_KEY) return new Status(CREATOR, "Maker", icon, image, "Creator, Designer, Maker");
        //else if (key == StatusCls.DELEGATE_KEY) return new Status(CREATOR, "Delegate", icon, image, "President, Senator, Deputy");
        //else if (key == StatusCls.CERTIFIED_KEY) return new Status(CREATOR, "Certified", icon, image, "Certified, Notarized, Confirmed");
        //else if (key == StatusCls.MARRIED_KEY) return new Status(CREATOR, "Married", icon, image, "Husband, Wife, Spouse");

        return new Status(CREATOR, "Право %1 ур. в объед. %2", icon, image, "Уровень %1 прав (власти) в объединении %2", false);
    }

    private static byte[] generateAccountSeed(byte[] seed, int nonce) {
        byte[] nonceBytes = Ints.toByteArray(nonce);
        byte[] accountSeed = Bytes.concat(nonceBytes, seed, nonceBytes);
        return Crypto.getInstance().doubleDigest(accountSeed);
    }

    private void initItems() {

        transactions = new ArrayList<Transaction>();
        ///// ASSETS
        //CREATE ERA ASSET
        //asset0 = makeAsset(AssetCls.ERA_KEY);
        //transactions.add(new GenesisIssueAssetTransaction(asset0));
        //CREATE JOB ASSET
        //asset1 = makeAsset(AssetCls.FEE_KEY);
        //transactions.add(new GenesisIssueAssetTransaction(asset1));
        // ASSET OTHER
        for (int i = 1; i <= AssetCls.FEE_KEY; i++) {
            AssetVenture asset = makeAsset(i);
            // MAKE OLD STYLE ASSET with DEVISIBLE:
            // PROP1 = 0 (unMOVABLE, SCALE = 8, assetTYPE = 1 (divisible)
            asset = new AssetVenture((byte) 0, asset.getOwner(), asset.getName(),
                    asset.getIcon(), asset.getImage(), asset.getDescription(), AssetCls.AS_INSIDE_ASSETS, 8, 0l);
            transactions.add(new GenesisIssueAssetTransaction(asset));
        }

        ///// TEMPLATES
        for (int i = 1; i <= TemplateCls.UNHIRING_KEY; i++)
            transactions.add(new GenesisIssueTemplateRecord(makeTemplate(i)));

        ///// STATUSES
        for (int i = 1; i <= StatusCls.MEMBER_KEY; i++)
            transactions.add(new GenesisIssueStatusRecord(makeStatus(i)));
    }

    //GETTERS

    private void addDebt(String address, int val, List<List<Object>> genesisDebtors) {

        Account recipient;
        if (address.equals("7DedW8f87pSDiRnDArq381DNn1FsTBa68Y")
                || address.equals("74MxuwvW8EhtJKZqF7McbcAMzu5V5bnQap")
            //|| address.equals("7GWr8njMyjkDs1gdRAgQ6MaEp2DMkK26h7") Матюхин
            // Бобылева Оксана
        )
            return;

        //int i = 0;
        for (int i = 0; i < genesisDebtors.size(); i++) {

            List<Object> item = genesisDebtors.get(i);
            String address_deb = (String) item.get(0);

            if (address_deb.length() > 36) {
                recipient = new PublicKeyAccount(address_deb);
            } else {
                recipient = new Account(address_deb);
            }

            if (recipient.equals(address)) {
                val += (int) item.get(1);
                genesisDebtors.set(i, Arrays.asList(address_deb, val));
                return;
            }
            i++;
        }
        genesisDebtors.add(Arrays.asList(address, val));
    }

    @Override
    public long getTimestamp() {
        return this.genesisTimestamp;
    }

    public long getGenesisBlockTimestamp() {
        return this.genesisTimestamp;
    }

    public String getTestNetInfo() {
        return this.testnetInfo;
    }
	/*
	@Override
	public int getGeneratingBalance()
	{
		return 0;
	}
	 */

    @Override
    public Block getParent(DCSet db) {
        //PARENT DOES NOT EXIST
        return null;
    }

    //VALIDATE

    public byte[] generateHeadHash() {
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

    @Override
    public boolean isSignatureValid() {

        //VALIDATE BLOCK SIGNATURE
        byte[] digest = generateHeadHash();
        if (!Arrays.equals(digest, this.signature)) {
            return false;
        }

        return true;
    }

    @Override
    public boolean isValid(DCSet db, boolean andProcess) {
        //CHECK IF NO OTHER BLOCK IN DB
        if (db.getBlockMap().last() != null) {
            return false;
        }

        //VALIDATE TRANSACTIONS
        byte[] transactionsSignatures = new byte[0];
        for (Transaction transaction : this.getTransactions()) {
            transaction.setDC(db);
            if (transaction.isValid(Transaction.FOR_NETWORK, 0l) != Transaction.VALIDATE_OK) {
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

    public void process(DCSet dcSet) throws Exception {

        this.target = BlockChain.BASE_TARGET;

        this.blockHead = new BlockHead(this);

        super.process(dcSet);

        if (false) {
            AssetVenture item = new AssetVenture(CREATOR, AssetCls.LIA_NAME, null, null, AssetCls.LIA_DESCR, AssetCls.AS_ACCOUNTING, 0, 0l);
            item.setReference(this.signature);
            dcSet.getItemAssetMap().put(AssetCls.LIA_KEY, item);
        }

    }

    public void orphan(DCSet dcSet) throws Exception {

        if (false)
            dcSet.getItemAssetMap().remove(AssetCls.LIA_KEY);

        super.orphan(dcSet);

    }

}
