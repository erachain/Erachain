package org.erachain.core.item.polls;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import org.erachain.core.BlockChain;
import org.erachain.core.account.Account;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.item.ItemCls;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;
import org.erachain.datachain.ItemMap;
import org.erachain.datachain.VoteOnItemPollMap;
import org.erachain.lang.Lang;
import org.erachain.utils.Pair;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.mapdb.Fun;
import org.mapdb.Fun.Tuple3;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.*;

public abstract class PollCls extends ItemCls {

    public static final int TYPE_KEY = ItemCls.POLL_TYPE;

    public static final int POLL = 1;
    public static final int INITIAL_FAVORITES = 0;
    protected static final int OPTIONS_SIZE_LENGTH = 4;
    protected static final int BASE_LENGTH = OPTIONS_SIZE_LENGTH;
    private List<String> options;

    public PollCls(byte[] typeBytes, byte[] appData, PublicKeyAccount maker, String name, byte[] icon, byte[] image, String description, List<String> options) {
        super(typeBytes, appData, maker, name, icon, image, description);
        this.options = options;

    }

    public PollCls(int type, byte[] appData, PublicKeyAccount maker, String name, byte[] icon, byte[] image, String description, List<String> options) {
        this(new byte[TYPE_LENGTH], appData, maker, name, icon, image, description, options);
        this.typeBytes[0] = (byte) type;
    }

    //GETTERS/SETTERS

    @Override
    public int getItemType() {
        return TYPE_KEY;
    }

    @Override
    public long START_KEY() {
        if (Transaction.parseHeightDBRef(dbRef) > BlockChain.START_KEY_UP)
            return BlockChain.START_KEY_UP_ITEMS;

        return START_KEY_OLD;
    }

    @Override
    public long MIN_START_KEY() {
        if (Transaction.parseHeightDBRef(dbRef) > BlockChain.START_KEY_UP)
            return BlockChain.START_KEY_UP_ITEMS;

        return MIN_START_KEY_OLD;
    }

    public String getItemTypeName() {
        return "poll";
    }

    public List<String> getOptions() {
        return this.options;
    }

    public List<String> viewOptions() {
        List<String> result = new ArrayList<>();
        int count = 0;
        for (String option: this.options) {
            result.add(++count + ": " + option);
        }
        return result;
    }

    @Override
    public ItemMap getDBMap(DCSet dc) {
        return dc.getItemPollMap();
    }

    public boolean hasVotes(DCSet dc) {
        return dc.getVoteOnItemPollMap().hasVotes(this.key);
    }

    public BigDecimal getTotalVotes(DCSet dcSet) {
        return getTotalVotes(dcSet, 2);
    }

    public BigDecimal getTotalVotes(DCSet dcSet, long assetKey) {
        BigDecimal votesSum = BigDecimal.ZERO;
        VoteOnItemPollMap map = dcSet.getVoteOnItemPollMap();
        NavigableSet<Tuple3> optionVoteKeys;
        Account voter;

        optionVoteKeys = map.getVotes(this.key);
        for (Tuple3<Long, Integer, BigInteger> key : optionVoteKeys) {
            voter = Account.makeAccountFromShort(key.c);
            votesSum = votesSum.add(voter.getBalanceUSE(assetKey));
        }

        return votesSum;
    }

    public BigDecimal getTotalVotes(DCSet dcSet, long assetKey, int option) {
        BigDecimal votesSum = BigDecimal.ZERO;
        VoteOnItemPollMap map = dcSet.getVoteOnItemPollMap();
        NavigableSet<Tuple3> optionVoteKeys;
        Account voter;

        optionVoteKeys = map.getVotes(this.key);
        for (Tuple3<Long, Integer, BigInteger> key : optionVoteKeys) {
            if (option != key.b)
                continue;

            voter = Account.makeAccountFromShort(key.c);
            votesSum = votesSum.add(voter.getBalanceUSE(assetKey));
        }

        return votesSum;
    }


