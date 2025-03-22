# Erachain Blockchain platform

## Erachain Open Source Server

Erachain software is a feature rich blockchain platform with built-in functions and dApps. This is the canonical source
of ErachainBP where all development takes place.

Keep up with the latest news and articles, and find out all about events happening on the
Erachain [website](https://erachain.org/).

## Software stack

ErachainBP is a Java application and supported by all operation systems.

## ⛓ Links

- API - https://app.swaggerhub.com/apis-docs/Erachain/era-api/1.0.0-oas3
- RPC - http://datachains.world/static/RPC.html
- JS SDK - https://github.com/erachain/sdk-js
- PHP SDK - https://github.com/erachain/sdk-php

## Documentation

All documentation can be found on [WIKI](https://wiki.erachain.org/ru/home).

### Smart-contracts - DAPP

See folder in source java/org/erachain/dapp

# 🚀️ Clone and Build (Windows)

## Dependencies

InteliJ IDEA - https://www.jetbrains.com/idea/
Startup Setup - Application and be sure to select the native Java SDK 1.8, you can not use the built-in IDEA! Otherwise
you will get an error when committing the database.  
https://www.oracle.com/technetwork/java/javase/downloads/index-jsp-138363.html#javasejdk

### Clone code:

1. Use InteliJ IDEA - New > Project from Version Control...
2. Choose ERA github
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
git clone https://github.com/erachain/Erachain
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
</details>

# 🔧 Launching a node.

## Running an Erachain Node on Linux (Ubuntu 20.04)

Connect to server via SSH.  
Install the necessary packages to run a full node.

``sudo apt install screen``

``sudo apt install unzip``

``sudo apt -y install openjdk-8-jre``

Download the full node archive

``wget https://github.com/erachain/Erachain/releases/latest/download/Erachain.zip``

Unzip the archive and navigate to the folder.

``unzip Erachain``

``cd Erachain``

Use the screen program to create a new session

``screen -S erachain``

Run a full Erachain node. Use the specified parameters:

``java -jar erachain.jar -pass=123456789 -seed=1:new:123456789 -nogui -rpc=on -rpcallowed=127.0.0.1``

If necessary, the values of the following parameters can be changed:
- The **-pass** parameter contains the password required to unlock the wallet. Replace 123456789 with the value you wish.
- The **-seed** parameter contains information about the seed and is divided into sub-parameters:
    - The **first sub-parameter** is responsible for the number of accounts to be created.
    - The **second sub-parameter** is responsible for the seed itself. Valid values for this parameter are any existing seed or "new" to create a new seed.
    - The **third sub-parameter** is responsible for the password. Here it's necessary to duplicate the password from the **-pass** parameter

Collapse the erachain session:  ``Ctrl+A+D``

Run a full Erachain node in command line mode:

``java -jar erachain.jar -cli``

Use this command to unlock the wallet:

``post wallet/unlock 123456789``

To find out the received address, enter the following:

``get addresses``

Also, be sure to save seed. You can use it to log in to your web wallet:

``get wallet/seed``

If you need to restart the machine and then go to the already created wallet, change the startup line a little, because
the account information is already saved.

``java -jar erachain.jar -pass=123456789 -nogui -rpc=on -rpcallowed=127.0.0.1``

# Telegram bot onboard

## Blockchain Storage

@blockchain_storage_bot - telegram-bot for auto-storage messages in blockchain
Code in org.erachain.bot.telegram.ErachainStorageBot
Documentation
see https://docs.google.com/document/d/1aW9KqeKFTgGru7z6g4GplRIyqQERWIQVd4Zjje_OrNc/edit#heading=h.6vhy1texy59d

# 🤝 Contributing

Erachain is an open source project and we are very happy to accept community contributions. Please refer
to [Contributing to GitHub page](https://github.com/erachain/Erachain/blob/master/CONTRIBUTING.md) for more details.

For major changes, please open an issue first to discuss what you would like to change. Please make sure to update tests
as appropriate.

# Getting help

If something isn't clear, feel free to ask by email <support@erachain.org> or [Discord](https://discord.gg/vcDbPHyZ).

# Hiring

We are hiring developers, support people, and production engineers all the time. If you're interested, please write to
us <info@erachain.org>

# 📝 Licence

ErachainBP server is licensed under the [aGPL v3](https://github.com/erachain/Erachain/blob/master/LICENSE).

# Copyright

(c) 2018-2022 Erachain