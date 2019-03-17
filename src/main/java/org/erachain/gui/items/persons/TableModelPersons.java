package org.erachain.gui.items.persons;

import org.erachain.core.item.ItemCls;
import org.erachain.core.item.persons.PersonCls;
import org.erachain.datachain.DCSet;
import org.erachain.datachain.ItemPersonMap;
import org.erachain.gui.items.TableModelItemsSearch;
import org.erachain.lang.Lang;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import javax.validation.constraints.Null;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
public class TableModelPersons extends TableModelItemsSearch {

    public static final int COLUMN_KEY = 0;
    public static final int COLUMN_NAME = 1;
    public static final int COLUMN_BORN = 2;
    public static final int COLUMN_PUBLISHER = 3;
    public static final int COLUMN_FAVORITE = 4;
    static Logger LOGGER = LoggerFactory.getLogger(TableModelPersons.class.getName());
    private Boolean[] column_AutuHeight = new Boolean[]{false, true, true, false};
    private ItemPersonMap db;
    private List<ItemCls> list;
    private String filter_Name = "";
    private long key_filter = 0;

    public TableModelPersons() {
        super(new String[]{"Key", "Name", "Birthday", "Publisher", "Favorite"});
        super.COLUMN_FAVORITE = COLUMN_FAVORITE;
        db = DCSet.getInstance().getItemPersonMap();
    }

    // читаем колонки которые изменяем высоту
    public Boolean[] get_Column_AutoHeight() {

        return this.column_AutuHeight;
    }

    // устанавливаем колонки которым изменить высоту
    public void set_get_Column_AutoHeight(Boolean[] arg0) {
        this.column_AutuHeight = arg0;
    }

    @Override
    public Object getValueAt(int row, int column) {
        if (list == null || row > this.list.size() - 1) {
            return null;
        }

        PersonCls person = (PersonCls) list.get(row);

        switch (column) {
            case COLUMN_KEY:

                return person.getKey();

            case COLUMN_NAME:

                return person.getName();

            case COLUMN_PUBLISHER:

                return person.getOwner().getPersonAsString();

            case COLUMN_FAVORITE:

                return person.isFavorite();

            case COLUMN_BORN:

                // DateFormat f = new DateFormat("DD-MM-YYYY");
                // SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-YYYY");
                // return dateFormat.format( new Date(person.getBirthday()));
                return person.getBirthdayStr();

        }

        return null;
    }

}
