package org.erachain.core.blockexplorer;

import org.erachain.core.item.ItemCls;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.item.persons.PersonCls;
import org.erachain.core.item.statuses.StatusCls;
import org.erachain.datachain.DCSet;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ReceiverMapForBlockExplorer {
    private static final Logger logger = LoggerFactory.getLogger(ReceiverMapForBlockExplorer.class);
    private int start;
    private List<ItemCls> listItems;
    private int size;
    private int numberOfRepresentsItemsOnPage;
    private Map map;
    private long key;

    ReceiverMapForBlockExplorer(int startPerson, List<ItemCls> listPersons, int size) {
        this.start = startPerson;
        this.listItems = listPersons;
        this.size = size;
    }

    Map getMap() {
        return map;
    }

    public long getKey() {
        return key;
    }

    void setNumberOfRepresentsItemsOnPage(int numberOfRepresentsItemsOnPage) {
        this.numberOfRepresentsItemsOnPage = numberOfRepresentsItemsOnPage;
    }

    <T> void process(Class<T> type, DCSet dcSet, JSONObject langObj) throws Exception {
        //Если начальный номер элемента не задан - берем последний
        if (start == -1) {
            start = (int) listItems.get(size - 1).getKey();
        }
        int number = 0;
        //Подсчитать количество элементов, ключи которых меньше или равны переданному параметру из url
        while (size > number && start >= listItems.get(number).getKey()) {
            number++;
        }
        //Данные для отправки
        List<T> list = new ArrayList<>();
        //Если количество найденных элементов меньше количества элементов для отображения на странице,
        //то передаем все элементы
        int max = Math.max(0, number - numberOfRepresentsItemsOnPage);
        for (int i = max; i >= 0 && i < number; i++) {
            //Получаем элемент из списка найденных личностей(person)
            T element = (T) listItems.get(i);
            //Если элемент не null - то добавляем его
            if (element != null) {
                list.add(element);
            }
        }
        //Преобразование данных из списка(list) в словарь(map)
        if (type == PersonCls.class) {
            map = ConverterListInMap.personsJSON((List<PersonCls>) list);
            if (list.size() != 0) {
                //Берем последний ключ в найденном списке
                key = ((List<PersonCls>) (list)).get(list.size() - 1).getKey();
            }
        } else if (type == AssetCls.class) {
            map = ConverterListInMap.assetsJSON((List<AssetCls>) list,dcSet, langObj);
            if (list.size() != 0) {
                //Берем последний ключ в найденном списке
                key = ((List<AssetCls>) (list)).get(list.size() - 1).getKey();
            }
        } else if (type == StatusCls.class) {
            map = ConverterListInMap.statusTemplateJSON(StatusCls.class, (List<StatusCls>) list);
            if (list.size() != 0) {
                //Берем последний ключ в найденном списке
                key = ((List<StatusCls>) (list)).get(list.size() - 1).getKey();
            }
        } else {
            logger.error("Incorrect generic type in receiver JSON");
        }
        //Добавление полученного словаря(map) в данные для отправки
        //Элемент, с которого идет отсчет страниц в обозревателе блоков(block explorer)
        if (list.size() == 0) {
            //Если элементов в списке найденных элементов нет - берем, который пришел
            key = start;
        }
    }
}
