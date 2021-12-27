package org.erachain.datachain;

import org.erachain.dbs.IteratorCloseable;

/**
 * Iterators for this TAB
 */
public interface TransactionFinalSuit {

    void deleteForBlock(Integer height);

    /**
     * Use in FORK by MergedOR_IteratorsNoDuplicates
     *
     * @param height
     * @param descending
     * @return
     */
    IteratorCloseable<Long> getOneBlockIterator(Integer height, boolean descending);

    IteratorCloseable<Long> getIteratorByCreator(byte[] addressShort, boolean descending);

    IteratorCloseable<Long> getIteratorByCreator(byte[] addressShort, Long fromSeqNo, boolean descending);

    IteratorCloseable<Long> getIteratorByCreator(byte[] addressShort, Long fromSeqNo, Long toSeqNo, boolean descending);


    IteratorCloseable<Long> getIteratorByRecipient(byte[] addressShort, boolean descending);

    IteratorCloseable<Long> getIteratorByRecipient(byte[] addressShort, Long fromSeqNo, boolean descending);

    IteratorCloseable<Long> getIteratorByRecipient(byte[] addressShort, Long fromSeqNo, Long toSeqNo, boolean descending);

    IteratorCloseable<Long> getIteratorOfDialog(byte[] addressesKey, Long fromSeqNo, boolean descending);

    /**
     * @param addressShort
     * @param type         - TRANSACTION type
     * @param isCreator    True - only CREATORS, False - only RECIPIENTS, None - all
     * @param descending
     * @return
     */
    IteratorCloseable<Long> getIteratorByAddressAndType(byte[] addressShort, Integer type, Boolean isCreator, boolean descending);

    /**
     * Здесь обязательно нужно задавать тип транзакции и получатель или создатель - иначе по FROM_ID работать не будет в RocksDB.
     * Иначе используйте getIteratorByCreator.
     * Если надо делать поиск с заданного fromID - то надо передать сюда полный индекс для начального поиска если какие-то
     * из параметров не заданные - чтобы его поставить как поисковый для начала а дальше уже индекс ограничения - хотя тоже не сработает
     * ограничение так как там по fromID перед ним значения будут выше и останов только по LIMIT выше делать надо.
     * Таким образом для по-страничного перебора и в getIteratorByAddressAndType - туда надо передавать текущий индекс
     * для начального поиска и с него уже итератор брать - тогда страницами можно организовать. <br>
     * Получается что если задан fromID но не задан какой-либо уточняющий параметр то надо наверху взять транзакцию
     * и из нее взять эти недостающие параметры чтобы точно найти первую запись.
     * <b>Значит сюда нужно передавать как начальные параметры так и ограничивающие сверху - тогда все четко будет работать</b>
     *
     * @param addressShort
     * @param type         - TRANSACTION type
     * @param isCreator    True - only CREATORS, False - only RECIPIENTS, None - all
     * @param fromID
     * @param descending
     * @return
     */
    IteratorCloseable<Long> getIteratorByAddressAndType(byte[] addressShort, Integer type, Boolean isCreator, Long fromID, boolean descending);
    IteratorCloseable<Long> getIteratorByAddressAndType(byte[] addressShort, Integer type, Boolean isCreator, Long fromID, Long toID, boolean descending);

    IteratorCloseable<Long> getIteratorByTitle(String filter, boolean asFilter, String fromWord, Long fromSeqNo, boolean descending);

    IteratorCloseable<Long> getAddressesIterator(byte[] addressShort, Long fromSeqNo, boolean descending);

}
