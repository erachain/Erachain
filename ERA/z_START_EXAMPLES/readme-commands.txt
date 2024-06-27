Start Commands

-nonet
The network is not in use. Node will generate a blocks even when disconnected from the network

-testnet[=GENESIS_TIMESTAMP]
-testnet=[XXXX] - Start in TestNET mode and set Genesis Block Timestamp (in millis).
-testnet - set genesis Timestamp to current Date and -nonet is ON and use 9065 port and clear chain database (SIMPLE_TEST mode). For quick tests.
   !! Current database wil be erased! Please set 'datachainpath=dataTEST' parameter for save current database.
-testnet=demo - set genesis Timestamp = DEMO chain on 9066 port
-testnet=0 - set current NTP genesis Timestamp on port 9065
For start generation of blocks need more than 5 accounts on all nodes or in Your wallet is used '-testnet'.

-cli
start as Command Line Interpretator.

-pass=PASSWORD
start forging

-peers=PEER1,PEER2,...
example: -peers=34.12.211.156,35.99.02.177

-seed=ACCOUNTS_NUMBER:SEED:PASSWORD
if SEED lenght < 30 - It will made new SEED
example for restore: -seed=3:AXR1wqktmgNYVnpR5uYwBh5v6K6kFb2XH1KYjwDroKcy:1
example for auto make wallet keys: -seed=3:new:1

-fpool
Start forging pool
!! Used only with [-pass] parameter
!! Need setup forging pool [address] by settings_fpool.json (see example in settings_fpool-example.json).
!! Need switch ON web server [-web=on -weballowed=*] - for see statistics
See RPC and API fpool commands for control and statistic.
Send in DEBT some forging stake to pool address (setted in settings_pool.json) for start forging. You may confiscate that DEBT (backward) later.

-backup

-rechain
Rebuild the chain database (datachain). The rebuild is autorun if a new version of the database structure is used in the node.
Ignored in SIMPLE_TEST mode.

-nogui
Start without GUI

-web=on
-weballowed=*|IP1,IP2,..
* - all
-webport=PORT

-rpc=on
-rpcpallowed=*|IP1,IP2,..
* - all
-rpcpport=PORT

-nousewallet
Not use secret wallet Keys - speed up - not forging

-nodatawallet
Not use data Wallet - speed up

-opi
Only Protocol Indexing - speed up

-nocalculated
Not store calculated transactions in DB. Make speed up

-hardwork=0
Set 0..10 - use big buffers. Seed up

-cache=off | lru | weak | soft | hard

-dbschain=mapdb (default)| rocksdb | fast
Select DataBase for dataChain. rocksdb - RocksDB or mapdb - MapDB or fast - it is the complex DB for fast speed
MapDB store all data in one huge file.
RocksDB separate data in some files (experimental).
Fast mode - it use MapDB and RockDB for most fastest mode (experimental).

-testdb=[TX_IN_BLOCK]
Start test for DB system. Make blocks with TX_IN_BLOCK count

-datachainpath=[PATH]
Set path for /datachain folder (there is blockchain database) so You may use one datachain for many Erachain application with different wallets.
Exapmle 1:  -datachainpath=rrr - make and use folder 'rrr' in folder of application
Exapmle 2:  -datachainpath=\rrr - make and use folder 'rrr' in root folder of disk

**** Examples
example for Windows:
	>start "erachain" java -jar erachain.jar -pass=1 -seed=3:AXRJwqktmgNYVnpR5uYwBh5v6K6kFb2XH1KYjwDroKcy:1
example for other OS:
	>java -jar erachain.jar -pass=1 -seed=3:AXRJwqktmgNYVnpR5uYwBh5v6K6kFb2XH1KYjwDroKcy:1

*** Pre-set commands
USE startARGS.txt for set arguments for start application. For example for Mac OS.
Examples see in startARGS_example.txt