package org.erachain.datachain;

import com.google.common.primitives.SignedBytes;
import org.mapdb.DB;

import java.util.HashMap;
import java.util.Map;

/**
 * Get the parent post for a comment (the blogpost that was commented)
 *
 * @author Skerberus
 */
public class CommentPostMap extends DCMap<byte[], byte[]> {

    private Map<Integer, Integer> observableData = new HashMap<Integer, Integer>();

    public CommentPostMap(DCSet databaseSet, DB database) {
        super(databaseSet, database);
    }

    public CommentPostMap(DCMap<byte[], byte[]> parent) {
        super(parent, null);
    }

    @Override
    protected Map<byte[], byte[]> getMap(DB database) {

        return database.createTreeMap("CommentPostMapTree")
                .comparator(SignedBytes.lexicographicalComparator())
                .makeOrGet();

    }

    public void add(byte[] signatureOfComment, byte[] signatureOfBlogPost) {
        set(signatureOfComment, signatureOfBlogPost);
    }

    public void remove(byte[] signatureOfComment) {
        delete(signatureOfComment);
    }

    @Override
    protected Map<byte[], byte[]> getMemoryMap() {
        return new HashMap<>();
    }

    @Override
    protected byte[] getDefaultValue() {
        return null;
    }

    @Override
    protected Map<Integer, Integer> getObservableData() {
        return this.observableData;
    }

    @Override
    protected void createIndexes(DB database) {
    }
}

