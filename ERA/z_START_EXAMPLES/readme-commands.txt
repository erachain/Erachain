Start Commands

-nonet
No use Network

-testnet[=GENESIS_TIMESTAMP]
-testnet=[XXXX] - Start in TestNET mode and set Genesis Block Timestamp (in millis).
-testnet - set genesis Timestamp = current Date and -nonet is ON and use 9065 port
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

-backup

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

-dbschain=rocksdb | mapdb | fast
Select DataBase for dataChain. rocksdb - RocksDB or mapdb - MapDB or fast - it si complex DB for fast speed (default)

-testdb=[TX_IN_BLOCK]
Start test for DB system. Make blocks with TX_IN_BLOCK count

-datachainpath=[PATH]
Set path for /datachain folder (there is blockchain database) so You may use one datachain for many Erachain application with different wallets.
Exapmle 1:  -datachainpath=rrr - make and use folder 'rrr' in folder of application
Exapmle 2:  -datachainpath=\rrr - make and use folder 'rrr' in root folder of disk

example for Windows:
	>start "erachain" java -jar erachain.jar -pass=1 -seed=3:AXRJwqktmgNYVnpR5uYwBh5v6K6kFb2XH1KYjwDroKcy:1
example for other OS:
	>java -jar erachain.jar -pass=1 -seed=3:AXRJwqktmgNYVnpR5uYwBh5v6K6kFb2XH1KYjwDroKcy:1


USE startARGS.txt for ser arguments for start application. For example for Mac OS