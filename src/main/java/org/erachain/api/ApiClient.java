package org.erachain.api;

import org.erachain.core.item.assets.AssetCls;
import org.erachain.settings.Settings;
import org.erachain.utils.StrJSonFine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public class ApiClient {


    public static final String APICALLKEY = "apicallkey";
    public static final int SELF_CALL = 10;
    private static final String ERA_ABBREV = AssetCls.ERA_ABBREV;
    private static final Logger LOGGER = LoggerFactory.getLogger(ApiClient.class);
    private static List<String> allowedcalls = new CopyOnWriteArrayList<>();
    static String[][] helpStrings =
            {
                    {
                            "GET assets",
                            "See assets API",
                    },
                    {
                            "GET persons",
                            "See persons API",
                    },
                    {
                            "GET polls",
                            "See polls API",
                    },
                    {
                            "GET r_note",
                            "See notes API",
                    },
                    {
                            "GET statuses",
                            "See statuses API",
                            "See statuses API",
                    },
                    {
                            "GET templates",
                            "See templates API",
                    },
                    {
                            "GET r_linkedhashes",
                            "See linked hashes API",
                    },


                    {
                            "GET core",
                            "Returns info of the application.",
                            "JSON"
                    },
                    {
                            "GET core/stop",
                            "Will stop the application. This command might not be able to return a http OK message.",
                            ""
                    },
                    {
                            "GET core/status",
                            "Returns the status of the application.",
                            "0 - No connections. 1 - Synchronizing 2 - OK"
                    },
                    {
                            "GET core/status/forging",
                            "Returns the status of the forging process.",
                            "0 - Forging disabled. 1 - Forging enabled 2 - Forging"
                    },
                    {
                            "GET core/isuptodate",
                            "Shows if the application is synchronized with the network.",
                            ""
                    },
                    {
                            "GET core/settings",
                            "Shows settings.",
                            ""
                    },
                    {
                            "GET core/sync/{toHeight}",
                            "Try synchronize to Height",
                            ""
                    },
                    {
                            "GET core/version",
                            "Returns the version and buildtime of the running client.",
                            ""
                    },
                    {
                            "GET core/monitor?log=true",
                            "Returns the monitor info",
                            ""
                    },
                    {
                            "GET core/monitor/{path}?log=true",
                            "Returns the monitor info. {path} = network_acceptor/network_creator/block_generator",
                            ""
                    },
                    {
                            "GET seed/<length>",
                            "Returns a base58 encoded random seed of 32 bytes. Use the optional parameter length to request a seed of <length> bytes.",
                            ""
                    },
                    {
                            "GET peers",
                            "Returns an array of all the IP's of the peers to which the application is currently connected.",
                            ""
                    },
                    {
                            "POST peers <address>",
                            "Adds address of peer.",
                            "Errors: 123 - invalid network address."
                    },
                    {
                            "GET peers/detail",
                            "Returns an array of all connected peer objects containing each peer's IP, height, version, ping time, onlineTime, findingTime, PingCounter, lastWhite time connection, lastGray time connection and status.",
                            ""
                    },
                    {
                            "GET peers/detail/<ip>",
                            "Returns all available information for peer with given ip.",
                            "Errors: 123 - invalid network address."
                    },
                    {
                            "GET peers/best",
                            "Returns an array of the best known nodes.",
                            ""
                    },
                    {
                            "GET peers/known",
                            "Returns an array of all known peers.",
                            ""
                    },
                    {
                            "GET peers/preset",
                            "Returns an array of all preset peers. From settings.json, peers.json, Internet, -peers=<address[,...]>",
                            ""
                    },
                    {
                            "DELETE peers/known",
                            "Forget all known peers with all statistics.",
                            ""
                    },
                    {
                            "GET record",
                            "HELP.",
                            "Errors: ."
                    },
                    {
                            "GET record/parse?data=Base58",
                            "Parse record from RAWDATA in Base58.",
                            "Errors: Transaction.ERRORS, JSON.ERRORS."
                    },
                    {
                            "GET record/parsetest?data=Base58",
                            "Parse record from RAWDATA without Signature",
                            "Errors: Transaction.ERRORS, JSON.ERRORS."
                    },
                    {
                            "GET record/getraw/<type>/<creator>",
                            "Make record and return as RAWDATA in Base58.",
                            "Parameters: TYPE - type of record, CREATOR - PublicKey in Base58.",
                            "Values - as need for that record.",
                            "Errors: Transaction.ERRORS, JSON.ERRORS."
                    },
                    {
                            "GET record/getraw/<type>/<version>/<creator>/<timestamp>/<feePow>",
                            "Make record and return as RAWDATA in Base58.",
                            "Parameters: TYPE - type of record, VERSION - version of record, CREATOR - PublicKey in Base58, TIMESTAMP - long, feePow - int.",
                            "Values - as need for that record.",
                            "Errors: Transaction.ERRORS, JSON.ERRORS."
                    },
                    {
                            "POST record/broadcast <rawdata>",
                            "Make record from RAWDATA, validate it and broadcast.",
                            "Errors: Transaction.ERRORS, JSON.ERRORS."
                    },
                    {
                            "GET transactions",
                            "Returns an array of your accounts each with their 50 last transactions.",
                            "Errors: 201 - Wallet does not exist."
                    },
                    {
                            "GET transactions/limit/<limit>",
                            "Returns an array of your accounts each with their <limit> last transactions.",
                            "Errors: 201 - Wallet does not exist."
                    },
                    {
                            "GET transactions/address/<address>",
                            "Returns an array of the last 50 transactions of a specific address in your wallet.",
                            "Errors: 102 - Invalid address. 201 - Wallet does not exist. 202 - address does not exist in wallet"
                    },
                    {
                            "GET transactions/address/<address>/limit/<limit>",
                            "Returns an array of the last <limit> transactions of a specific address in your wallet.",
                            "Errors: 102 - Invalid address. 201 - Wallet does not exist. 202 - address does not exist in wallet"
                    },
                    {
                            "GET transactions/signature/<signature>",
                            "Returns the transaction that matches the given signature.",
                            "Errors: 101 - Invalid signature. 311 - Transaction does not exist."
                    },
                    {
                            "GET transactions/seqno/<signature>",
                            "Returns the transaction Seq-No by signature.",
                            "Errors: 101 - Invalid signature. 311 - Transaction does not exist."
                    },
                    {
                            "GET transactions/network",
                            "Returns an array of all the unconfirmed transactions known to the client.",
                            ""
                    },
                    {
                            "GET transactions/unconfirmedof/<address>",
                            "Returns an array of all the unconfirmed transactions of address known to the client.",
                            ""
                    },
                    {
                            "GET transactions/unconfirmedincomes/<address>?type={type}&from={from}&count={count}&descending=true",
                            "Returns an array of all the unconfirmed incoming transactions of address known to the client filterer by Type.",
                            ""
                    },
                    {
                            "POST transactions/scan {\"start\": \"<startBlockSign>, \"blocklimit\":<amountBlocks>, \"transactionlimit\":<amountTransactions>, \"type\":<type>, \"service\":<service>, \"address\":\"<address>\"}",
                            "Returns all the transactions that match the filters. All filters are optional but please limit that amount of transactions or blocks to scan to avoid running into issues. Requests that don't specify a blocklimit <= 360 will be denied to remote users. Return the last block it scanned, the amount of blocks it scanned and the scanned transactions.",
                            "Filters:\nstart - The signature of the starting block. \nblocklimit - The maximum amount of blocks to scan. \ntransactionlimit - The maximum amount of transactions to return.\ntype - Only return transactions with the given type.\nservice - Only return Arbitrary Transactions with the given service.\naddress - Only return transactions where the given address is involved.\nErrors: 1 -Json error. 102 - Invalid address. 101 - Invalid signature. 301 - Block does not exist.",
                    },
                    {
                            "GET transactions/recipient/<address>/limit/<limit>",
                            "Returns an array of the last <limit> transactions with a specific recipient.",
                            ""
                    },
                    {
                            "GET transactions/sender/<address>/limit/<limit>",
                            "Returns an array of the <limit> transactions with a specific sender.",
                            ""
                    },
                    {
                            "GET transactions/address/<address>/type/<type>/limit/<limit>",
                            "Returns an array of the last <limit> transactions of a specific address and type.",
                            ""
                    },
                    {
                            "GET transactions/incoming/{height}",
                            "Returns an array of the transactions of a specific block height.",
                            ""
                    },
                    {
                            "GET transactions/incoming/{height}/{recipient}",
                            "Returns an array of the transactions of a specific block height and recipient.",
                            ""
                    },
                    {
                            "GET transactions/incoming/{height}/{address}/decrypt/{password}",
                            "Returns an array of the transactions of a specific block height and recipient with decryption.",
                            ""
                    },

                    {
                            "POST transactions/find {\"address\":\"<address>\", \"sender\":\"<sender>\", \"recipient\":\"<recipient>\", \"type\":<type>, \"from\":<from>, \"offset\":<offset>, \"limit\":<limit>, \"minHeight\":<minHeight>, \"maxHeight\":<maxHeight>, \"desc\":<true/false>, \"count\":<true/false>}, \"unconfirmed\":<true/false>",
                            "Returns an array of the <limit> transactions from given <offset> with a specific params. Set parameter \"count\" to true to find out the number of transactions. Set parameter \"desc\" to true for reverse order. Parameter \"from\" - from seq-No. \"minHeight\" and \"maxHeight\" means height of blocks. All params are optional, but must be specified at least one address field.",
                            "Errors: 102 - Invalid address."
                    },
                    {
                            "GET transactions/dialog/{address1}/{address2}?from=fromID&offset=0&limit=50&desc=false&unconfirmed=false",
                            "Returns an array of the <limit> transactions from given <offset> for dialod between addres1 and address2. Set parameter \"desc\" to true for reverse order.",
                            "Errors: 102 - Invalid address."
                    },

                    {
                            "GET transactions/datadecrypt/{signature}?password={password}",
                            "Returns decrypted data for transaction.",
                            ""
                    },
                    {
                            "GET blocks/addresses/<limit>",
                            "Returns an array of the LIMIT last blocks generated by your accounts.",
                            "Errors: 201 - Wallet does not exist."
                    },
                    {
                            "GET blocks/address/<address>/<limit>",
                            "Returns an array of the LIMIT last blocks generated by a specific address in your wallet.",
                            "Errors: 102 - Invalid address. 201 - Wallet does not exist. 202 - Address does not exist in wallet."
                    },
                    {
                            "GET blocks/<signature or height>",
                            "Returns the block that matches the given signature or height.",
                    },
                    {
                            "GET blocks/first",
                            "Returns the genesis block.",
                            ""
                    },
                    {
                            "GET blocks/last",
                            "Returns the last valid block.",
                            ""
                    },
                    {
                            "GET blocks/child/<signature>",
                            "Returns the child block of the block that matches the given signature.",
                            "Errors: 101 - Invalid signature. 301 - Block does not exist."
                    },
                    {
                            "GET blocks/generatingbalance",
                            "Calculates the generating balance of the block that will follow the last block.",
                            ""
                    },
                    {
                            "GET blocks/generatingbalance/<signature>",
                            "Calculates the generating balance of the block that will follow the block that matches the signature.",
                            "Errors: 101 - Invalid signature. 301 - Block does not exist."
                    },
                    {
                            "GET blocks/height",
                            "Returns the block height of the last block.",
                            ""
                    },
                    {
                            "GET blocks/headers/<signature>",
                            "Returns the list oof block headers - for test networ response in controller.Controller.onMessage(GET_SIGNATURES_TYPE).",
                            "Errors: 101 - Invalid signature. 301 - Block does not exist."
                    },
                    {
                            "GET blocks/fromheight/<height>",
                            "Returns the block from given height.",
                            "Errors: 301 - Block does not exist."
                    },
                    {
                            "GET blocks/orphanto/<height>",
                            "Orphan to given height.",
                            "Errors: 301 - Block does not exist."
                    },
                    {
                            "GET addresses",
                            "Returns an array of all the addresses in your wallet.",
                            "Errors: 201 - Wallet does not exist."
                    },
                    {
                            "GET addresses/validate/<address>",
                            "Validates the given address. Returns true/false.",
                            ""
                    },
                    {
                            "GET addresses/seed/<address>",
                            "Returns the 32-byte long base58-encoded account seed of the given address.",
                            "Errors: 102 - Invalid address. 201 - Wallet does not exist. 202 - Address does not exist in wallet. 203 - Wallet is locked."
                    },
                    {
                            "GET addresses/private/<address>",
                            "Returns the 64-byte long base58-encoded account private key of the given address.",
                            "Errors: 102 - Invalid address. 201 - Wallet does not exist. 202 - Address does not exist in wallet. 203 - Wallet is locked."
                    },
                    {
                            "GET addresses/publickey/<address>",
                            "Returns the 32-byte long base58-encoded account publickey of the given address.",
                            "Errors: 102 - Invalid address."
                    },
                    {
                            "GET addresses/new",
                            "Generates a new account and returns the newly generated address.",
                            "Errors: 201 - Wallet does not exist. 203 - Wallet is locked."
                    },
                    {
                            "POST addresses/makepairbyseed",
                            "Make a account pair by address seed (32 bytes in Base58) and returns private key and public key. Without inserting it into the wallet.",
                            "Errors: - wrong seed."
                    },
                    {
                            "POST addresses/makepairbyphrase",
                            "Make a account pair by address phrase (any text in UTF8) and returns private key and public key. Without inserting it into the wallet.",
                            "Errors: - wrong seed."
                    },
                    {
                            "POST addresses <addressSeed>",
                            "Imports the given 32-byte long base58-encoded account seed. Returns the address when successfully imported.",
                            "Errors: 103 - Invalid seed. 201 - Wallet does not exist. 203 - Wallet is locked."
                    },
                    {
                            "GET addresses/importaccountseed/<accountseed>",
                            "Imports the given 32-byte long base58-encoded account seed. Returns the address when successfully imported.",
                            "Errors: 103 - Invalid seed. 201 - Wallet does not exist. 203 - Wallet is locked."
                    },
                    {
                            "GET addresses/importprivatekey/<privatekey>",
                            "Imports the given 64-byte long base58-encoded private key (from mobile). Returns the address when successfully imported.",
                            "Errors: 103 - Invalid key. 201 - Wallet does not exist. 203 - Wallet is locked."
                    },
                    {
                            "DELETE addresses/<address>",
                            "Deletes the given address. Returns true/false.",
                            "Errors: 102 - Invalid address. 201 - Wallet does not exist. 203 - Wallet is locked."
                    },
                    {
                            "GET addresses/generatingbalance/<address>",
                            "Return the generating balance of the given address.",
                            ""
                    },
                    {
                            "GET addresses/balance/<address>",
                            "Returns the confirmed balance of the given address.",
                            "Errors: 102 - Invalid address."
                    },
                    {
                            "GET addresses/balance/<address>/<confirmation>",
                            "Calculates the balance of the given address after the given confirmations.",
                            "Errors: 102 - Invalid address."
                    },
                    {
                            "GET addresses/assetbalance/<key>/<address>",
                            "Returns the confirmed balance of the given address for the given asset key.",
                            "Errors: 102 - Invalid address. 601 - Invalid asset id."
                    },
                    {
                            "GET addresses/assetbalanceown/<key>/<address>",
                            "Returns the confirmed balance in OWN of the given address for the given asset key.",
                            "Errors: 102 - Invalid address. 601 - Invalid asset id."
                    },
                    {
                            "GET addresses/assetbalanceincomedown/<key>/<address>",
                            "Returns the confirmed incomed balance in OWN of the given address for the given asset key.",
                            "Errors: 102 - Invalid address. 601 - Invalid asset id."
                    },
                    {
                            "GET addresses/creditors/<address>/<key>",
                            "Returns list of creditors of the given address for the given asset key.",
                            "Errors: 102 - Invalid address. 601 - Invalid asset id."
                    },
                    {
                            "GET addresses/debitors/<address>/<key>",
                            "Returns list of debitors of the given address for the given asset key.",
                            "Errors: 102 - Invalid address. 601 - Invalid asset id."
                    },
                    {
                            "POST addresses/sign/<address> <message>",
                            "Signs the given message using the given address.",
                            "Errors: 102 - Invalid address. 201 - Wallet does not exist. 202 - Address does not exist in wallet. 203 - Wallet is locked."
                    },
                    {
                            "POST addresses/verify/<address> {\"message\":\"<message>\", \"publickey\":\"<publickey>\", \"signature\":\"<signature>\"}",
                            "Verifies if the given message was signed by the given address. Returns true/false.",
                            "Errors: 101 - Invalid signature. 102 - Invalid address. 112 - Invalid public key."
                    },
                    {
                            "GET addresses/assets/<address>",
                            "Returns the list of assets for this address with balances.",
                            "Errors: 102 - Invalid address."
                    },
                    {
                            "GET addresses/lastreference/{address}",
                            "Returns the 64-byte long base58-encoded signature of last transaction where the address is delivered as creator. Or the first incoming transaction. Returns \"false\" if there is no transactions.",
                            "Errors: 102 - Invalid address."
                    },
                    {
                            "GET addresses/lastreference/{address}/unconfirmed",
                            "Returns the 64-byte long base58-encoded signature of last transaction including unconfirmed where the address is delivered as creator. Or the first incoming transaction. Returns \"false\" if there is no transactions.",
                            "Errors: 102 - Invalid address."
                    },
                    {
                            "GET addresses/publickeygetaddress/{publickey}",
                            "Returns address for this publickey.",
                            "Errors: 112 - Invalid public key."
                    },
                    {
                            "GET wallet",
                            "Returns general information about the wallet.",
                            "{\"exists\":true,\"isunlocked\":false}"
                    },
                    {
                            "GET wallet/seed",
                            "Return the 32-byte long base58-encoded wallet seed.",
                            "Errors: 201 - Wallet does not exist. 203 - Wallet is locked."
                    },
                    {
                            "GET wallet/synchronize",
                            "Rescans the blockchain for data.",
                            "Errors: 201 - Wallet does not exist."
                    },
                    {
                            "GET wallet/lock",
                            "Locks the wallet.",
                            "Errors: 201 - Wallet does not exist."
                    },
                    {
                            "POST wallet {\"seed\":\"<seed>\", \"password\":\"<password>\", \"recover\":<false/true>,  \"amount\":<amount>, \"dir\":<wallet dir>} ",
                            "Creates a wallet using the given 32-byte long base58-encoded seed, password,recover flag and amount., Wallet dir",
                            "Errors: 1 - Json error. 103 - Invalid seed. 104 - Invalid amount. 204 - Wallet already exists."
                    },
                    {
                            "POST wallet/unlock <password>",
                            "Unlocks the wallet using the given password. Returns true/false depending on the fact if the password is correct.\n201 - Wallet does not exist.",
                            "Errors: 201 - Wallet does not exist."
                    },

                    {"GET r_send/{creator}/{recipient}?feePow={feePow}&assetKey={assetKey}&amount={amount}&title={title}&message={message}&nottext=true&encrypt=true&password={password}",
                            "Make and broadcast SEND asset amount and mail. If \"asset\" is omitted, 2 is provided (default commission asset"},
                    {"GET r_send/raw/{creator}/{recipient}?feePow={feePow}&assetKey={assetKey}&amount={amount}&title={title}&message={message}&nottext=true&encrypt=true&password={password}",
                            "Make RAW for SEND asset amount and mail. If \"asset\" is omitted, 2 is provided (default commission asset"},
                    {"POST r_send {\"creator\": \"<creator>\", \"recipient\": \"<recipient>\", \"asset\":\"<assetKey>\", \"amount\":\"<amount>\", \"title\": \"<title>\", \"message\": \"<message>\", \"nottext\": <true/false>, \"encrypt\": <true/false>,  \"password\": \"<password>\"}",
                            "Make and broadcast SEND asset amount and mail. If \"asset\" is omitted, 2 is provided (default commission asset"},
                    {"POST r_send/raw {\"creator\": \"<creator>\", \"recipient\": \"<recipient>\", \"asset\":\"<assetKey>\", \"amount\":\"<amount>\", \"title\": \"<title>\", \"message\": \"<message>\", \"nottext\": <true/false>, \"encrypt\": <true/false>,  \"password\": \"<password>\"}",
                            "Make RAW for SEND asset amount and mail. If \"asset\" is omitted, 2 is provided (default commission asset"},
                    {"GET r_send/multisend/{fromAddress}/{assetKey}/{forAssetKey}?position=1&amount=0&test=true&feePow=0&activeafter=[date]&activebefore=[date]&greatequal=[amount]&koeff=1&title=&onlyperson=false&selfpay=false&password=",
                            "Muli-send from Address [fromAddress] the asset [assetKey] by filter: Who has positive balance by asset [forAssetKey] where "
                                    + " position - balance position for test, amount and koeff: sensed AMOUNT = amount + koeff * BALANCE, test - set false for real send or true for statistics, activeafter and activebefore - check activity for address in format: [timestamp_in_sec | YYYY-MM-DD HH:MM],"
                                    + " greatequal=0 - if set balance in position must be great or equal this amount, activeTypeTX=0 - if set test activity on this type transactions,"
                                    + "selfPay=true - if set pay to self address too. Default = true"
                                    + " title=, onlyperson - get only personalized addresses, password="},

                    /*

                    {
                            "(deprecated) GET rec_payment/{feePow}/{sender}/{assetKey}/{amount}/{recipient}?password={password}",
                            "Send a new payment using the given data. Returns the transaction in JSON when successful. If \"asset\" is omitted, 2 is provided (default commission asset).",
                            "Errors: 1 - Json error. 104 - Invalid amount. 105 - Invalid fee. 106 - Invalid sender. 107 - Invalid recipient. 201 - Wallet does not exist. 203 - Wallet is locked."
                    },
                    {
                            "(deprecated) POST rec_payment {\"sender\": \"<sender>\", \"recipient\": \"<recipient>\", \"asset\":\"<assetId>\", \"amount\":\"<amount>\", \"title\": \"<title>\", \"message\": \"<message>\", \"istext\": <true/false>, \"encrypt\": <true/false>,  \"password\": \"<password>\"}",
                            "Send a payment with message using the given data. \"istext\" and \"encrypt\" are optional and default true. Sender and recipient can also be a name.",
                            "Errors: 1 - Json error. 104 - Invalid amount. 105 - Invalid fee. 106 - Invalid sender. 107 - Invalid recipient. 201 - Wallet does not exist. 203 - Wallet is locked."
                    },

                    {
                            "POST namepayment {\"asset\":\"<assetId>\", \"amount\":\"<amount>\", \"fee\":\"<fee>\", \"sender\":\"<senderAddress>\", \"recipient\":\"<recipientName>\"}",
                            "Send a new neme-payment using the given data. If \"asset\" is omitted, 2 is provided (default commission asset).",
                            "Errors: 1 - Json error. 104 - Invalid amount. 105 - Invalid fee. 106 - Invalid sender. 107 - Invalid recipient. 201 - Wallet does not exist. 203 - Wallet is locked. 701 - The name is not registered. 702 -  Names for sale. 703 = Name with trailing or leading spaces."
                    },
                    {
                            "GET names",
                            "Returns an array of all the names owned by your accounts.",
                            ""
                    },
                    {
                            "GET names/address/<address>",
                            "Returns an array of all the names owned by a specific address.",
                            "Errors: 102 - Invalid address."
                    },
                    {
                            "GET names/address/<address>/values",
                            "Returns an array of all the names with values owned by a specific address.",
                            "Errors: 102 - Invalid address."
                    },
                    {
                            "GET names/<name>",
                            "Returns details about the given name",
                            "Errors: 401 - Name does not exist."
                    },
                    {
                            "POST names {\"name\":\"<name>\", \"value\":\"<value>\", \"registrant\":\"registrantAddress\", \"fee\":\"<fee>\"}",
                            "Register a new name. Returns the transaction in JSON when successful.",
                            "Errors: 1 - Json error. 2 - Not enough balance. 102 - Invalid address. 105 - Invalid fee. 108 - Invalid name length. 109 - Invalid value length. 201 - Wallet does not exist. 203 - Wallet is locked. 402 - Name already exists. 404 - Name must be lower case."
                    },
                    {
                            "POST names/<name> {\"newvalue\":\"<newvalue>\", \"newmaker\":\"<newownerAddress>\", \"fee\":\"<fee>\"}",
                            "Updates an existing name. Returns the transaction in JSON when successful.",
                            "Errors: 1 - Json error. 2 - Not enough balance. 102 - Invalid address. 105 - Invalid fee. 108 - Invalid name length. 109 - Invalid value length. 201 - Wallet does not exist. 203 - Wallet is locked. 401 - Name does not exist. 403 - Name already for sale."
                    },
                    {
                            "DELETE names/key/<name> {\"fee\":\"<fee>\", \"key\":\"<key>\"}",
                            "Delete a key from a name.",
                            "Errors: 1 - Json error. 105 - Invalid fee. 201 - Wallet no exists. 203 - Wallet locked. 401 - Name no exists. 110 - Invalid name owner. 119 - Key not exists. 120 - Last key is default key"
                    },
                    {
                            "POST names/key/<name> {\"fee\":\"<fee>\", \"key\":\"<key>\", \"value\":\"<value>\", \"update\":\"<true/false>\"}",
                            "Create or update the value of a key on a name.  \"update\" is optional and defaults to true.",
                            "Errors: 1 - Json error. 117 - Invalid update value. 105 - Invalid fee. 201 - Wallet no exists. 203 - Wallet locked. 401 - Name no exists. 110 - Invalid name owner. 118 - Key already exists."
                    },
                    {
                            "GET namesales",
                            "Returns an array of all the namesales owned by your accounts.",
                            "Errors: 201 - Wallet does not exist."
                    },
                    {
                            "GET namesales/address/<address>",
                            "Returns an array of all the namesales owned by a specific address in your wallet.",
                            "Errors: 102 - Invalid address. 201 - Wallet does not exist. 202 - Address does not exist in wallet."
                    },
                    {
                            "GET namesales/<name>",
                            "Return details about the given name that is for sale.",
                            "Errors: 410 - Name is not for sale."
                    },
                    {
                            "GET namesales network",
                            "Returns an array of all the names that are for sale. For performance this array only contains the keys of the names that are for sale and not the details.",
                            ""
                    },
                    {
                            "POST namesales/<name> {\"amount\":\"<amount>\", \"fee\":\"<fee>\"}",
                            "Used to sell the given name. Returns the transaction in JSON when successful.",
                            "Errors: 1 -Json error. 2 - Not enough balance. 102 - Invalid address. 104 - Invalid amount. 105 - Invalid fee. 108 - Invalid name length. 109 - Invalid name owner. 201 - Wallet does not exist. 203 - Wallet is locked. 401 - Name does not exist. 403 - Name already for sale."
                    },
                    {
                            "DELETE namesales/<name>/<fee>",
                            "Used to cancel the sale of the given name. Returns the transaction in JSON when successful.",
                            "Errors: 1 - Json error. 2 - Not enough balance. 102 - Invalid address. 105 - Invalid fee. 108 - Invalid name length. 110 - Invalid name owner. 201 - Wallet does not exist. 203 - Wallet is locked. 401 - Name does not exist. 410 - Name is not for sale."
                    },
                    {
                            "POST namesales/buy/<name> {\"buyer\":\"<buyerAddress>\", \"fee\":\"<fee>\"}",
                            "Used to purchase the given name. Returns the transaction in JSON when successful.",
                            "Errors: 1 - Json error. 2 - Not enough balance. 102 - Invalid address. 105 - Invalid fee. 108 - Invalid name length. 111 - Invalid buyer. 201 - Wallet does not exist. 203 - Wallet is locked. 401 - Name does not exist. 410 - Name is not for sale. 411 - Buyer is already the owner."
                    },
                    {
                            "POST arbitrarytransactions {\"creator\":\"<creatorAddress>\", \"data\":\"<dataBase58>\", \"service\": <service>, \"fee\":\"<fee>\"}",
                            "Used to send an arbitrary transaction. The data of the arbitrary transaction must be base58 encoded and must be between 1-4000 bytes. Returns the transaction in JSON when successful. Also supports multipayments.",
                            "Errors: 1 - Json error. 2 - Not enough balance. 3 - Not yet released. 102 - Invalid address. 105 - Invalid fee. 115 - Invalid data. 116 - Invalid data length. 201 - Wallet does not exist. 202 - Address does not exist in wallet. 203 - Wallet is locked."
                    },
                    {
                            "GET at",
                            "Returns an array of all the Automated Transactions accounts.",
                            ""
                    },
                    {
                            "GET at/id/<id>",
                            "Return details about AT account with the given assress.",
                            ""
                    },
                    {
                            "GET at/transactions/id/<sender>",
                            "Returns an array of all the AT transactions with the given sender.",
                            ""
                    },
                    {
                            "GET at/creator/<creator>",
                            "Returns an array of all the AT transactions with the given creator.",
                            ""
                    },
                    {
                            "GET at/type/<type>",
                            "Returns an array of AT accounts with the given type.",
                            ""
                    },
                    {
                            "GET at/transactions/recipient/<recipient>",
                            "Returns an array of all the AT transactions with the given recipient.",
                            ""
                    },
                    {
                            "GET at/limit/<limit>",
                            "Returns an array of AT accounts with creationBlock more then given.",
                            ""
                    },
                    {
                            "POST at {\"creator\": \"creatorAddress\", \"name\": \"<name>\", \"description\": \"<description>\", \"type\": \"<type>\", \"tags\": \"<tags>\", \"fee\": \"<fee>\", \"quantity\": \"<quantity>\", \"code\": \"<code>\", \"data\": \"<dataHex>\", \"dpages\": \"<dpages>\", \"cspages\": \"<cspages>\", \"uspages\": \"<uspages>\"}",
                            "Used to create a new AT account. Returns the transaction in JSON when successful.",
                            ""
                    },
                    {
                            "POST calcfee/arbitrarytransactions {\"data\": \"<base58data>\"}",
                            "Calculates recommended fee and the length of the transaction for arbitrary transactions.",
                            ""
                    },
                    {
                            "POST calcfee/namereg {\"name\": \"<name>\", \"value\": \"<value>\"}",
                            "Calculates recommended fee and the length of the transaction for name registration.",
                            ""
                    },
                    {
                            "POST calcfee/nameupdate {\"name\": \"<name>\", \"newvalue\": \"<value>\"}",
                            "Calculates recommended fee and the length of the transaction for update name.",
                            ""
                    },
                    {
                            "POST calcfee/blogpost {\"blogname\":\"<blogname>\", \"author\": \"<author>\", \"title\": \"<title>\", \"body\":\"<body>\"}",
                            "Calculates recommended fee and the length of the transaction for blogpost.",
                            ""
                    },
                    {
                            "POST namestorage/update/<name> {\"removelist\":\"{\\\"<key>\\\":\\\"<value>\\\"}\",\"removecomplete\":\"{\\\"<key>\\\":\\\"\\\"}\",\"addlist\":\"{\\\"<key>\\\":\\\"<value>\\\"}\",\"addcomplete\":\"{\\\"<key>\\\":\\\"<value>\\\"}\",\"add\":\"{\\\"<key>\\\":\\\"<value>\\\"}\"}\"",
                            "To add or edit data in namestorage. Keys in the same command can be many. \"addcomplete\" - replaces current value, \"removecomplete\" - removes current value (complete key from storage), \"addlist\" - add value to a list if not in list seperator \";\", \"removelist\" - remove value from list if value there seperator \";\", \"add\" - add to current value without seperator.",
                            ""
                    },
                    {
                            "GET namestorage/<name>/keys",
                            "Returns an array of keys for name from namestorage.",
                            ""
                    },
                    {
                            "GET namestorage/<name>/list",
                            "Returns an array of keys with values for name from namestorage.",
                            ""
                    },
                    {
                            "GET namestorage/<name>/key/<key>",
                            "Returns an value of namekey-pair from namestorage.",
                            ""
                    },
                    {
                            "POST rec_hashes {\"sender\": \"<sender>\", \"url\": \"<url>\", \"message\": \"<message>\", \"hahses\": \"<hashes>\"}",
                            "Record HASHes. \"url\" link to files. \"message\" short description. \"hashes\" - String of HASHes delimited by \" \".",
                            ""
                    },
                    /////// BLOGS
                    {
                            "POST blogpost/<blogname> {\"fee\": \"<fee>\", \"creator\": \"<creator>\", \"author\": \"<author>\", \"title\": \"<title>\", \"body\": \"<body>\", \"share\": \"<share>\", \"delete\": \"<delete>\"}",
                            "Posts to a blog.  <blogname>, \"author\", \"share\", and \"delete\" are optional.",
                            "Errors: 901 - Body empty. 105 - Invalid fee. 201 - Wallet no exists. 203 - wallet locked. 903 - name not owner. 102 - invalid address. 202 - wallet adddress no exists. 902 - blog disabled."
                    },
                    {
                            "POST blogpost/comment {\"fee\": \"<fee>\", \"creator\": \"<creator>\", \"author\": \"<author>\", \"title\": \"<title>\", \"body\": \"<body>\", \"postid\": \"<signature>\"}",
                            "Leave a comment under the post with the given postid.  \"<title>\", \"author\", are optional.",
                            "Errors: 901 - Body empty. 105 - Invalid fee. 201 - Wallet no exists. 203 - wallet locked. 903 - name not owner. 102 - invalid address. 202 - wallet adddress no exists. 909 - commenting disabled."
                    },
                    {
                            "DELETE blogpost/comment/<signature>",
                            "Deletes the comment with given signature.",
                            "910 - comment not existing. 911 - invalid comment owner. 201 - Wallet no exists. 203 - wallet locked."
                    },
                    {
                            "GET blog",
                            "Equivalent to blog/posts/ERA",
                            ""
                    },
                    {
                            "GET blog/posts/<blogname>",
                            "List posts to a blog by transaction signature. If <blogname> is omitted, ERA is provided.",
                            "Errors: 401 - Name does not exist. 902 - Blog disabled."
                    },
                    {
                            "GET blog/post/<signature>",
                            "Get the content of a blog entry specified by transaction signature.",
                            "Errors: 101- Invalid signature. 311 - Transactions does not exist. 905 - Transaction with this signature contains no entries!"
                    },
                    {
                            "GET blog/entries/<blogname>",
                            "Returns the content of the entries for the blog.  If <blogname> is omitted, ERA is provided.",
                            "Errors: 401 - Name does not exist. 902 - Blog disabled."
                    },
                    {
                            "GET blog/entries/<blogname>/limit/<limit>",
                            "Returns the content of the specified number of blog entries.",
                            "Errors: 401 - Name does not exist. 902 - Blog disabled."
                    },
                    {
                            "GET blog/lastentry/<blogname>",
                            "Returns the content of the last entry of the blog.  If <blogname> is omitted, ERA is provided.",
                            "Errors: 401 - Name does not exist. 902 - Blog disabled. 906 - This blog is empty."
                    },
                    {
                            "POST multipayment {\"sender\":\"<sender>\", \"asset\":<defaultkey>, \"payments\": [{\"recipient\":\"<recipient 1>\", \"amount\": \"<amount 1>\", \"asset\":<key>},{\"recipient\":\"<recipient 2>\", \"amount\": \"<amount 2>\"}]}",
                            "Send a new multipayment using the given data. Returns the transaction in JSON when successful. If \"asset\" is omitted, 0 is provided (default asset: ERA).",
                            "Errors: 1 - Json error. 104 - Invalid amount. 106 - Invalid sender. 107 - Invalid recipient. 201 - Wallet does not exist. 203 - Wallet is locked."
                    },
                     */

                    /// telegrams
                    {
                            "GET telegrams",
                            "Returns an array of telegrams in the last 100 minutes."
                    },
                    {
                            "GET telegrams/address/<address>?filter={filter}",
                            "Returns an array of the telegrams of a specific address.",
                            "Errors: 102 - Invalid address. 201 - Wallet does not exist. 202 - address does not exist in wallet"
                    },
                    {
                            "GET telegrams/timestamp/<timestamp>?filter=<filter>",
                            "Returns an array of last telegrams from given timestamp by filter. Filter is optional parametr",
                            "Errors: 201 - Wallet does not exist."
                    },
                    {
                            "GET telegrams/address/<address>/timestamp/<timestamp>?filter={filter}",
                            "Returns an array of the telegrams of a specific address from given timestamp by head filter. Filter is optional parametr.",
                            "Errors: 102 - Invalid address. 201 - Wallet does not exist. 202 - address does not exist in wallet"
                    },
                    {
                            "GET telegrams/get/<signature>",
                            "Get the telegram that matches the given signature.",
                            "Errors: 101 - Invalid signature. 311 - Telegram does not exist."
                    },
                    {
                            "GET telegrams/send/78JFPWVVAVP3WW7S8HPgSkt24QF2vsGiS5/7C5HJALxTbAhzyhwVZeDCsGqVnSwcdEtqu/2/0.0001/title/<message>/true/false/122",
                            "Send a telegram using the given data. \"istext\" and \"encrypt\" are optional and default true.",
                            ""
                    },
                    {
                        "GET telegrams/send/{sender}/{recipient}?asset={asset}&amount={amount}&title={title}&message={message}&istext=true/false&encrypt=true/false&password={password}",
                        "Send a telegram using the given data. \"istext\" and \"encrypt\" are optional and default true.",
                        ""
                    },
                    {
                            "POST telegrams/send {\"sender\": \"<sender>\", \"recipient\": \"<recipient>\", \"asset\": <assetKey>, \"amount\": \"<amount>\", \"title\": \"<title>\", \"message\": \"<message>\", \"istext\": <true/false>, \"encrypt\": <true/false>, \"password\": \"<password>\"}",
                            "Send a telegram using the given data. \"istext\" and \"encrypt\" are optional and default true.",
                            ""
                    }
                    ,
                    {
                            "POST telegrams/delete {\"list\": [{signature1},{signature2}...]}",
                            "Delete telegram by list of signature"
                    },
                    {
                            "GET telegrams/info",
                            "Info about telegrams"
                    },
                    {
                            "GET trade/create/{creator}/{haveKey}/{wantKey}/{haveAmount}/{wantAmount}?feePow={feePow}&password={password}",
                            "make and broadcast CreateOrder "
                    },
                    {
                            "GET trade/cancel/{creator}/{signature}?password={password}",
                            "Cancel Order by orderID"
                    },
                    {
                            "GET trade/cancelbyid/{creator}/{orderID}?password={password}",
                            "Cancel Order by orderID"
                    },
                    {
                            "GET trade/updatepairs/[days]",
                            "Update pairs stat by trades deep days. May be need after resynchronization"
                    },
                    ///////// FORGING POOL
                    {
                            "GET fpool",
                            "Get FPool info"
                    },
                    {
                            "GET fpool/pending/blocks",
                            "Get pending blocks"
                    },
                    {
                            "GET fpool/pending/withdraws",
                            "Get pending withdraws"
                    },
                    {
                            "GET fpool/withdraw/all",
                            "Try withdraws all balances"
                    },
                    {
                            "GET fpool/settax/<tax>",
                            "set tax"
                    }


            };

    public static boolean isAllowedDebugWindowCall(String uuid) {
        return allowedcalls.contains(uuid);
    }

    /**
     *
     * @param command send command
     * @return result execute command or error
     */
    public String executeCommand(String command) {
        if (command.toLowerCase().equals("help all")) {
            String help = "\n";

            for (String[] helpString : helpStrings) {
                help += helpString[0] + "\n";
                if (helpString.length > 1)
                    help += "\t" + helpString[1] + "\n";
                if (helpString.length > 2)
                    help += "\t" + helpString[2] + "\n";
                if (helpString.length > 3)
                    help += "\t" + helpString[3] + "\n";
            }

            return help;
        }

        if (command.toLowerCase().equals("get persons")) {
            String help = "\n";
            Map map = ItemPersonsResource.help;
            for (Object key : map.keySet()) {
                help += key.toString() + ":\n     " + map.get(key) + "\n";
            }
            return help;

        }

        if (command.toLowerCase().startsWith("help")) {
            command = command.substring(4, command.length()).toLowerCase();
            String[] args = command.split("[ /<>]");

            String help = "";

            if (args.length > 1) {
                boolean found = false;
                for (String[] helpString : helpStrings) {

                    String[] helparray = helpString[0].toLowerCase().split("[ /<>]");

                    boolean notallfound = false;
                    for (String string : args) {
                        if (string.equals("")) {
                            continue;
                        }

                        if (Arrays.asList(helparray).indexOf(string) == -1) {
                            notallfound = true;
                            break;
                        }
                    }

                    if (!notallfound) {
                        help += helpString[0] + "\n";
                        if (helpString.length > 1)
                            help += "\t" + helpString[1] + "\n";
                        if (helpString.length > 2)
                            help += "\t" + helpString[2] + "\n";
                        if (helpString.length > 3)
                            help += "\t" + helpString[3] + "\n";
                        found = true;
                        if (helparray.length == args.length - 1) {
                            break;
                        }
                    }
                }
                if (!found) {
                    help += "Command not found!\n";

                }
            } else {
                help = "\n";
                for (String[] helpString : helpStrings) {
                    help += helpString[0] + "\n";
                    if (helpString.length > 1)
                        help += "\t" + helpString[1] + "\n";
                    if (helpString.length > 2)
                        help += "\t" + helpString[2] + "\n";
                    if (helpString.length > 3)
                        help += "\t" + helpString[3] + "\n";
                }
            }

            help += "\nType \"help all\" for detailed help for all commands. Or type \"help command\" to get detailed help for that command. Type \"clear\" for clear GUI concole.\n";

            return help;
        }

        try {
            //SPLIT
            String[] args = command.split(" ");

            //GET METHOD
            String method = args[0].toUpperCase();

            //GET PATH
            String path;

            //GET CONTENT
            String content = "";
            String vars = "";

            if (method.equals("POST")) {
                path =  args[1];
                content = command.substring((method + " " + path + " ").length());
            } else {
                path = command.substring((method + " ").length());

                // get telegrams/address?erty=132 123&sdf=вва
                int startVars = command.indexOf("?");
                if (startVars > 0) {
                    content = command.substring(startVars + 1);
                    path = command.substring((method + " ").length(), startVars);
                    String[] items = content.split("&");

                    for (String item : items) {
                        String[] value = item.split("=");
                        if (value.length > 1) {
                            vars += URLEncoder.encode(value[0], "UTF-8") + "=" + URLEncoder.encode(value[1], "UTF-8") + "&";
                        } else {
                            vars += URLEncoder.encode(item, "UTF-8") + "&";
                        }
                    }
                    vars = vars.substring(0, vars.length() - 1);

                }
            }

            //URL CANNOT CONTAIN UNICODE CHARACTERS
            String[] paths = path.split("/");
            String path2 = "";
            for (String string : paths) {
                path2 += URLEncoder.encode(string, "UTF-8") + "/";
            }
            path2 = path2.substring(0, path2.length() - 1);

            if (vars.length() > 0) {
                path2 += "?" + vars;
            }

            //CREATE CONNECTION
            URL url = new URL("http://127.0.0.1:" + Settings.getInstance().getRpcPort() + "/" + path2);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            //EXECUTE
            connection.setRequestMethod(method);

            UUID randomUUID = UUID.randomUUID();
            allowedcalls.add(randomUUID.toString());
            connection.setRequestProperty(APICALLKEY, randomUUID.toString());
            if (method.equals("POST")) {
                connection.setDoOutput(true);
                connection.getOutputStream().write(content.getBytes(StandardCharsets.UTF_8));
                connection.getOutputStream().flush();
                connection.getOutputStream().close();
            }

            //READ RESULT
            InputStream stream;
            if (connection.getResponseCode() == 400) {
                stream = connection.getErrorStream();
            } else {
                stream = connection.getInputStream();
            }

            String inputText = "";

            InputStreamReader isReader = new InputStreamReader(stream, "UTF-8");
            BufferedReader bufferedReader = new BufferedReader(isReader);
            String inputLine;
            while ((inputLine = bufferedReader.readLine()) != null)
                inputText += inputLine;
            bufferedReader.close();

            if (inputText.length() > 0) {
                inputText = inputText.trim();
                if (inputText.charAt(0) == '{' || inputText.charAt(0) == '[') {
                    try {
                        return StrJSonFine.convert(inputText);
                    } catch (Exception e) {
                    }
                }
            }
            return inputText;

        } catch (Exception ioe) {
            LOGGER.info(ioe.getMessage());
            return "Invalid command! \n" +
                    ioe.getMessage() + "\n" +
                    "Type help to get a list of commands. ";
        }
    }


}
