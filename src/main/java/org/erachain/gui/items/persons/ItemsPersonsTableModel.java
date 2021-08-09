package org.erachain.gui.items.persons;

import org.erachain.core.item.persons.PersonCls;
import org.erachain.datachain.DCSet;
import org.erachain.gui.items.SearchItemsTableModel;
import org.slf4j.LoggerFactory;

@SuppressWarnings("serial")
public class ItemsPersonsTableModel extends SearchItemsTableModel {

    public static final int COLUMN_KEY = 0;
    public static final int COLUMN_NAME = 1;
    public static final int COLUMN_BORN = 2;
    public static final int COLUMN_PUBLISHER = 3;
    public static final int COLUMN_FAVORITE = 4;

    public ItemsPersonsTableModel() {
        super(DCSet.getInstance().getItemPersonMap(), new String[]{"Key", "Name", "Birthday", "Publisher", "Favorite"},
                new Boolean[]{false, true, true, false},
                COLUMN_FAVORITE);
        logger = LoggerFactory.getLogger(ItemsPersonsTableModel.class);
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

                return person;

            case COLUMN_PUBLISHER:

                return person.getMaker().getPersonAsString();

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
