package org.erachain;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.settings.Settings;
import org.erachain.utils.FileUtils;
import org.json.simple.JSONArray;
import org.mapdb.Fun;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Paths;
import java.util.List;

//import org.erachain.utils.Logging;
//import org.springframework.boot.autoconfigure.SpringBootApplication;
//import org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration;
//import org.springframework.boot.builder.SpringApplicationBuilder;
//import org.springframework.context.annotation.Bean;
//import org.springframework.web.servlet.DispatcherServlet;

//@SpringBootApplication(exclude = {org.springframework.boot.autoconfigure.gson.GsonAutoConfiguration.class})
public class Start {

    private static final Logger LOGGER = LoggerFactory.getLogger(Settings.class);

    //@Bean(name = DispatcherServletAutoConfiguration.DEFAULT_DISPATCHER_SERVLET_BEAN_NAME)

    // logger of WEBSERVERS
//    public DispatcherServlet dispatcherServlet() {
//        return new Logging();
//    }


    public static void main(String args[]) throws Exception {

        if (java.nio.file.Files.exists(Paths.get(System.getProperty("user.dir"),"src", "main"))) {
            throw new RuntimeException("Wrong directory - set to /ERA");
        }

        System.out.println("Erachain " + Controller.getBuildDateTimeString());
        System.out.println("use option -?, -h, -help    - for see help");

        //SpringApplicationBuilder builder = new SpringApplicationBuilder(Start.class);

        if (args.length == 1 && (args[0].equals("-?") || args[0].equals("-h") || args[0].equals("-help"))) {
            File file = new File("z_START_EXAMPLES", "readme-commands.txt");
            List<String> lines = Files.readLines(file, Charsets.UTF_8);
            for (String line : lines) {
                System.out.println(line);
            }
            return;
        }

        //builder.headless(false).run(args);
        File file = new File("startARGS.txt");
        if (file.exists()) {
            LOGGER.info("startARGS.txt USED");
            try {
                List<String> lines = Files.readLines(file, Charsets.UTF_8);

                String parsString = "";
                for (String line : lines) {
                    if ((line = line.trim()).startsWith("//") || line.isEmpty()) {
                        // пропускаем //
                        continue;
                    }
                    parsString += " -" + line.trim(); // add -
                }

                String[] pars = parsString.trim().split(" ");
                String[] argsNew = new String[args.length + pars.length];
                int i = 0;
                // PARS has LOW priority
                for (String par : pars) {
                    argsNew[i++] = par;
                }
                // ARGS has HIGH priority и затрут потом в аврсинге значенийц те что были в файле заданы
                for (String arg : args) {
                    argsNew[i++] = arg;
                }

                args = argsNew;

            } catch (Exception e) {
                LOGGER.info("Error while reading " + file.getAbsolutePath());
                LOGGER.error(e.getMessage(), e);
                System.exit(3);
            }
        }

        long genesisStamp;
        for (String arg : args) {
            if (arg.equals("-testnet")) {
                genesisStamp = -1;
                Settings.simpleTestNet = true;
                Settings.genesisStamp = genesisStamp;
                Settings.NET_MODE = Settings.NET_MODE_TEST;
            } else if (arg.startsWith("-testnet=") && arg.length() > 9) {
                try {
                    genesisStamp = Long.parseLong(arg.substring(9));
                    Settings.NET_MODE = Settings.NET_MODE_TEST;

                } catch (Exception e) {
                    genesisStamp = Settings.DEFAULT_DEMO_NET_STAMP;
                    Settings.NET_MODE = Settings.NET_MODE_DEMO;
                }
                Settings.genesisStamp = genesisStamp;
            } else if (arg.startsWith("-testdb=") && arg.length() > 8) {
                try {
                    Settings.TEST_DB_MODE = Integer.parseInt(arg.substring(8));
                } catch (Exception e) {
                }
            } else if (arg.startsWith("-bugs=") && arg.length() > 6) {
                try {
                    String value = arg.substring(6);
                    Settings.CHECK_BUGS = Integer.parseInt(value);
                    LOGGER.info("-bugs = " + Settings.CHECK_BUGS);
                } catch (Exception e) {
                }
            }

        }

        ///////////////////  CLONECHAINS ///////////
        file = new File(Settings.CLONE_OR_SIDE.toLowerCase() + "GENESIS.json");
        if (Settings.NET_MODE == Settings.NET_MODE_MAIN && Settings.TEST_DB_MODE == 0 && file.exists()) {

            // START SIDE CHAIN
            LOGGER.info(Settings.CLONE_OR_SIDE.toLowerCase() + "GENESIS.json USED");
            Settings.genesisJSON = FileUtils.readCommentedJSONArray(file.getPath());

            if (Settings.genesisJSON == null) {
                LOGGER.error("Wrong JSON or not UTF-8 encode in " + file.getName());
                throw new Exception("Wrong JSON or not UTF-8 encode in " + file.getName());
            }

            JSONArray appArray = (JSONArray) Settings.genesisJSON.get(0);
            Settings.APP_NAME = appArray.get(0).toString();
            Settings.APP_FULL_NAME = appArray.get(1).toString();
            JSONArray timeArray = (JSONArray) Settings.genesisJSON.get(1);
            Settings.genesisStamp = new Long(timeArray.get(0).toString());

            try {
                // если там пустой список то включаем "у всех все есть"
                JSONArray holders = (JSONArray) Settings.genesisJSON.get(2);
                if (holders.isEmpty()) {
                    Settings.ERA_COMPU_ALL_UP = true;
                } else {
                    // CHECK VALID
                    for (int i = 0; i < holders.size(); i++) {
                        JSONArray holder = (JSONArray) holders.get(i);
                        // SEND FONDs
                        Fun.Tuple2<Account, String> accountItem = Account.tryMakeAccount(holder.get(0).toString());
                        if (accountItem.a == null) {
                            String error = accountItem.b + " - " + holder.get(0).toString();
                            LOGGER.error(error);
                            System.exit(4);
                        }

                        if (holder.size() > 3) {
                            // DEBTORS
                            JSONArray debtors = (JSONArray) holder.get(3);
                            BigDecimal totalCredit = BigDecimal.ZERO;
                            for (int j = 0; j < debtors.size(); j++) {
                                JSONArray debtor = (JSONArray) debtors.get(j);

                                accountItem = Account.tryMakeAccount(debtor.get(1).toString());
                                if (accountItem.a == null) {
                                    String error = accountItem.b + " - " + debtor.get(1).toString();
                                    LOGGER.error(error);
                                    System.exit(4);
                                }
                            }
                        }
                    }

                }
            } catch (Exception e) {
                LOGGER.info("Error while parse JSON " + file.getAbsolutePath() + " - " + e.getMessage());
                LOGGER.error(e.getMessage(), e);
                System.exit(3);
            }


            Settings.NET_MODE = Settings.NET_MODE_CLONE;

        }

        Settings.getInstance();

        Controller.getInstance().startApplication(args);

    }

}
