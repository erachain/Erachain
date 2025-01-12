package org.erachain.core.block;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import org.apache.commons.net.util.Base64;
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

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.erachain.core.item.assets.AssetTypes.AS_INSIDE_ASSETS;

public class GenesisBlock extends Block {

    public static final PublicKeyAccount CREATOR = new PublicKeyAccount(new byte[PublicKeyAccount.PUBLIC_KEY_LENGTH]);
    private static int genesisVersion = 0;
    private static byte[] genesisReference = Bytes.ensureCapacity(new byte[]{19, 66, 8, 21, 0, 0, 0, 0}, Crypto.SIGNATURE_LENGTH, 0);
    private static byte[] icon = new byte[0];
    private static byte[] image = new byte[0];
    private String testnetInfo;
    private long genesisTimestamp;
    private String sideSettingString;

    private static final byte[] itemAppData = null;

    public GenesisBlock() {

        super(genesisVersion, genesisReference, CREATOR);

        this.genesisTimestamp = Settings.getInstance().getGenesisStamp();

        Account recipient;
        BigDecimal bdAmount0;
        BigDecimal bdAmount1;

        // ISSUE ITEMS
        this.initItems();

        if (BlockChain.DEMO_MODE) {
            ;
        } else if (BlockChain.TEST_MODE) {
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
                    // AS List - for use Arrays.asList() in init debtors in holders

                    List debtors = (List) holder.get(3);
                    BigDecimal totalCredit = BigDecimal.ZERO;
                    for (int j = 0; j < debtors.size(); j++) {
                        List debtor = (List) debtors.get(j);

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

            /// MAIN MODE

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
                return new AssetVenture(itemAppData, CREATOR, AssetCls.ERA_NAME, icon, image, AssetCls.ERA_DESCR, 0, 8, 0L);
            case (int) AssetCls.FEE_KEY:
                return new AssetVenture(itemAppData, BlockChain.FEE_ASSET_EMITTER == null ? (BlockChain.FEE_ASSET_EMITTER = CREATOR) : CREATOR,
                        AssetCls.FEE_NAME, icon, image, AssetCls.FEE_DESCR, 0, 8, 0L);
            case (int) AssetCls.TRUST_KEY:
                return new AssetVenture(itemAppData, CREATOR, AssetCls.TRUST_NAME, icon, image, AssetCls.TRUST_DESCR, 0, 8, 0L);
            case (int) AssetCls.REAL_KEY:
                return new AssetVenture(itemAppData, CREATOR, AssetCls.REAL_NAME, icon, image, AssetCls.REAL_DESCR, 0, 8, 0L);
            case (int) AssetCls.REAL_KEY + 1:
                return new AssetVenture(
                        itemAppData, CREATOR,
                        "РА", icon, image, "Единица Ра",
                        0, 8, 0L);
            case (int) AssetCls.REAL_KEY + 2:
                return new AssetVenture(
                        itemAppData, CREATOR,
                        "RUNEURO", icon, image, "RuNeuro",
                        0, 8, 0L);
            case (int) AssetCls.REAL_KEY + 3:
                return new AssetVenture(
                        itemAppData, CREATOR,
                        "ERG", icon, image, "1 миллион ЕРГ. Основная учётная единица, мера полезного ЭНЕРГОПОТОКА (пользы для ноосферы) управления данной средой - ЭРГ (ERG). Для обеспчения жизни на земле постоянно требуется поток энергии. Из общего потока энергии полезный поток всегда меньше полного. Отношение полезного энергопотока к полному энергопотоку = КПД Системы.",
                        0, 8, 0L);
            case (int) AssetCls.REAL_KEY + 4:
                return new AssetVenture(
                        itemAppData, CREATOR,
                        "LERG", icon, image, "1 миллион потраченных ЕРГ - ПЭРГ (Lost ERG)",
                        0, 8, 0L);
            case (int) AssetCls.REAL_KEY + 5:
                return new AssetVenture(
                        itemAppData, new PublicKeyAccount(Base58.decode("5mgpEGqUGpfme4W2tHJmG7Ew21Te2zNY7Ju3e9JfUmRF")),
                        "A", icon, image, "ARONICLE.COM shares",
                        0, 8, 0L);
        }
        return null;
    }

    // make templates
    public static Template makeTemplate(int key) {
        switch (key) {
            case (int) TemplateCls.LICENSE_KEY:
                    return null;
            case (int) TemplateCls.MARRIAGE_KEY:
                return new Template(itemAppData, CREATOR, "Заявление о бракосочетании", icon, image, "Мы, %person1% и %person2%, женимся!");
            case (int) TemplateCls.UNMARRIAGE_KEY:
                return new Template(itemAppData, CREATOR, "Заявление о разводе", icon, image, "Я, %person1%, развожусь с %person2%");
            case (int) TemplateCls.HIRING_KEY:
                return new Template(itemAppData, CREATOR, "Заявление о приёме на работу", icon, image, "Прошу принять меня в объединение %union%, на должность %job%");
            case (int) TemplateCls.UNHIRING_KEY:
                return new Template(itemAppData, CREATOR, "Заявление об уволнении", icon, image, "Прошу уволить меня из объединения %union% по собственному желанию");
        }
        return new Template(itemAppData, CREATOR, "empty", icon, image, "empty");
    }

    // make statuses
    public static Status makeStatus(int key) {
        if (key == StatusCls.MEMBER_KEY) return new Status(itemAppData, CREATOR,
                "Членство %1 ур. в объед. %2", icon, image, "Уровень %1 членства в объединении %2", false);
        //else if (key == StatusCls.ALIVE_KEY) return new Status(CREATOR, "Alive", icon, image, "Alive or Dead");
        //else if (key == StatusCls.RANK_KEY) return new Status(CREATOR, "Rank", icon, image, "General, Major or Minor");
        //else if (key == StatusCls.USER_KEY) return new Status(CREATOR, "User", icon, image, "Admin, User, Observer");
        //else if (key == StatusCls.MAKER_KEY) return new Status(CREATOR, "Maker", icon, image, "Creator, Designer, Maker");
        //else if (key == StatusCls.DELEGATE_KEY) return new Status(CREATOR, "Delegate", icon, image, "President, Senator, Deputy");
        //else if (key == StatusCls.CERTIFIED_KEY) return new Status(CREATOR, "Certified", icon, image, "Certified, Notarized, Confirmed");
        //else if (key == StatusCls.MARRIED_KEY) return new Status(CREATOR, "Married", icon, image, "Husband, Wife, Spouse");

        return new Status(itemAppData, CREATOR, "Право %1 ур. в объед. %2", icon, image, "Уровень %1 прав (власти) в объединении %2", false);
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
            asset = new AssetVenture((byte) 0, itemAppData, asset.getMaker(), asset.getName(),
                    asset.getIcon(), asset.getImage(), asset.getDescription(), AS_INSIDE_ASSETS, 8, 0L);
            transactions.add(new GenesisIssueAssetTransaction(asset));
        }

        ///// TEMPLATES
        for (int i = 1; i <= TemplateCls.UNHIRING_KEY; i++)
            if (i == 2) {
                try {
                    transactions.add(TransactionFactory.getInstance().parse(Base64.decodeBase64("AgEAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAACV0J/QvtC70YzQt9C+0LLQsNGC0LXQu9GM0YHQutC+0LUg0YHQvtCz0LvQsNGI0LXQvdC40LUg0L3QsCDQuNGB0L/QvtC70YzQt9C+0LLQsNC90LjQtSDQtNCw0L3QvdC+0LPQviDQv9GA0L7Qs9GA0LDQvNC80L3QvtCz0L4g0L/RgNC+0LTRg9C60YLQsCAiRVJNNCIAAAAAAAAAALD977u/0KPQodCb0J7QktCY0K8g0JvQmNCm0JXQndCX0JjQntCd0J3QntCT0J4g0KHQntCT0JvQkNCo0JXQndCY0K8gKNCd0JXQmNCh0JrQm9Cu0KfQmNCi0JXQm9Cs0J3QkNCvINCb0JjQptCV0J3Ql9CY0K8pINCd0JAg0JjQodCf0J7Qm9Cs0JfQntCS0JDQndCY0JUg0J/QoNCe0JPQoNCQ0JzQnNCd0J7Qk9CeINCe0JHQldCh0J/QldCn0JXQndCY0K8gRVJNNApodHRwOi8vYXJvbmljbGUuY29tCgoK0J/RgNC40L3QuNC80LDRjyDQvdCw0YHRgtC+0Y/RidC10LUg0KHQvtCz0LvQsNGI0LXQvdC40LUg0LjQu9C4INC40YHQv9C+0LvRjNC30YPRjyDQv9GA0L7Qs9GA0LDQvNC80L3Ri9C5INC/0YDQvtC00YPQutGCIEVSTTQgKNC00LDQu9C10LUg0L/QviDRgtC10LrRgdGC0YMg4oCTIMKr0L/RgNC+0LPRgNCw0LzQvNC90L7QtSDQvtCx0LXRgdC/0LXRh9C10L3QuNC1IEVSTTTCuyksINCS0Ysg0YHQvtCz0LvQsNGI0LDQtdGC0LXRgdGMINGB0L4g0LLRgdC10LzQuCDQvdCw0YHRgtC+0Y/RidC40LzQuCDRg9GB0LvQvtCy0LjRj9C80LgsINCwINGC0LDQutC20LUg0LTQsNC10YLQtSDRgdC+0LPQu9Cw0YHQuNC1INCf0YDQsNCy0L7QvtCx0LvQsNC00LDRgtC10LvRjiDQv9GA0L7Qs9GA0LDQvNC80L3QvtCz0L4g0L7QsdC10YHQv9C10YfQtdC90LjRjyBFUk00IC0g0JPRgNCw0LbQtNCw0L3QuNC90YMg0KDQpCDQldGA0LzQvtC70LDQtdCy0YMg0JTQvNC40YLRgNC40Y4g0KHQtdGA0LPQtdC10LLQuNGH0YMg0Lgg0LTRgNGD0LPQuNC8INC/0L7Qu9GM0LfQvtCy0LDRgtC10LvRj9C8INC/0YDQvtCz0YDQsNC80LzQvdC+0LPQviDQvtCx0LXRgdC/0LXRh9C10L3QuNGPIEVSTTQg0L3QsCDQv9C10YDQtdC00LDRh9GDINC4INC+0LHRgNCw0LHQvtGC0LrRgyDQu9GO0LHQvtC5INC40L3RhNC+0YDQvNCw0YbQuNC4INC/0YDQuCDQuNGB0L/QvtC70YzQt9C+0LLQsNC90LjQuCDQuCDRgNCw0LHQvtGC0LUg0L/RgNC+0LPRgNCw0LzQvNC90L7Qs9C+INC+0LHQtdGB0L/QtdGH0LXQvdC40Y8gRVJNNCwg0LIg0YLQvtC8INGH0LjRgdC70LUg0L/RgNC4INCy0LfQsNC40LzQvtC+0YLQvdC+0YjQtdC90LjQuCDRgSDQtNGA0YPQs9C40LzQuCDQu9C40YbQsNC80LgsINC40YHQv9C+0LvRjNC30YPRjtGJ0LjQvNC4INC/0YDQvtCz0YDQsNC80LzQvdC+0LUg0L7QsdC10YHQv9C10YfQtdC90LjQtSBFUk00INCyINGB0L7QvtGC0LLQtdGC0YHRgtCy0LjQuCDRgSDQv9C+0LvQvtC20LXQvdC40Y/QvNC4INC90LDRgdGC0L7Rj9GJ0LXQs9C+INCh0L7Qs9C70LDRiNC10L3QuNGPLiDQldGB0LvQuCDQktGLINC90LUg0L/RgNC40L3QuNC80LDQtdGC0LUg0L3QsNGB0YLQvtGP0YnQuNC1INGD0YHQu9C+0LLQuNGPLCDQktGLINC90LUg0LzQvtC20LXRgtC1INC40YHQv9C+0LvRjNC30L7QstCw0YLRjCDQvdCw0YHRgtC+0Y/RidC10LUg0L/RgNC+0LPRgNCw0LzQvNC90L7QtSDQvtCx0LXRgdC/0LXRh9C10L3QuNC1IEVSTTQuCgrQndCw0YHRgtC+0Y/RidC10LUg0YHQvtCz0LvQsNGI0LXQvdC40LUg0L7RgtC90L7RgdC40YLRgdGPINC6INC/0YDQvtCz0YDQsNC80LzQvdC+0LzRgyDQvtCx0LXRgdC/0LXRh9C10L3QuNGOIEVSTTQsINC60L7RgtC+0YDQvtC1INCS0Ysg0YPRgdGC0LDQvdCw0LLQu9C40LLQsNC10YLQtSDQvdCwINCS0LDRiNC1INGD0YHRgtGA0L7QudGB0YLQstC+INGB0LDQvNC+0YHRgtC+0Y/RgtC10LvRjNC90L4sINC70Y7QsdGL0Lwg0YjRgNC40YTRgtCw0LwsINC30L3QsNGH0LrQsNC8LCDQuNC30L7QsdGA0LDQttC10L3QuNGP0Lwg0LjQu9C4INC30LLRg9C60L7QstGL0Lwg0YTQsNC50LvQsNC8LCDQsdCw0LfQsNC80Lgg0LTQsNC90L3Ri9GFINC/0YDQtdC00L7RgdGC0LDQstC70Y/QtdC80YvQvCDQsiDRgdC+0YHRgtCw0LLQtSDQv9GA0L7Qs9GA0LDQvNC80L3QvtCz0L4g0L7QsdC10YHQv9C10YfQtdC90LjRjyBFUk00LCDQsCDRgtCw0LrQttC1INC60L4g0LLRgdC10Lwg0L7QsdC90L7QstC70LXQvdC40Y/QvCwg0LTQvtC/0L7Qu9C90LXQvdC40Y/QvCDQuNC70Lgg0YHQu9GD0LbQsdCw0Lwg0L/RgNC+0LPRgNCw0LzQvNC90L7Qs9C+INC+0LHQtdGB0L/QtdGH0LXQvdC40Y8gRVJNNCwg0LXRgdC70Lgg0LIg0LjRhSDQvtGC0L3QvtGI0LXQvdC40Lgg0L3QtSDQv9GA0LjQvNC10L3Rj9GO0YLRgdGPINC40L3Ri9C1INGD0YHQu9C+0LLQuNGPLgoK0J3QsNGB0YLQvtGP0YnQtdC1INC/0YDQvtCz0YDQsNC80LzQvdC+0LUg0L7QsdC10YHQv9C10YfQtdC90LjQtSBFUk00INC/0YDQtdC00L7RgdGC0LDQstC70Y/QtdGC0YHRjyDQv9C+INC90LXQuNGB0LrQu9GO0YfQuNGC0LXQu9GM0L3QvtC5INC70LjRhtC10L3Qt9C40Lgg0Lgg0LTQvtGB0YLRg9C/0L3QviDQtNC70Y8g0YHQutCw0YfQuNCy0LDQvdC40Y8g0L3QsCDRgdCw0LnRgtC1IGFyb25pY2xlLmNvbS4g0JIg0YDQsNC80LrQsNGFINC90LDRgdGC0L7Rj9GJ0LXQs9C+INGB0L7Qs9C70LDRiNC10L3QuNGPINCf0YDQsNCy0L7QvtCx0LvQsNC00LDRgtC10LvRjCDQv9GA0LXQtNC+0YHRgtCw0LLQu9GP0LXRgiDQstCw0Lwg0L/RgNCw0LLQviDRg9GB0YLQsNC90L7QstC40YLRjCDQuCDQt9Cw0L/Rg9GB0YLQuNGC0Ywg0LvRjtCx0L7QtSDQutC+0LvQuNGH0LXRgdGC0LLQviDRjdC60LfQtdC80L/Qu9GP0YDQvtCyINC/0YDQvtCz0YDQsNC80LzQvdC+0LPQviDQvtCx0LXRgdC/0LXRh9C10L3QuNGPIEVSTTQg0L3QsCDRg9GB0YLRgNC+0LnRgdGC0LLQtSAo0LvQuNGG0LXQvdC30LjRgNC+0LLQsNC90L3QvtC1INGD0YHRgtGA0L7QudGB0YLQstC+KSDQtNC70Y8g0L7QtNC90L7QstGA0LXQvNC10L3QvdC+0LPQviDQuNGB0L/QvtC70YzQt9C+0LLQsNC90LjRjyDQvtC00L3QuNC8INC70LjRhtC+0LwsINC10YHQu9C4INCS0Ysg0YHQvtCx0LvRjtC00LDQtdGC0LUg0LLRgdC1INGD0YHQu9C+0LLQuNGPINC90LDRgdGC0L7Rj9GJ0LXQs9C+INCh0L7Qs9C70LDRiNC10L3QuNGPLgoKMS4g0KLQtdGA0LzQuNC90Ysg0Lgg0L7Qv9GA0LXQtNC10LvQtdC90LjRjwrQn9C10YDQtdC0INC40YHQv9C+0LvRjNC30L7QstCw0L3QuNC10Lwg0L/RgNC+0LPRgNCw0LzQvNC90L7Qs9C+INC+0LHQtdGB0L/QtdGH0LXQvdC40Y8gRVJNNCDQvdCw0YHRgtC+0Y/RidC40Lwg0J/QvtC70YzQt9C+0LLQsNGC0LXQu9GMINC/0L7QvdC40LzQsNC10YIg0Lgg0YHQvtCz0LvQsNGI0LDQtdGC0YHRjyDRgdC+INCy0YHQtdC80Lgg0YLQtdGA0LzQuNC90LDQvNC4INC4INGD0YHQu9C+0LLQuNGP0LzQuCDQvdCw0YHRgtC+0Y/RidC10LPQviDQodC+0LPQu9Cw0YjQtdC90LjRjywg0LAg0LjQvNC10L3QvdC+OgrQl9Cw0LrRgNGL0YLRi9C5ICjRgdC10LrRgNC10YLQvdGL0LkpINC4INC/0YDQvtCy0LXRgNC+0YfQvdGL0LkgKNC+0YLQutGA0YvRgtGL0LkpINC60LvRjtGH0Lgg0J/QvtC70YzQt9C+0LLQsNGC0LXQu9GPIOKAlCDRjdGC0L4g0LrQu9GO0YfQuCwg0YHQvtC30LTQsNC90L3Ri9C1INCf0L7Qu9GM0LfQvtCy0LDRgtC10LvQtdC8INGBINC/0L7QvNC+0YnRjNGOINC/0YDQvtCz0YDQsNC80LzQvdC+0LPQviDQvtCx0LXRgdC/0LXRh9C10L3QuNGPIEVSTTQg0LIg0L/QvtGA0Y/QtNC60LUsINGD0YHRgtCw0L3QvtCy0LvQtdC90L3QvtC8INC90LDRgdGC0L7Rj9GJ0LjQvCDQodC+0LPQu9Cw0YjQtdC90LjQtdC8LiDQmtCw0LbQtNGL0Lkg0LfQsNC60YDRi9GC0YvQuSDQutC70Y7RhyDQuNC80LXQtdGCINGB0L7QvtGC0LLQtdGC0YHRgtCy0YPRjtGJ0LjQuSDQtdC80YMg0L7RgtC60YDRi9GC0YvQuSDQutC70Y7Rhywg0YHQvtC30LTQsNC90L3Ri9C5INGBINC/0L7QvNC+0YnRjNGOINC/0YDQvtCz0YDQsNC80LzQvdC+0LPQviDQvtCx0LXRgdC/0LXRh9C10L3QuNGPIEVSTTQuCtCt0LvQtdC60YLRgNC+0L3QvdCw0Y8g0L/QvtC00L/QuNGB0YwgKNCt0J8pIOKAlCDRg9GB0LjQu9C10L3QvdCw0Y8g0L3QtdC60LLQsNC70LjRhNC40YbQuNGA0L7QstCw0L3QvdCw0Y8g0Y3Qu9C10LrRgtGA0L7QvdC90LDRjyDQv9C+0LTQv9C40YHRjCDQutCw0Log0L3QsNCx0L7RgCDRhtC40YTRgNC+0LLRi9GFINC00LDQvdC90YvRhSwg0YHQvtC30LTQsNC90L3Ri9C5INC40Lcg0LfQsNC00LDQvdC90L7Qs9C+INC90LDQsdC+0YDQsCDRhtC40YTRgNC+0LLRi9GFINC00LDQvdC90YvRhSAo0LjQvdGE0L7RgNC80LDRhtC40LgpINC4INC90LXQutC+0YLQvtGA0L7Qs9C+INC30LDQutGA0YvRgtC+0LPQviDQutC70Y7Rh9CwINC/0YPRgtGR0Lwg0LjRgdC/0L7Qu9GM0LfQvtCy0LDQvdC40Y8g0LfQsNC00LDQvdC90L7Qs9C+ICjRgNC10LDQu9C40LfQvtCy0LDQvdC90L7Qs9C+KSDQv9C+0YDRj9C00LrQsCAo0LDQu9Cz0L7RgNC40YLQvNCwKSDQsiDQv9GA0L7Qs9GA0LDQvNC80L3QvtC8INC+0LHQtdGB0L/QtdGH0LXQvdC40LggRVJNNCwg0L/QvtC30LLQvtC70Y/RjtGJ0LDRjyDQvtC00L3QvtC30L3QsNGH0L3QviDQvtC/0YDQtdC00LXQu9C40YLRjCDQsdGL0LvQuCDQu9C4INC40LfQvNC10L3QtdC90Ysg0Y3RgtC4INC00LDQvdC90YvQtSwg0L/RgNC40YfRkdC8INGN0YLQviDQvtC/0YDQtdC00LXQu9C10L3QuNC1INC00LXQu9Cw0Y7RgiDQv9GD0YLRkdC8INC40YHQv9C+0LvRjNC30L7QstCw0L3QuNGPINC30LDQtNCw0L3QvdC+0LPQviAo0YDQtdCw0LvQuNC30L7QstCw0L3QvdC+0LPQvikg0L/QvtGA0Y/QtNC60LAgKNCw0LvQs9C+0YDQuNGC0LzQsCkg0LIg0L/RgNC+0LPRgNCw0LzQvNC90L7QvCDQvtCx0LXRgdC/0LXRh9C10L3QuNC4IEVSTTQg0L/QviDQv9GA0L7QstC10YDQvtGH0L3QvtC80YMg0LrQu9GO0YfRgywg0L/RgNC40LvQsNCz0LDQtdC80L7QvNGDINC6INGN0YLQuNC8INC00LDQvdC90YvQvCwg0Lgg0LrQvtGC0L7RgNGL0Lkg0YHQvtC+0YLQstC10YLRgdGC0LLRg9C10YIg0LjRgdC/0L7Qu9GM0LfQvtCy0LDQvdC90L7QvNGDINC00LvRjyDRgdC+0LfQtNCw0L3QuNGPINGN0YLQvtC5INCt0LvQtdC60YLRgNC+0L3QvdC+0Lkg0L/QvtC00L/QuNGB0Lgg0LfQsNC60YDRi9GC0L7QvNGDINC60LvRjtGH0YMuCtCm0LjRhNGA0L7QstCw0Y8g0LvQtdGC0L7Qv9C40YHRjCAo0YLQsNC60LbQtSDQtNCw0LvQtdC1INC/0L4g0YLQtdC60YHRgtGDIOKAnNCb0LXRgtC+0L/QuNGB0YzigJ0sIOKAnNCm0LXQv9C+0YfQutCwINC00LDQvdC90YvRheKAnSwg4oCcRGF0YWNoYWlu4oCdKSDigJQg0L/RgNC+0LjQt9Cy0LXQtNC10L3QuNC1LCDQstGL0L/QvtC70L3QtdC90L3QvtC1INCyINCy0LjQtNC1INGA0LDRgdC/0YDQtdC00LXQu9GR0L3QvdC+0Lkg0LHQsNC30Ysg0LTQsNC90L3Ri9GFINC/0L4g0YLQtdGF0L3QvtC70L7Qs9C40LggwqvQsdC70L7QutGH0LXQudC9wrsg0YEg0LjRgdC/0L7Qu9GM0LfQvtCy0LDQvdC40LXQvCDQv9GA0L7Qs9GA0LDQvNC80L3QvtCz0L4g0L/RgNC+0LTRg9C60YLQsCBFUk00LCDQt9Cw0L/QuNGB0Lgg0LIg0LrQvtGC0L7RgNC+0Lkg0LLQutC70Y7Rh9Cw0Y7RgtGB0Y8g0LIg0L3QsNCx0L7RgNGLICjQsdC70L7QutC4KSwg0YHRgdGL0LvQsNGO0YnQuNC10YHRjyDQtNGA0YPQsyDQvdCwINC00YDRg9Cz0LAsINGB0L7Qt9C00LDRjtGJ0LjQtSDRgtCw0LrQuNC8INC+0LHRgNCw0LfQvtC8INC90LXRgNCw0LfRgNGL0LLQvdGD0Y4g0YbQtdC/0L7Rh9C60YMg0L3QsNCx0L7RgNC+0LIsINGA0LDRgdC/0L7Qu9C+0LbQtdC90LjQtSDQutC+0YLQvtGA0YvRhSDQsiDRjdGC0L7QuSDRhtC10L/QvtGH0LrQtSDRgdC+0L7RgtCy0LXRgtGB0YLQstGD0LXRgiDRgNC+0YHRgtGDINCy0YDQtdC80LXQvdC4INC40YUg0YHQvtC30LTQsNC90LjRjywg0LPQtNC1INC60LDQttC00LDRjyDQt9Cw0L/QuNGB0Ywg0Lgg0LrQsNC20LTRi9C5INC90LDQsdC+0YAg0YHQvtC00LXRgNC20LjRgiDQrdC70LXQutGC0YDQvtC90L3Rg9GOINC/0L7QtNC/0LjRgdGMLCDRgdC+0LfQtNCw0L3QvdGD0Y4g0L3QsCDQvtGB0L3QvtCy0LUg0LTQsNC90L3Ri9GFINCyINC90LjRhSDQuCDQv9GA0L7QstC10YDQvtGH0L3Ri9C5INC60LvRjtGHINC00LvRjyDQvtC/0YDQtdC00LXQu9C10L3QuNGPINC90LXQuNC30LzQtdC90L3QvtGB0YLQuCDRjdGC0LjRhSDQtNCw0L3QvdGL0YUg0L/QviDRjdGC0L7QuSDQrdC70LXQutGC0YDQvtC90L3QvtC5INC/0L7QtNC/0LjRgdC4LgrQl9Cw0L/QuNGB0Ywg0JvQtdGC0L7Qv9C40YHQuCAo0JfQsNC/0LjRgdGMKSAtINC90LDQsdC+0YAg0YbQuNGE0YDQvtCy0YvRhSDQtNCw0L3QvdGL0YUsINCy0LrQu9GO0YfQsNGO0YnQuNC5INGB0LDQvNC4INC00LDQvdC90YvQtSwg0LTQsNGC0YMg0YHQvtC30LTQsNC90LjRjyDRjdGC0L7Qs9C+INC90LDQsdC+0YDQsCwg0K3Qu9C10LrRgtGA0L7QvdC90LDRjyDQv9C+0LTQv9C40YHRjCDQuCDQv9GA0L7QstC10YDQvtGH0L3Ri9C5INC60LvRjtGHLCDQv9C+INC60L7RgtC+0YDRi9C8INGB0L7Qs9C70LDRgdC90L4g0L/QvtGA0Y/QtNC60LAgKNCw0LvQs9C+0YDQuNGC0LzQsCkg0L/RgNC+0LPRgNCw0LzQvNC90L7Qs9C+INC+0LHQtdGB0L/QtdGH0LXQvdC40Y8gRVJNNCDQvNC+0LbQvdC+INC+0L/RgNC10LTQtdC70LjRgtGMINC90LXQuNC30LzQtdC90L3QvtGB0YLRjCDQtNCw0L3QvdGL0YUg0LIg0LfQsNC/0LjRgdC4INC4INGB0L7QvtGC0LLQtdGC0YHRgtCy0LjQtSDQuNGFINC00LDQvdC90L7QuSDQrdC70LXQutGC0YDQvtC90L3QvtC5INC/0L7QtNC/0LjRgdC4INC/0L4g0L/RgNC40LvQsNCz0LDQtdC80L7QvNGDINC/0YDQvtCy0LXRgNC+0YfQvdC+0LzRgyDQutC70Y7Rh9GDLgrQn9C+0LvRjNC30L7QstCw0YLQtdC70Ywg0JvQtdGC0L7Qv9C40YHQuCAo0J/QvtC70YzQt9C+0LLQsNGC0LXQu9GMKSAtINC70LjRhtC+LCDQuNGB0L/QvtC70YzQt9GD0Y7RidC10LUg0L3QsNGB0YLQvtGP0YnQtdC1INC/0YDQvtCz0YDQsNC80LzQvdC+0LUg0L7QsdC10YHQv9C10YfQtdC90LjQtSBFUk00INC00LvRjyDQtNC+0YHRgtGD0L/QsCDQuiDQm9C10YLQvtC/0LjRgdC4INC00LvRjyDRg9C00L7QstC70LXRgtCy0L7RgNC10L3QuNGPINGB0LLQvtC40YUg0L3Rg9C20LQuCtCj0YfRkdGC0L3QsNGPINC10LTQuNC90LjRhtCwICjQldC00LjQvdC40YbQsCkg4oCUINGH0LjRgdC70L7QstCw0Y8g0LLQtdC70LjRh9C40L3QsCwg0YPRh9GR0YIg0LrQvtGC0L7RgNC+0Lkg0LLQtdC00ZHRgtGB0Y8g0LIg0JvQtdGC0L7Qv9C40YHQuC4K0KHRh9GR0YIg0JvQtdGC0L7Qv9C40YHQuCAo0KHRh9GR0YIpIC0g0Y3RgtC+INGD0YHQtdGH0LXQvdC90YvQuSDQv9C+INC/0L7RgNGP0LTQutGDICjQsNC70LPQvtGA0LjRgtC80YMpLCDQt9Cw0LTQsNC90L3QvtC80YMg0LIg0L/RgNC+0LPRgNCw0LzQvNC90L7QvCDQvtCx0LXRgdC/0LXRh9C10L3QuNC4IEVSTTQsINC+0YLQutGA0YvRgtGL0Lkg0LrQu9GO0YcuINCa0LDQuiDQv9GA0LDQstC40LvQviDQvdCwINGB0YfQtdGC0LDRhSDRhdGA0LDQvdGP0YLRgdGPINCy0LXQu9C40YfQuNC90Ysg0YPRh9GR0YLQvdGL0YUg0LXQtNC40L3QuNGGINC4INC60LDQuiDQv9GA0LDQstC40LvQviDQuiDRgdGH0LXRgtCw0Lwg0L/RgNC40LLRj9C30LDQvdGLINCy0YHQtSDQtNC10LnRgdGC0LLQuNGPINCyINCb0LXRgtC+0L/QuNGB0LguCtCt0LvQtdC60YLRgNC+0L3QvdCw0Y8g0L/QvtC00L/QuNGB0Ywg0J/QvtC70YzQt9C+0LLQsNGC0LXQu9GPIC0g0K3Qu9C10LrRgtGA0L7QvdC90LDRjyDQv9C+0LTQv9C40YHRjCwg0LrQvtGC0L7RgNCw0Y8g0YHQvtC30LTQsNC90LAg0YEg0L/QvtC80L7RidGM0Y4g0L/RgNC+0LPRgNCw0LzQvNC90L7Qs9C+INC+0LHQtdGB0L/QtdGH0LXQvdC40Y8gRVJNNCDQuNC3INC30LDQutGA0YvRgtC+0LPQviDQutC70Y7Rh9CwLCDQutC+0YLQvtGA0YvQvCDQvtCx0LvQsNC00LDQtdGCINC00LDQvdC90YvQuSDQn9C+0LvRjNC30L7QstCw0YLQtdC70YwuCtCa0L7Qu9C40YfQtdGB0YLQstC+INC/0L7QtNGC0LLQtdGA0LbQtNC10L3QuNC5INC30LDQv9C40YHQuCAo0J/QvtC00YLQstC10YDQttC00LXQvdC40Y8pIOKAlCDRh9C40YHQu9C+INC90LDQsdC+0YDQvtCyINCyINGG0LXQv9C+0YfQutC1LCDQstC60LvRjtGH0LXQvdC90YvRhSDQv9C+0YHQu9C1INC90LDQsdC+0YDQsCwg0LIg0LrQvtGC0L7RgNC+0Lwg0YDQsNGB0L/QvtC70L7QttC10L3QsCDRjdGC0LAg0LfQsNC/0LjRgdGMLCDQv9C70Y7RgSAxLiDQldGB0LvQuCDQvdCw0LHQvtGAINC10YnQtSDQvdC1INCy0LrQu9GO0YfQtdC9INCyINGG0LXQv9C+0YfQutGDLCDQuNC70Lgg0YHQsNC80LAg0LfQsNC/0LjRgdGMINC90LUg0YDQsNGB0L/QvtC70L7QttC10L3QsCDQsiDQutCw0LrQvtC8LdC70LjQsdC+INC90LDQsdC+0YDQtSwg0YLQviDRh9C40YHQu9C+INC/0L7QtNGC0LLQtdGA0LbQtNC10L3QuNC5INC00LvRjyDQt9Cw0L/QuNGB0Lgg0YDQsNCy0L3QviDQvdGD0LvRjiwg0Lgg0L7QvdCwINGB0YfQuNGC0LDRjtGC0YHRjyDQvdC10L/QvtC00YLQstC10YDQttC00LXQvdC90L7QuS4K0J/RgNCw0LLQvtCy0LDRjyDQtdC00LjQvdC40YbQsCDigJQg0YPRh9GR0YLQvdCw0Y8g0LXQtNC40L3QuNGG0LAsINCy0LjQtNCwIOKAnNCw0LrRgtC40LLigJ0g0YEg0L3QvtC80LXRgNC+0LwgMSwg0Lgg0LrQvtGC0L7RgNCw0Y8g0LTQsNC10YIg0L/RgNCw0LLQviDQn9C+0LvRjNC30L7QstCw0YLQtdC70Y4sINC+0LHQu9Cw0LTQsNGO0YnQtdC80YMg0LTQvtGB0YLQsNGC0L7Rh9C90YvQvCDQutC+0LvQuNGH0LXRgdGC0LLQvtC8INGN0YLQuNGFINC10LTQuNC90LjRhiwg0L/QvtGA0L7QsyDQtNC+0YHRgtCw0YLQvtGH0L3QvtGB0YLQuCDQutC+0YLQvtGA0L7Qs9C+ICjQvtGC0L0uINC6IMKr0LrQvtC70LjRh9C10YHRgtCy0L7QvMK7KSDQt9Cw0LTQsNC9INCyINC/0YDQvtCz0YDQsNC80LzQvdC+0Lwg0L7QsdC10YHQv9C10YfQtdC90LjQuCBFUk00LCDRgdC+0LfQtNCw0LLQsNGC0Ywg0L3QsNCx0L7RgNGLLgrQoNCw0LHQvtGH0LDRjyDQtdC00LjQvdC40YbQsCDigJQg0YPRh9GR0YLQvdCw0Y8g0LXQtNC40L3QuNGG0LAsINCy0LjQtNCwIOKAnNCw0LrRgtC40LLigJ0g0YEg0L3QvtC80LXRgNC+0LwgMiwg0Lgg0LrQvtGC0L7RgNCw0Y8g0LjRgdC/0L7Qu9GM0LfRg9C10YLRgdGPINC00LvRjyDQvtC/0LvQsNGC0Ysg0YPRgdC70YPQs9C4INCy0L3QtdGB0LXQvdC40Y8g0LfQsNC/0LjRgdC10Lkg0LIg0L3QsNCx0L7RgCDQuCDQt9Cw0YLQtdC8INCyINCb0LXRgtC+0L/QuNGB0YwuCtCj0LTQvtGB0YLQvtCy0LXRgNC10L3QuNC1INCf0L7Qu9GM0LfQvtCy0LDRgtC10LvRjyAtINGB0L7Qt9C00LDQvdC40LUg0YHQvtC+0YLQstC10YLRgdGC0LLQuNGPINC80LXQttC00YMg0J/QvtC70YzQt9C+0LLQsNGC0LXQu9C10Lwg0JvQtdGC0L7Qv9C40YHQuCDQuCDQtdCz0L4g0L7RgtC60YDRi9GC0YvQvCDQutC70Y7Rh9C+0LwsINC/0L7Qt9Cy0L7Qu9GP0Y7RidC10LUg0L7Qv9GA0LXQtNC10LvQuNGC0YwgKNC40LTQtdC90YLQuNGE0LjRhtC40YDQvtCy0LDRgtGMKSDQtdCz0L4g0L/RgNC40YfQsNGB0YLQvdC+0YHRgtGMINC6INGB0L7Qt9C00LDQvdC40Y4g0K3Qu9C10LrRgtGA0L7QvdC90L7QuSDQv9C+0LTQv9C40YHQuCwg0L/Rg9GC0ZHQvCDRgdC+0LfQtNCw0L3QuNGPINC4INCy0L3QtdGB0LXQvdC40Y8g0LIg0JvQtdGC0L7Qv9C40YHRjCDQv9GA0Lgg0L/QvtC80L7RidC4INC/0YDQvtCz0YDQsNC80LzQvdC+0LPQviDQvtCx0LXRgdC/0LXRh9C10L3QuNGPIEVSTTQg0L7RgdC+0LHQvtC5INCj0LTQvtGB0YLQvtCy0LXRgNGP0Y7RidC10Lkg0LfQsNC/0LjRgdC4LCAtICDQl9Cw0L/QuNGB0Lgg0YEg0LrQvtC00L7QvCAzNi4K0JLQu9Cw0LTQtdC90LjQtSwg0LLQviDQstC70LDQtNC10L3QuNC4IOKAlCDRjdGC0L4g0YLQviDRh9GC0L4g0J/QvtC70YzQt9C+0LLQsNGC0LXQu9GMINC40LzQtdC10YIg0LrQsNC6INGB0L7QsdGB0YLQstC10L3QvdC+0YHRgtGMLgrQo9C/0YDQsNCy0LvQtdC90LjQtSwg0LIg0YPQv9GA0LDQstC70LXQvdC40LggLSDRgtC+INGH0LXQvCDQv9C+0LvRjNC30L7QstCw0YLQtdC70Ywg0YPQv9GA0LDQstC70Y/QtdGCINC4INC10LzRgyDRjdGC0L4g0L3QtSDQv9GA0LjQvdCw0LTQu9C10LbQuNGCINC60LDQuiDQtdCz0L4g0YHQvtCx0YHRgtCy0LXQvdC90L7RgdGC0YwuCtCe0LHQu9Cw0LTQsNC90LjQtSwg0LIg0L7QsdC70LDQtNCw0L3QuNC4IC0g0Y3RgtC+INGB0L7QstC+0LrRg9C/0L3QvtGB0YLRjCDQuNC80YPRidC10YHRgtCy0LAsINC90LDRhdC+0LTRj9GJ0LXQs9C+0YHRjyDQsiDRgdC+0LHRgdGC0LLQtdC90L3QvtGB0YLQuCDQuCDQsiDRg9C/0YDQsNCy0LvQtdC90LjQuCwg0L/RgNC40YfQtdC8INC10YHQu9C4INCf0L7Qu9GM0LfQvtCy0LDRgtC10LvRjCDQv9C+0LvRg9GH0LjQuyDQsiDRg9C/0YDQsNCy0LvQtdC90LjQtSDQodC+0LHRgdGC0LLQtdC90L3QvtGB0YLRjCwg0YLQviDQvtC90LAg0YPRh9C40YLRi9Cy0LDQtdGC0YHRjyDQutCw0Log0L/QvtC70L7QttC40YLQtdC70YzQvdCw0Y8g0LLQtdC70LjRh9C40L3QsCwg0LAg0LXRgdC70Lgg0L/QtdGA0LXQtNCw0Lsg0LIg0YPQv9GA0LDQstC70LXQvdC40LUg0LTRgNGD0LPQvtC80YMg0L/QvtC70YzQt9C+0LLQsNGC0LXQu9GOLCDRgtC+INC60LDQuiDQvtGC0YDQuNGG0LDRgtC10LvRjNC90LDRjyDQstC10LvQuNGH0LjQvdCwLiDQndCw0L/RgNC40LzQtdGAINC10YHQu9C4INC/0L7Qu9GM0LfQvtCy0LDRgtC10LvRjCDCq9CQwrsg0LjQvNC10LXRgiDQsiDRgdC+0LHRgdGC0LLQtdC90L3QvtGB0YLQuCAxMDAwMCDQtdC00LjQvdC40YYg0Lgg0LLRi9C00LDQuyDQsiDRg9C/0YDQsNCy0LvQtdC90LjQtSDQn9C+0LvRjNC30L7QstCw0YLQtdC70Y4gwqvQkcK7IDMwMDAg0LXQtNC40L3QuNGGLCDRgtC+INCyINC+0LHQu9Cw0LTQsNC90LjQuCDRgyDQvdC10LPQviDQvtGB0YLQsNC90LXRgtGB0Y8gMTAwMDAgLSAzMDAwID0gNzAwMCDQtdC00LjQvdC40YYuCtCX0LDQstC10YDQuNGC0LXQu9GMIC0g0J/QvtC70YzQt9C+0LLQsNGC0LXQu9GMINCb0LXRgtC+0L/QuNGB0LgsINC+0LHQu9Cw0LTQsNGO0YnQuNC5INC00L7RgdGC0LDRgtC+0YfQvdGL0Lwg0LrQvtC70LjRh9C10YHRgtCy0L7QvCDQv9GA0LDQstC+0LLRi9GFINC10LTQuNC90LjRhiwg0L/QvtGA0L7QsyDQtNC+0YHRgtCw0YLQvtGH0L3QvtGB0YLQuCDQutC+0YLQvtGA0L7Qs9C+ICjQvtGC0L0uINC6IMKr0LrQvtC70LjRh9C10YHRgtCy0L7QvMK7KSDQt9Cw0LTQsNC9INCyINC/0YDQvtCz0YDQsNC80LzQvdC+0Lwg0L7QsdC10YHQv9C10YfQtdC90LjQuCBFUk00LCDQuCDQutC+0YLQvtGA0L7QtSDQv9C+0LfQstC+0LvRj9C10YIg0LXQvNGDLCDQu9C40LHQviDQv9C+INC00YDRg9Cz0LjQvCDQv9GA0LDQstC40LvQsNC8INC30LDQtNCw0L3QvdGL0Lwg0LIg0L/RgNC+0LPRgNCw0LzQvNC90L7QvCDQvtCx0LXRgdC/0LXRh9C10L3QuNC4IEVSTTQsINC/0YDQvtC40LfQstC+0LTQuNGC0Ywg0YPQtNC+0YHRgtC+0LLQtdGA0LXQvdC40LUg0LTRgNGD0LPQvtCz0L4g0J/QvtC70YzQt9C+0LLQsNGC0LXQu9GPLiDQotC+0LvRjNC60L4g0JfQsNCy0LXRgNC40YLQtdC70Lgg0LjQvNC10Y7RgiDQv9GA0LDQstC+INGD0LTQvtGB0YLQvtCy0LXRgNGP0YLRjCDQn9C+0LvRjNC30L7QstCw0YLQtdC70LXQuS4K0KTQvtGA0LbQtdGAIC0g0J/QvtC70YzQt9C+0LLQsNGC0LXQu9GMINCb0LXRgtC+0L/QuNGB0LgsINC+0LHQu9Cw0LTQsNGO0YnQuNC5INC00L7RgdGC0LDRgtC+0YfQvdGL0Lwg0LrQvtC70LjRh9C10YHRgtCy0L7QvCDQv9GA0LDQstC+0LLRi9GFINC10LTQuNC90LjRhiwg0L/QvtGA0L7QsyDQtNC+0YHRgtCw0YLQvtGH0L3QvtGB0YLQuCDQutC+0YLQvtGA0L7Qs9C+ICjQvtGC0L0uINC6IMKr0LrQvtC70LjRh9C10YHRgtCy0L7QvMK7KSDQt9Cw0LTQsNC9INCyINC/0YDQvtCz0YDQsNC80LzQvdC+0Lwg0L7QsdC10YHQv9C10YfQtdC90LjQuCBFUk00LCDQuCDQutC+0YLQvtGA0L7QtSDQv9C+0LfQstC+0LvRj9C10YIg0LXQvNGDLCDQu9C40LHQviDQv9C+INC00YDRg9Cz0LjQvCDQv9GA0LDQstC40LvQsNC8INC30LDQtNCw0L3QvdGL0Lwg0LIg0L/RgNC+0LPRgNCw0LzQvNC90L7QvCDQvtCx0LXRgdC/0LXRh9C10L3QuNC4IEVSTTQsINGB0L7QsdC40YDQsNGC0Ywg0L3QsNCx0L7RgNGLINC30LDQv9C40YHQtdC5INC4INCy0L3QvtGB0LjRgtGMINC40YUg0LIg0JvQtdGC0L7Qv9C40YHRjC4g0KLQvtC70YzQutC+INCk0L7RgNC20LXRgNGLINC40LzQtdGO0YIg0L/RgNCw0LLQviDQstC90L7RgdC40YLRjCDQl9Cw0L/QuNGB0Lgg0LIg0LLQuNC00LUg0LfQsNGP0LLQvtC6INCyINC90LDQsdC+0YDRiyDQt9Cw0L/QuNGB0LXQuSwg0YHQvtC30LTQsNCy0LDRgtGMINGN0YLQuCDQvdCw0LHQvtGA0YssINC/0L7QtNC/0LjRgdGL0LLQsNGC0Ywg0LjRhSDRgdCy0L7QuNC80Lgg0K3Qu9C10LrRgtGA0L7QvdC90YvQvNC4INC/0L7QtNC/0LjRgdGP0LzQuCDQuCDQstC90L7RgdC40YLRjCDRjdGC0Lgg0L3QsNCx0L7RgNGLINCyINCb0LXRgtC+0L/QuNGB0YwuCtCj0LTQvtGB0YLQvtCy0LXRgNC10L3QvdGL0Lkg0L/QvtC70YzQt9C+0LLQsNGC0LXQu9GMICjQo9GH0LDRgdGC0L3QuNC6KSDigJQg0J/QvtC70YzQt9C+0LLQsNGC0LXQu9GMINCb0LXRgtC+0L/QuNGB0Lgg0LLQu9Cw0LTQtdC90LjQtSDQvtGC0LrRgNGL0YLRi9C8INC60LvRjtGH0L7QvCwg0LrQvtGC0L7RgNC+0LPQviDRg9C00L7RgdGC0L7QstC10YDQtdC90L4g0JfQsNCy0LXRgNC40YLQtdC70LXQvCwg0L/RgNC4INGN0YLQvtC8INGC0LDQutC+0Lkg0L7RgtC60YDRi9GC0YvQuSDQutC70Y7RhyDRj9Cy0LvRj9C10YLRgdGPINGD0LTQvtGB0YLQvtCy0LXRgNC10L3QvdGL0Lwg0LrQu9GO0YfQvtC8LCDQsCDRgdC+0LfQtNCw0L3QvdGL0LUg0Y3Qu9C10LrRgtGA0L7QvdC90YvQtSDQv9C+0LTQv9C40YHQuCwg0LrQvtGC0L7RgNGL0LUg0L/RgNC+0LLQtdGA0Y/RjtGC0YHRjyDRgtCw0LrQuNC8INGD0LTQvtGB0YLQvtCy0LXRgNC10L3QvdGL0Lwg0L7RgtC60YDRi9GC0YvQvCDQutC70Y7Rh9C+0Lwg0Y/QstC70Y/RjtGC0YHRjyDRg9C00L7RgdGC0L7QstC10YDQtdC90L3Ri9C80LguCtCU0LXQudGB0YLQstC40YLQtdC70YzQvdCw0Y8g0LfQsNC/0LjRgdGMICjQl9Cw0YDRg9Cx0LrQsCkgLSDQt9Cw0L/QuNGB0Ywg0JvQtdGC0L7Qv9C40YHQuCwg0L/QvtC70YPRh9C40LLRiNCw0Y8gMzAg0Lgg0LHQvtC70LXQtSDQv9C+0LTRgtCy0LXRgNC20LTQtdC90LjQuS4K0J/QvtC00YLQstC10YDQttC00LXQvdC90LDRjyDQt9Cw0L/QuNGB0YwgKNCX0LDQv9C40YHRjCkgLSDQt9Cw0L/QuNGB0Ywg0JvQtdGC0L7Qv9C40YHQuCwg0L/QvtC70YPRh9C40LLRiNCw0Y8gMyDQuCDQsdC+0LvQtdC1INCf0L7QtNGC0LLQtdGA0LbQtNC10L3QuNC5LgrQl9Cw0Y/QstC70LXQvdC90LDRjyDQt9Cw0L/QuNGB0YwgKNCX0LDRj9Cy0LrQsCkgLSDQt9Cw0L/QuNGB0Ywg0JvQtdGC0L7Qv9C40YHQuCwg0L3QtSDQv9C+0LvRg9GH0LjQstGI0LDRjyDQvdC4INC+0LTQvdC+0LPQviDQn9C+0LTRgtCy0LXRgNC20LTQtdC90LjRjyAo0L3QtdC/0L7QtNGC0LLQtdGA0LbQtNGR0L3QvdCw0Y8g0LfQsNC/0LjRgdGMKSDigJQg0L3QtdC/0L7QtNGC0LLQtdGA0LbQtNGR0L3QvdCw0Y8g0JfQsNC/0LjRgdGMLgrQo9C00L7RgdGC0L7QstC10YDQtdC90L3Ri9C5INGB0YfRkdGCIC0g0YHRh9C10YIsINGB0L7Qt9C00LDQvdC90YvQuSDRg9C00L7RgdGC0L7QstC10YDQtdC90L3Ri9C8INC+0YLQutGA0YvRgtGL0Lwg0LrQu9GO0YfQvtC8LgrQo9GB0YLRgNC+0LnRgdGC0LLQviDigJUg0Y3RgtC+INCw0L/Qv9Cw0YDQsNGC0L3QsNGPINGB0LjRgdGC0LXQvNCwICjRhNC40LfQuNGH0LXRgdC60LDRjyDQuNC70Lgg0LLQuNGA0YLRg9Cw0LvRjNC90LDRjykg0YHQviDQstGB0YLRgNC+0LXQvdC90YvQvCDQt9Cw0L/QvtC80LjQvdCw0Y7RidC40Lwg0YPRgdGC0YDQvtC50YHRgtCy0L7QvCwg0LIg0LrQvtGC0L7RgNC+0Lkg0LzQvtC20LXRgiDQsdGL0YLRjCDQt9Cw0L/Rg9GJ0LXQvdC+INC/0YDQvtCz0YDQsNC80LzQvdC+0LUg0L7QsdC10YHQv9C10YfQtdC90LjQtSBFUk00LiDQmtCw0LbQtNGL0Lkg0LDQv9C/0LDRgNCw0YLQvdGL0Lkg0YDQsNC30LTQtdC7INC40LvQuCDRgdGC0L7QtdGH0L3Ri9C5INC80L7QtNGD0LvRjCDRgdGH0LjRgtCw0LXRgtGB0Y8g0YPRgdGC0YDQvtC50YHRgtCy0L7QvC4KCgoKCjIuINCe0LPRgNCw0L3QuNGH0LXQvdC40Y8K0J/RgNCw0LLQvtC+0LHQu9Cw0LTQsNGC0LXQu9GMINGB0L7RhdGA0LDQvdGP0LXRgiDQstGB0LUg0L/RgNCw0LLQsCwg0LLQutC70Y7Rh9Cw0Y8g0L/RgNCw0LLQsCwg0L/RgNC10LTRg9GB0LzQvtGC0YDQtdC90L3Ri9C1INC30LDQutC+0L3QsNC80Lgg0L4g0LfQsNGJ0LjRgtC1INC40L3RgtC10LvQu9C10LrRgtGD0LDQu9GM0L3QvtC5INGB0L7QsdGB0YLQstC10L3QvdC+0YHRgtC4LCDQutC+0YLQvtGA0YvQtSDQvdC1INC/0YDQtdC00L7RgdGC0LDQstC70Y/RjtGC0YHRjyDRj9Cy0L3Ri9C8INC+0LHRgNCw0LfQvtC8INCyINGA0LDQvNC60LDRhSDQvdCw0YHRgtC+0Y/RidC10LPQviDRgdC+0LPQu9Cw0YjQtdC90LjRjy4g0JTQsNC90L3QsNGPINC70LjRhtC10L3Qt9C40Y8g0L3QtSDQv9GA0LXQtNC+0YHRgtCw0LLQu9GP0LXRgiDQktCw0Lwg0L/RgNCw0LLQsDoKYS4g0L/RgNC10LTQvtGB0YLQsNCy0LvRj9GC0Ywg0L/RgNC+0LPRgNCw0LzQvNC90L7QtSDQvtCx0LXRgdC/0LXRh9C10L3QuNC1IEVSTTQg0LfQsCDQvtC/0LvQsNGC0YMg0LIg0L/RgNC+0LrQsNGCLCDQsiDQsNGA0LXQvdC00YMg0LjQu9C4INCy0L4g0LLRgNC10LzQtdC90L3QvtC1INC/0L7Qu9GM0LfQvtCy0LDQvdC40LU7CmIuINC/0YDQvtC00LDQstCw0YLRjCwg0YPRgdGC0YPQv9Cw0YLRjCDQv9GA0LDQstCwINC30LAg0L7Qv9C70LDRgtGDLCDQuNC70Lgg0LvRjtCx0YvQvCDQuNC90YvQvCDRgdC/0L7RgdC+0LHQvtC8INC/0LXRgNC10LTQsNCy0LDRgtGMINC30LAg0L/Qu9Cw0YLRgyDQv9GA0L7Qs9GA0LDQvNC80L3QvtC1INC+0LHQtdGB0L/QtdGH0LXQvdC40LUgRVJNNDsKYy4g0L/Ri9GC0LDRgtGM0YHRjyDQvtCx0L7QudGC0Lgg0YLQtdGF0L3QuNGH0LXRgdC60LjQtSDQvtCz0YDQsNC90LjRh9C10L3QuNGPINCyINC/0YDQvtCz0YDQsNC80LzQvdC+0Lwg0L7QsdC10YHQv9C10YfQtdC90LjQuCBFUk00OwpkLiDQsiDRgdC70YPRh9Cw0LUg0LXRgdC70Lgg0LIg0L/RgNC+0LPRgNCw0LzQvNC90L7QvCDQvtCx0LXRgdC/0LXRh9C10L3QuNC4IEVSTTQg0L/RgNC10LTQvtGB0YLQsNCy0LvQtdC90L4g0J/QvtC70YzQt9C+0LLQsNGC0LXQu9GOINCx0LXQtyDQuNGB0YXQvtC00L3Ri9GFINC60L7QtNC+0LIsIC0g0LjQt9GD0YfQsNGC0Ywg0YLQtdGF0L3QvtC70L7Qs9C40Y4sINC00LXQutC+0LzQv9C40LvQuNGA0L7QstCw0YLRjCwg0LTQtdCw0YHRgdC10LzQsdC70LjRgNC+0LLQsNGC0Ywg0L/RgNC+0LPRgNCw0LzQvNC90L7QtSDQvtCx0LXRgdC/0LXRh9C10L3QuNC1IEVSTTQg0LjQu9C4INC/0YDQtdC00L/RgNC40L3QuNC80LDRgtGMINC/0L7Qv9GL0YLQutC4INGB0L7QstC10YDRiNC10L3QuNGPINGC0LDQutC40YUg0LTQtdC50YHRgtCy0LjQuSwg0LfQsCDQuNGB0LrQu9GO0YfQtdC90LjQtdC8INGB0LvRg9GH0LDQtdCyLCDQutC+0LPQtNCwINCy0YvRiNC10YPQv9C+0LzRj9C90YPRgtGL0LUg0LTQtdC50YHRgtCy0LjRjyDRgNCw0LfRgNC10YjQtdC90Ysg0L/RgNC40LzQtdC90LjQvNGL0Lwg0L/RgNCw0LLQvtC8OwplLiDQstC90L7RgdC40YLRjCDQuNC30LzQtdC90LXQvdC40Y8g0LIg0L/RgNC+0LPRgNCw0LzQvNC90L7QtSDQvtCx0LXRgdC/0LXRh9C10L3QuNC1IEVSTTQsINC90LDRgNGD0YjQsNGO0YnQuNC1INCw0LLRgtC+0YDRgdC60LjQtSDQv9GA0LDQstCwINC40LvQuCDQtNCw0L3QvdC+0LUg0KHQvtCz0LvQsNGI0LXQvdC40LUsINC40LvQuCDQv9C+0YDRj9C00L7QuiAo0LDQu9Cz0L7RgNC40YLQvCDQuCDQv9GA0L7RgtC+0LrQvtC7KSDRgNCw0LHQvtGC0Ysg0L/RgNC+0LPRgNCw0LzQvNC90L7Qs9C+INC+0LHQtdGB0L/QtdGH0LXQvdC40Y8gRVJNNCDQsdC10Lcg0YDQsNC30YDQtdGI0LXQvdC40Y8g0YHQviDRgdGC0L7RgNC+0L3RiyDQn9GA0LDQstC+0L7QsdC70LDQtNCw0YLQtdC70Y87CmYuINC/0YDQuCDQuNGB0L/QvtC70YzQt9C+0LLQsNC90LjQuCDQuNC90YLQtdGA0L3QtdGCLdCy0L7Qt9C80L7QttC90L7RgdGC0LXQuSDQv9GA0L7Qs9GA0LDQvNC80L3QvtCz0L4g0L7QsdC10YHQv9C10YfQtdC90LjRjyBFUk00INCS0Ysg0L3QtSDQuNC80LXQtdGC0LUg0L/RgNCw0LLQsCDQuNGB0L/QvtC70YzQt9C+0LLQsNGC0Ywg0LjRhSDQutCw0LrQuNC8LdC70LjQsdC+INC+0LHRgNCw0LfQvtC8LCDQutC+0YLQvtGA0YvQuSDQvNC+0LbQtdGCINC/0L7QvNC10YjQsNGC0Ywg0LjRhSDQuNGB0L/QvtC70YzQt9C+0LLQsNC90LjRjiDQtNGA0YPQs9C40LzQuCDQu9C40YbQsNC80Lgg0LjQu9C4INC40YHQutCw0LfQuNGC0Ywg0L/QtdGA0LXQtNCw0LLQsNC10LzRg9GOINCS0LDQvNC4INC40L3RhNC+0YDQvNCw0YbQuNGOINGC0YDQtdGC0YzQuNC8INC70LjRhtCw0LwuCgozLiDQn9GA0LXQtNGD0L/RgNC10LbQtNC10L3QuNGPINCx0LXQt9C+0L/QsNGB0L3QvtGB0YLQuCDQuCDRgNC40YHQutC4INCf0L7Qu9GM0LfQvtCy0LDRgtC10LvRjwrQn9C+0LvRjNC30L7QstCw0YLQtdC70Ywg0L3QtdGB0LXRgiDQv9C+0LvQvdGD0Y4g0L7RgtCy0LXRgtGB0YLQstC10L3QvdC+0YHRgtGMINC30LAg0LHQtdC30L7Qv9Cw0YHQvdC+0YHRgtGMINGB0LLQvtC10LPQviDQutC+0LzQv9GM0Y7RgtC10YDQsC4g0JXRgdC70Lgg0LrQvtC80L/RjNGO0YLQtdGAINCf0L7Qu9GM0LfQvtCy0LDRgtC10LvRjyDRgdC60L7QvNC/0YDQvtC80LXRgtC40YDQvtCy0LDQvSwg0YLQviDQn9C+0LvRjNC30L7QstCw0YLQtdC70Ywg0LzQvtC20LXRgiDQv9C+0L3QtdGB0YLQuCDRg9GJ0LXRgNCxINC4INGD0LHRi9GC0LrQuC4g0JjQvNC10L3QvdC+INCf0L7Qu9GM0LfQvtCy0LDRgtC10LvRjCDQv9GA0L7Qs9GA0LDQvNC80L3QvtCz0L4g0L7QsdC10YHQv9C10YfQtdC90LjRjyBFUk00INC+0YLQstC10YLRgdGC0LLQtdC9INC30LAg0LLRgdC1INGB0LLQvtC4INC00LXQudGB0YLQstC40Y8g0YHQsNC8LiDQldGB0LvQuCDQn9C+0LvRjNC30L7QstCw0YLQtdC70Ywg0YHQu9C+0LzQsNC10YIg0YfRgtC+LdGC0L4g0LjQu9C4INC90LDRgNGD0YjQuNGCINC70Y7QsdGL0LUg0LfQsNC60L7QvdGLLCDRgtC+INGN0YLQviDQtdCz0L4g0L7RgtCy0LXRgtGB0YLQstC10L3QvdC+0YHRgtGMINC4INGC0L7Qu9GM0LrQviDQtdCz0L4g0L7RgtCy0LXRgtGB0YLQstC10L3QvdC+0YHRgtGMLgrQotCw0LrQttC1INCf0L7Qu9GM0LfQvtCy0LDRgtC10LvRjCDRgdC+0LPQu9Cw0YjQsNGP0YHRjCDRgSDQvdCw0YHRgtC+0Y/RidC40Lwg0YHQvtCz0LvQsNGI0LXQvdC40LXQvCDQv9C+0LTRgtCy0LXRgNC20LTQsNC10YIsINGH0YLQviDQvtC9INC30L3QsNC10YIsINGH0YLQviDQn9GA0LDQstC+0L7QsdC70LDQtNCw0YLQtdC70Ywg0L3QtSDQstC+0YHQv9C+0LvQvdGP0LXRgiDQv9C+0YLQtdGA0LgsINGD0LHRi9GC0LrQuCDQuCDQvdC1INC90LXRgdC10YIg0L7RgtCy0LXRgtGB0YLQstC10L3QvdC+0YHRgtC4INC30LAg0LvRjtCx0YvQtSDRgdC+0LHRi9GC0LjRjyDQuCDRgNC40YHQutC4LCDQstC+0LfQvdC40LrQsNGO0YnQuNC1INCyINGB0LvQtdC00YHRgtCy0LjQuCDRgdC+0LHRi9GC0LjQuSDQuNC3INGB0LvQtdC00YPRjtGJ0LjRhSDQutCw0YLQtdCz0L7RgNC40Lkg0LfQsCDQutC+0YLQvtGA0YvQtSDQn9C+0LvRjNC30L7QstCw0YLQtdC70Ywg0LHQtdGA0LXRgiDQvdCwINGB0LXQsdGPINC+0YLQstC10YLRgdGC0LLQtdC90L3QvtGB0YLRjCDQsiDQv9C+0LvQvdC+0Lwg0L7QsdGK0LXQvNC1OgphLiDQntGI0LjQsdC60Lgg0L/QvtC70YzQt9C+0LLQsNGC0LXQu9GPLCDRgdCy0Y/Qt9Cw0L3QvdGL0LUg0YEg0L/RgNC+0LPRgNCw0LzQvNC90YvQvCDQvtCx0LXRgdC/0LXRh9C10L3QuNC10LwgRVJNNCwg0LrQsNC6LdGC0L4g0LfQsNCx0YvRgtGL0LUg0L/QsNGA0L7Qu9C4LCDQv9C70LDRgtC10LbQuCDQv9C+INC90LXQv9GA0LDQstC40LvRjNC90L7QvNGDINCw0LTRgNC10YHRgyDQutC+0YjQtdC70YzQutCwLCDRgdC70YPRh9Cw0LnQvdC+0LUg0YPQtNCw0LvQtdC90LjQtSDQutC+0YjQtdC70YzQutCwLgpiLiDQn9GA0L7QsdC70LXQvNGLINGE0YPQvdC60YbQuNC+0L3QuNGA0L7QstCw0L3QuNGPINC/0YDQvtCz0YDQsNC80LzQvdC+0LPQviDQvtCx0LXRgdC/0LXRh9C10L3QuNGPIEVSTTQg0LrQsNC6LdGC0L46INC/0L7QstGA0LXQttC00LXQvdC90YvQuSDRhNCw0LnQuy3QutC+0YjQtdC70LXQuiwg0L3QtdC/0YDQsNCy0LjQu9GM0L3QviDRgdC+0LfQtNCw0L3QvdGL0LUg0YLRgNCw0L3Qt9Cw0LrRhtC40LgsINC90LXQsdC10LfQvtC/0LDRgdC90YvQtSDQutGA0LjQv9GC0L7Qs9GA0LDRhNC40YfQtdGB0LrQuNC1INCx0LjQsdC70LjQvtGC0LXQutC4LCDQstGA0LXQtNC+0L3QvtGB0L3Ri9C5INC60L7QtCwg0L/QvtGA0LDQt9C40LLRiNC40Lkg0LLQsNGIINGN0LrQt9C10LzQv9C70Y/RgCDQn9CfINC40LvQuCDQu9GO0LHQvtC5INGB0LLRj9C30LDQvdC90YvQuSDRgSDQv9GA0L7Qs9GA0LDQvNC80L3Ri9C8INC+0LHQtdGB0L/QtdGH0LXQvdC40LXQvCBFUk00INGB0LXRgNCy0LjRgS4KYy4g0KLQtdGF0L3QuNGH0LXRgdC60LjQtSDQv9GA0L7QsdC70LXQvNGLINGD0YHRgtGA0L7QudGB0YLQsiDQn9C+0LvRjNC30L7QstCw0YLQtdC70Y8g0LjQu9C4INC70Y7QsdC+0LPQviDRgdCy0Y/Qt9Cw0L3QvdC+0LPQviDQv9GA0L7Qs9GA0LDQvNC80L3Ri9C8INC+0LHQtdGB0L/QtdGH0LXQvdC40LXQvCBFUk00INGB0LXRgNCy0LjRgdCwINC40LvQuCDRgdCw0LzQvtCz0L4g0L/RgNC+0LPRgNCw0LzQvNC90L7Qs9C+INC+0LHQtdGB0L/QtdGH0LXQvdC40Y8gRVJNNCwg0L3QsNC/0YDQuNC80LXRgCwg0LLQstC40LTRgyDQv9C+0YLQtdGA0Lgg0LTQsNC90L3Ri9GFINC40Lct0LfQsCDQvdC10LjRgdC/0YDQsNCy0L3QvtCz0L4g0LjQu9C4INC/0L7QstGA0LXQttC00LXQvdC90L7Qs9C+INGD0YHRgtGA0L7QudGB0YLQstCwINGF0YDQsNC90LXQvdC40Y8sINC/0L7QstGA0LXQttC00LXQvdC40Y8g0YHQsNC80L7Qs9C+INGD0YHRgtGA0L7QudGB0YLQstCwLCDQsiDRgtC+0Lwg0YfQuNGB0LvQtSDQstC40YDRgtGD0LDQu9GM0L3QvtCz0L47CmQuINCf0YDQvtCx0LvQtdC80Ysg0YEg0LHQtdC30L7Qv9Cw0YHQvdC+0YHRgtGM0Y4sINGBINC60L7RgtC+0YDRi9C80Lgg0LzQvtC20LXRgiDRgdGC0L7Qu9C60L3Rg9GC0YzRgdGPINC70Y7QsdC+0Lkg0L/QvtC70YzQt9C+0LLQsNGC0LXQu9GMINC/0YDQvtCz0YDQsNC80LzQvdC+0LPQviDQvtCx0LXRgdC/0LXRh9C10L3QuNGPIEVSTTQsINC90LDQv9GA0LjQvNC10YAsINGBINC90LXRgdCw0L3QutGG0LjQvtC90LjRgNC+0LLQsNC90L3Ri9C8INC00L7RgdGC0YPQv9C+0Lwg0Log0LrQvtGI0LXQu9GM0LrQsNC8INC/0L7Qu9GM0LfQvtCy0LDRgtC10LvQtdC5INC4L9C40LvQuCDQsNC60LrQsNGD0L3RgtCw0LwsINC30LDQutGA0YvRgtGL0Lwg0LrQu9GO0YfQsNC8INC00L7RgdGC0YPQv9CwLCDQv9Cw0YDQvtC70Y/QvCwg0YHQsNC80L7QvNGDINGD0YHRgtGA0L7QudGB0YLQstGDINCf0L7Qu9GM0LfQvtCy0LDRgtC10LvRjy4KZS4g0JTQtdC50YHRgtCy0LjRjyDQuNC70Lgg0LHQtdC30LTQtdC50YHRgtCy0LjQtSDRgtGA0LXRgtGM0LjRhSDQu9C40YYg0Lgv0LjQu9C4INGB0L7QsdGL0YLQuNGPLCDQstC+0LfQtNC10LnRgdGC0LLRg9GO0YnQuNC1INC90LAg0YLRgNC10YLRjNC40YUg0LvQuNGGLCDQvdCw0L/RgNC40LzQtdGALCDQsdCw0L3QutGA0L7RgtGB0YLQstC+INCf0L7Qu9GM0LfQvtCy0LDRgtC10LvQtdC5LCDQsNGC0LDQutC4INC90LAg0LjQvdGE0L7RgNC80LDRhtC40L7QvdC90YPRjiDQsdC10LfQvtC/0LDRgdC90L7RgdGC0Ywg0LjQu9C4INGB0LXRgNCy0LjRgS3Qv9GA0L7QstCw0LnQtNC10YDQvtCyINC4INC80L7RiNC10L3QvdC40YfQtdGB0YLQstC+INGB0L4g0YHRgtC+0YDQvtC90Ysg0YLRgNC10YLRjNC40YUg0LvQuNGGLgpmLiDQmNC90LLQtdGB0YLQuNGG0LjQuCDQsiDRg9GH0ZHRgtC90YvQtSDQtdC00LjQvdC40YbRiywg0YPRh9GR0YIg0LrQvtGC0L7RgNGL0YUg0L/RgNC+0LjQt9Cy0L7QtNC40YLRgdGPINCyINCb0LXRgtC+0L/QuNGB0LgsINGBINC/0L7QvNC+0YnRjNGOINC/0YDQvtCz0YDQsNC80LzQvdC+0LPQviDQvtCx0LXRgdC/0LXRh9C10L3QuNGPIEVSTTQsINC4INC00YDRg9Cz0LjQtSDQtNC10LnRgdGC0LLQuNGPICjQvtC/0LXRgNCw0YbQuNC4KSDRgSDQsNC60YLQuNCy0LDQvNC4INC4INGB0LvRg9C20LHQsNC80LggKNGB0LXRgNCy0LjRgdCw0LzQuCkuINCf0L7Qu9GM0LfQvtCy0LDRgtC10LvRjCDQv9C+0L3QuNC80LDQtdGCINGH0YLQviDRgtCw0LrQuNC1INC40L3QstC10YHRgtC40YbQuNC4INC80L7Qs9GD0YIg0L/RgNC40LLQtdGB0YLQuCDQuiDQtNC10L3QtdC20L3Ri9C8INC/0L7RgtC10YDRj9C8INCyINC60YDQsNGC0LrQvtGB0YDQvtGH0L3QvtC5INC40LvQuCDQtNCw0LbQtSDQtNC+0LvQs9C+0YHRgNC+0YfQvdC+0Lkg0L/QtdGA0YHQv9C10LrRgtC40LLQtS4g0JvQuNGG0LAsINC40YHQv9C+0LvRjNC30YPRjtGJ0LjQtSDQstC+0LfQvNC+0LbQvdC+0YHRgtC4INC/0YDQvtCz0YDQsNC80LzQvdC+0LPQviDQvtCx0LXRgdC/0LXRh9C10L3QuNGPIEVSTTQsINC00L7Qu9C20L3RiyDQv9C+0L3QuNC80LDRgtGMLCDRh9GC0L4g0YbQtdC90Ysg0LzQvtCz0YPRgiDQutC+0LvQtdCx0LDRgtGM0YHRjyDQsiDRiNC40YDQvtC60L7QvCDQtNC40LDQv9Cw0LfQvtC90LUuINCY0L3RhNC+0YDQvNCw0YbQuNGPLCDRgNCw0LfQvNC10YnQsNC10LzQsNGPINCyINCb0LXRgtC+0L/QuNGB0Lgg0YEg0L/QvtC80L7RidGM0Y4g0L/RgNC+0LPRgNCw0LzQvNC90L7Qs9C+INC+0LHQtdGB0L/QtdGH0LXQvdC40Y8gRVJNNCwg0L3QtSDQs9Cw0YDQsNC90YLQuNGA0YPQtdGCLCDRh9GC0L4g0LjQvdCy0LXRgdGC0L7RgNGLINCyINGD0YfRkdGC0L3Ri9C1INC10LTQuNC90LjRhtGLINC90LUg0L/QvtGC0LXRgNGP0Y7RgiDQtNC10L3RjNCz0LguCmcuINCd0LDQu9C+0LPQvtCy0YvQtSDQstC+0L/RgNC+0YHRiywg0L/RgNC40LzQtdC90Y/QtdC80YvQtSDQuiDQtNC10LnRgdGC0LLQuNGP0Lwg0J/QvtC70YzQt9C+0LLQsNGC0LXQu9GPINGBINC/0YDQvtCz0YDQsNC80LzQvdGL0Lwg0L7QsdC10YHQv9C10YfQtdC90LjQtdC8IEVSTTQsINC60L7RgtC+0YDRi9C1INC+0L3QuCDQtNC+0LvQttC90Ysg0LjRgdGH0LjRgdC70Y/RgtGMINC4INC/0LvQsNGC0LjRgtGMINGB0LDQvNC+0YHRgtC+0Y/RgtC10LvRjNC90L4uINCf0YDQsNCy0L7QvtCx0LvQsNC00LDRgtC10LvRjCDQuCDQtNGA0YPQs9C40LUg0J/QvtC70YzQt9C+0LLQsNGC0LXQu9C4INC/0YDQvtCz0YDQsNC80LzQvdC+0LPQviDQvtCx0LXRgdC/0LXRh9C10L3QuNGPINC90LUg0L3QtdGB0YPRgiDQvtGC0LLQtdGC0YHRgtCy0LXQvdC90L7RgdGC0Lgg0LfQsCDQvdCw0LvQvtCz0L7QvtCx0LvQvtC20LXQvdC40LUsINC60L7RgtC+0YDQvtC1INC/0YDQuNC80LXQvdGP0LXRgtGB0Y8g0Log0LTQtdC50YHRgtCy0LjRjyDQvdCwINC00LDQvdC90YvQvNC4INGBINC/0L7QvNC+0YnRjNGOINC/0YDQvtCz0YDQsNC80LzQvdC+0LPQviDQvtCx0LXRgdC/0LXRh9C10L3QuNGPIEVSTTQuCgo0LiDQn9C+0LvRjNC30L7QstCw0YLQtdC70Ywg0YLQsNC60LbQtSDRgdC+0LPQu9Cw0YjQsNC10YLRgdGPLCDRh9GC0L46CmEuINC+0L0g0L/QvtC70L3QvtGB0YLRjNGOINC+0YLQstC10YLRgdGC0LLQtdC90LXQvSDQt9CwINGB0L7RhdGA0LDQvdC90L7RgdGC0Ywg0YHQstC+0LXQs9C+INCX0LDQutGA0YvRgtC+0LPQviDQutC70Y7Rh9CwLCDQsCDRgdC70YPRh9Cw0LUg0YDQsNGB0LrRgNGL0YLQuNGPICjQutC+0LzQv9GA0L7QvNC10YLQsNGG0LjQuCAtINC/0L7Qu9GD0YfQtdC90LjRjyDQtNC+0YHRgtGD0L/QsCDQuiDQvdC10LzRgyDRgtGA0LXRgtGM0LjRhSDQu9C40YYpINGN0YLQvtCz0L4g0LrQu9GO0YfQsCDQn9C+0LvRjNC30L7QstCw0YLQtdC70Ywg0L7QsdGP0LfQsNC9INGD0LLQtdC00L7QvNC40YLRjCDQvtCxINGN0YLQvtC8INCf0YDQsNCy0L7QvtCx0LvQsNC00LDRgtC10LvRjyDQuNC70Lgg0JfQsNCy0LXRgNC40YLQtdC70Y8g0LjQu9C4INGB0LDQvNC+0YHRgtC+0Y/RgtC10LvRjNC90L4g0L/RgNC+0LjQt9Cy0LXRgdGC0Lgg0LTQtdC50YHRgtCy0LjRjyDQv9C+INGB0L3Rj9GC0LjRjiDRg9C00L7RgdGC0L7QstC10YDQtdC90LjRjyDRgSDRjdGC0L7Qs9C+INC60LvRjtGH0LAsINC/0YPRgtGR0Lwg0YHQvtC30LTQsNC90LjRjyDRgdC+0L7RgtCy0LXRgtGB0YLQstGD0Y7RidC10Lkg0LfQsNC/0LjRgdC4INC4INC+0YLRgdGL0LvQutC1INC10ZEg0LIg0JvQtdGC0L7Qv9C40YHRjC4g0J7QvSDRgdCw0LzQvtGB0YLQvtGP0YLQtdC70YzQvdC+INCy0LXQtNC10YIg0LTQtdGP0YLQtdC70YzQvdC+0YHRgtGMINC/0L4g0L7RhdGA0LDQvdC1INC4INC30LDRidC40YLQtSDRgdCy0L7QuNGFINCX0LDQutGA0YvRgtGL0YUg0LrQu9GO0YfQtdC5LCDQuCDQvdC40LrRgtC+INC60YDQvtC80LUg0L3QtdCz0L4g0YHQsNC80L7Qs9C+INC90LUg0L7RgtCy0LXRh9Cw0LXRgiDQt9CwINC40YUg0YHQvtGF0YDQsNC90L3QvtGB0YLRjCDQuCDQt9Cw0YnQuNGC0YMsINCwINGD0YLQtdGA0Y/QvdC90YvQuSDQl9Cw0LrRgNGL0YLRi9C5INC60LvRjtGHINC90LUg0LzQvtC20LXRgiDQsdGL0YLRjCDQvdC40LrQtdC8INCy0L7RgdGB0YLQsNC90L7QstC70LXQvS4gCmIuINC+0LHQu9Cw0LTQsNC90LjQtSAo0YDQsNCy0L3QviDQutCw0Log0Lgg0L/QvtGC0LXRgNGPINC+0LHQu9Cw0LTQsNC90LjRjykg0LjQvCDQl9Cw0LrRgNGL0YLRi9GFINC60LvRjtGH0LXQuSDRg9C00L7RgdGC0L7QstC10YDRj9C10YLRgdGPINCX0LDQstC10YDQuNGC0LXQu9C10Lwg0YHQvtC+0YLQstC10YLRgdGC0LLRg9GO0YnQuNC8INCy0LjQtNC+0Lwg0LfQsNC/0LjRgdC10LksIC0g0KPQtNC+0YHRgtC+0LLQtdGA0Y/RjtGJ0LDRjyDQt9Cw0L/QuNGB0YwsINC/0YPRgtGR0Lwg0YPRgdGC0LDQvdC+0LLQu9C10L3QuNGPINCyINGC0LDQutC+0Lkg0LfQsNC/0LjRgdC4INGB0L7QvtGC0LLQtdGC0YHRgtCy0LjRjyDQvNC10LbQtNGDINC90LjQvCDQuCDQv9GA0L7QstC10YDQvtGH0L3Ri9C8INC60LvRjtGH0L7QvCwg0YHQvtC+0YLQstC10YLRgdGC0LLRg9GO0YnQuNC8INC00LDQvdC90L7QvNGDINC30LDQutGA0YvRgtC+0LzRgyDQutC70Y7Rh9GDLCDQutC+0YLQvtGA0YvQvCDQvtC9INC+0LHQu9Cw0LTQsNC10YIg0LjQu9C4INC60L7RgtC+0YDRi9C5INC+0L0g0YPRgtC10YDRj9C7LgpjLiDQvtC9INC/0YDQvtC40LfQstC+0LTQuNGCINC+0L/Qu9Cw0YLRgyDQt9CwINGD0YHQu9GD0LPRgyDQstC90LXRgdC10L3QuNGPINCX0LDQv9C40YHQtdC5INCyINCb0LXRgtC+0L/QuNGB0Ywg0KDQsNCx0L7Rh9C40LzQuCDQtdC00LjQvdC40YbQsNC80Lgg0L/QviDQt9Cw0LTQsNC90L3QvtC80YMg0L/QvtGA0Y/QtNC60YMgKNCw0LvQs9C+0YDQuNGC0LzRgykg0L/RgNC+0LPRgNCw0LzQvNC90L7Qs9C+INC+0LHQtdGB0L/QtdGH0LXQvdC40Y8gRVJNNCDQuCDQvNC+0LbQtdGCINGD0LrQsNC30LDRgtGMINGD0YDQvtCy0LXQvdGMINC+0L/Qu9Cw0YLRizog0L7RgiAwLdCz0L4g0LTQviA2LdCz0L4g0YPRgNC+0LLQvdGPLCDQv9GA0LjRh9C10Lwg0LrQvtC90LrRgNC10YLQvdCw0Y8g0LLQtdC70LjRh9C40L3QsCDQoNCw0LHQvtGH0LjRhSDQtdC00LjQvdC40YYsINCy0LfRj9GC0LDRjyDQutCw0Log0L7Qv9C70LDRgtCwINC30LAg0LLQvdC10YHQtdC90LjQtSDQl9Cw0L/QuNGB0Lgg0LIg0JvQtdGC0L7Qv9C40YHRjCwg0LjRgdGH0LjRgdC70Y/QtdGC0YHRjyDQsNCy0YLQvtC80LDRgtC40YfQtdGB0LrQuCDQuCDRgdC/0LjRgdGL0LLQsNC10YLRgdGPINGB0L4g0KHRh9GR0YLQsCwg0YHQvtC30LTQsNC90L3QvtCz0L4g0LjQtyDQv9GD0LHQu9C40YfQvdC+0LPQviDQutC70Y7Rh9CwLCDRgdC+0L7RgtCy0LXRgtGB0YLQstGD0Y7RidC10LPQviDQt9Cw0LrRgNGL0YLQvtC80YMg0LrQu9GO0YfRgywg0YEg0L/QvtC80L7RidGM0Y4g0LrQvtGC0L7RgNC+0LPQviDRgdC+0LfQtNCw0L3QsCDQrdC70LXQutGC0YDQvtC90L3QsNGPINC/0L7QtNC/0LjRgdGMINC00LvRjyDQtNCw0L3QvdC+0Lkg0JfQsNC/0LjRgdC4LgpkLiDRgdC+0LfQtNCw0L3QvdCw0Y8g0LjQvCDQl9Cw0L/QuNGB0Ywg0Y/QstC70Y/QtdGC0YHRjyDQu9C40YjRjCDQt9Cw0Y/QstC70LXQvdC40LXQvCDQvdCwINCy0L3QtdGB0LXQvdC40LUg0LXRkSDQsiDQm9C10YLQvtC/0LjRgdGMINC4INC90LUg0LzQvtC20LXRgiDQsdGL0YLRjCDQstC90LXRgdC10L3QsCDQsiDQm9C10YLQvtC/0LjRgdGMINCyINC+0LHRj9C30LDRgtC10LvRjNC90L7QvCDQv9C+0YDRj9C00LrQtS4g0KLQsNC60YPRjiDQt9Cw0Y/QstC60YMg0LzQvtCz0YPRgiDRg9C00L7QstC70LXRgtCy0L7RgNC40YLRjCDRgtC+0LvRjNC60L4g0KTQvtGA0LbQtdGA0Ysg0L/QviDRgdCy0L7QtdC80YMg0YPRgdC80L7RgtGA0LXQvdC40Y4sINC/0YDQuCDRg9GB0LvQvtCy0LjQuCDRh9GC0L4g0YMg0L3QtdCz0L4g0LTQvtGB0YLQsNGC0L7Rh9C90L4g0KDQsNCx0L7Rh9C40YUg0LXQtNC40L3QuNGGINC00LvRjyDQvtC/0LvQsNGC0Ysg0LfQsCDRg9GB0LvRg9Cz0YMg0LLQvdC10YHQtdC90LjRjyDQl9Cw0L/QuNGB0Lgg0LIg0JvQtdGC0L7Qv9C40YHRjCDRgdC+0LPQu9Cw0YHQvdC+INC/0YDQsNCy0LjQuyDQv9GA0L7Qs9GA0LDQvNC80L3QvtCz0L4g0L7QsdC10YHQv9C10YfQtdC90LjRjyBFUk00INC4INGN0YLQsCDQt9Cw0L/QuNGB0Ywg0L3QtSDQvdCw0YDRg9GI0LDQtdGCINC/0YDQsNCy0LjQuyDRgNCw0LHQvtGC0Ysg0JvQtdGC0L7Qv9C40YHQuCDQuCDQv9GA0LDQstC40Lsg0L/RgNC+0LPRgNCw0LzQvNC90L7Qs9C+INC+0LHQtdGB0L/QtdGH0LXQvdC40Y8gRVJNNC4KZS4g0L7QvSDQv9GA0LjQt9C90LDQtdGCINC+0YLQvdC+0YjQtdC90LjRjywg0LTQtdC50YHRgtCy0LjRjyDQuCDRg9GH0ZHRgiwg0L/RgNC+0LjQt9Cy0L7QtNC40LzRi9GFINC4INCy0YvRgNCw0LbQtdC90L3Ri9GFINCX0LDQv9C40YHRj9C80Lgg0LIg0JvQtdGC0L7Qv9C40YHQuCwg0YHQvtCz0LvQsNGB0L3QviDQuNGFINGB0L7Qt9C00LDQvdC40Y4sINC40YHQv9C+0LvRjNC30L7QstCw0L3QuNGOINC4INC+0LHRgNCw0LHQvtGC0LrQuCDQsiDQv9GA0L7Qs9GA0LDQvNC80L3QvtC8INC+0LHQtdGB0L/QtdGH0LXQvdC40LggRVJNNCwg0LrQsNC6INGB0LLQvtC4INCz0YDQsNC20LTQsNC90YHQutC+LdC/0YDQsNCy0L7QstGL0LUg0L7RgtC90L7RiNC10L3QuNGPLCDRjtGA0LjQtNC40YfQtdGB0LrQuCDQt9C90LDRh9C40LzRi9C1INC00LXQudGB0YLQstC40Y8g0Lgg0YPRh9GR0YIsINGB0L7QvtGC0LLQtdGC0YHRgtCy0YPRjtGJ0LjQtSDRgdC80YvRgdC70YMg0LjQu9C4INC/0L7RgNGP0LTQutGDINC40YHQv9C+0LvQvdC10L3QuNGPINGN0YLQuNGFINCX0LDQv9C40YHQtdC5LCDQsiDRgtC+0Lwg0YfQuNGB0LvQtSDQutCw0Log0LLRi9C/0YPRgdC6INC4INC+0LHQvtGA0L7RgiDRgtC+0LLQsNGA0L7Qsiwg0YPRgdC70YPQsywg0L7QsdGP0LfQsNGC0LXQu9GM0YHRgtCyINC4INC00YDRg9Cz0LjRhSDQuNC80YPRidC10YHRgtCy0LXQvdC90YvRhSDQv9GA0LDQsiwg0LLRi9C/0YPRgdC6INC4INGD0YfRkdGCINC90LDQt9C90LDRh9C10L3QuNC5LCDQt9Cy0LDQvdC40LksINGA0LDQt9GA0LXRiNC10L3QuNC5ICjRgdC10YDRgtC40YTQuNC60LDRgtC+0LIg0Lgg0LvQuNGG0LXQvdC30LjQuSksINCz0YDQsNC80L7RgiAo0LTQuNC/0LvQvtC80L7QsiksINC+0YbQtdC90L7QuiDQuCDQtNGA0YPQs9C40YUg0YHRgtCw0YLRg9GB0L7Qsiwg0YHQvtC30LTQsNC90LjQtSDQt9Cw0Y/QstC70LXQvdC40LksINC/0L7QtNC/0LjRgdCw0L3QuNC1INC00L7Qs9C+0LLQvtGA0L7Qsiwg0LLQtdC00LXQvdC40LUg0L/QtdGA0LXQv9C40YHQutC4LCDQvtGA0LPQsNC90LjQt9Cw0YbQuNC4INCz0L7Qu9C+0YHQvtCy0LDQvdC40Lkg0Lgg0LTRgNGD0LPQuNGFINC+0YLQvdC+0YjQtdC90LjQuSwg0YPRh9C40YLRi9Cy0LDQtdC80YvRhSDRgSDQv9C+0LzQvtGJ0YzRjiDQv9GA0L7Qs9GA0LDQvNC80L3QvtCz0L4g0L/RgNC+0LTRg9C60YLQsCBFUk00INCyINCb0LXRgtC+0L/QuNGB0LguCmYuINC+0L0g0L/RgNC40LfQvdCw0ZHRgiDQl9Cw0L/QuNGB0Lgg0Lgv0LjQu9C4INGN0LvQtdC60YLRgNC+0L3QvdGL0LUg0LTQvtC60YPQvNC10L3RgtGLLCDQv9C+0LTQv9C40YHQsNC90L3Ri9C1INGD0LTQvtGB0YLQvtCy0LXRgNC10L3QvdC+0Lkg0K3Qu9C10LrRgtGA0L7QvdC90L7QuSDQv9C+0LTQv9C40YHRjNGOINCj0YfQsNGB0YLQvdC40LrQsCDQm9C10YLQvtC/0LjRgdC4INGB0L7Qs9C70LDRgdC90L4g0LjRgdC/0L7Qu9GM0LfRg9C10LzQvtC5INGC0LXRhdC90L7Qu9C+0LPQuNC4INC/0YDQvtCz0YDQsNC80LzQvdC+0LPQviDQvtCx0LXRgdC/0LXRh9C10L3QuNGPIEVSTTQsINC/0L7Qu9C90L7RgdGC0YzRjiDRgNCw0LLQvdC+0LfQvdCw0YfQvdGL0Lwg0LTQvtC60YPQvNC10L3RgtGDINC90LAg0LHRg9C80LDQttC90L7QvCDQvdC+0YHQuNGC0LXQu9C1LCDQv9C+0LTQv9C40YHQsNC90L3QvtC80YMg0YHQvtCx0YHRgtCy0LXQvdC90L7RgNGD0YfQvdC+0Lkg0L/QvtC00L/QuNGB0YzRjiDRjdGC0L7Qs9C+INCj0YfQsNGB0YLQvdC40LrQsCDQm9C10YLQvtC/0LjRgdC4LCDQv9GA0LjRh9GR0Lwg0L/QvtGA0Y/QtNC+0Log0L/RgNC+0LLQtdGA0LrQuCDQrdC70LXQutGC0YDQvtC90L3QvtC5INC/0L7QtNC/0LjRgdC4INCj0YfQsNGB0YLQvdC40LrQsCDQt9Cw0LTQsNGR0YLRgdGPINC/0L7RgNGP0LTQutC+0LwgKNCw0LvQs9C+0YDQuNGC0LzQvtC8KSDQsiDQv9GA0L7Qs9GA0LDQvNC80L3QvtC8INC+0LHQtdGB0L/QtdGH0LXQvdC40LggRVJNNCDQuCDRj9Cy0LvRj9C10YLRgdGPINC+0LHRidC10LjQt9Cy0LXRgdGC0L3Ri9C8INGB0L/QvtGB0L7QsdC+0LwgKNCw0LvQs9C+0YDQuNGC0LzQvtC8KSDQvtGB0YPRidC10YHRgtCy0LvQtdC90LjRjyDQrdC70LXQutGC0YDQvtC90L3QvtC5INC/0L7QtNC/0LjRgdC4LgpnLiDQvtC9INC/0YDQuNC30L3QsNGR0YIg0YHQstC+0Lgg0Y7RgNC40LTQuNGH0LXRgdC60Lgg0LfQvdCw0YfQuNC80YvQvNC4INC00LXQudGB0YLQstC40Y8sINC+0YLQvdC+0YjQtdC90LjRjyDQuNC70Lgg0YPRh9GR0YIsINC/0YDQvtC40LfQstC+0LTQuNC80YvQvNC4INC4INCy0YvRgNCw0LbQtdC90L3Ri9C80Lgg0LIg0KPQtNC+0YHRgtC+0LLQtdGA0LXQvdC90YvRhSDQl9Cw0L/QuNGB0Y/RhSDQm9C10YLQvtC/0LjRgdC4INGB0L7Qt9C00LDQvdC90YvQvNC4INC40LvQuCDQvtCx0YDQsNCx0L7RgtCw0L3QvdGL0LzQuCDRgSDQv9C+0LzQvtGJ0YzRjiDQv9GA0L7Qs9GA0LDQvNC80L3QvtCz0L4g0L7QsdC10YHQv9C10YfQtdC90LjRjyBFUk00LCDQsiDRgtC+0Lwg0YfQuNGB0LvQtSDQsiDRgNC10LfRg9C70YzRgtCw0YLQtSDQuNGB0L/QvtC70YzQt9C+0LLQsNC90LjRjyDQv9GA0L7Qs9GA0LDQvNC80L3QvtCz0L4g0L7QsdC10YHQv9C10YfQtdC90LjRjyBFUk00INC00YDRg9Cz0LjQvNC4INCf0L7Qu9GM0LfQvtCy0LDRgtC10LvRj9C80LguCmguINC+0L0g0YHQsNC80L7RgdGC0L7Rj9GC0LXQu9GM0L3QviDQtNC+0LrQsNC30YvQstCw0LXRgiDRjtGA0LjQtNC40YfQtdGB0LrRg9GOINC30L3QsNGH0LjQvNC+0YHRgtGMINCX0LDQv9C40YHQuCDQuCDQv9GA0LjQvdCw0LTQu9C10LbQvdC+0YHRgtGMINCt0LvQtdC60YLRgNC+0L3QvdC+0Lkg0L/QvtC00L/QuNGB0Lgg0Y3RgtC+0Lkg0JfQsNC/0LjRgdC4INC90LXQutC+0YLQvtGA0L7QvNGDINCf0L7Qu9GM0LfQvtCy0LDRgtC10LvRjiwg0LIg0YHQu9GD0YfQsNC1INC10YHQu9C4INGN0YLQsCDQrdC70LXQutGC0YDQvtC90L3QsNGPINC/0L7QtNC/0LjRgdGMINGP0LLQu9GP0LXRgtGB0Y8g0L3QtSDRg9C00L7RgdGC0L7QstC10YDQtdC90L3QvtC5LgppLiDQtNCw0L3QvdGL0LUg0LIg0LTQtdC50YHRgtCy0LjRgtC10LvRjNC90YvRhSDQt9Cw0L/QuNGB0Y/RhSAo0JfQsNGA0YPQsdC60LDRhSkg0JvQtdGC0L7Qv9C40YHQuCwg0L/RgNC40LfQvdCw0Y7RgtGB0Y8g0LjQvCDQvdC10LjQt9C80LXQvdC90YvQvNC4INC4INC40YHRgtC40L3QvdGL0LzQuCwg0Lgg0LzQvtCz0YPRgiDQsdGL0YLRjCDQv9GA0LXQtNGB0YLQsNCy0LvQtdC90Ysg0LrQsNC6INC40Lwg0YHQsNC80LjQvCwg0YLQsNC6INC4INC00YDRg9Cz0LjQvNC4INCf0L7Qu9GM0LfQvtCy0LDRgtC10LvRj9C80Lgg0JvQtdGC0L7Qv9C40YHQuCDQutCw0Log0LTQvtC60LDQt9Cw0YLQtdC70YzRgdGC0LLQviDRjtGA0LjQtNC40YfQtdGB0LrQuCDQt9C90LDRh9C40LzQvtCz0L4g0LTQtdC50YHRgtCy0LjRjyDQo9GH0LDRgdGC0L3QuNC60LAsINGB0L7Qt9C00LDQstGI0LXQs9C+INGN0YLRgyDQl9Cw0YDRg9Cx0LrRgywg0LIg0YHRg9C00LDRhSDQuNC70Lgg0LjQvdGL0YUg0L7RgNCz0LDQvdCw0YUsINCyINGC0L7QvCDRh9C40YHQu9C1INC60LDQuiDQv9GA0L7RgtC40LIg0L3QtdCz0L4g0Lgg0LXQs9C+INC00LXQudGB0YLQstC40Lkv0LHQtdC30LTQtdC50YHRgtCy0LjQuSDRgtCw0Log0Lgg0LIg0LfQsNGJ0LjRgtGDLgpqLiDQv9C+0LTQv9C40YHQsNC90L3Ri9C1INC10LPQviDQrdC70LXQutGC0YDQvtC90L3QvtC5INC/0L7QtNC/0LjRgdGM0Y4g0LTQsNC90L3Ri9C1INCyINC00LXQudGB0YLQstC40YLQtdC70YzQvdGL0YUg0LfQsNC/0LjRgdGP0YUgKNCX0LDRgNGD0LHQutCw0YUpINCb0LXRgtC+0L/QuNGB0Lgg0LjQvNC10Y7RgiDQsdC+0LvRjNGI0LjQuSDQv9GA0LjQvtGA0LjRgtC10YIg0L3QsNC0INC40L3Ri9C80Lgg0Y7RgNC40LTQuNGH0LXRgdC60LjQvNC4INC00LXQudGB0YLQstC40Y/QvNC4INC4INC00L7QutGD0LzQtdC90YLQsNC80LgsINC+0YTQvtGA0LzQu9C10L3QvdGL0LzQuCDQutCw0Log0LIg0LHRg9C80LDQttC90L7QvCwg0YLQsNC6INC4INC40L3QvtC8ICjRg9GB0YLQvdC+0LwsINGN0LvQtdC60YLRgNC+0L3QvdC+0LwpINCy0LjQtNC1LCDRgtCw0LosINGH0YLQviDQstGB0LUg0L/RgNC+0YLQuNCy0L7RgNC10YfQsNGJ0LjQtSDQtNCw0L3QvdGL0LUg0LjQtyDQvdC40YUg0YHRh9C40YLQsNGO0YLRgdGPINC90LjRh9GC0L7QttC90YvQvNC4INC10YHQu9C4INC+0L3QuCDQuNC80LXRjtGCINCx0L7Qu9C10LUg0L/QvtC30LTQvdC10LUg0LLRgNC10LzRjyDRgdC+0LfQtNCw0L3QuNGPLCDRh9C10Lwg0L/RgNC+0YLQuNCy0L7RgNC10YfQsNGJ0LDRjyDQuNC8INC30LDQv9C40YHRjCDQsiDQm9C10YLQvtC/0LjRgdC4LgprLiDQvtC9INCx0YPQtNC10YIg0YHQstC+0LXQstGA0LXQvNC10L3QvdC+INC/0YDQvtC40LfQstC+0LTQuNGC0Ywg0L7QsdC90L7QstC70LXQvdC40LUg0L/RgNC+0LPRgNCw0LzQvNC90L7Qs9C+INC/0YDQvtC00YPQutGC0LAgRVJNNCDQvdCwINGB0LLQvtC40YUg0LrQvtC80L/RjNGO0YLQtdGA0LDRhSwg0L/QviDQutGA0LDQudC90LXQuSDQvNC10YDQtSDQvdC1INC/0L7Qt9C20LUg0YfQtdC8INGH0LXRgNC10Lcg0L7QtNC40L0g0LzQtdGB0Y/RhiDRgSDQtNCw0YLRiyDQstGL0YXQvtC00LAg0L3QvtCy0L7QuSDQstC10YDRgdC40Lgg0J/Qnywg0LrQvtGC0L7RgNCw0Y8g0LTQvtC70LbQvdCwINCx0YvRgtGMINCy0YvQu9C+0LbQtdC90LAg0J/RgNCw0LLQvtC+0LHQu9Cw0LTQsNGC0LXQu9C10Lwg0J/QnyDQtNC70Y8g0YHQstC+0LHQvtC00L3QvtC5INC30LDQutCw0YfQutC4INGBINGB0LDQudGC0LAgYXJvbmljbGUuY29tLgpsLiDQvtC9INC/0YDQuNC30L3QsNC10YIg0JvQtdGC0L7Qv9C40YHRjCDQutCw0Log0L7QsdGJ0LXQtNC+0YHRgtGD0L/QvdGL0Lkg0LjRgdGC0L7Rh9C90LjQuiDQtNCw0L3QvdGL0YUuCm0uINC+0L0g0L/RgNC10LTQvtGB0YLQsNCy0LvRj9C10YIg0LIg0L7RgtC60YDRi9GC0YvQuSDQvtCx0YnQuNC5INC00L7RgdGC0YPQvyDRgdCy0L7QuCDQu9C40YfQvdGL0LUgKNC/0LXRgNGB0L7QvdCw0LvRjNC90YvQtSkg0LTQsNC90L3Ri9C1LCDRgtCw0LrQuNC1INC60LDQujog0LjQtNC10L3RgtC40YTQuNC60LDRhtC40L7QvdC90YvQuSDQvdC+0LzQtdGAINCz0YDQsNC20LTQsNC90LjQvdCwINGB0YLRgNCw0L3RiyAo0JjQndCdLCDQodCd0JjQm9ChLCDQstGB0LUg0L/QsNGB0L/QvtGA0YLQvdGL0LUg0LTQsNC90L3Ri9C1ICjQvdC+0LzQtdGAINC4INGCLtC0Likg0Lgg0YIu0LQuKSwg0KTQsNC80LjQu9C40Y8sINCY0LzRjywg0J7RgtGH0LXRgdGC0LLQviwg0LTQsNGC0LAg0Lgg0LzQtdGB0YLQviAo0LPQtdC+LdC60L7QvtGA0LTQuNC90LDRgtGLKSDRgNC+0LbQtNC10L3QuNGPLCDQstC90LXRiNC90LjQuSDQstC40LQg0LIg0LLQuNC00LUg0YTQvtGC0L7Qs9GA0LDRhNC40Lgg0LvQuNGG0LAsINGG0LLQtdGC0LAg0LrQvtC20LgsINCz0LvQsNC3INC4INCy0L7Qu9C+0YEsINGA0L7RgdGCLCDQstC10YEg0Lgg0L/QvtC7LCDQsiDRgNC10LfRg9C70YzRgtCw0YLQtSDRh9C10LPQviDQvtC90Lgg0YHRgtCw0L3QvtCy0Y/RgtGB0Y8g0L7QsdGJ0LXQtNC+0YHRgtGD0L/QvdGL0LzQuCDQuCDRgdGH0LjRgtCw0Y7RgtGB0Y8g0YDQsNGB0LrRgNGL0YLRi9C80Lgg0LjQvCDRgdCw0LzQuNC8INC4INC90LUg0L/QvtC00LvQtdC20LDRgiDQt9Cw0YnQuNGC0LUsINC/0YPRgtGR0Lwg0YHQvtC30LTQsNC90LjRjyDQt9Cw0L/QuNGB0Lgg0LIg0JvQtdGC0L7Qv9C40YHQuCDRgdC+0LTQtdGA0LbQsNGJ0LXQuSDRjdGC0L7RgiDQvdCw0LHQvtGAINC00LDQvdC90YvRhSAo0LjQvdGE0L7RgNC80LDRhtC40Y4pINC4INC/0L7QtNC/0LjRgdCw0L3QvdC+0Lkg0LXQs9C+INCt0LvQtdC60YLRgNC+0L3QvdC+0Lkg0L/QvtC00L/QuNGB0YzRji4g0J7QvSDQv9GA0LXQtNC+0YHRgtCw0LLQu9GP0LXRgiDRgtC+0LvRjNC60L4g0LTQtdC50YHRgtCy0LjRgtC10LvRjNC90YvQtSDQu9C40YfQvdGL0LUg0LTQsNC90L3Ri9C1INC4INC90LXRgdC10YIg0LfQsCDQv9GA0LDQstC00LjQstC+0YHRgtGMINC40YUg0L7RgtCy0LXRgtGB0YLQstC10L3QvdC+0YHRgtGMINGB0LDQvNC+0YHRgtC+0Y/RgtC10LvRjNC90L4sINCwINCyINGB0LvRg9GH0LDQtSDQuNGFINCy0LXRgdC+0LzQvtCz0L4g0LjQt9C80LXQvdC10L3QuNGPINC+0LHRj9C30LDQvSDQstC90LXRgdGC0Lgg0LTQvtC/0L7Qu9C90Y/RjtGJ0YPRjiAo0LfQsNC80LXQvdGP0Y7RidGD0Y4pINCX0LDQv9C40YHRjCDQtNC70Y8g0YPRgdGC0YDQsNC90LXQvdC40Y8g0L3QtdGB0L7QvtGC0LLQtdGC0YHRgtCy0LjRjyDRgdCy0L7QuNGFINC70LjRh9C90YvRhSAo0L/QtdGA0YHQvtC90LDQu9GM0L3Ri9C8KSDQtNCw0L3QvdGL0YUg0YEg0LTQsNC90L3Ri9C80Lgg0LIg0JvQtdGC0L7Qv9C40YHQuC4gCm4uINCy0L3QtdGB0LXQvdC90YvQtSDQuNC8INCyINCb0LXRgtC+0L/QuNGB0Ywg0LTQsNC90L3Ri9C1ICjQuNC90YTQvtGA0LzQsNGG0LjRjykg0Y/QstC70Y/RjtGC0YHRjyDQvtCx0YnQtdC00L7RgdGC0YPQv9C90YvQvNC4INC4INC90LUg0L/QvtC00LvQtdC20LjRgiDQt9Cw0YnQuNGC0LUg0L7RgiDQv9GA0L7RgdC80L7RgtGA0LAg0Lgg0LrQvtC/0LjRgNC+0LLQsNC90LjRjywg0L/RgNC40YfRkdC8INGB0YLQsNGC0YPRgSDQvtCx0YnQtdC00L7RgdGC0YPQv9C90L7RgdGC0Lgg0LXRkSDQvdC1INC80L7QttC10YIg0LHRi9GC0Ywg0LjQt9C80LXQvdC10L0sINCwINGB0LDQvNC4INC00LDQvdC90YvQtSDQvdC1INC80L7Qs9GD0YIg0LHRi9GC0Ywg0YPQtNCw0LvQtdC90Ysg0LjQtyDQm9C10YLQvtC/0LjRgdC4LgpvLiDQvdC+0LLRi9C1INC/0YDQsNCy0LjQu9CwINGA0LDQsdC+0YLRiyDQm9C10YLQvtC/0LjRgdC4INGA0LDQt9C80LXRidCw0Y7RgtGB0Y8g0L/Rg9GC0LXQvCDQvtCx0L3QvtCy0LvQtdC90LjRjyDQvdCw0YHRgtC+0Y/RidC10LPQviDQodC+0LPQu9Cw0YjQtdC90LjRjyDQuCDQsiDRgdC70YPRh9Cw0LUg0L/RgNC+0LTQvtC70LbQtdC90LjRjyDQuNGB0L/QvtC70YzQt9C+0LLQsNC90LjRjyDQv9GA0L7Qs9GA0LDQvNC80L3QvtCz0L4g0L7QsdC10YHQv9C10YfQtdC90LjRjyBFUk00INGB0YfQuNGC0LDRjtGC0YHRjyDQv9GA0LjQvdGP0YLRi9C80Lgg0LjQvCDQuCDQvtCx0Y/Qt9Cw0YLQtdC70YzQvdGL0LzQuCDQtNC70Y8g0LjRgdC/0L7Qu9C90LXQvdC40Y8g0LjQvC4KcC4g0L7QvSDQv9GA0LjQt9C90LDRkdGCINC/0L7Qu9C90L7RgdGC0YzRjiDRgNCw0LLQvdC+0LfQvdCw0YfQvdGL0Lwg0LfQsNC60LDQt9C90L7QvNGDINC/0L7Rh9GC0L7QstC+0LzRgyDQvtGC0L/RgNCw0LLQu9C10L3QuNGOINC90LAg0LHRg9C80LDQttC90L7QvCDQvdC+0YHQuNGC0LXQu9C1INCX0LDRgNGD0LHQutGDLCDRgdC+0LfQtNCw0L3QvdGD0Y4g0Lgg0L/QvtC00L/QuNGB0LDQvdC90YPRjiDQo9GH0LDRgdGC0L3QuNC60L7QvC3QvtGC0L/RgNCw0LLQuNGC0LXQu9C10LwsINCyINC60L7RgtC+0YDQvtC5INGB0YfRkdGC0L7QvCDQv9C+0LvRg9GH0LDRgtC10LvRjyDQt9C90LDRh9C40YLRgdGPINGB0YfRkdGCLCDQv9GA0LjQvdCw0LTQu9C10LbQsNGJ0LjQuSDQtdC80YMsINC/0YDQuNGH0LXQvCDQvtC9INC/0YDQuNC30L3QsNGR0YIg0LXRkSDQv9C+0LvRg9GH0LXQvdC40LUg0L/QvtC0INGA0LDRgdC/0LjRgdC60YMg0Lgg0L/RgNC40LfQvdCw0ZHRgiDRh9GC0L4g0L7RgtC60YDRi9C7INC4INC/0YDQvtGH0LXQuyDRjdGC0L4g0L7RgtC/0YDQsNCy0LvQtdC90LjQtSDQv9C+INC/0YDQvtGI0LXRgdGC0LLQuNC4IDMt0YUg0YHRg9GC0L7QuiDQutCw0Log0JfQsNC/0LjRgdGMINGB0YLQsNC70LAg0JfQsNGA0YPQsdC60L7QuSAo0LTQtdC50YHRgtCy0LjRgtC10LvRjNC90L7QuSkuCnEuINC+0L0g0YHQvtC30LTQsNCyINC30LDQv9C40YHRjCDigJzQt9Cw0LLQtdGA0LXQvdC40Y/igJ0sICjQutC+0LQg0LrQvtGC0L7RgNC+0LkgNDApINC4INGD0LrQsNC30LDQsiDQsiDQvdC10Lkg0YHRgdGL0LvQutGDINC90LAg0LTRgNGD0LPRg9GOINCX0LDQv9C40YHRjCDQm9C10YLQvtC/0LjRgdC4LCDQv9GA0LjQt9C90LDQtdGCINC10ZEg0L/QvtC00L/QuNGB0LDQvdC40LUg0YHQstC+0LXQuSDQrdC70LXQutGC0YDQvtC90L3QvtC5INC/0L7QtNC/0LjRgdGM0Y4sINCyINGC0L7QvCDRh9C40YHQu9C1INC60LDQuiDQtdGRINC30LDQstC10YDQtdC90LjQtSwg0YPQtNC+0YHRgtC+0LLQtdGA0LXQvdC40LUg0LjQu9C4INC/0L7QtNGC0LLQtdGA0LbQtNC10L3QuNC1INC00LXQudGB0YLQstC40Y8sINC30LDQv9C10YfQsNGC0LvQtdC90L3QvtCz0L4g0LIg0JfQsNC/0LjRgdC4LCDQvdCwINC60L7RgtC+0YDRg9GOINGD0LrQsNC30YvQstCw0LXRgiDRgdGB0YvQu9C60LAuCnIuINC+0L0sINC60LDQuiDQl9Cw0LLQtdGA0LjRgtC10LvRjCDRg9C00L7RgdGC0L7QstC10YDQuNCy0YjQuNC5INC00YDRg9Cz0L7Qs9C+INCf0L7Qu9GM0LfQvtCy0LDRgtC10LvRjywg0L3QtdGB0LXRgiDQvtGC0LLQtdGC0YHRgtCy0LXQvdC90L7RgdGC0Ywg0LfQsCDQtNC+0YHRgtC+0LLQtdGA0L3QvtGB0YLRjCDRg9C00L7RgdGC0L7QstC10YDQtdC90L3Ri9GFINC40Lwg0LTQsNC90L3Ri9GFLgpzLiDQvtC9INC90LUg0L3QtdGB0ZHRgiDQvtGC0LLQtdGC0YHRgtCy0LXQvdC90L7RgdGC0Lgg0L/QviDQtNC10LvQsNC8INC00YDRg9Cz0LjRhSDQn9C+0LvRjNC30L7QstCw0YLQtdC70LXQuS4KdC4g0L7QvSDRgdC+0YHRgtC+0Y8g0LIgMTAwINC90LDQuNCx0L7Qu9C10LUg0LHQvtCz0LDRgtGL0YUg0LIg0L7QsdC70LDQtNCw0L3QuNC4INC/0YDQsNCy0L7QstGL0LzQuCDQtdC00LjQvdC40YbQsNC80Lgg0L3QtSDQvNC+0LbQtdGCINGD0LLQtdC70LjRh9C40LLQsNGC0Ywg0LLQtdC70LjRh9C40L3RgyDRgdCy0L7QtdCz0L4g0L7QsdC70LDQtNCw0L3QuNGPINC40LzQuC4g0JAg0LXRgdC70Lgg0YLQsNC60L7QtSDQstGB0ZEg0LbQtSDRgdC70YPRh9Cw0LXRgtGB0Y8sINGC0L4g0L7QsdGP0LfRg9C10YLRgdGPINCyINGC0LXRh9C10L3QuNC4INGB0YPRgtC+0Log0L/QtdGA0LXQtNCw0YLRjCDQsiDRg9C/0YDQsNCy0LvQtdC90LjQtSDRh9Cw0YHRgtGMINGN0YLQuNGFINC10LTQuNC90LjRhiDQtNGA0YPQs9C+0LzRgyDQn9C+0LvRjNC30L7QstCw0YLQtdC70Y4g0YLQsNC6LCDRh9GC0L7QsdGLINCy0LXQu9C40YfQuNC90LAg0YHQstC+0LXQs9C+INC+0LHQu9Cw0LTQsNC90LjRjyDRgdC90LjQt9C40LvQsNGB0Ywg0L/QviDQutGA0LDQudC90LXQuSDQvNC10YDQtSDQtNC+INC/0YDQtdC00YvQtNGD0YnQtdCz0L4g0LfQvdCw0YfQtdC90LjRjywg0LAg0LLQtdC70LjRh9C40L3QsCDQvtCx0LvQsNC00LDQvdC40Y8g0YMg0L/QvtC70YPRh9C40LLRiNC10LPQviDQuNGFINCf0L7Qu9GM0LfQvtCy0LDRgtC10LvRjyDQvdC1INGB0YLQsNC70LAg0LHQvtC70YzRiNC1INC/0L7Qu9C+0LLQuNC90Ysg0YfQtdC8INGDINC/0LXRgNC10LTQsNCy0YjQtdCz0L4uCnUuINC/0YDQsNCy0LAg0L3QsCDQtNC10LnRgdGC0LLQuNGPINCyINCb0LXRgtC+0L/QuNGB0Lgg0LfQsNC00LDRjtGC0YHRjyDRh9C40YHQu9C+0Lwg0L/RgNCw0LLQvtCy0YvRhSDQtdC00LjQvdC40YYsINC90LDRhdC+0LTRj9GJ0LjRhdGB0Y8g0LIg0L7QsdC70LDQtNCw0L3QuNC4INGDINCf0L7Qu9GM0LfQvtCy0LDRgtC10LvRjywg0LLQtdC70LjRh9C40L3RiyDQv9C10YDQstC+0L3QsNGH0LDQu9GM0L3QvtCz0L4g0YDQsNGB0L/RgNC10LTQtdC70LXQvdC40Y8g0LrQvtGC0L7RgNGL0YUg0LfQsNC00LDRjtGC0YHRjyDRgdC+0LPQu9Cw0YHQvdC+INC30LDQv9C40YHRj9C8INCyINC90LDRh9Cw0LvRjNC90L7QvCDQsdC70L7QutC1ICjQs9C10L3QtdGB0LjQty3QsdC70L7QutC1KSDQm9C10YLQvtC/0LjRgdC4LCDQu9C40LHQviDQtNGA0YPQs9C40LzQuCDQv9GA0LDQstC40LvQsNC80LgsINC30LDQtNCw0L3QvdGL0LzQuCDQsiDQv9GA0L7Qs9GA0LDQvNC80L3QvtC8INC+0LHQtdGB0L/QtdGH0LXQvdC40LggRVJNNC4Kdi4g0L7QvSwg0LrRgNC+0LzQtSDQn9GA0LDQstC+0L7QsdC70LDQtNCw0YLQtdC70Y8g0L/RgNC+0LPRgNCw0LzQvNC90L7Qs9C+INC+0LHQtdGB0L/QtdGH0LXQvdC40Y8gRVJNNCwg0L7QsdGP0LfRg9C10YLRgdGPINC90LUg0LjQvNC10YLRjCDRgdC+0LLQvtC60YPQv9C90L4g0LHQvtC70LXQtSDRh9C10LwgMTAlINC/0YDQsNCy0L7QstGL0YUg0LXQtNC40L3QuNGGINCyINC+0LHQu9Cw0LTQsNC90LjQuCAo0YfRgtC+0LHRiyDRgdC+0LHQu9GO0YHRgtC4INC/0YDQuNC90YbQuNC/INC00LXRhtC10L3RgtGA0LDQu9C40LfQsNGG0LjQuCkuINCY0L3QsNGH0LUg0L7QvdC4INC00L7Qu9C20L3RiyDQsdGL0YLRjCDQv9C10YDQtdC00LDQvdGLINC00YDRg9Cz0LjQvCDRg9GH0LDRgdGC0L3QuNC60LDQvCDQsiDRg9C/0YDQsNCy0LvQtdC90LjQtS4KCjUuINCf0L7RgNGP0LTQvtC6INGB0L7Qt9C00LDQvdC40Y8g0L3QvtCy0YvRhSDQn9C+0LvRjNC30L7QstCw0YLQtdC70LXQuSDQuCDQuNGFINGD0LTQvtGB0YLQvtCy0LXRgNC10L3QuNC1CtCU0LvRjyDQstC90LXRgdC10L3QuNGPINGB0LXQsdGPINCyINCb0LXRgtC+0L/QuNGB0Ywg0YEg0L/QvtC80L7RidGM0Y4g0L/RgNC+0LPRgNCw0LzQvNC90L7Qs9C+INC+0LHQtdGB0L/QtdGH0LXQvdC40Y8gRVJNNCDQn9C+0LvRjNC30L7QstCw0YLQtdC70Ywg0LTQvtC70LbQtdC9OgphLiDQodCy0Y/Qt9Cw0YLRjNGB0Y8g0YEg0J/RgNCw0LLQvtC+0LHQu9Cw0LTQsNGC0LXQu9C10Lwg0L/RgNC+0LPRgNCw0LzQvNC90L7Qs9C+INC+0LHQtdGB0L/QtdGH0LXQvdC40Y8gRVJNNCAo0L7RgdC90L7QstC90L7QuSDQl9Cw0LLQtdGA0LjRgtC10LvRjCkg0LjQu9C4INGBINC40L3Ri9C8INCX0LDQstC10YDQuNGC0LXQu9C10LwuCmIuINCf0L7QtNGC0LLQtdGA0LTQuNGC0Ywg0YHQstC+0LUg0L7QsdC70LDQtNCw0L3QuNC1INC/0YDQvtCy0LXRgNC+0YfQvdGL0Lwg0LrQu9GO0YfQvtC8LCDQvtCx0LvQsNC00LDQvdC40LUg0LrQvtGC0L7RgNGL0Lwg0L7QvSDRhdC+0YfQtdGCINGD0LTQvtGB0YLQvtCy0LXRgNC40YLRjCwg0L/Rg9GC0ZHQvCDQv9C10YDQtdCy0L7QtNCwINCx0LXQt9C90LDQu9C40YfQvdGL0YUg0LTQtdC90LXQsyDRgdC+INGB0LLQvtC10LPQviDQu9C40YfQvdC+0LPQviDQsdCw0L3QutC+0LLRgdC60L7Qs9C+INGB0YfRkdGC0LAg0L3QsCDQsdCw0L3QutC+0LLRgdC60LjQuSDRgdGH0ZHRgiDQl9Cw0LLQtdGA0LjRgtC10LvRjyDRgSDRg9C60LDQt9Cw0L3QuNC10Lwg0LIg0L3QsNC30L3QsNGH0LXQvdC40Lgg0L/Qu9Cw0YLQtdC20LAg0YHRgtGA0L7Rh9C60Lgg4oCc0KHQvtCz0LvQsNGB0LXQvSDRgSDQm9C40YbQtdC90LfQuNC10LkgRVJNNCwg0LzQvtC5INC+0YLQutGA0YvRgtGL0Lkg0LrQu9GO0Ycg0J7QotCa0KDQq9Ci0KvQmV/QmtCb0K7Qp+KAnSwgLSDQs9C00LUg0LLQvNC10YHRgtC+INCe0KLQmtCg0KvQotCr0Jlf0JrQm9Cu0Kcg0L/QvtC00YHRgtCw0LLQuNGC0Ywg0YHQstC+0Lkg0L7RgtC60YDRi9GC0YvQuSDQutC70Y7RhyDQsiDQutC+0LTQuNGA0L7QstC60LUgQmFzZTU4LCDQutC+0YLQvtGA0YvQuSDQvNC+0LbQvdC+INCy0LfRj9GC0Ywg0LIg0L/RgNC+0LPRgNCw0LzQvNC90L7QvCDQvtCx0LXRgdC/0LXRh9C10L3QuNC4INCyINC30LDQutC70LDQtNC60LUgwqvQodGH0LXRgtCwwrsg0L/RgNC4INC90LDQttCw0YLQuNC4INC/0YDQsNCy0L7QuSDQutC90L7Qv9C60Lgg0LzRi9GI0Lgg0L3QsCDRgdCy0L7RkdC8INGB0YfQtdGC0LUuINCe0L/Qu9Cw0YfQtdC90L3QsNGPINGB0YPQvNC80LAg0J7RgdC90L7QstC90L7QvNGDINCX0LDQstC10YDQuNGC0LXQu9GOINC/0YDQuNC90LjQvNCw0LXRgtGB0Y8g0LrQsNC6INC+0L/Qu9Cw0YLQsCDQt9CwINGD0YHQu9GD0LPRgyDRg9C00L7RgdGC0L7QstC10YDQtdC90LjRjyDQn9C+0LvRjNC30L7QstCw0YLQtdC70Y8g0LIg0LXQs9C+INC/0L7Qu9GM0LfRgy4KYy4g0KHQvtC30LTQsNGC0Ywg0JfQsNC/0LjRgdGMINCb0LXRgtC+0L/QuNGB0Lgg0YHQviDRgdCy0L7QuNC80Lgg0LvQuNGH0L3Ri9C80LggKNC/0LXRgNGB0L7QvdCw0LvRjNC90YvQvNC4KSDQtNCw0L3QvdGL0LzQuCAtINC60L7QtCDQt9Cw0L/QuNGB0Lgg0LIg0L/RgNC+0YLQvtC60L7Qu9C1INC/0YDQvtCz0YDQsNC80LzQvdC+0LPQviDQvtCx0LXRgdC/0LXRh9C10L3QuNGPIEVSTTQgMjQsINC/0L7QtNC/0LjRgdCw0YLRjCDQtdGRINCt0LvQtdC60YLRgNC+0L3QvdC+0Lkg0L/QvtC00L/QuNGB0YzRjiwg0YHQvtC+0YLQstC10YLRgdGC0LLRg9GO0YnQtdC5INC+0YLQutGA0YvRgtC+0LzRgyDQutC70Y7Rh9GDLCDQutC+0YLQvtGA0YvQuSDQsdGL0Lsg0YPQutCw0LfQsNC9INCyINC90LDQt9C90LDRh9C10L3QuNC4INCx0LXQt9C90LDQu9C40YfQvdC+0LPQviDQv9C70LDRgtC10LbQsCDQuCDRgdC60L7Qv9C40YDQvtCy0LDRgtGMINCyINCx0YPRhNC10YAg0L7QsdC80LXQvdCwINC60L7QvNC/0YzRjtGC0LXRgNCwINGN0YLRgyDRgdC+0LfQtNCw0L3QvdGD0Y4g0JfQsNC/0LjRgdGMINGBINC/0L7QvNC+0YnRjNGOINC60L7QvNCw0L3QtNGLINC/0YDQvtCz0YDQsNC80LzQvdC+0LPQviDQvtCx0LXRgdC/0LXRh9C10L3QuNGPIEVSTTQg4oCc0KHQutC+0L/QuNGA0L7QstCw0YLRjCDQsiDQsdGD0YTQtdGA4oCdLiDQn9GA0Lgg0Y3RgtC+0Lwg0JfQsNC/0LjRgdGMINCx0YPQtNC10YIg0LfQsNC60L7QtNC40YDQvtCy0LDQvdCwINCyIEJhc2U1OCDQuCDQstC90LXRgdC10L3QsCDQsiDQsdGD0YTQtdGAINC60L7Qv9C40YDQvtCy0LDQvdC40Y8g0LrQvtC80L/RjNGO0YLQtdGA0LAuCmQuINCf0LXRgNC10LTQsNGC0Ywg0YHQvtC30LTQsNC90L3Rg9GOINCX0LDQv9C40YHRjCDQsiDQstC40LTQtSDRgtC10LrRgdGC0LAg0LrQvtC00LjRgNC+0LLQutC4IEJhc2U1OCDQo9GH0LDRgdGC0L3QuNC60YMg0JvQtdGC0L7Qv9C40YHQuCDQtNC70Y8g0LLQvdC10YHQtdC90LjRjyDRgdCy0L7QuNGFINC00LDQvdC90YvRhSDQsiDQm9C10YLQvtC/0LjRgdGMLgplLiDQo9GH0LDRgdGC0L3QuNC60LAsINC/0L7Qu9GD0YfQuNCy0YjQuNC5INGC0LDQutGD0Y4g0LfQsNC/0LjRgdGMINC+0LHRj9C30LDQvSDQv9GA0L7QstC10YDQuNGC0Ywg0LXQtSDQvtGC0LrRgNGL0YLRi9C5INC60LvRjtGHINC90LAg0YHQsNC50YLQtSDQl9Cw0LLQtdGA0LjRgtC10LvRjyDQuNC70Lgg0J/RgNCw0LLQvtC+0LHQu9Cw0LTQsNGC0LXQu9GPINC/0YDQvtCz0YDQsNC80LzQvdC+0LPQviDQvtCx0LXRgdC/0LXRh9C10L3QuNGPIEVSTTQsINC4INGD0LTQvtGB0YLQvtCy0LXRgNC40YLRjNGB0Y8g0YfRgtC+INC/0L4g0LTQsNC90L3Ri9C8INGBINGN0YLQvtCz0L4g0YHQsNC50YLQsCDRgtCw0LrQvtC5INC+0YLQutGA0YvRgtGL0Lkg0LrQu9GO0Ycg0L/RgNC40L3QsNC00LvQtdC20LjRgiAo0YHQvtC+0YLQstC10YLRgdGC0LLRg9C10YIpINC00LDQvdC90L7QvNGDINCf0L7Qu9GM0LfQvtCy0LDRgtC10LvRjiDRgSDRg9C60LDQt9Cw0L3QvdGL0Lwg0KTQmNCeLCDQuCDQv9GA0Lgg0L/QvtC00YLQstC10YDQttC00LXQvdC40Lgg0YLQsNC60L7Qs9C+INGB0L7QvtGC0LLQtdGC0YHRgtCy0LjRjyDQstC70L7QttC40YLRjCDRjdGC0YMg0JfQsNC/0LjRgdGMINCyINGB0LLQvtGOINCX0LDQv9C40YHRjCDQuCDQstC90LXRgdGC0Lgg0LXRkSDQsiDQm9C10YLQvtC/0LjRgdGMINC40LvQuCDQv9C+0LTQsNGC0Ywg0JfQsNGP0LLQutGDINC90LAg0LLQvdC10YHQtdC90LjQtSDRjdGC0L7QuSDQl9Cw0L/QuNGB0Lgg0LIg0JvQtdGC0L7Qv9C40YHRjC4KZi4g0J/QvtGB0LvQtSDQv9C+0LvRg9GH0LXQvdC40Y8g0Y3RgtC+0Lkg0JfQsNC/0LjRgdGM0Y4g0YHRgtCw0YLRg9GB0LAg0K3Qu9C10LrRgtGA0L7QvdC90L7QuSDQv9C+0LTQv9C40YHQuCDigJzQv9C+0LTRgtCy0LXRgNC20LTQtdC90L3QsNGP4oCdINC4INC/0L7Rj9Cy0LvQtdC90LjRjyDQtNCw0L3QvdGL0YUg0L4g0YHQstC+0LXQuSDQv9C10YDRgdC+0L3QtSDQsiDQm9C10YLQvtC/0LjRgdC4INCyINGB0L/QuNGB0LrQtSDQv9C10YDRgdC+0L0g0YEg0L/RgNC40YHQstC+0LXQvdC90YvQvCDQtdGOINC90L7QvNC10YDQvtC8LCDQvdC+0LLRi9C5INCf0L7Qu9GM0LfQvtCy0LDRgtC10LvRjCDQvtCx0Y/Qt9Cw0L0g0LvQuNGH0L3QviDRgdCy0Y/Qt9Cw0YLRjNGB0Y8g0YEg0JfQsNCy0LXRgNC40YLQtdC70LXQvCwg0L/RgNC10LTQvtGB0YLQsNCy0LjRgtGMINGB0LLQvtC4INC70LjRh9C90YvQtSDQtNCw0L3QvdGL0LUsINCy0LrQu9GO0YfQsNGPINC00LDQvdC90YvQtSDQv9Cw0YHQv9C+0YDRgtCwINC40LvQuCDQuNC90L7Qs9C+INC00L7QutGD0LzQtdC90YLQsCDRg9C00L7RgdGC0L7QstC10YDRj9GO0YnQtdCz0L4g0LvQuNGH0L3QvtGB0YLRjCDQv9C+0LvQvdC+0YHRgtGM0Y4sINC4INGB0LLQtdGA0LjRgtGMINC40YUg0YEg0LTQsNC90L3Ri9C80Lgg0L/QtdGA0YHQvtC90Ysg0LIg0YHQv9C40YHQutC1INCb0LXRgtC+0L/QuNGB0Lgg0L/QvtC0INGB0L7QvtGC0LLQtdGC0YHRgtCy0YPRjtGJ0LjQvCDQvdC+0LzQtdGA0L7QvC4KZy4g0JIg0YHQu9GD0YfQsNC1INGB0L7QstC/0LDQtNC10L3QuNGPINC00LDQvdC90YvRhSDQv9C10YDRgdC+0L3RiyDQt9CwINGB0L7QvtGC0LLQtdGC0YHRgtCy0YPRjtGJ0LjQvCDQvdC+0LzQtdGA0L7QvCDQsiDRgdC/0LjRgdC60LUg0JvQtdGC0L7Qv9C40YHQuCwg0LTQsNC90L3Ri9GFINC/0YDQtdC00L7RgdGC0LDQstC70LXQvdC90YvRhSDQn9C+0LvRjNC30L7QstCw0YLQtdC70LXQvCDQl9Cw0LLQtdGA0LjRgtC10LvRjiDQuCDQtNCw0L3QvdGL0YUg0LIg0LHQsNC90LrQvtCy0YHQutC+0Lwg0L/Qu9Cw0YLQtdC20LUg0L7RgiDRjdGC0L7Qs9C+INCf0L7Qu9GM0LfQvtCy0LDRgtC10LvRjywg0JfQsNCy0LXRgNC40YLQtdC70Ywg0L7QsdGP0LfQsNC9INGD0LTQvtGB0YLQvtCy0LXRgNC40YLRjCDRjdGC0L7Qs9C+INCf0L7Qu9GM0LfQvtCy0LDRgtC10LvRjyDQv9GD0YLRkdC8INGB0L7Qt9C00LDQvdC40Y8g0YPQtNC+0YHRgtC+0LLQtdGA0Y/RjtGJ0LXQuSDQl9Cw0L/QuNGB0Lgg0LIg0LrQvtGC0L7RgNC+0Lkg0YPQutCw0LfRi9Cy0LDQtdGC0YHRjyDRgdGB0YvQu9C60LAg0L3QsCDRgdC+0L7RgtCy0LXRgtGB0YLQstGD0Y7RidGD0Y4g0L/QtdGA0YHQvtC90YMg0L/QviDQtdGRINC90L7QvNC10YDRgyDQsiDQm9C10YLQvtC/0LjRgdC4INC4INC+0YLQutGA0YvRgtGL0Lkg0LrQu9GO0YcsINC60L7RgtC+0YDRi9C5INC/0YDQtdC00L7RgdGC0LDQstC40Lsg0Y3RgtC+0YIg0J/QvtC70YzQt9C+0LLQsNGC0LXQu9GMINCyINCx0LDQvdC60L7QstGB0LrQvtC8INCx0LXQt9C90LDQu9C40YfQvdC+0Lwg0L/Qu9Cw0YLQtdC20LUuCmguINCSINGB0LvRg9GH0LDQtSDQvdC10LLQvtC30LzQvtC20L3QvtGB0YLQuCDRgdC+0LLQtdGA0YjQuNGC0Ywg0L/QvtC00YLQstC10YDQttC00LDRjtGJ0LjQuSDQsdCw0L3QutC+0LLRgdC60LjQuSDQsdC10LfQvdCw0LvQuNGH0L3Ri9C5INC/0LvQsNGC0ZHQtiwg0YPQutCw0LfQsNC90L3Ri9C5INCyINC/LmIg0YDQsNC30LTQtdC70LAgNSDQvdCw0YHRgtC+0Y/RidC10LPQviDQodC+0LPQu9Cw0YjQtdC90LjRjywg0J/QvtC70YzQt9C+0LLQsNGC0LXQu9GMINC00L7Qu9C20LXQvSDQv9GA0LXQtNC+0YHRgtCw0LLQuNGC0Ywg0LIg0L/QtdGH0LDRgtC90L7QvCDQstC40LTQtSDQv9C+0LTQv9C40YHQsNC90L3QvtC1INCf0L7Qu9GM0LfQvtCy0LDRgtC10LvQtdC8INGB0L7Qs9C70LDRiNC10L3QuNC1INC+0LEg0LjRgdC/0L7Qu9GM0LfQvtCy0LDQvdC40Lgg0YPRgdC40LvQtdC90L3Ri9GFINC90LXQutCy0LDQu9C40YTQuNGG0LjRgNC+0LLQsNC90L3Ri9GFINGN0LvQtdC60YLRgNC+0L3QvdGL0YUg0L/QvtC00L/QuNGB0LXQuSDQuCDRjdC70LXQutGC0YDQvtC90L3QvtCz0L4g0LLQt9Cw0LjQvNC+0LTQtdC50YHRgtCy0LjRjyDRgSDQn9GA0LDQstC+0L7QsdC70LDQtNCw0YLQtdC70LXQvCDQn9GA0L7Qs9GA0LDQvNC80L3QvtCz0L4g0L7QsdC10YHQv9C10YfQtdC90LjRjyBFUk00INC4INC40L3Ri9C80Lgg0J/QvtC70YzQt9C+0LLQsNGC0LXQu9GP0LzQuCDQv9GA0L7Qs9GA0LDQvNC80L3QvtCz0L4g0L7QsdC10YHQv9C10YfQtdC90LjRjyDQv9GA0Lgg0YHQvtCy0LXRgNGI0LXQvdC40Lgg0LvRjtCx0YvRhSDQtNC10LnRgdGC0LLQuNC5INGBINC40YHQv9C+0LvRjNC30L7QstCw0L3QuNC10Lwg0Y3RgtC40YUg0Y3Qu9C10LrRgtGA0L7QvdC90YvRhSDQutC70Y7Rh9C10Lkg0Lgg0K3Qu9C10LrRgtGA0L7QvdC90YvRhSDQv9C+0LTQv9C40YHQtdC5INC60LDQuiDRgSDQn9GA0LDQstC+0L7QsdC70LDQtNCw0YLQtdC70LXQvCBFUk00LCDRgtCw0Log0Lgg0YEg0LjQvdGL0LzQuCDQn9C+0LvRjNC30L7QstCw0YLQtdC70Y/QvNC4INC/0YDQvtCz0YDQsNC80LzQvdC+0LPQviDQvtCx0LXRgdC/0LXRh9C10L3QuNGPIEVSTTQuCgo2LiDQndC10YIg0LPQsNGA0LDQvdGC0LjQuQrQn9GA0L7Qs9GA0LDQvNC80L3QvtC1INC+0LHQtdGB0L/QtdGH0LXQvdC40LUgRVJNNCDRhNGD0L3QutGG0LjQvtC90LjRgNGD0LXRgiDQv9C+INC/0YDQuNC90YbQuNC/0YMgItC60LDQuiDQtdGB0YLRjCIg0LHQtdC3INC60LDQutC40YUt0LvQuNCx0L4g0LPQsNGA0LDQvdGC0LjQuSDQu9GO0LHQvtCz0L4g0YDQvtC00LAg0LIg0L7RgtC90L7RiNC10L3QuNC4INCf0J8g0Lgv0LjQu9C4INC70Y7QsdC+0LPQviDRgdC+0LTQtdGA0LbQuNC80L7Qs9C+LCDQtNCw0L3QvdGL0YUsINC80LDRgtC10YDQuNCw0LvQvtCyINC4L9C40LvQuCDRg9GB0LvRg9CzLCDQvtC60LDQt9GL0LLQsNC10LzRi9GFINCyINGA0LDQvNC60LDRhSDQn9GA0L7Qs9GA0LDQvNC80L3QvtCz0L4g0L7QsdC10YHQv9C10YfQtdC90LjRjyBFUk00LgoKNy4g0J7Qs9GA0LDQvdC40YfQtdC90LjQtSDQvtGC0LLQtdGC0YHRgtCy0LXQvdC90L7RgdGC0LgK0JXRgdC70Lgg0LjQvdC+0LUg0L3QtSDQv9GA0LXQtNGD0YHQvNC+0YLRgNC10L3QviDQt9Cw0LrQvtC90L7QvCwg0L3QuCDQv9GA0Lgg0LrQsNC60LjRhSDQvtCx0YHRgtC+0Y/RgtC10LvRjNGB0YLQstCw0YUg0J/RgNCw0LLQvtC+0LHQu9Cw0LTQsNGC0LXQu9GMINC4INCf0L7Qu9GM0LfQvtCy0LDRgtC10LvQuCDQn9CfINC90LUg0L3QtdGB0YPRgiDQvtGC0LLQtdGC0YHRgtCy0LXQvdC90L7RgdGC0Lgg0LfQsCDRg9Cx0YvRgtC60Lgg0LvRjtCx0L7Qs9C+INGA0L7QtNCwLCDQstC60LvRjtGH0LDRjywg0L3QviDQvdC1INC+0LPRgNCw0L3QuNGH0LjQstCw0Y/RgdGMLCDRg9GC0YDQsNGC0YMg0LLQvtC30LzQvtC20L3QvtGB0YLQuCDRjdC60YHQv9C70YPQsNGC0LDRhtC40LgsINC/0L7RgtC10YDRjiDQv9GA0LjQsdGL0LvQuCwg0L/QvtGC0LXRgNGOINC00LDQvdC90YvRhSwg0LLRi9GC0LXQutCw0Y7RidC40LUg0LjQu9C4INGB0LLRj9C30LDQvdC90YvQtSDQutCw0LrQuNC8LdC70LjQsdC+INC+0LHRgNCw0LfQvtC8INGBINC40YHQv9C+0LvRjNC30L7QstCw0L3QuNC10Lwg0L/RgNC+0LPRgNCw0LzQvNC90L7Qs9C+INC+0LHQtdGB0L/QtdGH0LXQvdC40Y8gRVJNNC4KCjguINCQ0YDQsdC40YLRgNCw0LYK0J/QvtC70YzQt9C+0LLQsNGC0LXQu9GMINC/0YDQvtCz0YDQsNC80LzQvdC+0LPQviDQvtCx0LXRgdC/0LXRh9C10L3QuNGPIEVSTTQg0YHQvtCz0LvQsNGB0LXQvSDQvdCwINCw0YDQsdC40YLRgNCw0LbQvdC+0LUg0YDQsNGB0YHQvNC+0YLRgNC10L3QuNC1INC70Y7QsdC+0LPQviDRgdC/0L7RgNCwLCDRgdCy0Y/Qt9Cw0L3QvdC+0LPQviDRgSDQv9GA0L7Qs9GA0LDQvNC80L3Ri9C8INC/0YDQvtC00YPQutGC0L7QvCBFUk00LCDQutC+0YLQvtGA0L7QtSDQvtGB0YPRidC10YHRgtCy0LvRj9C10YLRgdGPINC00LvRjyDQuNC90L7RgdGC0YDQsNC90L3Ri9GFINCz0YDQsNC20LTQsNC9INC4INGO0YDQuNC00LjRh9C10YHQutC40YUg0LvQuNGGLCDQvdC1INC/0YDQvtC20LjQstCw0Y7RidC40YUg0L3QsCDRgtC10YDRgNC40YLQvtGA0LjQuCDQoNCkINC40LvQuCDQvdC1INC40LzQtdGO0YnQuNGFINC90LAg0YLQtdGA0YDQuNGC0L7RgNC40Lgg0KDQpCDQv9GA0LXQtNGB0YLQsNCy0LjRgtC10LvRjNGB0YLQsiDQuNC70Lgg0YTQuNC70LjQsNC70L7QsiDQsiDQnNC10LbQtNGD0L3QsNGA0L7QtNC90L7QvCDQutC+0LzQvNC10YDRh9C10YHQutC+0Lwg0LDRgNCx0LjRgtGA0LDQttC1INCg0KQsINCwINC00LvRjyDQstGB0LXRhSDQvtGB0YLQsNC70YzQvdGL0YUg0LIg0LrQvtC80L/QtdGC0LXQvdGC0L3Ri9GFINGB0YPQtNCw0YUg0KDQpCwg0LrRgNC+0LzQtSDRgdC/0L7RgNC+0LIsINC+0YLQvdC+0YHRj9GJ0LjRhdGB0Y8g0Log0LDQstGC0L7RgNGB0LrQuNC8INC/0YDQsNCy0LDQvCwg0LvQvtCz0L7RgtC40L/QsNC8LCDRgtC+0LLQsNGA0L3Ri9C8INC30L3QsNC60LDQvCwg0YTQuNGA0LzQtdC90L3Ri9C8INC90LDQuNC80LXQvdC+0LLQsNC90LjRj9C8LCDRgtC+0YDQs9C+0LLRi9C8INGB0LXQutGA0LXRgtCw0Lwg0Lgg0L/QsNGC0LXQvdGC0LDQvC4KCjkuINCS0L3QtdGB0LXQvdC40LUg0LjQt9C80LXQvdC10L3QuNC5INCyINC90LDRgdGC0L7Rj9GJ0LXQtSDQodC+0LPQu9Cw0YjQtdC90LjQtQrQn9GA0LDQstC+0L7QsdC70LDQtNCw0YLQtdC70Ywg0LjQvNC10LXRgiDQv9GA0LDQstC+INCy0L3QtdGB0YLQuCDQsiDQu9GO0LHQvtC1INCy0YDQtdC80Y8g0LvRjtCx0YvQtSDQuNC30LzQtdC90LXQvdC40Y8g0LIg0L3QsNGB0YLQvtGP0YnQtdC1INCh0L7Qs9C70LDRiNC10L3QuNC1LiDQlNCw0L3QvdGL0LUg0LjQt9C80LXQvdC10L3QuNGPINGP0LLQu9GP0Y7RgtGB0Y8g0LLRgdGC0YPQv9C40LLRiNC40LzQuCDQsiDRgdC40LvRgyDQsiDQvNC+0LzQtdC90YIg0LjRhSDQvtCx0L3QvtCy0LvQtdC90LjRjy4g0J/RgNC+0LTQvtC70LbQtdC90LjQtSDQuNGB0L/QvtC70YzQt9C+0LLQsNC90LjRjyDQv9GA0L7Qs9GA0LDQvNC80L3QvtCz0L4g0L7QsdC10YHQv9C10YfQtdC90LjRjyBFUk00INGP0LLQu9GP0LXRgtGB0Y8g0YHQvtCz0LvQsNGB0LjQtdC8INGBINC90L7QstGL0LzQuCDRg9GB0LvQvtCy0LjRj9C80Lgg0L3QsNGB0YLQvtGP0YnQtdCz0L4g0KHQvtCz0LvQsNGI0LXQvdC40Y8uCgoxMC4g0JDQstGC0L7RgNGB0LrQuNC1INCf0YDQsNCy0LAg0Lgg0LfQsNC40LzRgdGC0LLQvtCy0LDQvdC40LUg0LTQsNC90L3QvtC5INCb0LjRhtC10L3Qt9C40LgK0JTQsNC90L3QvtC1INCh0L7Qs9C70LDRiNC10L3QuNC1INGP0LLQu9GP0LXRgtGB0Y8g0LDQstGC0L7RgNGB0LrQuNC8INC/0YDQvtC40LfQstC10LTQtdC90LjQtdC8LCDQstGB0LUg0LDQstGC0L7RgNGB0LrQuNC1INC/0YDQsNCy0LAg0L/RgNC40L3QsNC00LvQtdC20LDRgiDQldGA0LzQvtC70LDQtdCy0YMg0JTQvNC40YLRgNC40Y4g0KHQtdGA0LPQtdC10LLQuNGH0YMsINC4INC80L7QttC10YIg0LHRi9GC0Ywg0LfQsNC40LzRgdGC0LLQvtCy0LDQvdC+INC00YDRg9Cz0LjQvNC4INC/0YDQsNCy0L7QvtCx0LvQsNC00LDRgtC10LvRj9C80Lgg0LIg0LrQsNGH0LXRgdGC0LLQtSDQvtGB0L3QvtCy0Ysg0LTQu9GPINC70LjRhtC10L3Qt9C40Lgg0L3QsCDQtNGA0YPQs9C40LUg0L/QvtC00L7QsdC90YvQtSDQv9GA0L7Qs9GA0LDQvNC80L3Ri9C1INC/0YDQvtC00YPQutGC0Ysg0L/RgNC4INGD0YHQu9C+0LLQuNC4INC/0YDQuNGB0YPRgtGB0YLQstC40Y8g0LIg0L3QsNC30LLQsNC90LjQuCDQu9C40YbQtdC90LfQuNC4INGB0LvQvtCy0L7RgdC+0YfQtdGC0LDQvdC40Y8gwqvQu9C40YbQtdC90LfQuNGPIEFyb25pY2xlwrssINC90LDQv9GA0LjQvNC10YAsINGC0LDQuiDCq9Cb0LjRhtC10L3Qt9C40Y8gQXJvbmljbGUg0L3QsCDQv9GA0L7Qs9GA0LDQvNC80L3Ri9C5INC/0YDQvtC00YPQutGCIE15U29mdCDQvtGCINC/0YDQsNCy0L7QvtCx0LvQsNC00LDRgtC10LvRjyDQmNC80Y/RgNC10LrCuy4K"), Transaction.FOR_NETWORK));
                } catch (Exception e) {
                }
            } else
                transactions.add(new GenesisIssueTemplateRecord(makeTemplate(i)));

        ///// STATUSES
        for (int i = 1; i <= StatusCls.MEMBER_KEY; i++)
            transactions.add(new GenesisIssueStatusRecord(makeStatus(i)));

        if (BlockChain.TEST_MODE) {
            for (String name : BlockChain.NOVA_ASSETS.keySet()) {
                AssetVenture asset = new AssetVenture((byte) 0, itemAppData, creator, name,
                        null, null, "", AS_INSIDE_ASSETS, 8, 0L);
                transactions.add(new GenesisIssueAssetTransaction(asset));
            }
        }

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

    public String getTestNetInfo() {
        return this.testnetInfo;
    }

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
        byte[] flagsBytes = Bytes.ensureCapacity(genesisReference, Crypto.SIGNATURE_LENGTH, 0);
        data = Bytes.concat(data, flagsBytes);

        //WRITE TIMESTAMP
        byte[] genesisTimestampBytes = Longs.toByteArray(this.genesisTimestamp);
        genesisTimestampBytes = Bytes.ensureCapacity(genesisTimestampBytes, 8, 0);
        data = Bytes.concat(data, genesisTimestampBytes);

        if (BlockChain.CLONE_MODE) {
            //WRITE SIDE SETTINGS
            byte[] genesisjsonCloneBytes = this.sideSettingString.getBytes(StandardCharsets.UTF_8);
            data = Bytes.concat(data, genesisjsonCloneBytes);
        }

        //DIGEST [32]
        byte[] digest = Crypto.getInstance().digest(data);

        //DIGEST + transactionsHash = byte[64]
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

    @Override
    public void assetsFeeProcess(DCSet dcSet, boolean asOrphan) {
    }

    public void process(DCSet dcSet, boolean notLog) throws Exception {

        this.target = BlockChain.BASE_TARGET;

        this.blockHead = new BlockHead(this);

        super.process(dcSet, notLog);

    }

    public void orphan(DCSet dcSet) throws Exception {

        if (false)
            dcSet.getItemAssetMap().remove(AssetCls.LIA_KEY);

        super.orphan(dcSet);

    }

}
