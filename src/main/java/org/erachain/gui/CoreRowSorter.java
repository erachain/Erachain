package org.erachain.gui;

import org.erachain.gui.models.SortedListTableModelCls;
import org.erachain.gui.models.TableModelCls;

import javax.swing.*;
import javax.swing.table.TableModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class CoreRowSorter extends RowSorter<TableModel> {

    private List<SortKey> keys;
    private SortedListTableModelCls<?, ?> model;
    private Map<Integer, Integer> indexes;

    public CoreRowSorter(SortedListTableModelCls<?, ?> model, Map<Integer, Integer> indexes) {
        this.keys = new ArrayList<SortKey>();
        this.model = model;
        this.indexes = indexes;
    }

    @Override
    public TableModel getModel() {
        return this.model;
    }

    @Override
    public void toggleSortOrder(int column) {

        if (this.indexes.containsKey(column)) {
            SortOrder order = SortOrder.ASCENDING;
            if (!this.keys.isEmpty() && this.keys.get(0).getColumn() == column && this.keys.get(0).getSortOrder().equals(SortOrder.ASCENDING)) {
                order = SortOrder.DESCENDING;
            }

            this.keys.clear();
            this.keys.add(new SortKey(column, order));
            this.model.getSortableList().sort(this.indexes.get(column), order.equals(SortOrder.DESCENDING));
        }
    }

    @Override
    public int convertRowIndexToModel(int index) {
        return index;
    }

    @Override
    public int convertRowIndexToView(int index) {
        return index;
    }

    @Override
    public List<? extends SortKey> getSortKeys() {
        return keys;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setSortKeys(List<? extends SortKey> keys) {
        this.keys = (List<SortKey>) keys;
    }

    @Override
    public int getViewRowCount() {
        return getModelRowCount();
    }

    @Override
    public int getModelRowCount() {
        return this.model.getRowCount();
    }

    @Override
    public void modelStructureChanged() {
    }

    @Override
    public void allRowsChanged() {
    }

    @Override
    public void rowsInserted(int firstRow, int endRow) {
    }

    @Override
    public void rowsDeleted(int firstRow, int endRow) {
    }

    @Override
    public void rowsUpdated(int firstRow, int endRow) {
    }

    @Override
    public void rowsUpdated(int firstRow, int endRow, int column) {
    }
}