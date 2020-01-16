Examples for command files for start with forging and loop restart.

Copy bath-file to root folder for use it.


Start Commands

-testnet[=genesis_timestamp]
Start in TestNET mode and set Genesis Block Timestamp (in millis).

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

-nousewallet
Not use secret wallet Keys - speed up - not forging

-nodatawallet
Not use data Wallet - speed up

-opi
Only Protocol Indexing - speed up

-nocalculated
Not store calculated transactions in DB. Make speed up

-dbschain=rocksdb | mapdb | fast
Select DataBase for dataChain. rocksdb - RocksDB or mapdb - MapDB or fast - it si complex DB for fast speed (default)

example foe Windows:
	start "erachain" java -jar erachain.jar -pass=1 -seed=3:AXRJwqktmgNYVnpR5uYwBh5v6K6kFb2XH1KYjwDroKcy:1