    public List<Pair<Account, Integer>> getVotes(DCSet dcSet) {
        List<Pair<Account, Integer>> votes = new ArrayList<Pair<Account, Integer>>();

        VoteOnItemPollMap map = dcSet.getVoteOnItemPollMap();
        NavigableSet<Tuple3> optionVoteKeys;
        Pair<Account, Integer> vote;
        Account voter;

        optionVoteKeys = map.getVotes(this.key);
        for (Tuple3<Long, Integer, BigInteger> key : optionVoteKeys) {
            voter = Account.makeAccountFromShort(key.c);
            vote = new Pair<Account, Integer>(voter, key.b);
            votes.add(vote);
        }

        return votes;
    }

    /**
     * список всех персон голосующих
     * @param dcSet
     * @return
     */
    public List<Pair<Account, Integer>> getPersonVotes(DCSet dcSet) {
        List<Pair<Account, Integer>> votes = new ArrayList<Pair<Account, Integer>>();

        VoteOnItemPollMap map = dcSet.getVoteOnItemPollMap();
        NavigableSet<Tuple3> optionVoteKeys;
        Pair<Account, Integer> vote;
        Account voter;

        optionVoteKeys = map.getVotes(this.key);
        for (Tuple3<Long, Integer, BigInteger> key : optionVoteKeys) {
            voter = Account.makeAccountFromShort(key.c);
            if (voter.isPerson(dcSet, 0)) {
                vote = new Pair<Account, Integer>(voter, key.b);
                votes.add(vote);
            }
        }

        return votes;
    }

    /**
     * тут ошибка так как при переголосвании не учитывается повторное голосование - используй votesWithPersons
     * @param dcSet
     * @return
     */
    public List<Long> getPersonCountVotes(DCSet dcSet) {


        List<Long> votes = new ArrayList<>(this.options.size());
        for (int i = 0; i < options.size(); i++) {
            votes.add(0L);
        }

        VoteOnItemPollMap map = dcSet.getVoteOnItemPollMap();
        NavigableSet<Tuple3> optionVoteKeys;
        Pair<Account, Integer> vote;
        Account voter;

        optionVoteKeys = map.getVotes(this.key);
        for (Tuple3<Long, Integer, BigInteger> key : optionVoteKeys) {
            voter = Account.makeAccountFromShort(key.c);
            Integer optionNo = key.b;
            if (voter.isPerson(dcSet, 0)) {
                Long count = votes.get(optionNo);
                votes.add(optionNo, count + 1L);
            }
        }

        return votes;
    }

    /**
     * Можно задавать как номер актива так и позицию баланса. Если позиция = 0 то берем Имею + Долг
     * @param dcSet
     * @param assetKey
     * @param balancePosition
     * @return
     */
    public Fun.Tuple4<Integer, long[], BigDecimal, BigDecimal[]> votesWithPersons(DCSet dcSet, long assetKey, int balancePosition) {

        int optionsSize = options.size();
        long[] personVotes = new long[optionsSize];
        int personsTotal = 0;

        BigDecimal[] optionVotes = new BigDecimal[optionsSize];
        for (int i = 0; i < optionVotes.length; i++) {
            optionVotes[i] = BigDecimal.ZERO;
        }

        BigDecimal votesSum = BigDecimal.ZERO;

        Set personsVotedSet = new HashSet<Long>();
        Iterable<Pair<Account, Integer>> votes = getVotes(dcSet);
        Iterator iterator = votes.iterator();
        while (iterator.hasNext()) {

            Pair<Account, Integer> item = (Pair<Account, Integer>)iterator.next();

            int option = item.getB();
            // voter = Account.makeAccountFromShort(item.getA());

            Account voter = item.getA();
            Fun.Tuple4<Long, Integer, Integer, Integer> personInfo = voter.getPersonDuration(dcSet);

            // запретим голосовать много раз разными счетами одной персоне
            if (personInfo != null
                    && !personsVotedSet.contains(personInfo.a)) {
                personVotes[option]++;
                personsTotal++;

                // запомним что он голосовал
                personsVotedSet.add(personInfo.a);
            }

            BigDecimal votesVol;
            if (balancePosition > 0) {
                votesVol = voter.getBalanceForPosition(dcSet, assetKey, balancePosition).b;
            } else {
                votesVol = voter.getBalanceUSE(assetKey, dcSet);
            }

            optionVotes[option] = optionVotes[option].add(votesVol);
            votesSum = votesSum.add(votesVol);

        }

        return new Fun.Tuple4<>(personsTotal, personVotes, votesSum, optionVotes);
    }

