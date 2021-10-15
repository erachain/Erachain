package org.erachain.datachain;

import org.erachain.controller.Controller;
import org.erachain.dbs.DBTab;
import org.mapdb.*;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple3;
import org.mapdb.Fun.Tuple4;

import java.math.BigDecimal;
import java.util.TreeMap;

/**
 * balances for Essence + Asset -> BALANCE (example datachain.PersonAssetsBalance)
 * TODO: SOFT HARD TRUE
 * -> in_OWN(a, b), in_RENT(a, b), on_HOLD(a, b), SPEND(a, b)
 *   = in_USE (TOTAL on HAND)
 *   a - income
 *   b - balance
 *   outcome = b - a
 */
public abstract class BalanceMap extends DCUMap<Tuple2<Long, Long>,
        Tuple4<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>,
                Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>> {

    private String name;

    @SuppressWarnings("rawtypes")
    private BTreeMap assetKeyMap;

    public BalanceMap(DCSet databaseSet, DB database,
                      String name, int reset_BALANCE_TYPE, int add_BALANCE_TYPE, int remove_BALANCE_TYPE, int list_BALANCE_TYPE
    ) {
        super(databaseSet, database);

        this.name = name;

        if (databaseSet.isWithObserver()) {
            this.observableData.put(DBTab.NOTIFY_RESET, reset_BALANCE_TYPE);
            this.observableData.put(DBTab.NOTIFY_LIST, list_BALANCE_TYPE);
            this.observableData.put(DBTab.NOTIFY_ADD, add_BALANCE_TYPE);
            this.observableData.put(DBTab.NOTIFY_REMOVE, remove_BALANCE_TYPE);
        }
    }

    public BalanceMap(BalanceMap parent, DCSet dcSet) {
        super(parent, dcSet);
    }

    @SuppressWarnings({"unchecked"})
    @Override
    public void openMap() {

        //sizeEnable = true; // разрешаем счет размера - это будет немного тормозить работу

        //OPEN MAP
        map = database.createTreeMap("assets_balances_" + this.name)
                .keySerializer(BTreeKeySerializer.TUPLE2)
                //.counterEnable()
                .makeOrGet();

        //HAVE/WANT KEY
        this.assetKeyMap = database.createTreeMap("balances_key_asset_" + this.name)
                .comparator(Fun.COMPARATOR)
                .makeOrGet();

        if (Controller.getInstance().onlyProtocolIndexing)
            // NOT USE SECONDARY INDEXES
            return;

        Bind.secondaryKey((BTreeMap)map, this.assetKeyMap, new Fun.Function2<Tuple3<Long, Tuple4<Tuple2<BigDecimal, BigDecimal>,
                Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>, Long>,
                Tuple2<Long, Long>, Tuple4<Tuple2<BigDecimal, BigDecimal>,
                Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>>() {
            @Override
            public Tuple3<Long, Tuple4<Tuple2<BigDecimal, BigDecimal>,
                    Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>, Long> run(Tuple2<Long, Long> key, Tuple4<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>> value) {
                return new Tuple3<Long, Tuple4<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>, Long>(
                        key.b, new Tuple4<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>
                        (
                                new Tuple2<BigDecimal, BigDecimal>(value.a.a.negate(), value.a.b.negate()),
                                new Tuple2<BigDecimal, BigDecimal>(value.b.a.negate(), value.b.b.negate()),
                                new Tuple2<BigDecimal, BigDecimal>(value.c.a.negate(), value.c.b.negate()),
                                new Tuple2<BigDecimal, BigDecimal>(value.d.a.negate(), value.d.b.negate())
                        ),
                        key.a);
            }
        });
    }

    @Override
    protected void getMemoryMap() {
        map = new TreeMap<Tuple2<Long, Long>, Tuple4<Tuple2<BigDecimal, BigDecimal>,
                Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>>(Fun.TUPLE2_COMPARATOR);
    }

    @Override
    public Tuple4<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>> getDefaultValue() {
        return new Tuple4<Tuple2<BigDecimal, BigDecimal>,
                Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>
                (
                        new Tuple2<BigDecimal, BigDecimal>(BigDecimal.ZERO, BigDecimal.ZERO),
                        new Tuple2<BigDecimal, BigDecimal>(BigDecimal.ZERO, BigDecimal.ZERO),
                        new Tuple2<BigDecimal, BigDecimal>(BigDecimal.ZERO, BigDecimal.ZERO),
                        new Tuple2<BigDecimal, BigDecimal>(BigDecimal.ZERO, BigDecimal.ZERO)
                );
    }

    public void put(Long essence, long key, Tuple4<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>> value) {
        this.put(new Tuple2<Long, Long>(essence, key), value);
    }

    public Tuple4<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>> get(long essence, long key) {
        return this.get(new Tuple2<Long, Long>(essence, key));
    }

}
