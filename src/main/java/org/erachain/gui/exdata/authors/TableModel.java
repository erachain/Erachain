package org.erachain.gui.exdata.authors;

import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.crypto.Crypto;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.item.persons.PersonCls;
import org.erachain.core.item.persons.PersonHuman;
import org.erachain.lang.Lang;
import org.mapdb.Fun;

import javax.swing.table.DefaultTableModel;
import javax.validation.constraints.Null;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

public class TableModel extends DefaultTableModel {
    private static final int CODE_HUMAN_COL =0;
    private static final int NAME_HUMAN_COL =2;
    private static final int HEIGHT_HUMAN_COL =1;
    private static final int DESCRIPTION_COL =3;



        public TableModel(int rows) {
            super(new Object[]{Lang.getInstance().translate("Number"),
                            Lang.getInstance().translate("Вес"),
                            Lang.getInstance().translate("Name"),
                            Lang.getInstance().translate("Description")
                    },
                    rows);
            this.addRow(new Object[]{(Integer)0, "","",""});

        }

        @Override
        public boolean isCellEditable(int row, int column) {
            switch (column) {
                case CODE_HUMAN_COL:
                    return true;

                case HEIGHT_HUMAN_COL:
                    if(((String)getValueAt(row,NAME_HUMAN_COL)).equals("")) return false;
                    return true;

                case DESCRIPTION_COL:
                    if(((String)getValueAt(row,NAME_HUMAN_COL)).equals("")) return false;
                    return true;
            }

            return false;
        }

        public Class<? extends Object> getColumnClass(int c) {     // set column type
            Object o = getValueAt(0, c);
            return o == null ? Null.class : o.getClass();
        }


        public Object getValueAt(int row, int col) {

            if (this.getRowCount() < row || this.getRowCount() == 0) return null;

            return super.getValueAt(row, col);


        }

        @Override
        public void setValueAt(Object aValue, int row, int column) {
            //IF STRING
            switch (column) {
                case CODE_HUMAN_COL :
                    PersonHuman result = null;
                    int codePerson = (Integer) aValue;
                    Iterator a = this.dataVector.iterator();
                    // find duble
                    while (a.hasNext()){
                        Vector b = (Vector)a.next();
                       if ((Integer)b.get(0)==codePerson) return;

                    }
                    result = (PersonHuman) Controller.getInstance().getPerson(codePerson);
                    if (result != null ) {
                        super.setValueAt(aValue, row, column);
                        super.setValueAt(result.getName(), row, NAME_HUMAN_COL);
                        this.addRow(new Object[]{(Integer)0, "","",""});
                    }
                    return;
                case HEIGHT_HUMAN_COL:
                    super.setValueAt((String)aValue, row, column);
                    return;
                case DESCRIPTION_COL:
                    super.setValueAt((String)aValue, row, column);
                    return;
                }
              return;

        }

        public void setAuthors(PersonHuman[] Authors) {
            clearAuthors();

            for (int i = 0; i < Authors.length; ++i) {
                addRow(new Object[]{Authors[i].getKey(), "", Authors[i].getName(),Authors[i].getDescription()});
            }
        }

        public void clearAuthors() {
            while (getRowCount() > 0) {
                this.removeRow(getRowCount() - 1);
            }

            //this.addRow(new Object[]{"", ""});
        }

        public ArrayList<Integer> getAuthors() {
            if (this.getRowCount() == 0)
                return null;
            ArrayList<Integer> result = new ArrayList<Integer>();
            Iterator a = this.dataVector.iterator();
            // find duble
            while (a.hasNext()){
                Vector b = (Vector)a.next();
                result.add((Integer)b.get(0));

            }
            return result;
        }

        @Override
        public void removeRow(int row){
            super.removeRow(row);
            if (this.getRowCount()==0){
                 this.addRow(new Object[]{(Integer)0, "","",""});
            }

        }
    }

