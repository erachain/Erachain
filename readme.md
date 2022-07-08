# Erachain Blockchain platform

## Erachain Open Source Server

Erachain software is a feature rich blockchain platform with built-in functions and dApps. This is the canonical source
of ErachainBP where all development takes place.

Keep up with the latest news and articles, and find out all about events happening on the
Erachain [website](https://erachain.org/).

## Software stack

ErachainBP is a Java application and supported by all operation systems.

## ⛓ Links

- API - https://gitlab.com/erachain/node-API
- RPC - https://gitlab.com/erachain/node-RPC
- JS SDK - https://gitlab.com/erachain/sdk-js
- PHP SDK - https://gitlab.com/erachain/sdk-php

## Documentation

All documentation can be found on <>.

# 🚀️ Clone and Build (Windows)

## Dependencies

InteliJ IDEA - https://www.jetbrains.com/idea/
Startup Setup - Application and be sure to select the native Java SDK 1.8, you can not use the built-in IDEA! Otherwise
you will get an error when committing the database.  
https://www.oracle.com/technetwork/java/javase/downloads/index-jsp-138363.html#javasejdk

### Clone code:

1. Use InteliJ IDEA - New > Project from Version Control...
2. Choose ERA gitlab
3. After load project - import Gradle project
4. Install Lombok (Settings - Plugins)

### Start Application

1. select Application, set org.erachain.Start
2. Set Working Directory for new Application to \ERA
3. select ERA_main module

### For Build Application

1. Select Gradle build
2. Set Task: build
3. Set Arguments: --exclude-task test

### For use start arguments on Mac or by default

Make in folder file startARGS.txt (see example in z_START_EXAMPLES folder)

The description of the application launch keys is in the file:  
ERA\z_START_EXAMPLES\readme.txt

You can also specify startup keys in startARGS.txt file - see example in startARGS_example.txt

### Set Version

For set version in JAR application set `String versionEra =` in file `build.gradle`.   
For set version in running from IDEA application set `controller.Controller.version`

### Java

For MapDB to work correctly Java 1.8 (vers 8) is required and the project is built and tested with this version.
Otherwise it will cause an error when you commit and close the database:

> DCSet.close:1674 - java.io.IOException: The requested operation cannot be performed on a file with a user opened mapped section

## 👨‍💻 Development (Linux, Mac)

The node can be built and installed wherever Java can run. To build and test this project, you will have to follow these
steps:

<details><summary><b>Show instructions</b></summary>

*1. Setup the environment.*

- Install Java for your platform:

```bash
sudo apt-get update
sudo apt-get install openjdk-8-jre                     # Ubuntu
# or
# brew cask install adoptopenjdk/openjdk/adoptopenjdk8 # Mac
```

- Install SBT (Scala Build Tool)

Please follow the SBT installation instructions depending on your
platform ([Linux](https://www.scala-sbt.org/1.0/docs/Installing-sbt-on-Linux.html)
, [Mac](https://www.scala-sbt.org/1.0/docs/Installing-sbt-on-Mac.html)
, [Windows](https://www.scala-sbt.org/1.0/docs/Installing-sbt-on-Windows.html))

*2. Clone this repo*

```bash
git clone https://gitlab.com/erachain/erachain-public
```

*3. Compile and run tests*

```bash
sbt checkPR
```

*4. Run integration tests (optional)*

- Run one test:

```bash
sbt node-it/testOnly *.TestClassName
# or 
# bash node-it/testOnly full.package.TestClassName
```

*5. Build packages*

```bash
sbt packageAll                   # Mainnet
```

`sbt packageAll` ‌produces only `deb` package along with a fat `jar`.

*6. Install DEB package*

`deb` package is located in target folder. You can replace '*' with actual package name:

```bash
sudo dpkg -i node/target/*.deb
```

# 🔧 Launching a node.

## Windows, Mac and Linux

Run erachain.jar file.

If your node will only used for forging then run it with the keys:
-nodatawallet -pass=[PASSWORD]  
Startup key description in z_START_EXAMPLES\readme.txt

## Local network

If you have turned on searching for nodes in local network and have restarted the node and no local nodes are found, you
have to add them explicitly to peers.json file (or peer-test.json or peers-demo.json or clonePEERS.json)

## Test Network (TestNet)

See readme in /z_START_EXAMPLES

## Demo Network (DemoNet)

see readme in /z_START_EXAMPLES

## Clonechains (for testers)

see readme in /z_GENESIS_EXAMPLES

### Disable journaling on disk system

Disable journaling on NTFS command line: fsutil usn deletejournal /d c:

# 🤝 Contributing

ErachainBP is an open source project and we are very happy to accept community contributions. Please refer
to [Contributing to GitLab page](https://) for more details.

For major changes, please open an issue first to discuss what you would like to change. Please make sure to update tests
as appropriate.

# Getting help

If something isn't clear, feel free to ask by email <support@erachain.org> or [Discord](https://discord.gg/vcDbPHyZ).


# ✨ Appendix: Extra-mining, extra-stacking and extra-inviting

The project has the ability to include different types of earnings for users: `extra-mining`, `extra-stacking`
and `extra-inviting`.

## Extra-mining - a system of rewards for active users

Is concluded in the rewards for the activity of users. Is awarded as a percentage of the balance of an asset or their
combination, if the user has been active for the specified period of time. Can be set up in a wide range. For example,
it is charged only when the transaction is created From an user's account or when transactions come to his account. With
a limited accrual period, etc.

For example - everyone will receive a reward on the AAA token balance at a rate of 5% per month, and the reward is paid
if on this account came the transaction, or from this account left the transaction, but the award can not be less than
0.01 and the period from the last payment of not more than 30 days.

The `extra-mining` settings are in the core.BlockChain module:

+ `ACTION_ROYALTY_START` - if more than 0 then `extra-mining` will start accruing,
+ `ACTION_ROYALTY_PERCENT` - sets the percentage of charge per month in thousandths of a percent. For example, a value
  of 1000 is 1% per month,
+ `ACTION_ROYALTY_MIN` - the minimum amount of payment. If the accumulated accrual is less than that, the payment for
  the given account on the given transaction does not occur,
+ `ACTION_ROYALTY_MAX_DAYS` - maximum accrual period in days. If more days have passed between the points, then this
  value this value for accrual
+ `ACTION_ROYALTY_TO_HOLD_ROYALTY_PERCENT` - percentage which is additionally credited to the program `extra-stacking` (
  see below),
+ `ACTION_ROYALTY_ASSET` - asset for which the account balance is calculated,
+ `ACTION_ROYALTY_PERSONS_ONLY` - to make accruals only for persons. In this case, the total balance of all accounts of
  the person is calculated.

So for example if you set the maximum accrual period in days - 10, then if someone was not active in the blockchain for
say 30 days, then next time he will be paid only for 10 days.

## Extra-staking - a dividend system of user rewards

Concludes in rewards for users' investments. It is given as a percentage on the balance of some asset or their aggregate
of course. In fact, it is a dividend payment for owning a share of some asset. It is possible to set the terms of
payments, minimum and maximum payments, as well as the source of total payments. For example, the source can be a
percentage of the total number of payments on `extra-mining` or `extra-inviting` made during the period of accrual
of `extra-staking`.

The settings of `extra-staking` are in the module core.BlockChain:

+ `HOLD_ROYALTY_PERIOD_DAYS` - how often we accrue in days. If = 0 - do not accrue,
+ `HOLD_ROYALTY_MIN` - the minimum payment - if less, we do not make payments in the given period for this account,
+ `HOLD_ROYALTY_EMITTER` - from which account the awards are issued
+ `HOLD_ROYALTY_ASSET` - account balance

## Extra-inviting - referral system of rewarding users

Is concluded in the rewards for attracting other users. Is charged as a percentage of the commission paid by the invited
user. Has several levels of accounting for invitations, so that awards can be received from your invited invitees, etc.
You can set the maximum depth, the share of payouts at each level, the share of total payouts.

The `extra-inviting` settings are in the core.BlockChain module:

+ `REFERAL_BONUS_FOR_PERSON` - the number of the block from which the invitation reward will work,
+ `FEE_INVITED_DEEP` - what is the maximum accounting level? If = 0 - do not charge,
+ ` BONUS_STOP_PERSON_KEY` - on what number of person to stop payment of referrals. That is, do not pay numbers less
  than specified,
+ ` FEE_INVITED_SHIFT ` - (not used now) - shift 2 for the coefficient for a share of the commission. 1 - 1/2 goes to
  referral. 2 - 1/4th, etc,
+ `BONUS_REFERAL` - maximum reward,
+ `FEE_INVITED_SHIFT_IN_LEVEL` - shift 2 for the factor of the next level. 1 - means half goes to the lower level. 2 -
  means 1/4th only, etc,
+ `BONUS_FOR_PERSON(int height)` - procedure, sets the reward for certifying a person's account,
+ `REFERAL_BONUS_FOR_PERSON(int height)` - procedure, triggers reward for invitations from a given block.

## Extra-tax and extra-burn

You can also set the percentage of commission on the volume of the asset transfer. And the percentage of asset burn on
transfer.

The `extra-staking` settings are in the core.BlockChain module:  
`ASSET_TRANSFER_PERCENTAGE` - list by asset number, value - commission percentage of volume and minimum commission. That
is, if the calculated value of commission will be less, then the specified lowest value will be deducted.

The settings of `extra-burn` are in the module core.BlockChain:  
`ASSET_BURN_PERCENTAGE` - list by asset number, value - the share of burn. If the asset is not specified in this list,
but is set in the list `ASSET_TRANSFER_PERCENTAGE`, then by default half of the commission is burnt.

# Hiring

We are hiring developers, support people, and production engineers all the time. If you're interested, please write to
us <info@erachain.org>

# 📝 Licence

ErachainBP server is licensed under the aGPL v3.

# Copyright

(c) 2018-2022 Erachain World PTE LTD