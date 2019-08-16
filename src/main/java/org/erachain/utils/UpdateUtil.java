package org.erachain.utils;

import org.apache.commons.lang3.StringUtils;
import org.erachain.api.BlogPostResource;
import org.erachain.core.block.Block;
import org.erachain.core.block.GenesisBlock;
import org.erachain.core.transaction.ArbitraryTransaction;
import org.erachain.core.transaction.Transaction;
import org.erachain.database.SortableList;
import org.erachain.datachain.BlockMap;
import org.erachain.datachain.DCSet;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class UpdateUtil {

    static Logger LOGGER = LoggerFactory.getLogger(UpdateUtil.class.getName());

    public static void repopulateNameStorage(int height) {
        DCSet.getInstance().getNameStorageMap().reset();
        DCSet.getInstance().getOrphanNameStorageHelperMap().reset();
        DCSet.getInstance().getOrphanNameStorageMap().reset();
        DCSet.getInstance().getHashtagPostMap().reset();

        SortableList<Integer, Block> blocks = DCSet.getInstance().getBlockMap().getList();
        blocks.sort(BlockMap.HEIGHT_INDEX);

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

        dcSet.getTransactionFinalMap().reset();

        Block b = new GenesisBlock();
        do {
            List<Transaction> txs = b.getTransactions();
            int counter = 1;
            for (Transaction tx : txs) {
                dcSet.getTransactionFinalMap().add(b.getHeight(), counter, tx);
                counter++;
            }
            if (b.getHeight() % 1000 == 0) {
                LOGGER.info("UpdateUtil - Repopulating TransactionMap : " + b.getHeight());
            }
            dcSet.flush((3 + b.getTransactionCount()) * 5, false);

            b = b.getChild(dcSet);

        } while (b != null);

    }

    public static void repopulateCommentPostMap() {
        DCSet.getInstance().getPostCommentMap().reset();

        Block b = new GenesisBlock();
        int height = b.getHeight();
        do {
            List<Transaction> txs = b.getTransactions();
            int seqNo = 0;
            for (Transaction tx : txs) {
                tx.setDC(DCSet.getInstance(), Transaction.FOR_NETWORK, height, ++seqNo);

                if (tx instanceof ArbitraryTransaction) {
                    int service = ((ArbitraryTransaction) tx).getService();
                    if (service == BlogUtils.COMMENT_SERVICE_ID) {
                        ((ArbitraryTransaction) tx).addToCommentMapOnDemand();
                    }
                }
            }
            if (b.getHeight() % 2000 == 0) {
                LOGGER.info("UpdateUtil - Repopulating CommentPostMap : " + b.getHeight());
                DCSet.getInstance().flush((3 + b.getTransactionCount()) * 5, false);
            }
            b = b.getChild(DCSet.getInstance());
        } while (b != null);

    }
}

