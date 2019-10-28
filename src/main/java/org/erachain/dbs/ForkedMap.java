package org.erachain.dbs;

public interface ForkedMap {

    DBTab getParent();

    /**
     * ВНИМАНИЕ!!! в связи с работой этого метода при сливе - нельяза в стандартных методах Источника
     * set и delete делать какую либо логику! иначе будут двойные срабатывания - в Форке и тут при сливе.
     * Все имена set и delete дополнить AndProcess.
     * Это касаейтся DCU источников и Suit источников. Более верхний уровень поидее не участвует в сливе с set & delete
     */
    void writeToParent();
}
