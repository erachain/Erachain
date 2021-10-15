package org.erachain.utils;

import org.apache.commons.lang3.StringUtils;
import org.erachain.api.BlogPostResource;
import org.erachain.core.block.Block;
import org.erachain.core.block.GenesisBlock;
import org.erachain.core.transaction.ArbitraryTransaction;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class UpdateUtil {

    static Logger LOGGER = LoggerFactory.getLogger(UpdateUtil.class.getName());

    public static void repopulateNameStorage(int height) {
        DCSet.getInstance().getNameStorageMap().clear();
        DCSet.getInstance().getOrphanNameStorageHelperMap().clear();
        DCSet.getInstance().getOrphanNameStorageMap().clear();
        DCSet.getInstance().getHashtagPostMap().clear();

        Block b = new GenesisBlock();
        do {
            if (b.getHeight() >= height) {
                List<Transaction> txs = b.getTransactions();
                for (Transaction tx : txs) {
                    if (tx instanceof ArbitraryTransaction) {
                        ArbitraryTransaction arbTx = (ArbitraryTransaction) tx;
                        int service = arbTx.getService();
                        if (service == 10) {
                            StorageUtils.processUpdate(arbTx.getData(),
                                    arbTx.getSignature(), arbTx.getCreator(),
                                    DCSet.getInstance());
                        } else if (service == 777) {
                            byte[] data = arbTx.getData();
                            String string = new String(data);


                            JSONObject jsonObject = (JSONObject) JSONValue
                                    .parse(string);
                            if (jsonObject != null) {
                                String post = (String) jsonObject
                                        .get(BlogPostResource.POST_KEY);

                                String share = (String) jsonObject
                                        .get(BlogPostResource.SHARE_KEY);


                                boolean isShare = false;
                                if (StringUtils.isNotEmpty(share)) {
                                    isShare = true;
                                }

                                // DOES POST MET MINIMUM CRITERIUM?
                                if (StringUtils.isNotBlank(post)) {

                                    // Shares won't be hashtagged!
                                    if (!isShare) {
                                        List<String> hashTags = BlogUtils
                                                .getHashTags(post);
                                        for (String hashTag : hashTags) {
                                            DCSet.getInstance()
                                                    .getHashtagPostMap()
                                                    .add(hashTag,
                                                            arbTx.getSignature());
                                        }
                                    }

                                }

                            }
                        }
                    }
                }
            }
            b = b.getChild(DCSet.getInstance());
        } while (b != null);

    }


    public static void repopulateTransactionFinalMap(DCSet dcSet) {

        dcSet.getTransactionFinalMap().clear();

        Block block = new GenesisBlock();
        do {
            List<Transaction> txs = block.getTransactions();
            int counter = 1;
            for (Transaction tx : txs) {
                dcSet.getTransactionFinalMap().add(block.getHeight(), counter, tx);
                counter++;
            }
            if (block.getHeight() % 1000 == 0) {
                LOGGER.info("UpdateUtil - Repopulating TransactionMap : " + block.getHeight());
            }
            dcSet.flush(512 + block.blockHead.transactionsCount * 512 + block.blockHead.size << 1, false, false);

            block = block.getChild(dcSet);

        } while (block != null);

        dcSet.flush(0, true, true);

    }

    public static void repopulateCommentPostMap() {
        DCSet.getInstance().getPostCommentMap().clear();

        Block block = new GenesisBlock();
        int height = block.getHeight();
        do {
            List<Transaction> txs = block.getTransactions();
            int seqNo = 0;
            for (Transaction tx : txs) {
                tx.setDC(DCSet.getInstance(), Transaction.FOR_NETWORK, height, ++seqNo, false);

                if (tx instanceof ArbitraryTransaction) {
                    int service = ((ArbitraryTransaction) tx).getService();
                    if (service == BlogUtils.COMMENT_SERVICE_ID) {
                        ((ArbitraryTransaction) tx).addToCommentMapOnDemand();
                    }
                }
            }
            if (block.getHeight() % 1000 == 0) {
                LOGGER.info("UpdateUtil - Repopulating CommentPostMap : " + block.getHeight());
                DCSet.getInstance().flush(512 + block.blockHead.transactionsCount * 512 + block.blockHead.size << 1, false, false);
            }
            block = block.getChild(DCSet.getInstance());
        } while (block != null);

    }
}

