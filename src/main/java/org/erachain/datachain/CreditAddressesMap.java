package org.erachain.datachain;

import org.erachain.core.transaction.Transaction;
import org.mapdb.BTreeKeySerializer;
import org.mapdb.BTreeMap;
import org.mapdb.DB;
import org.mapdb.Fun;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple3;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeMap;

/** Общая сумма переданных средств в кредит на другой счет
 * Используется для проверки сумм которые отдаются или забираются у заемщика<br><br>
 *
 * <b>Ключ:</b> account.address Creditor + asset key + account.address Debtor<br>
 *
 * <b>Значение:</b> сумма средств
 *
 */

public class CreditAddressesMap extends DCUMap<Tuple3<String, Long, String>, BigDecimal> {


    public CreditAddressesMap(DCSet databaseSet, DB database) {
        super(databaseSet, database);
    }

    public CreditAddressesMap(CreditAddressesMap parent, DCSet dcSet) {
        super(parent, dcSet);
    }

    @Override
    public void openMap() {
        HI = new Tuple3(Fun.HI, Fun.HI, Fun.HI);

        //OPEN MAP
        map = database.createTreeMap("credit_debt")
                .keySerializer(BTreeKeySerializer.TUPLE3)
                //.comparator(UnsignedBytes.lexicographicalComparator())
                //.comparator(Fun.COMPARATOR)
                //.comparator(Fun.COMPARABLE_ARRAY_COMPARATOR)
                //.comparator(SignedBytes.lexicographicalComparator())
                .makeOrGet();
    }

    @Override
    protected void getMemoryMap() {
        map = new TreeMap<Tuple3<String, Long, String>, BigDecimal>();
    }

    @Override
    public BigDecimal getDefaultValue() {
        return BigDecimal.ZERO;
    }

    public BigDecimal add(Tuple3<String, Long, String> key, BigDecimal amount) {
        BigDecimal summ = this.get(key).add(amount);
        this.put(key, summ);
        return summ;
    }

    public BigDecimal add(String creditorAddress, long key, String debtorAddress, BigDecimal amount) {
        return this.add(new Tuple3<String, Long, String>(creditorAddress, key, debtorAddress), amount);
    }

    public BigDecimal sub(Tuple3<String, Long, String> key, BigDecimal amount) {
        BigDecimal summ = this.get(key).subtract(amount);
        this.put(key, summ);
        return summ;
    }

    public BigDecimal get(String creditorAddress, long key, String debtorAddress) {
        return this.get(new Tuple3<String, Long, String>(creditorAddress, key, debtorAddress));
    }

    /**
     * For GUI only
     * @param creditorAddress
     * @param key
     * @return
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public List<Tuple2<Tuple3<String, Long, String>, BigDecimal>> getList(String creditorAddress, long key) {
        BTreeMap map = (BTreeMap) this.map;
        //GET ALL TRANSACTIONS THAT BELONG TO THAT ADDRESS
        Collection<Tuple3> keys = ((BTreeMap<Tuple3, Transaction>) map).subMap(
                Fun.t3(creditorAddress, key, null),
                Fun.t3(creditorAddress, key, Fun.HI())).keySet();

        //DELETE TRANSACTIONS
        List<Tuple2<Tuple3<String, Long, String>, BigDecimal>> result = new ArrayList<Tuple2<Tuple3<String, Long, String>, BigDecimal>>();

        for (Tuple3<String, Long, String> keyMap : keys) {
            result.add(new Tuple2<Tuple3<String, Long, String>, BigDecimal>(keyMap, this.get(keyMap)));
        }

        return result;
    }

    public void delete(String creditorAddress, long key, String debtorAddress) {
        this.delete(new Tuple3<String, Long, String>(creditorAddress, key, debtorAddress));
    }
}
