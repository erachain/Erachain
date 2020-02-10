package org.erachain.dbs.nativeMemMap;

import org.junit.Test;

public class NativeMapTreeMapForkTest {

    @Test
    public void openMap() {
    }

    /**
     * Надо сделать тест тут так как была уже ситация что не удалялись данные из-за того что ключ БАЙТЫ а мапка this.deleted = HashMap создавалась
     * и в ней поиск байт массивов не делался
     * <p>
     * Поэтому задаем КОМПАРАТОР при создании и тогда там в deleted = TreeMap(COMPARATOR) - все вернопроисходит
     */
    @Test
    public void delete() {
    }

}