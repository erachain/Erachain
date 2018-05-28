package gui.items.persons;

import core.item.ItemCls;
import core.item.persons.PersonCls;
import datachain.DCSet;
import datachain.ItemPersonMap;
import gui.items.TableModelItems;
import lang.Lang;
import org.apache.log4j.Logger;

import javax.validation.constraints.Null;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
public class TableModelPersons extends TableModelItems {

    public static final int COLUMN_KEY = 0;
    public static final int COLUMN_NAME = 1;
    public static final int COLUMN_BORN = 2;
    public static final int COLUMN_PUBLISHER = 3;
    public static final int COLUMN_FAVORITE = 4;
    static Logger LOGGER = Logger.getLogger(TableModelPersons.class.getName());
    private String[] columnNames = Lang.getInstance()
            .translate(new String[]{"Key", "Name", "Birthday", "Publisher", "Favorite"});// ,
    // "Favorite"});
    private Boolean[] column_AutuHeight = new Boolean[]{false, true, true, false};
    private ItemPersonMap db;
    private List<ItemCls> list;
    private String filter_Name = "";
    private long key_filter = 0;

    public TableModelPersons() {
        super.COLUMN_FAVORITE = COLUMN_FAVORITE;
        db = DCSet.getInstance().getItemPersonMap();
    }

    public void set_Filter_By_Name(String str) {
        filter_Name = str;
        list = db.get_By_Name(filter_Name, false);
        this.fireTableDataChanged();

    }

    public void clear() {
        list = new ArrayList<ItemCls>();
        this.fireTableDataChanged();

    }

    public Class<? extends Object> getColumnClass(int c) { // set column type
        Object o = getValueAt(0, c);
        return o == null ? Null.class : o.getClass();
    }

    // читаем колонки которые изменяем высоту
    public Boolean[] get_Column_AutoHeight() {

        return this.column_AutuHeight;
    }

    // устанавливаем колонки которым изменить высоту
    public void set_get_Column_AutoHeight(Boolean[] arg0) {
        this.column_AutuHeight = arg0;
    }

    public PersonCls getPerson(int row) {
        return (PersonCls) this.list.get(row);
    }

    @Override
    public ItemCls getItem(int row) {
        return this.list.get(row);
    }

    @Override
    public int getColumnCount() {
        return this.columnNames.length;
    }

    @Override
    public String getColumnName(int index) {
        return this.columnNames[index];
    }

    @Override
    public int getRowCount() {
        if (this.list == null)
            return 0;
        ;
        return this.list.size();

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


    public void Find_item_from_key(String text) {
        // TODO Auto-generated method stub
        if (text.equals("") || text == null) return;
        if (!text.matches("[0-9]*")) return;
        key_filter = new Long(text);
        list = new ArrayList<ItemCls>();
        PersonCls pers = (PersonCls) db.get(key_filter);
        if (pers == null) return;
        list.add(pers);
        try {
            this.fireTableDataChanged();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            LOGGER.info("fireTableDataChanged ?");
        }


    }
}
