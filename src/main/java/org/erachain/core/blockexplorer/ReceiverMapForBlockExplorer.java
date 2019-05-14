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
    private int page;
    private List<ItemCls> listItems;
    private int size;
    private int numberOfRepresentsItemsOnPage;
    private Map map;
    private long key;

    ReceiverMapForBlockExplorer(int page, List<ItemCls> list, int numberOfRepresentsItemsOnPage) {
        this.page = page;
        this.listItems = list;
        this.size = list.size();
        this.numberOfRepresentsItemsOnPage = numberOfRepresentsItemsOnPage;
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

    public int getPage() {
        return page;
    }

    <T> void process(Class<T> type, DCSet dcSet, JSONObject langObj) throws Exception {
        //Если начальный номер элемента не задан - берем первый
        if (page == -1) {
            page = 1;
        }
        //Данные для отправки
        List<T> list = new ArrayList<>();

        for (int i = (page - 1) * numberOfRepresentsItemsOnPage; i < page * numberOfRepresentsItemsOnPage && i < listItems.size(); i++) {
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
            map = ConverterListInMap.assetsJSON((List<AssetCls>) list, dcSet, langObj);
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
        if (list.size() == 0) {
            //Если элементов в списке найденных элементов нет - берем, который пришел
            key = page;
        }
    }
}
