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

public class TableModel extends DefaultTableModel {

    public HashMap<Integer,PersonHuman> authors;

        public TableModel(int rows) {
            super(new Object[]{Lang.getInstance().translate("Number"),
                            Lang.getInstance().translate("Вес"),
                            Lang.getInstance().translate("Name"),
                            Lang.getInstance().translate("Description")
                    },
                    rows);
            this.addRow(new Object[]{(int)0, "","",""});

        }

        @Override
        public boolean isCellEditable(int row, int column) {
            if (column == 0)
                return true;
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
            PersonHuman result = null;

                //CHECK IF NOT EMPTY
                int codePerson = (Integer) aValue;
                if (codePerson!=0) {
                    //CHECK IF LAST ROW
                    if (row == this.getRowCount() - 1) {
                        this.addRow(new Object[]{(int) 0, "", "", ""});
                    }

                    result = (PersonHuman) Controller.getInstance().getPerson(codePerson);

                    if (result == null) {
                       super.setValueAt(
                                Lang.getInstance().translate("Person not found"),
                                row, column + 2);
                    } else {
                        super.setValueAt(aValue, row, column);
                        super.setValueAt("", row, column + 1);
                        super.setValueAt(result.getName(), row, column + 2);
                        super.setValueAt(result.getDescription(), row, column + 3);
                        authors.put((Integer) aValue, result);
                    }

            } else {
                super.setValueAt(aValue, row, column);


                //CHECK IF LAST ROW
                if (row == this.getRowCount() - 1) {
                    this.addRow(new Object[]{((int) 0), "","",""});
                }
            }
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
            authors.clear();
            //this.addRow(new Object[]{"", ""});
        }

        public PersonHuman[] getAuthors() {
            if (getRowCount() == 0)
                return null;

            return (PersonHuman[]) authors.entrySet().toArray();
        }

        @Override
        public void removeRow(int row){
            super.removeRow(row);
         int  code = (int) super.getValueAt(row,0);
         authors.remove(code);

        }
    }