    public long getPersonCountTotalVotes(DCSet dcSet) {
        long votes = 0L;

        VoteOnItemPollMap map = dcSet.getVoteOnItemPollMap();
        NavigableSet<Tuple3> optionVoteKeys;
        Pair<Account, Integer> vote;
        Account voter;

        optionVoteKeys = map.getVotes(this.key);
        for (Tuple3<Long, Integer, BigInteger> key : optionVoteKeys) {
            voter = Account.makeAccountFromShort(key.c);
            Integer optionNo = key.b;
            if (voter.isPerson(dcSet, 0)) {
                ++votes;
            }
        }

        return votes;
    }

    public List<Pair<Account, Integer>> getVotes(DCSet dcSet, List<Account> accounts) {
        List<Pair<Account, Integer>> votes = new ArrayList<Pair<Account, Integer>>();

        VoteOnItemPollMap map = dcSet.getVoteOnItemPollMap();
        NavigableSet<Tuple3> optionVoteKeys; // <Long, Integer, BigInteger>
        Pair<Account, Integer> vote;
        Account voter;

        optionVoteKeys = map.getVotes(this.key);
        for (Tuple3<Long, Integer, BigInteger> key : optionVoteKeys) {
            for (Account account : accounts) {
                if (account.equals(key.c)) {
                    vote = new Pair<Account, Integer>(account, key.b);
                    votes.add(vote);
                }
            }
        }

        return votes;
    }

    public int getOption(String option) {

        int i = 0;
        for (String pollOption : this.options) {
            if (pollOption.equals(option)) {
                return i;
            }

            i++;
        }

        return -1;
    }

    public String viewOption(int option) {
        return (option + 1) + ": " + this.options.get(option);
    }

    // PARSE
    public byte[] toBytes(int forDeal, boolean includeReference, boolean onlyBody) {

        byte[] data = super.toBytes(forDeal, includeReference, onlyBody);

        //WRITE OPTIONS SIZE
        byte[] optionsLengthBytes = Ints.toByteArray(this.options.size());
        data = Bytes.concat(data, optionsLengthBytes);

        //WRITE OPTIONS
        for (String option : this.options) {

            //WRITE NAME SIZE
            byte[] optionBytes = option.getBytes(StandardCharsets.UTF_8);
            data = Bytes.concat(data, new byte[]{(byte) optionBytes.length});

            //WRITE NAME
            data = Bytes.concat(data, optionBytes);
        }

        return data;
    }


    public int getDataLength(boolean includeReference) {
        int length = super.getDataLength(includeReference) + BASE_LENGTH;

        for (String option : this.options) {
            length += 1 + option.getBytes(StandardCharsets.UTF_8).length;
        }

        return length;

    }


    //OTHER

    @SuppressWarnings("unchecked")
    public JSONObject toJson() {

        JSONObject pollJSON = super.toJson();

        JSONArray jsonOptions = new JSONArray();
        for (String option : this.options) {
            jsonOptions.add(option);
        }

        pollJSON.put("options", jsonOptions);
        pollJSON.put("totalVotes", getTotalVotes(DCSet.getInstance()).toPlainString());

        return pollJSON;
    }

    public JSONObject jsonForExplorerPage(JSONObject langObj, Object[] args) {

        JSONObject json = super.jsonForExplorerPage(langObj, args);

        json.put("optionsCount", options.size());
        json.put("totalVotes", getTotalVotes(DCSet.getInstance()).toPlainString());

        return json;
    }

    public JSONObject jsonForExplorerInfo(DCSet dcSet, JSONObject langObj, boolean forPrint) {

        JSONObject itemJson = super.jsonForExplorerInfo(dcSet, langObj, forPrint);
        itemJson.put("Label_Poll", Lang.T("Poll", langObj));


        if (!forPrint) {
        }

        return itemJson;
    }

}
