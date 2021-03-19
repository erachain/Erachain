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
import org.json.simple.JSONArray;
import org.mapdb.Fun.Tuple2;

import java.io.File;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
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
    private static byte[] genesisReference = Bytes.ensureCapacity(new byte[]{19, 66, 8, 21, 0, 0, 0, 0}, Crypto.SIGNATURE_LENGTH, 0);
    private static byte[] icon = new byte[0];
    private static byte[] image = new byte[0];
    private String testnetInfo;
    private long genesisTimestamp;
    private String sideSettingString;

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

        if (BlockChain.TEST_MODE && !BlockChain.DEMO_MODE) {
            this.testnetInfo = "";

            //ADD TESTNET GENESIS TRANSACTIONS
            this.testnetInfo += "\ngenesisStamp: " + String.valueOf(genesisTimestamp);

            byte[] seed = Crypto.getInstance().digest(Longs.toByteArray(genesisTimestamp));

            this.testnetInfo += "\ngenesisSeed: " + Base58.encode(seed);

            this.testnetInfo += "\nStart the other nodes with command" + ":";
            this.testnetInfo += "\njava -Xms512m -Xmx1024m -jar erachain.jar -testnet=" + genesisTimestamp;

        } else if (BlockChain.CLONE_MODE) {

            sideSettingString = "";
            sideSettingString += Settings.genesisJSON.get(0).toString();
            sideSettingString += Settings.genesisJSON.get(1).toString();

            Account leftRecipiend = null;
            BigDecimal totalSended = BigDecimal.ZERO;
            JSONArray holders = (JSONArray) Settings.genesisJSON.get(2);
            if (!Settings.ERA_COMPU_ALL_UP) {
                for (int i = 0; i < holders.size(); i++) {
                    JSONArray holder = (JSONArray) holders.get(i);

                    sideSettingString += holder.get(0).toString();
                    sideSettingString += holder.get(1).toString();

                    // SEND FONDs
                    Account founder = new Account(holder.get(0).toString());
                    if (leftRecipiend == null) {
                        leftRecipiend = founder;
                    }

                    BigDecimal fondAamount = new BigDecimal(holder.get(1).toString()).setScale(BlockChain.AMOUNT_DEDAULT_SCALE);
                    transactions.add(new GenesisTransferAssetTransaction(founder,
                            AssetCls.ERA_KEY, fondAamount));

                    totalSended = totalSended.add(fondAamount);

                    if (holder.size() < 3)
                        continue;

                    String COMPUstr = holder.get(2).toString();
                    if (COMPUstr.length() > 0 && !COMPUstr.equals("0")) {
                        BigDecimal compu = new BigDecimal(COMPUstr).setScale(BlockChain.FEE_SCALE);
                        transactions.add(new GenesisTransferAssetTransaction(founder,
                                AssetCls.FEE_KEY, compu));
                        sideSettingString += compu.toString();
                    }

                    if (holder.size() < 4)
                        continue;

                    // DEBTORS
                    JSONArray debtors = (JSONArray) holder.get(3);
                    BigDecimal totalCredit = BigDecimal.ZERO;
                    for (int j = 0; j < debtors.size(); j++) {
                        JSONArray debtor = (JSONArray) debtors.get(j);

                        BigDecimal creditAmount = new BigDecimal(debtor.get(0).toString()).setScale(BlockChain.AMOUNT_DEDAULT_SCALE);
                        if (totalCredit.add(creditAmount).compareTo(fondAamount) > 0) {
                            break;
                        }

                        sideSettingString += creditAmount.toString();
                        sideSettingString += debtor.get(1).toString();

                        transactions.add(new GenesisTransferAssetTransaction(new Account(debtor.get(1).toString()),
                                -AssetCls.ERA_KEY,
                                creditAmount, founder));

                        totalCredit = totalCredit.add(creditAmount);
                    }
                }

                if (totalSended.compareTo(new BigDecimal(BlockChain.GENESIS_ERA_TOTAL)) < 0) {
                    // ADJUST end
                    transactions.add(new GenesisTransferAssetTransaction(
                            leftRecipiend, AssetCls.ERA_KEY,
                            new BigDecimal(BlockChain.GENESIS_ERA_TOTAL).subtract(totalSended).setScale(BlockChain.AMOUNT_DEDAULT_SCALE)));
                }
            }

        } else {

            List<Tuple2<Account, BigDecimal>> sends_toUsers = new ArrayList<Tuple2<Account, BigDecimal>>();

            /*
             */
            ///////// GENEGAL
            List<List<Object>> generalGenesisUsers = Arrays.asList(
                    Arrays.asList("7R2WUFaS7DF2As6NKz13Pgn9ij4sFw6ymZ", "800000"),
                    Arrays.asList("7EpDngzSLXrqnRBJ5x9YKTU395VEpsz5Mz", "900000"),
                    Arrays.asList("7Psb8dEDd4drdHxJvd4bFihembSWBJQDvC", "800000"),
                    Arrays.asList("7B3gTXXKB226bxTxEHi8cJNfnjSbuuDoMC", "900000"),
                    Arrays.asList("77QnJnSbS9EeGBa2LPZFZKVwjPwzeAxjmy", "800000"),
                    Arrays.asList("78JFPWVVAVP3WW7S8HPgSkt24QF2vsGiS5", "800000"),
                    Arrays.asList("7R4jwh5C83HLj7C1FiSbsGptMHqfAirr8R", "800000"),
                    Arrays.asList("75hXUtuRoKGCyhzps7LenhWnNtj9BeAF12", "800000"),
                    Arrays.asList("7Dwjk4TUB74CqW6PqfDQF1siXquK48HSPB", "800000")
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
                    ////
                    Arrays.asList("7DedW8f87pSDiRnDArq381DNn1FsTBa68Y", "333000"),
                    Arrays.asList("7PnyFvPSVxczqueXfmjtwZNXN54vU9Zxsw", "300000"),
                    Arrays.asList("74rRXsxoKtVKJqN8z6t1zHfufBXsELF94y", "300000"),
                    Arrays.asList("74MxuwvW8EhtJKZqF7McbcAMzu5V5bnQap", "300000"),
                    Arrays.asList("7PChKkoASF1eLtCnAMx8ynU2sMYdSPwkGV", "100000"),
                    Arrays.asList("7QuuSeJqTsuNBUsTfrfqHgRZTZ6ymKxYoJ", "100000"),

                    ////
                    Arrays.asList("7Mr6qTY2vN1int3Byo6NmZQDRmH7zuLEZ7", "1800"),
                    Arrays.asList("7J1S62H1YrVhPcLibcUtA2vFACMtiLakMA", "1289.69596627"),
                    Arrays.asList("7J1S62H1YrVhPcLibcUtA2vFACMtiLakMA", "1289.69596627"), // двойная запись она по сингнатуре не ищется - 1-43 - 1-44
                    Arrays.asList("73igNXcJbLZxoM989B2yj4214oztMHoLGc", "43.84966285"),
                    Arrays.asList("7A9FFw3mQfDrP9y8WCifrZ3pvsKwerkMLr", "1289.69596627"),
                    Arrays.asList("7QqeSR442vstwcf5Hzm3t2pWgqupQNxRTv", "257.93919325"),
                    Arrays.asList("7CbRHH27V9xsaqKfTzSqNwNFhxKLhbf4g5", "41.27027092"),
                    Arrays.asList("77fdZVgXhnebykEmhuEkkxYxs7nFoTEWdP", "2579.39193253"),
                    Arrays.asList("74MxuwvW8EhtJKZqF7McbcAMzu5V5bnQap", "257.93919325"),
                    Arrays.asList("7Cp622VhpUwpzWnzDV3XyPepVM5AF682UF", "1289.69596627"),
                    Arrays.asList("76UjGyQ4TG9buoK8yQ1PmW2GE6PoPAEDZw", "1289.69596627"),
                    Arrays.asList("7ANHQck4rANJ5K2RsF1aAGYYTyshpFP4cM", "1131.83718"),
                    // sold out Arrays.asList("78HfjphyuwWkLw7jMymcTM3UsRdXCE5auq", "257.93919325"),
                    Arrays.asList("CmmGpEbumf3FspKEC9zTzpFTk86ibLRwEbqxZ3GuAykL", "154.76351595"),
                    Arrays.asList("7DedW8f87pSDiRnDArq381DNn1FsTBa68Y", "141866.556289"), //
                    Arrays.asList("7EMFYDJW2mxBAPDgWsVbAULMSx5BzhC9tq", "1870.05915109"),
                    Arrays.asList("7GWr8njMyjkDs1gdRAgQ6MaEp2DMkK26h7", "25793.9193253"),
                    Arrays.asList("7JMtB4zjEyig1sfBqFTHdvuPqvbtmaQcvL", "859.71133111"),
                    Arrays.asList("7LPhKZXmd6miLE9XxWZciabydoC8vf4f64", "3353.20951229"),
                    Arrays.asList("7Gdt8ZdiFuHrwe9zdLcsE1cKtoFVLDqyho", "2298.23821189"),
                    Arrays.asList("788AwMejUTX3tEcD5Leym8TTzKgbxVSgzr", "210.47838169"),
                    Arrays.asList("77QMFKSdY4ZsG8bFHynYdFNCmis9fNw5yP", "232145.273928"),
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
                    Arrays.asList("79qUjyTW4VoSgMKpF2dLW9eCwGVTSSnP2H", "502.98142684"),
                    Arrays.asList("7MRWHqXZRmNYL7TUHkVht9CQcime3K4Cm3", "253.451"),
                    Arrays.asList("7677tDJSjTSHnjDe3msjVmJYhWMZZED2jj", "2000"),
                    Arrays.asList("75R3LayKe3orQrtZnMWR1VdadBdypj2NWW", "1001"),
                    Arrays.asList("7JwZCVyg4gZiwpV5Qa9nWGvmvT7ESD83Rk", "150")

            );

            ////////// ACTIVISTS
            List<List<Object>> genesisActivists = Arrays.asList(
                    Arrays.asList("7PChKkoASF1eLtCnAMx8ynU2sMYdSPwkGV", "1000.0"), //
                    Arrays.asList("76Um7KRBKDjoLWbLDWMdbtmBJkxjW9GNpZ", "1000.0"),
                    Arrays.asList("76u1ywTpSTdZvpq9bNk5GdnwTxD5uNo6dF", "1000.0"),
                    Arrays.asList("7KcBS1bmK1NiYwJD1mgwhz1ZFWESviQthG", "1000.0"),
                    Arrays.asList("78Eo2dL898wzqXBn6zbGanEnwXtdDF2BWV", "1000.0"),
                    Arrays.asList("73igNXcJbLZxoM989B2yj4214oztMHoLGc", "1000.0"),
                    Arrays.asList("7PnyFvPSVxczqueXfmjtwZNXN54vU9Zxsw", "1000.0"),
                    Arrays.asList("7D7S5veDCiAwvBCkoK4G2YqdXC4dZ3SH1Q", "1000.0"),
                    Arrays.asList("74MxuwvW8EhtJKZqF7McbcAMzu5V5bnQap", "10000.0"),
                    Arrays.asList("7FPm2tet9HTVmBMe5xvRzp4sWoS6d8PgWZ", "1000.0"),
                    Arrays.asList("78cK2QS34j8cPLWwHDqCBy36ZmikiCzLcg", "1000.0"),
                    Arrays.asList("79gQ4iB4Cs8EkhrUanEiDQtKArt6k6NAdu", "1000.0"),
                    Arrays.asList("7Kh5KvHCuWAq8XHioKyUBZxRmbwCJZV5b2", "1000.0"),
                    Arrays.asList("74Rcp979npxf6Q5zV6ZnpEnsxrsCHdXeNU", "1000.0"),
                    Arrays.asList("78HfjphyuwWkLw7jMymcTM3UsRdXCE5auq", "1000.0"),
                    Arrays.asList("7DedW8f87pSDiRnDArq381DNn1FsTBa68Y", "1000.0"), //
                    Arrays.asList("7K4XaDVf98J1fKDdCS8oYofYgFgoezFEAA", "1000.0"),
                    Arrays.asList("7Cy2J5ST6ukHSJVWtQd7eH4wbhbSBbMbZD", "1000.0"),
                    Arrays.asList("7DRH1MjEo3GgtySGsXjzfdqeQYagutXqeP", "1000.0"),
                    Arrays.asList("74rRXsxoKtVKJqN8z6t1zHfufBXsELF94y", "2000.0"),
                    Arrays.asList("7A9FFw3mQfDrP9y8WCifrZ3pvsKwerkMLr", "1000.0"),
                    Arrays.asList("7MPmVWSobucE6TdJvnEeohFAZnCej7fr2F", "1000.0"),
                    Arrays.asList("75qZ6ncf5T4Gkz1vrwkqjCPJ1A5gr2Cyah", "1000.0"),
                    Arrays.asList("7JNUfHeuCRLApKX9MievkAoGdFgVfBf7DE", "1000.0"),
                    Arrays.asList("7Fgkw8cuPiTc4LVRvkYBuXEEfGYxrg6XiX", "1000.0"),
                    Arrays.asList("75rVEuvpzhLJznkXZaYyxJq8L9pVCeqFbk", "1000.0"),
                    Arrays.asList("7J3M8xwJeG5gyBC5kLPb5c2kVHoTsMT5MK", "1000.0"),
                    Arrays.asList("75rEoNUknMU3qYGjS3wriY53n1aRUznFus", "1000.0"),
                    Arrays.asList("73dXJb1orwqk1ADW364KEAzPVQNGa1vX9S", "10000.0"),
                    Arrays.asList("7CPGk25mTFGhANaBCiV4LqrowcUfrfLcRe", "1000.0"),
                    Arrays.asList("78KCkgNeSvxwtnVJTyzLFGGzmP8SUUuN1J", "1000.0"),
                    Arrays.asList("7AJNCwQvbEbGn7Mt3mzPHbK1Zxvy9t6xtA", "1000.0"),
                    Arrays.asList("77GYw61CPhDhdHsHg8oYCaKhenq2izAps8", "1000.0"),
                    Arrays.asList("7NeUmKbZadHLwS9FfLdhFL4ymVYSieF9Uc", "1000.0"),
                    Arrays.asList("73yfeCDiSciBF1vc3PG8uyJMty4jRDxxL9", "1000.0"),
                    Arrays.asList("7AXey16ivPRCQoFWzkMU4Q7V8FZugqjYUX", "10000.0"),
                    Arrays.asList("7GWr8njMyjkDs1gdRAgQ6MaEp2DMkK26h7", "3000.0"),
                    Arrays.asList("7HWxbcgVRxzdxDiVj9oc5ZG39a93imLUWz", "1000.0"),
                    Arrays.asList("79qUjyTW4VoSgMKpF2dLW9eCwGVTSSnP2H", "1000.0")
            );

            // GENESIS FORGERS
            //ArrayList<String> arrayList = new ArrayList<String>(Arrays.asList(arr));
            // NEED for .add() ::
            ArrayList<List<Object>> genesisDebtors = new ArrayList<List<Object>>(Arrays.asList(
                    Arrays.asList("7DRH1MjEo3GgtySGsXjzfdqeQYagutXqeP", 2), //
                    Arrays.asList("7EM7P1neMZkw2EXr2kn15XMixfYVVTwvWF", 2), //
                    Arrays.asList("7EMFYDJW2mxBAPDgWsVbAULMSx5BzhC9tq", 2), //
                    Arrays.asList("7LcwdEBZWVyFyaFhFoGC3SUxyqH5Uo9Zrq", 2), //
                    Arrays.asList("75qZ6ncf5T4Gkz1vrwkqjCPJ1A5gr2Cyah", 2), //
                    Arrays.asList("76UjGyQ4TG9buoK8yQ1PmW2GE6PoPAEDZw", 2), //
                    Arrays.asList("76Um7KRBKDjoLWbLDWMdbtmBJkxjW9GNpZ", 2), //
                    Arrays.asList("78cK2QS34j8cPLWwHDqCBy36ZmikiCzLcg", 2), //
                    Arrays.asList("7ARdsYAd4c92mHUofN7fLS8C3VeMwbTJAr", 2), //
                    Arrays.asList("7Cp622VhpUwpzWnzDV3XyPepVM5AF682UF", 2), //
                    Arrays.asList("7Es5nngafwj42ULGf9xxiwnSoQACCtj2WA", 2), //
                    Arrays.asList("7Fgkw8cuPiTc4LVRvkYBuXEEfGYxrg6XiX", 2), //
                    Arrays.asList("7JMtB4zjEyig1sfBqFTHdvuPqvbtmaQcvL", 2), //
                    Arrays.asList("7JNUfHeuCRLApKX9MievkAoGdFgVfBf7DE", 2), //
                    Arrays.asList("7MPmVWSobucE6TdJvnEeohFAZnCej7fr2F", 2), //
                    Arrays.asList("7PrZEW6ZdkZDj5GMCCp918n7EbyHVf3mRa", 2), //
                    Arrays.asList("7ANHQck4rANJ5K2RsF1aAGYYTyshpFP4cM", 2), //
                    Arrays.asList("75rVEuvpzhLJznkXZaYyxJq8L9pVCeqFbk", 2), //
                    Arrays.asList("7Gdt8ZdiFuHrwe9zdLcsE1cKtoFVLDqyho", 2), //
                    Arrays.asList("7QqeSR442vstwcf5Hzm3t2pWgqupQNxRTv", 2), //
                    Arrays.asList("736RAxF1dwRE1FqKCyBVztvGSmDYj9Z8VD", 4), //
                    Arrays.asList("73UxSPEhB9R5deSxL62c8ckCKvQdCALBcu", 4), //

                    Arrays.asList("7LPhKZXmd6miLE9XxWZciabydoC8vf4f64", 3), //
                    Arrays.asList("7J1S62H1YrVhPcLibcUtA2vFACMtiLakMA", 1),
                    Arrays.asList("7MdXzNcKgWXvy7unJ7WPLmp3LQvUdiNEAz", 1),
                    Arrays.asList("73igNXcJbLZxoM989B2yj4214oztMHoLGc", 2), //
                    Arrays.asList("8Q7zyxx1rYKBbiKVfs66H5G16Vtsag54wCHnV2tHY5nA", 2),
                    Arrays.asList("74fCzX79v5etyt1pjtfAQhyrLCRSVfm6AM", 1),
                    Arrays.asList("7BbrDtJWt9WYfoFQg9VV4aW2yVdaQpsjH9", 1),
                    Arrays.asList("7NZAQieFR3Qyzzj8iZhWHHPZJ9D2TPW7uR", 1),
                    Arrays.asList("73shRmoD4YNAtMKzF8ZnFtsYVx4hx9cShi", 2), //
                    Arrays.asList("7EwDnU3F8znwp3bsFq1W5NA1b3YiEwYt7N", 3), //
                    Arrays.asList("3r1fXZPBcVf2acj5ELhEZ4uYGPdZoYFwY4bYS4qeG22F", 1),
                    Arrays.asList("7QF8kYdmv1dqT548S6HjSvgdtF7txncvbr", 2), //
                    Arrays.asList("3dLWtiRPb9PYDXbb6t1P9jmVqDkmZnBiMtYsCSJWWw3S", 1),
                    Arrays.asList("7FXqM9Lq9wGJkjpduXoYuBqLzrkHQqhH78", 1),
                    Arrays.asList("74Rcp979npxf6Q5zV6ZnpEnsxrsCHdXeNU", 2), //
                    Arrays.asList("7APgaQe1uiG8Vgzz5bZCTPw39mTwjPfgLF", 1),
                    Arrays.asList("79VxiuxRgFTp8cUTRDBAoZPGXEdqY7hD8h", 3), //
                    Arrays.asList("2B8NRmXsVJ3zSxS5Px78h7qieb5Yvp3XRnyzzK2qk8kq", 2),
                    Arrays.asList("7JJjBJqpySJmoJws6xSDsvwRo5yKS7wneg", 1),
                    Arrays.asList("78KCkgNeSvxwtnVJTyzLFGGzmP8SUUuN1J", 3), //
                    Arrays.asList("76JECepZ1DYWqbF4Vb2VZMBs4A6WsvrN53", 1),
                    Arrays.asList("7J3M8xwJeG5gyBC5kLPb5c2kVHoTsMT5MK", 3), //
                    Arrays.asList("7KstpqkbQrCiAuVD3WBQSitbDvoCUTh2D6", 1),
                    Arrays.asList("7CPGk25mTFGhANaBCiV4LqrowcUfrfLcRe", 3), //
                    Arrays.asList("7HLmWov2KYx4MBBceN1KqYom6m97ppp2wF", 2), //
                    Arrays.asList("7JRYHaNtKshTbAMdbGALjmScB2c1NksKD4", 1),
                    Arrays.asList("7AXey16ivPRCQoFWzkMU4Q7V8FZugqjYUX", 4), //
                    Arrays.asList("77GYw61CPhDhdHsHg8oYCaKhenq2izAps8", 5), //
                    Arrays.asList("7JAmAzeehdP5JWspXodhQR31dVqhKDR8sj", 2), //
                    Arrays.asList("77Atk56iAvdFgayLyC6EbfkBLmcJNBQzrh", 1), //
                    Arrays.asList("73yfeCDiSciBF1vc3PG8uyJMty4jRDxxL9", 2),
                    Arrays.asList("78F5m5oUf1N4iZB7XPdWmQJdwMT3tWMQ2j", 1),
                    Arrays.asList("7MoR3qqs959XAnDQ8mDr11sBnKs4woogkK", 2), //
                    Arrays.asList("7C17PgbPTJeju3yJPbw4Wmus9gj8Jeo3TF", 1),
                    Arrays.asList("79qUjyTW4VoSgMKpF2dLW9eCwGVTSSnP2H", 2), //
                    Arrays.asList("7CqCmqYwCqCDe599vHeWLdL4YSi1ShYg2r", 2), //
                    Arrays.asList("75Uej5KmQVmmqHusKfj3zP3AoZ5wkyyu7E", 1),
                    Arrays.asList("7McczL4B1xfNnDRhjqrgQKdtXVp7YdKYVM", 1),
                    Arrays.asList("75R3LayKe3orQrtZnMWR1VdadBdypj2NWW", 3), //
                    Arrays.asList("741kxf9sRgRk2JZfEpxt2D9NcooUCRAj2m", 2), //
                    Arrays.asList("7Luf2TRvoQuxaQriWmB1G9DgsZ6b1Pfith", 1),
                    Arrays.asList("7FPm2tet9HTVmBMe5xvRzp4sWoS6d8PgWZ", 1),
                    Arrays.asList("7G9QBw7TBgB9DLVcmDARmCBLX2yaLNnzXS", 2), //
                    Arrays.asList("7SErqYci2YesFsg4zcxowJ62G9LDk6mic3", 2), //
                    Arrays.asList("78HfjphyuwWkLw7jMymcTM3UsRdXCE5auq", 2), //
                    Arrays.asList("7DwK6UC648aGPKyGboQeU6WMCNhUSYAxHb", 2), //
                    Arrays.asList("7MKdGrRFzrmy3KLNmcrS7PmfJn6KYerR3G", 1),
                    Arrays.asList("7LDPFrsPUFedgGsomXACwh7qM2qdcRGso7", 1),
                    Arrays.asList("7KVfBqULZJx2DgbBHrkf1pZzkJMVDru8pB", 2), //
                    Arrays.asList("ByVNbfq6xp7AFqEH1bbacFr9eTVPGnxLQboyv46WqZra", 3),
                    Arrays.asList("7PPpw4H1UQm865jxe9FpTKBzC3fBULXN1w", 2),
                    Arrays.asList("EFLHKpYpQXZYUaJJ1mgMZ8H7i1jwoGXRojVV84rTA49h", 2),
                    Arrays.asList("7S1LnztovJEgYWS4MKLNyccZVriaFiJjUL", 1),
                    Arrays.asList("75rEoNUknMU3qYGjS3wriY53n1aRUznFus", 2), //
                    Arrays.asList("75v2xRHKypQqqSM4pwQs9pq49ZL9rxYg8B", 2), //
                    Arrays.asList("7PrskypEaZWX4nqN19BJQkssKEdaJcvaVu", 1),
                    Arrays.asList("7HgZk85BA5VxATncBRqgYRZ84mWDFpD8jS", 1),
                    Arrays.asList("7AAyNfFFGipUXVdRLwWJnhUwfj9FDqnJ2z", 2), //
                    Arrays.asList("77HyuCsr8u7f6znj2Lq8gXjK6DCG7osehs", 3), //
                    Arrays.asList("7NLEQV71W4X9YqopA15k5VNk2WFiKc3ePE", 1),
                    Arrays.asList("7MJyC8L6AQGtckhJaF4BS1MiMQHBeuk5ss", 1),
                    Arrays.asList("1A3P7u56G4NgYfsWMms1BuctZfnCeqrYk3", 1), //// 11111
                    Arrays.asList("7D9mKfdvXwgTpogHN1KTGmF78PjteidPA6", 1),

                    Arrays.asList("7RVngd4icw21J1ePCg8977sBetgQFARBUL", 1),
                    Arrays.asList("7AfGz1FJ6tUnxxKSAHfcjroFEm8jSyVm7r", 3),
                    Arrays.asList("7DWxrA51FMESx73rJ7xQcgZ3vJBye3oKdt", 1),
                    Arrays.asList("7F9cZPE1hbzMT21g96U8E1EfMimovJyyJ7", 3),
                    Arrays.asList("7BGULg8nCwQWTkDRVc8dTD47DqbMcmeYpE", 1),
                    Arrays.asList("74ZeQaNvhkpfhcPDXbpQMwmySqdaVhhi6S", 1),
                    Arrays.asList("7FMY7yG5sWf6YLvch1WvuB9tASotcRburU", 1),
                    Arrays.asList("2Rdm5J5Ha5pzogvFvbF8Kufid4LTxat5Lmo7G4ANVbpy", 1),

                    // 7NqEspTguift9AwRDDmGivUjzFQdkA4TBF 2
                    //
                    Arrays.asList("7RSLd62fpgBW5PyaGHLNh8rHZQbmRqcret", 2) //
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

                bdAmount1 = BigDecimal.ONE.setScale(BlockChain.FEE_SCALE);
                transactions.add(new GenesisTransferAssetTransaction(recipient, AssetCls.FEE_KEY, bdAmount1));

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
                    new Account("76ACGgH8c63VrrgEw1wQA4Dno1JuPLTsWe"), AssetCls.ERA_KEY,
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
        if (BlockChain.GENESIS_SIGNATURE == null) {
            this.signature = generateHeadHash();
        } else {
            this.signature = BlockChain.GENESIS_SIGNATURE;
        }

    }

    // make assets
    public static AssetVenture makeAsset(long key) {
        switch ((int) key) {
            case (int) AssetCls.ERA_KEY:
                return new AssetVenture(CREATOR, AssetCls.ERA_NAME, icon, image, AssetCls.ERA_DESCR, 0, 8, 0L);
            case (int) AssetCls.FEE_KEY:
                return new AssetVenture(BlockChain.FEE_ASSET_EMITTER, AssetCls.FEE_NAME, icon, image, AssetCls.FEE_DESCR, 0, 8, 0L);
            case (int) AssetCls.TRUST_KEY:
                return new AssetVenture(CREATOR, AssetCls.TRUST_NAME, icon, image, AssetCls.TRUST_DESCR, 0, 8, 0L);
            case (int) AssetCls.REAL_KEY:
                return new AssetVenture(CREATOR, AssetCls.REAL_NAME, icon, image, AssetCls.REAL_DESCR, 0, 8, 0L);
            case (int) AssetCls.REAL_KEY + 1:
                return new AssetVenture(
                        CREATOR,
                        "РА", icon, image, "Единица Ра",
                        0, 8, 0L);
            case (int) AssetCls.REAL_KEY + 2:
                return new AssetVenture(
                        CREATOR,
                        "RUNEURO", icon, image, "RuNeuro",
                        0, 8, 0L);
            case (int) AssetCls.REAL_KEY + 3:
                return new AssetVenture(
                        CREATOR,
                        "ERG", icon, image, "1 миллион ЕРГ. Основная учётная единица, мера полезного ЭНЕРГОПОТОКА (пользы для ноосферы) управления данной средой - ЭРГ (ERG). Для обеспчения жизни на земле постоянно требуется поток энергии. Из общего потока энергии полезный поток всегда меньше полного. Отношение полезного энергопотока к полному энергопотоку = КПД Системы.",
                        0, 8, 0L);
            case (int) AssetCls.REAL_KEY + 4:
                return new AssetVenture(
                        CREATOR,
                        "LERG", icon, image, "1 миллион потраченных ЕРГ - ПЭРГ (Lost ERG)",
                        0, 8, 0L);
            case (int) AssetCls.REAL_KEY + 5:
                return new AssetVenture(
                        new PublicKeyAccount(Base58.decode("5mgpEGqUGpfme4W2tHJmG7Ew21Te2zNY7Ju3e9JfUmRF")),
                        "A", icon, image, "ARONICLE.COM shares",
                        0, 8, 0L);
        }
        return null;
    }

    // make templates
    public static Template makeTemplate(int key) {
        switch (key) {
            case (int) TemplateCls.LICENSE_KEY:
                String license = "";
                if (!(BlockChain.TESTS_VERS != 0 && (BlockChain.CLONE_MODE || BlockChain.TEST_MODE))) {
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
        for (int i = 1; i <= AssetCls.REAL_KEY + 5; i++) {
            AssetVenture asset = makeAsset(i);
            // MAKE OLD STYLE ASSET with DEVISIBLE:
            // PROP1 = 0 (unMOVABLE, SCALE = 8, assetTYPE = 1 (divisible)
            asset = new AssetVenture((byte) 0, asset.getMaker(), asset.getName(),
                    asset.getIcon(), asset.getImage(), asset.getDescription(), AssetCls.AS_INSIDE_ASSETS, 8, 0L);
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

        if (BlockChain.CLONE_MODE) {
            //WRITE SIDE SETTINGS
            byte[] genesisjsonCloneBytes = this.sideSettingString.getBytes(StandardCharsets.UTF_8);
            data = Bytes.concat(data, genesisjsonCloneBytes);
        }

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
        if (!Arrays.equals(digest,
                // TODO - как защитить свой оригинальныЙ? Если задан наш оригинальный - то его и берем
                BlockChain.GENESIS_SIGNATURE_TRUE == null ?
                        this.signature : BlockChain.GENESIS_SIGNATURE_TRUE)) {
            return false;
        }

        return true;
    }

    @Override
    public int isValid(DCSet db, boolean andProcess) {
        //CHECK IF NO OTHER BLOCK IN DB
        if (db.getBlockMap().last() != null) {
            return INVALID_BLOCK_VERSION;
        }

        //VALIDATE TRANSACTIONS
        byte[] transactionsSignatures = new byte[0];
        for (Transaction transaction : this.getTransactions()) {
            transaction.setDC(db);
            if (transaction.isValid(Transaction.FOR_NETWORK, 0L) != Transaction.VALIDATE_OK) {
                return INVALID_BLOCK_VERSION;
            }
            transactionsSignatures = Bytes.concat(transactionsSignatures, transaction.getSignature());

        }
        transactionsSignatures = Crypto.getInstance().digest(transactionsSignatures);
        if (!Arrays.equals(this.transactionsHash, transactionsSignatures)) {
            LOGGER.error("*** GenesisBlock.digest(transactionsSignatures) invalid");
            return INVALID_BLOCK_VERSION;
        }

        return INVALID_NONE;
    }

    public void process(DCSet dcSet) throws Exception {

        this.target = BlockChain.BASE_TARGET;

        this.blockHead = new BlockHead(this);

        super.process(dcSet);

    }

    public void orphan(DCSet dcSet) throws Exception {

        if (false)
            dcSet.getItemAssetMap().remove(AssetCls.LIA_KEY);

        super.orphan(dcSet);

    }

}
