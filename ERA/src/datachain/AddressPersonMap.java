package datachain;

import org.mapdb.BTreeKeySerializer;
import org.mapdb.DB;
import org.mapdb.Fun.Tuple4;

import java.util.Map;
import java.util.Stack;
import java.util.TreeMap;

//import org.mapdb.Fun.Tuple2;
//import org.mapdb.Fun.Tuple3;

// address+key -> Stack person + end_date + block.height + transaction.reference
// transaction.reference = -1 allways??
// Controller.getInstance().getHeight()
public class AddressPersonMap extends DCMap<String, Stack<Tuple4<
        Long, // person key
        Integer, // end_date day
        Integer, // block height
        Integer>>> // transaction index
{
    private Map<Integer, Integer> observableData = new TreeMap<Integer, Integer>(); // icreator -HashMap

    public AddressPersonMap(DCSet databaseSet, DB database) {
        super(databaseSet, database);
    }

    public AddressPersonMap(AddressPersonMap parent) {
        super(parent, null);
    }

    protected void createIndexes(DB database) {
    }

    @Override
    protected Map<String, Stack<Tuple4<Long, Integer, Integer, Integer>>> getMap(DB database) {
        //OPEN MAP
        return database.createTreeMap("address_person")
                .keySerializer(BTreeKeySerializer.STRING)
                .counterEnable()
                .makeOrGet();
    }

    @Override
    protected Map<String, Stack<Tuple4<Long, Integer, Integer, Integer>>> getMemoryMap() {
        // HashMap ?
        return new TreeMap<String, Stack<Tuple4<Long, Integer, Integer, Integer>>>();
    }

    @Override
    protected Stack<Tuple4<Long, Integer, Integer, Integer>> getDefaultValue() {
        return new Stack<Tuple4<Long, Integer, Integer, Integer>>();
    }

    @Override
    protected Map<Integer, Integer> getObservableData() {
        return this.observableData;
    }

    ///////////////////////////////
    @SuppressWarnings("unchecked")
    public void addItem(String address, Tuple4<Long, Integer, Integer, Integer> item) {
        Stack<Tuple4<Long, Integer, Integer, Integer>> value = this.get(address);

        Stack<Tuple4<Long, Integer, Integer, Integer>> value_new;

        if (this.parent == null)
            value_new = value;
        else {
            // !!!! NEEED .clone() !!!
            // need for updates only in fork - not in parent DB
            value_new = (Stack<Tuple4<Long, Integer, Integer, Integer>>) value.clone();
        }

        value_new.push(item);

        this.set(address, value_new);

    }

    public Tuple4<Long, Integer, Integer, Integer> getItem(String address) {
        Stack<Tuple4<Long, Integer, Integer, Integer>> value = this.get(address);
        return !value.isEmpty() ? value.peek() : null;
    }

    @SuppressWarnings("unchecked")
    public void removeItem(String address) {
        Stack<Tuple4<Long, Integer, Integer, Integer>> value = this.get(address);
        if (value == null || value.isEmpty()) return;

        Stack<Tuple4<Long, Integer, Integer, Integer>> value_new;
        if (this.parent == null)
            value_new = value;
        else {
            // !!!! NEEED .clone() !!!
            // need for updates only in fork - not in parent DB
            value_new = (Stack<Tuple4<Long, Integer, Integer, Integer>>) value.clone();
        }

        value_new.pop();

        this.set(address, value_new);

    }

}
